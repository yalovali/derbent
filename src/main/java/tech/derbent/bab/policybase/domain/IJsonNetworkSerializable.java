package tech.derbent.bab.policybase.domain;

import tech.derbent.bab.utils.CJsonSerializer;
import tech.derbent.bab.utils.CJsonSerializer.EJsonScenario;

/** Base contract for network JSON serialization in BAB policy domain. */
public interface IJsonNetworkSerializable {

	default String toJson(final EJsonScenario scenario) {
		return CJsonSerializer.toJson(this, scenario);
	}
}
