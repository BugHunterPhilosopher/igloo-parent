package org.iglooproject.showcase.web.application.config.spring;

import org.apache.wicket.protocol.http.WebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.iglooproject.showcase.core.config.spring.ShowcaseCoreConfig;
import org.iglooproject.showcase.web.ShowcaseWebPackage;
import org.iglooproject.showcase.web.application.ShowcaseApplication;
import org.iglooproject.wicket.more.config.spring.AbstractWebappConfig;

@Configuration
@Import({
	ShowcaseCoreConfig.class,
	ShowcaseWebappSecurityConfig.class,
	ShowcaseWebappApplicationPropertyRegistryConfig.class,
	ShowcaseWebappJQPlotConfig.class
})
@ComponentScan(
		basePackageClasses = {
				ShowcaseWebPackage.class
		},
		excludeFilters = @Filter(Configuration.class)
)
public class ShowcaseWebappConfig extends AbstractWebappConfig {

	@Override
	@Bean(name = { "showcaseApplication", "application" })
	public WebApplication application() {
		return new ShowcaseApplication();
	}

}