// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Data : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.data.service;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitee.dbswitch.common.constant.DatabaseTypeEnum;
import com.gitee.dbswitch.common.util.CommonUtils;
import com.gitee.dbswitch.core.model.ColumnDescription;
import com.gitee.dbswitch.core.model.TableDescription;
import com.gitee.dbswitch.core.service.IMetaDataService;
import com.gitee.dbswitch.data.config.DbswichProperties;
import com.gitee.dbswitch.data.util.JdbcTemplateUtils;
import com.gitee.dbswitch.dbchange.ChangeCaculatorService;
import com.gitee.dbswitch.dbchange.IDatabaseChangeCaculator;
import com.gitee.dbswitch.dbchange.IDatabaseRowHandler;
import com.gitee.dbswitch.dbchange.RecordChangeTypeEnum;
import com.gitee.dbswitch.dbchange.pojo.TaskParamBean;
import com.gitee.dbswitch.dbcommon.database.DatabaseOperatorFactory;
import com.gitee.dbswitch.dbcommon.database.IDatabaseOperator;
import com.gitee.dbswitch.dbcommon.pojo.StatementResultSet;
import com.gitee.dbswitch.dbcommon.util.JdbcMetaDataUtils;
import com.gitee.dbswitch.dbsynch.DatabaseSynchronizeFactory;
import com.gitee.dbswitch.dbsynch.IDatabaseSynchronize;
import com.gitee.dbswitch.dbwriter.DatabaseWriterFactory;
import com.gitee.dbswitch.dbwriter.IDatabaseWriter;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

/**
 * 数据迁移主逻辑类
 * 
 * @author tang
 *
 */
@Slf4j
@Service("MainService")
public class MainService {

	private ObjectMapper jackson = new ObjectMapper();

	@Autowired
	private DbswichProperties properties;

	@Autowired
	private IMetaDataService metaDataService;

	/**
	 * 执行主逻辑
	 */
	public void run() {
		StopWatch watch = new StopWatch();
		watch.start();

		try {
			HikariDataSource targetDataSource = this.createTargetDataSource(properties.getTarget());

			IDatabaseWriter writer = DatabaseWriterFactory.createDatabaseWriter(targetDataSource,
					properties.getTarget().getWriterEngineInsert().booleanValue());
			
			log.info("service is running....");
			//log.info("Application properties configuration :{}", jackson.writeValueAsString(properties));
			
			List<DbswichProperties.SourceDataSourceProperties> sourcesProperties = properties.getSource();

			for (DbswichProperties.SourceDataSourceProperties sourceProperties : sourcesProperties) {
				
				HikariDataSource sourceDataSource = this.createSourceDataSource(sourceProperties);

				metaDataService.setDatabaseConnection(JdbcTemplateUtils.getDatabaseProduceName(sourceDataSource));
				
				// 判断处理的策略：是排除还是包含
				List<String> includes = stringToList(sourceProperties.getSourceIncludes());
				log.info("Includes tables is :{}", jackson.writeValueAsString(includes));
				List<String> filters = stringToList(sourceProperties.getSourceExcludes());
				log.info("Filter tables is :{}", jackson.writeValueAsString(filters));

				boolean useExcludeTables = includes.isEmpty();
				if (useExcludeTables) {
					log.info("!!!! Use dbswitch.source[{}].source-excludes to filter tables", sourcesProperties.indexOf(sourceProperties));
				} else {
					log.info("!!!! Use dbswitch.source[{}].source-includes to filter tables", sourcesProperties.indexOf(sourceProperties));
				}

				List<String> schemas = stringToList(sourceProperties.getSourceSchema());
				log.info("Source schema names is :{}", jackson.writeValueAsString(schemas));
				for (String schema : schemas) {
					// 读取源库指定schema里所有的表
					List<TableDescription> tableList = metaDataService.queryTableList(sourceProperties.getUrl(),
							sourceProperties.getUsername(), sourceProperties.getPassword(), schema);
					if (tableList.isEmpty()) {
						log.warn("### Find source database table list empty for shema={}", schema);
					} else {
						int finished = 0;
						for (TableDescription td : tableList) {
							String tableName = td.getTableName();
							if (useExcludeTables) {
								if (!filters.contains(tableName)) {
									this.doDataMigration(td, sourceProperties, sourceDataSource, writer);
								}
							} else {
								if (includes.contains(tableName)) {
									this.doDataMigration(td, sourceProperties, sourceDataSource, writer);
								}
							}

							log.info(
									"#### Complete data migration for schema [ {} ] count is {},total is {}, process is {}%",
									schema, ++finished, tableList.size(),
									(float) (finished * 100.0 / tableList.size()));
						}
					}

				}
				
				try {
					sourceDataSource.close();
				} catch (Exception e) {
					log.warn("Close data source error:",e);
				}
			}
			log.info("service run success!");
		} catch (Exception e) {
			log.error("error:", e);
		} finally {
			watch.stop();
			log.info("total elipse = {} s", watch.getTotalTimeSeconds());
		}
	}

