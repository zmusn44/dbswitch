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

import com.gitee.dbswitch.common.type.ProductTypeEnum;
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
 * ?????????????????????????????????????????????
 *
 * @author tang
 */
@Slf4j
public class MigrationHandler implements Supplier<Long> {

  private final long MAX_CACHE_BYTES_SIZE = 128 * 1024 * 1024;

  private int fetchSize = 100;
  private final DbswichProperties properties;
  private final SourceDataSourceProperties sourceProperties;

  private volatile boolean interrupted = false;

  // ?????????
  private final HikariDataSource sourceDataSource;
  private ProductTypeEnum sourceProductType;
  private String sourceSchemaName;
  private String sourceTableName;
  private String sourceTableRemarks;
  private List<ColumnDescription> sourceColumnDescriptions;
  private List<String> sourcePrimaryKeys;

  private IMetaDataByDatasourceService sourceMetaDataService;

  // ?????????
  private final HikariDataSource targetDataSource;
  private ProductTypeEnum targetProductType;
  private Set<String> targetExistTables;
  private String targetSchemaName;
  private String targetTableName;
  private List<ColumnDescription> targetColumnDescriptions;
  private List<String> targetPrimaryKeys;

  // ???????????????????????????
  private String tableNameMapString;

  public static MigrationHandler createInstance(TableDescription td,
      DbswichProperties properties,
      Integer sourcePropertiesIndex,
      HikariDataSource sds,
      HikariDataSource tds,
      Set<String> targetExistTables) {
    return new MigrationHandler(td, properties, sourcePropertiesIndex, sds, tds, targetExistTables);
  }

  private MigrationHandler(TableDescription td,
      DbswichProperties properties,
      Integer sourcePropertiesIndex,
      HikariDataSource sds,
      HikariDataSource tds,
      Set<String> targetExistTables) {
    this.sourceSchemaName = td.getSchemaName();
    this.sourceTableName = td.getTableName();
    this.properties = properties;
    this.sourceProperties = properties.getSource().get(sourcePropertiesIndex);
    this.sourceDataSource = sds;
    this.targetDataSource = tds;

    if (sourceProperties.getFetchSize() >= fetchSize) {
      fetchSize = sourceProperties.getFetchSize();
    }

    this.targetExistTables = targetExistTables;
    // ?????????????????????????????????
    this.targetSchemaName = properties.getTarget().getTargetSchema();
    this.targetTableName = PatterNameUtils.getFinalName(td.getTableName(),
        sourceProperties.getRegexTableMapper());

    if (StringUtils.isEmpty(this.targetTableName)) {
      throw new RuntimeException("?????????????????????????????????????????????[" + this.sourceTableName + "]????????????");
    }

    this.tableNameMapString = String.format("%s.%s --> %s.%s",
        td.getSchemaName(), td.getTableName(),
        targetSchemaName, targetTableName);
  }

  public void interrupt() {
    this.interrupted = true;
  }

