// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.dbcommon.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.sql.DataSource;
import org.springframework.jdbc.support.JdbcUtils;

/**
 * 基于JDBC的数据库元数据查询工具类
 * 
 * @author tang
 *
 */
public class JdbcMetaDataUtils {

	protected DataSource dataSource;

	public JdbcMetaDataUtils(DataSource dataSource) {
		this.dataSource = Objects.requireNonNull(dataSource);
	}

	/**
	 * 获取指定模式表的字段列表
	 * 
	 * @param schemaName 模式名称
	 * @param tableName  表名称
	 * @return 字段元信息列表
	 */
	public List<String> queryTableColumnName(String schemaName, String tableName) {
		Set<String> result = new HashSet<String>();
		Connection connection = null;

		try {
			connection = this.dataSource.getConnection();
			DatabaseMetaData medaData = connection.getMetaData();
			ResultSet columns = medaData.getColumns(null, schemaName, tableName, null);
			while (columns.next()) {
				String columnName = columns.getString("COLUMN_NAME");
				result.add(columnName);
			}
			return new ArrayList<>(result);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		} finally {
			JdbcUtils.closeConnection(connection);
		}

	}

	/**
	 * 获取指定模式表的主键字段列表
	 * 
	 * @param schemaName 模式名称
	 * @param tableName  表名称
	 * @return 主键字段名称列表
	 */
	public List<String> queryTablePrimaryKeys(String schemaName, String tableName) {
		Set<String> result = new HashSet<>();
		Connection connection = null;

		try {
			connection = this.dataSource.getConnection();
			DatabaseMetaData medaData = connection.getMetaData();
			ResultSet columns = medaData.getPrimaryKeys(null, schemaName, tableName);
			while (columns.next()) {
				String columnName = columns.getString("COLUMN_NAME");
				result.add(columnName);
			}
			return new ArrayList<>(result);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		} finally {
			JdbcUtils.closeConnection(connection);
		}
	}

}
