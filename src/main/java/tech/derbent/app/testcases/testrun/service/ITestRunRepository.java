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
			LEFT JOIN FETCH tr.attachments
			LEFT JOIN FETCH tr.comments
			LEFT JOIN FETCH tr.project
			WHERE tr.id = :id
			""")
	Optional<CTestRun> findById(@Param("id") Long id);


	@Override
	@Query("""
			SELECT tr FROM #{#entityName} tr
			WHERE tr.project = :project
			ORDER BY tr.executionStart DESC
			""")
	Page<CTestRun> listByProject(@Param("project") CProject project, Pageable pageable);

	@Query("""
			SELECT tr FROM #{#entityName} tr
			WHERE tr.project = :project
			ORDER BY tr.executionStart DESC
			""")
	List<CTestRun> listByProjectForPageView(@Param("project") CProject project);

	@Query("""
			SELECT tr FROM #{#entityName} tr
			WHERE tr.testScenario = :scenario
			ORDER BY tr.executionStart DESC
			""")
	List<CTestRun> listByScenario(@Param("scenario") CTestScenario scenario);
}
