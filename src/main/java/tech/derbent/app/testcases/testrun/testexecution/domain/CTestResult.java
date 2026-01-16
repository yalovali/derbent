package tech.derbent.app.testcases.testrun.testexecution.domain;

/** Test execution result status. */
public enum CTestResult {
	/** Test passed successfully. */
	PASSED,
	
	/** Test failed. */
	FAILED,
	
	/** Test was blocked due to preconditions not met. */
	BLOCKED,
	
	/** Test was skipped. */
	SKIPPED,
	
	/** Test is in progress. */
	IN_PROGRESS,
	
	/** Test was not executed yet. */
	NOT_EXECUTED
}