	/**
	 * 迁移每张表的结构与数据
	 * 
	 * @param tableDescription
	 * @param writer
	 */
	private void doDataMigration(TableDescription tableDescription,
			DbswichProperties.SourceDataSourceProperties sourceProperties, HikariDataSource sourceDataSource,
			IDatabaseWriter writer) {
		log.info("Migrate table for {}.{} ", tableDescription.getSchemaName(), tableDescription.getTableName());
		JdbcTemplate targetJdbcTemplate = new JdbcTemplate(writer.getDataSource());
		DatabaseTypeEnum targetDatabaseType = JdbcTemplateUtils.getDatabaseProduceName(writer.getDataSource());

		if (properties.getTarget().getTargetDrop().booleanValue()) {
			/**
			 * 如果配置了dbswitch.target.datasource-target-drop=true时，先执行drop table语句，然后执行create
			 * table语句
			 */

			// 先drop表
			try {
				IDatabaseOperator targetOperator = DatabaseOperatorFactory.createDatabaseOperator(writer.getDataSource());
				targetOperator.dropTable(properties.getTarget().getTargetSchema(),
						sourceProperties.getPrefixTable() + tableDescription.getTableName());
			} catch (Exception e) {
				log.info("Target Table {}.{} is not exits!", properties.getTarget().getTargetSchema(),
						sourceProperties.getPrefixTable() + tableDescription.getTableName());
			}

			// 然后create表
			List<ColumnDescription> columnDescs = metaDataService.queryTableColumnMeta(sourceProperties.getUrl(),
					sourceProperties.getUsername(), sourceProperties.getPassword(), tableDescription.getSchemaName(),
					tableDescription.getTableName());
			List<String> primaryKeys = metaDataService.queryTablePrimaryKeys(sourceProperties.getUrl(),
					sourceProperties.getUsername(), sourceProperties.getPassword(), tableDescription.getSchemaName(),
					tableDescription.getTableName());
			String sqlCreateTable = metaDataService.getDDLCreateTableSQL(targetDatabaseType, columnDescs, primaryKeys,
					properties.getTarget().getTargetSchema(),
					sourceProperties.getPrefixTable() + tableDescription.getTableName(),
					properties.getTarget().getCreateTableAutoIncrement().booleanValue());
			targetJdbcTemplate.execute(sqlCreateTable);
			log.info("Execute SQL: \n{}", sqlCreateTable);

			this.doFullCoverSynchronize(tableDescription, sourceProperties, sourceDataSource, writer);
		} else {
			// 判断是否具备变化量同步的条件：（1）两端表结构一致，且都有一样的主键字段；(2)MySQL使用Innodb引擎；
			if (properties.getTarget().getChangeDataSynch().booleanValue()) {
				// 根据主键情况判断同步的方式：增量同步或覆盖同步
				JdbcMetaDataUtils mds = new JdbcMetaDataUtils(sourceDataSource);
				JdbcMetaDataUtils mdt = new JdbcMetaDataUtils(writer.getDataSource());
				List<String> pks1 = mds.queryTablePrimaryKeys(tableDescription.getSchemaName(),
						tableDescription.getTableName());
				List<String> pks2 = mdt.queryTablePrimaryKeys(properties.getTarget().getTargetSchema(),
						sourceProperties.getPrefixTable() + tableDescription.getTableName());

				if (!pks1.isEmpty() && !pks2.isEmpty() && pks1.containsAll(pks2) && pks2.containsAll(pks1)) {
					if (targetDatabaseType == DatabaseTypeEnum.MYSQL
							&& !isMysqlInodbStorageEngine(properties.getTarget().getTargetSchema(),
									tableDescription.getTableName(), writer.getDataSource())) {
						this.doFullCoverSynchronize(tableDescription, sourceProperties, sourceDataSource, writer);
					} else {
						List<String> fields = mds.queryTableColumnName(tableDescription.getSchemaName(),
								tableDescription.getTableName());
						this.doIncreaseSynchronize(tableDescription, sourceProperties, sourceDataSource, writer, pks1,
								fields);
					}
				} else {
					this.doFullCoverSynchronize(tableDescription, sourceProperties, sourceDataSource, writer);
				}
			} else {
				this.doFullCoverSynchronize(tableDescription, sourceProperties, sourceDataSource, writer);
			}
		}
	}

