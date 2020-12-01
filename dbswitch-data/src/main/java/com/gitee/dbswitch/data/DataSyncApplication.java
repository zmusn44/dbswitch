// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Data : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.data;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import com.gitee.dbswitch.data.service.MainService;

/**
 * DATA模块启动类
 * 
 * @author tang
 *
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class DataSyncApplication {

	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(DataSyncApplication.class);
		springApplication.setWebApplicationType(WebApplicationType.NONE);
		springApplication.setBannerMode(Banner.Mode.OFF);
		ApplicationContext context = springApplication.run(args);
		MainService service = context.getBean("MainService", MainService.class);
		service.run();
	}

}
