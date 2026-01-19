package tech.derbent.plm.risks.risk.domain;

/**
 * Risk likelihood enum - represents probability of risk occurring
 */
public enum ERiskLikelihood {
	RARE,      // Very unlikely to occur
	UNLIKELY,  // Not expected but possible
	POSSIBLE,  // Might occur
	LIKELY,    // Will probably occur
	CERTAIN    // Almost certain to occur
}
