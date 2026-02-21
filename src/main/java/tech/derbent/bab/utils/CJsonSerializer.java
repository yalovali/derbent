package tech.derbent.bab.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.temporal.TemporalAccessor;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.policybase.domain.IJsonNetworkSerializable;

/** Simple recursive reflection serializer for network JSON payloads. */
public final class CJsonSerializer {

	private record CScenarioConfig(Map<String, Set<String>> classExcludedFieldNames, String jsonFilterId, Set<String> globalExcludedFieldNames) {}

	public enum EJsonScenario {
		/* for bab configuration */
		JSONSENARIO_BABCONFIGURATION,
		/* for bab policy */
		JSONSENARIO_BABPOLICY
	}

	private static final String BAB_FILTER_ID = "babScenarioFilter";
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final Map<EJsonScenario, CScenarioConfig> SCENARIO_CONFIGS = createScenarioConfigs();

	private static Map<String, Set<String>> createExcludedFieldMap_BabConfiguration() {
		final Map<String, Set<String>> map = new java.util.HashMap<>();
		map.put("EntityDB", Set.of(""));
		map.put("CEntityNamed", Set.of("createdDate", "lastModifiedDate", "description"));
		map.put("CEntityOfCompany", Set.of("company"));
		map.put("CProject", Set.of("entityType", "status", "userSettings", "company"));
		map.put("CProject_Bab", Set.of("httpClient", "authToken", "interfacesJson", "interfacesLastUpdated", "connectedToCalimero", "ipAddress",
				"lastConnectionAttempt", "policyRules"));
		return map;
	}

	private static Map<String, Set<String>> createExcludedFieldMap_BabPolicy() {
		final Map<String, Set<String>> map = new java.util.HashMap<>();
		map.put("EntityDB", Set.of(""));
		map.put("CEntityNamed", Set.of("createdDate", "lastModifiedDate", "description"));
		map.put("CEntityOfCompany", Set.of("company"));
		map.put("CProject", Set.of("entityType", "status", "userSettings", "company"));
		map.put("CProject_Bab", Set.of("httpClient", "authToken", "interfacesJson", "interfacesLastUpdated", "connectedToCalimero", "ipAddress",
				"lastConnectionAttempt"));
		map.put("CBabPolicyFilterBase", Set.of("parentNode", "canNodeEnabled", "fileNodeEnabled", "httpNodeEnabled", "modbusNodeEnabled",
				"rosNodeEnabled", "syslogNodeEnabled"));
		map.put("CBabCanNode", Set.of("nodeConfigJson", "connectionStatus", "protocolFileSummaryJson", "protocolFileJson", "protocolFileData",
				"placeHolder_createComponentProtocolFileData", "bitrate"));
		map.put("CBabPolicyTrigger",
				Set.of("canNodeEnabled", "fileNodeEnabled", "httpNodeEnabled", "modbusNodeEnabled", "rosNodeEnabled", "syslogNodeEnabled"));
		map.put("CBabNodeEntity", Set.of("nodeConfigJson", "connectionStatus"));
		map.put("CBabPolicyAction",
				Set.of("canNodeEnabled", "fileNodeEnabled", "httpNodeEnabled", "modbusNodeEnabled", "rosNodeEnabled", "syslogNodeEnabled"));
		map.put("CBabFileInputNode", Set.of("filePattern", "maxFileSizeMb"));
		map.put("CBabFileOutputNode", Set.of("filePattern", "maxFileSizeMb"));
		return map;
	}

	private static DefaultPrettyPrinter createPrettyPrinter() {
		final DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
		final DefaultIndenter indenter = new DefaultIndenter("  ", DefaultIndenter.SYS_LF);
		prettyPrinter.indentObjectsWith(indenter);
		prettyPrinter.indentArraysWith(indenter);
		return prettyPrinter;
	}

	private static Map<EJsonScenario, CScenarioConfig> createScenarioConfigs() {
		final Map<EJsonScenario, CScenarioConfig> map = new HashMap<>();
		map.put(EJsonScenario.JSONSENARIO_BABPOLICY, new CScenarioConfig(createExcludedFieldMap_BabPolicy(), BAB_FILTER_ID,
				Set.of("attachments", "comments", "links", "authToken", "interfacesJson")));
		map.put(EJsonScenario.JSONSENARIO_BABCONFIGURATION,
				new CScenarioConfig(createExcludedFieldMap_BabConfiguration(), BAB_FILTER_ID, Set.of("authToken", "interfacesJson")));
		return Map.copyOf(map);
	}

