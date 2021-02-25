// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Data : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.dbwriter;

import java.util.Map;
import java.util.HashMap;
import javax.sql.DataSource;
import com.gitee.dbswitch.dbcommon.util.DatabaseAwareUtils;
import java.lang.reflect.Constructor;

/**
 * 数据库写入器构造工厂类
 * 
 * @author tang
 *
 */
public class DatabaseWriterFactory {

	private static final Map<String, String> DATABASE_WRITER_MAPPER = new HashMap<String, String>() {

		private static final long serialVersionUID = 3365136872693503697L;

		{
			put("MYSQL", "com.gitee.dbswitch.dbwriter.mysql.MySqlWriterImpl");
			put("ORACLE", "com.gitee.dbswitch.dbwriter.oracle.OracleWriterImpl");
			put("SQLSERVER", "com.gitee.dbswitch.dbwriter.mssql.SqlServerWriterImpl");
			put("POSTGRESQL", "com.gitee.dbswitch.dbwriter.gpdb.GreenplumCopyWriterImpl");
			put("GREENPLUM", "com.gitee.dbswitch.dbwriter.gpdb.GreenplumCopyWriterImpl");
			put("DB2", "com.gitee.dbswitch.dbwriter.db2.DB2WriterImpl");
			put("DM", "com.gitee.dbswitch.dbwriter.dm.DmWriterImpl");
			//对于kingbase当前只能使用insert模式
			put("KINGBASE", "com.gitee.dbswitch.dbwriter.kingbase.KingbaseInsertWriterImpl");
		}
	};

	/**
	 * 获取指定数据库类型的写入器
	 * 
	 * @param dataSource 连接池数据源
	 * @return 写入器对象
	 */
	public static AbstractDatabaseWriter createDatabaseWriter(DataSource dataSource) {
		return DatabaseWriterFactory.createDatabaseWriter(dataSource, false);
	}

	/**
	 * 获取指定数据库类型的写入器
	 * 
	 * @param dataSource 连接池数据源
	 * @param insert     对于GP/GP数据库来说是否使用insert引擎写入
	 * @return 写入器对象
	 */
	public static AbstractDatabaseWriter createDatabaseWriter(DataSource dataSource, boolean insert) {
		String type = DatabaseAwareUtils.getDatabaseNameByDataSource(dataSource).toUpperCase();
		if (insert) {
			if ("POSTGRESQL".equalsIgnoreCase(type) || "GREENPLUM".equalsIgnoreCase(type)) {
				return new com.gitee.dbswitch.dbwriter.gpdb.GreenplumInsertWriterImpl(dataSource);
			}
			
			if ("KINGBASE".equalsIgnoreCase(type)) {
				return new com.gitee.dbswitch.dbwriter.kingbase.KingbaseInsertWriterImpl(dataSource);
			}
		}

		if (DATABASE_WRITER_MAPPER.containsKey(type.trim())) {
			String className = DATABASE_WRITER_MAPPER.get(type);
			try {
				Class<?>[] paraTypes = { DataSource.class };
				Object[] paraValues = { dataSource };
				Class<?> clas = Class.forName(className);
				Constructor<?> cons = clas.getConstructor(paraTypes);
				AbstractDatabaseWriter process = (AbstractDatabaseWriter) cons.newInstance(paraValues);
				return process;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		throw new RuntimeException(String.format("[dbwrite] Unkown Supported database type (%s)", type));
	}

}
