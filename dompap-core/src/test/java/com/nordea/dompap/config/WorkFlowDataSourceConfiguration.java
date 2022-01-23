package com.nordea.dompap.config;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class WorkFlowDataSourceConfiguration {
    @Bean
    @ConfigurationProperties("workflow.datasource")
    public DataSource getDataSource() {
        return DataSourceBuilder.create().type(BasicDataSource.class).build();
//        return DataSourceBuilder.create().build();
    }

}
