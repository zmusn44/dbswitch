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

  MYSQL(1, "mysql", "com.mysql.jdbc.Driver", "/* ping */ SELECT 1",
      "jdbc:mysql://{host}:{port>/{name}?useUnicode=true&characterEncoding=utf-8&useSSL=false&zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Shanghai&nullCatalogMeansCurrent=true&tinyInt1isBit=false"),
  MARIADB(2, "mariadb", "org.mariadb.jdbc.Driver", "SELECT 1",
      "jdbc:mariadb://{host}:{port}/{name}?useUnicode=true&characterEncoding=utf-8&useSSL=false&zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Shanghai&nullCatalogMeansCurrent=true&tinyInt1isBit=false"),
  ORACLE(3, "oracle", "oracle.jdbc.driver.OracleDriver", "SELECT 'Hello' from DUAL",
      "jdbc:oracle:thin:@{host}:{port}:{name}"),
  SQLSERVER(4, "sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "SELECT 1",
      "jdbc:sqlserver://{host}:{port};DatabaseName={name}"),
  POSTGRESQL(5, "postgresql", "org.postgresql.Driver", "SELECT 1",
      "jdbc:postgresql://{host}:{port}/{name}"),
  DB2(6, "db2", "com.ibm.db2.jcc.DB2Driver", "SELECT 1 FROM SYSIBM.SYSDUMMY1",
      "jdbc:db2://{host}:{port}/{name}:driverType=4;fullyMaterializeLobData=true;fullyMaterializeInputStreams=true;progressiveStreaming=2;progresssiveLocators=2;"),
  DM(7, "dm", "dm.jdbc.driver.DmDriver", "SELECT 'Hello' from DUAL", "jdbc:dm://{host}:{port}"),
  KINGBASE(8, "kingbase", "com.kingbase8.Driver", "SELECT 1",
      "jdbc:kingbase8://{host}:{port}/{name}"),
  ;

  private int id;
  private String name;
  private String driver;
  private String sql;
  private String template;

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
