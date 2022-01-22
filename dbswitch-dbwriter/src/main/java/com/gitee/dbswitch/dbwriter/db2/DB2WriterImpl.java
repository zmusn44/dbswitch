// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.dbwriter.db2;

import com.gitee.dbswitch.dbwriter.AbstractDatabaseWriter;
import com.gitee.dbswitch.dbwriter.IDatabaseWriter;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

/**
 * DB2数据库写入实现类
 *
 * @author tang
 */
@Slf4j
public class DB2WriterImpl extends AbstractDatabaseWriter implements IDatabaseWriter {

  public DB2WriterImpl(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  protected String getDatabaseProductName() {
    return "DB2";
  }

}
