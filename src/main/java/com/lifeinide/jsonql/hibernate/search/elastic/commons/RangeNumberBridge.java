package com.lifeinide.jsonql.hibernate.search.elastic.commons;

import com.lifeinide.jsonql.core.filters.SingleValueQueryFilter;
import com.lifeinide.jsonql.core.filters.ValueRangeQueryFilter;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.builtin.NumberBridge;

/**
 * A {@link FieldBridge} used to store {@link Number} values so that they are searchable using {@link SingleValueQueryFilter} and
 * {@link ValueRangeQueryFilter}.
 *
 * @see HibernateSearch How to use this bridge in searchable entities
 * @author Lukasz Frankowski
 */
public class RangeNumberBridge extends NumberBridge {

	public static final int NUMBER_SIZE = 20;
	public static final String ZEROS_PAD_TEMPLATE = "00000000000000000000";

	protected String padZeros(String s) {
		s = ZEROS_PAD_TEMPLATE + s;
		if (s.length() - NUMBER_SIZE > 0)
			return s.substring(s.length() - NUMBER_SIZE);
		return s;
	}

	@Override
	public String objectToString(Object object) {
		if (object==null)
			return null;

		return padZeros(super.objectToString(object));
	}

	@Override
	public Object stringToObject(String stringValue) {
		if (stringValue==null)
			return null;

		return Long.valueOf(stringValue);
	}
	
}
