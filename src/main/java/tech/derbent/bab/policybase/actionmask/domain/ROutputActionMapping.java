package tech.derbent.bab.policybase.actionmask.domain;

import java.io.Serializable;

/** Mapping entry from a source filter output variable to a destination protocol variable. */
public record ROutputActionMapping(String outputName, String outputDataType, String targetProtocolVariableName,
		String targetProtocolVariableDataType) implements Serializable {

	private static final long serialVersionUID = 1L;

	public ROutputActionMapping {
		outputName = outputName == null ? "" : outputName.trim();
		outputDataType = outputDataType == null ? "" : outputDataType.trim();
		targetProtocolVariableName = targetProtocolVariableName == null ? "" : targetProtocolVariableName.trim();
		targetProtocolVariableDataType = targetProtocolVariableDataType == null ? "" : targetProtocolVariableDataType.trim();
	}
}
