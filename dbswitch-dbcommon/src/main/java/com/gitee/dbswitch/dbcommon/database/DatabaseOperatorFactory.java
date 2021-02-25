// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Data : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.dbcommon.database;

import java.util.Map;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import javax.sql.DataSource;
import com.gitee.dbswitch.dbcommon.database.impl.DB2DatabaseOperator;
import com.gitee.dbswitch.dbcommon.database.impl.DmDatabaseOperator;
import com.gitee.dbswitch.dbcommon.database.impl.GreenplumDatabaseOperator;
import com.gitee.dbswitch.dbcommon.database.impl.KingbaseDatabaseOperator;
import com.gitee.dbswitch.dbcommon.database.impl.MysqlDatabaseOperator;
import com.gitee.dbswitch.dbcommon.database.impl.OracleDatabaseOperator;
import com.gitee.dbswitch.dbcommon.database.impl.PostgreSqlDatabaseOperator;
import com.gitee.dbswitch.dbcommon.database.impl.SqlServerDatabaseOperator;
import com.gitee.dbswitch.dbcommon.util.DatabaseAwareUtils;

/**
 * 数据库操作器构造工厂类
 * 
 * @author tang
 *
 */
public final class DatabaseOperatorFactory {

	private static final Map<String, String> DATABASE_OPERATOR_MAPPER = new HashMap<String, String>() {

		private static final long serialVersionUID = -5278835613240515265L;

		{
			put("MYSQL", MysqlDatabaseOperator.class.getName());
			put("ORACLE", OracleDatabaseOperator.class.getName());
			put("SQLSERVER", SqlServerDatabaseOperator.class.getName());
			put("POSTGRESQL", PostgreSqlDatabaseOperator.class.getName());
			put("GREENPLUM", GreenplumDatabaseOperator.class.getName());
			put("DB2", DB2DatabaseOperator.class.getName());
			put("DM", DmDatabaseOperator.class.getName());
			put("KINGBASE", KingbaseDatabaseOperator.class.getName());
		}
	};

	/**
	 * 根据数据源获取数据的读取操作器
	 * 
	 * @param dataSource 数据库源
	 * @return 指定类型的数据库读取器
	 */
	public static IDatabaseOperator createDatabaseOperator(DataSource dataSource) {
		String type = DatabaseAwareUtils.getDatabaseNameByDataSource(dataSource).toUpperCase();
		if (DATABASE_OPERATOR_MAPPER.containsKey(type)) {
			String className = DATABASE_OPERATOR_MAPPER.get(type);
			try {
				Class<?> clazz = Class.forName(className);
				Class<?>[] paraTypes = { DataSource.class };
				Object[] paraValues = { dataSource };
				Constructor<?> cons = clazz.getConstructor(paraTypes);
				return (IDatabaseOperator) cons.newInstance(paraValues);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		throw new RuntimeException(String.format("[dbcommon] Unkown Supported database type (%s)", type));
	}

}
