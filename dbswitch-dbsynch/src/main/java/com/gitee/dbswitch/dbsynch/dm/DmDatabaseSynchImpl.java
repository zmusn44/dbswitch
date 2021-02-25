// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Data : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.dbsynch.dm;

import javax.sql.DataSource;

import com.gitee.dbswitch.dbsynch.oracle.OracleDatabaseSynchImpl;

public class DmDatabaseSynchImpl extends OracleDatabaseSynchImpl {

	public DmDatabaseSynchImpl(DataSource ds) {
		super(ds);
	}

}
