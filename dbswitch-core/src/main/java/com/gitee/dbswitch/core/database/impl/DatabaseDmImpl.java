// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.core.database.impl;

import java.util.List;
import com.gitee.dbswitch.common.constant.DatabaseTypeEnum;
import com.gitee.dbswitch.core.constant.Const;
import com.gitee.dbswitch.core.database.AbstractDatabase;
import com.gitee.dbswitch.core.database.IDatabaseInterface;
import com.gitee.dbswitch.core.model.ColumnDescription;
import com.gitee.dbswitch.core.model.ColumnMetaData;

/**
 * 支持DM数据库的元信息实现
 * 
 * @author tang
 *
 */
public class DatabaseDmImpl extends AbstractDatabase implements IDatabaseInterface {

	public DatabaseDmImpl() {
		super("dm.jdbc.driver.DmDriver");
	}

	@Override
	public List<ColumnDescription> querySelectSqlColumnMeta(String sql) {
		String querySQL = String.format("SELECT * from (%s) tmp where ROWNUM<=1 ", sql.replace(";", ""));
		return this.getSelectSqlColumnMeta(querySQL, DatabaseTypeEnum.DM);
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
	public String getFieldDefinition(ColumnMetaData v, List<String> pks, boolean useAutoInc, boolean addCr) {
		String fieldname = v.getName();
		int length = v.getLength();
		int precision = v.getPrecision();

		StringBuilder retval = new StringBuilder(128);
		retval.append(" \"").append(fieldname).append("\"    ");

		int type = v.getType();
		switch (type) {
		case ColumnMetaData.TYPE_TIMESTAMP:
		case ColumnMetaData.TYPE_TIME:
			retval.append("TIMESTAMP");
			break;
		case ColumnMetaData.TYPE_DATE:
			retval.append("DATE");
			break;
		case ColumnMetaData.TYPE_BOOLEAN:
			retval.append("VARCHAR(32)");
			break;
		case ColumnMetaData.TYPE_NUMBER:
		case ColumnMetaData.TYPE_BIGNUMBER:
			retval.append("NUMBER");
			if (length > 0) {
				if (length > 38) {
					length = 38;
				}

				retval.append('(').append(length);
				if (precision > 0) {
					retval.append(", ").append(precision);
				}
				retval.append(')');
			}
			break;
		case ColumnMetaData.TYPE_INTEGER:
			retval.append("INTEGER");
			break;
		case ColumnMetaData.TYPE_STRING:
			if (2*length >= AbstractDatabase.CLOB_LENGTH) {
				retval.append("CLOB");
			} else {
				if (length == 1) {
					retval.append("NVARCHAR2(2)");
				} else if (length > 0 && length < 2048) {
					retval.append("NVARCHAR2(").append(2*length).append(')');
				} else {
					retval.append("CLOB");
				}
			}
			break;
		case ColumnMetaData.TYPE_BINARY:
			retval.append("BLOB");
			break;
		default:
			retval.append("CLOB");
			break;
		}

		if (addCr) {
			retval.append(Const.CR);
		}

		return retval.toString();
	}

}
