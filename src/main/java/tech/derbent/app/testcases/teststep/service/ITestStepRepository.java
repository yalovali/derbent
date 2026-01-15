package tech.derbent.app.testcases.teststep.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entity.service.IAbstractRepository;
import tech.derbent.app.testcases.testcase.domain.CTestCase;
import tech.derbent.app.testcases.teststep.domain.CTestStep;

public interface ITestStepRepository extends IAbstractRepository<CTestStep> {

	/** Find all test steps for a test case.
	 * @param testCase the test case
	 * @return list of test steps ordered by step order */
	@Query("""
			SELECT ts FROM #{#entityName} ts
			LEFT JOIN FETCH ts.testCase
			WHERE ts.testCase = :testCase
			ORDER BY ts.stepOrder ASC
			""")
	List<CTestStep> findByTestCase(@Param("testCase") CTestCase testCase);

	/** Get next step order for a test case.
	 * @param testCase the test case
	 * @return next step order number */
	@Query("SELECT COALESCE(MAX(ts.stepOrder), 0) + 1 FROM #{#entityName} ts WHERE ts.testCase = :testCase")
	Integer getNextStepOrder(@Param("testCase") CTestCase testCase);
}
