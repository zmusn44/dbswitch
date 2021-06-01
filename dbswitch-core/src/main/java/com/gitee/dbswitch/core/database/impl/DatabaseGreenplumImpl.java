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

import java.util.Collections;
import java.util.List;
import com.alibaba.druid.sql.SQLUtils;
import com.gitee.dbswitch.core.constant.Const;
import com.gitee.dbswitch.common.constant.DatabaseTypeEnum;
import com.gitee.dbswitch.core.database.AbstractDatabase;
import com.gitee.dbswitch.core.database.IDatabaseInterface;
import com.gitee.dbswitch.core.model.ColumnDescription;
import com.gitee.dbswitch.core.model.ColumnMetaData;
import org.springframework.util.CollectionUtils;

/**
 * 支持Greenplum数据库的元信息实现
 * 
 * @author tang
 *
 */
public class DatabaseGreenplumImpl extends AbstractDatabase implements IDatabaseInterface {

	public DatabaseGreenplumImpl() {
		super("com.pivotal.jdbc.GreenplumDriver");
	}

	@Override
	public List<ColumnDescription> querySelectSqlColumnMeta(String sql) {
		String querySQL = String.format(" %s LIMIT 1 OFFSET 0 ", sql.replace(";", ""));
		return this.getSelectSqlColumnMeta(querySQL, DatabaseTypeEnum.GREENPLUM);
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
		return SQLUtils.formatPGSql(sql, null);
	}

	@Override
	public String getFieldDefinition(ColumnMetaData v, List<String> pks, boolean useAutoInc, boolean addCr) {
		String fieldname = v.getName();
		int length = v.getLength();
		int precision = v.getPrecision();
		int type = v.getType();

		String retval =" \""+fieldname + "\"   ";

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
			retval += "VARCHAR(32)";
			break;
		case ColumnMetaData.TYPE_NUMBER:
		case ColumnMetaData.TYPE_INTEGER:
		case ColumnMetaData.TYPE_BIGNUMBER:
			if (!CollectionUtils.isEmpty(pks) && pks.contains(fieldname)) {
				if (useAutoInc) {
					retval += "BIGSERIAL";
				} else {
					retval += "BIGINT";
				}
			} else {
				if (length > 0) {
					if (precision > 0 || length > 18) {
						if ((length + precision) > 0 && precision > 0) {
							// Numeric(Precision, Scale): Precision = total length; Scale = decimal places
							retval += "NUMERIC(" + (length + precision) + ", " + precision + ")";
						} else {
							retval += "DOUBLE PRECISION";
						}
					} else {
						if (length > 9) {
							retval += "BIGINT";
						} else {
							if (length < 5) {
								retval += "SMALLINT";
							} else {
								retval += "INTEGER";
							}
						}
					}

				} else {
					retval += "DOUBLE PRECISION";
				}
			}
			break;
		case ColumnMetaData.TYPE_STRING:
			if (length < 1 || length >= AbstractDatabase.CLOB_LENGTH) {
				retval += "TEXT";
			} else {
				if (null != pks && pks.contains(fieldname)) {
					retval += "VARCHAR(" + length + ")";
				} else {
					retval += "TEXT";
				}
			}
			break;
		case ColumnMetaData.TYPE_BINARY:
			retval += "BYTEA";
			break;
		default:
			retval += " TEXT";
			break;
		}

		if (addCr) {
			retval += Const.CR;
		}

		return retval;
	}
}
