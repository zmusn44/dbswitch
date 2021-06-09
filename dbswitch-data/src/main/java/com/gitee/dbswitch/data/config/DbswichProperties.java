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

import java.util.ArrayList;
import java.util.List;

import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Properties属性映射配置
 * 
 * @author tang
 *
 */
@Configuration
@Data
@ToString
@ConfigurationProperties(prefix = "dbswitch", ignoreInvalidFields=false, ignoreUnknownFields = false)
@PropertySource("classpath:config.properties")
public class DbswichProperties {

	private List<SourceDataSourceProperties> source = new ArrayList<>();

	private TargetDataSourceProperties target = new TargetDataSourceProperties();

	@Data
	@NoArgsConstructor
	public static class SourceDataSourceProperties {
		private String url;
		private String driverClassName;
		private String username;
		private String password;

		private Integer fetchSize=5000;
		private String sourceSchema="";
		private String prefixTable="";
		private String sourceIncludes="";
		private String sourceExcludes="";
	}

	@Data
	@NoArgsConstructor
	public static class TargetDataSourceProperties {
		private String url;
		private String driverClassName;
		private String username;
		private String password;

		private String targetSchema="";
		private Boolean targetDrop=Boolean.TRUE;
		private Boolean createTableAutoIncrement=Boolean.FALSE;
		private Boolean writerEngineInsert=Boolean.FALSE;
		private Boolean changeDataSynch=Boolean.FALSE;
	}
}
