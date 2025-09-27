package tech.derbent.meetings.service;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.services.IEntityOfProjectRepository;
import tech.derbent.meetings.domain.CMeetingType;

/** CMeetingTypeRepository - Repository interface for CMeetingType entity. Layer: Service (MVC) Provides data access operations for project-aware
 * meeting types with eager loading support. */
public interface IMeetingTypeRepository extends IEntityOfProjectRepository<CMeetingType> {

	/** Finds a meeting type by ID with eagerly loaded relationships using generic pattern */
	@Query (
		"SELECT mt FROM #{#entityName} mt LEFT JOIN FETCH mt.project LEFT JOIN FETCH mt.assignedTo LEFT JOIN FETCH mt.createdBy WHERE mt.id = :id"
	)
	Optional<CMeetingType> findByIdWithRelationships(@Param ("id") Long id);
}
