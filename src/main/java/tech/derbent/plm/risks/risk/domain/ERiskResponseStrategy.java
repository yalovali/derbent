package tech.derbent.plm.risks.risk.domain;

/**
 * Risk Response Strategy enum based on ISO 31000:2018 Risk Management standard.
 * Defines standard approaches for treating identified risks.
 * 
 * @see <a href="https://www.iso.org/iso-31000-risk-management.html">ISO 31000:2018</a>
 */
public enum ERiskResponseStrategy {
	
	/** Eliminate the risk by changing plans or approach to avoid the threat entirely. */
	AVOID("Avoid", "Eliminate the risk by changing plans or removing the risk source"),
	
	/** Shift the risk impact and management responsibility to a third party (insurance, outsourcing, contracts). */
	TRANSFER("Transfer", "Shift risk impact to third party (insurance, contract, outsourcing)"),
	
	/** Reduce the probability or impact of the risk through proactive actions. */
	MITIGATE("Mitigate", "Reduce probability or impact through preventive actions"),
	
	/** Acknowledge the risk and take no proactive action; monitor and be prepared to respond if it occurs. */
	ACCEPT("Accept", "Acknowledge risk, no proactive action, monitor only"),
	
	/** Escalate the risk to higher management or stakeholders who have authority to address it. */
	ESCALATE("Escalate", "Escalate to higher authority or stakeholder for decision");
	
	private final String displayName;
	private final String description;
	
	ERiskResponseStrategy(final String displayName, final String description) {
		this.displayName = displayName;
		this.description = description;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public String getDescription() {
		return description;
	}
	
	@Override
	public String toString() {
		return displayName;
	}
}
