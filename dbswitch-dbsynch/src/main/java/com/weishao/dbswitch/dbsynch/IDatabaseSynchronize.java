// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Data : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.weishao.dbswitch.dbsynch;

import java.util.List;
import javax.sql.DataSource;

/**
 * 数据同步接口定义
 * 
 * @author tang
 *
 */
public interface IDatabaseSynchronize {

	/**
	 * 获取数据源对象
	 * 
	 * @return DataSource数据源对象
	 */
	DataSource getDataSource();

	/**
	 * 批量Insert/Update/Delete预处理
	 * 
	 * @param schemaName schema名称
	 * @param tableName  table名称
	 * @param fieldNames 字段列表
	 * @param pks        主键字段列表
	 */
	void prepare(String schemaName, String tableName, List<String> fieldNames, List<String> pks);

	/**
	 * 批量数据Insert
	 * 
	 * @param records 数据记录
	 * @return 返回实际影响的记录条数
	 */
	long executeInsert(List<Object[]> records);

	/**
	 * 批量数据Update
	 * 
	 * @param records 数据记录
	 * @return 返回实际影响的记录条数
	 */
	long executeUpdate(List<Object[]> records);

	/**
	 * 批量数据Delete
	 * 
	 * @param records 数据记录
	 * @return 返回实际影响的记录条数
	 */
	long executeDelete(List<Object[]> records);
}