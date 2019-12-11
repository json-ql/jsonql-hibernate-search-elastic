package com.lifeinide.jsonql.hibernate.search.elastic.commons;

import com.lifeinide.jsonql.hibernate.search.elastic.HibernateSearchFilterQueryBuilder;
import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;

import javax.persistence.EntityManager;

import static org.hibernate.search.util.StringHelper.*;

/**
 * Hibernate search helper.
 *
 * @see HibernateSearchFilterQueryBuilder
 * @author Lukasz Frankowski
 */
public class HibernateSearch {

	/**
	 * A common field for storing text content.
	 * <p>
	 * This kind of field is appropriate to index <strong>case-insensitively all natural-language fields</strong> and should be analyzed
	 * with some analyzer. The indexed entity field has has usually the following definition:
	 * <pre>{@code
	 * @Field(name = HibernateSearch.FIELD_TEXT)
	 * @Analyzer(impl = ... , definition = ...)
	 * protected String myfield;
	 * }</pre>
	 * </p>
	 **/
	public static final String FIELD_TEXT = "text";

	/**
	 * A common field for storing text indentificator, like document numbers {@code FV/2016/12/223412}.
	 * <p>
	 * This kind of field is appropriate to index <strong>case-insensitively all fields tokenized only with whitespaces</strong> and
	 * prevents from tokenizing using usual document number separators like slash or dash. This kind of field should be analyzed
	 * with no analyzer nor tokenizer. The indexed entity field has has usually the following definition:
	 * <pre>{@code
	 * @Field(name = HibernateSearch.FIELD_ID, analyze = Analyze.NO, norms = Norms.NO)
	 * protected String myfield;
	 * }</pre>
	 * </p>
	 **/
	public static final String FIELD_ID = "textid";

	protected EntityManager entityManager;

	public HibernateSearch(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public FullTextEntityManager fullTextEntityManager() {
		return Search.getFullTextEntityManager(entityManager);
	}

	public QueryBuilder queryBuilder(Class entityClass) {
		return Search.getFullTextEntityManager(entityManager).getSearchFactory().buildQueryBuilder().forEntity(entityClass).get();
	}

	public FullTextQuery buildQuery(Query query, Class entityClass) {
		return fullTextEntityManager().createFullTextQuery(query, entityClass);
	}

	public static String makeWild(String s) {
		if (isEmpty(s))
			return s;
		if (s.endsWith("*"))
			return s;
		return s+"*";
	}

}
