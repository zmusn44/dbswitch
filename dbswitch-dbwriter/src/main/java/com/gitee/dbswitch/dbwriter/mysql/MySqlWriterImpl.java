// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.dbwriter.mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.*;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import com.gitee.dbswitch.dbwriter.AbstractDatabaseWriter;
import com.gitee.dbswitch.dbwriter.IDatabaseWriter;
import lombok.extern.slf4j.Slf4j;

/**
 * MySQL数据库写入实现类
 * 
 * @author tang
 *
 */
@Slf4j
public class MySqlWriterImpl extends AbstractDatabaseWriter implements IDatabaseWriter {

	private DefaultTransactionDefinition definition;

	public MySqlWriterImpl(DataSource dataSource) {
		super(dataSource);

		this.definition = new DefaultTransactionDefinition();
		this.definition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
		this.definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		this.definition.setTimeout(3600);
	}

	@Override
	public void prepareWrite(String schemaName, String tableName) {
		String sql = String.format("SELECT *  FROM `%s`.`%s`  WHERE 1=2", schemaName, tableName);
		Map<String, Integer> columnMetaData = new HashMap<>();
		Boolean ret = this.jdbcTemplate.execute((Connection conn) -> {
			Statement stmt = null;
			ResultSet rs = null;
			try {
				stmt = conn.createStatement();
				rs = stmt.executeQuery(sql);
				ResultSetMetaData rsMetaData = rs.getMetaData();
				for (int i = 0, len = rsMetaData.getColumnCount(); i < len; i++) {
					columnMetaData.put(rsMetaData.getColumnName(i + 1), rsMetaData.getColumnType(i + 1));
				}

				return true;
			} catch (Exception e) {
				throw new RuntimeException(String.format("获取表:%s.%s 的字段的元信息时失败. 请联系 DBA 核查该库、表信息.", schemaName, tableName), e);
			} finally {
				JdbcUtils.closeResultSet(rs);
				JdbcUtils.closeStatement(stmt);
			}
		});

		if (ret.booleanValue()) {
			this.schemaName = schemaName;
			this.tableName = tableName;
			this.columnType = Objects.requireNonNull(columnMetaData);

			if (this.columnType.isEmpty()) {
				throw new RuntimeException(
						String.format("获取表:%s.%s 的字段的元信息时失败. 请联系 DBA 核查该库、表信息.", schemaName, tableName));
			}
		} else {
			throw new RuntimeException("内部代码出现错误，请开发人员排查！");
		}
	}

	@Override
	public long write(List<String> fieldNames, List<Object[]> recordValues) {
		String schemaName = Objects.requireNonNull(this.schemaName, "schema-name名称为空，不合法!");
		String tableName = Objects.requireNonNull(this.tableName, "table-name名称为空，不合法!");

		List<String> placeHolders = Collections.nCopies(fieldNames.size(), "?");

		String sqlInsert = String.format("INSERT INTO `%s`.`%s` ( `%s` ) VALUES ( %s )", schemaName, tableName,
				StringUtils.join(fieldNames, "`,`"), StringUtils.join(placeHolders, ","));

		int[] argTypes = new int[fieldNames.size()];
		for (int i = 0; i < fieldNames.size(); ++i) {
			String col = fieldNames.get(i);
			argTypes[i] = this.columnType.get(col);
		}

		PlatformTransactionManager transactionManager = new DataSourceTransactionManager(this.dataSource);
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager, definition);
		Integer ret = transactionTemplate.execute((TransactionStatus transactionStatus) -> {
			try {
				int[] affects = jdbcTemplate.batchUpdate(sqlInsert, recordValues, argTypes);
				int affectCount = 0;
				for (int i : affects) {
					affectCount += i;
				}
				return affectCount;
			} catch (Throwable t) {
				transactionStatus.setRollbackOnly();
				throw t;
			}
		});

		recordValues.clear();
		if (log.isDebugEnabled()) {
			log.debug("MySQL insert write data  affect count:{}", ret.longValue());
		}

		return ret.longValue();
	}

}
