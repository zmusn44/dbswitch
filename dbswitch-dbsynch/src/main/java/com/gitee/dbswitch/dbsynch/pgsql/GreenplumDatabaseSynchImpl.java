// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.dbsynch.pgsql;

import javax.sql.DataSource;
import com.gitee.dbswitch.dbsynch.IDatabaseSynchronize;

/**
 * Greenplum数据库DML同步实现类
 * 
 * @author tang
 *
 */
public class GreenplumDatabaseSynchImpl extends PostgresqlDatabaseSynchImpl implements IDatabaseSynchronize {

	public GreenplumDatabaseSynchImpl(DataSource ds) {
		super(ds);
	}

}
