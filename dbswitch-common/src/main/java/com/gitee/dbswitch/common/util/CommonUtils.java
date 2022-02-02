// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.common.util;

import com.gitee.dbswitch.common.type.DatabaseTypeEnum;
import java.util.List;

/**
 * 普通工具类
 *
 * @author tang
 */
public final class CommonUtils {

  private CommonUtils() {
  }

  /**
   * 根据数据库类型获取表的全名：schema.table
   *
   * @param dbType 数据库类型
   * @param schema schema名
   * @param table  table名
   * @return 表的全名字符串
   */
  public static String getTableFullNameByDatabase(DatabaseTypeEnum dbType, String schema,
      String table) {
    if (dbType == DatabaseTypeEnum.MYSQL || dbType == DatabaseTypeEnum.MARIADB
        || dbType == DatabaseTypeEnum.HIVE) {
      return String.format("`%s`.`%s`", schema, table);
    } else if (dbType == DatabaseTypeEnum.SQLSERVER || dbType == DatabaseTypeEnum.SQLSERVER2000) {
      return String.format("[%s].[%s]", schema, table);
    } else {
      return String.format("\"%s\".\"%s\"", schema, table);
    }
  }

  /**
   * 拼接SELECT查询指定字段的SQL语句
   *
   * @param dbType  数据库类型
   * @param schema  schema名
   * @param table   table名
   * @param columns 列名列表
   * @return SQL语句字符串
   */
  public static String getSelectColumnsSQL(DatabaseTypeEnum dbType, String schema, String table,
      List<String> columns) {
    StringBuilder sb = new StringBuilder();
    sb.append(" SELECT ");
    for (int i = 0; i < columns.size(); ++i) {
      String field = columns.get(i);
      String quoteField = quoteString(dbType, field);
      sb.append(quoteField);

      if (i < columns.size() - 1) {
        sb.append(",");
      }
    }
    sb.append(" FROM ");
    if (null != schema && !schema.isEmpty()) {
      sb.append(quoteString(dbType, schema));
      sb.append(".");
    }
    sb.append(quoteString(dbType, table));

    return sb.toString();
  }

  private static String quoteString(DatabaseTypeEnum dbType, String keyName) {
    if (dbType == DatabaseTypeEnum.MYSQL || dbType == DatabaseTypeEnum.MARIADB
        || dbType == DatabaseTypeEnum.HIVE) {
      return String.format("`%s`", keyName);
    } else if (dbType == DatabaseTypeEnum.SQLSERVER || dbType == DatabaseTypeEnum.SQLSERVER2000) {
      return String.format("[%s]", keyName);
    } else {
      return String.format("\"%s\"", keyName);
    }
  }

}
