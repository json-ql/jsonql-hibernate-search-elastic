package com.lifeinide.jsonql.hibernate.search.elastic.commons;

import com.lifeinide.jsonql.hibernate.search.elastic.HibernateSearchFilterQueryBuilder;
import org.apache.lucene.search.Query;
import org.hibernate.search.query.dsl.QueryBuilder;

/**
 * Represents the way the field is searched with {@link HibernateSearchFilterQueryBuilder}.
 *
 * @author Lukasz Frankowski
 */
public enum FieldSearchType {

	/** Does {@link org.apache.lucene.search.PhraseQuery} **/
	PHRASE {

		@Override
		public Query createQuery(QueryBuilder queryBuilder, String field, String query) {
			return queryBuilder
				.phrase()
				.onField(field)
				.sentence(query)
				.createQuery();
		}

	},

	/** Does {@link org.apache.lucene.search.TermQuery} with {@link org.hibernate.search.query.dsl.WildcardContext} **/
	WILDCARD_TERM {

		@Override
		public Query createQuery(QueryBuilder queryBuilder, String field, String query) {
			return queryBuilder
				.keyword()
				.wildcard()
				.onField(field)
				.matching(HibernateSearch.makeWild(query))
				.createQuery();
		}

	};

   public abstract Query createQuery(QueryBuilder queryBuilder, String field, String query);

}
