package com.lifeinide.jsonql.hibernate.search.elastic;

import com.lifeinide.jsonql.core.BaseQueryBuilderContext;
import com.lifeinide.jsonql.elasticql.node.EQLRoot;
import com.lifeinide.jsonql.elasticql.node.component.EQLBoolComponent;
import com.lifeinide.jsonql.elasticql.node.query.EQLBool;
import com.lifeinide.jsonql.hibernate.search.HibernateSearch;
import org.hibernate.search.metadata.IndexedTypeDescriptor;

/**
 * @author Lukasz Frankowski
 */
public class HibernateSearchElasticQueryBuilderContext<E> extends BaseQueryBuilderContext {

	protected String query;
	protected Class<E> entityClass;
	protected HibernateSearch hibernateSearch;
	protected IndexedTypeDescriptor indexedTypeDescriptor;

	/** Query root **/
	protected EQLRoot eqlRoot;

	/** Main query bool. We always use query with {@code {query: {bool: {...}}}}. **/
	protected EQLBool eqlBool;

	public HibernateSearchElasticQueryBuilderContext(String query, Class<E> entityClass, HibernateSearch hibernateSearch,
													 IndexedTypeDescriptor indexedTypeDescriptor) {
		this.query = query;
		this.entityClass = entityClass;
		this.hibernateSearch = hibernateSearch;
		this.indexedTypeDescriptor = indexedTypeDescriptor;

		this.eqlBool = EQLBool.of();
		this.eqlRoot = EQLRoot.of().withQuery(EQLBoolComponent.of(this.eqlBool));
	}

	public String getQuery() {
		return query;
	}

	public Class<E> getEntityClass() {
		return entityClass;
	}

	public HibernateSearch getHibernateSearch() {
		return hibernateSearch;
	}

	public IndexedTypeDescriptor getIndexedTypeDescriptor() {
		return indexedTypeDescriptor;
	}

	public EQLRoot getEqlRoot() {
		return eqlRoot;
	}

	public EQLBool getEqlBool() {
		return eqlBool;
	}
	
}
