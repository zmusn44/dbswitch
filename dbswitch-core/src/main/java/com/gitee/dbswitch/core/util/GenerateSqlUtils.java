// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Data : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.core.util;

import com.gitee.dbswitch.common.type.DatabaseTypeEnum;
import com.gitee.dbswitch.core.constant.Const;
import com.gitee.dbswitch.core.database.AbstractDatabase;
import com.gitee.dbswitch.core.database.DatabaseFactory;
import com.gitee.dbswitch.core.model.ColumnDescription;
import com.gitee.dbswitch.core.model.ColumnMetaData;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 拼接SQL工具类
 *
 * @author tang
 */
public class GenerateSqlUtils {

  public static String getDDLCreateTableSQL(
      DatabaseTypeEnum type,
      List<ColumnDescription> fieldNames,
      List<String> primaryKeys,
      String schemaName,
      String tableName,
      boolean autoIncr) {
    StringBuilder retval = new StringBuilder();
    List<String> pks = fieldNames.stream()
        .filter((cd) -> primaryKeys.contains(cd.getFieldName()))
        .map((cd) -> cd.getFieldName())
        .collect(Collectors.toList());

    AbstractDatabase db = DatabaseFactory.getDatabaseInstance(type);

    retval.append(Const.CREATE_TABLE);
    // if(ifNotExist && type!=DatabaseType.ORACLE) {
    // retval.append( Const.IF_NOT_EXISTS );
    // }
    retval.append(db.getQuotedSchemaTableCombination(schemaName, tableName));
    retval.append("(");

    for (int i = 0; i < fieldNames.size(); i++) {
      if (i > 0) {
        retval.append(", ");
      } else {
        retval.append("  ");
      }

      ColumnMetaData v = fieldNames.get(i).getMetaData();
      retval.append(db.getFieldDefinition(v, pks, autoIncr, false));
    }

    if (!pks.isEmpty()) {
      String pk = db.getPrimaryKeyAsString(pks);
      retval.append(", PRIMARY KEY (").append(pk).append(")");
    }

    retval.append(")");
    if (DatabaseTypeEnum.MYSQL == type) {
      retval.append("ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin");
    }

    return DDLFormatterUtils.format(retval.toString());
  }
}
