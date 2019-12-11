package com.lifeinide.jsonql.hibernate.search.elastic.test;

import com.lifeinide.jsonql.core.test.IJsonQLTestEntity;
import com.lifeinide.jsonql.core.test.JsonQLTestEntityEnum;
import com.lifeinide.jsonql.hibernate.search.HibernateSearch;
import com.lifeinide.jsonql.hibernate.search.bridge.BigDecimalRangeBridge;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A mapping for this entity in ElasticSearch:
 *
 * <pre>{@code
 * {
 *     "dynamic": "strict",
 *     "properties": {
 *         "booleanVal": {
 *             "type": "boolean"
 *         },
 *         "dateVal": {
 *             "type": "date",
 *             "format": "strict_date||yyyyyyyyy-MM-dd"
 *         },
 *         "decimalVal": {
 *             "type": "keyword"
 *         },
 *         "entityVal": {
 *             "type": "keyword"
 *         },
 *         "enumVal": {
 *             "type": "keyword"
 *         },
 *         "id": {
 *             "type": "keyword",
 *             "store": true
 *         },
 *         "longVal": {
 *             "type": "long"
 *         },
 *         "text": {
 *             "type": "text",
 *             "analyzer": "standard"
 *         },
 *         "textid": {
 *             "type": "keyword"
 *         }
 *     }
 * }}</pre>
 *
 * @author Lukasz Frankowski
 */
@Entity
@Indexed(index = "hibernatesearchentity")
public class HibernateSearchEntity implements IJsonQLTestEntity<Long, HibernateSearchAssociatedEntity> {

	@Id private Long id;

	@Field(name = HibernateSearch.FIELD_TEXT)
	@Analyzer(definition = "standard")
	protected String q = HibernateSearchQueryBuilderTest.SEARCHABLE_STRING;

	@Field(name = HibernateSearch.FIELD_ID, analyze = Analyze.NO, norms = Norms.NO)
	protected String stringVal;

	@Field(analyze = Analyze.NO, norms = Norms.NO)
	protected boolean booleanVal;

	@Field(analyze = Analyze.NO, norms = Norms.NO)
	protected Long longVal;

	@Field(analyze = Analyze.NO, norms = Norms.NO)
	@FieldBridge(impl = BigDecimalRangeBridge.class) // TODOLF test with field type overwritten with FieldType.LONG + MetadataProvidingFieldBridge
	protected BigDecimal decimalVal;

	@Field(analyze = Analyze.NO, norms = Norms.NO)
	protected LocalDate dateVal;

	@Enumerated(EnumType.STRING)
	@Field(analyze = Analyze.NO, norms = Norms.NO)
	protected JsonQLTestEntityEnum enumVal;

	@ManyToOne
	@Field(analyze = Analyze.NO, norms = Norms.NO)
	@FieldBridge(impl = DomainFieldBridge.class)
	protected HibernateSearchAssociatedEntity entityVal;

	public HibernateSearchEntity() {
	}

	public HibernateSearchEntity(Long id) {
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

	@Override
	public String getStringVal() {
		return stringVal;
	}

	@Override
	public void setStringVal(String stringVal) {
		this.stringVal = stringVal;
	}

	@Override
	public boolean isBooleanVal() {
		return booleanVal;
	}

	@Override
	public void setBooleanVal(boolean booleanVal) {
		this.booleanVal = booleanVal;
	}

	@Override
	public Long getLongVal() {
		return longVal;
	}

	@Override
	public void setLongVal(Long longVal) {
		this.longVal = longVal;
	}

	@Override
	public BigDecimal getDecimalVal() {
		return decimalVal;
	}

	@Override
	public void setDecimalVal(BigDecimal decimalVal) {
		this.decimalVal = decimalVal;
	}

	@Override
	public LocalDate getDateVal() {
		return dateVal;
	}

	@Override
	public void setDateVal(LocalDate dateVal) {
		this.dateVal = dateVal;
	}

	@Override
	public JsonQLTestEntityEnum getEnumVal() {
		return enumVal;
	}

	@Override
	public void setEnumVal(JsonQLTestEntityEnum enumVal) {
		this.enumVal = enumVal;
	}

	@Override
	public HibernateSearchAssociatedEntity getEntityVal() {
		return entityVal;
	}

	@Override
	public void setEntityVal(HibernateSearchAssociatedEntity entityVal) {
		this.entityVal = entityVal;
	}
	
}
