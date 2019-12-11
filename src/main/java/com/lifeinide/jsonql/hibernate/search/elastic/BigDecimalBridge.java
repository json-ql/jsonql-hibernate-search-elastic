package com.lifeinide.jsonql.hibernate.search.elastic;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.MetadataProvidingFieldBridge;
import org.hibernate.search.bridge.spi.FieldMetadataBuilder;
import org.hibernate.search.bridge.spi.FieldType;

import java.math.BigDecimal;

/**
 * Standard {@link BigDecimal} field is mapped in ElasticSearch as a {@code keyword} field. Use this bridge to map it with {@link double}
 * type instead.
 *
 * @author Lukasz Frankowski
 */
public class BigDecimalBridge extends org.hibernate.search.bridge.builtin.BigDecimalBridge implements MetadataProvidingFieldBridge {

	@Override
	public void configureFieldMetadata(String name, FieldMetadataBuilder builder) {
		builder.field(name, FieldType.DOUBLE);
	}

	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
		luceneOptions.addFieldToDocument(name, objectToString(value), document);
	}
}
