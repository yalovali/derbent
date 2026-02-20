package tech.derbent.bab.policybase.node.can;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import tech.derbent.api.utils.Check;

/** A2L parser compatible with the legacy C++ output structure used in BAB CAN tooling. */
public final class CA2LFileParser {

	/** A2L source tags in merged JSON. */
	public enum ESourceType {
		A2L(1),
		DBC(2),
		BUFFER(3);

		private final int code;

		ESourceType(final int code) {
			this.code = code;
		}

		public int getCode() { return code; }
	}

	/** Supported parser states. */
	public enum EParserState {
		NONE,
		BEGIN_CHARACTERISTIC,
		BEGIN_MEASUREMENT,
		BEGIN_COMPU_METHOD,
		BEGIN_MEMORY_REGION,
		BEGIN_AXIS_PTS,
		IN_AXIS_DESCR
	}

	/** Supported field types in parsed output. */
	public enum EFieldType {
		CHARACTERISTIC("CHARACTERISTIC"),
		MEASUREMENT("MEASUREMENT"),
		COMPU_METHOD("COMPU_METHOD"),
		MEMORY_REGION("MEMORY_REGION"),
		AXIS_PTS("AXIS_PTS");

		private final String value;

		EFieldType(final String value) {
			this.value = value;
		}

		public String getValue() { return value; }
	}

	/** Constants for parser directives and JSON keys. */
	public static final class CKeys {

		private CKeys() {
			// constants only
		}

		public static final String ADDRESS = "Address";
		public static final String ADDRESS_LENGTH = "AddressLength";
		public static final String ADDRESS_ORIGINAL = "Address_Original";
		public static final String ADDRESS_START = "AddressStart";
		public static final String ACCURACY = "Accuracy";
		public static final String ALL_A2L_CHARACTERISTICS = "_all_a2l_characteristics";
		public static final String ALL_A2L_CURVES = "_all_a2l_curves";
		public static final String ALL_A2L_FLOAT_VALUES = "_all_a2l_float_values";
		public static final String ALL_A2L_MAPS = "_all_a2l_maps";
		public static final String ALL_A2L_MEASUREMENTS = "_all_a2l_measurements";
		public static final String ALL_A2L_NONFLOAT_VALUES = "_all_a2l_nonfloat_values";
		public static final String ALL_A2L_SINGLE_VALUES = "_all_a2l_single_values";
		public static final String ALL_A2L_VARIABLES = "_all_a2l_variables";
		public static final String ALL_BUFFER_VALUES = "_all_buffer_values";
		public static final String ALL_DBC_VALUES = "_all_dbc_values";
		public static final String AXIS2_INPUT = "Axis2Input";
		public static final String AXIS2_LOWER_LIMIT = "Axis2LowerLimit";
		public static final String AXIS2_NUMBER_OF_POINTS = "Axis2NumberOfPoints";
		public static final String AXIS2_UPPER_LIMIT = "Axis2UpperLimit";
		public static final String AXIS_CONVERSION_0 = "AxisConversion0";
		public static final String AXIS_INPUT_0 = "AxisInput0";
		public static final String AXIS_LOWER_LIMIT_0 = "AxisLowerLimit0";
		public static final String AXIS_NUMBER_OF_POINTS_0 = "AxisNumberOfPoints0";
		public static final String AXIS_PTS_REF = "_AXIS_PTS_REF";
		public static final String AXIS_UPPER_LIMIT_0 = "AxisUpperLimit0";
		public static final String AXIS_X_AXIS_PTS = "AxisX_PTS";
		public static final String AXIS_X_BYTE_ORDER = "AxisXByteOrder";
		public static final String AXIS_X_CONVERSION = "AxisXConversion";
		public static final String AXIS_X_FORMAT = "AxisXFormat";
		public static final String AXIS_X_INPUT_QUANTITY = "AxisXInput";
		public static final String AXIS_X_LOWER_LIMIT = "AxisXLowerLimit";
		public static final String AXIS_X_NUMBER_OF_POINTS = "AxisXNumberOfPoints";
		public static final String AXIS_X_TYPE = "AxisXType";
		public static final String AXIS_X_UPPER_LIMIT = "AxisXUpperLimit";
		public static final String AXIS_Y_AXIS_PTS = "AxisY_PTS";
		public static final String AXIS_Y_BYTE_ORDER = "AxisYByteOrder";
		public static final String AXIS_Y_CONVERSION = "AxisYConversion";
		public static final String AXIS_Y_FORMAT = "AxisYFormat";
		public static final String AXIS_Y_INPUT_QUANTITY = "AxisYInput";
		public static final String AXIS_Y_LOWER_LIMIT = "AxisYLowerLimit";
		public static final String AXIS_Y_NUMBER_OF_POINTS = "AxisYNumberOfPoints";
		public static final String AXIS_Y_TYPE = "AxisYType";
		public static final String AXIS_Y_UPPER_LIMIT = "AxisYUpperLimit";
		public static final String BIT_MASK = "BIT_MASK";
		public static final String BYTE_ORDER = "BYTE_ORDER";
		public static final String COEFFICIENTS = "Coefficients";
		public static final String COMPUTE_METHODS = "_COMPUTE_METHODS";
		public static final String CONVERSION = "Conversion";
		public static final String DATA_TYPE = "DataType";
		public static final String FIELD_TYPE = "FieldType";
		public static final String FORMAT = "Format";
		public static final String IDENTIFIER = "Identifier";
		public static final String INDEX = "Index";
		public static final String LOWER_LIMIT = "LowerLimit";
		public static final String MAX_DIFF = "MaxDiff";
		public static final String MEMORY_REGIONS = "_MEMORY_REGIONS";
		public static final String NAME = "Name";
		public static final String NO_COMPU_METHOD = "NO_COMPU_METHOD";
		public static final String RECORD_TYPE = "RecordType";
		public static final String RESOLUTION = "Resolution";
		public static final String SOURCE = "Source";
		public static final String UPPER_LIMIT = "UpperLimit";
		public static final String UNITS = "Units";

		public static final String DATA_TYPE_CURVE = "CURVE";
		public static final String DATA_TYPE_MAP = "MAP";
		public static final String DATA_TYPE_VALUE = "VALUE";

		public static final String RECORD_FLOAT32_IEEE = "FLOAT32_IEEE";
		public static final String RECORD_SLONG = "SLONG";
		public static final String RECORD_UBYTE = "UBYTE";
		public static final String RECORD_ULONG = "ULONG";
		public static final String RECORD_UWORD = "UWORD";
	}

