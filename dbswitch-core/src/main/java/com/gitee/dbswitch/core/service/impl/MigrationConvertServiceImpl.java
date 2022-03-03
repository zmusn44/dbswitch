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

import com.gitee.dbswitch.common.type.DBTableType;
import com.gitee.dbswitch.common.type.DatabaseTypeEnum;
import com.gitee.dbswitch.core.database.AbstractDatabase;
import com.gitee.dbswitch.core.database.DatabaseFactory;
import com.gitee.dbswitch.core.model.ColumnDescription;
import com.gitee.dbswitch.core.model.DatabaseDescription;
import com.gitee.dbswitch.core.model.SchemaTableData;
import com.gitee.dbswitch.core.model.SchemaTableMeta;
import com.gitee.dbswitch.core.model.TableDescription;
import com.gitee.dbswitch.core.service.IMigrationService;
import com.gitee.dbswitch.core.util.GenerateSqlUtils;
import com.gitee.dbswitch.core.util.JdbcUrlUtils;
import java.util.Collections;
import java.util.List;

/**
 * 结构迁移转换实现类
 * <p>
 * 备注：字段信息、主键、生成建表的SQL语句
 * <p>
 * 说明：不支持并发调用，调用方需要做并发一致性保证
 *
 * @author tang
 */
public class MigrationConvertServiceImpl implements IMigrationService {

  private static int connectTimeOut = 6;
  protected AbstractDatabase database = null;
  protected DatabaseDescription databaseDesc = null;

  @Override
  public void setDatabaseConnection(DatabaseDescription databaseDesc) {
    this.database = DatabaseFactory.getDatabaseInstance(databaseDesc.getType());
    this.databaseDesc = databaseDesc;
  }

  @Override
  public List<String> querySchemaList() {
    String jdbcUrl = JdbcUrlUtils.getJdbcUrl(
        this.databaseDesc, MigrationConvertServiceImpl.connectTimeOut);
    String username = this.databaseDesc.getUsername();
    String password = this.databaseDesc.getPassword();

    try {
      this.database.connect(jdbcUrl, username, password);
      return this.database.querySchemaList();
    } finally {
      this.database.close();
    }
  }

  @Override
  public List<TableDescription> queryTableList(String schemaName) {
    String jdbcUrl = JdbcUrlUtils.getJdbcUrl(
        this.databaseDesc, MigrationConvertServiceImpl.connectTimeOut);
    String username = this.databaseDesc.getUsername();
    String password = this.databaseDesc.getPassword();

    try {
      this.database.connect(jdbcUrl, username, password);
      return this.database.queryTableList(schemaName);
    } finally {
      this.database.close();
    }
  }

  @Override
  public String getTableDDL(String schemaName, String tableName) {
    String jdbcUrl = JdbcUrlUtils.getJdbcUrl(
        this.databaseDesc, MigrationConvertServiceImpl.connectTimeOut);
    String username = this.databaseDesc.getUsername();
    String password = this.databaseDesc.getPassword();

    try {
      this.database.connect(jdbcUrl, username, password);
      return this.database.getTableDDL(schemaName, tableName);
    } finally {
      this.database.close();
    }
  }

  @Override
  public String getViewDDL(String schemaName, String tableName) {
    String jdbcUrl = JdbcUrlUtils.getJdbcUrl(
        this.databaseDesc, MigrationConvertServiceImpl.connectTimeOut);
    String username = this.databaseDesc.getUsername();
    String password = this.databaseDesc.getPassword();

    try {
      this.database.connect(jdbcUrl, username, password);
      return this.database.getViewDDL(schemaName, tableName);
    } finally {
      this.database.close();
    }
  }

  @Override
  public List<ColumnDescription> queryTableColumnMeta(String schemaName, String tableName) {
    String jdbcUrl = JdbcUrlUtils.getJdbcUrl(
        this.databaseDesc, MigrationConvertServiceImpl.connectTimeOut);
    String username = this.databaseDesc.getUsername();
    String password = this.databaseDesc.getPassword();

    try {
      this.database.connect(jdbcUrl, username, password);
      return this.database.queryTableColumnMeta(schemaName, tableName);
    } finally {
      this.database.close();
    }
  }

