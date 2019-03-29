package org.igloo.spring.autoconfigure.property;

import org.iglooproject.spring.config.CorePropertyPlaceholderConfigurer;
import org.iglooproject.spring.config.spring.IPropertyRegistryConfig;
import org.iglooproject.spring.property.dao.IImmutablePropertyDao;
import org.iglooproject.spring.property.dao.ImmutablePropertyDaoImpl;
import org.iglooproject.spring.property.service.IPropertyRegistry;
import org.iglooproject.spring.property.service.IPropertyService;
import org.iglooproject.spring.property.service.PropertyServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IglooPropertyAutoConfiguration {

	private static final Logger LOGGER = LoggerFactory.getLogger(IglooPropertyAutoConfiguration.class);

	@ConditionalOnMissingBean
	@Bean
	public CorePropertyPlaceholderConfigurer corePropertyPlaceholderConfigurer() {
		return new CorePropertyPlaceholderConfigurer();
	}

	@Bean
	public IImmutablePropertyDao immutablePropertyDao() {
		return new ImmutablePropertyDaoImpl();
	}

	@Bean
	public IPropertyService propertyService() {
		return new PropertyServiceImpl();
	}

	@ConditionalOnMissingBean
	@Bean
	public IPropertyRegistryConfig propertyRegistryConfig() {
		return new IPropertyRegistryConfig() {
			
			@Override
			public void register(IPropertyRegistry registry) {
				LOGGER.warn("Please define at least one {} bean. No property registered.",
						IPropertyRegistry.class.getSimpleName());
			}
		};
	}

}
