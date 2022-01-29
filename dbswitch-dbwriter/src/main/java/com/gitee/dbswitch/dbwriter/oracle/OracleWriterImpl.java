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
import org.apache.commons.lang3.StringUtils;

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
    /**  处理Oracle的Clob/Array类型需写入String类型的数据的问题   */
    recordValues.parallelStream().forEach((Object[] row) -> {
      for (int i = 0; i < row.length; ++i) {
        try {
          row[i] = convert(row[i]);
        } catch (Exception e) {
          log.warn("Field Value convert from {} to java.lang.String failed, field name is : {} ",
              row[i].getClass().getName(), fieldNames.get(i));
          row[i] = null;
        }
      }
    });

    return super.write(fieldNames, recordValues);
  }

  private Object convert(Object o) {
    if (null == o) {
      return null;
    }

    if (o instanceof Clob) {
      /**
       * 将java.sql.Clob 类型转换为java.lang.String
       */
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
    } else if (o instanceof java.sql.Array) {
      /**
       * 将java.sql.Array 类型转换为java.lang.String
       * <p>
       *  Oracle 没有数组类型
       */
      String v = o.toString();
      String a = o.getClass().getName() + "@" + Integer.toHexString(o.hashCode());
      if (a.length() == v.length() && StringUtils.equals(a, v)) {
        log.warn("Unsupported type for convert {} to java.lang.String", o.getClass().getName());
        return null;
      }

      return v;
    } else {
      return o;
    }
  }

}