	private static CScenarioConfig getScenarioConfig(final EJsonScenario scenario) {
		Check.notNull(scenario, "Serialization scenario cannot be null");
		final CScenarioConfig config = SCENARIO_CONFIGS.get(scenario);
		Check.notNull(config, "No serializer configuration found for scenario: " + scenario);
		return config;
	}

	private static boolean isJsonSerializableEntity(final Object value) {
		if (value == null) {
			return true;
		}
		return !(value instanceof CEntityDB<?>) || value instanceof IJsonNetworkSerializable;
	}

	private static boolean isJsonSerializableGraphValue(final Object value) {
		return value == null || isSimpleValue(value.getClass(), value) || isJsonSerializableEntity(value);
	}

	private static boolean isScenarioFieldExcluded(final Class<?> ownerClass, final String fieldName, final EJsonScenario scenario) {
		final CScenarioConfig config = getScenarioConfig(scenario);
		final Set<String> classSpecificExclusions = config.classExcludedFieldNames.get(ownerClass.getSimpleName());
		if (classSpecificExclusions != null && classSpecificExclusions.contains(fieldName)) {
			return true;
		}
		if (config.globalExcludedFieldNames.isEmpty()) {
			return false;
		}
		if (config.jsonFilterId == null || config.jsonFilterId.isBlank()) {
			return config.globalExcludedFieldNames.contains(fieldName);
		}
		final JsonFilter jsonFilter = ownerClass.getAnnotation(JsonFilter.class);
		if (jsonFilter == null || !config.jsonFilterId.equals(jsonFilter.value())) {
			return false;
		}
		return config.globalExcludedFieldNames.contains(fieldName);
	}

	private static boolean isSimpleValue(final Class<?> clazz, final Object value) {
		return clazz.isPrimitive() || Number.class.isAssignableFrom(clazz) || Boolean.class.isAssignableFrom(clazz)
				|| Character.class.isAssignableFrom(clazz) || String.class.isAssignableFrom(clazz) || clazz.isEnum()
				|| Class.class.isAssignableFrom(clazz) || value instanceof TemporalAccessor;
	}

