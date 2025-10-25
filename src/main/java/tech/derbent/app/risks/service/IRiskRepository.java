package tech.derbent.app.risks.service;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.services.IEntityOfProjectRepository;
import tech.derbent.app.risks.domain.CRisk;
import tech.derbent.app.risks.domain.CRiskType;

public interface IRiskRepository extends IEntityOfProjectRepository<CRisk> {

	@Query ("SELECT COUNT(a) FROM #{#entityName} a WHERE a.riskType = :type")
	long countByType(@Param ("type") CRiskType type);
	@Override
	@Query (
		"SELECT r FROM CRisk r LEFT JOIN FETCH r.project LEFT JOIN FETCH r.assignedTo LEFT JOIN FETCH r.createdBy "
				+ "LEFT JOIN FETCH r.status LEFT JOIN FETCH r.riskType " + "WHERE r.id = :id"
	)
	Optional<CRisk> findById(@Param ("id") Long id);
}
