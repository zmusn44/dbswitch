package com.gitee.dbswitch.data.util;

import com.gitee.dbswitch.data.config.DbswichProperties;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.Objects;

/**
 * DataSource工具类
 *
 * @author tang
 * @date 2021/6/8 22:00
 */
@Slf4j
public final class DataSouceUtils {

    /**
     * 创建于指定数据库连接描述符的连接池
     *
     * @param description 数据库连接描述符
     * @return HikariDataSource连接池
     */
    public static HikariDataSource createSourceDataSource(DbswichProperties.SourceDataSourceProperties description) {
        HikariDataSource ds = new HikariDataSource();
        ds.setPoolName("The_Source_DB_Connection");
        ds.setJdbcUrl(description.getUrl());
        ds.setDriverClassName(description.getDriverClassName());
        ds.setUsername(description.getUsername());
        ds.setPassword(description.getPassword());
        if (description.getDriverClassName().contains("oracle")) {
            ds.setConnectionTestQuery("SELECT 'Hello' from DUAL");
        } else if (description.getDriverClassName().contains("db2")) {
            ds.setConnectionTestQuery("SELECT 1 FROM SYSIBM.SYSDUMMY1");
        } else {
            ds.setConnectionTestQuery("SELECT 1");
        }
        ds.setMaximumPoolSize(8);
        ds.setMinimumIdle(5);
        ds.setConnectionTimeout(60000);
        ds.setIdleTimeout(60000);

        return ds;
    }

    /**
     * 创建于指定数据库连接描述符的连接池
     *
     * @param description 数据库连接描述符
     * @return HikariDataSource连接池
     */
    public static HikariDataSource createTargetDataSource(DbswichProperties.TargetDataSourceProperties description) {
        HikariDataSource ds = new HikariDataSource();
        ds.setPoolName("The_Target_DB_Connection");
        ds.setJdbcUrl(description.getUrl());
        ds.setDriverClassName(description.getDriverClassName());
        ds.setUsername(description.getUsername());
        ds.setPassword(description.getPassword());
        if (description.getDriverClassName().contains("oracle")) {
            ds.setConnectionTestQuery("SELECT 'Hello' from DUAL");
        } else if (description.getDriverClassName().contains("db2")) {
            ds.setConnectionTestQuery("SELECT 1 FROM SYSIBM.SYSDUMMY1");
        } else {
            ds.setConnectionTestQuery("SELECT 1");
        }
        ds.setMaximumPoolSize(8);
        ds.setMinimumIdle(5);
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
     * 关闭HikariDataSource
     *
     * @param dataSource
     */
    public static void closeHikariDataSource(HikariDataSource dataSource){
        try {
            dataSource.close();
        } catch (Exception e) {
            log.warn("Close data source error:", e);
        }
    }

    private DataSouceUtils(){}
}
