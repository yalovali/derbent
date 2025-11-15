package tech.derbent.app.milestones.milestone.service;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.milestones.milestone.domain.CMilestone;
import tech.derbent.app.milestones.milestonetype.domain.CMilestoneType;

public interface IMilestoneRepository extends IEntityOfProjectRepository<CMilestone> {

	@Query ("SELECT COUNT(a) FROM {#entityName} a WHERE a.entityType = :type")
	long countByType(@Param ("type") CMilestoneType type);
	@Override
	@Query (
		"SELECT r FROM CMilestone r LEFT JOIN FETCH r.project LEFT JOIN FETCH r.assignedTo LEFT JOIN FETCH r.createdBy LEFT JOIN FETCH r.status LEFT JOIN FETCH r.entityType " + "WHERE r.id = :id"
	)
	Optional<CMilestone> findById(@Param ("id") Long id);
}
