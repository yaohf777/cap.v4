package com.sap.rc.main.config;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

/*
 * Spring Boot auto configuration for database 
 * dataSource --> DataSourceConfiguration & application.properties
 * 
 * Refer to: https://www.javacodegeeks.com/2016/05/approaches-binding-spring-boot-application-service-cloud-foundry.html
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.sap.rc")
@Profile("cloud")
public class CloudDatabaseConfig extends AbstractCloudConfig {

    @Value("${application.basepackage:com.sap.rc}")
    private String basePackage;
    
    @Bean("entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        return EntityManagerFactoryProvider.get(dataSource, basePackage);
    }

    @Bean("transactionManager")
    public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
