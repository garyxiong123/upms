package com.yofish.yyconfig.client.timer;

import com.google.common.util.concurrent.RateLimiter;
import com.yofish.yyconfig.client.component.exceptions.ApolloConfigException;
import com.yofish.yyconfig.client.component.util.ExceptionUtil;
import com.yofish.yyconfig.client.lifecycle.preboot.inject.ApolloInjector;
import com.yofish.yyconfig.client.lifecycle.preboot.internals.ClientConfig;
import com.yofish.yyconfig.client.lifecycle.preboot.internals.ConfigServiceLocator;
import com.yofish.yyconfig.client.repository.RemoteConfigRepository;
import com.yofish.yyconfig.common.framework.apollo.core.utils.ApolloThreadFactory;
import com.yofish.yyconfig.common.framework.apollo.tracer.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 定时任务： 客户端 建立config端的长连接:  b
 */
@Slf4j
public class VersionMonitor4Namespace {
    private static final Logger logger = LoggerFactory.getLogger(VersionMonitor4Namespace.class);
    //90 seconds, should be longer than server side's long polling timeout, which is now 60 seconds

    private final ExecutorService m_longPollingService;
    private final AtomicBoolean m_longPollingStopped;

    private RateLimiter m_longPollRateLimiter;
    private final AtomicBoolean m_longPollStarted;

    private ClientConfig clientConfig;

    private ConfigServiceLocator m_serviceLocator;
    private Client client;

    /**
     * Constructor.
     */
    public VersionMonitor4Namespace() {
        m_longPollingStopped = new AtomicBoolean(false);
        m_longPollingService = Executors.newSingleThreadExecutor(ApolloThreadFactory.create("VersionMonitor4Namespace", true));
        m_longPollStarted = new AtomicBoolean(false);

        clientConfig = ApolloInjector.getInstance(ClientConfig.class);

        m_serviceLocator = ApolloInjector.getInstance(ConfigServiceLocator.class);
        m_longPollRateLimiter = RateLimiter.create(clientConfig.getLongPollQPS());
        client = clientConfig.getOrCreateClient();

    }

    /**
     * 添加到版本变更控制
     *
     * @param namespace
     * @param remoteConfigRepository
     * @return
     */
    public boolean add2VersionControl(String namespace, RemoteConfigRepository remoteConfigRepository) {
        boolean added = client.addConfigRepository(namespace, remoteConfigRepository);

        if (!m_longPollStarted.get()) {
            startLongPollRemoteConfig();
        }
        return added;
    }

    private void startLongPollRemoteConfig() {
        if (!m_longPollStarted.compareAndSet(false, true)) {
            //already started
            return;
        }
        try {


            final long longPollingInitialDelayInMills = clientConfig.getLongPollingInitialDelayInMills();
            m_longPollingService.submit(() -> {
                if (longPollingInitialDelayInMills > 0) {
                    try {
                        logger.debug("Long polling will start in {} ms.", longPollingInitialDelayInMills);
                        TimeUnit.MILLISECONDS.sleep(longPollingInitialDelayInMills);
                    } catch (InterruptedException e) {
                    }
                }
                while (!m_longPollingStopped.get() && !Thread.currentThread().isInterrupted()) {
                    if (!m_longPollRateLimiter.tryAcquire(5, TimeUnit.SECONDS)) {
                        //wait at most 5 seconds
                        try {
                            TimeUnit.SECONDS.sleep(5);
                        } catch (InterruptedException e) {
                        }
                    }
                    client.nsVersionCompareAndSyncConfig();
                }
            });
        } catch (Throwable ex) {
            m_longPollStarted.set(false);
            ApolloConfigException exception = new ApolloConfigException("Schedule long polling refresh failed", ex);
            Tracer.logError(exception);
            logger.warn(ExceptionUtil.getDetailMessage(exception));
        }
    }

    void stopLongPollingRefresh() {
        this.m_longPollingStopped.compareAndSet(false, true);
    }


}
