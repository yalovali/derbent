package tech.derbent.bab.policybase.filter.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.context.annotation.Profile;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.ui.component.enhanced.CComponentMultiColumnListSelection.CMultiColumnStringRow;
import tech.derbent.api.ui.component.enhanced.CCrudToolbar;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterCAN;
import tech.derbent.bab.policybase.node.can.CA2LFileParser;
import tech.derbent.bab.policybase.node.can.CBabCanNode;
import tech.derbent.bab.policybase.node.can.CBabCanNodeService;

@Profile ("bab")
public class CPageServiceBabPolicyFilterCAN extends CPageServiceDynamicPage<CBabPolicyFilterCAN> {

	@Override
	public void actionCreate() {
		CNotificationService.showWarning("Policy filters are created from nodes. Please select a node and add filters from there.");
	}

	@Override
	protected void configureToolbar(final CCrudToolbar toolbar) {
		toolbar.configureButtonVisibility(false, true, true, true);
	}

	private static final TypeReference<Map<String, Object>> JSON_MAP_TYPE = new TypeReference<Map<String, Object>>() {};
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static String findRecordTypeLabelForVariable(final String variableName, final String protocolJson) {
		if (protocolJson == null || protocolJson.isBlank()) {
			return "";
		}
		try {
			final Map<String, Object> jsonMap = OBJECT_MAPPER.readValue(protocolJson, JSON_MAP_TYPE);
			for (final Object entryValue : jsonMap.values()) {
				if (!(entryValue instanceof final Map<?, ?> entryMap)) {
					continue;
				}
				final Object nameValue = entryMap.get(CA2LFileParser.CKeys.NAME);
				if (nameValue == null || !variableName.equalsIgnoreCase(String.valueOf(nameValue).trim())) {
					continue;
				}
				final Object recordType = entryMap.get(CA2LFileParser.CKeys.RECORD_TYPE);
				if (recordType != null && !String.valueOf(recordType).isBlank()) {
					return String.valueOf(recordType).trim().toUpperCase();
				}
			}
		} catch (final Exception e) {
			// Return empty record type if JSON contains unsupported structure.
		}
		return "";
	}

	private static String findTypeLabelForVariable(final String variableName, final String protocolJson) {
		if (protocolJson == null || protocolJson.isBlank()) {
			return "";
		}
		try {
			final Map<String, Object> jsonMap = OBJECT_MAPPER.readValue(protocolJson, JSON_MAP_TYPE);
			for (final Object entryValue : jsonMap.values()) {
				if (!(entryValue instanceof final Map<?, ?> entryMap)) {
					continue;
				}
				final Object nameValue = entryMap.get(CA2LFileParser.CKeys.NAME);
				if (nameValue == null || !variableName.equalsIgnoreCase(String.valueOf(nameValue).trim())) {
					continue;
				}
				final Object dataType = entryMap.get(CA2LFileParser.CKeys.DATA_TYPE);
				if (dataType != null && !String.valueOf(dataType).isBlank()) {
					return normalizeTypeLabel(String.valueOf(dataType));
				}
				final Object fieldType = entryMap.get(CA2LFileParser.CKeys.FIELD_TYPE);
				if (fieldType != null && !String.valueOf(fieldType).isBlank()) {
					return normalizeTypeLabel(String.valueOf(fieldType));
				}
			}
		} catch (final Exception e) {
			// Return empty type if JSON contains unsupported structure.
		}
		return "";
	}

	private static String normalizeTypeLabel(final String rawType) {
		if (rawType == null || rawType.isBlank()) {
			return "";
		}
		final String normalized = rawType.trim().toUpperCase();
		return switch (normalized) {
		case "UBYTE" -> "UBYTE (uint8)";
		case "UWORD", "WORD", "USHORT" -> "UWORD (uint16)";
		case "ULONG", "LONG", "UINT", "DWORD" -> "ULONG (uint32)";
		case "SBYTE" -> "SBYTE (int8)";
		case "SWORD", "SHORT", "INT16" -> "SWORD (int16)";
		case "SLONG", "INT", "INT32" -> "SLONG (int32)";
		case "FLOAT32_IEEE", "FLOAT", "FLOAT32" -> "FLOAT32_IEEE (float)";
		case "FLOAT64", "DOUBLE" -> "FLOAT64 (double)";
		default -> rawType.trim();
		};
	}

	public CPageServiceBabPolicyFilterCAN(final IPageServiceImplementer<CBabPolicyFilterCAN> view) {
		super(view);
	}

	public List<CMultiColumnStringRow> getComboValuesOfProtocolVariableNames(final CBabPolicyFilterCAN entity) {
		if (entity == null) {
			return List.of();
		}
		final CBabCanNodeService canNodeService = CSpringContext.getBean(CBabCanNodeService.class);
		// Deterministic sort for UI rows (case-insensitive) so grid ordering is stable.
		final Set<String> availableVariables = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		String protocolJson = null;
		if (entity.getParentNode() instanceof final CBabCanNode parentCanNode) {
			protocolJson = parentCanNode.getProtocolFileJson();
			if ((protocolJson == null || protocolJson.isBlank()) && parentCanNode.getId() != null) {
				protocolJson = canNodeService.loadProtocolContentFromDb(parentCanNode.getId(), CBabCanNodeService.EProtocolContentField.JSON);
			}
			availableVariables.addAll(canNodeService.extractProtocolVariableNames(protocolJson));
		}
		if (entity.getProtocolVariableNames() != null) {
			availableVariables.addAll(entity.getProtocolVariableNames());
		}
		final String protocolJsonSnapshot = protocolJson;
		return availableVariables.stream().map(variableName -> {
			// LinkedHashMap keeps insertion order; CFormBuilder reads keys in this exact order as grid columns.
			final Map<String, String> columns = new LinkedHashMap<>();
			// Column 3: low-level record type (UBYTE/UWORD/etc.).
			columns.put("recordType", findRecordTypeLabelForVariable(variableName, protocolJsonSnapshot));
			// Column 2: human-readable type context.
			columns.put("variableType", findTypeLabelForVariable(variableName, protocolJsonSnapshot));
			// Column 1: persisted return value column.
			columns.put("protocolVariableName", variableName);
			// One grid row record = icon + ordered column values map.
			return new CMultiColumnStringRow("vaadin:code", "#1C88FF", columns);
		}).toList();
	}
}
