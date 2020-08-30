// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Data : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.weishao.dbswitch.core.service.impl;

import java.util.List;
import java.util.Objects;
import com.weishao.dbswitch.common.constant.DatabaseTypeEnum;
import com.weishao.dbswitch.core.database.AbstractDatabase;
import com.weishao.dbswitch.core.database.DatabaseFactory;
import com.weishao.dbswitch.core.model.ColumnDescription;
import com.weishao.dbswitch.core.model.TableDescription;
import com.weishao.dbswitch.core.service.IMetaDataService;
import com.weishao.dbswitch.core.util.GenerateSqlUtils;

/**
 * 元信息数据迁移实现类
 * 
 * @author tang
 *
 */
public class MigrationMetaDataServiceImpl implements IMetaDataService {

	protected AbstractDatabase database = null;

	@Override
	public void setDatabaseConnection(DatabaseTypeEnum dbtype) {
		this.database = DatabaseFactory.getDatabaseInstance(dbtype);
	}

	@Override
	public List<String> querySchemaList(String jdbcUrl, String username, String password) {
		AbstractDatabase db = Objects.requireNonNull(this.database, "Please call setDatabaseConnection() first!");
		try {
			db.connect(jdbcUrl, username, password);
			return db.querySchemaList();
		} finally {
			db.close();
		}
	}

	@Override
	public List<TableDescription> queryTableList(String jdbcUrl, String username, String password, String schemaName) {
		AbstractDatabase db = Objects.requireNonNull(this.database, "Please call setDatabaseConnection() first!");
		try {
			db.connect(jdbcUrl, username, password);
			return db.queryTableList(schemaName);
		} finally {
			db.close();
		}
	}

	@Override
	public List<ColumnDescription> queryTableColumnMeta(String jdbcUrl, String username, String password,
			String schemaName, String tableName) {
		AbstractDatabase db = Objects.requireNonNull(this.database, "Please call setDatabaseConnection() first!");
		try {
			db.connect(jdbcUrl, username, password);
			return db.queryTableColumnMeta(schemaName, tableName);
		} finally {
			db.close();
		}
	}

	@Override
	public List<ColumnDescription> querySqlColumnMeta(String jdbcUrl, String username, String password,
			String querySql) {
		AbstractDatabase db = Objects.requireNonNull(this.database, "Please call setDatabaseConnection() first!");
		try {
			db.connect(jdbcUrl, username, password);
			return db.querySelectSqlColumnMeta(querySql);
		} finally {
			db.close();
		}
	}

	@Override
	public List<String> queryTablePrimaryKeys(String jdbcUrl, String username, String password, String schemaName,
			String tableName) {
		AbstractDatabase db = Objects.requireNonNull(this.database, "Please call setDatabaseConnection() first!");
		try {
			db.connect(jdbcUrl, username, password);
			return db.queryTablePrimaryKeys(schemaName, tableName);
		} finally {
			db.close();
		}
	}

	@Override
	public void testQuerySQL(String jdbcUrl, String username, String password, String sql) {
		AbstractDatabase db = Objects.requireNonNull(this.database, "Please call setDatabaseConnection() first!");
		try {
			db.connect(jdbcUrl, username, password);
			db.testQuerySQL(sql);
		} finally {
			db.close();
		}
	}

	@Override
	public String getDDLCreateTableSQL(DatabaseTypeEnum type, List<ColumnDescription> fieldNames, List<String> primaryKeys,
			String schemaName, String tableName, boolean autoIncr) {
		return GenerateSqlUtils.getDDLCreateTableSQL(type, fieldNames, primaryKeys, schemaName, tableName, autoIncr);
	}
}