	/**
	 * 执行覆盖同步
	 * 
	 * @param tableDescription 表的描述信息，可能是视图表，可能是物理表
	 * @param writer           目的端的写入器
	 */
	private void doFullCoverSynchronize(TableDescription tableDescription,
			DbswichProperties.SourceDataSourceProperties sourceProperties, HikariDataSource sourceDataSource,
			IDatabaseWriter writer) {
		int fetchSize = 100;
		if (sourceProperties.getFetchSize() >= fetchSize) {
			fetchSize = sourceProperties.getFetchSize();
		}
		final int BATCH_SIZE = fetchSize;

		// 准备目的端的数据写入操作
		writer.prepareWrite(properties.getTarget().getTargetSchema(), sourceProperties.getPrefixTable() + tableDescription.getTableName());

		// 清空目的端表的数据
		IDatabaseOperator targetOperator = DatabaseOperatorFactory.createDatabaseOperator(writer.getDataSource());
		targetOperator.truncateTableData(properties.getTarget().getTargetSchema(), sourceProperties.getPrefixTable() + tableDescription.getTableName());

		// 查询源端数据并写入目的端
		IDatabaseOperator sourceOperator = DatabaseOperatorFactory.createDatabaseOperator(sourceDataSource);
		sourceOperator.setFetchSize(BATCH_SIZE);

		DatabaseTypeEnum sourceDatabaseType = JdbcTemplateUtils.getDatabaseProduceName(sourceDataSource);
		String fullTableName = CommonUtils.getTableFullNameByDatabase(sourceDatabaseType,
				tableDescription.getSchemaName(), tableDescription.getTableName());
		Map<String, Integer> columnMetaData = JdbcTemplateUtils.getColumnMetaData(new JdbcTemplate(sourceDataSource),
				fullTableName);

		List<String> fields = new ArrayList<>(columnMetaData.keySet());
		StatementResultSet srs = sourceOperator.queryTableData(tableDescription.getSchemaName(),
				tableDescription.getTableName(), fields);

		List<Object[]> cache = new LinkedList<Object[]>();
		long totalCount = 0;
		try {
			ResultSet rs = srs.getResultset();
			while (rs.next()) {
				Object[] record = new Object[fields.size()];
				for (int i = 1; i <= fields.size(); ++i) {
					try {
						record[i - 1] = rs.getObject(i);
					} catch (Exception e) {
						log.warn("!!! Read data use function ResultSet.getObject() error", e);
						record[i - 1] = null;
					}
				}

				cache.add(record);
				++totalCount;

				if (cache.size() >= BATCH_SIZE) {
					long ret = writer.write(fields, cache);
					log.info("[FullCoverSynch] handle table [{}] data count: {}", fullTableName, ret);
					cache.clear();
				}
			}

			if (cache.size() > 0) {
				long ret = writer.write(fields, cache);
				log.info("[FullCoverSynch] handle table [{}] data count: {}", fullTableName, ret);
				cache.clear();
			}

			log.info("[FullCoverSynch] handle table [{}] total data count:{} ", fullTableName, totalCount);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			srs.close();
		}
	}

