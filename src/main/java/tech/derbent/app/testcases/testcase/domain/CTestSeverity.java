package tech.derbent.app.testcases.testcase.domain;

/** Test case severity levels indicating impact if test fails. */
public enum CTestSeverity {
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
