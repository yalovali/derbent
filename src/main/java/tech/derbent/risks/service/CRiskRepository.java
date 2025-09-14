package tech.derbent.risks.service;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.abstracts.services.CEntityOfProjectRepository;
import tech.derbent.risks.domain.CRisk;

public interface CRiskRepository extends CEntityOfProjectRepository<CRisk> {

	/** Finds a risk by ID with eagerly loaded relationships to prevent N+1 queries.
	 * @param id the risk ID
	 * @return Optional containing the risk with loaded relationships */
	@Override
	@Query (
		"SELECT r FROM CRisk r " + "LEFT JOIN FETCH r.project " + "LEFT JOIN FETCH r.assignedTo " + "LEFT JOIN FETCH r.createdBy "
				+ "LEFT JOIN FETCH r.status " + "WHERE r.id = :id"
	)
	Optional<CRisk> findByIdWithEagerLoading(@Param ("id") Long id);
}