	/**
	 * 变化量同步
	 * 
	 * @param tableDescription 表的描述信息，这里只能是物理表
	 * @param writer           目的端的写入器
	 */
	private void doIncreaseSynchronize(TableDescription tableDescription,
			DbswichProperties.SourceDataSourceProperties sourceProperties, HikariDataSource sourceDataSource,
			IDatabaseWriter writer, List<String> pks, List<String> fields) {
		int fetchSize = 100;
		if (sourceProperties.getFetchSize() >= fetchSize) {
			fetchSize = sourceProperties.getFetchSize();
		}
		final int BATCH_SIZE = fetchSize;

		DatabaseTypeEnum sourceDatabaseType = JdbcTemplateUtils.getDatabaseProduceName(sourceDataSource);
		String fullTableName = CommonUtils.getTableFullNameByDatabase(sourceDatabaseType,
				tableDescription.getSchemaName(), sourceProperties.getPrefixTable() + tableDescription.getTableName());

		TaskParamBean.TaskParamBeanBuilder taskBuilder = TaskParamBean.builder();
		taskBuilder.oldDataSource(writer.getDataSource());
		taskBuilder.oldSchemaName(properties.getTarget().getTargetSchema());
		taskBuilder.oldTableName(sourceProperties.getPrefixTable() + tableDescription.getTableName());
		taskBuilder.newDataSource(sourceDataSource);
		taskBuilder.newSchemaName(tableDescription.getSchemaName());
		taskBuilder.newTableName(tableDescription.getTableName());
		taskBuilder.fieldColumns(fields);

		TaskParamBean param = taskBuilder.build();

		IDatabaseSynchronize synch = DatabaseSynchronizeFactory.createDatabaseWriter(writer.getDataSource());
		synch.prepare(param.getOldSchemaName(), param.getOldTableName(), fields, pks);

		IDatabaseChangeCaculator changeCaculator = new ChangeCaculatorService();
		changeCaculator.setFetchSize(BATCH_SIZE);
		changeCaculator.setRecordIdentical(false);
		changeCaculator.setCheckJdbcType(false);

		// 执行实际的变化同步过程
		changeCaculator.executeCalculate(param, new IDatabaseRowHandler() {

			private long countInsert = 0;
			private long countUpdate = 0;
			private long countDelete = 0;
			private long count = 0;
			private List<Object[]> cacheInsert = new LinkedList<Object[]>();
			private List<Object[]> cacheUpdate = new LinkedList<Object[]>();
			private List<Object[]> cacheDelete = new LinkedList<Object[]>();

			@Override
			public void handle(List<String> fields, Object[] record, RecordChangeTypeEnum flag) {
				if (flag == RecordChangeTypeEnum.VALUE_INSERT) {
					cacheInsert.add(record);
					countInsert++;
				} else if (flag == RecordChangeTypeEnum.VALUE_CHANGED) {
					cacheUpdate.add(record);
					countUpdate++;
				} else {
					cacheDelete.add(record);
					countDelete++;
				}

				count++;
				checkFull(fields);
			}

			/**
			 * 检测缓存是否已满，如果已满执行同步操作
			 * 
			 * @param fields 同步的字段列表
			 */
			private void checkFull(List<String> fields) {
				if (cacheInsert.size() >= BATCH_SIZE || cacheUpdate.size() >= BATCH_SIZE
						|| cacheDelete.size() >= BATCH_SIZE) {
					if (cacheDelete.size() > 0) {
						doDelete(fields);
					}

					if (cacheInsert.size() > 0) {
						doInsert(fields);
					}

					if (cacheUpdate.size() > 0) {
						doUpdate(fields);
					}
				}
			}

			@Override
			public void destroy(List<String> fields) {
				if (cacheDelete.size() > 0) {
					doDelete(fields);
				}

				if (cacheInsert.size() > 0) {
					doInsert(fields);
				}

				if (cacheUpdate.size() > 0) {
					doUpdate(fields);
				}

				log.info("[IncreaseSynch] Handle table [{}] total count: {}, Insert:{},Update:{},Delete:{} ", fullTableName, count,
						countInsert, countUpdate, countDelete);
			}

			private void doInsert(List<String> fields) {
				long ret = synch.executeInsert(cacheInsert);
				log.info("[IncreaseSynch] Handle table [{}] data Insert count: {}", fullTableName, ret);
				cacheInsert.clear();
			}

			private void doUpdate(List<String> fields) {
				long ret = synch.executeUpdate(cacheUpdate);
				log.info("[IncreaseSynch] Handle table [{}] data Update count: {}", fullTableName, ret);
				cacheUpdate.clear();
			}

			private void doDelete(List<String> fields) {
				long ret = synch.executeDelete(cacheDelete);
				log.info("[IncreaseSynch] Handle table [{}] data Delete count: {}", fullTableName, ret);
				cacheDelete.clear();
			}

		});
	}