	/** Parsed result with final JSON object and next free index counter. */
	public record CA2LParseResult(Map<String, Object> jsonObject, int nextIndex) {
	}

	private static final String DIRECTIVE_BEGIN_AXIS_DESCR = "/begin AXIS_DESCR";
	private static final String DIRECTIVE_BEGIN_AXIS_PTS = "/begin AXIS_PTS";
	private static final String DIRECTIVE_BEGIN_CHARACTERISTIC = "/begin CHARACTERISTIC";
	private static final String DIRECTIVE_BEGIN_COMPU_METHOD = "/begin COMPU_METHOD";
	private static final String DIRECTIVE_BEGIN_MEASUREMENT = "/begin MEASUREMENT";
	private static final String DIRECTIVE_BEGIN_MEMORY_REGION = "/begin MEMORY_REGION";
	private static final String DIRECTIVE_END_AXIS_DESCR = "/end AXIS_DESCR";
	private static final String DIRECTIVE_END_AXIS_PTS = "/end AXIS_PTS";
	private static final String DIRECTIVE_END_CHARACTERISTIC = "/end CHARACTERISTIC";
	private static final String DIRECTIVE_END_MEASUREMENT = "/end MEASUREMENT";
	private static final String KEYWORD_AXIS_PTS_REF = "AXIS_PTS_REF";
	private static final String KEYWORD_BIT_MASK = "BIT_MASK";
	private static final String KEYWORD_BYTE_ORDER = "BYTE_ORDER";
	private static final String KEYWORD_DEPOSIT = "DEPOSIT";
	private static final String KEYWORD_ECU_ADDRESS = "ECU_ADDRESS";
	private static final String KEYWORD_FORMAT = "FORMAT";
	private static final int MAX_A2L_ENTRY = 100_000;
	private static final ObjectMapper OBJECT_MAPPER =
			new ObjectMapper().enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
	private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
	private static final Pattern VALID_ADDRESS_PATTERN = Pattern.compile("^0x[0-9A-Fa-f]+$");
	private static final Pattern VALID_HEX_PATTERN = Pattern.compile("^0x[0-9A-Fa-f]+$");
	private static final TypeReference<Map<String, Object>> JSON_MAP_TYPE = new TypeReference<Map<String, Object>>() {
	};

	private static Map<String, String> initializeTypeDB() {
		final Map<String, String> typeDB = new LinkedHashMap<>();
		typeDB.put("Lookup1D_BOOLEAN", CKeys.RECORD_UBYTE);
		typeDB.put("Lookup1D_BYTE", CKeys.RECORD_UBYTE);
		typeDB.put("Lookup1D_FLOAT32_IEEE", CKeys.RECORD_FLOAT32_IEEE);
		typeDB.put("Lookup1D_SLONG", CKeys.RECORD_SLONG);
		typeDB.put("Lookup1D_SLONG", CKeys.RECORD_ULONG); // matches legacy overwrite behavior
		typeDB.put("Lookup1D_UBYTE", CKeys.RECORD_UBYTE);
		typeDB.put("Lookup1D_ULONG", CKeys.RECORD_ULONG);
		typeDB.put("Lookup1D_UWORD", CKeys.RECORD_UWORD);
		typeDB.put("Lookup1D_WORD", CKeys.RECORD_UWORD);
		typeDB.put("Lookup1D_X_BOOLEAN", CKeys.RECORD_UBYTE);
		typeDB.put("Lookup1D_X_BYTE", CKeys.RECORD_UBYTE);
		typeDB.put("Lookup1D_X_FLOAT32_IEEE", CKeys.RECORD_FLOAT32_IEEE);
		typeDB.put("Lookup1D_X_LONG", CKeys.RECORD_ULONG);
		typeDB.put("Lookup1D_X_UBYTE", CKeys.RECORD_UBYTE);
		typeDB.put("Lookup1D_X_ULONG", CKeys.RECORD_ULONG);
		typeDB.put("Lookup1D_X_UWORD", CKeys.RECORD_UWORD);
		typeDB.put("Lookup1D_X_WORD", CKeys.RECORD_UWORD);
		typeDB.put("Lookup2D_BOOLEAN", CKeys.RECORD_UBYTE);
		typeDB.put("Lookup2D_BYTE", CKeys.RECORD_UBYTE);
		typeDB.put("Lookup2D_FLOAT32_IEEE", CKeys.RECORD_FLOAT32_IEEE);
		typeDB.put("Lookup2D_LONG", CKeys.RECORD_ULONG);
		typeDB.put("Lookup2D_UBYTE", CKeys.RECORD_UBYTE);
		typeDB.put("Lookup2D_ULONG", CKeys.RECORD_ULONG);
		typeDB.put("Lookup2D_UWORD", CKeys.RECORD_UWORD);
		typeDB.put("Lookup2D_WORD", CKeys.RECORD_UWORD);
		typeDB.put("Lookup2D_X_BOOLEAN", CKeys.RECORD_UBYTE);
		typeDB.put("Lookup2D_X_BYTE", CKeys.RECORD_UBYTE);
		typeDB.put("Lookup2D_X_FLOAT32_IEEE", CKeys.RECORD_FLOAT32_IEEE);
		typeDB.put("Lookup2D_X_LONG", CKeys.RECORD_ULONG);
		typeDB.put("Lookup2D_X_UBYTE", CKeys.RECORD_UBYTE);
		typeDB.put("Lookup2D_X_ULONG", CKeys.RECORD_ULONG);
		typeDB.put("Lookup2D_X_UWORD", CKeys.RECORD_UWORD);
		typeDB.put("Lookup2D_X_WORD", CKeys.RECORD_UWORD);
		typeDB.put("RL_X_FLOAT32_IEEE", CKeys.RECORD_FLOAT32_IEEE);
		typeDB.put("RL_X_UWORD", CKeys.RECORD_UWORD);
		typeDB.put("SBYTE", CKeys.RECORD_UBYTE);
		typeDB.put("Scalar_BOOLEAN", CKeys.RECORD_UBYTE);
		typeDB.put("Scalar_BYTE", CKeys.RECORD_UBYTE);
		typeDB.put("Scalar_FLOAT32_IEEE", CKeys.RECORD_FLOAT32_IEEE);
		typeDB.put("Scalar_LONG", CKeys.RECORD_ULONG);
		typeDB.put("Scalar_SWORD", CKeys.RECORD_UWORD);
		typeDB.put("Scalar_UBYTE", CKeys.RECORD_UBYTE);
		typeDB.put("Scalar_ULONG", CKeys.RECORD_ULONG);
		typeDB.put("Scalar_UWORD", CKeys.RECORD_UWORD);
		typeDB.put("USHORT", CKeys.RECORD_UWORD);
		typeDB.put("SWORD", CKeys.RECORD_UWORD);
		typeDB.put(CKeys.DATA_TYPE_CURVE, CKeys.DATA_TYPE_CURVE);
		typeDB.put(CKeys.RECORD_FLOAT32_IEEE, CKeys.RECORD_FLOAT32_IEEE);
		typeDB.put(CKeys.DATA_TYPE_MAP, CKeys.DATA_TYPE_MAP);
		typeDB.put(CKeys.RECORD_UBYTE, CKeys.RECORD_UBYTE);
		typeDB.put(CKeys.RECORD_ULONG, CKeys.RECORD_ULONG);
		typeDB.put(CKeys.RECORD_UWORD, CKeys.RECORD_UWORD);
		typeDB.put(CKeys.RECORD_SLONG, CKeys.RECORD_SLONG);
		return Collections.unmodifiableMap(typeDB);
	}

