// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Data : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.dbwriter.kingbase;

import javax.sql.DataSource;
import com.gitee.dbswitch.dbwriter.IDatabaseWriter;
import com.gitee.dbswitch.dbwriter.gpdb.GreenplumInsertWriterImpl;

public class KingbaseInsertWriterImpl  extends GreenplumInsertWriterImpl implements IDatabaseWriter {

	public KingbaseInsertWriterImpl(DataSource dataSource) {
		super(dataSource);
	}
}
