package tech.derbent.bab.policybase.node.can;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.policybase.node.service.CBabNodeService;

/** CBabCanNodeService - Service for CAN Bus virtual network nodes. Layer: Service (MVC) Active when: 'bab' profile is active Following Derbent
 * pattern: Entity service extending common node base service. Provides CAN-specific business logic: - Bitrate validation - CAN configuration
 * validation - Interface uniqueness validation */
@Service
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public class CBabCanNodeService extends CBabNodeService<CBabCanNode> implements IEntityRegistrable, IEntityWithView {

	public enum EProtocolSummaryStatus {
		ERROR, NO_FILE, PARSED
	}

	public record CProtocolFileSummary(EProtocolSummaryStatus status, String message, int loadedEntityCount, int loadedCharacteristicCount,
			int loadedMeasurementCount, int loadedCurveCount, int loadedMapCount, long fileSizeBytes) {}

	public enum EProtocolContentField {
		JSON, RAW
	}

	public enum EProtocolJsonKey {

		ALL_A2L_VARIABLES(CA2LFileParser.CKeys.ALL_A2L_VARIABLES), NAME(CA2LFileParser.CKeys.NAME);

		private final String key;

		EProtocolJsonKey(final String key) {
			this.key = key;
		}

		public String getKey() { return key; }
	}

	private static final String A2L_EXTENSION = ".a2l";
	private static final CA2LFileParser A2L_FILE_PARSER = new CA2LFileParser();
	private static final String A2L_JSON_EXTENSION = ".a2l.json";
	private static final String CSV_SEPARATOR = ",";
	private static final TypeReference<Map<String, Object>> JSON_MAP_TYPE = new TypeReference<Map<String, Object>>() {};
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabCanNodeService.class);
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static void addVariableNamesFromCsvKey(final Set<String> targetVariableNames, final Object csvValue) {
		if (csvValue == null) {
			return;
		}
		final String csv = String.valueOf(csvValue);
		java.util.Arrays.stream(csv.split(CSV_SEPARATOR)).map(String::trim).filter(token -> !token.isEmpty()).forEach(targetVariableNames::add);
	}

	@SuppressWarnings ("unchecked")
	private static void addVariableNamesFromEntries(final Set<String> targetVariableNames, final Map<String, Object> protocolJsonMap) {
		for (final Map.Entry<String, Object> entry : protocolJsonMap.entrySet()) {
			if (entry.getKey().startsWith("_")) {
				continue;
			}
			if (!(entry.getValue() instanceof final Map<?, ?> entryMap)) {
				continue;
			}
			final Object nameValue = ((Map<String, Object>) entryMap).get(EProtocolJsonKey.NAME.getKey());
			if (nameValue == null) {
				continue;
			}
			final String variableName = String.valueOf(nameValue).trim();
			if (!variableName.isEmpty()) {
				targetVariableNames.add(variableName);
			}
		}
	}

	private static int csvCount(final Object csvValue) {
		if (csvValue == null) {
			return 0;
		}
		final String csv = String.valueOf(csvValue).trim();
		if (csv.isEmpty()) {
			return 0;
		}
		return (int) java.util.Arrays.stream(csv.split(CSV_SEPARATOR)).map(String::trim).filter(token -> !token.isEmpty()).count();
	}

	private static void ensureA2LExtension(final Path filePath) {
		final String fileName = filePath.getFileName().toString().toLowerCase();
		if (!fileName.endsWith(A2L_EXTENSION)) {
			throw new IllegalArgumentException("Expected an A2L file with extension '.a2l': " + filePath);
		}
	}

	private static Path getDefaultCompanionJsonPath(final Path a2LPath) {
		final String fileName = a2LPath.getFileName().toString();
		final String companionFileName = fileName.endsWith(A2L_EXTENSION) ? fileName + ".json" : fileName + A2L_JSON_EXTENSION;
		return a2LPath.resolveSibling(companionFileName);
	}

	private static boolean isBlank(final String value) {
		return value == null || value.isBlank();
	}

	private static EProtocolSummaryStatus parseStatus(final String statusText) {
		try {
			return EProtocolSummaryStatus.valueOf(statusText);
		} catch (final Exception e) {
			return EProtocolSummaryStatus.ERROR;
		}
	}

	private static int toInt(final Object value) {
		if (value instanceof final Number number) {
			return number.intValue();
		}
		try {
			return value == null ? 0 : Integer.parseInt(String.valueOf(value));
		} catch (final Exception e) {
			return 0;
		}
	}

	private static long toLong(final Object value) {
		if (value instanceof final Number number) {
			return number.longValue();
		}
		try {
			return value == null ? 0L : Long.parseLong(String.valueOf(value));
		} catch (final Exception e) {
			return 0L;
		}
	}

	private static String toSummaryJson(final EProtocolSummaryStatus status, final String message, final int loadedEntityCount,
			final int loadedCharacteristicCount, final int loadedMeasurementCount, final int loadedCurveCount, final int loadedMapCount,
			final long fileSizeBytes) {
		final Map<String, Object> jsonMap = new LinkedHashMap<>();
		jsonMap.put("status", status.name());
		jsonMap.put("message", message);
		jsonMap.put("loadedEntityCount", loadedEntityCount);
		jsonMap.put("loadedCharacteristicCount", loadedCharacteristicCount);
		jsonMap.put("loadedMeasurementCount", loadedMeasurementCount);
		jsonMap.put("loadedCurveCount", loadedCurveCount);
		jsonMap.put("loadedMapCount", loadedMapCount);
		jsonMap.put("fileSizeBytes", fileSizeBytes);
		jsonMap.put("updatedAt", LocalDateTime.now().toString());
		try {
			return OBJECT_MAPPER.writeValueAsString(jsonMap);
		} catch (final Exception e) {
			return "{\"status\":\"ERROR\",\"message\":\"File parse error. Failed to write summary JSON.\",\"loadedEntityCount\":0}";
		}
	}

	public CBabCanNodeService(final ICanNodeRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Build nodeConfigJson directly from in-memory protocol file data. */
	public String buildNodeConfigJsonFromProtocolFileData(final CBabCanNode node) {
		Check.notNull(node, "Node cannot be null");
		return getOrLoadProtocolFileJson(node, false);
	}

	/** Clear in-memory protocol file cache fields on the node. */
	public void clearProtocolFileCache(final CBabCanNode node) {
		Check.notNull(node, "Node cannot be null");
		node.clearProtocolFileCache();
	}

	/** Copy entity-specific fields from source to target. MANDATORY: All entity services must implement this method.
	 * @param source  the source entity to copy from
	 * @param target  the target entity to copy to
	 * @param options clone options controlling what fields to copy */
	@Override
	public void copyEntityFieldsTo(final CBabCanNode source, final CEntityDB<?> target, final CCloneOptions options) {
		// STEP 1: ALWAYS call parent first
		super.copyEntityFieldsTo(source, target, options);
		// STEP 2: Type-check target
		if (!(target instanceof CBabCanNode targetNode)) {
			return;
		}
		// STEP 3: Copy CAN-specific fields using DIRECT setter/getter
		targetNode.setBitrate(source.getBitrate());
		targetNode.setErrorWarningLimit(source.getErrorWarningLimit());
		targetNode.setProtocolType(source.getProtocolType());
		targetNode.setProtocolFileData(source.getProtocolFileData());
		targetNode.setProtocolFileJson(source.getProtocolFileJson());
		targetNode.setProtocolFileSummaryJson(source.getProtocolFileSummaryJson());
		// STEP 4: Log completion
		LOGGER.debug("Copied {} '{}' with options: {}", getClass().getSimpleName(), source.getName(), options);
	}

	public String createNoFileSummaryJson() {
		return toSummaryJson(EProtocolSummaryStatus.NO_FILE, "No protocol file is loaded.", 0, 0, 0, 0, 0, 0L);
	}

	public String createParsedSummaryJson(final String parsedProtocolJson, final long fileSizeBytes) {
		Check.notBlank(parsedProtocolJson, "Parsed protocol JSON cannot be null or empty");
		try {
			final Map<String, Object> jsonMap = OBJECT_MAPPER.readValue(parsedProtocolJson, JSON_MAP_TYPE);
			final int loadedEntityCount = csvCount(jsonMap.get("_all_a2l_variables"));
			final int loadedCharacteristicCount = csvCount(jsonMap.get("_all_a2l_characteristics"));
			final int loadedMeasurementCount = csvCount(jsonMap.get("_all_a2l_measurements"));
			final int loadedCurveCount = csvCount(jsonMap.get("_all_a2l_curves"));
			final int loadedMapCount = csvCount(jsonMap.get("_all_a2l_maps"));
			final String message = "Loaded protocol entities: " + loadedEntityCount + " (characteristics: " + loadedCharacteristicCount
					+ ", measurements: " + loadedMeasurementCount + ").";
			return toSummaryJson(EProtocolSummaryStatus.PARSED, message, loadedEntityCount, loadedCharacteristicCount, loadedMeasurementCount,
					loadedCurveCount, loadedMapCount, Math.max(fileSizeBytes, 0L));
		} catch (final Exception e) {
			return createParseErrorSummaryJson("Failed to build summary: " + e.getMessage(), fileSizeBytes);
		}
	}

	public String createParseErrorSummaryJson(final String errorMessage, final long fileSizeBytes) {
		final String message = errorMessage == null || errorMessage.isBlank() ? "File parse error." : "File parse error. " + errorMessage;
		return toSummaryJson(EProtocolSummaryStatus.ERROR, message, 0, 0, 0, 0, 0, Math.max(fileSizeBytes, 0L));
	}

	private void ensureProtocolFileLoaded(final CBabCanNode node) {
		if (!isBlank(node.getProtocolFileData()) && !isBlank(node.getProtocolFileJson())) {
			return;
		}
		if (isBlank(node.getProtocolFileData())) {
			return;
		}
		node.setProtocolFileJson(parseA2LContentAsJson(node.getProtocolFileData()));
	}

	/** Extract unique protocol variable names from a stored protocol JSON blob. */
	public List<String> extractProtocolVariableNames(final String protocolJson) {
		if (isBlank(protocolJson)) {
			return List.of();
		}
		final Set<String> variableNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		try {
			final Map<String, Object> jsonMap = OBJECT_MAPPER.readValue(protocolJson, JSON_MAP_TYPE);
			addVariableNamesFromCsvKey(variableNames, jsonMap.get(EProtocolJsonKey.ALL_A2L_VARIABLES.getKey()));
			if (variableNames.isEmpty()) {
				addVariableNamesFromEntries(variableNames, jsonMap);
			}
		} catch (final Exception e) {
			LOGGER.warn("Failed to extract protocol variable names from protocol JSON: {}", e.getMessage());
		}
		return new ArrayList<>(variableNames);
	}

	@Override
	public Class<CBabCanNode> getEntityClass() { return CBabCanNode.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CBabCanNodeInitializerService.class; }
	// IEntityRegistrable implementation

	/** Get raw protocol file content from node cache or load it on demand. */
	public String getOrLoadProtocolFileData(final CBabCanNode node) {
		Check.notNull(node, "Node cannot be null");
		return node.getProtocolFileData();
	}

	/** Get parsed protocol file JSON from node cache or load it on demand. */
	public String getOrLoadProtocolFileJson(final CBabCanNode node, @SuppressWarnings ("unused") final boolean mergeCompanionJson) {
		Check.notNull(node, "Node cannot be null");
		ensureProtocolFileLoaded(node);
		return node.getProtocolFileJson();
	}

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceCanNode.class; }

	@Override
	public Class<?> getServiceClass() { return CBabCanNodeService.class; }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		// CAN-specific initialization if needed
	}

	/** List unique protocol variable names from all CAN nodes in a project. */
	@Transactional (readOnly = true)
	public List<String> listProtocolVariableNamesByProject(final CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		final Set<String> variableNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		listByProject(project).forEach((final CBabCanNode node) -> variableNames.addAll(extractProtocolVariableNames(node.getProtocolFileJson())));
		return new ArrayList<>(variableNames);
	}

	/** Load protocol content directly from database for display dialogs.
	 * @param nodeId node id to load from DB
	 * @param field  which protocol field to return
	 * @return stored field content or null if not found */
	@Transactional (readOnly = true)
	public String loadProtocolContentFromDb(final Long nodeId, final EProtocolContentField field) {
		Check.notNull(nodeId, "Node id cannot be null");
		Check.notNull(field, "Protocol content field cannot be null");
		final CBabCanNode node = getById(nodeId).orElse(null);
		if (node == null) {
			return null;
		}
		return switch (field) {
		case RAW -> node.getProtocolFileData();
		case JSON -> node.getProtocolFileJson();
		};
	}

	/** Parse raw A2L content string and return pretty JSON output. */
	public String parseA2LContentAsJson(final String a2LContent) {
		Check.notBlank(a2LContent, "A2L content cannot be null or empty");
		final Map<String, Object> parsed = A2L_FILE_PARSER.parseContentAndSummarize(a2LContent, Set.of(), 0).jsonObject();
		try {
			return A2L_FILE_PARSER.toPrettyJson(parsed);
		} catch (final JsonProcessingException e) {
			throw new IllegalArgumentException("Failed to serialize parsed A2L JSON from in-memory content", e);
		}
	}

	/** Parse an A2L file to Derbent CAN JSON format (including summary keys). */
	public Map<String, Object> parseA2LFile(final Path a2LPath) {
		Check.notNull(a2LPath, "A2L file path cannot be null");
		ensureA2LExtension(a2LPath);
		try {
			return A2L_FILE_PARSER.parseFileAndSummarize(a2LPath, Set.of(), 0).jsonObject();
		} catch (final IOException e) {
			throw new IllegalArgumentException("Failed to parse A2L file: " + a2LPath, e);
		}
	}

	public Map<String, Object> parseA2LFile(final String a2LPath) {
		Check.notBlank(a2LPath, "A2L file path cannot be null or empty");
		return parseA2LFile(Path.of(a2LPath));
	}

	public String parseA2LFileAsJson(final Path a2LPath, final boolean mergeCompanionJson) {
		final Map<String, Object> parsed = mergeCompanionJson ? parseA2LFileWithDefaultCompanion(a2LPath) : parseA2LFile(a2LPath);
		try {
			return A2L_FILE_PARSER.toPrettyJson(parsed);
		} catch (final IOException e) {
			throw new IllegalArgumentException("Failed to serialize parsed A2L JSON for file: " + a2LPath, e);
		}
	}

	public String parseA2LFileAsJson(final String a2LPath, final boolean mergeCompanionJson) {
		Check.notBlank(a2LPath, "A2L file path cannot be null or empty");
		return parseA2LFileAsJson(Path.of(a2LPath), mergeCompanionJson);
	}

	/** Parse an A2L file and automatically merge sibling `<name>.a2l.json` when available. */
	public Map<String, Object> parseA2LFileWithDefaultCompanion(final Path a2LPath) {
		final Path companionJsonPath = getDefaultCompanionJsonPath(a2LPath);
		if (Files.exists(companionJsonPath)) {
			return parseA2LFileWithSupplement(a2LPath, companionJsonPath);
		}
		return parseA2LFile(a2LPath);
	}

	/** Parse an A2L file and merge supplemental non-A2L entries (BUFFER/DBC) from a companion JSON file. */
	public Map<String, Object> parseA2LFileWithSupplement(final Path a2LPath, final Path supplementalJsonPath) {
		Check.notNull(a2LPath, "A2L file path cannot be null");
		Check.notNull(supplementalJsonPath, "Supplemental JSON path cannot be null");
		ensureA2LExtension(a2LPath);
		try {
			final Map<String, Object> parsed = A2L_FILE_PARSER.parseFile(a2LPath, Set.of(), 0).jsonObject();
			if (Files.exists(supplementalJsonPath)) {
				final Map<String, Object> supplemental = A2L_FILE_PARSER.readJsonObject(supplementalJsonPath);
				A2L_FILE_PARSER.mergeSupplementalEntries(parsed, supplemental);
			}
			A2L_FILE_PARSER.summarizeA2LObject(parsed);
			return parsed;
		} catch (final IOException e) {
			throw new IllegalArgumentException("Failed to parse A2L file with supplemental JSON: " + a2LPath + " + " + supplementalJsonPath, e);
		}
	}

	public CProtocolFileSummary parseSummaryJson(final String summaryJson) {
		if (summaryJson == null || summaryJson.isBlank()) {
			return new CProtocolFileSummary(EProtocolSummaryStatus.NO_FILE, "No protocol file is loaded.", 0, 0, 0, 0, 0, 0L);
		}
		try {
			final Map<String, Object> jsonMap = OBJECT_MAPPER.readValue(summaryJson, JSON_MAP_TYPE);
			final EProtocolSummaryStatus status = parseStatus(String.valueOf(jsonMap.getOrDefault("status", EProtocolSummaryStatus.NO_FILE.name())));
			final String message = String.valueOf(jsonMap.getOrDefault("message", "No protocol file is loaded."));
			return new CProtocolFileSummary(status, message, toInt(jsonMap.get("loadedEntityCount")), toInt(jsonMap.get("loadedCharacteristicCount")),
					toInt(jsonMap.get("loadedMeasurementCount")), toInt(jsonMap.get("loadedCurveCount")), toInt(jsonMap.get("loadedMapCount")),
					toLong(jsonMap.get("fileSizeBytes")));
		} catch (final Exception e) {
			return new CProtocolFileSummary(EProtocolSummaryStatus.ERROR, "File parse error. Invalid summary JSON.", 0, 0, 0, 0, 0, 0L);
		}
	}

	/** Parse in-memory protocol file data and update nodeConfigJson. */
	public void updateNodeConfigJsonFromProtocolFileData(final CBabCanNode node) {
		Check.notNull(node, "Node cannot be null");
		node.setNodeConfigJson(getOrLoadProtocolFileJson(node, false));
	}

	@Override
	protected void validateEntity(final CBabCanNode entity) {
		super.validateEntity(entity); // ✅ Common node validation (name, interface, uniqueness)
		// LOGGER.debug("Validating CAN Bus specific fields: {}", entity.getName());
		// CAN-specific validation
		if (entity.getBitrate() == null) {
			throw new IllegalArgumentException("Bitrate is required");
		}
		validateNumericField(entity.getBitrate(), "Bitrate", 1000000);
		if (entity.getBitrate() < 10000) {
			throw new IllegalArgumentException("Bitrate must be at least 10000 bps");
		}
		// Error warning limit validation
		if (entity.getErrorWarningLimit() != null) {
			validateNumericField(entity.getErrorWarningLimit(), "Error Warning Limit", 255);
		}
		// LOGGER.debug("CAN Bus node validation passed: {}", entity.getName());
	}
}
