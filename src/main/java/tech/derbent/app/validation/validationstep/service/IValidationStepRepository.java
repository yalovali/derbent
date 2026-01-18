package tech.derbent.app.validation.validationstep.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.interfaces.IChildEntityRepository;
import tech.derbent.app.validation.validationcase.domain.CValidationCase;
import tech.derbent.app.validation.validationstep.domain.CValidationStep;

public interface IValidationStepRepository extends IChildEntityRepository<CValidationStep, CValidationCase> {

	@Override
	@Query("""
			SELECT ts FROM #{#entityName} ts
			LEFT JOIN FETCH ts.validationCase
			WHERE ts.validationCase = :master
			ORDER BY ts.stepOrder ASC
			""")
	List<CValidationStep> findByMaster(@Param("master") CValidationCase master);

	@Override
	@Query("""
			SELECT ts FROM #{#entityName} ts
			LEFT JOIN FETCH ts.validationCase
			WHERE ts.validationCase.id = :masterId
			ORDER BY ts.stepOrder ASC
			""")
	List<CValidationStep> findByMasterId(@Param("masterId") Long masterId);

	@Override
	@Query("SELECT COUNT(ts) FROM #{#entityName} ts WHERE ts.validationCase = :master")
	Long countByMaster(@Param("master") CValidationCase master);

	@Override
	@Query("SELECT COALESCE(MAX(ts.stepOrder), 0) + 1 FROM #{#entityName} ts WHERE ts.validationCase = :master")
	Integer getNextItemOrder(@Param("master") CValidationCase master);
}
