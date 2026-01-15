package tech.derbent.app.testcases.testscenario.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IProjectItemRespository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.app.testcases.testscenario.domain.CTestScenario;

public interface ITestScenarioRepository extends IProjectItemRespository<CTestScenario> {

	@Override
	@Query("""
			SELECT ts FROM #{#entityName} ts
			LEFT JOIN FETCH ts.attachments
			LEFT JOIN FETCH ts.comments
			LEFT JOIN FETCH ts.project
			LEFT JOIN FETCH ts.assignedTo
			LEFT JOIN FETCH ts.createdBy
			LEFT JOIN FETCH ts.status
			LEFT JOIN FETCH ts.entityType et
			LEFT JOIN FETCH et.workflow
			WHERE ts.id = :id
			""")
	Optional<CTestScenario> findById(@Param("id") Long id);

	@Query("""
			SELECT ts FROM #{#entityName} ts
			WHERE ts.project = :project
			ORDER BY ts.id DESC
			""")
	Page<CTestScenario> listByProject(@Param("project") CProject project, Pageable pageable);

	@Query("""
			SELECT ts FROM #{#entityName} ts
			WHERE ts.project = :project
			ORDER BY ts.id DESC
			""")
	List<CTestScenario> listByProjectForPageView(@Param("project") CProject project);
}
