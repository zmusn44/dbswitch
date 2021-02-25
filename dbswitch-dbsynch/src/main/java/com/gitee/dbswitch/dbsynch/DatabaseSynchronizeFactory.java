// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Data : 2020/1/2
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
import java.lang.reflect.Constructor;

/**
 * 数据库同步器构造工厂类
 * 
 * @author tang
 *
 */
public final class DatabaseSynchronizeFactory {

	private static final Map<String, String> DATABASE_SYNCH_MAPPER = new HashMap<String, String>() {

		private static final long serialVersionUID = -2359773637275934408L;

		{
			put("MYSQL", MySqlDatabaseSynchImpl.class.getName());
			put("ORACLE", OracleDatabaseSynchImpl.class.getName());
			put("SQLSERVER", SqlServerDatabaseSynchImpl.class.getName());
			put("POSTGRESQL", PostgresqlDatabaseSynchImpl.class.getName());
			put("GREENPLUM", GreenplumDatabaseSynchImpl.class.getName());
			put("DB2",DB2DatabaseSynchImpl.class.getName());
			put("DM",DmDatabaseSynchImpl.class.getName());
			put("KINGBASE",KingbaseDatabaseSynchImpl.class.getName());
		}
	};

	/**
	 * 获取指定数据源的同步器
	 * 
	 * @param dataSource 数据源
	 * @return 同步器对象
	 */
	public static AbstractDatabaseSynchronize createDatabaseWriter(DataSource dataSource) {
		String type = DatabaseAwareUtils.getDatabaseNameByDataSource(dataSource).toUpperCase();

		if (DATABASE_SYNCH_MAPPER.containsKey(type)) {
			String className = DATABASE_SYNCH_MAPPER.get(type);
			try {
				Class<?>[] paramTypes = { DataSource.class };
				Object[] paramValues = { dataSource };
				Class<?> clas = Class.forName(className);
				Constructor<?> cons = clas.getConstructor(paramTypes);
				return (AbstractDatabaseSynchronize) cons.newInstance(paramValues);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		throw new RuntimeException(String.format("[dbsynch] Unkown Supported database type (%s)", type));
	}
}
