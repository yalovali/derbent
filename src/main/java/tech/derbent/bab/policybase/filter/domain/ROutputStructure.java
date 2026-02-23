package tech.derbent.bab.policybase.filter.domain;

import java.io.Serializable;

/** Output structure entry produced by policy filters for downstream consumers. */
public record ROutputStructure(String name, String dataType) implements Serializable {

	private static final long serialVersionUID = 1L;

	public ROutputStructure {
		name = name == null ? "" : name.trim();
		dataType = dataType == null ? "" : dataType.trim();
	}
}
