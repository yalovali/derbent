package tech.derbent.app.validation.validationcase.domain;

/** Validation case severity levels indicating impact if test fails. */
public enum CValidationSeverity {
	/** Blocker - prevents further testing or critical functionality broken. */
	BLOCKER,
	
	/** Critical - major functionality broken. */
	CRITICAL,
	
	/** Major - important feature not working as expected. */
	MAJOR,
	
	/** Normal - standard issues. */
	NORMAL,
	
	/** Minor - cosmetic or low impact issues. */
	MINOR,
	
	/** Trivial - minor cosmetic issues. */
	TRIVIAL
}
