package tech.derbent.app.testcases.testcase.domain;

/** Test case priority levels. */
public enum CTestPriority {
	/** Critical priority - must be tested before release. */
	CRITICAL,
	
	/** High priority - important tests. */
	HIGH,
	
	/** Medium priority - standard tests. */
	MEDIUM,
	
	/** Low priority - nice to have tests. */
	LOW
}
