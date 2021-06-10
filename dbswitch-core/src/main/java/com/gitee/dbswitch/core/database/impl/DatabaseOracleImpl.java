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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.gitee.dbswitch.core.constant.Const;
import com.gitee.dbswitch.common.constant.DatabaseTypeEnum;
import com.gitee.dbswitch.core.database.AbstractDatabase;
import com.gitee.dbswitch.core.database.IDatabaseInterface;
import com.gitee.dbswitch.core.model.ColumnDescription;
import com.gitee.dbswitch.core.model.ColumnMetaData;
import com.gitee.dbswitch.core.model.TableDescription;
import com.gitee.dbswitch.core.util.JdbcOperatorUtils;

/**
 * 支持Oracle数据库的元信息实现
 * 备注：
 * （1）Oracle12c安装教程：
 *  官方安装版：https://www.w3cschool.cn/oraclejc/oraclejc-vuqx2qqu.html
 *  Docker版本：http://www.pianshen.com/article/4448142743/
 *            https://www.cnblogs.com/Dev0ps/p/10676930.html
 *  (2) Oralce的一个表里至多只能有一个字段为LONG类型
 * @author tang
 *
 */
public class DatabaseOracleImpl extends AbstractDatabase implements IDatabaseInterface {
	
	public DatabaseOracleImpl() {
		super("oracle.jdbc.driver.OracleDriver");
	}

	@Override
	public List<TableDescription> queryTableList(String schemaName) {
		List<TableDescription> ret = new ArrayList<>();
		String sql = String.format(
				"SELECT \"OWNER\",\"TABLE_NAME\",\"TABLE_TYPE\",\"COMMENTS\" from all_tab_comments where \"OWNER\"='%s'",
				schemaName);
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			pstmt = this.connection.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				TableDescription td=new TableDescription();
				td.setSchemaName(rs.getString("OWNER"));
				td.setTableName(rs.getString("TABLE_NAME"));
				td.setRemarks(rs.getString("COMMENTS"));
				String tableType=rs.getString("TABLE_TYPE").trim();
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
	public List<String> queryTablePrimaryKeys(String schemaName, String tableName) {
		// Oracle表的主键可以使用如下命令设置主键是否生效
		// 使主键失效：alter table tableName disable primary key;
		// 使主键恢复：alter table tableName enable primary key;
		Set<String> ret = new HashSet<>();
		String sql = String.format("SELECT COLUMN_NAME FROM user_cons_columns WHERE owner='%s' and constraint_name = "
				+ "(SELECT constraint_name FROM user_constraints WHERE table_name = '%s' AND constraint_type = 'P' and STATUS='ENABLED') ",
				schemaName, tableName);
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			pstmt = this.connection.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				ret.add(rs.getString("COLUMN_NAME"));
			}

			return new ArrayList<String>(ret);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			JdbcOperatorUtils.closeResultSet(rs);
			JdbcOperatorUtils.closeStatement(pstmt);
		}
	}
	
	@Override
	public List<ColumnDescription> querySelectSqlColumnMeta(String sql) {
		String querySQL = String.format("SELECT * from (%s) tmp where ROWNUM<=1 ", sql.replace(";", ""));
		return this.getSelectSqlColumnMeta(querySQL, DatabaseTypeEnum.ORACLE);
	}
	
	@Override
	protected String getTableFieldsQuerySQL(String schemaName, String tableName) {
		return String.format("SELECT * FROM \"%s\".\"%s\" ", schemaName, tableName);
	}
	
	@Override
	protected String getTestQuerySQL(String sql) {
		return String.format("explain plan for %s", sql.replace(";", ""));
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
			if (length >= AbstractDatabase.CLOB_LENGTH) {
				retval.append("CLOB");
			} else {
				if (length == 1) {
					retval.append("NVARCHAR2(1)");
				} else if (length > 0 && length < 2000) {
					// VARCHAR2(size)，size最大值为4000，单位是字节；而NVARCHAR2(size)，size最大值为2000，单位是字符
					retval.append("NVARCHAR2(").append(length).append(')');
				} else {
					retval.append("CLOB");// We don't know, so we just use the maximum...
				}
			}
			break;
		case ColumnMetaData.TYPE_BINARY: // the BLOB can contain binary data.
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
