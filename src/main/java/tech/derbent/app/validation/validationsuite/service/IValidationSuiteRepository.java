package tech.derbent.app.validation.validationsuite.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IProjectItemRespository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.app.validation.validationsuite.domain.CValidationSuite;

public interface IValidationSuiteRepository extends IProjectItemRespository<CValidationSuite> {

	@Override
	@Query("""
			SELECT ts FROM #{#entityName} ts
			LEFT JOIN FETCH ts.attachments
			LEFT JOIN FETCH ts.comments
			LEFT JOIN FETCH ts.project
			WHERE ts.id = :id
			""")
	Optional<CValidationSuite> findById(@Param("id") Long id);

	@Query("""
			SELECT ts FROM #{#entityName} ts
			WHERE ts.project = :project
			ORDER BY ts.id DESC
			""")
	Page<CValidationSuite> listByProject(@Param("project") CProject project, Pageable pageable);

	@Query("""
			SELECT ts FROM #{#entityName} ts
			WHERE ts.project = :project
			ORDER BY ts.id DESC
			""")
	List<CValidationSuite> listByProjectForPageView(@Param("project") CProject project);
}
