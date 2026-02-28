package tech.derbent.bab.policybase.actionmask.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.List;
import org.junit.jupiter.api.Test;

class COutputActionMappingsJsonConverterTest {

	private final COutputActionMappingsJsonConverter converter = new COutputActionMappingsJsonConverter();

	@Test
	void convertToDatabaseColumn_shouldSerializeMappingsAsJson() {
		final List<ROutputActionMapping> mappings =
				List.of(new ROutputActionMapping("EngineSpeed", "int", "TargetSpeed", "int"));

		final String json = converter.convertToDatabaseColumn(mappings);

		assertThat(json).contains("EngineSpeed");
		assertThat(json).contains("TargetSpeed");
	}

	@Test
	void convertToEntityAttribute_shouldDeserializeMappingsFromJson() {
		final String json = """
				[
				  {
				    "outputName": "Torque",
				    "outputDataType": "int",
				    "targetProtocolVariableName": "DstTorque",
				    "targetProtocolVariableDataType": "int"
				  }
				]
				""";

		final List<ROutputActionMapping> mappings = converter.convertToEntityAttribute(json);

		assertThat(mappings).hasSize(1);
		assertThat(mappings.get(0).outputName()).isEqualTo("Torque");
		assertThat(mappings.get(0).targetProtocolVariableName()).isEqualTo("DstTorque");
	}

	@Test
	void convertToEntityAttribute_shouldReturnEmptyListWhenJsonIsInvalid() {
		assertThatThrownBy(() -> converter.convertToEntityAttribute("not-json"))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("Invalid JSON format");
	}
}
