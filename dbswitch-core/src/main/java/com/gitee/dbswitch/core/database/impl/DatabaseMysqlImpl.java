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
import java.util.Map;
import java.util.ArrayList;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.lang3.StringUtils;
import com.gitee.dbswitch.core.constant.Const;
import com.gitee.dbswitch.common.constant.DatabaseTypeEnum;
import com.gitee.dbswitch.core.database.AbstractDatabase;
import com.gitee.dbswitch.core.database.IDatabaseInterface;
import com.gitee.dbswitch.core.model.ColumnDescription;
import com.gitee.dbswitch.core.model.ColumnMetaData;
import com.gitee.dbswitch.core.model.TableDescription;
import com.gitee.dbswitch.core.util.JdbcOperatorUtils;
import com.gitee.dbswitch.core.util.JdbcUrlUtils;
import org.springframework.util.CollectionUtils;

/**
 * 支持MySQL数据库的元信息实现
 * 
 * @author tang
 *
 */
public class DatabaseMysqlImpl extends AbstractDatabase implements IDatabaseInterface {

	public DatabaseMysqlImpl() {
		super("com.mysql.cj.jdbc.Driver");
	}
	
	public DatabaseMysqlImpl(String driverClassName) {
		super(driverClassName);
	}

	@Override
	public List<String> querySchemaList() {
		String mysqlJdbcUrl=null;
		try {
			mysqlJdbcUrl = this.metaData.getURL();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		Map<String, String> data=JdbcUrlUtils.findParamsByMySqlJdbcUrl(mysqlJdbcUrl);
		List<String> ret=new ArrayList<String>();
		ret.add(data.get("schema"));
		return ret;
	}
	
	@Override
	public List<TableDescription> queryTableList(String schemaName) {
		List<TableDescription> ret = new ArrayList<>();
		String sql = String.format(
				"SELECT `TABLE_SCHEMA`,`TABLE_NAME`,`TABLE_TYPE`,`TABLE_COMMENT` FROM `information_schema`.`TABLES` where `TABLE_SCHEMA`='%s'",
				schemaName);
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			pstmt = this.connection.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				TableDescription td=new TableDescription();
				td.setSchemaName(rs.getString("TABLE_SCHEMA"));
				td.setTableName(rs.getString("TABLE_NAME"));
				td.setRemarks(rs.getString("TABLE_COMMENT"));
				String tableType=rs.getString("TABLE_TYPE");
				 if(tableType.equalsIgnoreCase("VIEW")) {
					td.setTableType("VIEW");
				}else {
					td.setTableType("TABLE");
				}
				 
				 ret.add(td);
			}

			return ret;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			JdbcOperatorUtils.closeResultSet(rs);
			JdbcOperatorUtils.closeStatement(pstmt);
		}
	}

	@Override
	public List<ColumnDescription> querySelectSqlColumnMeta(String sql) {
		String querySQL = String.format(" %s LIMIT 1", sql.replace(";", ""));
		return this.getSelectSqlColumnMeta(querySQL, DatabaseTypeEnum.MYSQL);
	}

	@Override
	protected String getTableFieldsQuerySQL(String schemaName, String tableName) {
		return String.format("SELECT * FROM `%s`.`%s` ", schemaName, tableName);
	}

	@Override
	protected String getTestQuerySQL(String sql) {
		return String.format("explain %s", sql.replace(";", ""));
	}

	@Override
	public String getQuotedSchemaTableCombination(String schemaName, String tableName) {
		return String.format("  `%s`.`%s` ", schemaName, tableName);
	}
	
	@Override
	public String getFieldDefinition(ColumnMetaData v, List<String> pks, boolean useAutoInc, boolean addCr) {
		String fieldname = v.getName();
		int length = v.getLength();
		int precision = v.getPrecision();
		int type = v.getType();
		
		String retval = " `"+fieldname + "`  ";

		switch (type) {
		case ColumnMetaData.TYPE_TIMESTAMP:
			retval += "DATETIME";
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
					retval += "BIGINT AUTO_INCREMENT NOT NULL";
				} else {
					retval += "BIGINT NOT NULL";
				}
			} else {
				// Integer values...
				if (precision == 0) {
					if (length > 9) {
						if (length < 19) {
							// can hold signed values between -9223372036854775808 and 9223372036854775807
							// 18 significant digits
							retval += "BIGINT";
						} else {
							retval += "DECIMAL(" + length + ")";
						}
					} else {
						retval += "INT";
					}
				} else {
					// Floating point values...
					if (length > 15) {
						retval += "DECIMAL(" + length;
						if (precision > 0) {
							retval += ", " + precision;
						}
						retval += ")";
					} else {
						// A double-precision floating-point number is accurate to approximately 15
						// decimal places.
						// http://mysql.mirrors-r-us.net/doc/refman/5.1/en/numeric-type-overview.html
						retval += "DOUBLE";
					}
				}
			}
			break;
		case ColumnMetaData.TYPE_STRING:
			if (length > 0) {
				if (length == 1) {
					retval += "CHAR(1)";
				} else if (length < 256) {
					retval += "VARCHAR(" + length + ")";
				}else if (!CollectionUtils.isEmpty(pks) && pks.contains(fieldname)) {
					/*
					 * MySQL5.6中varchar字段为主键时最大长度为254,例如如下的建表语句在MySQL5.7下能通过，但在MySQL5.6下无法通过：
					 *	create table `t_test`(
					 *	`key` varchar(1024) binary,
					 *	`val` varchar(1024) binary,
					 *	primary key(`key`)
					 * );
					 */
					retval += "VARCHAR(254) BINARY";
				} else if (length < 65536) {
					retval += "TEXT";
				} else if (length < 16777216) {
					retval += "MEDIUMTEXT";
				} else {
					retval += "LONGTEXT";
				}
			} else {
				retval += "TINYTEXT";
			}
			break;
		case ColumnMetaData.TYPE_BINARY:
			retval += "LONGBLOB";
			break;
		default:
			retval += " LONGTEXT";
			break;
		}

		if (addCr) {
			retval += Const.CR;
		}

		return retval;
	}

	@Override
	public String getPrimaryKeyAsString(List<String> pks) {
		if (!CollectionUtils.isEmpty(pks)) {
			StringBuilder sb = new StringBuilder();
			sb.append("`");
			sb.append(StringUtils.join(pks, "` , `"));
			sb.append("`");
			return sb.toString();
		}

		return "";
	}
	
}
