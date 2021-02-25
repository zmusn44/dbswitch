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
import com.gitee.dbswitch.dbwriter.gpdb.GreenplumCopyWriterImpl;

public class KingbaseCopyWriterImpl extends GreenplumCopyWriterImpl implements IDatabaseWriter {

	public KingbaseCopyWriterImpl(DataSource dataSource) {
		super(dataSource);
	}

}
