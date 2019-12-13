package com.lifeinide.jsonql.hibernate.search.elastic;

import com.lifeinide.jsonql.core.dto.Page;
import com.lifeinide.jsonql.hibernate.search.FieldSearchStrategy;

import javax.persistence.EntityManager;
import java.util.Map;

/**
 * @author Lukasz Frankowski
 */
public class DefaultHibernateSearchElasticFilterQueryBuilder<E> extends HibernateSearchElasticFilterQueryBuilder<E, Page<E>> {

	public DefaultHibernateSearchElasticFilterQueryBuilder(EntityManager entityManager, Class<E> entityClass, String q) {
		super(entityManager, entityClass, q);
	}

	public DefaultHibernateSearchElasticFilterQueryBuilder(EntityManager entityManager, Class<E> entityClass, String q, Map<String, FieldSearchStrategy> fields) {
		super(entityManager, entityClass, q, fields);
	}
	
}
