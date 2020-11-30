// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Data : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.dbwriter.gpdb;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import com.gitee.dbswitch.dbwriter.AbstractDatabaseWriter;
import com.gitee.dbswitch.dbwriter.IDatabaseWriter;
import lombok.extern.slf4j.Slf4j;

/**
 * Greenplum数据库Insert写入实现类
 * 
 * @author tang
 *
 */
@Slf4j
public class GreenplumInsertWriterImpl extends AbstractDatabaseWriter implements IDatabaseWriter {

	public GreenplumInsertWriterImpl(DataSource dataSource) {
		super(dataSource);
	}

	@Override
	public long write(List<String> fieldNames, List<Object[]> recordValues) {
		List<String> placeHolders = new ArrayList<String>();
		for (int i = 0; i < fieldNames.size(); ++i) {
			placeHolders.add("?");
		}

		String schemaName = Objects.requireNonNull(this.schemaName, "schema-name名称为空，不合法!");
		String tableName = Objects.requireNonNull(this.tableName, "table-name名称为空，不合法!");
		String sqlInsert = String.format("INSERT INTO \"%s\".\"%s\" ( \"%s\" ) VALUES ( %s )", schemaName, tableName,
				StringUtils.join(fieldNames, "\",\""), StringUtils.join(placeHolders, ","));

		int[] argTypes = new int[fieldNames.size()];
		for (int i = 0; i < fieldNames.size(); ++i) {
			String col = fieldNames.get(i);
			argTypes[i] = this.columnType.get(col);
		}

		DefaultTransactionDefinition defination = new DefaultTransactionDefinition();
		defination.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
		defination.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		PlatformTransactionManager transactionManager = new DataSourceTransactionManager(
				this.jdbcTemplate.getDataSource());
		TransactionStatus status = transactionManager.getTransaction(defination);

		try {
			int[] affects = jdbcTemplate.batchUpdate(sqlInsert, recordValues, argTypes);
			int affectCount = 0;
			for (int i : affects) {
				affectCount += i;
			}

			recordValues.clear();
			transactionManager.commit(status);
			if (log.isDebugEnabled()) {
				log.debug("Greenplum insert data  affect count:{}", affectCount);
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

}
