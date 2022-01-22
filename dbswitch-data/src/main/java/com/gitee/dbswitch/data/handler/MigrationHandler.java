// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.data.handler;

import com.gitee.dbswitch.common.type.DatabaseTypeEnum;
import com.gitee.dbswitch.common.util.CommonUtils;
import com.gitee.dbswitch.core.model.ColumnDescription;
import com.gitee.dbswitch.core.model.TableDescription;
import com.gitee.dbswitch.core.service.IMetaDataService;
import com.gitee.dbswitch.core.service.impl.MigrationMetaDataServiceImpl;
import com.gitee.dbswitch.data.config.DbswichProperties;
import com.gitee.dbswitch.data.util.BytesUnitUtils;
import com.gitee.dbswitch.data.util.JdbcTemplateUtils;
import com.gitee.dbswitch.dbchange.ChangeCalculatorService;
import com.gitee.dbswitch.dbchange.IDatabaseChangeCaculator;
import com.gitee.dbswitch.dbchange.IDatabaseRowHandler;
import com.gitee.dbswitch.dbchange.RecordChangeTypeEnum;
import com.gitee.dbswitch.dbchange.TaskParamEntity;
import com.gitee.dbswitch.dbcommon.database.DatabaseOperatorFactory;
import com.gitee.dbswitch.dbcommon.database.IDatabaseOperator;
import com.gitee.dbswitch.dbcommon.domain.StatementResultSet;
import com.gitee.dbswitch.dbcommon.util.JdbcMetaDataUtils;
import com.gitee.dbswitch.dbsynch.DatabaseSynchronizeFactory;
import com.gitee.dbswitch.dbsynch.IDatabaseSynchronize;
import com.gitee.dbswitch.dbwriter.DatabaseWriterFactory;
import com.gitee.dbswitch.dbwriter.IDatabaseWriter;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.sizeof.SizeOf;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 在一个线程内的单表迁移处理逻辑
 *
 * @author tang
 */
@Slf4j
public class MigrationHandler implements Supplier<Long> {

  private final long MAX_CACHE_BYTES_SIZE = 64 * 1024 * 1024;

  private int fetchSize = 100;
  private final TableDescription tableDescription;
  private final DbswichProperties properties;
  private final DbswichProperties.SourceDataSourceProperties sourceProperties;
  private final HikariDataSource sourceDataSource;
  private final IMetaDataService sourceMetaDataService;
  private final HikariDataSource targetDataSource;

  public static MigrationHandler createInstance(TableDescription td,
      DbswichProperties properties,
      Integer sourcePropertiesIndex,
      HikariDataSource sds,
      HikariDataSource tds) {
    return new MigrationHandler(td, properties, sourcePropertiesIndex, sds, tds);
  }

  private MigrationHandler(TableDescription td,
      DbswichProperties properties,
      Integer sourcePropertiesIndex,
      HikariDataSource sds,
      HikariDataSource tds) {
    this.tableDescription = td;
    this.properties = properties;
    this.sourceProperties = properties.getSource().get(sourcePropertiesIndex);
    this.sourceDataSource = sds;
    this.sourceMetaDataService = new MigrationMetaDataServiceImpl();
    this.targetDataSource = tds;

    if (sourceProperties.getFetchSize() >= fetchSize) {
      fetchSize = sourceProperties.getFetchSize();
    }

    this.sourceMetaDataService
        .setDatabaseConnection(JdbcTemplateUtils.getDatabaseProduceName(sourceDataSource));
  }

