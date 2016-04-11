package fr.openwide.core.wicket.more.config.spring;

import org.apache.wicket.protocol.http.WebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import fr.openwide.core.jpa.exception.ServiceException;
import fr.openwide.core.jpa.more.rendering.service.IRendererService;
import fr.openwide.core.wicket.more.link.service.DefaultLinkParameterConversionService;
import fr.openwide.core.wicket.more.link.service.ILinkParameterConversionService;
import fr.openwide.core.wicket.more.notification.service.IHtmlNotificationCssService;
import fr.openwide.core.wicket.more.notification.service.IWicketContextExecutor;
import fr.openwide.core.wicket.more.notification.service.PhlocCssHtmlNotificationCssServiceImpl;
import fr.openwide.core.wicket.more.notification.service.WicketContextExecutorImpl;
import fr.openwide.core.wicket.more.rendering.service.RendererServiceImpl;

@Configuration
@Import({
	WicketMoreServiceConfig.class,
	WicketMoreApplicationPropertyRegistryConfig.class
})
public abstract class AbstractWebappConfig {

	@Bean
	public abstract WebApplication application();
	
	@Bean
	/* Use a proxy to fix a circular dependency.
	 * There's no real notion of scope here, since the bean is a singleton: we just want it to be proxyfied so that
	 * the circular dependency is broken.
	 */
	@Scope(proxyMode = ScopedProxyMode.INTERFACES)
	public IWicketContextExecutor wicketContextExecutor(WebApplication defaultApplication) {
		return new WicketContextExecutorImpl(defaultApplication);
	}
	
	@Bean
	public IRendererService rendererService(IWicketContextExecutor wicketContextExecutor) {
		return new RendererServiceImpl(wicketContextExecutor);
	}
	
	@Bean
	public ILinkParameterConversionService linkParameterConversionService() {
		return new DefaultLinkParameterConversionService();
	}
	
	@Bean
	public IHtmlNotificationCssService htmlNotificationCssService() throws ServiceException {
		return new PhlocCssHtmlNotificationCssServiceImpl();
	}

}