	private static final Map<String, String> TYPE_DB = initializeTypeDB();

	/** Parse only A2L blocks without generating summary keys. */
	public Map<String, Object> parseFile(final Path fileName) throws IOException {
		return parseFile(fileName, Set.of(), 0).jsonObject();
	}

	/** Parse A2L blocks and keep indexing compatible with legacy parser. */
	public CA2LParseResult parseFile(final Path fileName, final Set<String> invalidA2LNames, final int startIndex) throws IOException {
		Check.notNull(fileName, "A2L file path cannot be null");
		if (!Files.exists(fileName)) {
			throw new IllegalArgumentException("A2L file does not exist: " + fileName);
		}
		final List<String> lines = Files.readAllLines(fileName, StandardCharsets.UTF_8);
		return parseLines(lines, invalidA2LNames, startIndex);
	}

	/** Parse A2L content from an in-memory string without reading from filesystem. */
	public Map<String, Object> parseContent(final String a2LContent) {
		return parseContent(a2LContent, Set.of(), 0).jsonObject();
	}

	/** Parse A2L content and keep indexing compatible with legacy parser. */
	public CA2LParseResult parseContent(final String a2LContent, final Set<String> invalidA2LNames, final int startIndex) {
		Check.notNull(a2LContent, "A2L content cannot be null");
		final List<String> lines = Arrays.asList(a2LContent.split("\\R", -1));
		return parseLines(lines, invalidA2LNames, startIndex);
	}

	/** Parse A2L content from string and append summary fields expected by CAN tooling. */
	public CA2LParseResult parseContentAndSummarize(final String a2LContent, final Set<String> invalidA2LNames, final int startIndex) {
		final CA2LParseResult result = parseContent(a2LContent, invalidA2LNames, startIndex);
		summarizeA2LObject(result.jsonObject());
		return result;
	}

	private CA2LParseResult parseLines(final List<String> lines, final Set<String> invalidA2LNames, final int startIndex) {
		Check.notNull(lines, "A2L lines cannot be null");
		final CTokenReader reader = new CTokenReader(lines);
		final CParserContext context = new CParserContext(new LinkedHashMap<>(), invalidA2LNames, startIndex);
		while (reader.hasNext()) {
			final CToken token = reader.readNextToken();
			if ((token == null) || token.token().isBlank()) {
				continue;
			}
			switch (context.state) {
				case NONE -> parseStateNone(context, reader, token);
				case BEGIN_CHARACTERISTIC, BEGIN_MEASUREMENT -> parseStateCharacteristicOrMeasurement(context, reader, token);
				case IN_AXIS_DESCR -> parseStateAxisDescr(context, token);
				case BEGIN_AXIS_PTS -> parseStateAxisPts(context, token);
				default -> {
					// guarded by parser transitions
				}
			}
		}
		return new CA2LParseResult(context.rootJson, context.indexCounter);
	}

	/** Parse and append summary fields expected by CAN tooling. */
	public CA2LParseResult parseFileAndSummarize(final Path fileName, final Set<String> invalidA2LNames, final int startIndex) throws IOException {
		final CA2LParseResult result = parseFile(fileName, invalidA2LNames, startIndex);
		summarizeA2LObject(result.jsonObject());
		return result;
	}

	/** Merge supplemental non-A2L entries (for example BUFFER/DBC objects) into already parsed A2L data. */
	public void mergeSupplementalEntries(final Map<String, Object> a2L, final Map<String, Object> supplemental) {
		Check.notNull(a2L, "Target A2L object cannot be null");
		if ((supplemental == null) || supplemental.isEmpty()) {
			return;
		}

		final Map<String, Object> targetComputeMethods = getOrCreateObject(a2L, CKeys.COMPUTE_METHODS);
		final Map<String, Object> sourceComputeMethods = asObject(supplemental.get(CKeys.COMPUTE_METHODS));

		if (sourceComputeMethods != null) {
			for (final Map.Entry<String, Object> entry : sourceComputeMethods.entrySet()) {
				final Map<String, Object> method = asObject(entry.getValue());
				if (method == null) {
					continue;
				}
				final int sourceType = readSourceType(method);
				if (sourceType != ESourceType.A2L.getCode()) {
					targetComputeMethods.put(entry.getKey(), deepCopyMap(method));
				}
			}
		}

		for (final Map.Entry<String, Object> entry : supplemental.entrySet()) {
			final String key = entry.getKey();
			if (isMetaKey(key) || CKeys.COMPUTE_METHODS.equals(key) || CKeys.MEMORY_REGIONS.equals(key) || CKeys.AXIS_PTS_REF.equals(key)) {
				continue;
			}
			final Map<String, Object> value = asObject(entry.getValue());
			if (value == null) {
				continue;
			}
			final int sourceType = readSourceType(value);
			if (sourceType == ESourceType.A2L.getCode()) {
				continue;
			}
			a2L.put(key, deepCopyMap(value));
		}
	}

	/** Read JSON object from path as mutable map. */
	public Map<String, Object> readJsonObject(final Path filePath) throws IOException {
		Check.notNull(filePath, "JSON file path cannot be null");
		if (!Files.exists(filePath)) {
			throw new IllegalArgumentException("JSON file does not exist: " + filePath);
		}
		final Map<String, Object> parsed = OBJECT_MAPPER.readValue(filePath.toFile(), JSON_MAP_TYPE);
		return deepCopyMap(parsed);
	}