	private static boolean shouldSkipField(final Field field, final Class<?> ownerClass, final EJsonScenario scenario) {
		final int modifiers = field.getModifiers();
		if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers) || field.isSynthetic()) {
			return true;
		}
		final String fieldName = field.getName();
		return isScenarioFieldExcluded(ownerClass, fieldName, scenario) || fieldName.contains("hibernateLazyInitializer")
				|| fieldName.contains("$$_hibernate") || fieldName.contains("handler");
	}

	private static JsonNode toJsonNode(final Object value, final IdentityHashMap<Object, Boolean> visited, final String path,
			final EJsonScenario scenario) {
		Check.notNull(visited, "Visited map cannot be null");
		Check.notBlank(path, "JSON path cannot be blank");
		Check.notNull(scenario, "Serialization scenario cannot be null");
		if (value == null) {
			return NullNode.instance;
		}
		final Class<?> clazz = value.getClass();
		Check.notNull(clazz, "Value class cannot be null at path=" + path);
		if (isSimpleValue(clazz, value)) {
			return toSimpleNode(value);
		}
		if (visited.containsKey(value)) {
			final ObjectNode ref = MAPPER.createObjectNode();
			ref.put("$ref", clazz.getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(value)));
			return ref;
		}
		visited.put(value, true);
		try {
			if (clazz.isArray()) {
				final ArrayNode arrayNode = MAPPER.createArrayNode();
				final int length = Array.getLength(value);
				for (int index = 0; index < length; index++) {
					final Object element = Array.get(value, index);
					if (!isJsonSerializableGraphValue(element)) {
						continue;
					}
					arrayNode.add(toJsonNode(element, visited, path + "[" + index + "]", scenario));
				}
				return arrayNode;
			}
			if (value instanceof final Set<?> set) {
				final ArrayNode arrayNode = MAPPER.createArrayNode();
				int index = 0;
				for (final Object element : set) {
					if (!isJsonSerializableGraphValue(element)) {
						index++;
						continue;
					}
					arrayNode.add(toJsonNode(element, visited, path + "[" + index + "]", scenario));
					index++;
				}
				return arrayNode;
			}
			if (value instanceof final List<?> list) {
				final ArrayNode arrayNode = MAPPER.createArrayNode();
				for (int index = 0; index < list.size(); index++) {
					final Object element = list.get(index);
					if (!isJsonSerializableGraphValue(element)) {
						continue;
					}
					arrayNode.add(toJsonNode(element, visited, path + "[" + index + "]", scenario));
				}
				return arrayNode;
			}
			if (value instanceof final Iterable<?> iterable) {
				final ArrayNode arrayNode = MAPPER.createArrayNode();
				int index = 0;
				for (final Object element : iterable) {
					if (!isJsonSerializableGraphValue(element)) {
						index++;
						continue;
					}
					arrayNode.add(toJsonNode(element, visited, path + "[" + index + "]", scenario));
					index++;
				}
				return arrayNode;
			}
			if (value instanceof final Map<?, ?> map) {
				final ObjectNode mapNode = MAPPER.createObjectNode();
				for (final Map.Entry<?, ?> entry : map.entrySet()) {
					if (entry.getKey() != null) {
						final String key = String.valueOf(entry.getKey());
						final Object entryValue = entry.getValue();
						if (!isJsonSerializableGraphValue(entryValue)) {
							continue;
						}
						mapNode.set(key, toJsonNode(entryValue, visited, path + "['" + key + "']", scenario));
					}
				}
				return mapNode;
			}
			return toObjectNode(value, visited, path, scenario);
		} catch (final Exception e) {
			throw new IllegalStateException("Failed to serialize object graph via reflection. path=" + path + ", class=" + clazz.getName(), e);
		} finally {
			visited.remove(value);
		}
	}

	private static ObjectNode toObjectNode(final Object value, final IdentityHashMap<Object, Boolean> visited, final String path,
			final EJsonScenario scenario) {
		Check.notNull(value, "Object value cannot be null");
		Check.notNull(visited, "Visited map cannot be null");
		Check.notBlank(path, "JSON path cannot be blank");
		final ObjectNode objectNode = MAPPER.createObjectNode();
		Class<?> current = value.getClass();
		while (current != null && current != Object.class) {
			for (final Field field : current.getDeclaredFields()) {
				if (shouldSkipField(field, current, scenario)) {
					continue;
				}
				final String fieldName = field.getName();
				final String fieldPath = path + "." + fieldName;
				field.setAccessible(true);
				final Object fieldValue;
				try {
					fieldValue = field.get(value);
				} catch (final Exception e) {
					throw new IllegalStateException("Failed to read field via reflection. field=" + current.getName() + "." + fieldName
							+ ", fieldType=" + field.getType().getName() + ", path=" + fieldPath, e);
				}
				if (!isJsonSerializableGraphValue(fieldValue)) {
					continue;
				}
				try {
					objectNode.set(fieldName, toJsonNode(fieldValue, visited, fieldPath, scenario));
				} catch (final Exception e) {
					throw new IllegalStateException(
							"Failed to serialize field. field=" + current.getName() + "." + fieldName + ", fieldType=" + field.getType().getName()
									+ ", valueClass=" + (fieldValue != null ? fieldValue.getClass().getName() : "null") + ", path=" + fieldPath,
							e);
				}
			}
			current = current.getSuperclass();
		}
		return objectNode;
	}

	public static String toPrettyJson(final Object object, final EJsonScenario scenario) {
		try {
			Check.notNull(object, "Object to serialize cannot be null");
			Check.notNull(scenario, "Serialization scenario cannot be null");
			return MAPPER.writer(createPrettyPrinter()).writeValueAsString(toJsonNode(object, new IdentityHashMap<>(), "$", scenario));
		} catch (final Exception e) {
			final String rootClass = object != null ? object.getClass().getName() : "null";
			throw new IllegalStateException("Failed to serialize object to pretty JSON. rootClass=" + rootClass, e);
		}
	}

	private static JsonNode toSimpleNode(final Object value) {
		Check.notNull(value, "Simple value cannot be null");
		if (value instanceof Class<?>) {
			return TextNode.valueOf(((Class<?>) value).getSimpleName());
		}
		if (value instanceof TemporalAccessor) {
			return TextNode.valueOf(value.toString());
		}
		return MAPPER.valueToTree(value);
	}

	private CJsonSerializer() {}
}
