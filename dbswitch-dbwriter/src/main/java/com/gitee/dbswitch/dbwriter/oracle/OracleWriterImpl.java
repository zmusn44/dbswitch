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
import com.gitee.dbswitch.dbwriter.util.ObjectCastUtils;
import java.sql.Types;
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
    /**
     * 将java.sql.Array 类型转换为java.lang.String
     * <p>
     *  Oracle 没有数组类型，这里以文本类型进行存在
     */
    recordValues.parallelStream().forEach((Object[] row) -> {
      for (int i = 0; i < row.length; ++i) {
        try {
          int dataType = this.columnType.get(fieldNames.get(i));
          switch (dataType) {
            // oracle.jdbc.driver.OraclePreparedStatement.setObjectCritical
            case Types.BLOB:
              // 需要oracle.sql.BLOB类型
              log.warn("Unsupported type for convert {} to oracle.sql.BLOB",
                  row[i].getClass().getName());
              row[i] = null;
              break;
            case Types.ROWID:
            case Types.ARRAY:
            case Types.NCLOB:
            case Types.REF:
            case Types.SQLXML:
              row[i] = null;
              break;
            default:
              row[i] = ObjectCastUtils.castByDetermine(row[i]);
              break;
          }
        } catch (Exception e) {
          row[i] = null;
        }
      }
    });

    return super.write(fieldNames, recordValues);
  }
}
