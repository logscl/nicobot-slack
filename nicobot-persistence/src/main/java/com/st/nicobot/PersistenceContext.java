package com.st.nicobot;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.ConnectionProvider;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;


/**
 * Created by Logs on 02-01-17.
 */
@Configuration
@EnableTransactionManagement
public class PersistenceContext {

    /**
     * Creates and configures the HikariCP datasource bean.
     * @param env   The runtime environment of  our application.
     * @return
     */
    @Bean(destroyMethod = "close")
    DataSource dataSource(Environment env) {
        HikariConfig dataSourceConfig = new HikariConfig();
        dataSourceConfig.setDriverClassName(env.getRequiredProperty("nicobot.database.driver"));
        dataSourceConfig.setJdbcUrl(env.getRequiredProperty("nicobot.database.jdbc"));

        return new HikariDataSource(dataSourceConfig);
    }

    @Bean
    DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    TransactionAwareDataSourceProxy transactionAwareDataSource(DataSource dataSource) {
        return new TransactionAwareDataSourceProxy(dataSource);
    }

    @Bean
    DataSourceConnectionProvider connectionProvider(TransactionAwareDataSourceProxy transactionAwareDataSource) {
        return new DataSourceConnectionProvider(transactionAwareDataSource);
    }

    @Bean
    DefaultConfiguration config(Environment env, ConnectionProvider provider) {
        DefaultConfiguration config = new DefaultConfiguration();
        config.setConnectionProvider(provider);
        return config;
    }

    @Bean
    DefaultDSLContext dsl(DefaultConfiguration config) {
        return new DefaultDSLContext(config);
    }


}
