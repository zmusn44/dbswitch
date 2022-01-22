// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.dbwriter.gpdb;

import com.gitee.dbswitch.dbwriter.AbstractDatabaseWriter;
import com.gitee.dbswitch.dbwriter.IDatabaseWriter;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

/**
 * Greenplum数据库Insert写入实现类
 *
 * @author tang
 */
@Slf4j
public class GreenplumInsertWriterImpl extends AbstractDatabaseWriter implements IDatabaseWriter {

  public GreenplumInsertWriterImpl(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  protected String getDatabaseProductName() {
    return "Greenplum";
  }

}
