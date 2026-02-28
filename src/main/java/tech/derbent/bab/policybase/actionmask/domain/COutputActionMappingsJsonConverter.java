package tech.derbent.bab.policybase.actionmask.domain;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Persists output-action mappings as JSON text for deterministic round-trip behavior. */
@Converter (autoApply = false)
public class COutputActionMappingsJsonConverter implements AttributeConverter<List<ROutputActionMapping>, String> {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final TypeReference<List<ROutputActionMapping>> LIST_TYPE = new TypeReference<List<ROutputActionMapping>>() {};

	@Override
	public String convertToDatabaseColumn(final List<ROutputActionMapping> attribute) {
		if (attribute == null || attribute.isEmpty()) {
			return "[]";
		}
		try {
			return OBJECT_MAPPER.writeValueAsString(attribute);
		} catch (final Exception e) {
			throw new IllegalStateException("Failed to serialize output action mappings to JSON", e);
		}
	}

	@Override
	public List<ROutputActionMapping> convertToEntityAttribute(final String dbData) {
		if (dbData == null || dbData.isBlank()) {
			return new ArrayList<>();
		}
		try {
			return new ArrayList<>(OBJECT_MAPPER.readValue(dbData, LIST_TYPE));
		} catch (final Exception e) {
			throw new IllegalStateException("Invalid JSON format in output_action_mappings column. Data must be JSON array.", e);
		}
	}
}
