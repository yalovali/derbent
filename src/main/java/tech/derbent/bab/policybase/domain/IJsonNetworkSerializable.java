package tech.derbent.bab.policybase.domain;

import tech.derbent.bab.utils.CJsonSerializer;

/** Base contract for network JSON serialization in BAB policy domain. */
public interface IJsonNetworkSerializable {

	default String toJson() {
		return CJsonSerializer.toJson(this);
	}

	default String toPrettyJson() {
		return CJsonSerializer.toPrettyJson(this);
	}
}
