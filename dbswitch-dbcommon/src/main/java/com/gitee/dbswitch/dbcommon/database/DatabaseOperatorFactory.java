// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.dbcommon.database;

import com.gitee.dbswitch.common.type.DatabaseTypeEnum;
import com.gitee.dbswitch.common.util.DatabaseAwareUtils;
import com.gitee.dbswitch.dbcommon.database.impl.DB2DatabaseOperator;
import com.gitee.dbswitch.dbcommon.database.impl.DmDatabaseOperator;
import com.gitee.dbswitch.dbcommon.database.impl.GreenplumDatabaseOperator;
import com.gitee.dbswitch.dbcommon.database.impl.HiveDatabaseOperator;
import com.gitee.dbswitch.dbcommon.database.impl.KingbaseDatabaseOperator;
import com.gitee.dbswitch.dbcommon.database.impl.MysqlDatabaseOperator;
import com.gitee.dbswitch.dbcommon.database.impl.OracleDatabaseOperator;
import com.gitee.dbswitch.dbcommon.database.impl.OscarDatabaseOperator;
import com.gitee.dbswitch.dbcommon.database.impl.PostgreSqlDatabaseOperator;
import com.gitee.dbswitch.dbcommon.database.impl.SqlServerDatabaseOperator;
import com.gitee.dbswitch.dbcommon.database.impl.SqliteDatabaseOperator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.sql.DataSource;

/**
 * 数据库操作器构造工厂类
 *
 * @author tang
 */
public final class DatabaseOperatorFactory {

  private static final Map<DatabaseTypeEnum, Function<DataSource, IDatabaseOperator>> DATABASE_OPERATOR_MAPPER
      = new HashMap<DatabaseTypeEnum, Function<DataSource, IDatabaseOperator>>() {

    private static final long serialVersionUID = -5278835613240515265L;

    {
      put(DatabaseTypeEnum.MYSQL, MysqlDatabaseOperator::new);
      put(DatabaseTypeEnum.MARIADB, MysqlDatabaseOperator::new);
      put(DatabaseTypeEnum.ORACLE, OracleDatabaseOperator::new);
      put(DatabaseTypeEnum.SQLSERVER, SqlServerDatabaseOperator::new);
      put(DatabaseTypeEnum.SQLSERVER2000, SqlServerDatabaseOperator::new);
      put(DatabaseTypeEnum.POSTGRESQL, PostgreSqlDatabaseOperator::new);
      put(DatabaseTypeEnum.GREENPLUM, GreenplumDatabaseOperator::new);
      put(DatabaseTypeEnum.DB2, DB2DatabaseOperator::new);
      put(DatabaseTypeEnum.DM, DmDatabaseOperator::new);
      put(DatabaseTypeEnum.KINGBASE, KingbaseDatabaseOperator::new);
      put(DatabaseTypeEnum.OSCAR, OscarDatabaseOperator::new);
      put(DatabaseTypeEnum.GBASE8A, MysqlDatabaseOperator::new);
      put(DatabaseTypeEnum.HIVE, HiveDatabaseOperator::new);
      put(DatabaseTypeEnum.SQLITE3, SqliteDatabaseOperator::new);
    }
  };

  /**
   * 根据数据源获取数据的读取操作器
   *
   * @param dataSource 数据库源
   * @return 指定类型的数据库读取器
   */
  public static IDatabaseOperator createDatabaseOperator(DataSource dataSource) {
    DatabaseTypeEnum type = DatabaseAwareUtils.getDatabaseTypeByDataSource(dataSource);
    if (!DATABASE_OPERATOR_MAPPER.containsKey(type)) {
      throw new RuntimeException(
          String.format("[dbcommon] Unsupported database type (%s)", type));
    }

    return DATABASE_OPERATOR_MAPPER.get(type).apply(dataSource);
  }

}