  @Override
  public Long get() {
    log.info("Begin Migrate table for {}.{} ", tableDescription.getSchemaName(),
        tableDescription.getTableName());

    JdbcTemplate targetJdbcTemplate = new JdbcTemplate(targetDataSource);
    DatabaseTypeEnum targetDatabaseType = JdbcTemplateUtils
        .getDatabaseProduceName(targetDataSource);
    IDatabaseWriter writer = DatabaseWriterFactory.createDatabaseWriter(targetDataSource,
        properties.getTarget().getWriterEngineInsert());

    if (properties.getTarget().getTargetDrop()) {
      /*
        如果配置了dbswitch.target.datasource-target-drop=true时，先执行drop table语句，然后执行create
        table语句
       */

      // 先drop表
      try {
        IDatabaseOperator targetOperator = DatabaseOperatorFactory
            .createDatabaseOperator(targetDataSource);
        targetOperator.dropTable(properties.getTarget().getTargetSchema(),
            sourceProperties.getPrefixTable() + tableDescription.getTableName());
      } catch (Exception e) {
        log.info("Target Table {}.{} is not exits!", properties.getTarget().getTargetSchema(),
            sourceProperties.getPrefixTable() + tableDescription.getTableName());
      }

      // 然后create表
      List<ColumnDescription> columnDescriptions = sourceMetaDataService
          .queryTableColumnMeta(sourceProperties.getUrl(),
              sourceProperties.getUsername(), sourceProperties.getPassword(),
              tableDescription.getSchemaName(),
              tableDescription.getTableName());
      List<String> primaryKeys = sourceMetaDataService
          .queryTablePrimaryKeys(sourceProperties.getUrl(),
              sourceProperties.getUsername(), sourceProperties.getPassword(),
              tableDescription.getSchemaName(),
              tableDescription.getTableName());
      String sqlCreateTable = sourceMetaDataService
          .getDDLCreateTableSQL(targetDatabaseType, columnDescriptions, primaryKeys,
              properties.getTarget().getTargetSchema(),
              sourceProperties.getPrefixTable() + tableDescription.getTableName(),
              properties.getTarget().getCreateTableAutoIncrement());
      targetJdbcTemplate.execute(sqlCreateTable);
      log.info("Execute SQL: \n{}", sqlCreateTable);

      return doFullCoverSynchronize(tableDescription, sourceProperties, sourceDataSource, writer);
    } else {
      // 判断是否具备变化量同步的条件：（1）两端表结构一致，且都有一样的主键字段；(2)MySQL使用Innodb引擎；
      if (properties.getTarget().getChangeDataSync()) {
        // 根据主键情况判断同步的方式：增量同步或覆盖同步
        JdbcMetaDataUtils mds = new JdbcMetaDataUtils(sourceDataSource);
        JdbcMetaDataUtils mdt = new JdbcMetaDataUtils(targetDataSource);
        List<String> pks1 = mds.queryTablePrimaryKeys(tableDescription.getSchemaName(),
            tableDescription.getTableName());
        List<String> pks2 = mdt.queryTablePrimaryKeys(properties.getTarget().getTargetSchema(),
            sourceProperties.getPrefixTable() + tableDescription.getTableName());

        if (!pks1.isEmpty() && !pks2.isEmpty() && pks1.containsAll(pks2) && pks2
            .containsAll(pks1)) {
          if (targetDatabaseType == DatabaseTypeEnum.MYSQL
              && !JdbcTemplateUtils
              .isMysqlInnodbStorageEngine(properties.getTarget().getTargetSchema(),
                  sourceProperties.getPrefixTable() + tableDescription.getTableName(),
                  targetDataSource)) {
            return doFullCoverSynchronize(tableDescription, sourceProperties, sourceDataSource,
                writer);
          } else {
            List<String> fields = mds.queryTableColumnName(tableDescription.getSchemaName(),
                tableDescription.getTableName());
            return doIncreaseSynchronize(tableDescription, sourceProperties, sourceDataSource,
                writer, pks1, fields);
          }
        } else {
          return doFullCoverSynchronize(tableDescription, sourceProperties, sourceDataSource,
              writer);
        }
      } else {
        return doFullCoverSynchronize(tableDescription, sourceProperties, sourceDataSource, writer);
      }
    }
  }

