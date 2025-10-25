package tech.derbent.app.decisions.service;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.services.IEntityOfProjectRepository;
import tech.derbent.app.decisions.domain.CDecision;
import tech.derbent.app.projects.domain.CProject;

public interface IDecisionRepository extends IEntityOfProjectRepository<CDecision> {

	@Override
	@Query (
		"SELECT d FROM #{#entityName} d LEFT JOIN FETCH d.project LEFT JOIN FETCH d.assignedTo LEFT JOIN FETCH d.createdBy LEFT JOIN FETCH d.entityType LEFT JOIN FETCH d.decisionStatus LEFT JOIN FETCH d.accountableUser WHERE d.id = :id"
	)
	Optional<CDecision> findById(@Param ("id") Long id);
	@Override
	@Query (
		"SELECT d FROM #{#entityName} d LEFT JOIN FETCH d.project LEFT JOIN FETCH d.assignedTo LEFT JOIN FETCH d.createdBy LEFT JOIN FETCH d.entityType LEFT JOIN FETCH d.decisionStatus LEFT JOIN FETCH d.accountableUser WHERE d.project = :project"
	)
	Page<CDecision> listByProject(@Param ("project") CProject project, Pageable pageable);
}
