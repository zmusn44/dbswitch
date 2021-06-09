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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import com.gitee.dbswitch.core.service.impl.MigrationMetaDataServiceImpl;
import com.gitee.dbswitch.data.domain.PerfStat;
import com.gitee.dbswitch.data.handler.MigrationHandler;
import com.gitee.dbswitch.data.util.BytesUnitUtils;
import com.gitee.dbswitch.data.util.DataSouceUtils;
import com.gitee.dbswitch.data.util.StrUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
@Service
public class MainService {

    private ObjectMapper jackson = new ObjectMapper();

    @Autowired
    private DbswichProperties properties;

    private List<PerfStat> perfStats;

    public MainService(){
        perfStats=new ArrayList<>();
    }

    /**
     * 执行主逻辑
     */
    public void run() {
        StopWatch watch = new StopWatch();
        watch.start();

        log.info("service is started....");

        try {
            //log.info("Application properties configuration \n{}", properties);

            HikariDataSource targetDataSource = DataSouceUtils.createTargetDataSource(properties.getTarget());

            List<DbswichProperties.SourceDataSourceProperties> sourcesProperties = properties.getSource();

            int sourcePropertiesIndex = 0;
            int totalTableCount = 0;
            for (DbswichProperties.SourceDataSourceProperties sourceProperties : sourcesProperties) {

                HikariDataSource sourceDataSource = DataSouceUtils.createSourceDataSource(sourceProperties);
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
                AtomicLong totalBytesSize=new AtomicLong(0L);
                final int indexInternal=sourcePropertiesIndex;
                for (String schema : schemas) {
                    // 读取源库指定schema里所有的表
                    List<TableDescription> tableList = sourceMetaDataService.queryTableList(sourceProperties.getUrl(),
                            sourceProperties.getUsername(), sourceProperties.getPassword(), schema);
                    if (tableList.isEmpty()) {
                        log.warn("### Find source database table list empty for schema name is : {}", schema);
                    } else {
                        for (TableDescription td : tableList) {
                            String tableName = td.getTableName();
                            Supplier<Long> supplier = () -> new MigrationHandler(td, properties, indexInternal, sourceDataSource, targetDataSource).call();
                            Function<Throwable, Long> exceptFunction = (e) -> {
                                log.error("Error migration for table: {}.{}, error message:", td.getSchemaName(), td.getTableName(), e);
                                numberOfFailures.incrementAndGet();
                                throw new RuntimeException(e);
                            };
                            Consumer<Long> finishConsumer = (r) -> totalBytesSize.addAndGet(r.longValue());
                            CompletableFuture<Void> future = CompletableFuture.supplyAsync(supplier).exceptionally(exceptFunction).thenAccept(finishConsumer);

                            if (useExcludeTables) {
                                if (!filters.contains(tableName)) {
                                    futures.add(future);
                                }
                            } else {
                                if (includes.size() == 1 && (includes.get(0).contains("*") || includes.get(0).contains("?"))) {
                                    if (Pattern.matches(includes.get(0), tableName)) {
                                        futures.add(future);
                                    }
                                } else if (includes.contains(tableName)) {
                                    futures.add(future);
                                }
                            }

                        }

                    }

                }

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();
                log.info("#### Complete data migration for the [ {} ] data source:\ntotal count={}\nfailure count={}\ntotal bytes size={}",
                        sourcePropertiesIndex, futures.size(), numberOfFailures.get(), BytesUnitUtils.bytesSizeToHuman(totalBytesSize.get()));
                perfStats.add(new PerfStat(sourcePropertiesIndex,futures.size(),numberOfFailures.get(),totalBytesSize.get()));
                DataSouceUtils.closeHikariDataSource(sourceDataSource);

                ++sourcePropertiesIndex;
                totalTableCount += futures.size();
            }
            log.info("service run all success, total migrate table count={} ", totalTableCount);
        } catch (Exception e) {
            log.error("error:", e);
        } finally {
            watch.stop();
            log.info("total elipse = {} s", watch.getTotalTimeSeconds());
            System.out.println("===================================");
            System.out.println(String.format("total elipse time:\t %f s", watch.getTotalTimeSeconds()));
            this.perfStats.stream().forEach(st -> System.out.println(st));
            System.out.println("===================================");
        }
    }

}
