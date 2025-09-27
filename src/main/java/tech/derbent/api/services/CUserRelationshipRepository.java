package tech.derbent.api.services;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.domains.CEntityDB;

/** CUserRelationshipRepository - Base repository interface for user relationship entities. Layer: Service (MVC) - Repository interface Provides
 * common query methods for user relationship entities using the #{#entityName} pattern. This repository handles relationships between users and other
 * entities (projects, companies, etc.).
 * @param <EntityClass> The relationship entity class that extends CEntityDB */
@NoRepositoryBean
public interface CUserRelationshipRepository<EntityClass extends CEntityDB<EntityClass>> extends CAbstractUserEntityRelationRepository<EntityClass> {

	/** Find all relationship entities for a specific user with eager loading
	 * @param userId the ID of the user
	 * @return list of relationship entities with eagerly loaded associations */
	@Query ("SELECT r FROM #{#entityName} r LEFT JOIN FETCH r.user WHERE r.user.id = :userId")
	List<EntityClass> findByUserIdWithUser(@Param ("userId") Long userId);
	/** Find all active relationship entities for a specific user This method requires the relationship entity to have an 'isActive' field
	 * @param userId the ID of the user
	 * @return list of active relationship entities */
	@Query ("SELECT r FROM #{#entityName} r WHERE r.user.id = :userId AND r.isActive = true")
	List<EntityClass> findActiveByUserId(@Param ("userId") Long userId);
	/** Delete all relationship entities for a specific user
	 * @param userId the ID of the user */
	@Modifying
	@Transactional
	@Query ("DELETE FROM #{#entityName} r WHERE r.user.id = :userId")
	void deleteByUserId(@Param ("userId") Long userId);
}
