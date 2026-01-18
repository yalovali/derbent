package tech.derbent.app.validation.validationsession.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entity.service.IAbstractRepository;
import tech.derbent.app.validation.validationsession.domain.CValidationCaseResult;
import tech.derbent.app.validation.validationsession.domain.CValidationStepResult;
import tech.derbent.app.validation.validationstep.domain.CValidationStep;

/** IValidationStepResultRepository - Repository for validation step execution results. */
public interface IValidationStepResultRepository extends IAbstractRepository<CValidationStepResult> {

	/** Find all validation step results for a validation case result.
	 * @param validationCaseResult the validation case result
	 * @return list of validation step results ordered by validation step order */
	@Query("""
			SELECT tsr FROM #{#entityName} tsr
			LEFT JOIN FETCH tsr.validationStep ts
			WHERE tsr.validationCaseResult = :validationCaseResult
			ORDER BY ts.stepOrder ASC
			""")
	List<CValidationStepResult> findByValidationCaseResult(@Param("validationCaseResult") CValidationCaseResult validationCaseResult);

	/** Find all validation step results for a specific validation step.
	 * @param validationStep the validation step
	 * @return list of validation step results for this step */
	@Query("""
			SELECT tsr FROM #{#entityName} tsr
			WHERE tsr.validationStep = :validationStep
			ORDER BY tsr.id DESC
			""")
	List<CValidationStepResult> findByValidationStep(@Param("validationStep") CValidationStep validationStep);
}
