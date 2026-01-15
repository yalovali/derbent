package tech.derbent.app.testcases.testrun.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IProjectItemRespository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.app.testcases.testrun.domain.CTestRun;
import tech.derbent.app.testcases.testscenario.domain.CTestScenario;

public interface ITestRunRepository extends IProjectItemRespository<CTestRun> {

	@Override
	@Query("""
			SELECT tr FROM #{#entityName} tr
			LEFT JOIN FETCH tr.project
			LEFT JOIN FETCH tr.assignedTo
			LEFT JOIN FETCH tr.createdBy
			LEFT JOIN FETCH tr.testScenario
			LEFT JOIN FETCH tr.testCaseResults
			LEFT JOIN FETCH tr.attachments
			LEFT JOIN FETCH tr.comments
			LEFT JOIN FETCH tr.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH tr.status
			WHERE tr.id = :id
			""")
	Optional<CTestRun> findById(@Param("id") Long id);

	@Override
	@Query("""
			SELECT tr FROM #{#entityName} tr
			LEFT JOIN FETCH tr.project
			LEFT JOIN FETCH tr.assignedTo
			LEFT JOIN FETCH tr.createdBy
			LEFT JOIN FETCH tr.testScenario
			LEFT JOIN FETCH tr.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH tr.status
			WHERE tr.project = :project
			ORDER BY tr.executionDate DESC
			""")
	Page<CTestRun> listByProject(@Param("project") CProject project, Pageable pageable);

	@Override
	@Query("""
			SELECT tr FROM #{#entityName} tr
			LEFT JOIN FETCH tr.project
			LEFT JOIN FETCH tr.assignedTo
			LEFT JOIN FETCH tr.createdBy
			LEFT JOIN FETCH tr.testScenario
			LEFT JOIN FETCH tr.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH tr.status
			WHERE tr.project = :project
			ORDER BY tr.executionDate DESC
			""")
	List<CTestRun> listByProjectForPageView(@Param("project") CProject project);

	/** Find test runs by scenario.
	 * @param scenario the test scenario
	 * @return list of test runs for the scenario ordered by execution date descending */
	@Query("""
			SELECT tr FROM #{#entityName} tr
			LEFT JOIN FETCH tr.project
			LEFT JOIN FETCH tr.assignedTo
			LEFT JOIN FETCH tr.testScenario
			LEFT JOIN FETCH tr.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH tr.status
			WHERE tr.testScenario = :scenario
			ORDER BY tr.executionDate DESC
			""")
	List<CTestRun> listByScenario(@Param("scenario") CTestScenario scenario);
}
