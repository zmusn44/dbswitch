// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Data : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.weishao.dbswitch.dbcommon.util;

import javax.sql.DataSource;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;

/**
 * 数据库类型识别工具类
 * 
 * @author tang
 *
 */
public final class DatabaseAwareUtils {

	/**
	 * 私有化构造函数，阻断继承
	 */
	private DatabaseAwareUtils() {
	}

	/**
	 * 获取数据库的产品名
	 * 
	 * @param dataSource 数据源
	 * @return 数据库产品名称字符串
	 */
	public static final String getDatabaseNameByDataSource(DataSource dataSource) {
		try {
			String productName = JdbcUtils.commonDatabaseName(
					JdbcUtils.extractDatabaseMetaData(dataSource, "getDatabaseProductName").toString());
			if (productName.equalsIgnoreCase("Greenplum")) {
				return "greenplum";
			} else if (productName.equalsIgnoreCase("Microsoft SQL Server")) {
				return "sqlserver";
			}

			DatabaseDriver databaseDriver = DatabaseDriver.fromProductName(productName);
			if (databaseDriver == DatabaseDriver.UNKNOWN) {
				throw new IllegalStateException("Unable to detect database type from data source instance");
			}
			return databaseDriver.getId();
		} catch (MetaDataAccessException ex) {
			throw new IllegalStateException("Unable to detect database type", ex);
		}

	}

	/**
	 * 判断数据源是否为MySQL数据源
	 * 
	 * @param dataSource 数据源
	 * @return 当数据源为MySQL时，返回true，否则为false
	 */
	public static final boolean isUsingMySQL(DataSource dataSource) {
		return getDatabaseNameByDataSource(dataSource).equalsIgnoreCase("mysql");
	}
}
