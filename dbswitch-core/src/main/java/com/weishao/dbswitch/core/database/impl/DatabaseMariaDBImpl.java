// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Data : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.weishao.dbswitch.core.database.impl;

import java.util.List;
import com.weishao.dbswitch.common.constant.DatabaseTypeEnum;
import com.weishao.dbswitch.core.model.ColumnDescription;

/**
 * 支持MariaDB数据库的元信息实现
 * 
 * @author tang
 *
 */
public class DatabaseMariaDBImpl extends DatabaseMysqlImpl {

	public DatabaseMariaDBImpl() {
		super("org.mariadb.jdbc.Driver");
	}
	
	@Override
	public List<ColumnDescription> querySelectSqlColumnMeta(String sql) {
		String querySQL = String.format(" %s LIMIT 0 ", sql.replace(";", ""));
		return this.getSelectSqlColumnMeta(querySQL, DatabaseTypeEnum.MARIADB);
	}
}