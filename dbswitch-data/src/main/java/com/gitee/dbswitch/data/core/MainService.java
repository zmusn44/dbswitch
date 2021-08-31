// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.data.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import com.gitee.dbswitch.data.domain.PerfStat;
import com.gitee.dbswitch.data.handler.MigrationHandler;
import com.gitee.dbswitch.data.util.BytesUnitUtils;
import com.gitee.dbswitch.data.util.DataSouceUtils;
import com.gitee.dbswitch.data.util.StrUtils;
import com.gitee.dbswitch.core.service.impl.MigrationMetaDataServiceImpl;
import org.springframework.util.StopWatch;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitee.dbswitch.core.model.TableDescription;
import com.gitee.dbswitch.core.service.IMetaDataService;
import com.gitee.dbswitch.data.config.DbswichProperties;
import com.gitee.dbswitch.data.util.JdbcTemplateUtils;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

/**
 * 数据迁移主逻辑类
 *
 * @author tang
 */
@Slf4j
public class MainService {

    /**
     * JSON序列化工具
     */
    private final ObjectMapper jackson = new ObjectMapper();

    /**
     * 性能统计记录表
     */
    private final List<PerfStat> perfStats = new ArrayList<>();

    /**
     * 配置参数
     */
    private DbswichProperties properties;

    /**
     * 构造函数
     *
     * @param properties 配置信息
     */
    public MainService(DbswichProperties properties){
        this.properties=properties;
    }

    /**
     * 执行主逻辑
     */
    public void run() throws Exception {
        StopWatch watch = new StopWatch();
        watch.start();

        log.info("dbswitch data service is started....");

        try {
            //log.info("Application properties configuration \n{}", properties);
            List<DbswichProperties.SourceDataSourceProperties> sourcesProperties = properties.getSource();

            HikariDataSource targetDataSource = DataSouceUtils.createTargetDataSource(properties.getTarget());

            int sourcePropertiesIndex = 0;
            int totalTableCount = 0;
            for (DbswichProperties.SourceDataSourceProperties sourceProperties : sourcesProperties) {

                try (HikariDataSource sourceDataSource = DataSouceUtils.createSourceDataSource(sourceProperties)) {
                    IMetaDataService sourceMetaDataService = new MigrationMetaDataServiceImpl();

                    sourceMetaDataService.setDatabaseConnection(JdbcTemplateUtils.getDatabaseProduceName(sourceDataSource));

                    // 判断处理的策略：是排除还是包含
                    List<String> includes = StrUtils.stringToList(sourceProperties.getSourceIncludes());
                    log.info("Includes tables is :{}", jackson.writeValueAsString(includes));
                    List<String> filters = StrUtils.stringToList(sourceProperties.getSourceExcludes());
                    log.info("Filter tables is :{}", jackson.writeValueAsString(filters));

                    boolean useExcludeTables = includes.isEmpty();
                    if (useExcludeTables) {
                        log.info("!!!! Use dbswitch.source[{}].source-excludes parameter to filter tables", sourcePropertiesIndex);
                    } else {
                        log.info("!!!! Use dbswitch.source[{}].source-includes parameter to filter tables", sourcePropertiesIndex);
                    }

                    List<CompletableFuture<Void>> futures = new ArrayList<>();

                    List<String> schemas = StrUtils.stringToList(sourceProperties.getSourceSchema());
                    log.info("Source schema names is :{}", jackson.writeValueAsString(schemas));

                    AtomicInteger numberOfFailures = new AtomicInteger(0);
                    AtomicLong totalBytesSize = new AtomicLong(0L);
                    final int indexInternal = sourcePropertiesIndex;
                    for (String schema : schemas) {
                        // 读取源库指定schema里所有的表
                        List<TableDescription> tableList = sourceMetaDataService.queryTableList(sourceProperties.getUrl(),
                                sourceProperties.getUsername(), sourceProperties.getPassword(), schema);
                        if (tableList.isEmpty()) {
                            log.warn("### Find source database table list empty for schema name is : {}", schema);
                        } else {
                            for (TableDescription td : tableList) {
                                String tableName = td.getTableName();

                                if (useExcludeTables) {
                                    if (!filters.contains(tableName)) {
                                        futures.add(makeFutureTask(td, indexInternal, sourceDataSource, targetDataSource, numberOfFailures, totalBytesSize));
                                    }
                                } else {
                                    if (includes.size() == 1 && (includes.get(0).contains("*") || includes.get(0).contains("?"))) {
                                        if (Pattern.matches(includes.get(0), tableName)) {
                                            futures.add(makeFutureTask(td, indexInternal, sourceDataSource, targetDataSource, numberOfFailures, totalBytesSize));
                                        }
                                    } else if (includes.contains(tableName)) {
                                        futures.add(makeFutureTask(td, indexInternal, sourceDataSource, targetDataSource, numberOfFailures, totalBytesSize));
                                    }
                                }

                            }

                        }

                    }

                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();
                    log.info("#### Complete data migration for the [ {} ] data source:\ntotal count={}\nfailure count={}\ntotal bytes size={}",
                            sourcePropertiesIndex, futures.size(), numberOfFailures.get(), BytesUnitUtils.bytesSizeToHuman(totalBytesSize.get()));

                    perfStats.add(new PerfStat(sourcePropertiesIndex, futures.size(), numberOfFailures.get(), totalBytesSize.get()));

                    ++sourcePropertiesIndex;
                    totalTableCount += futures.size();
                }
            }
            log.info("service run all success, total migrate table count={} ", totalTableCount);
        } catch (Exception e) {
            log.error("error:", e);
            throw e;
        } finally {
            watch.stop();
            log.info("total elipse = {} s", watch.getTotalTimeSeconds());

            StringBuilder sb = new StringBuilder();
            sb.append("===================================\n");
            sb.append(String.format("total elipse time:\t %f s\n", watch.getTotalTimeSeconds()));
            sb.append("-------------------------------------\n");
            perfStats.forEach(st -> {
                sb.append(st);
                if(perfStats.size()>1){
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
     * @param numberOfFailures 失败的数量
     * @param totalBytesSize   同步的字节大小
     * @return CompletableFuture<Void>
     */
    private CompletableFuture<Void> makeFutureTask(TableDescription td, Integer indexInternal,
                                                   HikariDataSource sds, HikariDataSource tds,
                                                   AtomicInteger numberOfFailures, AtomicLong totalBytesSize) {
        return CompletableFuture.supplyAsync(getMigrateHandler(td, indexInternal, sds, tds))
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
     * @return Supplier<Long>
     */
    private Supplier<Long> getMigrateHandler(TableDescription td, Integer indexInternal, HikariDataSource sds, HikariDataSource tds) {
        return () -> MigrationHandler.createInstance(td, properties, indexInternal, sds, tds).get();
    }

    /**
     * 异常处理函数方法
     *
     * @param td               表描述上下文
     * @param numberOfFailures 失败记录数
     * @return Function<Throwable, Long>
     */
    private Function<Throwable, Long> getExceptHandler(TableDescription td, AtomicInteger numberOfFailures) {
        return (e) -> {
            log.error("Error migration for table: {}.{}, error message:", td.getSchemaName(), td.getTableName(), e);
            numberOfFailures.incrementAndGet();
            throw new RuntimeException(e);
        };
    }

}