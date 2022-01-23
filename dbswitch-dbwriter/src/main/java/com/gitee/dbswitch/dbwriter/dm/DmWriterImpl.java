// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.dbwriter.dm;

import com.gitee.dbswitch.dbwriter.IDatabaseWriter;
import com.gitee.dbswitch.dbwriter.oracle.OracleWriterImpl;
import javax.sql.DataSource;

/**
 * DM数据库写入实现类
 *
 * @author tang
 */
public class DmWriterImpl extends OracleWriterImpl implements IDatabaseWriter {

  public DmWriterImpl(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  protected String getDatabaseProductName() {
    return "DM";
  }

}
