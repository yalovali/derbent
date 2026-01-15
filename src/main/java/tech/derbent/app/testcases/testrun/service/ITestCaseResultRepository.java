package tech.derbent.app.testcases.testrun.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entity.service.IAbstractRepository;
import tech.derbent.app.testcases.testcase.domain.CTestCase;
import tech.derbent.app.testcases.testrun.domain.CTestCaseResult;
import tech.derbent.app.testcases.testrun.domain.CTestRun;

public interface ITestCaseResultRepository extends IAbstractRepository<CTestCaseResult> {

	@Query("""
			SELECT tcr FROM #{#entityName} tcr
			WHERE tcr.testRun = :testRun
			ORDER BY tcr.executionOrder ASC
			""")
	List<CTestCaseResult> findByTestRun(@Param("testRun") CTestRun testRun);

	@Query("""
			SELECT tcr FROM #{#entityName} tcr
			WHERE tcr.testCase = :testCase
			ORDER BY tcr.id DESC
			""")
	List<CTestCaseResult> findByTestCase(@Param("testCase") CTestCase testCase);
}
