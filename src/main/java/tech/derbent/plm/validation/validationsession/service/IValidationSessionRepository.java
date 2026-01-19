package tech.derbent.plm.validation.validationsession.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IProjectItemRespository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.plm.validation.validationsession.domain.CValidationSession;
import tech.derbent.plm.validation.validationsuite.domain.CValidationSuite;

public interface IValidationSessionRepository extends IProjectItemRespository<CValidationSession> {

	@Override
	@Query ("""
			SELECT tr FROM #{#entityName} tr
			LEFT JOIN FETCH tr.attachments
			LEFT JOIN FETCH tr.comments
			LEFT JOIN FETCH tr.project
			WHERE tr.id = :id
			""")
	Optional<CValidationSession> findById(@Param ("id") Long id);
	@Override
	@Query ("""
			SELECT tr FROM #{#entityName} tr
			WHERE tr.project = :project
			ORDER BY tr.executionStart DESC
			""")
	Page<CValidationSession> listByProject(@Param ("project") CProject<?> project, Pageable pageable);
	@Override
	@Query ("""
			SELECT tr FROM #{#entityName} tr
			WHERE tr.project = :project
			ORDER BY tr.executionStart DESC
			""")
	List<CValidationSession> listByProjectForPageView(@Param ("project") CProject<?> project);
	@Query ("""
			SELECT tr FROM #{#entityName} tr
			WHERE tr.validationSuite = :scenario
			ORDER BY tr.executionStart DESC
			""")
	List<CValidationSession> listByScenario(@Param ("scenario") CValidationSuite scenario);
}
