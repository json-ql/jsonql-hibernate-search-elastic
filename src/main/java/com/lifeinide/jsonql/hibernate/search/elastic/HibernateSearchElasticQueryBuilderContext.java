package com.lifeinide.jsonql.hibernate.search.elastic;

import com.lifeinide.jsonql.elasticql.node.EQLRoot;
import com.lifeinide.jsonql.elasticql.node.component.EQLBoolComponent;
import com.lifeinide.jsonql.elasticql.node.query.EQLBool;
import com.lifeinide.jsonql.hibernate.search.BaseHibernateSearchQueryBuilderContext;
import com.lifeinide.jsonql.hibernate.search.HibernateSearch;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Lukasz Frankowski
 */
public class HibernateSearchElasticQueryBuilderContext<E> extends BaseHibernateSearchQueryBuilderContext<E> {

	/** Query root **/
	protected EQLRoot eqlRoot;

	/** Main query bool. We always use query with {@code {query: {bool: {...}}}}. **/
	protected EQLBool eqlBool;

	/** Filter query bool. We always use query with {@code {filter: [{bool: {...}}]}}. **/
	protected EQLBool eqlFilterBool;

	public HibernateSearchElasticQueryBuilderContext(@Nullable String query, @Nonnull Class<E> entityClass,
													 @Nonnull HibernateSearch hibernateSearch) {
		super(query, entityClass, hibernateSearch);

		this.eqlBool = EQLBool.of();
		this.eqlRoot = EQLRoot.of().withQuery(EQLBoolComponent.of(this.eqlBool));
	}

	@Nonnull public EQLRoot getEqlRoot() {
		return eqlRoot;
	}

	@Nonnull public EQLBool getEqlBool() {
		return eqlBool;
	}

	@Nonnull public EQLBool getEqlFilterBool() {
		if (eqlFilterBool==null) {
			eqlFilterBool = EQLBool.of();
			eqlBool.withFilter(EQLBoolComponent.of(eqlFilterBool));
		}

		return eqlFilterBool;
	}
}
