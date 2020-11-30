// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Data : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.core.service.impl;

import java.util.List;
import com.gitee.dbswitch.common.constant.DatabaseTypeEnum;
import com.gitee.dbswitch.core.database.AbstractDatabase;
import com.gitee.dbswitch.core.database.DatabaseFactory;
import com.gitee.dbswitch.core.model.ColumnDescription;
import com.gitee.dbswitch.core.model.DatabaseDescription;
import com.gitee.dbswitch.core.model.TableDescription;
import com.gitee.dbswitch.core.service.IMigrationService;
import com.gitee.dbswitch.core.util.GenerateSqlUtils;
import com.gitee.dbswitch.core.util.JdbcUrlUtils;

/**
 * 结构迁移转换实现类
 * 备注：字段信息、主键、生成建表的SQL语句
 * 说明：不支持并发调用，调用方需要做并发一致性保证
 * 
 * @author tang
 *
 */
public class MigrationConvertServiceImpl implements IMigrationService {

	private static int connectTimeOut=6;
	protected AbstractDatabase database=null;
	protected DatabaseDescription databaseDesc=null;
	
	@Override
	public void setDatabaseConnection(DatabaseDescription databaseDesc) {
		this.database=DatabaseFactory.getDatabaseInstance(databaseDesc.getType());
		this.databaseDesc=databaseDesc;
	}
	
	@Override
	public List<String> querySchemaList() {
		String jdbcUrl=JdbcUrlUtils.getJdbcUrl(this.databaseDesc,MigrationConvertServiceImpl.connectTimeOut);
		String username=this.databaseDesc.getUsername();
		String password=this.databaseDesc.getPassword();

		try {
			this.database.connect(jdbcUrl, username, password);
			return this.database.querySchemaList();
		}finally {
			this.database.close();
		}
	}

	@Override
	public List<TableDescription> queryTableList(String schemaName) {
		String jdbcUrl=JdbcUrlUtils.getJdbcUrl(this.databaseDesc,MigrationConvertServiceImpl.connectTimeOut);
		String username=this.databaseDesc.getUsername();
		String password=this.databaseDesc.getPassword();

		try {
			this.database.connect(jdbcUrl, username, password);
			return this.database.queryTableList(schemaName);
		}finally {
			this.database.close();
		}
	}

	@Override
	public List<ColumnDescription> queryTableColumnMeta(String schemaName, String tableName) {
		String jdbcUrl=JdbcUrlUtils.getJdbcUrl(this.databaseDesc,MigrationConvertServiceImpl.connectTimeOut);
		String username=this.databaseDesc.getUsername();
		String password=this.databaseDesc.getPassword();

		try {
			this.database.connect(jdbcUrl, username, password);
			return this.database.queryTableColumnMeta(schemaName, tableName);
		}finally {
			this.database.close();
		}
	}
	
	@Override
	public List<ColumnDescription> querySqlColumnMeta(String querySql){
		String jdbcUrl=JdbcUrlUtils.getJdbcUrl(this.databaseDesc,MigrationConvertServiceImpl.connectTimeOut);
		String username=this.databaseDesc.getUsername();
		String password=this.databaseDesc.getPassword();

		try {
			this.database.connect(jdbcUrl, username, password);
			return this.database.querySelectSqlColumnMeta(querySql);
		}finally {
			this.database.close();
		}
	}

	@Override
	public List<String> queryTablePrimaryKeys(String schemaName, String tableName) {
		String jdbcUrl=JdbcUrlUtils.getJdbcUrl(this.databaseDesc,MigrationConvertServiceImpl.connectTimeOut);
		String username=this.databaseDesc.getUsername();
		String password=this.databaseDesc.getPassword();

		try {
			this.database.connect(jdbcUrl, username, password);
			return this.database.queryTablePrimaryKeys(schemaName, tableName);
		}finally {
			this.database.close();
		}
	}

	@Override
	public void testQuerySQL(String sql) {
		String jdbcUrl=JdbcUrlUtils.getJdbcUrl(this.databaseDesc,MigrationConvertServiceImpl.connectTimeOut);
		String username=this.databaseDesc.getUsername();
		String password=this.databaseDesc.getPassword();

		try {
			this.database.connect(jdbcUrl, username, password);
			this.database.testQuerySQL(sql);
		}finally {
			this.database.close();
		}
	}

	@Override
	public String getDDLCreateTableSQL(DatabaseTypeEnum type, List<ColumnDescription> fieldNames, List<String> primaryKeys,
			String schemaName, String tableName, boolean autoIncr) {
		return GenerateSqlUtils.getDDLCreateTableSQL(type, fieldNames, primaryKeys, schemaName, tableName, autoIncr);
	}

}
