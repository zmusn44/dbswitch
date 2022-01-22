// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.dbwriter.oracle;

import com.gitee.dbswitch.dbwriter.AbstractDatabaseWriter;
import com.gitee.dbswitch.dbwriter.IDatabaseWriter;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

/**
 * Oracle数据库写入实现类
 *
 * @author tang
 */
@Slf4j
public class OracleWriterImpl extends AbstractDatabaseWriter implements IDatabaseWriter {

  public OracleWriterImpl(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  protected String getDatabaseProductName() {
    return "Oracle";
  }

  @Override
  public long write(List<String> fieldNames, List<Object[]> recordValues) {
    /**  处理Oracle的Clob类型需写入String类型的数据的问题   */
    recordValues.parallelStream().forEach((Object[] row) -> {
      for (int i = 0; i < row.length; ++i) {
        try {
          row[i] = convertClobToString(row[i]);
        } catch (Exception e) {
          log.warn(
              "Field Value convert from java.sql.Clob to java.lang.String failed, field name is : {} ",
              fieldNames.get(i));
          row[i] = null;
        }
      }
    });

    return super.write(fieldNames, recordValues);
  }

  /**
   * 将java.sql.Clob 类型转换为java.lang.String
   */
  private Object convertClobToString(Object o) {
    if (null == o) {
      return null;
    }

    if (o instanceof Clob) {
      Clob clob = (Clob) o;
      try (java.io.Reader is = clob.getCharacterStream();
          java.io.BufferedReader reader = new java.io.BufferedReader(is)) {
        String line = reader.readLine();
        StringBuilder sb = new StringBuilder();
        while (line != null) {
          sb.append(line);
          line = reader.readLine();
        }
        return sb.toString();
      } catch (SQLException | java.io.IOException e) {
        throw new RuntimeException(e);
      }
    }

    return o;
  }

}
