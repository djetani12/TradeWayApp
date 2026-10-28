package com.tradeway.reporting;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class ReportingDataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "reporting.datasource")
    public DataSourceProperties reportingDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "reportingDataSource")
    public DataSource reportingDataSource(@Qualifier("reportingDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean(name = "reportingJdbcTemplate")
    public JdbcTemplate reportingJdbcTemplate(@Qualifier("reportingDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
