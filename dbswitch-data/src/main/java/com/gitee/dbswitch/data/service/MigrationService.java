// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.data.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitee.dbswitch.common.util.DbswitchStrUtils;
import com.gitee.dbswitch.core.model.TableDescription;
import com.gitee.dbswitch.core.service.IMetaDataByDatasourceService;
import com.gitee.dbswitch.core.service.impl.MetaDataByDataSourceServiceImpl;
import com.gitee.dbswitch.data.config.DbswichProperties;
import com.gitee.dbswitch.data.domain.PerfStat;
import com.gitee.dbswitch.data.entity.SourceDataSourceProperties;
import com.gitee.dbswitch.data.handler.MigrationHandler;
import com.gitee.dbswitch.data.util.BytesUnitUtils;
import com.gitee.dbswitch.data.util.DataSourceUtils;
import com.zaxxer.hikari.HikariDataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

/**
 * 数据迁移主逻辑类
 *
 * @author tang
 */
@Slf4j
@Service
public class MigrationService {

  /**
   * JSON序列化工具
   */
  private final ObjectMapper jackson = new ObjectMapper();

  /**
   * 性能统计记录表
   */
  private final List<PerfStat> perfStats = new ArrayList<>();

  /**
   * 线程是否被中断的标识
   */
  private volatile boolean interrupted = false;

  /**
   * 任务列表
   */
  private List<MigrationHandler> migrationHandlers = new ArrayList<>();

  /**
   * 配置参数
   */
  private final DbswichProperties properties;

  /**
   * 任务执行线程池
   */
  private final AsyncTaskExecutor taskExecutor;

  /**
   * 构造函数
   *
   * @param properties 配置信息
   */
  public MigrationService(DbswichProperties properties, AsyncTaskExecutor tableMigrationExecutor) {
    this.properties = Objects.requireNonNull(properties, "properties is null");
    this.taskExecutor = Objects.requireNonNull(tableMigrationExecutor, "taskExecutor is null");
  }

  /**
   * 中断执行中的任务
   */
  synchronized public void interrupt() {
    this.interrupted = true;
    migrationHandlers.forEach(MigrationHandler::interrupt);
  }

  /**
   * 执行主逻辑
   */
  public void run() throws Exception {
    StopWatch watch = new StopWatch();
    watch.start();

    log.info("dbswitch data service is started....");
    //log.info("Application properties configuration \n{}", properties);

    try (HikariDataSource targetDataSource = DataSourceUtils.createTargetDataSource(properties.getTarget())) {
      IMetaDataByDatasourceService tdsService = new MetaDataByDataSourceServiceImpl(targetDataSource);
      Set<String> tablesAlreadyExist = tdsService.queryTableList(properties.getTarget().getTargetSchema())
          .stream().map(TableDescription::getTableName).collect(Collectors.toSet());
      int sourcePropertiesIndex = 0;
      int totalTableCount = 0;
      List<SourceDataSourceProperties> sourcesProperties = properties.getSource();
      for (SourceDataSourceProperties sourceProperties : sourcesProperties) {
        if (interrupted) {
          throw new RuntimeException("task is interrupted");
        }
        try (HikariDataSource sourceDataSource = DataSourceUtils.createSourceDataSource(sourceProperties)) {
          IMetaDataByDatasourceService
              sourceMetaDataService = new MetaDataByDataSourceServiceImpl(sourceDataSource);

          // 判断处理的策略：是排除还是包含
          List<String> includes = DbswitchStrUtils.stringToList(sourceProperties.getSourceIncludes());
          log.info("Includes tables is :{}", jackson.writeValueAsString(includes));
          List<String> filters = DbswitchStrUtils
              .stringToList(sourceProperties.getSourceExcludes());
          log.info("Filter tables is :{}", jackson.writeValueAsString(filters));

          boolean useExcludeTables = includes.isEmpty();
          if (useExcludeTables) {
            log.info("!!!! Use dbswitch.source[{}].source-excludes parameter to filter tables",
                sourcePropertiesIndex);
          } else {
            log.info("!!!! Use dbswitch.source[{}].source-includes parameter to filter tables",
                sourcePropertiesIndex);
          }

          List<CompletableFuture<Void>> futures = new ArrayList<>();

          List<String> schemas = DbswitchStrUtils.stringToList(sourceProperties.getSourceSchema());
          log.info("Source schema names is :{}", jackson.writeValueAsString(schemas));

          AtomicInteger numberOfFailures = new AtomicInteger(0);
          AtomicLong totalBytesSize = new AtomicLong(0L);
          final int indexInternal = sourcePropertiesIndex;
          for (String schema : schemas) {
            if (interrupted) {
              break;
            }
            List<TableDescription> tableList = sourceMetaDataService.queryTableList(schema);
            if (tableList.isEmpty()) {
              log.warn("### Find source database table list empty for schema name is : {}", schema);
            } else {
              String allTableType = sourceProperties.getTableType();
              for (TableDescription td : tableList) {
                // 当没有配置迁移的表名时，默认为根据类型同步所有
                if (includes.isEmpty()) {
                  if (null != allTableType && !allTableType.equals(td.getTableType())) {
                    continue;
                  }
                }

                String tableName = td.getTableName();

                if (useExcludeTables) {
                  if (!filters.contains(tableName)) {
                    futures.add(
                        makeFutureTask(td, indexInternal, sourceDataSource, targetDataSource, tablesAlreadyExist,
                            numberOfFailures, totalBytesSize));
                  }
                } else {
                  if (includes.size() == 1 && (includes.get(0).contains("*") || includes.get(0).contains("?"))) {
                    if (Pattern.matches(includes.get(0), tableName)) {
                      futures.add(
                          makeFutureTask(td, indexInternal, sourceDataSource, targetDataSource, tablesAlreadyExist,
                              numberOfFailures, totalBytesSize));
                    }
                  } else if (includes.contains(tableName)) {
                    futures.add(
                        makeFutureTask(td, indexInternal, sourceDataSource, targetDataSource, tablesAlreadyExist,
                            numberOfFailures, totalBytesSize));
                  }
                }

              }

            }

          }
          if (!interrupted) {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();
            log.info(
                "#### Complete data migration for the [ {} ] data source:\ntotal count={}\nfailure count={}\ntotal bytes size={}",
                sourcePropertiesIndex, futures.size(), numberOfFailures.get(),
                BytesUnitUtils.bytesSizeToHuman(totalBytesSize.get()));
            perfStats.add(new PerfStat(sourcePropertiesIndex, futures.size(),
                numberOfFailures.get(), totalBytesSize.get()));
            ++sourcePropertiesIndex;
            totalTableCount += futures.size();
          }
        }
      }
      log.info("service run all success, total migrate table count={} ", totalTableCount);
    } finally {
      watch.stop();
      log.info("total ellipse = {} s", watch.getTotalTimeSeconds());

      StringBuilder sb = new StringBuilder();
      sb.append("===================================\n");
      sb.append(String.format("total ellipse time:\t %f s\n", watch.getTotalTimeSeconds()));
      sb.append("-------------------------------------\n");
      perfStats.forEach(st -> {
        sb.append(st);
        if (perfStats.size() > 1) {
          sb.append("-------------------------------------\n");
        }
      });
      sb.append("===================================\n");
      log.info("\n\n" + sb.toString());
    }
  }

