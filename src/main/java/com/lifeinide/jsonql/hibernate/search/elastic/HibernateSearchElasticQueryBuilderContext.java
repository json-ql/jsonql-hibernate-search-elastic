package com.lifeinide.jsonql.hibernate.search.elastic;

import com.lifeinide.jsonql.elasticql.node.EQLRoot;
import com.lifeinide.jsonql.elasticql.node.component.EQLBoolComponent;
import com.lifeinide.jsonql.elasticql.node.query.EQLBool;
import com.lifeinide.jsonql.hibernate.search.BaseHibernateSearchQueryBuilderContext;
import com.lifeinide.jsonql.hibernate.search.HibernateSearch;

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

	public HibernateSearchElasticQueryBuilderContext(String query, Class<E> entityClass, HibernateSearch hibernateSearch) {
		super(query, entityClass, hibernateSearch);

		this.eqlBool = EQLBool.of();
		this.eqlRoot = EQLRoot.of().withQuery(EQLBoolComponent.of(this.eqlBool));
	}

	public EQLRoot getEqlRoot() {
		return eqlRoot;
	}

	public EQLBool getEqlBool() {
		return eqlBool;
	}

	public EQLBool getEqlFilterBool() {
		if (eqlFilterBool==null) {
			eqlFilterBool = EQLBool.of();
			eqlBool.withFilter(EQLBoolComponent.of(eqlFilterBool));
		}

		return eqlFilterBool;
	}
}
