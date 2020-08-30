// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Data : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.weishao.dbswitch.dbsynch.pgsql;

import javax.sql.DataSource;
import com.weishao.dbswitch.dbsynch.IDatabaseSynchronize;

/**
 * Greenplum数据库实现类
 * 
 * @author tang
 *
 */
public class GreenplumDatabaseSynchImpl extends PostgresqlDatabaseSynchImpl implements IDatabaseSynchronize {

	public GreenplumDatabaseSynchImpl(DataSource ds) {
		super(ds);
	}

}
