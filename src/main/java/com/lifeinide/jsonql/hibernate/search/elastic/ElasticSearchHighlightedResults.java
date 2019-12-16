package com.lifeinide.jsonql.hibernate.search.elastic;

import com.lifeinide.jsonql.core.intr.Pageable;
import com.lifeinide.jsonql.core.intr.Sortable;

/**
 * Highlited results for ElasticSearch query returned by {@link HibernateSearchElasticFilterQueryBuilder#highlight(Pageable, Sortable)}.
 *
 * @author Lukasz Frankowski
 */
public class ElasticSearchHighlightedResults<E> {

	protected E entity;
	protected double score;
	protected String highlight;

	public ElasticSearchHighlightedResults() {
	}

	public ElasticSearchHighlightedResults(E entity, double score, String highlight) {
		this.entity = entity;
		this.score = score;
		this.highlight = highlight;
	}

	public E getEntity() {
		return entity;
	}

	public double getScore() {
		return score;
	}

	public String getHighlight() {
		return highlight;
	}
	
}
