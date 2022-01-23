// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.dbsynch.dm;

import com.gitee.dbswitch.dbsynch.oracle.OracleDatabaseSyncImpl;
import javax.sql.DataSource;

/**
 * DM数据库DML同步实现类
 *
 * @author tang
 */
public class DmDatabaseSyncImpl extends OracleDatabaseSyncImpl {

  public DmDatabaseSyncImpl(DataSource ds) {
    super(ds);
  }

}