	/**
	 * 创建于指定数据库连接描述符的连接池
	 * 
	 * @param dbdesc 数据库连接描述符
	 * @return HikariDataSource连接池
	 */
	private HikariDataSource createSourceDataSource(DbswichProperties.SourceDataSourceProperties description) {
		HikariDataSource ds = new HikariDataSource();
		ds.setPoolName("The_Source_DB_Connection");
		ds.setJdbcUrl(description.getUrl());
		ds.setDriverClassName(description.getDriverClassName());
		ds.setUsername(description.getUsername());
		ds.setPassword(description.getPassword());
		if (description.getDriverClassName().contains("oracle")) {
			ds.setConnectionTestQuery("SELECT 'Hello' from DUAL");
		} else {
			ds.setConnectionTestQuery("SELECT 1");
		}
		ds.setMaximumPoolSize(5);
		ds.setMinimumIdle(2);
		ds.setConnectionTimeout(30000);
		ds.setIdleTimeout(60000);

		return ds;
	}

	/**
	 * 创建于指定数据库连接描述符的连接池
	 * 
	 * @param dbdesc 数据库连接描述符
	 * @return HikariDataSource连接池
	 */
	private HikariDataSource createTargetDataSource(DbswichProperties.TargetDataSourceProperties description) {
		HikariDataSource ds = new HikariDataSource();
		ds.setPoolName("The_Target_DB_Connection");
		ds.setJdbcUrl(description.getUrl());
		ds.setDriverClassName(description.getDriverClassName());
		ds.setUsername(description.getUsername());
		ds.setPassword(description.getPassword());
		if (description.getDriverClassName().contains("oracle")) {
			ds.setConnectionTestQuery("SELECT 'Hello' from DUAL");
		} else {
			ds.setConnectionTestQuery("SELECT 1");
		}
		ds.setMaximumPoolSize(5);
		ds.setMinimumIdle(2);
		ds.setConnectionTimeout(30000);
		ds.setIdleTimeout(60000);

		// 如果是Greenplum数据库，这里需要关闭会话的查询优化器
		if (description.getDriverClassName().contains("postgresql")) {
			org.springframework.jdbc.datasource.DriverManagerDataSource dataSource = new org.springframework.jdbc.datasource.DriverManagerDataSource();
			dataSource.setDriverClassName(description.getDriverClassName());
			dataSource.setUrl(description.getUrl());
			dataSource.setUsername(description.getUsername());
			dataSource.setPassword(description.getPassword());
			JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
			String versionString = jdbcTemplate.queryForObject("SELECT version()", String.class);
			if (Objects.nonNull(versionString) && versionString.contains("Greenplum")) {
				log.info("#### Target database is Greenplum Cluster, Close Optimizer now: set optimizer to 'off' ");
				ds.setConnectionInitSql("set optimizer to 'off'");
			}
		}

		return ds;
	}

	/**
	 * 检查MySQL数据库表的存储引擎是否为Innodb
	 * 
	 * @param dataSource 数据源
	 * @param task       任务实体
	 * @return 为Innodb存储引擎时返回True,否在为false
	 */
	private boolean isMysqlInodbStorageEngine(String shemaName, String tableName, DataSource dataSource) {
		String sql = "SELECT count(*) as total FROM information_schema.tables WHERE table_schema=? AND table_name=? AND ENGINE='InnoDB'";
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		return jdbcTemplate.queryForObject(sql, new Object[] { shemaName, tableName }, Integer.class) > 0;
	}

	/**
	 * 根据逗号切分字符串为数组
	 * 
	 * @param s 待切分的字符串
	 * @return List
	 */
	private List<String> stringToList(String s) {
		if (!StringUtils.isEmpty(s)) {
			String[] strs = s.split(",");
			if (strs.length > 0) {
				return new ArrayList<>(Arrays.asList(strs));
			}
		}

		return new ArrayList<>();
	}
}
