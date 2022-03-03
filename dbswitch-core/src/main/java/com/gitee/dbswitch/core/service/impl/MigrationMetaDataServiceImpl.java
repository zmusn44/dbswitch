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
import com.gitee.dbswitch.core.model.SchemaTableData;
import com.gitee.dbswitch.core.model.SchemaTableMeta;
import com.gitee.dbswitch.core.model.TableDescription;
import com.gitee.dbswitch.core.service.IMetaDataService;
import com.gitee.dbswitch.core.util.GenerateSqlUtils;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 元信息数据迁移实现类
 *
 * @author tang
 */
public class MigrationMetaDataServiceImpl implements IMetaDataService {

  private final static String MESSAGE_ERROR_INFO = "Please call setDatabaseConnection() first!";

  protected AbstractDatabase database;

  @Override
  public void setDatabaseConnection(DatabaseTypeEnum type) {
    this.database = DatabaseFactory.getDatabaseInstance(type);
  }

  @Override
  public List<String> querySchemaList(String jdbcUrl, String username, String password) {
    AbstractDatabase db = Objects.requireNonNull(this.database, MESSAGE_ERROR_INFO);
    try {
      db.connect(jdbcUrl, username, password);
      return db.querySchemaList();
    } finally {
      db.close();
    }
  }

  @Override
  public List<TableDescription> queryTableList(String jdbcUrl, String username, String password,
      String schemaName) {
    AbstractDatabase db = Objects.requireNonNull(this.database, MESSAGE_ERROR_INFO);
    try {
      db.connect(jdbcUrl, username, password);
      return db.queryTableList(schemaName);
    } finally {
      db.close();
    }
  }

  @Override
  public String getTableDDL(String jdbcUrl, String username, String password, String schemaName,
      String tableName) {
    AbstractDatabase db = Objects.requireNonNull(this.database, MESSAGE_ERROR_INFO);
    try {
      db.connect(jdbcUrl, username, password);
      return db.getTableDDL(schemaName, tableName);
    } finally {
      db.close();
    }
  }

  @Override
  public String getViewDDL(String jdbcUrl, String username, String password, String schemaName,
      String tableName) {
    AbstractDatabase db = Objects.requireNonNull(this.database, MESSAGE_ERROR_INFO);
    try {
      db.connect(jdbcUrl, username, password);
      return db.getViewDDL(schemaName, tableName);
    } finally {
      db.close();
    }
  }

  @Override
  public List<ColumnDescription> queryTableColumnMeta(String jdbcUrl, String username,
      String password, String schemaName, String tableName) {
    AbstractDatabase db = Objects.requireNonNull(this.database, MESSAGE_ERROR_INFO);
    try {
      db.connect(jdbcUrl, username, password);
      return db.queryTableColumnMeta(schemaName, tableName);
    } finally {
      db.close();
    }
  }

  @Override
  public List<ColumnDescription> querySqlColumnMeta(String jdbcUrl, String username,
      String password, String querySql) {
    AbstractDatabase db = Objects.requireNonNull(this.database, MESSAGE_ERROR_INFO);
    try {
      db.connect(jdbcUrl, username, password);
      return db.querySelectSqlColumnMeta(querySql);
    } finally {
      db.close();
    }
  }

  @Override
  public List<String> queryTablePrimaryKeys(String jdbcUrl, String username, String password,
      String schemaName, String tableName) {
    AbstractDatabase db = Objects.requireNonNull(this.database, MESSAGE_ERROR_INFO);
    try {
      db.connect(jdbcUrl, username, password);
      return db.queryTablePrimaryKeys(schemaName, tableName);
    } finally {
      db.close();
    }
  }

  @Override
  public SchemaTableMeta queryTableMeta(String jdbcUrl, String username, String password,
      String schemaName, String tableName) {
    AbstractDatabase db = Objects.requireNonNull(this.database, MESSAGE_ERROR_INFO);
    SchemaTableMeta tableMeta = new SchemaTableMeta();
    List<String> pks;
    String createSql;
    TableDescription tableDesc;
    List<ColumnDescription> columns;
    try {
      db.connect(jdbcUrl, username, password);
      tableDesc = db.queryTableMeta(schemaName, tableName);
      if (null == tableDesc) {
        throw new IllegalArgumentException("Table Or View Not Exist");
      }

      columns = db.queryTableColumnMeta(schemaName, tableName);
      if (tableDesc.getTableType().equals(DBTableType.TABLE.name())) {
        pks = db.queryTablePrimaryKeys(schemaName, tableName);
        createSql = db.getTableDDL(schemaName, tableName);
      } else {
        pks = Collections.emptyList();
        createSql = db.getViewDDL(schemaName, tableName);
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
      db.close();
    }
  }

  @Override
  public SchemaTableData queryTableData(String jdbcUrl, String username, String password,
      String schemaName, String tableName, int rowCount) {
    AbstractDatabase db = Objects.requireNonNull(this.database, MESSAGE_ERROR_INFO);
    try {
      db.connect(jdbcUrl, username, password);
      return db.queryTableData(schemaName, tableName, rowCount);
    } finally {
      db.close();
    }
  }

  @Override
  public void testQuerySQL(String jdbcUrl, String username, String password, String sql) {
    AbstractDatabase db = Objects.requireNonNull(this.database, MESSAGE_ERROR_INFO);
    try {
      db.connect(jdbcUrl, username, password);
      db.testQuerySQL(sql);
    } finally {
      db.close();
    }
  }

  @Override
  public String getDDLCreateTableSQL(DatabaseTypeEnum type, List<ColumnDescription> fieldNames,
      List<String> primaryKeys, String schemaName, String tableName, boolean autoIncr) {
    return GenerateSqlUtils.getDDLCreateTableSQL(
        type, fieldNames, primaryKeys, schemaName, tableName, autoIncr);
  }
}
