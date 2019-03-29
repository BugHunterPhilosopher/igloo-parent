package org.igloo.spring.autoconfigure.bootstrap;

import org.igloo.spring.autoconfigure.wicket.IglooWicketAutoConfiguration;
import org.iglooproject.wicket.bootstrap4.application.WicketBootstrapModule;
import org.iglooproject.wicket.bootstrap4.config.spring.WicketBootstrapServiceConfig;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnClass(value = WicketBootstrapModule.class)
@AutoConfigureAfter({ IglooWicketAutoConfiguration.class })
@Import({
	WicketBootstrapServiceConfig.class,
})
public class IglooBootstrap4AutoConfiguration {

}
