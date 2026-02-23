package tech.derbent.bab.policybase.filter.domain;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import tech.derbent.bab.policybase.node.can.CBabCanNode;

class CBabPolicyFilterCANTest {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final class CTestCanNode extends CBabCanNode {

		private CTestCanNode() {
			super();
		}
	}

	@Test
	void getOutputStructure_shouldReturnSelectedA2LObjectsAsRecords() throws Exception {
		final CBabPolicyFilterCAN filter = new CBabPolicyFilterCAN();
		final CTestCanNode parentCanNode = new CTestCanNode();
		parentCanNode.setProtocolFileJson("""
				{
				  "VarA": {"Name":"VarA","FieldType":"MEASUREMENT","RecordType":"UBYTE","Source":1},
				  "VarB": {"Name":"VarB","FieldType":"CHARACTERISTIC","RecordType":"UWORD","Source":1},
				  "_all_a2l_variables":"VarA,VarB"
				}
				""");
		filter.setParentNode(parentCanNode);
		filter.setProtocolVariableNames(List.of("varb", "VarA", "MissingVar"));

		final List<ROutputStructure> output = filter.getOutputStructure();

		assertThat(output).hasSize(2);
		assertThat(output.get(0).name()).isEqualTo("VarB");
		assertThat(output.get(0).dataType()).isEqualTo("int");
		assertThat(output.get(1).name()).isEqualTo("VarA");
		assertThat(output.get(1).dataType()).isEqualTo("char");
	}

	@Test
	void getOutputStructure_shouldReturnEmptyOutputsWhenProtocolJsonIsMissing() throws Exception {
		final CBabPolicyFilterCAN filter = new CBabPolicyFilterCAN();
		final CTestCanNode parentCanNode = new CTestCanNode();
		filter.setParentNode(parentCanNode);
		filter.setProtocolVariableNames(List.of("VarA"));

		final List<ROutputStructure> output = filter.getOutputStructure();

		assertThat(output).isEmpty();
	}

	@Test
	void outputStructureRecord_shouldBeJsonSerializable() throws Exception {
		final List<ROutputStructure> outputStructure = List.of(new ROutputStructure("EngineSpeed", "int"));

		final String json = OBJECT_MAPPER.writeValueAsString(outputStructure);

		assertThat(json).contains("\"name\":\"EngineSpeed\"");
		assertThat(json).contains("\"dataType\":\"int\"");
	}
}
