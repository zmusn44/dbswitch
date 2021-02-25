// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Data : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.dbsynch.kingbase;

import javax.sql.DataSource;

import com.gitee.dbswitch.dbsynch.pgsql.PostgresqlDatabaseSynchImpl;

public class KingbaseDatabaseSynchImpl extends PostgresqlDatabaseSynchImpl {

	public KingbaseDatabaseSynchImpl(DataSource ds) {
		super(ds);
	}

}
