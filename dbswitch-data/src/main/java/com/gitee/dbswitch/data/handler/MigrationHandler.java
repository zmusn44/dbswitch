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
import com.gitee.dbswitch.common.util.DatabaseAwareUtils;
import com.gitee.dbswitch.common.util.PatterNameUtils;
import com.gitee.dbswitch.core.model.ColumnDescription;
import com.gitee.dbswitch.core.model.TableDescription;
import com.gitee.dbswitch.core.service.IMetaDataByDatasourceService;
import com.gitee.dbswitch.core.service.impl.MetaDataByDataSourceServiceImpl;
import com.gitee.dbswitch.data.config.DbswichProperties;
import com.gitee.dbswitch.data.entity.SourceDataSourceProperties;
import com.gitee.dbswitch.data.util.BytesUnitUtils;
import com.gitee.dbswitch.dbchange.ChangeCalculatorService;
import com.gitee.dbswitch.dbchange.IDatabaseChangeCaculator;
import com.gitee.dbswitch.dbchange.IDatabaseRowHandler;
import com.gitee.dbswitch.dbchange.RecordChangeTypeEnum;
import com.gitee.dbswitch.dbchange.TaskParamEntity;
import com.gitee.dbswitch.dbcommon.database.DatabaseOperatorFactory;
import com.gitee.dbswitch.dbcommon.database.IDatabaseOperator;
import com.gitee.dbswitch.dbcommon.domain.StatementResultSet;
import com.gitee.dbswitch.dbsynch.DatabaseSynchronizeFactory;
import com.gitee.dbswitch.dbsynch.IDatabaseSynchronize;
import com.gitee.dbswitch.dbwriter.DatabaseWriterFactory;
import com.gitee.dbswitch.dbwriter.IDatabaseWriter;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.sizeof.SizeOf;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

/**
 * 在一个线程内的单表迁移处理逻辑
 *
 * @author tang
 */
@Slf4j
public class MigrationHandler implements Supplier<Long> {

  private final long MAX_CACHE_BYTES_SIZE = 64 * 1024 * 1024;

  private int fetchSize = 100;
  private final DbswichProperties properties;
  private final SourceDataSourceProperties sourceProperties;

  // 来源端
  private final HikariDataSource sourceDataSource;
  private DatabaseTypeEnum sourceProductType;
  private String sourceSchemaName;
  private String sourceTableName;
  private List<ColumnDescription> sourceColumnDescriptions;
  private List<String> sourcePrimaryKeys;

  private IMetaDataByDatasourceService sourceMetaDataService;

  // 目的端
  private final HikariDataSource targetDataSource;
  private DatabaseTypeEnum targetProductType;
  private String targetSchemaName;
  private String targetTableName;
  private List<ColumnDescription> targetColumnDescriptions;
  private List<String> targetPrimaryKeys;

  // 日志输出字符串使用
  private String tableNameMapString;

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
    this.sourceSchemaName = td.getSchemaName();
    this.sourceTableName = td.getTableName();
    this.properties = properties;
    this.sourceProperties = properties.getSource().get(sourcePropertiesIndex);
    this.sourceDataSource = sds;
    this.targetDataSource = tds;

    if (sourceProperties.getFetchSize() >= fetchSize) {
      fetchSize = sourceProperties.getFetchSize();
    }

    // 获取映射转换后新的表名
    this.targetSchemaName = properties.getTarget().getTargetSchema();
    this.targetTableName = PatterNameUtils.getFinalName(td.getTableName(),
        sourceProperties.getRegexTableMapper());

