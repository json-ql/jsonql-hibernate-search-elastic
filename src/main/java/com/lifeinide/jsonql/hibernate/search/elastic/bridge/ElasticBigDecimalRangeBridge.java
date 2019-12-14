package com.lifeinide.jsonql.hibernate.search.elastic.bridge;

import com.lifeinide.jsonql.core.filters.SingleValueQueryFilter;
import com.lifeinide.jsonql.core.filters.ValueRangeQueryFilter;
import com.lifeinide.jsonql.hibernate.search.elastic.HibernateSearchElasticFilterQueryBuilder;
import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.MetadataProvidingFieldBridge;
import org.hibernate.search.bridge.builtin.NumberBridge;
import org.hibernate.search.bridge.spi.FieldMetadataBuilder;
import org.hibernate.search.bridge.spi.FieldType;

import java.math.BigDecimal;

/**
 * A {@link FieldBridge} used to store {@link BigDecimal} values so that they are searchable using {@link SingleValueQueryFilter} and
 * {@link ValueRangeQueryFilter}.
 *
 * @see HibernateSearchElasticFilterQueryBuilder How to use this bridge in searchable entities
 * @author Lukasz Frankowski
 */
public class ElasticBigDecimalRangeBridge extends NumberBridge implements MetadataProvidingFieldBridge {

	@Override
	public Object stringToObject(String stringValue) {
		return new BigDecimal(stringValue);
	}

	@Override
	public void configureFieldMetadata(String name, FieldMetadataBuilder builder) {
		builder.field(name, FieldType.DOUBLE);
	}

	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
		luceneOptions.addFieldToDocument(name, objectToString(value), document);
	}
}
