package com.yofish.apollo.repository;

import com.yofish.apollo.domain.App;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

/**
 * Created on 2018/2/5.
 *
 * @author zlf
 * @since 1.0
 */
@Component
public interface AppRepository extends JpaRepository<App, Long> {


    App findByAppCode(String appId);


    Page<App> findByAppCodeContainingOrNameContaining(String appId, String name, Pageable pageable);
}
