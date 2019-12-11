package com.lifeinide.jsonql.hibernate.search.elastic.test;

import com.lifeinide.jsonql.core.test.IJsonQLBaseTestEntity;
import com.lifeinide.jsonql.hibernate.search.elastic.commons.HibernateSearch;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author Lukasz Frankowski
 */
@Entity
@Indexed(index = "hibernatesearchassociatedentity")
public class HibernateSearchAssociatedEntity implements IJsonQLBaseTestEntity<Long> {

	@Id Long id;

	@Field(name = HibernateSearch.FIELD_TEXT)
	@Analyzer(definition = "standard")
	protected String q = HibernateSearchQueryBuilderTest.SEARCHABLE_STRING;

	public HibernateSearchAssociatedEntity() {
	}

	public HibernateSearchAssociatedEntity(Long id) {
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