	/** Convert parsed object to pretty JSON with deterministic key order. */
	public String toPrettyJson(final Map<String, Object> jsonObject) throws JsonProcessingException {
		Check.notNull(jsonObject, "JSON object cannot be null");
		return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
	}

	/** Equivalent of legacy summarizeA2LObject in C++ parser. */
	public void summarizeA2LObject(final Map<String, Object> a2L) {
		Check.notNull(a2L, "A2L object cannot be null");

		final List<String> allA2LVariables = new ArrayList<>();
		final List<String> allA2LCurves = new ArrayList<>();
		final List<String> allA2LMaps = new ArrayList<>();
		final List<String> allA2LSingleValues = new ArrayList<>();
		final List<String> allA2LNonFloatValues = new ArrayList<>();
		final List<String> allA2LFloatValues = new ArrayList<>();
		final List<String> allA2LMeasurements = new ArrayList<>();
		final List<String> allA2LCharacteristics = new ArrayList<>();
		final List<String> allDBCVariables = new ArrayList<>();
		final List<String> allBufferVariables = new ArrayList<>();

		final Set<String> skipKeys = Set.of(CKeys.MEMORY_REGIONS, CKeys.COMPUTE_METHODS, CKeys.AXIS_PTS_REF);
		for (final String key : a2L.keySet()) {
			if (skipKeys.contains(key)) {
				continue;
			}
			final Map<String, Object> json = asObject(a2L.get(key));
			if (json == null) {
				continue;
			}

			final String fieldType = asString(json.get(CKeys.FIELD_TYPE));
			final String dataType = asString(json.get(CKeys.DATA_TYPE));
			final String recordType = asString(json.get(CKeys.RECORD_TYPE));
			final int sourceType = readSourceType(json);

			if (sourceType == ESourceType.DBC.getCode()) {
				allDBCVariables.add(key);
			} else if (sourceType == ESourceType.BUFFER.getCode()) {
				allBufferVariables.add(key);
			}

			if (EFieldType.CHARACTERISTIC.getValue().equals(fieldType)) {
				allA2LVariables.add(key);
				allA2LCharacteristics.add(key);
			} else if (EFieldType.MEASUREMENT.getValue().equals(fieldType)) {
				allA2LVariables.add(key);
				allA2LMeasurements.add(key);
			} else {
				continue;
			}

			if (CKeys.DATA_TYPE_MAP.equals(dataType)) {
				allA2LMaps.add(key);
			} else if (CKeys.DATA_TYPE_CURVE.equals(dataType)) {
				allA2LCurves.add(key);
			} else if (CKeys.DATA_TYPE_VALUE.equals(dataType) || EFieldType.MEASUREMENT.getValue().equals(fieldType)) {
				allA2LSingleValues.add(key);
			}

			final boolean isMapOrCurve = CKeys.DATA_TYPE_MAP.equals(dataType) || CKeys.DATA_TYPE_CURVE.equals(dataType);
			if (isMapOrCurve) {
				continue;
			}
			if (isNonFloatRecord(recordType)) {
				allA2LNonFloatValues.add(key);
			} else if (CKeys.RECORD_FLOAT32_IEEE.equals(recordType)) {
				allA2LFloatValues.add(key);
			}
		}

		sortCaseInsensitive(allA2LVariables);
		sortCaseInsensitive(allA2LCurves);
		sortCaseInsensitive(allA2LMaps);
		sortCaseInsensitive(allA2LSingleValues);
		sortCaseInsensitive(allA2LNonFloatValues);
		sortCaseInsensitive(allA2LFloatValues);
		sortCaseInsensitive(allA2LMeasurements);
		sortCaseInsensitive(allA2LCharacteristics);
		sortCaseInsensitive(allDBCVariables);
		sortCaseInsensitive(allBufferVariables);

		allDBCVariables.remove("DBC__all_pgns_by_address");
		allDBCVariables.remove("_all_pgns_by_address");

		a2L.put(CKeys.ALL_A2L_VARIABLES, String.join(",", allA2LVariables));
		a2L.put(CKeys.ALL_A2L_CURVES, String.join(",", allA2LCurves));
		a2L.put(CKeys.ALL_A2L_MAPS, String.join(",", allA2LMaps));
		a2L.put(CKeys.ALL_A2L_SINGLE_VALUES, String.join(",", allA2LSingleValues));
		a2L.put(CKeys.ALL_A2L_NONFLOAT_VALUES, String.join(",", allA2LNonFloatValues));
		a2L.put(CKeys.ALL_A2L_FLOAT_VALUES, String.join(",", allA2LFloatValues));
		a2L.put(CKeys.ALL_A2L_MEASUREMENTS, String.join(",", allA2LMeasurements));
		a2L.put(CKeys.ALL_A2L_CHARACTERISTICS, String.join(",", allA2LCharacteristics));
		a2L.put(CKeys.ALL_DBC_VALUES, String.join(",", allDBCVariables));
		a2L.put(CKeys.ALL_BUFFER_VALUES, String.join(",", allBufferVariables));
	}

	private static boolean isNonFloatRecord(final String recordType) {
		return CKeys.RECORD_UWORD.equals(recordType) || CKeys.RECORD_UBYTE.equals(recordType) || CKeys.RECORD_ULONG.equals(recordType)
				|| CKeys.RECORD_SLONG.equals(recordType) || "Scalar_BOOLEAN".equals(recordType);
	}

	private static void sortCaseInsensitive(final List<String> list) {
		list.sort(Comparator.comparing(v -> v.toLowerCase(Locale.ROOT)));
	}

	private static void parseStateNone(final CParserContext context, final CTokenReader reader, final CToken token) {
		final String line = token.token();
		if (containsIgnoreCase(line, DIRECTIVE_BEGIN_CHARACTERISTIC)) {
			parseCharacteristic(context, reader, token);
		} else if (containsIgnoreCase(line, DIRECTIVE_BEGIN_MEASUREMENT)) {
			parseMeasurement(context, reader, token);
		} else if (containsIgnoreCase(line, DIRECTIVE_BEGIN_COMPU_METHOD)) {
			parseComputeMethod(context, reader, token);
		} else if (containsIgnoreCase(line, DIRECTIVE_BEGIN_MEMORY_REGION)) {
			parseMemoryRegion(context, reader, token);
		} else if (containsIgnoreCase(line, DIRECTIVE_BEGIN_AXIS_PTS)) {
			parseAxisPtsReference(context, reader, token);
		}
	}

