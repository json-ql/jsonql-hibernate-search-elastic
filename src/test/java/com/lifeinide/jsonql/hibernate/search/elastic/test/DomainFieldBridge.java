package com.lifeinide.jsonql.hibernate.search.elastic.test;

import com.lifeinide.jsonql.core.test.IJsonQLBaseTestEntity;
import com.lifeinide.jsonql.hibernate.search.bridge.BaseDomainFieldBridge;

/**
 * @author Lukasz Frankowski
 */
public class DomainFieldBridge extends BaseDomainFieldBridge<IJsonQLBaseTestEntity<Long>> {

	@Override
	public String getEntityIdAsString(IJsonQLBaseTestEntity<Long> entity) {
		return String.valueOf(entity.getId());
	}

	@Override
	public boolean isEntity(Object entity) {
		return entity instanceof IJsonQLBaseTestEntity;
	}
	
}
