package tech.derbent.plm.validation.validationsession.domain;

/** Validation session result status.
 * Validation sessions execute validation suites and track overall results. */
public enum CValidationResult {
	/** All tests in the run passed successfully. */
	PASSED,
	
	/** One or more tests in the run failed. */
	FAILED,
	
	/** Validation session was blocked due to preconditions not met. */
	BLOCKED,
	
	/** Validation session was skipped. */
	SKIPPED,
	
	/** Validation session is currently in progress. */
	IN_PROGRESS,
	
	/** Validation session was not executed yet. */
	NOT_EXECUTED,
	
	/** Validation session completed with mixed results (some passed, some failed). */
	PARTIAL
}
