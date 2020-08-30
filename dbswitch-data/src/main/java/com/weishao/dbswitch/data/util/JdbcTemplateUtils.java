// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Data : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.weishao.dbswitch.data.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;
import com.weishao.dbswitch.common.constant.DatabaseTypeEnum;

/**
 * JdbcTemplate包制使用工具类型
 * 
 * @author tang
 *
 */
public final class JdbcTemplateUtils {

	private JdbcTemplateUtils() {

	}

	/**
	 * 获取数据库类型
	 * 
	 * @param dataSource 数据源
	 * @return DatabaseType 数据库类型
	 */
	public static DatabaseTypeEnum getDatabaseProduceName(BasicDataSource dataSource) {
		String driverClassName = dataSource.getDriverClassName();
		if (driverClassName.contains("mysql")) {
			return DatabaseTypeEnum.MYSQL;
		} else if (driverClassName.contains("oracle")) {
			return DatabaseTypeEnum.ORACLE;
		} else if (driverClassName.contains("postgresql")) {
			return DatabaseTypeEnum.POSTGRESQL;
		} else if (driverClassName.contains("Greenplum")) {
			return DatabaseTypeEnum.GREENPLUM;
		} else if (driverClassName.contains("sqlserver")) {
			return DatabaseTypeEnum.SQLSERVER;
		} else {
			throw new RuntimeException(
					String.format("Unsupport database type by driver class name [%s]", driverClassName));
		}

	}

	/**
	 * 获取表字段的元信息
	 * 
	 * @param sourceJdbcTemplate
	 * @param fullTableName      表的全名
	 * @return
	 */
	public static Map<String, Integer> getColumnMetaData(JdbcTemplate sourceJdbcTemplate, String fullTableName) {
		final String sql = String.format("select * from %s where 1=2", fullTableName);
		Map<String, Integer> columnMetaData = new HashMap<String, Integer>();
		Boolean ret = sourceJdbcTemplate.execute(new ConnectionCallback<Boolean>() {

			@Override
			public Boolean doInConnection(Connection conn) throws SQLException, DataAccessException {
				Statement stmt = null;
				ResultSet rs = null;
				try {
					stmt = conn.createStatement();
					rs = stmt.executeQuery(sql);
					ResultSetMetaData rsMetaData = rs.getMetaData();
					for (int i = 0, len = rsMetaData.getColumnCount(); i < len; i++) {
						columnMetaData.put(rsMetaData.getColumnName(i + 1), rsMetaData.getColumnType(i + 1));
					}
					return true;
				} catch (Exception e) {
					throw new RuntimeException(String.format("获取表:%s 的字段的元信息时失败. 请联系 DBA 核查该库、表信息.", fullTableName), e);
				} finally {
					JdbcUtils.closeResultSet(rs);
					JdbcUtils.closeStatement(stmt);
				}
			}
		});

		if (ret.booleanValue()) {
			return columnMetaData;
		}

		return null;
	}

}
