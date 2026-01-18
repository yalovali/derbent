package tech.derbent.app.validation.validationsession.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entity.service.IAbstractRepository;
import tech.derbent.app.validation.validationcase.domain.CValidationCase;
import tech.derbent.app.validation.validationsession.domain.CValidationCaseResult;
import tech.derbent.app.validation.validationsession.domain.CValidationSession;

public interface IValidationCaseResultRepository extends IAbstractRepository<CValidationCaseResult> {

	@Query("""
			SELECT tcr FROM #{#entityName} tcr
			WHERE tcr.validationSession = :validationSession
			ORDER BY tcr.executionOrder ASC
			""")
	List<CValidationCaseResult> findByValidationSession(@Param("validationSession") CValidationSession validationSession);

	@Query("""
			SELECT tcr FROM #{#entityName} tcr
			WHERE tcr.validationCase = :validationCase
			ORDER BY tcr.id DESC
			""")
	List<CValidationCaseResult> findByValidationCase(@Param("validationCase") CValidationCase validationCase);
}
