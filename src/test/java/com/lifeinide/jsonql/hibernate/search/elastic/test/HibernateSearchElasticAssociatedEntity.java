package com.lifeinide.jsonql.hibernate.search.elastic.test;

import com.lifeinide.jsonql.core.test.IJsonQLBaseTestEntity;
import com.lifeinide.jsonql.hibernate.search.HibernateSearch;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author Lukasz Frankowski
 */
@Entity
@Indexed(index = "hibernatesearchelasticassociatedentity")
public class HibernateSearchElasticAssociatedEntity implements IJsonQLBaseTestEntity<Long> {

	@Id Long id;

	@Field(name = HibernateSearch.FIELD_TEXT)
	@Analyzer(definition = "standard")
	protected String q = HibernateSearchElasticQueryBuilderTest.SEARCHABLE_STRING;

	public HibernateSearchElasticAssociatedEntity() {
	}

	public HibernateSearchElasticAssociatedEntity(Long id) {
		this.id = id;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getQ() {
		return q;
	}

	public void setQ(String q) {
		this.q = q;
	}
	
}