  @Override
  public List<ColumnDescription> querySqlColumnMeta(String querySql) {
    String jdbcUrl = JdbcUrlUtils.getJdbcUrl(
        this.databaseDesc, MigrationConvertServiceImpl.connectTimeOut);
    String username = this.databaseDesc.getUsername();
    String password = this.databaseDesc.getPassword();

    try {
      this.database.connect(jdbcUrl, username, password);
      return this.database.querySelectSqlColumnMeta(querySql);
    } finally {
      this.database.close();
    }
  }

  @Override
  public List<String> queryTablePrimaryKeys(String schemaName, String tableName) {
    String jdbcUrl = JdbcUrlUtils.getJdbcUrl(
        this.databaseDesc, MigrationConvertServiceImpl.connectTimeOut);
    String username = this.databaseDesc.getUsername();
    String password = this.databaseDesc.getPassword();

    try {
      this.database.connect(jdbcUrl, username, password);
      return this.database.queryTablePrimaryKeys(schemaName, tableName);
    } finally {
      this.database.close();
    }
  }

  @Override
  public SchemaTableMeta queryTableMeta(String schemaName, String tableName) {
    SchemaTableMeta tableMeta = new SchemaTableMeta();
    String jdbcUrl = JdbcUrlUtils.getJdbcUrl(
        this.databaseDesc, MigrationConvertServiceImpl.connectTimeOut);
    String username = this.databaseDesc.getUsername();
    String password = this.databaseDesc.getPassword();

    List<String> pks;
    String createSql;
    TableDescription tableDesc;
    List<ColumnDescription> columns;

    try {
      this.database.connect(jdbcUrl, username, password);
      tableDesc = this.database.queryTableMeta(schemaName, tableName);
      if (null == tableDesc) {
        throw new IllegalArgumentException("Table Or View Not Exist");
      }
      columns = this.queryTableColumnMeta(schemaName, tableName);
      if (tableDesc.getTableType().equals(DBTableType.TABLE.name())) {
        pks = this.database.queryTablePrimaryKeys(schemaName, tableName);
        createSql = this.database.getTableDDL(schemaName, tableName);
      } else {
        pks = Collections.emptyList();
        createSql = this.database.getViewDDL(schemaName, tableName);
      }

      tableMeta.setSchemaName(schemaName);
      tableMeta.setTableName(tableName);
      tableMeta.setTableType(tableDesc.getTableType());
      tableMeta.setRemarks(tableDesc.getRemarks());
      tableMeta.setColumns(columns);
      tableMeta.setPrimaryKeys(pks);
      tableMeta.setCreateSql(createSql);

      return tableMeta;
    } finally {
      this.database.close();
    }
  }

  @Override
  public SchemaTableData queryTableData(String schemaName, String tableName, int rowCount) {
    String jdbcUrl = JdbcUrlUtils.getJdbcUrl(
        this.databaseDesc, MigrationConvertServiceImpl.connectTimeOut);
    String username = this.databaseDesc.getUsername();
    String password = this.databaseDesc.getPassword();

    try {
      this.database.connect(jdbcUrl, username, password);
      return this.database.queryTableData(schemaName, tableName, rowCount);
    } finally {
      this.database.close();
    }
  }

  @Override
  public void testQuerySQL(String sql) {
    String jdbcUrl = JdbcUrlUtils.getJdbcUrl(
        this.databaseDesc, MigrationConvertServiceImpl.connectTimeOut);
    String username = this.databaseDesc.getUsername();
    String password = this.databaseDesc.getPassword();

    try {
      this.database.connect(jdbcUrl, username, password);
      this.database.testQuerySQL(sql);
    } finally {
      this.database.close();
    }
  }

  @Override
  public String getDDLCreateTableSQL(DatabaseTypeEnum type, List<ColumnDescription> fieldNames,
      List<String> primaryKeys, String schemaName, String tableName, boolean autoIncr) {
    return GenerateSqlUtils.getDDLCreateTableSQL(
        type, fieldNames, primaryKeys, schemaName, tableName, autoIncr);
  }

}
