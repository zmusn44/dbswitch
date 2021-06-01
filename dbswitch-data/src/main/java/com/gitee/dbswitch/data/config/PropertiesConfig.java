// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.data.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.gitee.dbswitch.core.service.IMetaDataService;
import com.gitee.dbswitch.core.service.impl.MigrationMetaDataServiceImpl;

/**
 * 配置类
 * 
 * @author tang
 *
 */
@Configuration
public class PropertiesConfig {
	
	@Bean
	public IMetaDataService getMetaDataService() {
		return new MigrationMetaDataServiceImpl();
	}
}
