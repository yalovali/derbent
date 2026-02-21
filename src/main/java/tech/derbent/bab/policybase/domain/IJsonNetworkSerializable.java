package tech.derbent.bab.policybase.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import tech.derbent.bab.utils.CJsonSerializer;
import tech.derbent.bab.utils.CJsonSerializer.EJsonScenario;

/** Base contract for network JSON serialization in BAB policy domain. */
public interface IJsonNetworkSerializable {

	@SuppressWarnings ("unused")
	default Map<String, Set<String>> getExcludedFieldMapForScenario(final EJsonScenario scenario) {
		return Map.of();
	}

	default Set<String> getExcludedFieldNames(final Class<?> ownerClass, final EJsonScenario scenario) {
		if (ownerClass == null || scenario == null) {
			return Set.of();
		}
		final Map<String, Set<String>> excludedFieldMap = getExcludedFieldMapForScenario(scenario);
		if (excludedFieldMap == null || excludedFieldMap.isEmpty()) {
			return Set.of();
		}
		final Set<String> excludedFields = excludedFieldMap.get(ownerClass.getSimpleName());
		return excludedFields != null ? excludedFields : Set.of();
	}

	default Map<String, Set<String>> getScenarioExcludedFieldMap(final EJsonScenario scenario,
			final Map<String, Set<String>> babConfigurationMap, final Map<String, Set<String>> babPolicyMap) {
		if (scenario == null) {
			return Map.of();
		}
		return switch (scenario) {
		case JSONSENARIO_BABCONFIGURATION -> babConfigurationMap != null ? babConfigurationMap : Map.of();
		case JSONSENARIO_BABPOLICY -> babPolicyMap != null ? babPolicyMap : Map.of();
		};
	}

	default Map<String, Set<String>> mergeExcludedFieldMaps(final Map<String, Set<String>> inheritedMap,
			final Map<String, Set<String>> currentMap) {
		final Map<String, Set<String>> merged = new HashMap<>();
		if (inheritedMap != null && !inheritedMap.isEmpty()) {
			merged.putAll(inheritedMap);
		}
		if (currentMap != null && !currentMap.isEmpty()) {
			merged.putAll(currentMap);
		}
		return merged.isEmpty() ? Map.of() : Map.copyOf(merged);
	}

	default String toJson(final EJsonScenario scenario) {
		return CJsonSerializer.toJson(this, scenario);
	}
}
