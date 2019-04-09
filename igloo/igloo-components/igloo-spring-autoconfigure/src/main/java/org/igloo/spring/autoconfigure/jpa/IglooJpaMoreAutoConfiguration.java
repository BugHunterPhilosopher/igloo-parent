package org.igloo.spring.autoconfigure.jpa;


import org.iglooproject.jpa.config.spring.JpaConfigUtils;
import org.iglooproject.jpa.config.spring.provider.IJpaConfigurationProvider;
import org.iglooproject.jpa.config.spring.provider.JpaPackageScanProvider;
import org.iglooproject.jpa.more.business.CoreJpaMoreBusinessPackage;
import org.iglooproject.jpa.more.business.search.query.HibernateSearchLuceneQueryFactoryImpl;
import org.iglooproject.jpa.more.business.search.query.IHibernateSearchLuceneQueryFactory;
import org.iglooproject.jpa.more.config.spring.JpaMoreApplicationPropertyRegistryConfig;
import org.iglooproject.jpa.more.config.spring.JpaMoreInfinispanConfig;
import org.iglooproject.jpa.more.util.CoreJpaMoreUtilPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Import({
	JpaMoreApplicationPropertyRegistryConfig.class,
	JpaMoreInfinispanConfig.class
})
@ComponentScan(basePackageClasses = { CoreJpaMoreBusinessPackage.class, CoreJpaMoreUtilPackage.class })
@Configuration
@AutoConfigureAfter({ IglooJpaAutoConfiguration.class })
public class IglooJpaMoreAutoConfiguration {
	
	@Autowired
	protected IJpaConfigurationProvider jpaConfigurationProvider;

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		return JpaConfigUtils.entityManagerFactory(jpaConfigurationProvider);
	}

	@Bean
	public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
		return new TransactionTemplate(transactionManager);
	}

	@Bean
	public JpaPackageScanProvider jpaMorePackageScanProvider() {
		return new JpaPackageScanProvider(CoreJpaMoreBusinessPackage.class.getPackage());
	}

	@Bean
	@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public IHibernateSearchLuceneQueryFactory hibernateSearchLuceneQueryFactory() {
		return new HibernateSearchLuceneQueryFactoryImpl();
	}

}