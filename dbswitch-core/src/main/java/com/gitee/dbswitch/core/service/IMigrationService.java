// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Data : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.core.service;

import com.gitee.dbswitch.common.type.DatabaseTypeEnum;
import com.gitee.dbswitch.core.model.ColumnDescription;
import com.gitee.dbswitch.core.model.DatabaseDescription;
import com.gitee.dbswitch.core.model.TableDescription;
import java.util.List;

/**
 * 表结构迁移接口定义
 *
 * @author tang
 */
public interface IMigrationService {

  /**
   * 设置数据库的连接信息
   *
   * @param databaseDesc 数据库的连接信息
   */
  void setDatabaseConnection(DatabaseDescription databaseDesc);

  /**
   * 获取数据库的schema模式列表
   *
   * @return
   */
  List<String> querySchemaList();

  /**
   * 获取指定Schema下所有的表列表
   *
   * @param schemaName 模式名称
   * @return
   */
  List<TableDescription> queryTableList(String schemaName);

  /**
   * 获取指定schema.table的表结构字段信息
   *
   * @param schemaName 模式名称
   * @param tableName  表或视图名称
   * @return
   */
  List<ColumnDescription> queryTableColumnMeta(String schemaName, String tableName);

  /**
   * 获取指定SQL结构字段信息
   *
   * @param querySql 查询的SQL语句
   * @return
   */
  List<ColumnDescription> querySqlColumnMeta(String querySql);

  /**
   * 获取表的主键信息字段列表
   *
   * @param schemaName
   * @param tableName
   * @return
   */
  List<String> queryTablePrimaryKeys(String schemaName, String tableName);

  /**
   * 测试数据库SQL查询
   *
   * @param sql 待查询的SQL语句
   */
  void testQuerySQL(String sql);

  /**
   * 根据字段结构信息组装对应数据库的建表DDL语句
   *
   * @param type        目的数据库类型
   * @param fieldNames  字段结构信息
   * @param primaryKeys 主键字段信息
   * @param tableName   模式名称
   * @param tableName   表名称
   * @param autoIncr    是否允许主键自增
   * @return 对应数据库的DDL建表语句
   */
  String getDDLCreateTableSQL(DatabaseTypeEnum type, List<ColumnDescription> fieldNames,
      List<String> primaryKeys, String schemaName, String tableName, boolean autoIncr);
}
