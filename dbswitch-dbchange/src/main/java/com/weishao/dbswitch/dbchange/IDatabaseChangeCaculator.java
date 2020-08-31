// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Data : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.weishao.dbswitch.dbchange;

import com.weishao.dbswitch.dbchange.pojo.TaskParamBean;

/**
 * 变化量计算器接口定义
 * 
 * @author tang
 *
 */
public interface IDatabaseChangeCaculator {

	/**
	 * 是否记录无变化的数据
	 * 
	 * @return 是否记录无变化的数据
	 */
	public boolean isRecordIdentical();

	/**
	 * 设置是否记录无变化的数据
	 * 
	 * @param recordOrNot 是否记录无变化的数据
	 */
	public void setRecordIdentical(boolean recordOrNot);
	
	/**
	 * 是否进行Jdbc的数据类型检查
	 * 
	 * @return 是否进行检查
	 */
	public boolean isCheckJdbcType();

	/**
	 * 设置是否进行Jdbc的数据类型检查
	 * 
	 * @param checkOrNot 是否进行检查
	 */
	public void setCheckJdbcType(boolean checkOrNot);

	/**
	 * 获取JDBC驱动批量读取数据的行数大小
	 * 
	 * @return 批量行数大小
	 */
	public int getFetchSize();

	/**
	 * 设置JDBC驱动批量读取数据的行数大小
	 * 
	 * @param size 批量行数大小
	 */
	public void setFetchSize(int size);

	/**
	 * 执行变化量计算任务
	 * 
	 * @param task    任务描述实体对象
	 * @param handler 计算结果回调处理器
	 */
	public void executeCalculate(TaskParamBean task, IDatabaseRowHandler handler);
}