	private static void parseCharacteristic(final CParserContext context, final CTokenReader reader, final CToken beginToken) {
		final Map<String, Object> json = new LinkedHashMap<>();
		json.put(CKeys.SOURCE, ESourceType.A2L.getCode());
		json.put(CKeys.FIELD_TYPE, EFieldType.CHARACTERISTIC.getValue());
		context.state = EParserState.BEGIN_CHARACTERISTIC;

		String name = valueAfterDirective(beginToken.token(), DIRECTIVE_BEGIN_CHARACTERISTIC);
		if (name.isEmpty()) {
			name = readRequiredToken(reader, "Missing characteristic name", beginToken.lineNumber());
		}
		json.put(CKeys.NAME, name);

		json.put(CKeys.IDENTIFIER, readRequiredToken(reader, "Missing characteristic identifier", beginToken.lineNumber()));
		json.put(CKeys.DATA_TYPE, readRequiredToken(reader, "Missing characteristic data type", beginToken.lineNumber()));

		final CToken addressToken = readRequired(reader, "Missing characteristic address", beginToken.lineNumber());
		final String address = addressToken.token();
		validateAddress(address, addressToken.lineNumber(), "characteristic");
		json.put(CKeys.ADDRESS, address);
		json.put(CKeys.INDEX, context.indexCounter++);
		json.put(CKeys.ADDRESS_ORIGINAL, extractAddressOriginal(addressToken.raw(), address));

		final CToken recordLayoutToken = readRequired(reader, "Missing characteristic record layout", beginToken.lineNumber());
		json.put(CKeys.RECORD_TYPE, resolveRecordType(recordLayoutToken.token(), recordLayoutToken.lineNumber()));

		json.put(CKeys.MAX_DIFF, readRequiredToken(reader, "Missing characteristic max diff", beginToken.lineNumber()));
		json.put(CKeys.CONVERSION, readRequiredToken(reader, "Missing characteristic conversion", beginToken.lineNumber()));
		json.put(CKeys.LOWER_LIMIT, parseNumber(readRequiredToken(reader, "Missing characteristic lower limit", beginToken.lineNumber())));
		json.put(CKeys.UPPER_LIMIT, parseNumber(readRequiredToken(reader, "Missing characteristic upper limit", beginToken.lineNumber())));
		context.current = json;
	}

	private static void parseMeasurement(final CParserContext context, final CTokenReader reader, final CToken beginToken) {
		final Map<String, Object> json = new LinkedHashMap<>();
		json.put(CKeys.SOURCE, ESourceType.A2L.getCode());
		json.put(CKeys.FIELD_TYPE, EFieldType.MEASUREMENT.getValue());
		context.state = EParserState.BEGIN_MEASUREMENT;

		String name = valueAfterDirective(beginToken.token(), DIRECTIVE_BEGIN_MEASUREMENT);
		if (name.isEmpty()) {
			name = readRequiredToken(reader, "Missing measurement name", beginToken.lineNumber());
		}
		json.put(CKeys.NAME, name);
		json.put(CKeys.IDENTIFIER, readRequiredToken(reader, "Missing measurement identifier", beginToken.lineNumber()));
		final CToken recordLayoutToken = readRequired(reader, "Missing measurement record layout", beginToken.lineNumber());
		json.put(CKeys.RECORD_TYPE, resolveRecordType(recordLayoutToken.token(), recordLayoutToken.lineNumber()));
		json.put(CKeys.CONVERSION, readRequiredToken(reader, "Missing measurement conversion", beginToken.lineNumber()));
		json.put(CKeys.RESOLUTION, readRequiredToken(reader, "Missing measurement resolution", beginToken.lineNumber()));
		json.put(CKeys.ACCURACY, readRequiredToken(reader, "Missing measurement accuracy", beginToken.lineNumber()));
		json.put(CKeys.LOWER_LIMIT, parseNumber(readRequiredToken(reader, "Missing measurement lower limit", beginToken.lineNumber())));
		json.put(CKeys.UPPER_LIMIT, parseNumber(readRequiredToken(reader, "Missing measurement upper limit", beginToken.lineNumber())));
		context.current = json;
	}

	private static void parseComputeMethod(final CParserContext context, final CTokenReader reader, final CToken beginToken) {
		final Map<String, Object> json = new LinkedHashMap<>();
		json.put(CKeys.SOURCE, ESourceType.A2L.getCode());
		json.put(CKeys.FIELD_TYPE, EFieldType.COMPU_METHOD.getValue());
		context.state = EParserState.BEGIN_COMPU_METHOD;

		String name = valueAfterDirective(beginToken.token(), DIRECTIVE_BEGIN_COMPU_METHOD);
		if (name.isEmpty()) {
			name = readRequiredToken(reader, "Missing compu method name", beginToken.lineNumber());
		}
		json.put(CKeys.NAME, name);
		json.put(CKeys.IDENTIFIER, readRequiredToken(reader, "Missing compu method identifier", beginToken.lineNumber()));
		json.put(CKeys.DATA_TYPE, readRequiredToken(reader, "Missing compu method data type", beginToken.lineNumber()));
		json.put(CKeys.FORMAT, readRequiredToken(reader, "Missing compu method format", beginToken.lineNumber()));
		json.put(CKeys.UNITS, readRequiredToken(reader, "Missing compu method units", beginToken.lineNumber()));
		json.put(CKeys.COEFFICIENTS, readRequiredToken(reader, "Missing compu method coefficients", beginToken.lineNumber()));

		final Map<String, Object> computeMethods = getOrCreateObject(context.rootJson, CKeys.COMPUTE_METHODS);
		computeMethods.put(name, json);

		// consume "/end COMPU_METHOD"
		readRequiredToken(reader, "Missing /end COMPU_METHOD", beginToken.lineNumber());
		context.state = EParserState.NONE;
		context.current = null;
	}

