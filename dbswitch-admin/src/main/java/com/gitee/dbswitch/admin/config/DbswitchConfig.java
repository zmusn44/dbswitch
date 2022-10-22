package com.gitee.dbswitch.admin.config;

import java.util.Properties;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DbswitchConfig {

  @Bean
  public DatabaseIdProvider getDatabaseIdProvider() {
    DatabaseIdProvider databaseIdProvider = new VendorDatabaseIdProvider();
    Properties props = new Properties();
    props.setProperty("PostgreSQL", "postgresql");
    props.setProperty("MySQL", "mysql");
    databaseIdProvider.setProperties(props);
    return databaseIdProvider;
  }

}
