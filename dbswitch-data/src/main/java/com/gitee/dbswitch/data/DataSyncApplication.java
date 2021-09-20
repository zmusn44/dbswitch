// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.data;

import com.gitee.dbswitch.data.config.DbswichProperties;
import com.gitee.dbswitch.data.core.MainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * DATA模块启动类
 *
 * @author tang
 */
@Slf4j
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class DataSyncApplication {

  public static void main(String[] args) {
    SpringApplication springApplication = new SpringApplication(DataSyncApplication.class);
    springApplication.setWebApplicationType(WebApplicationType.NONE);
    springApplication.setBannerMode(Banner.Mode.OFF);
    ConfigurableApplicationContext applicationContext = springApplication.run(args);
    try {
      DbswichProperties properties = applicationContext.getBean(DbswichProperties.class);
      MainService mainService = new MainService(properties);
      mainService.run();
    } catch (Exception e) {
      log.error("error:", e);
    } finally {
      applicationContext.close();
    }

  }

}
