// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Data : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.weishao.dbswitch.core.database.impl;

import java.util.List;
import com.weishao.dbswitch.common.constant.DatabaseTypeEnum;
import com.weishao.dbswitch.core.constant.Const;
import com.weishao.dbswitch.core.database.AbstractDatabase;
import com.weishao.dbswitch.core.database.IDatabaseInterface;
import com.weishao.dbswitch.core.model.ColumnDescription;
import com.weishao.dbswitch.core.model.ColumnMetaData;

/**
 * 支持DB2数据库的元信息实现
 * 
 * @author tang
 *
 */
public class DatabaseDB2Impl extends AbstractDatabase implements IDatabaseInterface {

	public DatabaseDB2Impl() {
		super("com.ibm.db2.jcc.DB2Driver");
	}

	@Override
	public List<ColumnDescription> querySelectSqlColumnMeta(String sql) {
		String querySQL = String.format(" %s LIMIT 0 ", sql.replace(";", ""));
		return this.getSelectSqlColumnMeta(querySQL, DatabaseTypeEnum.DB2);
	}

	@Override
	protected String getTableFieldsQuerySQL(String schemaName, String tableName) {
		return String.format("SELECT * FROM \"%s\".\"%s\"  ", schemaName, tableName);
	}

	@Override
	protected String getTestQuerySQL(String sql) {
		return String.format("explain %s", sql.replace(";", ""));
	}

	@Override
	public String formatSQL(String sql) {
		return sql;
	}

	@Override
	public String getFieldDefinition(ColumnMetaData v, List<String> pks, boolean useAutoInc, boolean addCr) {
		String fieldname = v.getName();
		int length = v.getLength();
		int precision = v.getPrecision();
		int type = v.getType();

		String retval = " \"" + fieldname + "\"   ";

		switch (type) {
		case ColumnMetaData.TYPE_TIMESTAMP:
			retval += "TIMESTAMP";
			break;
		case ColumnMetaData.TYPE_TIME:
			retval += "TIME";
			break;
		case ColumnMetaData.TYPE_DATE:
			retval += "DATE";
			break;
		case ColumnMetaData.TYPE_BOOLEAN:
			retval += "CHARACTER(32)";
			break;
		case ColumnMetaData.TYPE_NUMBER:
		case ColumnMetaData.TYPE_BIGNUMBER:
			if (null != pks && pks.contains(fieldname)) {
				if (useAutoInc) {
					retval += "BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1, NOCACHE)";
				} else {
					retval += "BIGINT NOT NULL";
				}
			} else {
				if (length > 0) {
					retval += "DECIMAL(" + length;
					if (precision > 0) {
						retval += ", " + precision;
					}
					retval += ")";
				} else {
					retval += "FLOAT";
				}
			}
			break;
		case ColumnMetaData.TYPE_INTEGER:
			if (null != pks && pks.contains(fieldname)) {
				if (useAutoInc) {
					retval += "INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1, NOCACHE)";
				} else {
					retval += "INTEGER NOT NULL";
				}
			} else {
				retval += "INTEGER";
			}
			break;
		case ColumnMetaData.TYPE_STRING:
			if (length*3 > 32672) {
				retval += "CLOB";
			} else {
				retval += "VARCHAR";
				if (length > 0) {
					retval += "(" + length*3;
				} else {
					retval += "(";
				}

				retval += ")";
			}
			
			if (null != pks && pks.contains(fieldname)) {
				retval += " NOT NULL";
			}
			
			break;
		case ColumnMetaData.TYPE_BINARY:
			if (length > 32672) {
				retval += "BLOB(" + length + ")";
			} else {
				if (length > 0) {
					retval += "CHAR(" + length + ") FOR BIT DATA";
				} else {
					retval += "BLOB";
				}
			}
			break;
		default:
			retval += "CLOB";
			break;
		}

		if (addCr) {
			retval += Const.CR;
		}

		return retval;
	}

}