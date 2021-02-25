// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Data : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.dbwriter.dm;

import javax.sql.DataSource;
import com.gitee.dbswitch.dbwriter.oracle.OracleWriterImpl;
import com.gitee.dbswitch.dbwriter.IDatabaseWriter;

public class DmWriterImpl extends OracleWriterImpl implements IDatabaseWriter {

	public DmWriterImpl(DataSource dataSource) {
		super(dataSource);
	}

}
