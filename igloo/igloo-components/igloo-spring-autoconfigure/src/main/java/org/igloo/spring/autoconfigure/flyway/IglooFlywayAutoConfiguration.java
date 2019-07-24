package org.igloo.spring.autoconfigure.flyway;


import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.flywaydb.core.Flyway;
import org.igloo.spring.autoconfigure.property.IglooPropertyAutoConfiguration;
import org.iglooproject.config.bootstrap.spring.annotations.IglooPropertySourcePriority;
import org.iglooproject.jpa.config.spring.FlywayPropertyRegistryConfig;
import org.iglooproject.jpa.more.config.util.FlywayConfiguration;
import org.iglooproject.jpa.more.config.util.FlywaySpring;
import org.iglooproject.jpa.property.FlywayPropertyIds;
import org.iglooproject.spring.property.service.IPropertyService;
import org.iglooproject.test.config.spring.ConfigurationPropertiesUrlConstants;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import com.google.common.collect.Maps;

@Configuration
@ConditionalOnClass(Flyway.class)
@Import({ FlywayPropertyRegistryConfig.class })
@AutoConfigureAfter(IglooPropertyAutoConfiguration.class)
@PropertySource(
	name = IglooPropertySourcePriority.COMPONENT,
	value = ConfigurationPropertiesUrlConstants.FLYWAY_COMMON
)
public class IglooFlywayAutoConfiguration {

	@Bean(initMethod = "migrate", value = { "flyway", "databaseInitialization" })
	public Flyway flyway(DataSource dataSource, FlywayConfiguration flywayConfiguration,
			IPropertyService propertyService, ConfigurableApplicationContext applicationContext) {
		FlywaySpring flyway = new FlywaySpring();
		flyway.setApplicationContext(applicationContext);
		flyway.setDataSource(dataSource);
		flyway.setSchemas(flywayConfiguration.getSchemas());
		flyway.setTable(flywayConfiguration.getTable());
		flyway.setLocations(StringUtils.split(flywayConfiguration.getLocations(), ","));
		flyway.setBaselineOnMigrate(true);
		// difficult to handle this case for the moment; we ignore mismatching checksums
		// TODO allow developers to handle mismatches during their tests.
		flyway.setValidateOnMigrate(false);
		
		Map<String, String> placeholders = Maps.newHashMap();
		for (String property : propertyService.get(FlywayPropertyIds.FLYWAY_PLACEHOLDERS_PROPERTIES)) {
			placeholders.put(property, propertyService.get(FlywayPropertyIds.property(property)));
		}
		flyway.setPlaceholderReplacement(true);
		flyway.setPlaceholders(placeholders);
		
		return flyway;
	}

	@Bean
	public FlywayConfiguration flywayConfiguration() {
		return new FlywayConfiguration();
	}

}