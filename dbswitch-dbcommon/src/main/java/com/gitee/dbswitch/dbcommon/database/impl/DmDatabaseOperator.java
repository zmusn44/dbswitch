package com.gitee.dbswitch.dbcommon.database.impl;

import javax.sql.DataSource;
import com.gitee.dbswitch.dbcommon.database.IDatabaseOperator;

public class DmDatabaseOperator extends OracleDatabaseOperator implements IDatabaseOperator {

	public DmDatabaseOperator(DataSource dataSource) {
		super(dataSource);
	}

}
