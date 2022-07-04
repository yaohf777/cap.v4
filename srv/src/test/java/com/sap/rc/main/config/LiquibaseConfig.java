package com.sap.rc.main.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.sap.rc.main.exception.ReadinessCheckException;

import liquibase.integration.spring.SpringLiquibase;

@Configuration
public class LiquibaseConfig {
    
    @Autowired
    private DataSource dataSource;

    @Autowired
    private ResourceLoader resourceLoader;

    @Bean("liquibase")
    @DependsOn("dataSource")
    public SpringLiquibase liquibase() {
    	
        // Locate change log file
        String changelogFile = "classpath:db/changelog.master.xml";
        Resource resource = resourceLoader.getResource(changelogFile);
        if (resource.exists() != true) {
            throw new ReadinessCheckException("Unable to find Liquibase change log file: {}" + changelogFile);
        }
       
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setChangeLog(changelogFile);
        liquibase.setDataSource(dataSource);
        return liquibase;
        
        
        /*
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setChangeLog(changelogFile);
        //liquibase.setContexts("test,dev,prod");
        liquibase.setDataSource(dataSource);
        liquibase.setDefaultSchema("mySchema");
        liquibase.setDropFirst(false);
        liquibase.setShouldRun(true);

        // Verbose logging
        Map<String, String> params = new HashMap<>();
        params.put("verbose", "true");
        liquibase.setChangeLogParameters(params);*/
    }
}
