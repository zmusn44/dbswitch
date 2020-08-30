package com.weishao.dbswitch.sql.ddl;

import java.util.Objects;

/**
 * DDL操作抽象类
 * 
 * @author tang
 *
 */
public abstract class AbstractSqlDdlOperator {

	private String name;

	public AbstractSqlDdlOperator(String name) {
		this.name = Objects.requireNonNull(name);
	}

	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return this.name;
	}

	public abstract String toSqlString(AbstractDatabaseDialect dialect);
}
