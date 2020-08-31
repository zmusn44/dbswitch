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

import java.util.List;

/**
 * 计算结果行记录处理器
 * 
 * @author tang
 *
 */
public interface IDatabaseRowHandler {

	/**
	 * 行数据处理
	 * 
	 * @param fields 字段名称列表，该列表只读
	 * @param record 一条数据记实录
	 * @param flag   数据变化状态
	 */
	public void handle(List<String> fields, Object[] record, RecordChangeTypeEnum flag);

	/**
	 * 计算结束通知
	 * 
	 * @param fields 字段名称列表，该列表只读
	 */
	public void destroy(List<String> fields);
}