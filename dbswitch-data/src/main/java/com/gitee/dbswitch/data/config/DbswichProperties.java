// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Data : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.data.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import lombok.Data;

/**
 * Properties属性映射配置
 * 
 * @author tang
 *
 */
@Data
@ConfigurationProperties("dbswitch")
@PropertySource("classpath:config.properties")
public class DbswichProperties {

	private final List<SourceDataSourceProperties> source = new ArrayList<>();

	private final TargetDataSourceProperties target = new TargetDataSourceProperties();

	@Data
	public static class SourceDataSourceProperties {
		private String url;
		private String driverClassName;
		private String username;
		private String password;

		private Integer fetchSize;
		private String sourceSchema;
		private String sourceIncludes;
		private String sourceExcludes;
	}

	@Data
	public static class TargetDataSourceProperties {
		private String url;
		private String driverClassName;
		private String username;
		private String password;

		private String targetSchema;
		private Boolean targetDrop;
		private Boolean createTableAutoIncrement;
		private Boolean writerEngineInsert;
		private Boolean changeDataSynch;
	}
}
