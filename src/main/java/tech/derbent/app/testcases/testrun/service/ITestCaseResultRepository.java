package tech.derbent.app.testcases.testrun.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entity.service.IAbstractRepository;
import tech.derbent.app.testcases.testcase.domain.CTestCase;
import tech.derbent.app.testcases.testrun.domain.CTestCaseResult;
import tech.derbent.app.testcases.testrun.domain.CTestRun;

public interface ITestCaseResultRepository extends IAbstractRepository<CTestCaseResult> {

	/** Find all test case results for a test run.
	 * @param testRun the test run
	 * @return list of test case results ordered by execution order */
	@Query("""
			SELECT tcr FROM #{#entityName} tcr
			LEFT JOIN FETCH tcr.testRun
			LEFT JOIN FETCH tcr.testCase
			WHERE tcr.testRun = :testRun
			ORDER BY tcr.executionOrder ASC
			""")
	List<CTestCaseResult> findByTestRun(@Param("testRun") CTestRun testRun);

	/** Find all test case results for a test case.
	 * @param testCase the test case
	 * @return list of test case results ordered by test run execution date descending */
	@Query("""
			SELECT tcr FROM #{#entityName} tcr
			LEFT JOIN FETCH tcr.testRun tr
			LEFT JOIN FETCH tcr.testCase
			WHERE tcr.testCase = :testCase
			ORDER BY tr.executionDate DESC
			""")
	List<CTestCaseResult> findByTestCase(@Param("testCase") CTestCase testCase);
}
