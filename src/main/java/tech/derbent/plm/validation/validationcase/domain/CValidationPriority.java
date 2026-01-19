package tech.derbent.plm.validation.validationcase.domain;

/** Validation case priority levels. */
public enum CValidationPriority {
	/** Critical priority - must be tested before release. */
	CRITICAL,
	
	/** High priority - important tests. */
	HIGH,
	
	/** Medium priority - standard tests. */
	MEDIUM,
	
	/** Low priority - nice to have tests. */
	LOW
}