  /**
   * 构造一个异步执行任务
   *
   * @param td               表描述上下文
   * @param indexInternal    源端索引号
   * @param sds              源端的DataSource数据源
   * @param tds              目的端的DataSource数据源
   * @param exists           目的端已经存在的表名列表
   * @param numberOfFailures 失败的数量
   * @param totalBytesSize   同步的字节大小
   * @return CompletableFuture<Void>
   */
  private CompletableFuture<Void> makeFutureTask(
      TableDescription td,
      Integer indexInternal,
      HikariDataSource sds,
      HikariDataSource tds,
      Set<String> exists,
      AtomicInteger numberOfFailures,
      AtomicLong totalBytesSize) {
    return CompletableFuture
        .supplyAsync(getMigrateHandler(td, indexInternal, sds, tds, exists), this.taskExecutor)
        .exceptionally(getExceptHandler(td, numberOfFailures))
        .thenAccept(totalBytesSize::addAndGet);
  }

  /**
   * 单表迁移处理方法
   *
   * @param td            表描述上下文
   * @param indexInternal 源端索引号
   * @param sds           源端的DataSource数据源
   * @param tds           目的端的DataSource数据源
   * @param exists        目的端已经存在的表名列表
   * @return Supplier<Long>
   */
  private Supplier<Long> getMigrateHandler(
      TableDescription td,
      Integer indexInternal,
      HikariDataSource sds,
      HikariDataSource tds,
      Set<String> exists) {
    MigrationHandler instance = MigrationHandler.createInstance(td, properties, indexInternal, sds, tds, exists);
    migrationHandlers.add(instance);
    return instance;
  }

  /**
   * 异常处理函数方法
   *
   * @param td               表描述上下文
   * @param numberOfFailures 失败记录数
   * @return Function<Throwable, Long>
   */
  private Function<Throwable, Long> getExceptHandler(
      TableDescription td,
      AtomicInteger numberOfFailures) {
    return (e) -> {
      log.error("Error migration for table: {}.{}, error message: {}",
          td.getSchemaName(), td.getTableName(), e.getMessage());
      numberOfFailures.incrementAndGet();
      throw new RuntimeException(e);
    };
  }

}
