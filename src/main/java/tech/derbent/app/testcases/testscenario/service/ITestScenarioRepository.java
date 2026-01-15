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
