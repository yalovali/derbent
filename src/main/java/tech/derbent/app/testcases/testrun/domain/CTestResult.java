package tech.derbent.app.testcases.testrun.domain;

/** Test run result status.
 * Test runs execute test scenarios and track overall results. */
public enum CTestResult {
	/** All tests in the run passed successfully. */
	PASSED,
	
	/** One or more tests in the run failed. */
	FAILED,
	
	/** Test run was blocked due to preconditions not met. */
	BLOCKED,
	
	/** Test run was skipped. */
	SKIPPED,
	
	/** Test run is currently in progress. */
	IN_PROGRESS,
	
	/** Test run was not executed yet. */
	NOT_EXECUTED,
	
	/** Test run completed with mixed results (some passed, some failed). */
	PARTIAL
}
