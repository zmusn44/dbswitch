// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.dbsynch;

import java.util.Map;
import java.util.HashMap;
import javax.sql.DataSource;
import com.gitee.dbswitch.dbcommon.util.DatabaseAwareUtils;
import com.gitee.dbswitch.dbsynch.db2.DB2DatabaseSynchImpl;
import com.gitee.dbswitch.dbsynch.dm.DmDatabaseSynchImpl;
import com.gitee.dbswitch.dbsynch.kingbase.KingbaseDatabaseSynchImpl;
import com.gitee.dbswitch.dbsynch.mssql.SqlServerDatabaseSynchImpl;
import com.gitee.dbswitch.dbsynch.mysql.MySqlDatabaseSynchImpl;
import com.gitee.dbswitch.dbsynch.oracle.OracleDatabaseSynchImpl;
import com.gitee.dbswitch.dbsynch.pgsql.GreenplumDatabaseSynchImpl;
import com.gitee.dbswitch.dbsynch.pgsql.PostgresqlDatabaseSynchImpl;
import java.util.function.Function;

/**
 * 数据库同步器构造工厂类
 *
 * @author tang
 */
public final class DatabaseSynchronizeFactory {

    private static final Map<String, Function<DataSource, IDatabaseSynchronize>> DATABASE_SYNCH_MAPPER = new HashMap<String, Function<DataSource, IDatabaseSynchronize>>() {

        private static final long serialVersionUID = -2359773637275934408L;

        {
            put("MYSQL", MySqlDatabaseSynchImpl::new);
            put("ORACLE", OracleDatabaseSynchImpl::new);
            put("SQLSERVER", SqlServerDatabaseSynchImpl::new);
            put("POSTGRESQL", PostgresqlDatabaseSynchImpl::new);
            put("GREENPLUM", GreenplumDatabaseSynchImpl::new);
            put("DB2", DB2DatabaseSynchImpl::new);
            put("DM", DmDatabaseSynchImpl::new);
            put("KINGBASE", KingbaseDatabaseSynchImpl::new);
        }
    };

    /**
     * 获取指定数据源的同步器
     *
     * @param dataSource 数据源
     * @return 同步器对象
     */
    public static IDatabaseSynchronize createDatabaseWriter(DataSource dataSource) {
        String type = DatabaseAwareUtils.getDatabaseNameByDataSource(dataSource).toUpperCase();

        if (!DATABASE_SYNCH_MAPPER.containsKey(type)) {
            throw new RuntimeException(String.format("[dbsynch] Unkown Supported database type (%s)", type));
        }

        return DATABASE_SYNCH_MAPPER.get(type).apply(dataSource);
    }
}