  @Override
  public Long get() {
    log.info("Begin Migrate table for {}", tableNameMapString);

    this.sourceProductType = DatabaseAwareUtils.getDatabaseTypeByDataSource(sourceDataSource);
    this.targetProductType = DatabaseAwareUtils.getDatabaseTypeByDataSource(targetDataSource);
    this.sourceMetaDataService = new MetaDataByDataSourceServiceImpl(sourceDataSource,
        sourceProductType);

    // ????????????????????????????????????
    this.sourceTableRemarks = sourceMetaDataService
        .getTableRemark(sourceSchemaName, sourceTableName);
    this.sourceColumnDescriptions = sourceMetaDataService
        .queryTableColumnMeta(sourceSchemaName, sourceTableName);
    this.sourcePrimaryKeys = sourceMetaDataService
        .queryTablePrimaryKeys(sourceSchemaName, sourceTableName);

    // ???????????????????????????????????????????????????????????????
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

    // ???????????????????????????????????????
    List<String> columnMapperPairs = new ArrayList<>();
    Map<String, String> mapChecker = new HashMap<>();
    for (int i = 0; i < sourceColumnDescriptions.size(); ++i) {
      String sourceColumnName = sourceColumnDescriptions.get(i).getFieldName();
      String targetColumnName = targetColumnDescriptions.get(i).getFieldName();
      if (StringUtils.hasLength(targetColumnName)) {
        columnMapperPairs.add(String.format("%s --> %s", sourceColumnName, targetColumnName));
        mapChecker.put(sourceColumnName, targetColumnName);
      } else {
        columnMapperPairs.add(String.format(
            "%s --> %s",
            sourceColumnName,
            String.format("<!Field(%s) is Deleted>", (i + 1))
        ));
      }
    }
    log.info("Mapping relation : \ntable mapper :\n\t{}  \ncolumn mapper :\n\t{} ",
        tableNameMapString, String.join("\n\t", columnMapperPairs));
    Set<String> valueSet = new HashSet<>(mapChecker.values());
    if (valueSet.size() <= 0) {
      throw new RuntimeException("???????????????????????????????????????????????????????????????????????????!");
    }
    if (!valueSet.containsAll(this.targetPrimaryKeys)) {
      throw new RuntimeException("????????????????????????????????????????????????????????????????????????!");
    }
    if (mapChecker.keySet().size() != valueSet.size()) {
      throw new RuntimeException("???????????????????????????????????????????????????????????????????????????!");
    }
    if (interrupted) {
      log.info("task job is interrupted!");
      throw new RuntimeException("task is interrupted");
    }
    IDatabaseWriter writer = DatabaseWriterFactory.createDatabaseWriter(
        targetDataSource, properties.getTarget().getWriterEngineInsert());

    if (properties.getTarget().getTargetDrop()) {
      /*
        ???????????????dbswitch.target.datasource-target-drop=true??????
        <p>
        ?????????drop table?????????????????????create table??????
       */

      try {
        DatabaseOperatorFactory.createDatabaseOperator(targetDataSource)
            .dropTable(targetSchemaName, targetTableName);
        log.info("Target Table {}.{} is exits, drop it now !", targetSchemaName, targetTableName);
      } catch (Exception e) {
        log.info("Target Table {}.{} is not exits, create it!", targetSchemaName, targetTableName);
      }

      // ???????????????????????????
      List<String> sqlCreateTable = sourceMetaDataService.getDDLCreateTableSQL(
          targetProductType,
          targetColumnDescriptions.stream()
              .filter(column -> StringUtils.hasLength(column.getFieldName()))
              .collect(Collectors.toList()),
          targetPrimaryKeys,
          targetSchemaName,
          targetTableName,
          sourceTableRemarks,
          properties.getTarget().getCreateTableAutoIncrement()
      );

      JdbcTemplate targetJdbcTemplate = new JdbcTemplate(targetDataSource);
      for (String sql : sqlCreateTable) {
        targetJdbcTemplate.execute(sql);
        log.info("Execute SQL: \n{}", sql);
      }

      // ??????????????????????????????????????????
      if (null != properties.getTarget().getOnlyCreate()
          && properties.getTarget().getOnlyCreate()) {
        return 0L;
      }

      if (interrupted) {
        log.info("task job is interrupted!");
        throw new RuntimeException("task is interrupted");
      }

      return doFullCoverSynchronize(writer);
    } else {
      // ??????????????????????????????????????????????????????????????????????????????
      if (null != properties.getTarget().getOnlyCreate()
          && properties.getTarget().getOnlyCreate()) {
        return 0L;
      }

      if (interrupted) {
        log.info("task job is interrupted!");
        throw new RuntimeException("task is interrupted");
      }

      if (!targetExistTables.contains(targetTableName)) {
        // ???????????????????????????????????????????????????????????????
        List<String> sqlCreateTable = sourceMetaDataService.getDDLCreateTableSQL(
            targetProductType,
            targetColumnDescriptions.stream()
                .filter(column -> StringUtils.hasLength(column.getFieldName()))
                .collect(Collectors.toList()),
            targetPrimaryKeys,
            targetSchemaName,
            targetTableName,
            sourceTableRemarks,
            properties.getTarget().getCreateTableAutoIncrement()
        );

        JdbcTemplate targetJdbcTemplate = new JdbcTemplate(targetDataSource);
        for (String sql : sqlCreateTable) {
          targetJdbcTemplate.execute(sql);
          log.info("Execute SQL: \n{}", sql);
        }

        if (interrupted) {
          log.info("task job is interrupted!");
          throw new RuntimeException("task is interrupted");
        }

        return doFullCoverSynchronize(writer);
      }

      // ????????????????????????????????????????????????1????????????????????????????????????????????????????????????(2)MySQL??????Innodb?????????
      if (properties.getTarget().getChangeDataSync()) {
        // ?????????????????????????????????????????????????????????????????????
        IMetaDataByDatasourceService metaDataByDatasourceService =
            new MetaDataByDataSourceServiceImpl(targetDataSource, targetProductType);
        List<String> dbTargetPks = metaDataByDatasourceService.queryTablePrimaryKeys(
            targetSchemaName, targetTableName);

        if (!targetPrimaryKeys.isEmpty() && !dbTargetPks.isEmpty()
            && targetPrimaryKeys.containsAll(dbTargetPks)
            && dbTargetPks.containsAll(targetPrimaryKeys)) {
          if (targetProductType == ProductTypeEnum.MYSQL
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
   * ??????????????????
   *
   * @param writer ?????????????????????
   */
  private Long doFullCoverSynchronize(IDatabaseWriter writer) {
    final int BATCH_SIZE = fetchSize;

    List<String> sourceFields = new ArrayList<>();
    List<String> targetFields = new ArrayList<>();
    for (int i = 0; i < targetColumnDescriptions.size(); ++i) {
      ColumnDescription scd = sourceColumnDescriptions.get(i);
      ColumnDescription tcd = targetColumnDescriptions.get(i);
      if (!StringUtils.isEmpty(tcd.getFieldName())) {
        sourceFields.add(scd.getFieldName());
        targetFields.add(tcd.getFieldName());
      }
    }
    // ????????????????????????????????????
    writer.prepareWrite(targetSchemaName, targetTableName, targetFields);

    // ???????????????????????????
    IDatabaseOperator targetOperator = DatabaseOperatorFactory
        .createDatabaseOperator(writer.getDataSource());
    targetOperator.truncateTableData(targetSchemaName, targetTableName);

    // ????????????????????????????????????
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
        if (interrupted) {
          log.info("task job is interrupted!");
          throw new RuntimeException("task is interrupted");
        }
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
        long bytes = SizeOf.newInstance().deepSizeOf(record);
        cacheBytes += bytes;
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
   * ???????????????
   *
   * @param writer ?????????????????????
   */
  private Long doIncreaseSynchronize(IDatabaseWriter writer) {
    final int BATCH_SIZE = fetchSize;

    List<String> sourceFields = new ArrayList<>();
    List<String> targetFields = new ArrayList<>();
    Map<String, String> columnNameMaps = new HashMap<>();
    for (int i = 0; i < targetColumnDescriptions.size(); ++i) {
      ColumnDescription scd = sourceColumnDescriptions.get(i);
      ColumnDescription tcd = targetColumnDescriptions.get(i);
      if (!StringUtils.isEmpty(tcd.getFieldName())) {
        sourceFields.add(scd.getFieldName());
        targetFields.add(tcd.getFieldName());
        columnNameMaps.put(scd.getFieldName(), tcd.getFieldName());
      }
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

    // ?????????????????????????????????
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

        long bytes = SizeOf.newInstance().deepSizeOf(record);
        cacheBytes += bytes;
        totalBytes.addAndGet(bytes);
        countTotal++;
        checkFull(fields);
      }

      /**
       * ?????????????????????????????????????????????????????????
       *
       * @param fields ?????????????????????
       */
      private void checkFull(List<String> fields) {
        if (interrupted) {
          log.info("task job is interrupted!");
          throw new RuntimeException("task is interrupted");
        }
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
