// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Data : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.webapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.gitee.dbswitch.sql.service.ISqlConvertService;
import com.gitee.dbswitch.sql.service.ISqlGeneratorService;
import com.gitee.dbswitch.sql.service.impl.CalciteSqlConvertServiceImpl;
import com.gitee.dbswitch.sql.service.impl.MyselfSqlGeneratorServiceImpl;

@Configuration
public class AdapterBeanConfiguration {

	@Bean("SqlConvertService")
	public ISqlConvertService getSqlConvertService() {
		return new CalciteSqlConvertServiceImpl();
	}
	
	@Bean("SqlGeneratorService")
	public ISqlGeneratorService getSqlGeneratorService() {
		return new MyselfSqlGeneratorServiceImpl();
	}
	
}
