// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Data : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.weishao.dbswitch.sql.service.impl;

import java.util.HashMap;
import java.util.Map;
import com.weishao.dbswitch.common.constant.DatabaseTypeEnum;
import com.weishao.dbswitch.sql.service.ISqlGeneratorService;
import com.weishao.dbswitch.sql.ddl.AbstractDatabaseDialect;
import com.weishao.dbswitch.sql.ddl.AbstractSqlDdlOperator;
import com.weishao.dbswitch.sql.ddl.pojo.TableDefinition;
import com.weishao.dbswitch.sql.ddl.sql.DdlSqlCreateTable;
import com.weishao.dbswitch.sql.ddl.sql.DdlSqlAlterTable;
import com.weishao.dbswitch.sql.ddl.sql.DdlSqlDropTable;
import com.weishao.dbswitch.sql.ddl.sql.DdlSqlTruncateTable;
import com.weishao.dbswitch.sql.ddl.sql.impl.GreenplumDialectImpl;
import com.weishao.dbswitch.sql.ddl.sql.impl.MySqlDialectImpl;
import com.weishao.dbswitch.sql.ddl.sql.impl.OracleDialectImpl;
import com.weishao.dbswitch.sql.ddl.sql.impl.PostgresDialectImpl;

/**
 * 拼接生成SQL实现类
 * 
 * @author tang
 *
 */
public class MyselfSqlGeneratorServiceImpl implements ISqlGeneratorService {

	private static final Map<DatabaseTypeEnum, String> DATABASE_MAPPER = new HashMap<DatabaseTypeEnum, String>();

	static {
		DATABASE_MAPPER.put(DatabaseTypeEnum.MYSQL, MySqlDialectImpl.class.getName());
		DATABASE_MAPPER.put(DatabaseTypeEnum.ORACLE, OracleDialectImpl.class.getName());
		DATABASE_MAPPER.put(DatabaseTypeEnum.POSTGRESQL, PostgresDialectImpl.class.getName());
		DATABASE_MAPPER.put(DatabaseTypeEnum.GREENPLUM, GreenplumDialectImpl.class.getName());
	}

	public static AbstractDatabaseDialect getDatabaseInstance(DatabaseTypeEnum type) {
		if (DATABASE_MAPPER.containsKey(type)) {
			String className = DATABASE_MAPPER.get(type);
			try {
				return (AbstractDatabaseDialect) Class.forName(className).newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		throw new RuntimeException(String.format("Unkown database type (%s)", type.name()));
	}

	@Override
	public String createTable(String dbtype, TableDefinition t) {
		DatabaseTypeEnum type = DatabaseTypeEnum.valueOf(dbtype.toUpperCase());
		AbstractDatabaseDialect dialect = getDatabaseInstance(type);
		AbstractSqlDdlOperator operator = new DdlSqlCreateTable(t);
		return operator.toSqlString(dialect);
	}

	@Override
	public String alterTable(String dbtype, String handle, TableDefinition t){
		DatabaseTypeEnum type = DatabaseTypeEnum.valueOf(dbtype.toUpperCase());
		AbstractDatabaseDialect dialect = getDatabaseInstance(type);
		AbstractSqlDdlOperator operator = new DdlSqlAlterTable(t,handle);
		return operator.toSqlString(dialect);
	}

	@Override
	public String dropTable(String dbtype, TableDefinition t) {
		DatabaseTypeEnum type = DatabaseTypeEnum.valueOf(dbtype.toUpperCase());
		AbstractDatabaseDialect dialect = getDatabaseInstance(type);
		AbstractSqlDdlOperator operator = new DdlSqlDropTable(t);
		return operator.toSqlString(dialect);
	}

	@Override
	public String truncateTable(String dbtype, TableDefinition t) {
		DatabaseTypeEnum type = DatabaseTypeEnum.valueOf(dbtype.toUpperCase());
		AbstractDatabaseDialect dialect = getDatabaseInstance(type);
		AbstractSqlDdlOperator operator = new DdlSqlTruncateTable(t);
		return operator.toSqlString(dialect);
	}

}