	private static void parseMemoryRegion(final CParserContext context, final CTokenReader reader, final CToken beginToken) {
		context.state = EParserState.BEGIN_MEMORY_REGION;
		final CToken valueToken = readRequired(reader, "Missing memory region content", beginToken.lineNumber());
		final String[] sections = WHITESPACE_PATTERN.split(valueToken.token().trim());
		if (sections.length != 5) {
			context.state = EParserState.NONE;
			context.current = null;
			return;
		}
		final String name = sections[0];
		final String virtualAddress = sections[1];
		final String physicalAddress = sections[2];
		final String size = sections[3];

		validateHex(virtualAddress, valueToken.lineNumber(), "memory region virtual address");
		validateHex(physicalAddress, valueToken.lineNumber(), "memory region physical address");
		validateHex(size, valueToken.lineNumber(), "memory region size");

		final Map<String, Object> json = new LinkedHashMap<>();
		json.put(CKeys.SOURCE, ESourceType.A2L.getCode());
		json.put(CKeys.FIELD_TYPE, EFieldType.MEMORY_REGION.getValue());
		json.put(CKeys.NAME, name);
		json.put(CKeys.ADDRESS_START, virtualAddress);
		json.put(CKeys.ADDRESS_LENGTH, physicalAddress);
		json.put(CKeys.FORMAT, size);

		final Map<String, Object> memoryRegions = getOrCreateObject(context.rootJson, CKeys.MEMORY_REGIONS);
		memoryRegions.put(name, json);

		// consume "/end MEMORY_REGION"
		readRequiredToken(reader, "Missing /end MEMORY_REGION", beginToken.lineNumber());
		context.state = EParserState.NONE;
		context.current = null;
	}

	private static void parseAxisPtsReference(final CParserContext context, final CTokenReader reader, final CToken beginToken) {
		final Map<String, Object> json = new LinkedHashMap<>();
		json.put(CKeys.SOURCE, ESourceType.A2L.getCode());
		json.put(CKeys.FIELD_TYPE, EFieldType.AXIS_PTS.getValue());
		context.state = EParserState.BEGIN_AXIS_PTS;

		String name = valueAfterDirective(beginToken.token(), DIRECTIVE_BEGIN_AXIS_PTS);
		if (name.isEmpty()) {
			name = readRequiredToken(reader, "Missing AXIS_PTS name", beginToken.lineNumber());
		}
		json.put(CKeys.NAME, name);
		json.put(CKeys.IDENTIFIER, readRequiredToken(reader, "Missing AXIS_PTS identifier", beginToken.lineNumber()));

		final CToken addressToken = readRequired(reader, "Missing AXIS_PTS address", beginToken.lineNumber());
		final String address = addressToken.token();
		validateAddress(address, addressToken.lineNumber(), "axis pts");
		json.put(CKeys.ADDRESS, address);
		json.put(CKeys.ADDRESS_ORIGINAL, extractAddressOriginal(addressToken.raw(), address));

		json.put(CKeys.AXIS_INPUT_0, readRequiredToken(reader, "Missing AXIS_PTS input quantity", beginToken.lineNumber()));
		final CToken layoutToken = readRequired(reader, "Missing AXIS_PTS record layout", beginToken.lineNumber());
		json.put(CKeys.RECORD_TYPE, resolveRecordType(layoutToken.token(), layoutToken.lineNumber()));
		json.put(CKeys.MAX_DIFF, parseNumber(readRequiredToken(reader, "Missing AXIS_PTS max diff", beginToken.lineNumber())));
		json.put(CKeys.AXIS_CONVERSION_0, readRequiredToken(reader, "Missing AXIS_PTS conversion", beginToken.lineNumber()));
		json.put(CKeys.AXIS_NUMBER_OF_POINTS_0,
				parseInteger(readRequiredToken(reader, "Missing AXIS_PTS number of points", beginToken.lineNumber())));
		json.put(CKeys.AXIS_LOWER_LIMIT_0, parseNumber(readRequiredToken(reader, "Missing AXIS_PTS lower limit", beginToken.lineNumber())));
		json.put(CKeys.AXIS_UPPER_LIMIT_0, parseNumber(readRequiredToken(reader, "Missing AXIS_PTS upper limit", beginToken.lineNumber())));
		context.current = json;
	}

	private static void parseStateCharacteristicOrMeasurement(final CParserContext context, final CTokenReader reader, final CToken token) {
		final String line = token.token();
		final Map<String, Object> json = context.current;
		if (json == null) {
			context.state = EParserState.NONE;
			return;
		}

		if (containsIgnoreCase(line, DIRECTIVE_END_CHARACTERISTIC) || containsIgnoreCase(line, DIRECTIVE_END_MEASUREMENT)) {
			context.state = EParserState.NONE;
			final String name = asString(json.get(CKeys.NAME));
			if (!name.isBlank() && !context.invalidA2LNames.contains(name)) {
				context.a2LCounter++;
				if (context.a2LCounter > MAX_A2L_ENTRY) {
					throw new IllegalArgumentException("A2L file contains more than allowed entries: " + MAX_A2L_ENTRY);
				}
				context.rootJson.put(name, json);
			}
			context.firstAxisReference = true;
			context.current = null;
			return;
		}

		if (startsWithKeyword(line, KEYWORD_ECU_ADDRESS)) {
			final String address = stripOuterQuotes(valueAfterKeyword(line, KEYWORD_ECU_ADDRESS));
			validateAddress(address, token.lineNumber(), "ECU_ADDRESS");
			json.put(CKeys.ADDRESS, address);
			json.put(CKeys.INDEX, context.indexCounter++);
		} else if (startsWithKeyword(line, KEYWORD_FORMAT)) {
			json.put(CKeys.FORMAT, stripBoundaryQuotes(valueAfterKeyword(line, KEYWORD_FORMAT)));
		} else if (startsWithKeyword(line, KEYWORD_BYTE_ORDER)) {
			json.put(CKeys.BYTE_ORDER, valueAfterKeyword(line, KEYWORD_BYTE_ORDER));
		} else if (startsWithKeyword(line, KEYWORD_BIT_MASK)) {
			json.put(CKeys.BIT_MASK, valueAfterKeyword(line, KEYWORD_BIT_MASK));
		} else if (startsWithKeyword(line, DIRECTIVE_BEGIN_AXIS_DESCR)) {
			context.previousState = context.state;
			context.state = EParserState.IN_AXIS_DESCR;
			readRequiredToken(reader, "Missing AXIS_DESCR line", token.lineNumber()); // legacy unconditional skip
			json.put(context.firstAxisReference ? CKeys.AXIS_X_TYPE : CKeys.AXIS_Y_TYPE,
					readRequiredToken(reader, "Missing AXIS_DESCR axis type", token.lineNumber()));
			json.put(context.firstAxisReference ? CKeys.AXIS_X_INPUT_QUANTITY : CKeys.AXIS2_INPUT,
					readRequiredToken(reader, "Missing AXIS_DESCR input quantity", token.lineNumber()));
			json.put(context.firstAxisReference ? CKeys.AXIS_X_CONVERSION : CKeys.AXIS_Y_CONVERSION,
					readRequiredToken(reader, "Missing AXIS_DESCR conversion", token.lineNumber()));
			json.put(context.firstAxisReference ? CKeys.AXIS_X_NUMBER_OF_POINTS : CKeys.AXIS2_NUMBER_OF_POINTS,
					parseInteger(readRequiredToken(reader, "Missing AXIS_DESCR number of points", token.lineNumber())));
			json.put(context.firstAxisReference ? CKeys.AXIS_X_LOWER_LIMIT : CKeys.AXIS2_LOWER_LIMIT,
					readRequiredToken(reader, "Missing AXIS_DESCR lower limit", token.lineNumber()));
			json.put(context.firstAxisReference ? CKeys.AXIS_X_UPPER_LIMIT : CKeys.AXIS2_UPPER_LIMIT,
					readRequiredToken(reader, "Missing AXIS_DESCR upper limit", token.lineNumber()));
		}
	}

