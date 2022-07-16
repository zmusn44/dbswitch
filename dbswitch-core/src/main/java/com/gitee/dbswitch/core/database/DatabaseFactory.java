// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.core.database;

import com.gitee.dbswitch.common.type.DatabaseTypeEnum;
import com.gitee.dbswitch.core.database.impl.DatabaseDB2Impl;
import com.gitee.dbswitch.core.database.impl.DatabaseDmImpl;
import com.gitee.dbswitch.core.database.impl.DatabaseGbase8aImpl;
import com.gitee.dbswitch.core.database.impl.DatabaseGreenplumImpl;
import com.gitee.dbswitch.core.database.impl.DatabaseHiveImpl;
import com.gitee.dbswitch.core.database.impl.DatabaseKingbaseImpl;
import com.gitee.dbswitch.core.database.impl.DatabaseMariaDBImpl;
import com.gitee.dbswitch.core.database.impl.DatabaseMysqlImpl;
import com.gitee.dbswitch.core.database.impl.DatabaseOracleImpl;
import com.gitee.dbswitch.core.database.impl.DatabaseOscarImpl;
import com.gitee.dbswitch.core.database.impl.DatabasePostgresImpl;
import com.gitee.dbswitch.core.database.impl.DatabaseSqliteImpl;
import com.gitee.dbswitch.core.database.impl.DatabaseSqlserver2000Impl;
import com.gitee.dbswitch.core.database.impl.DatabaseSqlserverImpl;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * 数据库实例构建工厂类
 *
 * @author tang
 */
public final class DatabaseFactory {

  private static final Map<DatabaseTypeEnum, Callable<AbstractDatabase>> DATABASE_MAPPER
      = new HashMap<DatabaseTypeEnum, Callable<AbstractDatabase>>() {

    private static final long serialVersionUID = 9202705534880971997L;

    {
      put(DatabaseTypeEnum.MYSQL, DatabaseMysqlImpl::new);
      put(DatabaseTypeEnum.MARIADB, DatabaseMariaDBImpl::new);
      put(DatabaseTypeEnum.ORACLE, DatabaseOracleImpl::new);
      put(DatabaseTypeEnum.SQLSERVER2000, DatabaseSqlserver2000Impl::new);
      put(DatabaseTypeEnum.SQLSERVER, DatabaseSqlserverImpl::new);
      put(DatabaseTypeEnum.POSTGRESQL, DatabasePostgresImpl::new);
      put(DatabaseTypeEnum.GREENPLUM, DatabaseGreenplumImpl::new);
      put(DatabaseTypeEnum.DB2, DatabaseDB2Impl::new);
      put(DatabaseTypeEnum.DM, DatabaseDmImpl::new);
      put(DatabaseTypeEnum.KINGBASE, DatabaseKingbaseImpl::new);
      put(DatabaseTypeEnum.OSCAR, DatabaseOscarImpl::new);
      put(DatabaseTypeEnum.GBASE8A, DatabaseGbase8aImpl::new);
      put(DatabaseTypeEnum.HIVE, DatabaseHiveImpl::new);
      put(DatabaseTypeEnum.SQLITE3, DatabaseSqliteImpl::new);
    }
  };

  public static AbstractDatabase getDatabaseInstance(DatabaseTypeEnum type) {
    Callable<AbstractDatabase> callable = DATABASE_MAPPER.get(type);
    if (null != callable) {
      try {
        return callable.call();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    throw new UnsupportedOperationException(
        String.format("Unknown database type (%s)", type.name()));
  }

  private DatabaseFactory() {
    throw new IllegalStateException();
  }

}
