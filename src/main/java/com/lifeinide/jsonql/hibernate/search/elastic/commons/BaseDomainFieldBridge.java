package com.lifeinide.jsonql.hibernate.search.elastic.commons;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.StringBridge;

/**
 * A {@link FieldBridge} to reflect entity objects in lucene index. To be implemented by the application.
 * 
 * @param <E> Entity type.
 * @see HibernateSearch How to use this bridge in searchable entities
 * @author Lukasz Frankowski
 */
@SuppressWarnings("unchecked")
public abstract class BaseDomainFieldBridge<E> implements FieldBridge, StringBridge {

	public static final String NULL_ID = "[NULL_ID]";

	/**
	 * Returns the entity ID as {@link String} so that the full text search index can store it and make searchable.
	 * @param entity (not null) entity
	 * @return String representation of entity ID.
	 */
	public abstract String getEntityIdAsString(E entity);

	/**
	 * Checks whether the object is of {@link E} entity type.
	 * @param entity (nullable) entity
	 */
	public abstract boolean isEntity(Object entity);

	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
		E model = (E) value;
		if (model == null)
			luceneOptions.addFieldToDocument(name, NULL_ID, document);
		else
			luceneOptions.addFieldToDocument(name, getEntityIdAsString(model), document);
	}

	@Override
	public String objectToString(Object object) {
		if (isEntity(object))
			return getEntityIdAsString((E) object);
		if (object == null)
			return NULL_ID;
		return object.toString();
	}

}
