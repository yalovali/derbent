package tech.derbent.bab.policybase.node.can;

import java.time.Clock;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import tech.derbent.api.session.service.ISessionService;

class CBabCanNodeServiceSummaryTest {

	private final CBabCanNodeService service =
			new CBabCanNodeService(mock(ICanNodeRepository.class), Clock.systemUTC(), mock(ISessionService.class));

	@Test
	void createNoFileSummaryJson_shouldReturnNoFileStatus() {
		final CBabCanNodeService.CProtocolFileSummary summary = service.parseSummaryJson(service.createNoFileSummaryJson());

		assertThat(summary.status()).isEqualTo(CBabCanNodeService.EProtocolSummaryStatus.NO_FILE);
		assertThat(summary.loadedEntityCount()).isZero();
		assertThat(summary.fileSizeBytes()).isZero();
		assertThat(summary.message()).contains("No protocol file is loaded.");
	}

	@Test
	void createParsedSummaryJson_shouldExtractCountsFromParsedJson() {
		final String parsedProtocolJson = """
				{
				  "_all_a2l_variables": "VarA, VarB, VarC, VarD",
				  "_all_a2l_characteristics": "VarA,VarB",
				  "_all_a2l_measurements": "VarC",
				  "_all_a2l_curves": "VarD",
				  "_all_a2l_maps": ""
				}
				""";

		final String summaryJson = service.createParsedSummaryJson(parsedProtocolJson, 4096L);
		final CBabCanNodeService.CProtocolFileSummary summary = service.parseSummaryJson(summaryJson);

		assertThat(summary.status()).isEqualTo(CBabCanNodeService.EProtocolSummaryStatus.PARSED);
		assertThat(summary.loadedEntityCount()).isEqualTo(4);
		assertThat(summary.loadedCharacteristicCount()).isEqualTo(2);
		assertThat(summary.loadedMeasurementCount()).isEqualTo(1);
		assertThat(summary.loadedCurveCount()).isEqualTo(1);
		assertThat(summary.loadedMapCount()).isZero();
		assertThat(summary.fileSizeBytes()).isEqualTo(4096L);
		assertThat(summary.message()).contains("Loaded protocol entities: 4");
	}

	@Test
	void createParseErrorSummaryJson_shouldPreserveErrorAndFileSize() {
		final String summaryJson = service.createParseErrorSummaryJson("Unexpected token near AXIS_DESCR", 128L);
		final CBabCanNodeService.CProtocolFileSummary summary = service.parseSummaryJson(summaryJson);

		assertThat(summary.status()).isEqualTo(CBabCanNodeService.EProtocolSummaryStatus.ERROR);
		assertThat(summary.fileSizeBytes()).isEqualTo(128L);
		assertThat(summary.message()).contains("File parse error.");
		assertThat(summary.message()).contains("Unexpected token near AXIS_DESCR");
	}

	@Test
	void parseSummaryJson_shouldReturnErrorForInvalidJson() {
		final CBabCanNodeService.CProtocolFileSummary summary = service.parseSummaryJson("{\"status\":");

		assertThat(summary.status()).isEqualTo(CBabCanNodeService.EProtocolSummaryStatus.ERROR);
		assertThat(summary.message()).contains("Invalid summary JSON");
		assertThat(summary.loadedEntityCount()).isZero();
	}

	@Test
	void extractProtocolVariableNames_shouldUseSummaryCsvWhenAvailable() {
		final String protocolJson = """
				{
				  "_all_a2l_variables": "VarA, VarB , VarA,VarC"
				}
				""";

		assertThat(service.extractProtocolVariableNames(protocolJson)).containsExactly("VarA", "VarB", "VarC");
	}

	@Test
	void extractProtocolVariableNames_shouldFallbackToNameFieldsWhenSummaryIsMissing() {
		final String protocolJson = """
				{
				  "Entry1": {"Name": "VarZ"},
				  "Entry2": {"Name": "VarA"},
				  "_COMPUTE_METHODS": {}
				}
				""";

		assertThat(service.extractProtocolVariableNames(protocolJson)).containsExactly("VarA", "VarZ");
	}
}
