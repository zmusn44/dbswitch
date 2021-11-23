// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.admin.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.StringUtils;

@Getter
@AllArgsConstructor
public enum SupportDbTypeEnum {

  MYSQL(1, "mysql", "com.mysql.jdbc.Driver", 3306, "/* ping */ SELECT 1",
      new String[]{"jdbc:mysql://{host}[:{port}]/[{database}][\\?{params}]"}),
  MARIADB(2, "mariadb", "org.mariadb.jdbc.Driver", 3306, "SELECT 1",
      new String[]{"jdbc:mariadb://{host}[:{port}]/[{database}][\\?{params}]"}),
  ORACLE(3, "oracle", "oracle.jdbc.driver.OracleDriver", 1521, "SELECT 'Hello' from DUAL",
      new String[]{"jdbc:oracle:thin:@{host}:{port}:{name}",
          "jdbc:oracle:thin:@//{host}[:{port}]/{name}"}),
  SQLSERVER(4, "sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver", 1433, "SELECT 1+2 as a",
      new String[]{"jdbc:sqlserver://{host}[:{port}][;databaseName={database}][;{params}]"}),
  POSTGRESQL(5, "postgresql", "org.postgresql.Driver", 5432, "SELECT 1",
      new String[]{"jdbc:postgresql://{host}[:{port}]/[{database}][\\?{params}]"}),
  DB2(6, "db2", "com.ibm.db2.jcc.DB2Driver", 50000, "SELECT 1 FROM SYSIBM.SYSDUMMY1",
      new String[]{"jdbc:db2://{host}:{port}/{database}[:{params}]"}),
  DM(7, "dm", "dm.jdbc.driver.DmDriver", 5236, "SELECT 'Hello' from DUAL",
      new String[]{"jdbc:dm://{host}:{port}[/{database}][\\?{params}]"}),
  KINGBASE(8, "kingbase", "com.kingbase8.Driver", 54321, "SELECT 1",
      new String[]{"jdbc:kingbase8://{host}[:{port}]/[{database}][\\?{params}]"}),
  ;

  private int id;
  private String name;
  private String driver;
  private int port;
  private String sql;
  private String[] url;

  public static SupportDbTypeEnum of(String name) {
    if (!StringUtils.isEmpty(name)) {
      for (SupportDbTypeEnum type : SupportDbTypeEnum.values()) {
        if (type.getName().equalsIgnoreCase(name)) {
          return type;
        }
      }
    }

    throw new IllegalArgumentException("cannot find enum name: " + name);
  }

}
