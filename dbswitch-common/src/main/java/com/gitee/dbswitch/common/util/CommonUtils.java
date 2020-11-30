// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Data : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.common.util;

import java.util.List;
import com.gitee.dbswitch.common.constant.DatabaseTypeEnum;

/**
 * 普通工具类
 * 
 * @author tang
 *
 */
public final class CommonUtils {

	private CommonUtils() {

	}

	public static String getTableFullNameByDatabase(DatabaseTypeEnum dbtype, String schemaName, String tableName) {
		if (dbtype == DatabaseTypeEnum.MYSQL) {
			return String.format("`%s`.`%s`", schemaName, tableName);
		} else if (dbtype == DatabaseTypeEnum.SQLSERVER) {
			return String.format("[%s].[%s]", schemaName, tableName);
		} else {
			return String.format("\"%s\".\"%s\"", schemaName, tableName);
		}
	}

	public static String getSelectColumnsSQL(DatabaseTypeEnum dbtype, String schema, String table, List<String> columns) {
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT ");
		for (int i = 0; i < columns.size(); ++i) {
			String field = columns.get(i);
			String quoteField = quoteString(dbtype, field);
			sb.append(quoteField);

			if (i < columns.size() - 1) {
				sb.append(",");
			}
		}
		sb.append(" FROM ");
		if (null != schema && !schema.isEmpty()) {
			sb.append(quoteString(dbtype, schema));
			sb.append(".");
		}
		sb.append(quoteString(dbtype, table));

		return sb.toString();
	}

	private static String quoteString(DatabaseTypeEnum dbtype, String keyName) {
		if (dbtype == DatabaseTypeEnum.MYSQL) {
			return String.format("`%s`", keyName);
		} else if (dbtype == DatabaseTypeEnum.SQLSERVER) {
			return String.format("[%s]", keyName);
		} else {
			return String.format("\"%s\"", keyName);
		}
	}

	public static String getQuotationChar(DatabaseTypeEnum dbtype) {
		if (dbtype == DatabaseTypeEnum.MYSQL) {
			return "`";
		} else {
			return "\"";
		}
	}
}
