package tech.derbent.api.services;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.domains.CEntityDB;

/** CAbstractUserEntityRelationRepository - Base repository interface for entities that manage user-entity relationships. Layer: Service (MVC) -
 * Repository interface Provides common query methods for user-entity relationships using the #{#entityName} pattern for better code reuse. This
 * repository handles bidirectional relationships where users are connected to other entities.
 * @param <EntityClass> The relationship entity class that extends CEntityDB */
@NoRepositoryBean
public interface CAbstractUserEntityRelationRepository<EntityClass extends CEntityDB<EntityClass>> extends CAbstractRepository<EntityClass> {

	/** Find all relationship entities for a specific user ID
	 * @param userId the ID of the user
	 * @return list of relationship entities for the user */
	@Query ("SELECT r FROM #{#entityName} r LEFT JOIN FETCH r.user WHERE r.user.id = :userId")
	List<EntityClass> findByUserId(@Param ("userId") Long userId);
	/** Count relationship entities for a specific user ID
	 * @param userId the ID of the user
	 * @return count of relationships for the user */
	@Query ("SELECT COUNT(r) FROM #{#entityName} r WHERE r.user.id = :userId")
	long countByUserId(@Param ("userId") Long userId);
	/** Check if a relationship exists between user and another entity by their IDs This method should be overridden in concrete implementations to
	 * specify the second entity field
	 * @param userId   the ID of the user
	 * @param entityId the ID of the other entity
	 * @return true if relationship exists, false otherwise */
	boolean existsByUserIdAndEntityId(Long userId, Long entityId);
	/** Find a specific relationship between user and another entity by their IDs This method should be overridden in concrete implementations to
	 * specify the second entity field
	 * @param userId   the ID of the user
	 * @param entityId the ID of the other entity
	 * @return optional relationship entity */
	Optional<EntityClass> findByUserIdAndEntityId(Long userId, Long entityId);
}
