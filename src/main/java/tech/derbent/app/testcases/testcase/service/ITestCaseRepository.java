package tech.derbent.app.testcases.testcase.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IProjectItemRespository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.app.testcases.testcase.domain.CTestCase;
import tech.derbent.app.testcases.testcase.domain.CTestPriority;
import tech.derbent.app.testcases.testcase.domain.CTestSeverity;
import tech.derbent.app.testcases.testscenario.domain.CTestScenario;

public interface ITestCaseRepository extends IProjectItemRespository<CTestCase> {

	@Override
	@Query("""
			SELECT tc FROM #{#entityName} tc
			LEFT JOIN FETCH tc.project
			LEFT JOIN FETCH tc.assignedTo
			LEFT JOIN FETCH tc.createdBy
			LEFT JOIN FETCH tc.testSteps
			LEFT JOIN FETCH tc.attachments
			LEFT JOIN FETCH tc.comments
			LEFT JOIN FETCH tc.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH tc.status
			WHERE tc.id = :id
			""")
	Optional<CTestCase> findById(@Param("id") Long id);

	@Override
	@Query("""
			SELECT tc FROM #{#entityName} tc
			LEFT JOIN FETCH tc.project
			LEFT JOIN FETCH tc.assignedTo
			LEFT JOIN FETCH tc.createdBy
			LEFT JOIN FETCH tc.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH tc.status
			WHERE tc.project = :project
			ORDER BY tc.id DESC
			""")
	Page<CTestCase> listByProject(@Param("project") CProject project, Pageable pageable);

	@Override
	@Query("""
			SELECT tc FROM #{#entityName} tc
			LEFT JOIN FETCH tc.project
			LEFT JOIN FETCH tc.assignedTo
			LEFT JOIN FETCH tc.createdBy
			LEFT JOIN FETCH tc.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH tc.status
			WHERE tc.project = :project
			ORDER BY tc.id DESC
			""")
	List<CTestCase> listByProjectForPageView(@Param("project") CProject project);

	/** Find test cases by scenario.
	 * @param scenario the test scenario
	 * @return list of test cases in the scenario */
	@Query("""
			SELECT tc FROM #{#entityName} tc
			LEFT JOIN FETCH tc.project
			LEFT JOIN FETCH tc.assignedTo
			LEFT JOIN FETCH tc.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH tc.status
			WHERE :scenario MEMBER OF tc.testScenarios
			ORDER BY tc.id DESC
			""")
	List<CTestCase> findByScenario(@Param("scenario") CTestScenario scenario);

	/** Find test cases by priority.
	 * @param project the project
	 * @param priority the test priority
	 * @return list of test cases with the specified priority */
	@Query("""
			SELECT tc FROM #{#entityName} tc
			LEFT JOIN FETCH tc.project
			LEFT JOIN FETCH tc.assignedTo
			LEFT JOIN FETCH tc.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH tc.status
			WHERE tc.project = :project
			AND tc.priority = :priority
			ORDER BY tc.id DESC
			""")
	List<CTestCase> findByPriority(@Param("project") CProject project, @Param("priority") CTestPriority priority);

	/** Find test cases by severity.
	 * @param project the project
	 * @param severity the test severity
	 * @return list of test cases with the specified severity */
	@Query("""
			SELECT tc FROM #{#entityName} tc
			LEFT JOIN FETCH tc.project
			LEFT JOIN FETCH tc.assignedTo
			LEFT JOIN FETCH tc.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH tc.status
			WHERE tc.project = :project
			AND tc.severity = :severity
			ORDER BY tc.id DESC
			""")
	List<CTestCase> findBySeverity(@Param("project") CProject project, @Param("severity") CTestSeverity severity);

	/** Find automated test cases.
	 * @param project the project
	 * @return list of automated test cases */
	@Query("""
			SELECT tc FROM #{#entityName} tc
			LEFT JOIN FETCH tc.project
			LEFT JOIN FETCH tc.assignedTo
			LEFT JOIN FETCH tc.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH tc.status
			WHERE tc.project = :project
			AND tc.automated = true
			ORDER BY tc.id DESC
			""")
	List<CTestCase> findAutomatedTests(@Param("project") CProject project);
}