  /**
   * 执行覆盖同步
   *
   * @param tableDescription 表的描述信息，可能是视图表，可能是物理表
   * @param writer           目的端的写入器
   */
  private Long doFullCoverSynchronize(TableDescription tableDescription,
      DbswichProperties.SourceDataSourceProperties sourceProperties,
      HikariDataSource sourceDataSource,
      IDatabaseWriter writer) {
    final int BATCH_SIZE = fetchSize;

    // 准备目的端的数据写入操作
    writer.prepareWrite(properties.getTarget().getTargetSchema(),
        sourceProperties.getPrefixTable() + tableDescription.getTableName());

    // 清空目的端表的数据
    IDatabaseOperator targetOperator = DatabaseOperatorFactory
        .createDatabaseOperator(writer.getDataSource());
    targetOperator.truncateTableData(properties.getTarget().getTargetSchema(),
        sourceProperties.getPrefixTable() + tableDescription.getTableName());

    // 查询源端数据并写入目的端
    IDatabaseOperator sourceOperator = DatabaseOperatorFactory
        .createDatabaseOperator(sourceDataSource);
    sourceOperator.setFetchSize(BATCH_SIZE);

    DatabaseTypeEnum sourceDatabaseType = JdbcTemplateUtils
        .getDatabaseProduceName(sourceDataSource);
    String fullTableName = CommonUtils.getTableFullNameByDatabase(sourceDatabaseType,
        tableDescription.getSchemaName(), tableDescription.getTableName());
    Map<String, Integer> columnMetaData = JdbcTemplateUtils
        .getColumnMetaData(new JdbcTemplate(sourceDataSource),
            fullTableName);

    List<String> fields = new ArrayList<>(columnMetaData.keySet());
    StatementResultSet srs = sourceOperator
        .queryTableData(tableDescription.getSchemaName(), tableDescription.getTableName(), fields);

    List<Object[]> cache = new LinkedList<>();
    long cacheBytes = 0;
    long totalCount = 0;
    long totalBytes = 0;
    try (ResultSet rs = srs.getResultset()) {
      while (rs.next()) {
        Object[] record = new Object[fields.size()];
        for (int i = 1; i <= fields.size(); ++i) {
          try {
            record[i - 1] = rs.getObject(i);
          } catch (Exception e) {
            log.warn("!!! Read data from table [ {} ] use function ResultSet.getObject() error",
                fullTableName, e);
            record[i - 1] = null;
          }
        }

        cache.add(record);
        cacheBytes += SizeOf.newInstance().deepSizeOf(record);
        ++totalCount;

        if (cache.size() >= BATCH_SIZE || cacheBytes >= MAX_CACHE_BYTES_SIZE) {
          long ret = writer.write(fields, cache);
          log.info("[FullCoverSync] handle table [{}] data count: {}, the batch bytes sie: {}",
              fullTableName, ret, BytesUnitUtils.bytesSizeToHuman(cacheBytes));
          cache.clear();
          totalBytes += cacheBytes;
          cacheBytes = 0;
        }
      }

      if (cache.size() > 0) {
        long ret = writer.write(fields, cache);
        log.info("[FullCoverSync] handle table [{}] data count: {}, last batch bytes sie: {}",
            fullTableName, ret, BytesUnitUtils.bytesSizeToHuman(cacheBytes));
        cache.clear();
        totalBytes += cacheBytes;
      }

      log.info("[FullCoverSync] handle table [{}] total data count:{}, total bytes={}",
          fullTableName, totalCount, BytesUnitUtils.bytesSizeToHuman(totalBytes));
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      srs.close();
    }

    return totalBytes;
  }