	private static void parseStateAxisDescr(final CParserContext context, final CToken token) {
		final Map<String, Object> json = context.current;
		if (json == null) {
			context.state = context.previousState;
			return;
		}
		final String line = token.token();
		if (containsIgnoreCase(line, DIRECTIVE_END_AXIS_DESCR)) {
			context.firstAxisReference = false;
			context.state = context.previousState;
			return;
		}
		if (startsWithKeyword(line, KEYWORD_FORMAT)) {
			final String key = context.firstAxisReference ? CKeys.AXIS_X_FORMAT : CKeys.AXIS_Y_FORMAT;
			json.put(key, stripBoundaryQuotes(valueAfterKeyword(line, KEYWORD_FORMAT)));
		} else if (startsWithKeyword(line, KEYWORD_BYTE_ORDER)) {
			final String key = context.firstAxisReference ? CKeys.AXIS_X_BYTE_ORDER : CKeys.AXIS_Y_BYTE_ORDER;
			json.put(key, valueAfterKeyword(line, KEYWORD_BYTE_ORDER));
		} else if (startsWithKeyword(line, KEYWORD_AXIS_PTS_REF)) {
			final String key = context.firstAxisReference ? CKeys.AXIS_X_AXIS_PTS : CKeys.AXIS_Y_AXIS_PTS;
			json.put(key, valueAfterKeyword(line, KEYWORD_AXIS_PTS_REF));
		}
	}

	private static void parseStateAxisPts(final CParserContext context, final CToken token) {
		final Map<String, Object> json = context.current;
		if (json == null) {
			context.state = EParserState.NONE;
			return;
		}
		final String line = token.token();
		if (containsIgnoreCase(line, DIRECTIVE_END_AXIS_PTS)) {
			final String name = asString(json.get(CKeys.NAME));
			if (!name.isBlank()) {
				final Map<String, Object> axisRefs = getOrCreateObject(context.rootJson, CKeys.AXIS_PTS_REF);
				axisRefs.put(name, json);
			}
			context.state = EParserState.NONE;
			context.current = null;
			return;
		}
		if (startsWithKeyword(line, KEYWORD_FORMAT)) {
			json.put(CKeys.FORMAT, stripBoundaryQuotes(valueAfterKeyword(line, KEYWORD_FORMAT)));
		} else if (startsWithKeyword(line, KEYWORD_BYTE_ORDER) || startsWithKeyword(line, KEYWORD_DEPOSIT)) {
			// intentionally ignored to match legacy parser behavior
		}
	}

	private static String resolveRecordType(final String rawType, final int lineNumber) {
		final String mapped = TYPE_DB.get(rawType);
		if ((mapped == null) || mapped.isBlank()) {
			throw new IllegalArgumentException("Unsupported A2L type at line " + lineNumber + ": " + rawType);
		}
		return mapped;
	}

	private static void validateAddress(final String address, final int lineNumber, final String context) {
		if (!VALID_ADDRESS_PATTERN.matcher(address).matches()) {
			throw new IllegalArgumentException("Invalid address at line " + lineNumber + " (" + context + "): " + address);
		}
	}

	private static void validateHex(final String value, final int lineNumber, final String context) {
		if (!VALID_HEX_PATTERN.matcher(value).matches()) {
			throw new IllegalArgumentException("Invalid hex value at line " + lineNumber + " (" + context + "): " + value);
		}
	}

	private static Number parseNumber(final String token) {
		final String value = token.trim();
		try {
			final BigDecimal decimal = new BigDecimal(value);
			final BigDecimal normalized = decimal.stripTrailingZeros();
			if (normalized.scale() <= 0) {
				try {
					return normalized.intValueExact();
				} catch (final ArithmeticException ignored) {
					return normalized.longValueExact();
				}
			}
			return decimal.doubleValue();
		} catch (final NumberFormatException ex) {
			throw new IllegalArgumentException("Cannot parse numeric value: " + token, ex);
		}
	}

	private static int parseInteger(final String token) {
		final String value = token.trim();
		try {
			return Integer.parseInt(value);
		} catch (final NumberFormatException ex) {
			throw new IllegalArgumentException("Cannot parse integer value: " + token, ex);
		}
	}

	private static String valueAfterDirective(final String token, final String directive) {
		final String value = token.substring(Math.min(token.length(), directive.length())).trim();
		return stripOuterQuotes(value);
	}

	private static boolean containsIgnoreCase(final String source, final String token) {
		return source.toLowerCase(Locale.ROOT).contains(token.toLowerCase(Locale.ROOT));
	}

	private static boolean startsWithKeyword(final String source, final String keyword) {
		if (source.length() < keyword.length()) {
			return false;
		}
		if (!source.regionMatches(true, 0, keyword, 0, keyword.length())) {
			return false;
		}
		return source.length() == keyword.length() || Character.isWhitespace(source.charAt(keyword.length()));
	}

	private static String valueAfterKeyword(final String source, final String keyword) {
		if (source.length() <= keyword.length()) {
			return "";
		}
		return source.substring(keyword.length()).trim();
	}

	private static String readRequiredToken(final CTokenReader reader, final String errorMessage, final int originLine) {
		return readRequired(reader, errorMessage, originLine).token();
	}

