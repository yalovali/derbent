package tech.derbent.app.risks.risk.domain;

/**
 * Risk criticality enum - represents overall importance/criticality of the risk
 */
public enum ERiskCriticality {
	LOW,      // Minor impact, low priority
	MODERATE, // Moderate impact, medium priority
	HIGH,     // Significant impact, high priority
	CRITICAL  // Severe impact, immediate attention required
}
