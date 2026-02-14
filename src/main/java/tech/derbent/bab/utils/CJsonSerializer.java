package tech.derbent.bab.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.temporal.TemporalAccessor;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.policybase.domain.IJsonNetworkSerializable;
import tech.derbent.bab.project.domain.CProject_Bab;

/** Simple recursive reflection serializer for network JSON payloads. */
public final class CJsonSerializer {

	public enum EJsonScenario {
		DEFAULT,
		BAB
	}

	private static final String BAB_SCENARIO_FILTER_ID = "babScenarioFilter";
	private static final Set<String> BAB_SCENARIO_GLOBAL_EXCLUDED_FIELD_NAMES =
			Set.of("attachments", "comments", "links", "authToken", "interfacesJson");
	private static final Map<String, Set<String>> BAB_SCENARIO_CLASS_EXCLUDED_FIELD_NAMES = createBabClassExcludedFieldMap();
	private static final Set<String> EXCLUDED_FIELD_NAMES = Set.of("LOGGER", "serialVersionUID");
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static Map<String, Set<String>> createBabClassExcludedFieldMap() {
		// Class-specific exclusions (short class names).
		// Includes all complex relation/object fields for CProject_Bab and its super classes.
		// Key format must be short class name (Class.getSimpleName()).
		return Map.of(
				"CProject_Bab", Set.of("httpClient"),
				"CProject", Set.of("entityType", "status", "userSettings"),
				"CEntityOfCompany", Set.of("company"));
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

	private static boolean isSimpleValue(final Class<?> clazz, final Object value) {
		return clazz.isPrimitive() || Number.class.isAssignableFrom(clazz) || Boolean.class.isAssignableFrom(clazz)
				|| Character.class.isAssignableFrom(clazz) || String.class.isAssignableFrom(clazz) || clazz.isEnum()
				|| Class.class.isAssignableFrom(clazz) || value instanceof TemporalAccessor;
	}

	public static String prettyPrintJson(final String json) {
		Check.notBlank(json, "JSON string cannot be blank");
		try {
			final JsonNode jsonNode = MAPPER.readTree(json);
			return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
		} catch (final Exception e) {
			throw new IllegalStateException("Failed to pretty-print JSON string", e);
		}
	}

	private static boolean isBabScenarioFieldExcluded(final Class<?> ownerClass, final String fieldName, final EJsonScenario scenario) {
		if (scenario != EJsonScenario.BAB) {
			return false;
		}
		final Set<String> classSpecificExclusions = BAB_SCENARIO_CLASS_EXCLUDED_FIELD_NAMES.get(ownerClass.getSimpleName());
		if (classSpecificExclusions != null && classSpecificExclusions.contains(fieldName)) {
			return true;
		}
		final JsonFilter jsonFilter = ownerClass.getAnnotation(JsonFilter.class);
		if ((jsonFilter == null) || !BAB_SCENARIO_FILTER_ID.equals(jsonFilter.value())) {
			return false;
		}
		if (BAB_SCENARIO_GLOBAL_EXCLUDED_FIELD_NAMES.contains(fieldName)) {
			return true;
		}
		return false;
	}

	private static boolean shouldSkipField(final Field field, final Class<?> ownerClass, final EJsonScenario scenario) {
		final int modifiers = field.getModifiers();
		if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers) || field.isSynthetic()) {
			return true;
		}
		final String fieldName = field.getName();
		return EXCLUDED_FIELD_NAMES.contains(fieldName) || isBabScenarioFieldExcluded(ownerClass, fieldName, scenario)
				|| fieldName.contains("hibernateLazyInitializer") || fieldName.contains("$$_hibernate") || fieldName.contains("handler");
	}

	public static String toJson(final Object object) {
		return toJson(object, EJsonScenario.BAB);
	}

	public static String toJson(final Object object, final EJsonScenario scenario) {
		try {
			return MAPPER.writeValueAsString(toJsonNode(object, new IdentityHashMap<>(), "$", scenario));
		} catch (final Exception e) {
			final String rootClass = object != null ? object.getClass().getName() : "null";
			throw new IllegalStateException("Failed to serialize object to JSON. rootClass=" + rootClass, e);
		}
	}

	private static JsonNode toJsonNode(final Object value, final IdentityHashMap<Object, Boolean> visited, final String path,
			final EJsonScenario scenario) {
		Check.notNull(visited, "Visited map cannot be null");
		Check.notBlank(path, "JSON path cannot be blank");
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

	public static String toPrettyJson(final Object object) {
		return toPrettyJson(object, EJsonScenario.BAB);
	}

	public static String toPrettyJson(final Object object, final EJsonScenario scenario) {
		try {
			return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(toJsonNode(object, new IdentityHashMap<>(), "$", scenario));
		} catch (final Exception e) {
			final String rootClass = object != null ? object.getClass().getName() : "null";
			throw new IllegalStateException("Failed to serialize object to pretty JSON. rootClass=" + rootClass, e);
		}
	}

	/** Serialize BAB project into an IoT-gateway-focused JSON payload.
	 * Excludes unrelated framework internals and large object graphs. */
	public static String toPrettyProjectBabJson(final CProject_Bab project) {
		Check.notNull(project, "Project cannot be null");
		try {
			final ObjectNode root = MAPPER.createObjectNode();
			root.put("id", project.getId());
			root.put("name", project.getName());
			root.put("description", project.getDescription());
			root.put("active", project.getActive());
			root.put("ipAddress", project.getIpAddress());
			root.put("connectedToCalimero", project.isConnectedToCalimero());
			root.put("interfacesLastUpdated", project.getInterfacesLastUpdated() != null ? project.getInterfacesLastUpdated().toString() : null);
			if (project.getInterfacesJson() != null && !project.getInterfacesJson().isBlank()) {
				try {
					root.set("interfaces", MAPPER.readTree(project.getInterfacesJson()));
				} catch (final Exception ignored) {
					root.put("interfaces", project.getInterfacesJson());
				}
			} else {
				root.putNull("interfaces");
			}
			final ObjectNode companyNode = MAPPER.createObjectNode();
			companyNode.put("id", project.getCompany() != null ? project.getCompany().getId() : null);
			companyNode.put("name", project.getCompany() != null ? project.getCompany().getName() : null);
			root.set("company", companyNode);
			final ObjectNode projectTypeNode = MAPPER.createObjectNode();
			projectTypeNode.put("id", project.getEntityType() != null ? project.getEntityType().getId() : null);
			projectTypeNode.put("name", project.getEntityType() != null ? project.getEntityType().getName() : null);
			root.set("projectType", projectTypeNode);
			final ObjectNode statusNode = MAPPER.createObjectNode();
			statusNode.put("id", project.getStatus() != null ? project.getStatus().getId() : null);
			statusNode.put("name", project.getStatus() != null ? project.getStatus().getName() : null);
			root.set("status", statusNode);
			return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(root);
		} catch (final Exception e) {
			throw new IllegalStateException("Failed to serialize CProject_Bab to IoT gateway JSON", e);
		}
	}

	private static JsonNode toSimpleNode(final Object value) {
		Check.notNull(value, "Simple value cannot be null");
		if (value instanceof TemporalAccessor) {
			return TextNode.valueOf(value.toString());
		}
		return MAPPER.valueToTree(value);
	}

	private CJsonSerializer() {}
}
