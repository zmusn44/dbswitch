package com.gitee.dbswitch.dbcommon.database.impl;

import javax.sql.DataSource;
import com.gitee.dbswitch.dbcommon.database.IDatabaseOperator;

public class KingbaseDatabaseOperator extends PostgreSqlDatabaseOperator implements IDatabaseOperator {

	public KingbaseDatabaseOperator(DataSource dataSource) {
		super(dataSource);
	}

}
