package org.iglooproject.jpa.search.dao;

import static org.iglooproject.jpa.property.JpaPropertyIds.HIBERNATE_SEARCH_REINDEX_BATCH_SIZE;
import static org.iglooproject.jpa.property.JpaPropertyIds.HIBERNATE_SEARCH_REINDEX_LOAD_THREADS;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.hibernate.CacheMode;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.search.backend.lucene.impl.LuceneBackendImpl;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.impl.HibernateSearchContextService;
import org.hibernate.search.mapper.orm.jpa.FullTextEntityManager;
import org.iglooproject.jpa.business.generic.model.GenericEntity;
import org.iglooproject.jpa.config.spring.provider.IJpaPropertiesProvider;
import org.iglooproject.jpa.exception.ServiceException;
import org.iglooproject.jpa.hibernate.analyzers.LuceneEmbeddedAnalyzerRegistry;
import org.iglooproject.jpa.util.HibernateUtils;
import org.iglooproject.spring.property.service.IPropertyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Maps;
import com.querydsl.jpa.hibernate.HibernateUtil;

@Repository("hibernateSearchDao")
public class HibernateSearchDaoImpl implements IHibernateSearchDao {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HibernateSearchDaoImpl.class);
	
	@Autowired
	private IPropertyService propertyService;
	
	@Autowired
	private IJpaPropertiesProvider jpaPropertiesProvider;
	
	/**
	 * With Elasticsearch used as backend, provides client-side Lucene analyzers. 
	 */
	@Autowired(required = false)
	private LuceneEmbeddedAnalyzerRegistry luceneEmbeddedAnalyzerRegistry;
	
	@PersistenceContext
	private EntityManager entityManager;

	private Analyzer KEYWORD_ANALYZER = new KeywordAnalyzer();
	
	public HibernateSearchDaoImpl() {
	}
	
	@Override
	public Analyzer getAnalyzer(String analyzerName) {
		if (jpaPropertiesProvider.isHibernateSearchElasticSearchEnabled()) {
			if ("default".equals(analyzerName)) {
				return KEYWORD_ANALYZER;
			} else {
				checkClientSideAnalyzers(true);
				return luceneEmbeddedAnalyzerRegistry.getAnalyzer(analyzerName);
			}
		} else {
			entityManager.getEntityManagerFactory().unwrap(SessionFactoryImplementor.class).getServiceRegistry().getService(HibernateSearchContextService.class).getMapping().createSearchManager(entityManager).search(targetedType)
			ExtendedSearchIntegrator searchIntegrator = Search.getFullTextEntityManager(getEntityManager()).getSearchFactory().unwrap(ExtendedSearchIntegrator.class);
			SearchIntegration integration = searchIntegrator.getIntegration(LuceneEmbeddedIndexManagerType.INSTANCE);
			return integration.getAnalyzerRegistry().getAnalyzerReference(analyzerName).unwrap(LuceneAnalyzerReference.class).getAnalyzer();
		}
	}

	// TODO hibernate 6 - find scoped analyzer for an entity type. Used to perform client-side analysis when needed
	@Override
	public Analyzer getAnalyzer(Class<?> entityType) {
		entityManager.getEntityManagerFactory().unwrap(SessionFactoryImplementor.class).getServiceRegistry()
			.getService(HibernateSearchContextService.class).getMapping().createSearchManager(entityManager)
			.search(entityType).predicate().match().onField("field").matching("value").toPredicate();
		SearchFactory searchFactory = Search.getFullTextEntityManager(getEntityManager()).getSearchFactory();
		ExtendedSearchIntegrator searchIntegrator = searchFactory.unwrap(ExtendedSearchIntegrator.class);
		IndexedTypeIdentifier indexedType = getIndexBoundType(entityType, searchIntegrator);
		
		if (jpaPropertiesProvider.isHibernateSearchElasticSearchEnabled()) {
			ScopedElasticsearchAnalyzerReference scopedAnalyzer = (ScopedElasticsearchAnalyzerReference) searchIntegrator.getAnalyzerReference(indexedType);
			try {
				// these properties are package protected ! Use a bypass !
				ElasticsearchAnalyzerReference globalAnalyzer = (ElasticsearchAnalyzerReference) FieldUtils.getField(ScopedElasticsearchAnalyzerReference.class, "globalAnalyzerReference", true).get(scopedAnalyzer);
				@SuppressWarnings("unchecked")
				Map<String, ElasticsearchAnalyzerReference> analyzers = (Map<String, ElasticsearchAnalyzerReference>) FieldUtils.getField(ScopedElasticsearchAnalyzerReference.class, "scopedAnalyzerReferences", true).get(scopedAnalyzer);
				
				Map<String, LuceneAnalyzerReference> luceneAnalyzers = Maps.newHashMap();
				for (Entry<String, ElasticsearchAnalyzerReference> analyzer : analyzers.entrySet()) {
					luceneAnalyzers.put(analyzer.getKey(), new SimpleLuceneAnalyzerReference(getAnalyzer(analyzer.getValue().getAnalyzerName(null))));
				}
				LuceneAnalyzerReference luceneGlobalAnalyzer = new SimpleLuceneAnalyzerReference(getAnalyzer(globalAnalyzer.getAnalyzerName(null)));
				return new ScopedLuceneAnalyzerReference.Builder(luceneGlobalAnalyzer, luceneAnalyzers).build().getAnalyzer();
			} catch (IllegalAccessException e) {
				throw new RuntimeException("illegal access on scoped analyzer", e);
			}
		} else {
			return Search.getFullTextEntityManager(getEntityManager()).getSearchFactory().unwrap(SearchIntegrator.class).getAnalyzer(indexedType);
		}
	}

	// TODO hibernate 6.0 : used to retrieve analyzer information on a given entity type. Here we find type
	// related information from an entity type. Type inheritance is computed.
	/**
	 * Extracted from ConnectedQueryContextBuilder
	 */
	private IndexedTypeIdentifier getIndexBoundType(Class<?> entityType, ExtendedSearchIntegrator factory) {
		IndexedTypeIdentifier realtype = new PojoIndexedTypeIdentifier( entityType );
		if ( factory.getIndexBinding( realtype ) != null ) {
			return realtype;
		}

		IndexedTypeSet indexedSubTypes = factory.getIndexedTypesPolymorphic( realtype.asTypeSet() );

		if ( !indexedSubTypes.isEmpty() ) {
			return indexedSubTypes.iterator().next();
		}

		return null;
	}

	@Override
	public void reindexAll() throws ServiceException {
		reindexClasses(Object.class);
	}

	// TODO hibernate 6.0 : no search factory -> find a way to list indexed types
	@Override
	public void reindexClasses(Class<?>... classes) throws ServiceException {
		try {
			FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
			
			reindexClasses(fullTextEntityManager, getIndexedRootEntities(fullTextEntityManager.getSearchFactory(),
					classes.length > 0 ? classes : new Class<?>[] { Object.class }));
		} catch (RuntimeException | InterruptedException e) {
			if (e instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			throw new ServiceException(e);
		}
	}

	// TODO hibernate 6.0 : no support for mass reindexation ?
	protected void reindexClasses(FullTextEntityManager fullTextEntityManager, Set<Class<?>> entityClasses)
			throws InterruptedException {
		int batchSize = propertyService.get(HIBERNATE_SEARCH_REINDEX_BATCH_SIZE);
		int loadThreads = propertyService.get(HIBERNATE_SEARCH_REINDEX_LOAD_THREADS);
		
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Targets for indexing job: {}", entityClasses);
		}
		
		for (Class<?> clazz : entityClasses) {
			ProgressMonitor progressMonitor = new ProgressMonitor();
			Thread t = new Thread(progressMonitor);
			LOGGER.info(String.format("Reindexing %1$s.", clazz));
			t.start();
			MassIndexer indexer = fullTextEntityManager.createIndexer(clazz);
			indexer.batchSizeToLoadObjects(batchSize)
					.threadsToLoadObjects(loadThreads)
					.cacheMode(CacheMode.NORMAL)
					.progressMonitor(progressMonitor)
					.startAndWait();
			progressMonitor.stop();
			t.interrupt();
			LOGGER.info(String.format("Reindexing %1$s done.", clazz));
		}
	}

	// TODO hibernate 6.0 : no search factory -> find a way to list indexed types
	@Override
	public Set<Class<?>> getIndexedRootEntities(Class<?>... selection) {
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
		
		Set<Class<?>> indexedEntityClasses = new TreeSet<Class<?>>(new Comparator<Class<?>>() {
			@Override
			public int compare(Class<?> o1, Class<?> o2) {
				// Legacy. French should not be considered as default locale.
				return GenericEntity.STRING_COLLATOR_FRENCH.compare(o1.getSimpleName(), o2.getSimpleName());
			}
		});
		indexedEntityClasses.addAll(getIndexedRootEntities(fullTextEntityManager.getSearchFactory(), selection));
		
		return indexedEntityClasses;
	}

	// TODO hibernate 6.0 : no support for reindex; is HibernateUtils.unwrap(...)
	// different from HibernateHelper.unproxy(...)
	@Override
	public <K extends Serializable & Comparable<K>, E extends GenericEntity<K, ?>> void reindexEntity(E entity) {
		if (entity != null) {
			FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
			fullTextEntityManager.index(HibernateUtils.unwrap(entity));
		}
	}
	
	// TODO hibernate 6.0 : no support for mass reindexation ?
	// TODO hibernate 6.0 : no search factory
	// purpose of SearchFactory was to enumerate all indexed types
	/**
	 * @see MassIndexerImpl#toRootEntities
	 */
	protected Set<Class<?>> getIndexedRootEntities(SearchFactory searchFactory, Class<?>... selection) {
		ExtendedSearchIntegrator searchIntegrator = searchFactory.unwrap(ExtendedSearchIntegrator.class);
		
		Set<Class<?>> entities = new HashSet<Class<?>>();
		
		// first build the "entities" set containing all indexed subtypes of "selection".
		for (Class<?> entityType : selection) {
			IndexedTypeSet targetedClasses = searchIntegrator.getIndexedTypesPolymorphic(IndexedTypeSets.fromClass(entityType));
			if (targetedClasses.isEmpty()) {
				String msg = entityType.getName() + " is not an indexed entity or a subclass of an indexed entity";
				throw new IllegalArgumentException(msg);
			}
			entities.addAll(targetedClasses.toPojosSet());
		}
		
		Set<Class<?>> cleaned = new HashSet<Class<?>>();
		Set<Class<?>> toRemove = new HashSet<Class<?>>();
		
		//now remove all repeated types to avoid duplicate loading by polymorphic query loading
		for (Class<?> type : entities) {
			boolean typeIsOk = true;
			for (Class<?> existing : cleaned) {
				if (existing.isAssignableFrom(type)) {
					typeIsOk = false;
					break;
				}
				if (type.isAssignableFrom(existing)) {
					toRemove.add(existing);
				}
			}
			if (typeIsOk) {
				cleaned.add(type);
			}
		}
		cleaned.removeAll(toRemove);
		
		return cleaned;
	}
	
	protected EntityManager getEntityManager() {
		return entityManager;
	}

	// TODO hibernate 6.0 : no reindex, no way to follow reindex progress
	private static final class ProgressMonitor implements MassIndexerProgressMonitor, Runnable {
		
		private static final Logger LOGGER = LoggerFactory.getLogger(ProgressMonitor.class);
		
		private long documentsAdded;
		private int documentsBuilt;
		private int entitiesLoaded;
		private int totalCount;
		private boolean indexingCompleted;
		private boolean stopped;
		
		@Override
		public void documentsAdded(long increment) {
			this.documentsAdded += increment;
		}
		
		@Override
		public void documentsBuilt(int number) {
			this.documentsBuilt += number;
		}
		
		@Override
		public void entitiesLoaded(int size) {
			this.entitiesLoaded += size;
		}
		
		@Override
		public void addToTotalCount(long count) {
			this.totalCount += count;
		}
		
		@Override
		public void indexingCompleted() {
			this.indexingCompleted = true;
		}
		
		public void stop() {
			this.stopped = true;
			log();
		}
		
		@Override
		public void run() {
			if (LOGGER.isDebugEnabled()) {
				try {
					while (true) {
						log();
						Thread.sleep(15000);
						if (indexingCompleted) {
							LOGGER.debug("Indexing done");
							break;
						}
					}
				} catch (RuntimeException | InterruptedException e) {
					if (e instanceof InterruptedException) {
						Thread.currentThread().interrupt();
					}
					if (!stopped) {
						LOGGER.error("Error ; massindexer monitor stopped", e);
					}
					LOGGER.debug("Massindexer monitor thread interrupted");
				}
			}
		}
		
		private void log() {
			LOGGER.debug(String.format("Indexing %1$d / %2$d (entities loaded: %3$d, documents built: %4$d)",
					documentsAdded, totalCount, entitiesLoaded, documentsBuilt));
		}
	}
	
	// TODO: hibernate 6.0 - no way to force flush to indexes ?
	@Override
	public void flushToIndexes() {
		Search.getFullTextEntityManager(entityManager).flushToIndexes();
	}
	
	@PostConstruct
	private void checkClientSideAnalyzers() {
		checkClientSideAnalyzers(false);
	}

	private boolean checkClientSideAnalyzers(boolean throwException) {
		if (luceneEmbeddedAnalyzerRegistry == null) {
			String message = String.format("No %s available; please add a bean implementation if you want to use client-side analysis with elasticsearch", LuceneEmbeddedAnalyzerRegistry.class.getSimpleName());
			if (throwException) {
				throw new IllegalStateException(message);
			} else {
				if (jpaPropertiesProvider.isHibernateSearchElasticSearchEnabled()) {
					// error if needed (elasticsearch mode)
					LOGGER.error(message);
				} else {
					// debug if not mandatory (lucene mode)
					LOGGER.debug(message);
				}
				return false;
			}
		} else {
			return true;
		}
	}
}
