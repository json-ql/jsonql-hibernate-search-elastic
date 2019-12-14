package com.lifeinide.jsonql.hibernate.search.elastic.bridge;

import com.lifeinide.jsonql.hibernate.search.bridge.BaseDomainFieldBridge;

/**
 * {@link BaseDomainFieldBridge} for ElasticSearch backend.
 *
 * @author Lukasz Frankowski
 */
public abstract class BaseElasticDomainFieldBridge<E> extends BaseDomainFieldBridge<E> {

	@Override
	protected boolean supportsNulls() {
		return true;
	}
	
}
