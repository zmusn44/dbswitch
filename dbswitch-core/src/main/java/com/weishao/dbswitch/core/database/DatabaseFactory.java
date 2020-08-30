// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Data : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.weishao.dbswitch.core.database;

import java.util.HashMap;
import java.util.Map;
import com.weishao.dbswitch.common.constant.DatabaseTypeEnum;
import com.weishao.dbswitch.core.database.impl.DatabaseGreenplumImpl;
import com.weishao.dbswitch.core.database.impl.DatabaseMysqlImpl;
import com.weishao.dbswitch.core.database.impl.DatabaseOracleImpl;
import com.weishao.dbswitch.core.database.impl.DatabasePostgresImpl;
import com.weishao.dbswitch.core.database.impl.DatabaseSqlserver2000Impl;
import com.weishao.dbswitch.core.database.impl.DatabaseSqlserverImpl;

/**
 * 数据库实例构建工厂类
 * @author tang
 *
 */
public final class DatabaseFactory {
	
	private static final Map<DatabaseTypeEnum,String> DATABASE_MAPPER=new HashMap<DatabaseTypeEnum, String>(){
		
		private static final long serialVersionUID = 9202705534880971997L;

	{  
	      put(DatabaseTypeEnum.MYSQL,DatabaseMysqlImpl.class.getName());
	      put(DatabaseTypeEnum.ORACLE,DatabaseOracleImpl.class.getName());
	      put(DatabaseTypeEnum.SQLSERVER2000,DatabaseSqlserver2000Impl.class.getName());
	      put(DatabaseTypeEnum.SQLSERVER,DatabaseSqlserverImpl.class.getName());
	      put(DatabaseTypeEnum.POSTGRESQL,DatabasePostgresImpl.class.getName());
	      put(DatabaseTypeEnum.GREENPLUM,DatabaseGreenplumImpl.class.getName());
	}}; 
	
	public static AbstractDatabase getDatabaseInstance(DatabaseTypeEnum type) {
		if(DATABASE_MAPPER.containsKey(type)) {
			String className= DATABASE_MAPPER.get(type);
			try {
				return (AbstractDatabase) Class.forName(className).newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		throw new RuntimeException(String.format("Unkown database type (%s)",type.name()));
	}
	
	private DatabaseFactory() {
		
	}
}
