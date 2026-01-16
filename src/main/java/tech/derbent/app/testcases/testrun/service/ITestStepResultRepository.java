package tech.derbent.app.testcases.testrun.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entity.service.IAbstractRepository;
import tech.derbent.app.testcases.testrun.domain.CTestCaseResult;
import tech.derbent.app.testcases.testrun.domain.CTestStepResult;
import tech.derbent.app.testcases.teststep.domain.CTestStep;

/** ITestStepResultRepository - Repository for test step execution results. */
public interface ITestStepResultRepository extends IAbstractRepository<CTestStepResult> {

	/** Find all test step results for a test case result.
	 * @param testCaseResult the test case result
	 * @return list of test step results ordered by test step order */
	@Query("""
			SELECT tsr FROM #{#entityName} tsr
			LEFT JOIN FETCH tsr.testStep ts
			WHERE tsr.testCaseResult = :testCaseResult
			ORDER BY ts.stepOrder ASC
			""")
	List<CTestStepResult> findByTestCaseResult(@Param("testCaseResult") CTestCaseResult testCaseResult);

	/** Find all test step results for a specific test step.
	 * @param testStep the test step
	 * @return list of test step results for this step */
	@Query("""
			SELECT tsr FROM #{#entityName} tsr
			WHERE tsr.testStep = :testStep
			ORDER BY tsr.id DESC
			""")
	List<CTestStepResult> findByTestStep(@Param("testStep") CTestStep testStep);
}