  /**
   * 变化量同步
   *
   * @param tableDescription 表的描述信息，这里只能是物理表
   * @param writer           目的端的写入器
   */
  private Long doIncreaseSynchronize(TableDescription tableDescription,
      DbswichProperties.SourceDataSourceProperties sourceProperties,
      HikariDataSource sourceDataSource,
      IDatabaseWriter writer, List<String> pks, List<String> fields) {
    final int BATCH_SIZE = fetchSize;

    DatabaseTypeEnum sourceDatabaseType = JdbcTemplateUtils
        .getDatabaseProduceName(sourceDataSource);
    String fullTableName = CommonUtils.getTableFullNameByDatabase(sourceDatabaseType,
        tableDescription.getSchemaName(),
        sourceProperties.getPrefixTable() + tableDescription.getTableName());

    TaskParamEntity.TaskParamEntityBuilder taskBuilder = TaskParamEntity.builder();
    taskBuilder.oldDataSource(writer.getDataSource());
    taskBuilder.oldSchemaName(properties.getTarget().getTargetSchema());
    taskBuilder.oldTableName(sourceProperties.getPrefixTable() + tableDescription.getTableName());
    taskBuilder.newDataSource(sourceDataSource);
    taskBuilder.newSchemaName(tableDescription.getSchemaName());
    taskBuilder.newTableName(tableDescription.getTableName());
    taskBuilder.fieldColumns(fields);

    TaskParamEntity param = taskBuilder.build();

    IDatabaseSynchronize synchronizer = DatabaseSynchronizeFactory
        .createDatabaseWriter(writer.getDataSource());
    synchronizer.prepare(param.getOldSchemaName(), param.getOldTableName(), fields, pks);

    IDatabaseChangeCaculator calculator = new ChangeCalculatorService();
    calculator.setFetchSize(BATCH_SIZE);
    calculator.setRecordIdentical(false);
    calculator.setCheckJdbcType(false);

    AtomicLong totalBytes = new AtomicLong(0);

    // 执行实际的变化同步过程
    calculator.executeCalculate(param, new IDatabaseRowHandler() {

      private long countInsert = 0;
      private long countUpdate = 0;
      private long countDelete = 0;
      private long countTotal = 0;
      private long cacheBytes = 0;
      private final List<Object[]> cacheInsert = new LinkedList<>();
      private final List<Object[]> cacheUpdate = new LinkedList<>();
      private final List<Object[]> cacheDelete = new LinkedList<>();

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

        cacheBytes += SizeOf.newInstance().deepSizeOf(record);
        totalBytes.addAndGet(cacheBytes);
        countTotal++;
        checkFull(fields);
      }

      /**
       * 检测缓存是否已满，如果已满执行同步操作
       *
       * @param fields 同步的字段列表
       */
      private void checkFull(List<String> fields) {
        if (cacheInsert.size() >= BATCH_SIZE || cacheUpdate.size() >= BATCH_SIZE
            || cacheDelete.size() >= BATCH_SIZE || cacheBytes >= MAX_CACHE_BYTES_SIZE) {
          if (cacheDelete.size() > 0) {
            doDelete(fields);
          }

          if (cacheInsert.size() > 0) {
            doInsert(fields);
          }

          if (cacheUpdate.size() > 0) {
            doUpdate(fields);
          }

          log.info("[IncreaseSync] Handle table [{}] data one batch size: {}", fullTableName,
              BytesUnitUtils.bytesSizeToHuman(cacheBytes));
          cacheBytes = 0;
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

        log.info("[IncreaseSync] Handle table [{}] total count: {}, Insert:{},Update:{},Delete:{} ",
            fullTableName, countTotal, countInsert, countUpdate, countDelete);
      }

      private void doInsert(List<String> fields) {
        long ret = synchronizer.executeInsert(cacheInsert);
        log.info("[IncreaseSync] Handle table [{}] data Insert count: {}", fullTableName, ret);
        cacheInsert.clear();
      }

      private void doUpdate(List<String> fields) {
        long ret = synchronizer.executeUpdate(cacheUpdate);
        log.info("[IncreaseSync] Handle table [{}] data Update count: {}", fullTableName, ret);
        cacheUpdate.clear();
      }

      private void doDelete(List<String> fields) {
        long ret = synchronizer.executeDelete(cacheDelete);
        log.info("[IncreaseSync] Handle table [{}] data Delete count: {}", fullTableName, ret);
        cacheDelete.clear();
      }

    });

    return totalBytes.get();
  }

}
