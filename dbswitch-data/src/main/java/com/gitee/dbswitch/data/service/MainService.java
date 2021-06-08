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
import java.util.regex.Pattern;
import javax.sql.DataSource;

import com.gitee.dbswitch.core.service.impl.MigrationMetaDataServiceImpl;
import com.gitee.dbswitch.data.handler.MigrationHandler;
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
@Service("MainService")
public class MainService {

    private ObjectMapper jackson = new ObjectMapper();

    @Autowired
    private DbswichProperties properties;

    /**
     * 执行主逻辑
     */
    public void run() {
        StopWatch watch = new StopWatch();
        watch.start();

        log.info("service is running....");

        try {
            //log.info("Application properties configuration \n{}", jackson.writeValueAsString(properties));

            HikariDataSource targetDataSource = DataSouceUtils.createTargetDataSource(properties.getTarget());

            List<DbswichProperties.SourceDataSourceProperties> sourcesProperties = properties.getSource();

            int sourcePropertiesIndex = 0;
            int totalTableCount = 0;
            for (DbswichProperties.SourceDataSourceProperties sourceProperties : sourcesProperties) {

                HikariDataSource sourceDataSource = DataSouceUtils.createSourceDataSource(sourceProperties);
                IMetaDataService sourceMetaDataService = getMetaDataService(sourceDataSource);

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

                AtomicInteger numberOfFailures = new AtomicInteger();

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
                                    futures.add(CompletableFuture.runAsync(
                                            new MigrationHandler(td, properties, sourcePropertiesIndex, sourceDataSource, targetDataSource)
                                            ).exceptionally(
                                            (e) -> {
                                                log.error("Error migration table: {}.{}, error message:", td.getSchemaName(), td.getTableName(), e);
                                                numberOfFailures.incrementAndGet();
                                                throw new RuntimeException(e);
                                            }
                                            )
                                    );
                                }
                            } else {
                                if (includes.size() == 1 && includes.get(0).contains("*")) {
                                    if (Pattern.matches(includes.get(0), tableName)) {
                                        futures.add(CompletableFuture.runAsync(
                                                new MigrationHandler(td, properties, sourcePropertiesIndex, sourceDataSource, targetDataSource)
                                                ).exceptionally(
                                                (e) -> {
                                                    log.error("Error migration table: {}.{}, error message:", td.getSchemaName(), td.getTableName(), e);
                                                    numberOfFailures.incrementAndGet();
                                                    throw new RuntimeException(e);
                                                }
                                                )
                                        );
                                    }
                                } else if (includes.contains(tableName)) {
                                    futures.add(CompletableFuture.runAsync(
                                            new MigrationHandler(td, properties, sourcePropertiesIndex, sourceDataSource, targetDataSource)
                                            ).exceptionally(
                                            (e) -> {
                                                log.error("Error migration table: {}.{}, error message:", td.getSchemaName(), td.getTableName(), e);
                                                numberOfFailures.incrementAndGet();
                                                throw new RuntimeException(e);
                                            }
                                            )
                                    );
                                }
                            }

                        }

                    }

                }

                CompletableFuture<Void> allFuture=CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{}));
                allFuture.get();
                log.info("#### Complete data migration for the [ {} ] data source ,total count={}, failure count={}",
                        sourcePropertiesIndex, futures.size(), numberOfFailures);
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
        }
    }

    /**
     * 获取MetaDataService对象
     *
     * @param dataSource
     * @return IMetaDataService
     */
    private IMetaDataService getMetaDataService(DataSource dataSource) {
        IMetaDataService metaDataService = new MigrationMetaDataServiceImpl();
        metaDataService.setDatabaseConnection(JdbcTemplateUtils.getDatabaseProduceName(dataSource));
        return metaDataService;
    }

}
