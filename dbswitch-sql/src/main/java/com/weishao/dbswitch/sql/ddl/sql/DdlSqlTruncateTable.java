// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Data : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.weishao.dbswitch.sql.ddl.sql;

import com.weishao.dbswitch.sql.ddl.AbstractDatabaseDialect;
import com.weishao.dbswitch.sql.ddl.AbstractSqlDdlOperator;
import com.weishao.dbswitch.sql.ddl.pojo.TableDefinition;

/**
 * Truncate语句操作类
 * 
 * @author tang
 *
 */
public class DdlSqlTruncateTable extends AbstractSqlDdlOperator {

	private TableDefinition table;
	
	public DdlSqlTruncateTable(TableDefinition t) {
		super("TRUNCATE TABLE ");
		this.table=t;
	}

	@Override
	public String toSqlString(AbstractDatabaseDialect dialect) {
		StringBuilder sb=new StringBuilder();
		sb.append(this.getName());
		String fullTableName=dialect.getSchemaTableName(table.getSchemaName(), table.getTableName());
		sb.append(fullTableName);
		return sb.toString();
	}

}
