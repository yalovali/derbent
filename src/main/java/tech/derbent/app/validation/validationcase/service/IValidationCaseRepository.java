package tech.derbent.app.validation.validationcase.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IProjectItemRespository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.app.validation.validationcase.domain.CValidationCase;
import tech.derbent.app.validation.validationcase.domain.CValidationPriority;
import tech.derbent.app.validation.validationcase.domain.CValidationSeverity;
import tech.derbent.app.validation.validationsuite.domain.CValidationSuite;

public interface IValidationCaseRepository extends IProjectItemRespository<CValidationCase> {

	@Query ("""
			SELECT tc FROM #{#entityName} tc
			WHERE tc.project = :project
			AND tc.automated = true
			ORDER BY tc.id DESC
			""")
	List<CValidationCase> findAutomatedTests(@Param ("project") CProject<?> project);
	@Override
	@Query ("""
			SELECT tc FROM #{#entityName} tc
			LEFT JOIN FETCH tc.attachments
			LEFT JOIN FETCH tc.comments
			LEFT JOIN FETCH tc.validationSteps
			LEFT JOIN FETCH tc.project
			LEFT JOIN FETCH tc.assignedTo
			LEFT JOIN FETCH tc.createdBy
			LEFT JOIN FETCH tc.status
			LEFT JOIN FETCH tc.entityType et
			LEFT JOIN FETCH et.workflow
			WHERE tc.id = :id
			""")
	Optional<CValidationCase> findById(@Param ("id") Long id);
	@Query ("""
			SELECT tc FROM #{#entityName} tc
			WHERE tc.project = :project
			AND tc.priority = :priority
			ORDER BY tc.id DESC
			""")
	List<CValidationCase> findByPriority(@Param ("project") CProject<?> project, @Param ("priority") CValidationPriority priority);
	@Query ("""
			SELECT tc FROM #{#entityName} tc
			WHERE tc.validationSuite = :scenario
			ORDER BY tc.id DESC
			""")
	List<CValidationCase> findByScenario(@Param ("scenario") CValidationSuite scenario);
	@Query ("""
			SELECT tc FROM #{#entityName} tc
			WHERE tc.project = :project
			AND tc.severity = :severity
			ORDER BY tc.id DESC
			""")
	List<CValidationCase> findBySeverity(@Param ("project") CProject<?> project, @Param ("severity") CValidationSeverity severity);
	@Override
	@Query ("""
			SELECT tc FROM #{#entityName} tc
			WHERE tc.project = :project
			ORDER BY tc.id DESC
			""")
	Page<CValidationCase> listByProject(@Param ("project") CProject<?> project, Pageable pageable);
	@Override
	@Query ("""
			SELECT tc FROM #{#entityName} tc
			WHERE tc.project = :project
			ORDER BY tc.id DESC
			""")
	List<CValidationCase> listByProjectForPageView(@Param ("project") CProject<?> project);
}
