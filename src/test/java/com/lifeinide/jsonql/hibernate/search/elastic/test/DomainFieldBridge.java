package com.lifeinide.jsonql.hibernate.search.elastic.test;

import com.lifeinide.jsonql.core.test.IJsonQLBaseTestEntity;
import com.lifeinide.jsonql.hibernate.search.elastic.bridge.BaseElasticDomainFieldBridge;

/**
 * @author Lukasz Frankowski
 */
public class DomainFieldBridge extends BaseElasticDomainFieldBridge<IJsonQLBaseTestEntity<Long>> {

	@Override
	public String getEntityIdAsString(IJsonQLBaseTestEntity<Long> entity) {
		return String.valueOf(entity.getId());
	}

	@Override
	public boolean isEntity(Object entity) {
		return entity instanceof IJsonQLBaseTestEntity;
	}

}
