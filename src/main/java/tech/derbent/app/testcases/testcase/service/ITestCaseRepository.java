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

	@Query("""
			SELECT tc FROM #{#entityName} tc
			WHERE tc.project = :project
			ORDER BY tc.id DESC
			""")
	Page<CTestCase> listByProject(@Param("project") CProject project, Pageable pageable);

	@Query("""
			SELECT tc FROM #{#entityName} tc
			WHERE tc.project = :project
			ORDER BY tc.id DESC
			""")
	List<CTestCase> listByProjectForPageView(@Param("project") CProject project);

	@Query("""
			SELECT tc FROM #{#entityName} tc
			WHERE tc.testScenario = :scenario
			ORDER BY tc.id DESC
			""")
	List<CTestCase> findByScenario(@Param("scenario") CTestScenario scenario);

	@Query("""
			SELECT tc FROM #{#entityName} tc
			WHERE tc.project = :project
			AND tc.priority = :priority
			ORDER BY tc.id DESC
			""")
	List<CTestCase> findByPriority(@Param("project") CProject project, @Param("priority") CTestPriority priority);

	@Query("""
			SELECT tc FROM #{#entityName} tc
			WHERE tc.project = :project
			AND tc.severity = :severity
			ORDER BY tc.id DESC
			""")
	List<CTestCase> findBySeverity(@Param("project") CProject project, @Param("severity") CTestSeverity severity);

	@Query("""
			SELECT tc FROM #{#entityName} tc
			WHERE tc.project = :project
			AND tc.automated = true
			ORDER BY tc.id DESC
			""")
	List<CTestCase> findAutomatedTests(@Param("project") CProject project);
}
