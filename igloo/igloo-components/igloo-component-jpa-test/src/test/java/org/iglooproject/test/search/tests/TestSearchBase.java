package org.iglooproject.test.search.tests;

import java.util.Map;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.lucene.analysis.Analyzer;
import org.assertj.core.api.Assertions;
import org.hibernate.search.analyzer.impl.ScopedLuceneAnalyzer;
import org.iglooproject.jpa.exception.SecurityServiceException;
import org.iglooproject.jpa.exception.ServiceException;
import org.iglooproject.jpa.search.dao.IHibernateSearchDao;
import org.iglooproject.jpa.util.EntityManagerUtils;
import org.iglooproject.test.search.model.Searchable;
import org.iglooproject.test.search.model.SearchableBinding;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TestSearchBase extends AbstractJpaSearchTestCase {
	
	private SearchableBinding searchableBinding = new SearchableBinding();

	@Autowired
	private EntityManagerUtils entityManagerUtils;

	@Autowired
	private IHibernateSearchDao hibernateSearchDao;

	/**
	 * Test {@link IHibernateSearchDao#getAnalyzer(Class)} behavior. This method is a hack as hibernate-search is
	 * designed to perform analysis late and based on metadata, but we need sometime to perform it manually
	 * (client-side analysis) and manually (string manipulation before query writing).
	 * 
	 * Example: manually split and analyze each token of an autocomplete query.
	 */
	@Test
	public void base() {
		Searchable searchable = new Searchable();
		entityManagerUtils.getCurrentEntityManager().persist(searchable);
		Analyzer analyzer = hibernateSearchDao.getAnalyzer(Searchable.class);
		Assertions.assertThat(analyzer).isNotNull();
	}

	/**
	 * Test {@link IHibernateSearchDao#getAnalyzer(Class)} behavior; especially, we need to ensure that we can
	 * do consistent client-side string analysis when we use elasticsearch backend (i.e., an actual Lucene
	 * implementation is provided in place of an Elasticsearch reference).
	 */
	@Test
	public void analyzer() throws IllegalArgumentException, IllegalAccessException {
		Searchable searchable = new Searchable();
		entityManagerUtils.getCurrentEntityManager().persist(searchable);
		Analyzer analyzer = hibernateSearchDao.getAnalyzer(Searchable.class);
		Assertions.assertThat(analyzer).isInstanceOf(ScopedLuceneAnalyzer.class);
		ScopedLuceneAnalyzer scopedAnalyzer = (ScopedLuceneAnalyzer) analyzer;
		Analyzer multipleIndexesAnalyzer = getFieldAnalyzer(scopedAnalyzer, searchableBinding.multipleIndexes().getPath());
		Analyzer multipleIndexesSortAnalyzer = getFieldAnalyzer(scopedAnalyzer, Searchable.MULTIPLE_INDEXES_SORT_FIELD_NAME);
		Analyzer notIndexedAnalyzer = getFieldAnalyzer(scopedAnalyzer, searchableBinding.notIndexed().getPath());
		Analyzer defaultAnalyzer = getFieldAnalyzer(scopedAnalyzer, "any");
		Analyzer anyOtherAnalyzer = getFieldAnalyzer(scopedAnalyzer, "anyOther");
		
		Assertions.assertThat(multipleIndexesAnalyzer).isNotEqualTo(defaultAnalyzer);
		// even if there is no difference between field, analyzer is not the same (hibernate-search implementation)
		Assertions.assertThat(multipleIndexesSortAnalyzer).isNotEqualTo(defaultAnalyzer).isNotEqualTo(multipleIndexesAnalyzer);
		
		// fallback for any not known or not indexed field on default entity analyzer
		Assertions.assertThat(defaultAnalyzer).isEqualTo(anyOtherAnalyzer).isEqualTo(notIndexedAnalyzer);
	}
	
	private Analyzer getFieldAnalyzer(Analyzer analyzer, String fieldName) throws IllegalArgumentException, IllegalAccessException {
		@SuppressWarnings("unchecked")
		Map<String, Analyzer> analyzers = (Map<String, Analyzer>) FieldUtils.getField(ScopedLuceneAnalyzer.class, "scopedAnalyzers", true).get(analyzer);
		Analyzer globalAnalyzer = (Analyzer) FieldUtils.getField(ScopedLuceneAnalyzer.class, "globalAnalyzer", true).get(analyzer);
		return analyzers.getOrDefault(fieldName, globalAnalyzer);
	}
	
	public void analyzeKeyword() {
		
	}

	@Override
	protected void cleanAll() throws ServiceException, SecurityServiceException {
		// no-op
	}

}