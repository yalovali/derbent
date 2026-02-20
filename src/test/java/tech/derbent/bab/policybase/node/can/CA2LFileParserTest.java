package tech.derbent.bab.policybase.node.can;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class CA2LFileParserTest {

	private static final Path SAMPLE_A2L_FILE = Path.of("others/protocolsamples/ECU_Variables.a2l");
	private static final Path SAMPLE_JSON_FILE = Path.of("others/protocolsamples/ECU_Variables.a2l.json");
	private final CA2LFileParser parser = new CA2LFileParser();

	@Test
	void parseFileAndSummarize_shouldProduceExpectedCoreStructure() throws Exception {
		final Map<String, Object> parsed = parser.parseFileAndSummarize(SAMPLE_A2L_FILE, Set.of(), 0).jsonObject();

		assertThat(parsed).containsKeys("product_type", "EngineHour_Ovv", "_MEMORY_REGIONS", "_COMPUTE_METHODS", "_AXIS_PTS_REF",
				"_all_a2l_variables");

		final Map<String, Object> productType = asObject(parsed.get("product_type"));
		assertThat(productType.get("FieldType")).isEqualTo("MEASUREMENT");
		assertThat(productType.get("Address")).isEqualTo("0x00080146");
		assertThat(((Number) productType.get("Index")).intValue()).isEqualTo(0);
		assertThat(productType.get("RecordType")).isEqualTo("UBYTE");

		final Map<String, Object> engineHourOvv = asObject(parsed.get("EngineHour_Ovv"));
		assertThat(engineHourOvv.get("FieldType")).isEqualTo("CHARACTERISTIC");
		assertThat(engineHourOvv.get("DataType")).isEqualTo("VALUE");
		assertThat(engineHourOvv.get("Address")).isEqualTo("0x400045A4");

		final Map<String, Object> accelPedal = asObject(parsed.get("AccrPedlSensr"));
		assertThat(accelPedal.get("AxisXType")).isEqualTo("UAccrPedl1");
		assertThat(accelPedal.get("AxisXInput")).isEqualTo("Voltage_CM");
		assertThat(accelPedal.get("AxisXConversion")).isEqualTo("2");

		final Map<String, Object> axisPtsRef = asObject(parsed.get("_AXIS_PTS_REF"));
		final Map<String, Object> axisEntry = asObject(axisPtsRef.get("DTqC_ErrNEng_A"));
		assertThat(axisEntry).containsEntry("FieldType", "AXIS_PTS");
		assertThat(axisEntry).containsEntry("RecordType", "FLOAT32_IEEE");
		assertThat(((Number) axisEntry.get("AxisNumberOfPoints0")).intValue()).isEqualTo(10);
	}

	@Test
	void mergeSupplementalEntries_shouldIncludeBufferAndCompuMethodSource3() throws Exception {
		final Map<String, Object> parsed = parser.parseFile(SAMPLE_A2L_FILE);
		final Map<String, Object> supplemental = parser.readJsonObject(SAMPLE_JSON_FILE);
		parser.mergeSupplementalEntries(parsed, supplemental);
		parser.summarizeA2LObject(parsed);

		final Map<String, Object> auxDtcCounter = asObject(parsed.get("Aux_DTC_Counter"));
		assertThat(((Number) auxDtcCounter.get("Source")).intValue()).isEqualTo(3);
		assertThat(auxDtcCounter.get("Name")).isEqualTo("DTC_Counter");

		final Map<String, Object> computeMethods = asObject(parsed.get("_COMPUTE_METHODS"));
		final Map<String, Object> ffAirPressureConversion = asObject(computeMethods.get("FF_AirPressure_Conv"));
		assertThat(((Number) ffAirPressureConversion.get("Source")).intValue()).isEqualTo(3);

		final String allBufferValues = String.valueOf(parsed.get("_all_buffer_values"));
		assertThat(allBufferValues).contains("Aux_DTC_Counter");
		assertThat(allBufferValues).contains("FFrame_FF16_Engine Hour");
	}

	@Test
	void parseAndMerge_shouldMatchProvidedSampleJsonExactly() throws Exception {
		final Map<String, Object> parsed = parser.parseFile(SAMPLE_A2L_FILE);
		final Map<String, Object> expected = parser.readJsonObject(SAMPLE_JSON_FILE);
		parser.mergeSupplementalEntries(parsed, expected);
		parser.summarizeA2LObject(parsed);

		final List<String> diffs = new ArrayList<>();
		collectDiffs("$", expected, parsed, diffs, 50);
		assertThat(diffs).withFailMessage("JSON mismatch:%n%s", String.join(System.lineSeparator(), diffs)).isEmpty();
	}

	@Test
	void parseContentAndSummarize_shouldMatchFileParsingResult() throws Exception {
		final String a2LContent = Files.readString(SAMPLE_A2L_FILE, StandardCharsets.UTF_8);
		final Map<String, Object> fromFile = parser.parseFileAndSummarize(SAMPLE_A2L_FILE, Set.of(), 0).jsonObject();
		final Map<String, Object> fromContent = parser.parseContentAndSummarize(a2LContent, Set.of(), 0).jsonObject();

		final List<String> diffs = new ArrayList<>();
		collectDiffs("$", fromFile, fromContent, diffs, 50);
		assertThat(diffs).withFailMessage("In-memory parse mismatch:%n%s", String.join(System.lineSeparator(), diffs)).isEmpty();
	}

	@SuppressWarnings ("unchecked")
	private static Map<String, Object> asObject(final Object value) {
		assertThat(value).isInstanceOf(Map.class);
		return (Map<String, Object>) value;
	}

	@SuppressWarnings ("unchecked")
	private static void collectDiffs(final String path,
			final Object expected,
			final Object actual,
			final List<String> diffs,
			final int maxDiffs) {
		if (diffs.size() >= maxDiffs) {
			return;
		}
		if ((expected == null) || (actual == null)) {
			if (expected != actual) {
				diffs.add(path + ": expected=" + expected + " actual=" + actual);
			}
			return;
		}
		if (expected instanceof Map<?, ?> expectedMap && actual instanceof Map<?, ?> actualMap) {
			for (final Map.Entry<?, ?> entry : expectedMap.entrySet()) {
				if (diffs.size() >= maxDiffs) {
					return;
				}
				final String key = String.valueOf(entry.getKey());
				if (!actualMap.containsKey(key)) {
					diffs.add(path + "." + key + ": missing in actual");
					continue;
				}
				collectDiffs(path + "." + key, entry.getValue(), actualMap.get(key), diffs, maxDiffs);
			}
			for (final Object actualKey : actualMap.keySet()) {
				if (diffs.size() >= maxDiffs) {
					return;
				}
				final String key = String.valueOf(actualKey);
				if (!expectedMap.containsKey(key)) {
					diffs.add(path + "." + key + ": unexpected in actual");
				}
			}
			return;
		}
		if (expected instanceof List<?> expectedList && actual instanceof List<?> actualList) {
			if (expectedList.size() != actualList.size()) {
				diffs.add(path + ": list size expected=" + expectedList.size() + " actual=" + actualList.size());
				return;
			}
			for (int i = 0; i < expectedList.size(); i++) {
				collectDiffs(path + "[" + i + "]", expectedList.get(i), actualList.get(i), diffs, maxDiffs);
				if (diffs.size() >= maxDiffs) {
					return;
				}
			}
			return;
		}
		if (expected instanceof Number expectedNumber && actual instanceof Number actualNumber) {
			final BigDecimal expectedDecimal = new BigDecimal(expectedNumber.toString());
			final BigDecimal actualDecimal = new BigDecimal(actualNumber.toString());
			if (expectedDecimal.compareTo(actualDecimal) != 0) {
				diffs.add(path + ": expected=" + expected + " actual=" + actual);
			}
			return;
		}
		if (!expected.equals(actual)) {
			diffs.add(path + ": expected=" + expected + " actual=" + actual);
		}
	}
}
