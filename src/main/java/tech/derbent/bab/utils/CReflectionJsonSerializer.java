package tech.derbent.bab.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.temporal.TemporalAccessor;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
public final class CReflectionJsonSerializer {

	private static final Set<String> EXCLUDED_FIELD_NAMES = Set.of("LOGGER", "serialVersionUID");
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private CReflectionJsonSerializer() {}

	public static String toJson(final Object object) {
		try {
			return MAPPER.writeValueAsString(toJsonNode(object, new IdentityHashMap<>(), "$"));
		} catch (final Exception e) {
			final String rootClass = object != null ? object.getClass().getName() : "null";
			throw new IllegalStateException("Failed to serialize object to JSON. rootClass=" + rootClass, e);
		}
	}

	private static JsonNode toJsonNode(final Object value, final IdentityHashMap<Object, Boolean> visited, final String path) {
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
					arrayNode.add(toJsonNode(element, visited, path + "[" + index + "]"));
				}
				return arrayNode;
			}
			if (value instanceof Set<?> set) {
				final ArrayNode arrayNode = MAPPER.createArrayNode();
				int index = 0;
				for (final Object element : set) {
					if (!isJsonSerializableGraphValue(element)) {
						index++;
						continue;
					}
					arrayNode.add(toJsonNode(element, visited, path + "[" + index + "]"));
					index++;
				}
				return arrayNode;
			}
			if (value instanceof List<?> list) {
				final ArrayNode arrayNode = MAPPER.createArrayNode();
				for (int index = 0; index < list.size(); index++) {
					final Object element = list.get(index);
					if (!isJsonSerializableGraphValue(element)) {
						continue;
					}
					arrayNode.add(toJsonNode(element, visited, path + "[" + index + "]"));
				}
				return arrayNode;
			}
			if (value instanceof Iterable<?> iterable) {
				final ArrayNode arrayNode = MAPPER.createArrayNode();
				int index = 0;
				for (final Object element : iterable) {
					if (!isJsonSerializableGraphValue(element)) {
						index++;
						continue;
					}
					arrayNode.add(toJsonNode(element, visited, path + "[" + index + "]"));
					index++;
				}
				return arrayNode;
			}
			if (value instanceof Map<?, ?> map) {
				final ObjectNode mapNode = MAPPER.createObjectNode();
				for (final Map.Entry<?, ?> entry : map.entrySet()) {
					if (entry.getKey() != null) {
						final String key = String.valueOf(entry.getKey());
						final Object entryValue = entry.getValue();
						if (!isJsonSerializableGraphValue(entryValue)) {
							continue;
						}
						mapNode.set(key, toJsonNode(entryValue, visited, path + "['" + key + "']"));
					}
				}
				return mapNode;
			}
			return toObjectNode(value, visited, path);
		} catch (final Exception e) {
			throw new IllegalStateException(
					"Failed to serialize object graph via reflection. path=" + path + ", class=" + clazz.getName(), e);
		} finally {
			visited.remove(value);
		}
	}

	private static boolean isSimpleValue(final Class<?> clazz, final Object value) {
		return clazz.isPrimitive()
				|| Number.class.isAssignableFrom(clazz)
				|| Boolean.class.isAssignableFrom(clazz)
				|| Character.class.isAssignableFrom(clazz)
				|| String.class.isAssignableFrom(clazz)
				|| clazz.isEnum()
				|| Class.class.isAssignableFrom(clazz)
				|| value instanceof TemporalAccessor;
	}

	private static JsonNode toSimpleNode(final Object value) {
		Check.notNull(value, "Simple value cannot be null");
		if (value instanceof TemporalAccessor) {
			return TextNode.valueOf(value.toString());
		}
		return MAPPER.valueToTree(value);
	}

	private static ObjectNode toObjectNode(final Object value, final IdentityHashMap<Object, Boolean> visited, final String path) {
		Check.notNull(value, "Object value cannot be null");
		Check.notNull(visited, "Visited map cannot be null");
		Check.notBlank(path, "JSON path cannot be blank");
		final ObjectNode objectNode = MAPPER.createObjectNode();
		Class<?> current = value.getClass();
			while ((current != null) && (current != Object.class)) {
				for (final Field field : current.getDeclaredFields()) {
					if (shouldSkipField(field)) {
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
						objectNode.set(fieldName, toJsonNode(fieldValue, visited, fieldPath));
					} catch (final Exception e) {
						throw new IllegalStateException("Failed to serialize field. field=" + current.getName() + "." + fieldName + ", fieldType="
								+ field.getType().getName() + ", valueClass="
								+ (fieldValue != null ? fieldValue.getClass().getName() : "null") + ", path=" + fieldPath, e);
					}
				}
			current = current.getSuperclass();
		}
		return objectNode;
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

	private static boolean shouldSkipField(final Field field) {
		final int modifiers = field.getModifiers();
		if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers) || field.isSynthetic()) {
			return true;
		}
		final String fieldName = field.getName();
		return field.isAnnotationPresent(JsonIgnore.class)
				|| EXCLUDED_FIELD_NAMES.contains(fieldName)
				|| fieldName.contains("hibernateLazyInitializer")
				|| fieldName.contains("$$_hibernate")
				|| fieldName.contains("handler");
	}
}