	private static CToken readRequired(final CTokenReader reader, final String errorMessage, final int originLine) {
		final CToken token = reader.readNextToken();
		if (token == null) {
			throw new IllegalArgumentException(errorMessage + " (origin line " + originLine + ")");
		}
		return token;
	}

	private static String extractAddressOriginal(final String rawLine, final String address) {
		String normalized = rawLine == null ? "" : rawLine;
		normalized = normalized.replace(" " + address + " ", " ");
		normalized = normalized.replace("/*", "");
		normalized = normalized.replace("*/", "");
		normalized = normalized.trim();
		if (normalized.regionMatches(true, 0, "Address", 0, "Address".length())) {
			normalized = normalized.substring("Address".length()).trim();
		} else if (normalized.regionMatches(true, 0, "ECU Address", 0, "ECU Address".length())) {
			final String suffix = normalized.substring("ECU Address".length()).trim();
			normalized = "ECU Address  " + suffix;
		}
		return normalized;
	}

	private static String normalizeSpaces(final String value) {
		return WHITESPACE_PATTERN.matcher(value.trim().replace('\t', ' ')).replaceAll(" ");
	}

	private static String stripOuterQuotes(final String value) {
		if (value == null) {
			return "";
		}
		final String trimmed = value.trim();
		if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
			return trimmed.substring(1, trimmed.length() - 1);
		}
		return trimmed;
	}

	private static String stripBoundaryQuotes(final String value) {
		if (value == null) {
			return "";
		}
		String trimmed = value.trim();
		if (!trimmed.isEmpty() && trimmed.charAt(0) == '"') {
			trimmed = trimmed.substring(1);
		}
		if (!trimmed.isEmpty() && trimmed.charAt(trimmed.length() - 1) == '"') {
			trimmed = trimmed.substring(0, trimmed.length() - 1);
		}
		return trimmed;
	}

	private static String asString(final Object value) {
		return value == null ? "" : String.valueOf(value);
	}

	@SuppressWarnings ("unchecked")
	private static Map<String, Object> asObject(final Object value) {
		if (value instanceof Map<?, ?> map) {
			return (Map<String, Object>) map;
		}
		return null;
	}

	private static int readSourceType(final Map<String, Object> json) {
		final Object source = json.get(CKeys.SOURCE);
		if (source instanceof Number number) {
			return number.intValue();
		}
		if (source instanceof String str) {
			try {
				return Integer.parseInt(str);
			} catch (final NumberFormatException ignored) {
				return ESourceType.A2L.getCode();
			}
		}
		return ESourceType.A2L.getCode();
	}

	private static boolean isMetaKey(final String key) {
		return (key != null) && key.startsWith("_");
	}

	private static Map<String, Object> getOrCreateObject(final Map<String, Object> object, final String key) {
		final Map<String, Object> existing = asObject(object.get(key));
		if (existing != null) {
			return existing;
		}
		final Map<String, Object> created = new LinkedHashMap<>();
		object.put(key, created);
		return created;
	}

	private static Map<String, Object> deepCopyMap(final Map<String, Object> source) {
		final Map<String, Object> copy = new LinkedHashMap<>();
		for (final Map.Entry<String, Object> entry : source.entrySet()) {
			copy.put(entry.getKey(), deepCopyValue(entry.getValue()));
		}
		return copy;
	}

	@SuppressWarnings ("unchecked")
	private static Object deepCopyValue(final Object value) {
		if (value instanceof Map<?, ?> map) {
			return deepCopyMap((Map<String, Object>) map);
		}
		if (value instanceof List<?> list) {
			final List<Object> copied = new ArrayList<>(list.size());
			for (final Object item : list) {
				copied.add(deepCopyValue(item));
			}
			return copied;
		}
		return value;
	}

	private static final class CParserContext {

		private int a2LCounter;
		private Map<String, Object> current;
		private boolean firstAxisReference = true;
		private int indexCounter;
		private final Set<String> invalidA2LNames;
		private EParserState previousState = EParserState.NONE;
		private final Map<String, Object> rootJson;
		private EParserState state = EParserState.NONE;

		private CParserContext(final Map<String, Object> rootJson, final Set<String> invalidA2LNames, final int startIndex) {
			this.rootJson = Objects.requireNonNull(rootJson, "rootJson");
			this.invalidA2LNames = Optional.ofNullable(invalidA2LNames).orElse(Set.of());
			indexCounter = Math.max(startIndex, 0);
		}
	}

	private record CToken(String token, String raw, int lineNumber) {
	}

	private static final class CTokenReader {

		private int currentLineIndex;
		private final List<String> lines;

		private CTokenReader(final List<String> lines) {
			this.lines = lines;
		}

		private boolean hasNext() {
			return currentLineIndex < lines.size();
		}

		private CToken readNextToken() {
			while (hasNext()) {
				currentLineIndex++;
				final String raw = lines.get(currentLineIndex - 1);
				final String token = extractToken(raw);
				if (token == null) {
					continue;
				}
				return new CToken(token, raw, currentLineIndex);
			}
			return null;
		}

		private static String extractToken(final String rawLine) {
			final String trimmed = rawLine == null ? "" : rawLine.trim();
			if (trimmed.isEmpty() || trimmed.startsWith("//")) {
				return null;
			}

			String value = trimmed;
			if (value.startsWith("/*")) {
				final int endIndex = value.indexOf("*/");
				if (endIndex < 0) {
					return null;
				}
				value = value.substring(endIndex + 2).trim();
			} else {
				final int commentStart = value.indexOf("/*");
				if (commentStart > -1) {
					value = value.substring(0, commentStart).trim();
				}
			}
			final int trailingCommentStart = value.indexOf("/*");
			if (trailingCommentStart > -1) {
				value = value.substring(0, trailingCommentStart).trim();
			}
			final int cxxCommentStart = value.indexOf("//");
			if (cxxCommentStart > -1) {
				value = value.substring(0, cxxCommentStart).trim();
			}

			if (value.isEmpty()) {
				return "";
			}
			return normalizeToken(value);
		}

		private static String normalizeToken(final String value) {
			final String normalized = normalizeSpaces(value);
			if (normalized.isEmpty()) {
				return "";
			}
			int from = 0;
			int to = normalized.length();
			if (normalized.charAt(0) == '"') {
				from++;
			}
			if (to > from && normalized.charAt(to - 1) == '"') {
				to--;
			}
			return normalized.substring(from, to);
		}
	}
}
