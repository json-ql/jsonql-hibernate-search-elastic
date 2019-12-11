package com.lifeinide.jsonql.hibernate.search.elastic;

import com.lifeinide.jsonql.core.BaseFilterQueryBuilder;
import com.lifeinide.jsonql.core.BaseQueryBuilderContext;
import com.lifeinide.jsonql.core.dto.Page;
import com.lifeinide.jsonql.core.filters.*;
import com.lifeinide.jsonql.core.intr.Pageable;
import com.lifeinide.jsonql.core.intr.QueryFilter;
import com.lifeinide.jsonql.core.intr.Sortable;
import org.hibernate.search.jpa.FullTextQuery;

/**
 * TODOLF docs
 *
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
