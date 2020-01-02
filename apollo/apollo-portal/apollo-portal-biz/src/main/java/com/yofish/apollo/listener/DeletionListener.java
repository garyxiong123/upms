//package com.yofish.apollo.listener;
//
//import com.ctrip.framework.apollo.common.dto.AppDTO;
//import com.ctrip.framework.apollo.common.dto.AppNamespaceDTO;
//import com.ctrip.framework.apollo.common.utils.BeanUtils;
//import com.ctrip.framework.apollo.core.enums.Env;
//import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
//import com.ctrip.framework.apollo.portal.component.PortalSettings;
//import com.ctrip.framework.apollo.tracer.Tracer;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.event.EventListener;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Component
//public class DeletionListener {
//
//  private static final Logger logger = LoggerFactory.getLogger(DeletionListener.class);
//
//  @Autowired
//  private PortalSettings portalSettings;
//  @Autowired
//  private AdminServiceAPI.AppAPI appAPI;
//  @Autowired
//  private AdminServiceAPI.NamespaceAPI namespaceAPI;
//
//  @EventListener
//  public void onAppDeletionEvent(AppDeletionEvent event) {
//    AppDTO appDTO = BeanUtils.transfrom(AppDTO.class, event.getApp());
//    String appCode = appDTO.getAppId();
//    String operator = appDTO.getDataChangeLastModifiedBy();
//
//    List<Env> envs = portalSettings.getActiveEnvs();
//    for (Env env : envs) {
//      try {
//        appAPI.deleteApp(env, appCode, operator);
//      } catch (Throwable e) {
//        logger.error("Delete app failed. Env = {}, AppId = {}", env, appCode, e);
//        Tracer.logError(String.format("Delete app failed. Env = %s, AppId = %s", env, appCode), e);
//      }
//    }
//  }
//
//  @EventListener
//  public void onAppNamespaceDeletionEvent(AppNamespaceDeletionEvent event) {
//    AppNamespaceDTO appNamespace = BeanUtils.transfrom(AppNamespaceDTO.class, event.getAppNamespace());
//    List<Env> envs = portalSettings.getActiveEnvs();
//    String appCode = appNamespace.getAppId();
//    String namespaceName = appNamespace.getName();
//    String operator = appNamespace.getDataChangeLastModifiedBy();
//
//    for (Env env : envs) {
//      try {
//        namespaceAPI.deleteAppNamespace(env, appCode, namespaceName, operator);
//      } catch (Throwable e) {
//        logger.error("Delete appNamespace failed. appCode = {}, appNamespace = {}, env = {}", appCode, namespaceName, env, e);
//        Tracer.logError(String
//            .format("Delete appNamespace failed. appCode = %s, appNamespace = %s, env = %s", appCode, namespaceName, env), e);
//      }
//    }
//  }
//}
