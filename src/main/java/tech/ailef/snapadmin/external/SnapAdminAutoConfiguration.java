/* 
 * SnapAdmin - An automatically generated CRUD admin UI for Spring Boot apps
 * Copyright (C) 2023 Ailef (http://ailef.tech)
 * 

 */

package tech.ailef.snapadmin.external;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import tech.ailef.snapadmin.internal.InternalSnapAdminConfiguration;
//import tech.ailef.snapadmin.internal.config.MyBatisPlusConfig;

/**
 * Auto-configuration for SnapAdmin.
 * Uses the main DataSource from the host application.
 */
@ConditionalOnProperty(name = "snapadmin.enabled", matchIfMissing = false)
@ComponentScan
@EnableConfigurationProperties(SnapAdminProperties.class)
@Configuration
@Import({InternalSnapAdminConfiguration.class})
public class SnapAdminAutoConfiguration {

	@Autowired
	private DataSource dataSource;

	/**
	 * Initialize internal tables on startup using the main DataSource.
	 */
	@Bean
	@Primary
	PlatformTransactionManager internalTransactionManager() {
		return new DataSourceTransactionManager(dataSource);
	}

	@Bean
	TransactionTemplate internalTransactionTemplate() {
	    return new TransactionTemplate(internalTransactionManager());
	}

	/**
	 * Ensures internal tables exist on startup using the main DataSource.
	 */
	@Bean
	ApplicationRunner initInternalTables() {
		return args -> {
			try {
				JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
				jdbcTemplate.execute(
					"CREATE TABLE IF NOT EXISTS `console_query` (" +
					"`id` VARCHAR(255) PRIMARY KEY, `tenant_id` varchar(255), " +
					"`sql` TEXT, " +
					"`title` VARCHAR(255), " +
					"`created_at` TIMESTAMP, " +
					"`updated_at` TIMESTAMP)"
				);

				jdbcTemplate.execute(
					"CREATE TABLE IF NOT EXISTS `user_action` (" +
					"`id` INTEGER AUTO_INCREMENT PRIMARY KEY, " +
					"`created_at` TIMESTAMP, `tenant_id` varchar(255), " +
					"`sql` TEXT, " +
					"`java_class` VARCHAR(255), " +
					"`on_table` VARCHAR(255), " +
					"`primary_key` VARCHAR(255), " +
					"`action_type` VARCHAR(50), " +
					"`username` VARCHAR(255))"
				);

				jdbcTemplate.execute(
					"CREATE TABLE IF NOT EXISTS `user_setting` (" +
					"`id` VARCHAR(255) PRIMARY KEY, `tenant_id` varchar(255), " +
					"`setting_value` TEXT)"
				);
			} catch (Exception e) {
				System.out.println("Note: internal tables initialization: " + e.getMessage());
			}
		};
	}

}
