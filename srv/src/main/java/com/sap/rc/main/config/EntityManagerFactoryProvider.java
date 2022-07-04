package com.sap.rc.main.config;

import static org.eclipse.persistence.config.PersistenceUnitProperties.ALLOW_NATIVE_SQL_QUERIES;
import static org.eclipse.persistence.config.PersistenceUnitProperties.CACHE_SHARED_DEFAULT;
import static org.eclipse.persistence.config.PersistenceUnitProperties.CLASSLOADER;
import static org.eclipse.persistence.config.PersistenceUnitProperties.CONNECTION_POOL_MAX;
import static org.eclipse.persistence.config.PersistenceUnitProperties.LOGGING_LEVEL;
import static org.eclipse.persistence.config.PersistenceUnitProperties.SCHEMA_DATABASE_MAJOR_VERSION;
import static org.eclipse.persistence.config.PersistenceUnitProperties.SCHEMA_DATABASE_PRODUCT_NAME;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.persistence.jpa.PersistenceProvider;
import org.springframework.instrument.classloading.SimpleLoadTimeWeaver;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;

import com.sap.rc.main.multitenancy.TenantContext;
import com.sap.rc.main.util.Constants;

public class EntityManagerFactoryProvider {

	/**
	 * Based on the given DataSource instance, create and configure an EntityManagerFactory. Part of this configuration
	 * is that the given packages are scanned for entity classes, so that the created EntityManagers know about them.
	 */
	public static LocalContainerEntityManagerFactoryBean get(DataSource dataSource, String... packagesToScan) {

		LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();

		entityManagerFactory.setPersistenceProvider(new PersistenceProvider());
		entityManagerFactory.setPackagesToScan(packagesToScan);
		entityManagerFactory.setDataSource(dataSource);

		// For JPA we use the class loader that Spring uses to avoid class loader issues
		entityManagerFactory.setJpaPropertyMap(getJPAProperties(dataSource.getClass().getClassLoader()));
		entityManagerFactory.setLoadTimeWeaver(new SimpleLoadTimeWeaver());
		entityManagerFactory.setJpaVendorAdapter(new EclipseLinkJpaVendorAdapter());

		entityManagerFactory.afterPropertiesSet();

		return entityManagerFactory;
	}

	/**
	 * Set some basic EclipseLink properties.
	 */
	private static Map<String, Object> getJPAProperties(ClassLoader classLoader) {

		Map<String, Object> properties = new HashMap<>();

		properties.put(TenantContext.TENANT_ID, TenantContext.getCurrentTenant());

		// Disable automatic DB update which is implicit and hard to manage
		// properties.put(DDL_GENERATION, CREATE_OR_EXTEND);
		// properties.put(DDL_GENERATION_MODE, DDL_DATABASE_GENERATION);

		// HANA DB with version like HDB.2.200 is concatenated as HDB22.00 as vendorNameAndVersion in
		// DatabaseSessionImpl.java and in turn the DB platform is incorrectly determined as DB2 in
		// DBPlatformHelper.java since it happens to contain key word DB2. This causes issue when JPA generates platform
		// specific SQL.
		// To overcome this issue without modifying EclipseLink code, we specify the DB metadata explicitly. Then the
		// vendorNameAndVersion is concatenated as HDB.2 and the platform is determined as HANA correctly.
		String vcapServices = System.getenv().get(Constants.VCAP_SERVICES);
		if (StringUtils.containsIgnoreCase(vcapServices, Constants.HANA_DB_DRIVER)) {
			properties.put(SCHEMA_DATABASE_PRODUCT_NAME, Constants.HANA_DB_PRODUCT_NAME);
			properties.put(SCHEMA_DATABASE_MAJOR_VERSION, Constants.HANA_DB_2_MAJOR_VERSION);
		}

		properties.put(CLASSLOADER, classLoader);
		properties.put(LOGGING_LEVEL, "INFO"); // "FINE" provides more details
		properties.put(ALLOW_NATIVE_SQL_QUERIES, "true");// allow native SQL query

		// Do not cache entities locally which causes problems if multiple application instances are used
		properties.put(CACHE_SHARED_DEFAULT, "false");

		// You can also tweak your application performance by configuring your database connection pool.
		// http://www.eclipse.org/eclipselink/documentation/2.4/jpa/extensions/p_connection_pool.htm
		properties.put(CONNECTION_POOL_MAX, 50);

		return properties;
	}
}
