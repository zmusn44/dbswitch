// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Data : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.weishao.dbswitch.data.service;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weishao.dbswitch.common.constant.DatabaseTypeEnum;
import com.weishao.dbswitch.common.util.CommonUtils;
import com.weishao.dbswitch.core.model.ColumnDescription;
import com.weishao.dbswitch.core.model.TableDescription;
import com.weishao.dbswitch.core.service.IMetaDataService;
import com.weishao.dbswitch.data.config.PropertiesConfig;
import com.weishao.dbswitch.data.util.JdbcTemplateUtils;
import com.weishao.dbswitch.dbchange.ChangeCaculatorService;
import com.weishao.dbswitch.dbchange.IDatabaseChangeCaculator;
import com.weishao.dbswitch.dbchange.IDatabaseRowHandler;
import com.weishao.dbswitch.dbchange.RecordChangeTypeEnum;
import com.weishao.dbswitch.dbchange.pojo.TaskParamBean;
import com.weishao.dbswitch.dbcommon.database.DatabaseOperatorFactory;
import com.weishao.dbswitch.dbcommon.database.IDatabaseOperator;
import com.weishao.dbswitch.dbcommon.pojo.StatementResultSet;
import com.weishao.dbswitch.dbcommon.util.JdbcMetaDataUtils;
import com.weishao.dbswitch.dbsynch.DatabaseSynchronizeFactory;
import com.weishao.dbswitch.dbsynch.IDatabaseSynchronize;
import com.weishao.dbswitch.dbwriter.DatabaseWriterFactory;
import com.weishao.dbswitch.dbwriter.IDatabaseWriter;

import lombok.extern.slf4j.Slf4j;

/**
 * 数据迁移服务类
 * 
 * @author tang
 *
 */
@Slf4j
@Service("MainService")
public class MainService {

	private ObjectMapper jackson = new ObjectMapper();

	@Autowired
	@Qualifier("sourceDataSource")
	private BasicDataSource sourceDataSource;

	@Autowired
	@Qualifier("targetDataSource")
	private BasicDataSource targetDataSource;

	@Autowired
	private PropertiesConfig properties;

	@Autowired
	private IMetaDataService metaDataService;

