// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.core.database;

import com.gitee.dbswitch.core.model.ColumnDescription;
import com.gitee.dbswitch.core.model.ColumnMetaData;
import com.gitee.dbswitch.core.model.TableDescription;
import java.util.List;

/**
 * 数据库访问通用业务接口
 *
 * @author tang
 */
public interface IDatabaseInterface extends AutoCloseable {

  /**
   * 建立数据库连接
   *
   * @param jdbcurl  JDBC的URL连接字符串
   * @param username 用户名
   * @param password 密码
   */
  void connect(String jdbcurl, String username, String password);

  /**
   * 断开数据库连接
   */
  @Override
  void close();

  /**
   * 获取数据库的模式schema列表
   *
   * @return 模式名列表
   */
  List<String> querySchemaList();

  /**
   * 获取指定模式Schema内的所有表列表
   *
   * @param schemaName 模式名称
   * @return 表及视图名列表
   */
  List<TableDescription> queryTableList(String schemaName);

  /**
   * 获取指定模式表的元信息
   *
   * @param schemaName 模式名称
   * @param tableName  表或视图名称
   * @return 字段元信息列表
   */
  List<ColumnDescription> queryTableColumnMeta(String schemaName, String tableName);

  /**
   * 获取指定查询SQL的元信息
   *
   * @param sql SQL查询语句
   * @return 字段元信息列表
   */
  List<ColumnDescription> querySelectSqlColumnMeta(String sql);

  /**
   * 获取指定模式表的主键字段列表
   *
   * @param schemaName 模式名称
   * @param tableName  表名称
   * @return 主键字段名称列表
   */
  List<String> queryTablePrimaryKeys(String schemaName, String tableName);

  /**
   * 测试查询SQL语句的有效性
   *
   * @param sql 待验证的SQL语句
   */
  void testQuerySQL(String sql);

  /**
   * 获取数据库的表全名
   *
   * @param schemaName 模式名称
   * @param tableName  表名称
   * @return 表全名
   */
  String getQuotedSchemaTableCombination(String schemaName, String tableName);

  /**
   * 获取字段列的结构定义
   *
   * @param v          值元数据定义
   * @param pks        主键字段名称列表
   * @param addCr      是否结尾换行
   * @param useAutoInc 是否自增
   * @return 字段定义字符串
   */
  String getFieldDefinition(ColumnMetaData v, List<String> pks, boolean useAutoInc, boolean addCr);

  /**
   * 主键列转换为逗号分隔的字符串
   *
   * @param pks 主键字段列表
   * @return 主键字段拼接串
   */
  String getPrimaryKeyAsString(List<String> pks);
}
