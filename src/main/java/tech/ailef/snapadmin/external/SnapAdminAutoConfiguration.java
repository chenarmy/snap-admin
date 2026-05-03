/* 
 * SnapAdmin - An automatically generated CRUD admin UI for Spring Boot apps
 * Copyright (C) 2023 Ailef (http://ailef.tech)
 * 

 */

package tech.ailef.snapadmin.external;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import tech.ailef.snapadmin.internal.InternalSnapAdminConfiguration;
import tech.ailef.snapadmin.internal.config.MyBatisPlusConfig;

/**
 * The configuration class for "internal" data source. This is not the
 * source connected to the user's data/entities, but rather an internal
 * H2 database which is used by SnapAdmin to store user
 * settings and other information like operations history. 
 * Now uses MyBatis-Plus instead of JPA.
 */
@ConditionalOnProperty(name = "snapadmin.enabled", matchIfMissing = false)
@ComponentScan
@EnableConfigurationProperties(SnapAdminProperties.class)
@Configuration
@Import({InternalSnapAdminConfiguration.class, MyBatisPlusConfig.class})
public class SnapAdminAutoConfiguration {
	@Autowired
	private SnapAdminProperties props;

	/**
	 * Builds and returns the internal data source.
	 * 
	 * @return
	 */
	@Bean
	DataSource internalDataSource() {
		DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
		dataSourceBuilder.driverClassName("org.h2.Driver");
		if (props.isTestMode()) {
			dataSourceBuilder.url("jdbc:h2:mem:test");
		} else {
			dataSourceBuilder.url("jdbc:h2:file:./snapadmin_internal");
		}
		
		dataSourceBuilder.username("sa");
		dataSourceBuilder.password("password");
		return dataSourceBuilder.build();
	}

	/**
	 * The internal transaction manager for MyBatis-Plus.
	 * @return
	 */
	@Bean
	PlatformTransactionManager internalTransactionManager() {
		return new DataSourceTransactionManager(internalDataSource());
	}
	
	@Bean
	TransactionTemplate internalTransactionTemplate() {
	    return new TransactionTemplate(internalTransactionManager());
	}

}