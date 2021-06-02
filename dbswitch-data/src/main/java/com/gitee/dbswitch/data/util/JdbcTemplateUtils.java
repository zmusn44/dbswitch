// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.data.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;
import com.gitee.dbswitch.common.constant.DatabaseTypeEnum;
import com.gitee.dbswitch.dbcommon.util.DatabaseAwareUtils;

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
	public static DatabaseTypeEnum getDatabaseProduceName(DataSource dataSource) {
		String productName = DatabaseAwareUtils.getDatabaseNameByDataSource(dataSource);
		if (productName.equalsIgnoreCase("Greenplum")) {
			return DatabaseTypeEnum.GREENPLUM;
		} else if (productName.equalsIgnoreCase("SQLServer")) {
			return DatabaseTypeEnum.SQLSERVER;
		} else if (productName.equalsIgnoreCase("DM")) {
			return DatabaseTypeEnum.DM;
		} else if (productName.equalsIgnoreCase("Kingbase")) {
			return DatabaseTypeEnum.KINGBASE;
		} else {
			DatabaseDriver databaseDriver = DatabaseDriver.fromProductName(productName);
			if (DatabaseDriver.MARIADB == databaseDriver) {
				return DatabaseTypeEnum.MARIADB;
			} else if (DatabaseDriver.MYSQL == databaseDriver) {
				return DatabaseTypeEnum.MYSQL;
			} else if (DatabaseDriver.ORACLE == databaseDriver) {
				return DatabaseTypeEnum.ORACLE;
			} else if (DatabaseDriver.POSTGRESQL == databaseDriver) {
				return DatabaseTypeEnum.POSTGRESQL;
			} else if (DatabaseDriver.DB2 == databaseDriver) {
				return DatabaseTypeEnum.DB2;
			} else {
				throw new RuntimeException(String.format("Unsupport database type by product name [%s]", productName));
			}
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
