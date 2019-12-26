package org.iglooproject.jpa.search.util;

public final class HibernateSearchAnalyzer {
	
	public static final String TEXT = "text";
	
	public static final String TEXT_STEMMING = "textStemming";
	
	/**
	 * @deprecated Use {@link org.hibernate.search.annotations.Normalizer} instead.
	 */
	@Deprecated
	public static final String TEXT_SORT = "textSort";
	
	public static final String KEYWORD = "keyword";
	
	public static final String KEYWORD_CLEAN = "keywordClean";
	
	private HibernateSearchAnalyzer() {
	}

}