    this.tableNameMapString = String.format("%s.%s --> %s.%s",
        td.getSchemaName(), td.getTableName(),
        targetSchemaName, targetTableName);
  }

  @Override
  public Long get() {
    log.info("Begin Migrate table for {}", tableNameMapString);

    this.sourceProductType = DatabaseAwareUtils.getDatabaseTypeByDataSource(sourceDataSource);
    this.targetProductType = DatabaseAwareUtils.getDatabaseTypeByDataSource(targetDataSource);
    this.sourceMetaDataService = new MetaDataByDataSourceServiceImpl(sourceDataSource,
        sourceProductType);

    // 读取源表的字段元数据
    this.sourceColumnDescriptions = sourceMetaDataService
        .queryTableColumnMeta(sourceSchemaName, sourceTableName);
    this.sourcePrimaryKeys = sourceMetaDataService
        .queryTablePrimaryKeys(sourceSchemaName, sourceTableName);

    // 根据表的列名映射转换准备目标端表的字段信息
    this.targetColumnDescriptions = sourceColumnDescriptions.stream()
        .map(column -> {
          String newName = PatterNameUtils.getFinalName(
              column.getFieldName(),
              sourceProperties.getRegexColumnMapper());
          ColumnDescription description = column.copy();
          description.setFieldName(newName);
          description.setLabelName(newName);
          return description;
        }).collect(Collectors.toList());
    this.targetPrimaryKeys = sourcePrimaryKeys.stream()
        .map(name ->
            PatterNameUtils.getFinalName(name, sourceProperties.getRegexColumnMapper())
        ).collect(Collectors.toList());

    // 打印表名与字段名的映射关系
    List<String> columnMapperPairs = new ArrayList<>();
    Map<String, String> mapChecker = new HashMap<>();
    for (int i = 0; i < sourceColumnDescriptions.size(); ++i) {
      String sourceColumnName = sourceColumnDescriptions.get(i).getFieldName();
      String targetColumnName = targetColumnDescriptions.get(i).getFieldName();
      if (StringUtils.hasLength(targetColumnName)) {
        columnMapperPairs.add(String.format("%s --> %s", sourceColumnName, targetColumnName));
      } else {
        columnMapperPairs.add(String.format("%s --> %s", sourceColumnName, "<!Field is Deleted>"));
      }
      mapChecker.put(sourceColumnName, targetColumnName);
    }
    log.info("Mapping relation : \ntable mapper :\n\t{}  \ncolumn mapper :\n\t{} ",
        tableNameMapString, columnMapperPairs.stream().collect(Collectors.joining("\n\t")));
    Set<String> valueSet = new HashSet<>(mapChecker.values());
    if (mapChecker.keySet().size() != valueSet.size()) {
      throw new RuntimeException("字段映射配置有误，多个字段映射到一个同名字段!");
    }

    IDatabaseWriter writer = DatabaseWriterFactory.createDatabaseWriter(
        targetDataSource, properties.getTarget().getWriterEngineInsert());

    if (properties.getTarget().getTargetDrop()) {
      /*
        如果配置了dbswitch.target.datasource-target-drop=true时，
        <p>
        先执行drop table语句，然后执行create table语句
       */

      try {
        DatabaseOperatorFactory.createDatabaseOperator(targetDataSource)
            .dropTable(targetSchemaName, targetTableName);
        log.info("Target Table {}.{} is exits, drop it now !", targetSchemaName, targetTableName);
      } catch (Exception e) {
        log.info("Target Table {}.{} is not exits, create it!", targetSchemaName, targetTableName);
      }

      // 生成建表语句并创建
      String sqlCreateTable = sourceMetaDataService.getDDLCreateTableSQL(
          targetProductType,
          targetColumnDescriptions.stream()
              .filter(column -> StringUtils.hasLength(column.getFieldName()))
              .collect(Collectors.toList()),
          targetPrimaryKeys,
          targetSchemaName,
          targetTableName,
          properties.getTarget().getCreateTableAutoIncrement()
      );

      JdbcTemplate targetJdbcTemplate = new JdbcTemplate(targetDataSource);
      targetJdbcTemplate.execute(sqlCreateTable);
      log.info("Execute SQL: \n{}", sqlCreateTable);

      return doFullCoverSynchronize(writer);
    } else {
      // 判断是否具备变化量同步的条件：（1）两端表结构一致，且都有一样的主键字段；(2)MySQL使用Innodb引擎；
      if (properties.getTarget().getChangeDataSync()) {
        // 根据主键情况判断同步的方式：增量同步或覆盖同步
        IMetaDataByDatasourceService metaDataByDatasourceService =
            new MetaDataByDataSourceServiceImpl(targetDataSource, targetProductType);
        List<String> dbTargetPks = metaDataByDatasourceService.queryTablePrimaryKeys(
            targetSchemaName, targetTableName);

        if (!targetPrimaryKeys.isEmpty() && !dbTargetPks.isEmpty()
            && targetPrimaryKeys.containsAll(dbTargetPks)
            && dbTargetPks.containsAll(targetPrimaryKeys)) {
          if (targetProductType == DatabaseTypeEnum.MYSQL
              && !DatabaseAwareUtils.isMysqlInnodbStorageEngine(
              targetSchemaName, targetTableName, targetDataSource)) {
            return doFullCoverSynchronize(writer);
          } else {
            return doIncreaseSynchronize(writer);
          }
        } else {
          return doFullCoverSynchronize(writer);
        }
      } else {
        return doFullCoverSynchronize(writer);
      }
    }
  }

  /**
   * 执行覆盖同步
   *
   * @param writer 目的端的写入器
   */
  private Long doFullCoverSynchronize(IDatabaseWriter writer) {
    final int BATCH_SIZE = fetchSize;

    List<String> sourceFields = sourceColumnDescriptions.stream()
        .map(ColumnDescription::getFieldName)
        .collect(Collectors.toList());
    List<String> targetFields = targetColumnDescriptions.stream()
        .map(ColumnDescription::getFieldName)
        .collect(Collectors.toList());
    List<Integer> deletedFieldIndexes = new ArrayList<>();
    for (int i = 0; i < targetFields.size(); ++i) {
      if (StringUtils.isEmpty(targetFields.get(i))) {
        deletedFieldIndexes.add(i);
      }
    }
    Collections.reverse(deletedFieldIndexes);
    deletedFieldIndexes.forEach(i -> {
      sourceFields.remove(sourceFields.get(i));
      targetFields.remove(targetFields.get(i));
    });

    // 准备目的端的数据写入操作
    writer.prepareWrite(targetSchemaName, targetTableName, targetFields);

    // 清空目的端表的数据
    IDatabaseOperator targetOperator = DatabaseOperatorFactory
        .createDatabaseOperator(writer.getDataSource());
    targetOperator.truncateTableData(targetSchemaName, targetTableName);

    // 查询源端数据并写入目的端
    IDatabaseOperator sourceOperator = DatabaseOperatorFactory
        .createDatabaseOperator(sourceDataSource);
    sourceOperator.setFetchSize(BATCH_SIZE);

    StatementResultSet srs = sourceOperator.queryTableData(
        sourceSchemaName, sourceTableName, sourceFields
    );

    List<Object[]> cache = new LinkedList<>();
    long cacheBytes = 0;
    long totalCount = 0;
    long totalBytes = 0;
    try (ResultSet rs = srs.getResultset()) {
      while (rs.next()) {
        Object[] record = new Object[sourceFields.size()];
        for (int i = 1; i <= sourceFields.size(); ++i) {
          try {
            record[i - 1] = rs.getObject(i);
          } catch (Exception e) {
            log.warn("!!! Read data from table [ {} ] use function ResultSet.getObject() error",
                tableNameMapString, e);
            record[i - 1] = null;
          }
        }

        cache.add(record);
        cacheBytes += SizeOf.newInstance().deepSizeOf(record);
        ++totalCount;

        if (cache.size() >= BATCH_SIZE || cacheBytes >= MAX_CACHE_BYTES_SIZE) {
          long ret = writer.write(targetFields, cache);
          log.info("[FullCoverSync] handle table [{}] data count: {}, the batch bytes sie: {}",
              tableNameMapString, ret, BytesUnitUtils.bytesSizeToHuman(cacheBytes));
          cache.clear();
          totalBytes += cacheBytes;
          cacheBytes = 0;
        }
      }

      if (cache.size() > 0) {
        long ret = writer.write(targetFields, cache);
        log.info("[FullCoverSync] handle table [{}] data count: {}, last batch bytes sie: {}",
            tableNameMapString, ret, BytesUnitUtils.bytesSizeToHuman(cacheBytes));
        cache.clear();
        totalBytes += cacheBytes;
      }

      log.info("[FullCoverSync] handle table [{}] total data count:{}, total bytes={}",
          tableNameMapString, totalCount, BytesUnitUtils.bytesSizeToHuman(totalBytes));
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
   * @param writer 目的端的写入器
   */
  private Long doIncreaseSynchronize(IDatabaseWriter writer) {
    final int BATCH_SIZE = fetchSize;
    List<String> sourceFields = sourceColumnDescriptions.stream()
        .map(ColumnDescription::getFieldName)
        .collect(Collectors.toList());
    List<String> targetFields = targetColumnDescriptions.stream()
        .map(ColumnDescription::getFieldName)
        .collect(Collectors.toList());
    List<Integer> deletedFieldIndexes = new ArrayList<>();
    for (int i = 0; i < targetFields.size(); ++i) {
      if (StringUtils.isEmpty(targetFields.get(i))) {
        deletedFieldIndexes.add(i);
      }
    }
    Collections.reverse(deletedFieldIndexes);
    deletedFieldIndexes.forEach(i -> {
      sourceFields.remove(sourceFields.get(i));
      targetFields.remove(targetFields.get(i));
    });
    Map<String, String> columnNameMaps = new HashMap<>();
    for (int i = 0; i < sourceFields.size(); ++i) {
      columnNameMaps.put(sourceFields.get(i), targetFields.get(i));
    }

    TaskParamEntity.TaskParamEntityBuilder taskBuilder = TaskParamEntity.builder();
    taskBuilder.oldDataSource(writer.getDataSource());
    taskBuilder.oldSchemaName(targetSchemaName);
    taskBuilder.oldTableName(targetTableName);
    taskBuilder.newDataSource(sourceDataSource);
    taskBuilder.newSchemaName(sourceSchemaName);
    taskBuilder.newTableName(sourceTableName);
    taskBuilder.fieldColumns(sourceFields);
    taskBuilder.columnsMap(columnNameMaps);

    TaskParamEntity param = taskBuilder.build();

    IDatabaseSynchronize synchronizer = DatabaseSynchronizeFactory
        .createDatabaseWriter(writer.getDataSource());
    synchronizer.prepare(targetSchemaName, targetTableName, targetFields, targetPrimaryKeys);

    IDatabaseChangeCaculator calculator = new ChangeCalculatorService();
    calculator.setFetchSize(fetchSize);
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

          log.info("[IncreaseSync] Handle table [{}] data one batch size: {}",
              tableNameMapString, BytesUnitUtils.bytesSizeToHuman(cacheBytes));
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
            tableNameMapString, countTotal, countInsert, countUpdate, countDelete);
      }

      private void doInsert(List<String> fields) {
        long ret = synchronizer.executeInsert(cacheInsert);
        log.info("[IncreaseSync] Handle table [{}] data Insert count: {}", tableNameMapString, ret);
        cacheInsert.clear();
      }

      private void doUpdate(List<String> fields) {
        long ret = synchronizer.executeUpdate(cacheUpdate);
        log.info("[IncreaseSync] Handle table [{}] data Update count: {}", tableNameMapString, ret);
        cacheUpdate.clear();
      }

      private void doDelete(List<String> fields) {
        long ret = synchronizer.executeDelete(cacheDelete);
        log.info("[IncreaseSync] Handle table [{}] data Delete count: {}", tableNameMapString, ret);
        cacheDelete.clear();
      }

    });

    return totalBytes.get();
  }

}
