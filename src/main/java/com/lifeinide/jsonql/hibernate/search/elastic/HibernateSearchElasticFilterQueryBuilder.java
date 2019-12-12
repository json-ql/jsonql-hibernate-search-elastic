package com.lifeinide.jsonql.hibernate.search.elastic;

import com.lifeinide.jsonql.core.BaseFilterQueryBuilder;
import com.lifeinide.jsonql.core.BaseQueryBuilderContext;
import com.lifeinide.jsonql.core.dto.Page;
import com.lifeinide.jsonql.core.filters.*;
import com.lifeinide.jsonql.core.intr.FilterQueryBuilder;
import com.lifeinide.jsonql.core.intr.Pageable;
import com.lifeinide.jsonql.core.intr.QueryFilter;
import com.lifeinide.jsonql.core.intr.Sortable;
import com.lifeinide.jsonql.hibernate.search.HibernateSearch;
import com.lifeinide.jsonql.hibernate.search.HibernateSearchFilterQueryBuilder;
import org.hibernate.search.jpa.FullTextQuery;

/**
 * Implementation of {@link FilterQueryBuilder} for Hibernate Search using ElasticSearch service.
 *
 * <h2>Searchable fields implementation</h2>
 *
 * {@link HibernateSearchElasticFilterQueryBuilder} can search entities of given type for a text contained in searchable fields with
 * additional filtering support by custom fields. By default we support following types of searchable fields in entities
 * (this behavior is configurable using one of constructors, though).
 *
 * <p>
 * {@link HibernateSearch#FIELD_TEXT} which is appropriate to index <strong>case-insensitively all natural-language fields</strong> and
 * should be analyzed with some analyzer. The indexed entity field has has usually the following definition:
 *
 * <pre>{@code
 * @Field(name = HibernateSearch.FIELD_TEXT)
 * @Analyzer(definition = "standard")
 * protected String myfield;
 * }</pre>
 *
 * Note, that in the example above we use {@code standard} analyzer from ElasticSearch, but there are of course other options.
 * Please see them <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/analysis-analyzers.html">here</a>.
 * </p>
 *
 * <p>
 * {@link HibernateSearch#FIELD_ID} which is appropriate to index <strong>case-insensitively all fields tokenized only with
 * whitespaces</strong> and prevents from tokenizing using usual document number separators like slash or dash. So, this kind of field
 * should be analyzed with appropriate analyzer. The indexed entity field has has usually the following definition:
 * <pre>{@code
 * @Field(name = HibernateSearch.FIELD_ID)
 * @Analyzer(definition = "keyword")
 * protected String myfield;
 * }</pre>
 *
 * Note, that in the example above we use {@code keyword} analyzer from ElasticSearch, which is almost perfect for searching by
 * text ids. However this analyzer has a one flaw, which is the lack of {@code lowercase} filter. If you want to apply such a filter,
 * you'd need to create your own custom keyword analyzer in ElasticSearch, as described
 * <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/analysis-keyword-analyzer.html">here</a>.
 * </p>
 *
 * @see HibernateSearchFilterQueryBuilder More information about Hibernate Search-related filtering.
 * @author Lukasz Frankowski
 */
public class HibernateSearchElasticFilterQueryBuilder<E, P extends Page<E>>
extends BaseFilterQueryBuilder<E, P, FullTextQuery, BaseQueryBuilderContext, HibernateSearchElasticFilterQueryBuilder<E, P>> {

	@Override
	public BaseQueryBuilderContext context() {
		// TODOLF impl HibernateSearchFilterQueryBuilder.context
		return null;
	}

	@Override
	public HibernateSearchElasticFilterQueryBuilder<E, P> add(String field, DateRangeQueryFilter filter) {
		// TODOLF impl HibernateSearchFilterQueryBuilder.add
		return null;
	}

	@Override
	public HibernateSearchElasticFilterQueryBuilder<E, P> add(String field, EntityQueryFilter<?> filter) {
		// TODOLF impl HibernateSearchFilterQueryBuilder.add
		return null;
	}

	@Override
	public HibernateSearchElasticFilterQueryBuilder<E, P> add(String field, ListQueryFilter<? extends QueryFilter> filter) {
		// TODOLF impl HibernateSearchFilterQueryBuilder.add
		return null;
	}

	@Override
	public HibernateSearchElasticFilterQueryBuilder<E, P> add(String field, SingleValueQueryFilter<?> filter) {
		// TODOLF impl HibernateSearchFilterQueryBuilder.add
		return null;
	}

	@Override
	public HibernateSearchElasticFilterQueryBuilder<E, P> add(String field, ValueRangeQueryFilter<? extends Number> filter) {
		// TODOLF impl HibernateSearchFilterQueryBuilder.add
		return null;
	}

	@Override
	public FullTextQuery build() {
		// TODOLF impl HibernateSearchFilterQueryBuilder.build
		return null;
	}

	@Override
	public P list(Pageable pageable, Sortable<?> sortable) {
		// TODOLF impl HibernateSearchFilterQueryBuilder.list
		return null;
	}

}
