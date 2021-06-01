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

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import com.gitee.dbswitch.dbwriter.AbstractDatabaseWriter;
import com.gitee.dbswitch.dbwriter.IDatabaseWriter;
import lombok.extern.slf4j.Slf4j;

/**
 * Oracle数据库写入实现类
 * 
 * @author tang
 *
 */
@Slf4j
public class OracleWriterImpl extends AbstractDatabaseWriter implements IDatabaseWriter {

	public OracleWriterImpl(DataSource dataSource) {
		super(dataSource);
	}

	@Override
	public long write(List<String> fieldNames, List<Object[]> recordValues) {
		String schemaName = Objects.requireNonNull(this.schemaName, "schema-name名称为空，不合法!");
		String tableName = Objects.requireNonNull(this.tableName, "table-name名称为空，不合法!");

		List<String> placeHolders = Collections.nCopies(fieldNames.size(), "?");

		String sqlInsert = String.format("INSERT INTO \"%s\".\"%s\" ( \"%s\" ) VALUES ( %s )", schemaName, tableName,
				StringUtils.join(fieldNames, "\",\""), StringUtils.join(placeHolders, ","));

		int[] argTypes = new int[fieldNames.size()];
		for (int i = 0; i < fieldNames.size(); ++i) {
			String col = fieldNames.get(i);
			argTypes[i] = this.columnType.get(col);
		}

		/**  处理Oracle的Clob类型需写入String类型的数据的问题   */
		recordValues.parallelStream().forEach((Object[] row) -> {
			for (int i = 0; i < row.length; ++i) {
				try {
					row[i] = convertClobToString(row[i]);
				} catch (Exception e) {
					log.warn("Field Value convert from java.sql.Clob to java.lang.String failed, field name is : {} ", fieldNames.get(i));
					row[i] = null;
				}
			}
		});

		DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
		definition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
		definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(this.dataSource);
		TransactionStatus status = transactionManager.getTransaction(definition);

		try {
			int affectCount = 0;
			jdbcTemplate.batchUpdate(sqlInsert, recordValues, argTypes);
			affectCount = recordValues.size();
			recordValues.clear();
			transactionManager.commit(status);

			if (log.isDebugEnabled()) {
				log.debug("Oracle insert write data  affect count:{}", affectCount);
			}

			return affectCount;
		} catch (TransactionException e) {
			transactionManager.rollback(status);
			throw e;
		} catch (Exception e) {
			transactionManager.rollback(status);
			throw e;
		}

	}

	/**
	 * 将java.sql.Clob 类型转换为java.lang.String
	 * @param o
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	private Object convertClobToString(Object o) throws SQLException, IOException {
		if (null == o) {
			return null;
		}

		if (o instanceof Clob) {
			Clob clob = (Clob) o;
			java.io.Reader is = null;
			java.io.BufferedReader reader = null;
			try {
				is = clob.getCharacterStream();
				reader = new java.io.BufferedReader(is);
				String line = reader.readLine();
				StringBuilder sb = new StringBuilder();
				while (line != null) {
					sb.append(line);
					line = reader.readLine();
				}
				return sb.toString();
			} catch (SQLException | java.io.IOException e) {
				throw new RuntimeException(e);
			} finally {
				try {
					if (null != reader) {
						reader.close();
					}
					if (null != is) {
						is.close();
					}
				} catch (Exception ex) {
				}
			}
		}

		return o;
	}

}
