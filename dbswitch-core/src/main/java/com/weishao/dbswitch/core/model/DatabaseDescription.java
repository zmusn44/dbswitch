// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Data : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.weishao.dbswitch.core.model;

import com.weishao.dbswitch.common.constant.DatabaseTypeEnum;

/**
 * 数据库连接描述符信息定义(Database Description)
 * @author tang
 *
 */
public class DatabaseDescription {
	protected DatabaseTypeEnum type;
	protected String host;
	protected int port;
	protected String mode;//对于Oracle数据库的模式，可取范围为：sid,servicename,tnsname三种
	protected String dbname;
	protected String charset;
	protected String username;
	protected String password;
	
	public DatabaseDescription(String dbtype, String host, int port, String mode, String dbname, String charset,String username, String password) {
		this.type = DatabaseTypeEnum.valueOf(dbtype.toUpperCase());
		this.host = host;
		this.port = port;
		this.mode = mode;
		this.dbname = dbname;
		this.charset = charset;
		this.username = username;
		this.password = password;
	}
	
	public DatabaseTypeEnum getType() {
		return type;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getMode() {
		return mode;
	}
	
	public String getDbname() {
		return dbname;
	}

	public String getCharset() {
		return charset;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	@Override
	public String toString() {
		return "DatabaseDescription [type=" + type + ", host=" + host + ", port=" + port + ", mode=" + mode
				+ ", dbname=" + dbname + ", charset=" + charset + ", username=" + username + ", password=" + password
				+ "]";
	}

}
