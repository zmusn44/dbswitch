// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.dbcommon.database.impl;

import java.util.List;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import com.gitee.dbswitch.dbcommon.database.AbstractDatabaseOperator;
import com.gitee.dbswitch.dbcommon.database.IDatabaseOperator;
import com.gitee.dbswitch.dbcommon.pojo.StatementResultSet;

/**
 * PostgreSQL数据库实现类
 * 
 * @author tang
 *
 */
public class PostgreSqlDatabaseOperator extends AbstractDatabaseOperator implements IDatabaseOperator {

	public PostgreSqlDatabaseOperator(DataSource dataSource) {
		super(dataSource);
	}

	@Override
	public String getSelectTableSql(String schemaName, String tableName, List<String> fields) {
		return String.format("select \"%s\" from \"%s\".\"%s\" ", StringUtils.join(fields, "\",\""), schemaName,
				tableName);
	}

	@Override
	public StatementResultSet queryTableData(String schemaName, String tableName, List<String> fields,
			List<String> orders) {
		String sql = String.format("select \"%s\" from \"%s\".\"%s\" order by \"%s\" asc ",
				StringUtils.join(fields, "\",\""), schemaName, tableName, StringUtils.join(orders, "\",\""));
		return this.selectTableData(sql, this.fetchSize);
	}

	@Override
	public StatementResultSet queryTableData(String schemaName, String tableName, List<String> fields) {
		String sql = String.format("select \"%s\" from \"%s\".\"%s\" ", StringUtils.join(fields, "\",\""), schemaName,
				tableName);
		return this.selectTableData(sql, this.fetchSize);
	}

	@Override
	public void truncateTableData(String schemaName, String tableName) {
		String sql = String.format("TRUNCATE TABLE \"%s\".\"%s\" RESTART IDENTITY ", schemaName, tableName);
		this.executeSql(sql);
	}

	@Override
	public void dropTable(String schemaName, String tableName) {
		String sql = String.format("DROP TABLE \"%s\".\"%s\" cascade ", schemaName, tableName);
		this.executeSql(sql);
	}
}
