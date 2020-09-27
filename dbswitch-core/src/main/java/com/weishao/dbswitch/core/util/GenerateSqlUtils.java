// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Data : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.weishao.dbswitch.core.util;

import java.util.List;
import java.util.stream.Collectors;

import com.weishao.dbswitch.common.constant.DatabaseTypeEnum;
import com.weishao.dbswitch.core.constant.Const;
import com.weishao.dbswitch.core.database.AbstractDatabase;
import com.weishao.dbswitch.core.database.DatabaseFactory;
import com.weishao.dbswitch.core.model.ColumnDescription;
import com.weishao.dbswitch.core.model.ColumnMetaData;

/**
 * 拼接SQL工具类
 * 
 * @author tang
 *
 */
public class GenerateSqlUtils {

	public static String getDDLCreateTableSQL(DatabaseTypeEnum type, List<ColumnDescription> fieldNames,
			List<String> primaryKeys, String schemaName, String tableName, boolean autoIncr) {
		StringBuilder retval = new StringBuilder();
		List<String> pks = fieldNames.stream().filter((cd) -> primaryKeys.contains(cd.getFieldName()))
				.map((cd) -> cd.getFieldName()).collect(Collectors.toList());

		AbstractDatabase db = DatabaseFactory.getDatabaseInstance(type);

		retval.append(Const.CREATE_TABLE);
		// if(ifNotExist && type!=DatabaseType.ORACLE) {
		// retval.append( Const.IF_NOT_EXISTS );
		// }
		retval.append(db.getQuotedSchemaTableCombination(schemaName, tableName) + Const.CR);
		retval.append("(").append(Const.CR);

		for (int i = 0; i < fieldNames.size(); i++) {
			if (i > 0) {
				retval.append(", ");
			} else {
				retval.append("  ");
			}

			ColumnMetaData v = fieldNames.get(i).getMetaData();
			retval.append(db.getFieldDefinition(v, pks, autoIncr, true));
		}

		if (!pks.isEmpty()) {
			String pk = db.getPrimaryKeyAsString(pks);
			retval.append(", PRIMARY KEY (").append(pk).append(")").append(Const.CR);
		}

		retval.append(")").append(Const.CR);
		if (DatabaseTypeEnum.MYSQL == type) {
			retval.append("ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin").append(Const.CR);
		}

		return db.formatSQL(retval.toString());
	}
}
