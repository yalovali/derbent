package tech.derbent.app.testcases.teststep.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.interfaces.IChildEntityRepository;
import tech.derbent.app.testcases.testcase.domain.CTestCase;
import tech.derbent.app.testcases.teststep.domain.CTestStep;

public interface ITestStepRepository extends IChildEntityRepository<CTestStep, CTestCase> {

	@Override
	@Query("""
			SELECT ts FROM #{#entityName} ts
			LEFT JOIN FETCH ts.testCase
			WHERE ts.testCase = :master
			ORDER BY ts.stepOrder ASC
			""")
	List<CTestStep> findByMaster(@Param("master") CTestCase master);

	@Override
	@Query("""
			SELECT ts FROM #{#entityName} ts
			LEFT JOIN FETCH ts.testCase
			WHERE ts.testCase.id = :masterId
			ORDER BY ts.stepOrder ASC
			""")
	List<CTestStep> findByMasterId(@Param("masterId") Long masterId);

	@Override
	@Query("SELECT COUNT(ts) FROM #{#entityName} ts WHERE ts.testCase = :master")
	Long countByMaster(@Param("master") CTestCase master);

	@Override
	@Query("SELECT COALESCE(MAX(ts.stepOrder), 0) + 1 FROM #{#entityName} ts WHERE ts.testCase = :master")
	Integer getNextItemOrder(@Param("master") CTestCase master);
}
