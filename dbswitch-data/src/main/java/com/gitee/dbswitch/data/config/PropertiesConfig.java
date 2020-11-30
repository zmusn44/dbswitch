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
import java.util.Arrays;
import java.util.List;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import com.gitee.dbswitch.core.service.IMetaDataService;
import com.gitee.dbswitch.core.service.impl.MigrationMetaDataServiceImpl;

/**
 * 配置数据属性配置类
 * 
 * @author tang
 *
 */
@Configuration
@PropertySource("classpath:config.properties")
public class PropertiesConfig {

	@Value("${source.datasource.url}")
	public String dbSourceJdbcUrl;
	
	@Value("${source.datasource.driver-class-name}")
	public String dbSourceClassName;

	@Value("${source.datasource.username}")
	public String dbSourceUserName;

	@Value("${source.datasource.password}")
	public String dbSourcePassword;
	
	@Value("${target.datasource.url}")
	public String dbTargetJdbcUrl;
	
	@Value("${target.datasource.driver-class-name}")
	public String dbTargetClassName;

	@Value("${target.datasource.username}")
	public String dbTargetUserName;

	@Value("${target.datasource.password}")
	public String dbTargetPassword;

	/////////////////////////////////////////////

	@Value("${source.datasource-fetch.size}")
	public int fetchSizeSource;

	@Value("${source.datasource-source.schema}")
	private String schemaNameSource;
	
	public List<String> getSourceSchemaNames() {
		if (!Strings.isEmpty(schemaNameSource)) {
			String[] strs = schemaNameSource.split(",");
			if (strs.length > 0) {
				return new ArrayList<>(Arrays.asList(strs));
			}
		}

		return new ArrayList<>();
	}

	@Value("${source.datasource-source.includes}")
	private String tableNameIncludesSource;
	
	@Value("${source.datasource-source.excludes}")
	private String tableNameExcludesSource;
	
	public List<String> getSourceTableNameIncludes() {
		if (!Strings.isEmpty(tableNameIncludesSource)) {
			String[] strs = tableNameIncludesSource.split(",");
			if (strs.length > 0) {
				return new ArrayList<>(Arrays.asList(strs));
			}
		}

		return new ArrayList<>();
	}

	public List<String> getSourceTableNameExcludes() {
		if (!Strings.isEmpty(tableNameExcludesSource)) {
			String[] strs = tableNameExcludesSource.split(",");
			if (strs.length > 0) {
				return new ArrayList<>(Arrays.asList(strs));
			}
		}

		return new ArrayList<>();
	}

	////////////////////////////////////////////

	@Value("${target.datasource-target.schema}")
	public String dbTargetSchema;

	@Value("${target.datasource-target.drop}")
	public Boolean dropTargetTable;

	@Value("${target.create-table.auto-increment}")
	public Boolean createSupportAutoIncr;

	@Value("${target.writer-engine.insert}")
	public Boolean engineInsert;

	@Value("${target.change-data-synch}")
	public Boolean changeSynch;

	////////////////////////////////////////////

	@Bean(name="sourceDataSource")
	@Qualifier("sourceDataSource")
	@ConfigurationProperties(prefix="source.datasource")
	public BasicDataSource sourceDataSource() {
		return DataSourceBuilder.create().type(BasicDataSource.class).build();
	}
	
	@Bean(name="targetDataSource")
	@Qualifier("targetDataSource")
	@ConfigurationProperties(prefix="target.datasource")
	public BasicDataSource targetDataSource() {
		return DataSourceBuilder.create().type(BasicDataSource.class).build();
	}

	@Bean(name = "sourceJdbcTemplate")
	public JdbcTemplate sourceJdbcTemplate(@Qualifier("sourceDataSource") BasicDataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	@Bean(name = "targetJdbcTemplate")
	public JdbcTemplate target(@Qualifier("targetDataSource") BasicDataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	@Bean
	public IMetaDataService getMetaDataService() {
		return new MigrationMetaDataServiceImpl();
	}
	
}
