// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Data : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.weishao.dbswitch.dbsynch.mssql;

import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import com.weishao.dbswitch.dbsynch.AbstractDatabaseSynchronize;
import com.weishao.dbswitch.dbsynch.IDatabaseSynchronize;

/**
 * SQLServer数据库实现类
 * 
 * @author tang
 *
 */
public class SqlServerDatabaseSynchImpl extends AbstractDatabaseSynchronize implements IDatabaseSynchronize {

	public SqlServerDatabaseSynchImpl(DataSource ds) {
		super(ds);
	}

	@Override
	public String getColumMetaDataSql(String schemaName, String tableName) {
		return String.format("SELECT * FROM [%s].[%s] WHERE 1=2", schemaName, tableName);
	}

	@Override
	public String getInsertPrepareStatementSql(String schemaName, String tableName, List<String> fieldNames) {
		List<String> placeHolders = new ArrayList<String>();
		for (int i = 0; i < fieldNames.size(); ++i) {
			placeHolders.add("?");
		}

		return String.format("INSERT INTO [%s].[%s] ( [%s] ) VALUES ( %s )", schemaName, tableName,
				StringUtils.join(fieldNames, "],["), StringUtils.join(placeHolders, ","));
	}

	@Override
	public String getUpdatePrepareStatementSql(String schemaName, String tableName, List<String> fieldNames,
			List<String> pks) {
		List<String> uf = new ArrayList<String>();
		for (String field : fieldNames) {
			if (!pks.contains(field)) {
				uf.add(String.format("[%s]=?", field));
			}
		}

		List<String> uw = new ArrayList<String>();
		for (String pk : pks) {
			uw.add(String.format("[%s]=?", pk));
		}

		return String.format("UPDATE [%s].[%s] SET %s WHERE %s", schemaName, tableName, StringUtils.join(uf, " , "),
				StringUtils.join(uw, " AND "));
	}

	@Override
	public String getDeletePrepareStatementSql(String schemaName, String tableName, List<String> pks) {
		List<String> uw = new ArrayList<String>();
		for (String pk : pks) {
			uw.add(String.format("[%s]=?", pk));
		}

		return String.format("DELETE FROM [%s].[%s] WHERE %s ", schemaName, tableName, StringUtils.join(uw, "  AND  "));
	}

}
