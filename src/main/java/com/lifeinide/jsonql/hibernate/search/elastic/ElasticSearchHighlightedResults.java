package com.lifeinide.jsonql.hibernate.search.elastic;

import com.lifeinide.jsonql.core.intr.Pageable;
import com.lifeinide.jsonql.core.intr.Sortable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Highlited results for ElasticSearch query returned by {@link HibernateSearchElasticFilterQueryBuilder#highlight(Pageable, Sortable)}.
 *
 * @author Lukasz Frankowski
 */
public class ElasticSearchHighlightedResults<E> {

	protected E entity;
	protected String id;
	protected String type;
	protected double score;
	protected String highlight;

	public ElasticSearchHighlightedResults() {
	}

	public ElasticSearchHighlightedResults(String id, String type, double score, String highlight) {
		this.id = id;
		this.type = type;
		this.score = score;
		this.highlight = highlight;
	}

	/**
	 * We need to do some hack over HS to get the entity id. It might be not possible for some non-standard entity mappings to get it. If
	 * the id is not recoverable in {@link HibernateSearchElasticFilterQueryBuilder#highlight(Pageable, Sortable)}, the entity here will
	 * be {@code null}.
	 */
	@Nullable public E getEntity() {
		return entity;
	}

	public void setEntity(E entity) {
		this.entity = entity;
	}

	public double getScore() {
		return score;
	}

	@Nonnull public String getHighlight() {
		return highlight;
	}

	@Nonnull public String getId() {
		return id;
	}

	@Nonnull public String getType() {
		return type;
	}
}
