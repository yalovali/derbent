package tech.derbent.bab.policybase.domain;

import tech.derbent.bab.policybase.service.CJsonSerializationService;

/** Base contract for network JSON serialization in BAB policy domain. */
public interface IJsonNetworkSerializable {

	default String toJson() {
		return CJsonSerializationService.toJson(this);
	}
}