	/**
	 * 执行主逻辑
	 */
	public void run() {
		DatabaseTypeEnum sourceDatabaseType = JdbcTemplateUtils.getDatabaseProduceName(this.sourceDataSource);
		metaDataService.setDatabaseConnection(sourceDatabaseType);

		IDatabaseWriter writer = DatabaseWriterFactory.createDatabaseWriter(this.targetDataSource,
				properties.engineInsert);

		StopWatch watch = new StopWatch();
		watch.start();

		try {
			log.info("service is running....");

			// 判断处理的策略：是排除还是包含
			List<String> includes = properties.getSourceTableNameIncludes();
			log.info("Includes tables is :{}", jackson.writeValueAsString(includes));
			List<String> filters = properties.getSourceTableNameExcludes();
			log.info("Filter tables is :{}", jackson.writeValueAsString(filters));

			boolean useExcludeTables = includes.isEmpty();
			if (useExcludeTables) {
				log.info("!!!! Use source.datasource-source.excludes to filter tables");
			} else {
				log.info("!!!! Use source.datasource-source.includes to filter tables");
			}

			// 读取源库指定shema里所有的表
			List<TableDescription> tableList = metaDataService.queryTableList(properties.dbSourceJdbcUrl,
					properties.dbSourceUserName, properties.dbSourcePassword, properties.schemaNameSource);
			if (tableList.isEmpty()) {
				log.warn("### Find table list empty for shema={}", properties.schemaNameSource);
			} else {
				int finished = 0;
				for (TableDescription td : tableList) {
					String tableName = td.getTableName();
					if (useExcludeTables) {
						if (!filters.contains(tableName)) {
							this.doDataMigration(td, writer);
						}
					} else {
						if (includes.contains(tableName)) {
							this.doDataMigration(td, writer);
						}
					}

					log.info("#### Complete data migration count is {},total is {}, process is {}%", ++finished,
							tableList.size(), finished * 100.0 / tableList.size());
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
	private void doDataMigration(TableDescription tableDescription, IDatabaseWriter writer) {
		JdbcTemplate targetJdbcTemplate = new JdbcTemplate(this.targetDataSource);
		DatabaseTypeEnum targetDatabaseType = JdbcTemplateUtils.getDatabaseProduceName(this.targetDataSource);

		if (properties.dropTargetTable.booleanValue()) {
			/**
			 * 如果配置了target.datasource-target.drop=true时，先执行drop table语句，然后执行create table语句
			 */

			// 先drop表
			try {
				IDatabaseOperator targetOperator = DatabaseOperatorFactory
						.createDatabaseOperator(this.targetDataSource);
				targetOperator.dropTable(properties.dbTargetSchema, tableDescription.getTableName());
			} catch (Exception e) {
				log.info("Table {}.{} is not exits!", properties.dbTargetSchema, tableDescription.getTableName());
			}

			// 然后create表
			List<ColumnDescription> columnDescs = metaDataService.queryTableColumnMeta(properties.dbSourceJdbcUrl,
					properties.dbSourceUserName, properties.dbSourcePassword, tableDescription.getSchemaName(),
					tableDescription.getTableName());
			List<String> primaryKeys = metaDataService.queryTablePrimaryKeys(properties.dbSourceJdbcUrl,
					properties.dbSourceUserName, properties.dbSourcePassword, tableDescription.getSchemaName(),
					tableDescription.getTableName());
			String sqlCreateTable = metaDataService.getDDLCreateTableSQL(targetDatabaseType, columnDescs, primaryKeys,
					properties.dbTargetSchema, tableDescription.getTableName(), true);
			targetJdbcTemplate.execute(sqlCreateTable);
			log.info("Execute SQL: \n{}", sqlCreateTable);

			this.doFullCoverSynchronize(tableDescription, writer);
		} else {
			// 判断是否具备变化量同步的条件：（1）两端表结构一致，且都有一样的主键字段；(2)MySQL使用Innodb引擎；
			if (properties.changeSynch.booleanValue()) {
				// 根据主键情况判断推送的方式
				JdbcMetaDataUtils mds = new JdbcMetaDataUtils(this.sourceDataSource);
				JdbcMetaDataUtils mdt = new JdbcMetaDataUtils(this.targetDataSource);
				List<String> pks1 = mds.queryTablePrimaryKeys(tableDescription.getSchemaName(),
						tableDescription.getTableName());
				List<String> pks2 = mdt.queryTablePrimaryKeys(properties.dbTargetSchema,
						tableDescription.getTableName());

				if (!pks1.isEmpty() && !pks2.isEmpty() && pks1.containsAll(pks2) && pks2.containsAll(pks1)) {
					if (targetDatabaseType == DatabaseTypeEnum.MYSQL
							&& !isMysqlInodbStorageEngine(properties.dbTargetSchema, tableDescription.getTableName())) {
						this.doFullCoverSynchronize(tableDescription, writer);
					} else {
						List<String> fields = mds.queryTableColumnName(tableDescription.getSchemaName(),
								tableDescription.getTableName());
						this.doIncreaseSynchronize(tableDescription, writer, fields, pks1);
					}
				} else {
					this.doFullCoverSynchronize(tableDescription, writer);
				}
			} else {
				this.doFullCoverSynchronize(tableDescription, writer);
			}
		}
	}

	/**
	 * 执行覆盖同步
	 * 
	 * @param tableDescription 表的描述信息，可能是视图表，可能是物理表
	 * @param writer           目的端的写入器
	 */
	private void doFullCoverSynchronize(TableDescription tableDescription, IDatabaseWriter writer) {
		int fetchSize = 100;
		if (properties.fetchSizeSource >= fetchSize) {
			fetchSize = properties.fetchSizeSource;
		}
		final int BATCH_SIZE = fetchSize;

		// 准备目的端的数据写入操作
		writer.prepareWrite(properties.dbTargetSchema, tableDescription.getTableName());

		// 清空目的端表的数据
		IDatabaseOperator targetOperator = DatabaseOperatorFactory.createDatabaseOperator(this.targetDataSource);
		targetOperator.truncateTableData(properties.dbTargetSchema, tableDescription.getTableName());

		// 查询源端数据并写入目的端
		IDatabaseOperator sourceOperator = DatabaseOperatorFactory.createDatabaseOperator(this.sourceDataSource);
		sourceOperator.setFetchSize(BATCH_SIZE);

		DatabaseTypeEnum sourceDatabaseType = JdbcTemplateUtils.getDatabaseProduceName(this.sourceDataSource);
		String fullTableName = CommonUtils.getTableFullNameByDatabase(sourceDatabaseType,
				tableDescription.getSchemaName(), tableDescription.getTableName());
		Map<String, Integer> columnMetaData = JdbcTemplateUtils
				.getColumnMetaData(new JdbcTemplate(this.sourceDataSource), fullTableName);

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
					log.info("handle table [{}] data count: {}", fullTableName, ret);
					cache.clear();
				}
			}

			if (cache.size() > 0) {
				long ret = writer.write(fields, cache);
				log.info("handle table [{}] data count: {}", fullTableName, ret);
				cache.clear();
			}

			log.info("handle table [{}]  total data count:{} ", fullTableName, totalCount);
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
	private void doIncreaseSynchronize(TableDescription tableDescription, IDatabaseWriter writer, List<String> pks,
			List<String> fields) {
		int fetchSize = 100;
		if (properties.fetchSizeSource >= fetchSize) {
			fetchSize = properties.fetchSizeSource;
		}
		final int BATCH_SIZE = fetchSize;

		DatabaseTypeEnum sourceDatabaseType = JdbcTemplateUtils.getDatabaseProduceName(this.sourceDataSource);
		String fullTableName = CommonUtils.getTableFullNameByDatabase(sourceDatabaseType,
				tableDescription.getSchemaName(), tableDescription.getTableName());

		TaskParamBean.TaskParamBeanBuilder taskBuilder = TaskParamBean.builder();
		taskBuilder.oldDataSource(this.targetDataSource);
		taskBuilder.oldSchemaName(properties.dbTargetSchema);
		taskBuilder.oldTableName(tableDescription.getTableName());
		taskBuilder.newDataSource(this.sourceDataSource);
		taskBuilder.newSchemaName(tableDescription.getSchemaName());
		taskBuilder.newTableName(tableDescription.getTableName());
		taskBuilder.fieldColumns(fields);

		TaskParamBean param = taskBuilder.build();

		IDatabaseSynchronize synch = DatabaseSynchronizeFactory.createDatabaseWriter(this.targetDataSource);
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

				log.info("Handle table [{}] total count: {}, Insert:{},Update:{},Delete:{} ", fullTableName, count,
						countInsert, countUpdate, countDelete);
			}

			private void doInsert(List<String> fields) {
				long ret = synch.executeInsert(cacheInsert);
				log.info("handle table [{}] data Insert count: {}", fullTableName, ret);
				cacheInsert.clear();
			}

			private void doUpdate(List<String> fields) {
				long ret = synch.executeUpdate(cacheUpdate);
				log.info("handle table [{}] data Update count: {}", fullTableName, ret);
				cacheUpdate.clear();
			}

			private void doDelete(List<String> fields) {
				long ret = synch.executeDelete(cacheDelete);
				log.info("handle table [{}] data Delete count: {}", fullTableName, ret);
				cacheDelete.clear();
			}

		});
	}

	/**
	 * 检查MySQL数据库表的存储引擎是否为Innodb
	 * 
	 * @param dataSource 数据源
	 * @param task       任务实体
	 * @return 为Innodb存储引擎时返回True,否在为false
	 */
	private boolean isMysqlInodbStorageEngine(String shemaName, String tableName) {
		String sql = "SELECT count(*) as total FROM information_schema.tables WHERE table_schema=? AND table_name=? AND ENGINE='InnoDB'";
		JdbcTemplate jdbcTemplate = new JdbcTemplate(this.targetDataSource);
		return jdbcTemplate.queryForObject(sql, new Object[] { shemaName, tableName }, Integer.class) > 0;
	}
}
