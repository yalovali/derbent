package tech.derbent.bab.policybase.filter.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.bab.policybase.filter.service.CBabPolicyFilterCANService;
import tech.derbent.bab.policybase.filter.service.CPageServiceBabPolicyFilterCAN;
import tech.derbent.bab.policybase.node.can.CA2LFileParser;
import tech.derbent.bab.policybase.node.can.CBabCanNode;

/** CAN-specific policy filter entity. */
@Entity
@Table (name = "cbab_policy_filter_can")
@DiscriminatorValue ("CAN")
@Profile ("bab")
@JsonFilter ("babScenarioFilter")
public final class CBabPolicyFilterCAN extends CBabPolicyFilterBase<CBabPolicyFilterCAN> {

	private static final TypeReference<Map<String, Object>> JSON_MAP_TYPE = new TypeReference<Map<String, Object>>() {};
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public static final String DEFAULT_CAN_FRAME_ID_REGULAR_EXPRESSION = ".*";
	public static final String DEFAULT_CAN_PAYLOAD_REGULAR_EXPRESSION = ".*";
	public static final String DEFAULT_COLOR = "#FF9800";
	public static final String DEFAULT_ICON = "vaadin:car";
	public static final String ENTITY_TITLE_PLURAL = "CAN Policy Filters";
	public static final String ENTITY_TITLE_SINGULAR = "CAN Policy Filter";
	public static final String FILTER_KIND = "CAN";
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyFilterCAN.class);
	public static final String VIEW_NAME = "CAN Policy Filters View";
	@Column (name = "can_frame_id_regular_expression", length = 100, nullable = false)
	@AMetaData (
			displayName = "Frame-ID Regex", required = false, readOnly = false, description = "Regex used to match CAN frame IDs", hidden = false,
			maxLength = 100
	)
	private String canFrameIdRegularExpression = DEFAULT_CAN_FRAME_ID_REGULAR_EXPRESSION;
	@Column (name = "can_payload_regular_expression", length = 255, nullable = false)
	@AMetaData (
			displayName = "Payload Regex", required = false, readOnly = false, description = "Regex used to match CAN payload values", hidden = false,
			maxLength = 255
	)
	private String canPayloadRegularExpression = DEFAULT_CAN_PAYLOAD_REGULAR_EXPRESSION;
	@Column (name = "protocol_variable_names", length = 4000)
	@AMetaData (
			displayName = "Protocol Variables", required = false, readOnly = false,
			description = "Protocol variable names selected from loaded CAN protocol JSON data", hidden = false, useGridSelection = true,
			dataProviderBean = "pageservice", dataProviderMethod = "getComboValuesOfProtocolVariableNames", dataProviderParamBean = "context",
			dataProviderParamMethod = "getValue"
	)
	private List<String> protocolVariableNames = new ArrayList<>();
	@Column (name = "require_extended_frame", nullable = false)
	@AMetaData (
			displayName = "Require Extended Frame", required = false, readOnly = false,
			description = "If enabled, only CAN extended-frame packets are accepted", hidden = false
	)
	private Boolean requireExtendedFrame = false;

	/** Default constructor for JPA. */
	protected CBabPolicyFilterCAN() {
		// JPA constructor must not initialize business defaults.
	}

	public CBabPolicyFilterCAN(final String name, final CBabCanNode parentNode) {
		super(CBabPolicyFilterCAN.class, name, parentNode);
		initializeDefaults();
	}

	@Override
	public Class<CBabCanNode> getAllowedNodeType() { return CBabCanNode.class; }

	public String getCanFrameIdRegularExpression() {
		return canFrameIdRegularExpression != null && !canFrameIdRegularExpression.isBlank() ? canFrameIdRegularExpression
				: DEFAULT_CAN_FRAME_ID_REGULAR_EXPRESSION;
	}

	public String getCanPayloadRegularExpression() {
		return canPayloadRegularExpression != null && !canPayloadRegularExpression.isBlank() ? canPayloadRegularExpression
				: DEFAULT_CAN_PAYLOAD_REGULAR_EXPRESSION;
	}

	@Override
	public String getFilterKind() { return FILTER_KIND; }

	@Override
	public List<ROutputStructure> getOutputStructure() { return buildOutputStructureFromSelectedVariables(); }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceBabPolicyFilterCAN.class; }

	public List<String> getProtocolVariableNames() { return protocolVariableNames; }

	public Boolean getRequireExtendedFrame() { return requireExtendedFrame; }

	@Override
	public Class<?> getServiceClass() { return CBabPolicyFilterCANService.class; }

	private final void initializeDefaults() {
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	public static Map<String, ROutputStructure> getOutputStructureByVariableName(final String protocolJson) {
		if (protocolJson == null || protocolJson.isBlank()) {
			return Map.of();
		}
		try {
			final Map<String, Object> protocolMap = OBJECT_MAPPER.readValue(protocolJson, JSON_MAP_TYPE);
			final Map<String, Map<String, Object>> outputsByVariableName = indexOutputsByVariableName(protocolMap);
			final Map<String, ROutputStructure> outputStructureByVariableName = new LinkedHashMap<>();
			for (final Map.Entry<String, Map<String, Object>> entry : outputsByVariableName.entrySet()) {
				final String normalizedVariableName = normalizeVariableName(entry.getKey());
				if (normalizedVariableName.isEmpty() || outputStructureByVariableName.containsKey(normalizedVariableName)) {
					continue;
				}
				final Map<String, Object> a2lEntry = entry.getValue();
				outputStructureByVariableName.put(normalizedVariableName,
						new ROutputStructure(resolveOutputName(entry.getKey(), a2lEntry), resolveOutputDataType(a2lEntry)));
			}
			return Map.copyOf(outputStructureByVariableName);
		} catch (final Exception e) {
			LOGGER.warn("Failed to parse CAN protocol output structure map: {}", e.getMessage());
			return Map.of();
		}
	}

	public List<ROutputStructure> buildOutputStructureFromSelectedVariables() {
		if (protocolVariableNames == null || protocolVariableNames.isEmpty()) {
			return List.of();
		}
		if (!(getParentNode() instanceof final CBabCanNode parentCanNode)) {
			return List.of();
		}
		final String protocolJson = parentCanNode.getProtocolFileJson();
		if (protocolJson == null || protocolJson.isBlank()) {
			return List.of();
		}
		try {
			final Map<String, Object> protocolMap = OBJECT_MAPPER.readValue(protocolJson, JSON_MAP_TYPE);
			final Map<String, Map<String, Object>> outputsByVariableName = indexOutputsByVariableName(protocolMap);
			final List<ROutputStructure> outputs = new ArrayList<>();
			for (final String variableName : protocolVariableNames) {
				if (variableName == null || variableName.isBlank()) {
					continue;
				}
				final Map<String, Object> a2lEntry = outputsByVariableName.get(normalizeVariableName(variableName));
				if (a2lEntry != null) {
					final String outputName = resolveOutputName(variableName, a2lEntry);
					final String outputDataType = resolveOutputDataType(a2lEntry);
					outputs.add(new ROutputStructure(outputName, outputDataType));
				}
			}
			return outputs;
		} catch (final Exception e) {
			LOGGER.warn("Failed to build CAN output structure for '{}': {}", getName(), e.getMessage());
			return List.of();
		}
	}

	@SuppressWarnings ("unchecked")
	private static Map<String, Map<String, Object>> indexOutputsByVariableName(final Map<String, Object> protocolMap) {
		final Map<String, Map<String, Object>> outputsByVariableName = new LinkedHashMap<>();
		for (final Map.Entry<String, Object> entry : protocolMap.entrySet()) {
			final String entryKey = entry.getKey();
			if (entryKey == null || entryKey.startsWith("_")) {
				continue;
			}
			if (!(entry.getValue() instanceof final Map<?, ?> entryMap)) {
				continue;
			}
			final Map<String, Object> a2lEntry = new LinkedHashMap<>((Map<String, Object>) entryMap);
			final String normalizedEntryKey = entryKey.trim().toLowerCase(Locale.ROOT);
			if (!normalizedEntryKey.isEmpty()) {
				outputsByVariableName.putIfAbsent(normalizedEntryKey, a2lEntry);
			}
			final Object nameValue = a2lEntry.get(CA2LFileParser.CKeys.NAME);
			if (nameValue == null) {
				continue;
			}
			final String normalizedName = String.valueOf(nameValue).trim().toLowerCase(Locale.ROOT);
			if (!normalizedName.isEmpty()) {
				outputsByVariableName.putIfAbsent(normalizedName, a2lEntry);
			}
		}
		return outputsByVariableName;
	}

	public static String normalizeDataType(final String rawType) {
		if (rawType == null || rawType.isBlank()) {
			return "";
		}
		final String normalized = rawType.trim().toUpperCase(Locale.ROOT);
		return switch (normalized) {
		case "UBYTE", "SBYTE", "BYTE", "CHAR", "UINT8", "INT8" -> "char";
		case "UWORD", "WORD", "USHORT", "SHORT", "SWORD", "UINT16", "INT16" -> "int";
		case "ULONG", "LONG", "SLONG", "UINT", "DWORD", "UINT32", "INT32", "SINT32" -> "int";
		case "FLOAT32_IEEE", "FLOAT32", "FLOAT" -> "float";
		case "FLOAT64", "DOUBLE" -> "double";
		case "BOOLEAN", "BOOL" -> "boolean";
		default -> rawType.trim().toLowerCase(Locale.ROOT);
		};
	}

	private static String resolveOutputDataType(final Map<String, Object> a2lEntry) {
		final Object dataType = a2lEntry.get(CA2LFileParser.CKeys.DATA_TYPE);
		if (dataType != null && !String.valueOf(dataType).isBlank()) {
			return normalizeDataType(String.valueOf(dataType));
		}
		final Object recordType = a2lEntry.get(CA2LFileParser.CKeys.RECORD_TYPE);
		if (recordType != null && !String.valueOf(recordType).isBlank()) {
			return normalizeDataType(String.valueOf(recordType));
		}
		final Object fieldType = a2lEntry.get(CA2LFileParser.CKeys.FIELD_TYPE);
		if (fieldType != null && !String.valueOf(fieldType).isBlank()) {
			return normalizeDataType(String.valueOf(fieldType));
		}
		return "";
	}

	private static String resolveOutputName(final String fallbackName, final Map<String, Object> a2lEntry) {
		final Object nameValue = a2lEntry.get(CA2LFileParser.CKeys.NAME);
		if (nameValue != null && !String.valueOf(nameValue).isBlank()) {
			return String.valueOf(nameValue).trim();
		}
		return fallbackName.trim();
	}

	public static String normalizeVariableName(final String variableName) {
		if (variableName == null || variableName.isBlank()) {
			return "";
		}
		return variableName.trim().toLowerCase(Locale.ROOT);
	}

	public void setCanFrameIdRegularExpression(final String canFrameIdRegularExpression) {
		this.canFrameIdRegularExpression = canFrameIdRegularExpression == null || canFrameIdRegularExpression.isBlank()
				? DEFAULT_CAN_FRAME_ID_REGULAR_EXPRESSION : canFrameIdRegularExpression.trim();
		updateLastModified();
	}

	public void setCanPayloadRegularExpression(final String canPayloadRegularExpression) {
		this.canPayloadRegularExpression = canPayloadRegularExpression == null || canPayloadRegularExpression.isBlank()
				? DEFAULT_CAN_PAYLOAD_REGULAR_EXPRESSION : canPayloadRegularExpression.trim();
		updateLastModified();
	}

	public void setProtocolVariableNames(final List<String> protocolVariableNames) {
		if (protocolVariableNames == null) {
			this.protocolVariableNames = new ArrayList<>();
			updateLastModified();
			return;
		}
		this.protocolVariableNames = new ArrayList<>(protocolVariableNames.stream()
				.filter(variableName -> variableName != null && !variableName.isBlank()).map(String::trim).distinct().toList());
		updateLastModified();
	}

	public void setRequireExtendedFrame(final Boolean requireExtendedFrame) {
		this.requireExtendedFrame = requireExtendedFrame;
		updateLastModified();
	}
}
