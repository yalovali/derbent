package tech.derbent.base.users.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.interfaces.IUserRelationshipRepository;
import tech.derbent.base.users.domain.CUserProjectSettings;

/** Repository interface for CUserProjectSettings entity. Provides data access methods for user-project relationships. */
@Repository
public interface IUserProjectSettingsRepository extends IUserRelationshipRepository<CUserProjectSettings> {

	/** Count users for a specific project using generic pattern */
	@Query ("SELECT COUNT(r) FROM #{#entityName} r WHERE r.project.id = :projectId")
	long countByProjectId(@Param ("projectId") Long projectId);
	/** Delete all user-project relationships for a specific project using generic pattern */
	@Modifying
	@Transactional
	@Query ("DELETE FROM #{#entityName} r WHERE r.project.id = :projectId")
	void deleteByProjectId(@Param ("projectId") Long projectId);
	/** Delete a specific user-project relationship by user and project IDs using generic pattern */
	@Modifying
	@Transactional
	@Query ("DELETE FROM #{#entityName} r WHERE r.user.id = :userId AND r.project.id = :projectId")
	void deleteByUserIdProjectId(@Param ("userId") Long userId, @Param ("projectId") Long projectId);

	/** Check if a relationship exists between user and project */
	@Override
	default boolean existsByUserIdAndEntityId(Long userId, Long entityId) {
		return existsByUserIdAndProjectId(userId, entityId);
	}

	/** Check if a relationship exists between user and project - concrete implementation */
	boolean existsByUserIdAndProjectId(Long userId, Long projectId);
	@Override
	@Query ("SELECT r FROM #{#entityName} r LEFT JOIN FETCH r.user LEFT JOIN FETCH r.project LEFT JOIN FETCH r.role")
	List<CUserProjectSettings> findAllForPageView(Sort sort);
	/** Find all settings by permission using generic pattern */
	@Query ("SELECT r FROM #{#entityName} r LEFT JOIN FETCH r.project LEFT JOIN FETCH r.user LEFT JOIN FETCH r.role WHERE r.permission = :permission")
	List<CUserProjectSettings> findByPermission(@Param ("permission") String permission);
	/** Find all user project settings for a specific project with eager loading */
	@Query ("SELECT r FROM #{#entityName} r LEFT JOIN FETCH r.project LEFT JOIN FETCH r.user LEFT JOIN FETCH r.role WHERE r.project.id = :projectId")
	List<CUserProjectSettings> findByProjectId(@Param ("projectId") Long projectId);
	/** Find all settings by role using generic pattern */
	@Query ("SELECT r FROM #{#entityName} r LEFT JOIN FETCH r.project LEFT JOIN FETCH r.user LEFT JOIN FETCH r.role WHERE r.role = :role")
	List<CUserProjectSettings> findByRole(@Param ("role") String role);
	/** Find all user project settings for a specific user with eager loading of project, user, and role. Overrides the base method to include role
	 * fetching which is specific to project settings. */
	@Override
	@Query ("SELECT r FROM #{#entityName} r LEFT JOIN FETCH r.user LEFT JOIN FETCH r.project LEFT JOIN FETCH r.role WHERE r.user.id = :userId")
	List<CUserProjectSettings> findByUserId(@Param ("userId") Long userId);

	/** Find relationship by user and entity IDs - concrete implementation */
	@Override
	default Optional<CUserProjectSettings> findByUserIdAndEntityId(Long userId, Long entityId) {
		return findByUserIdAndProjectId(userId, entityId);
	}

	/** Find a specific user project setting by user and project using generic pattern */
	@Query (
		"SELECT r FROM #{#entityName} r LEFT JOIN FETCH r.project LEFT JOIN FETCH r.user LEFT JOIN FETCH r.role WHERE r.user.id = :userId AND r.project.id = :projectId"
	)
	Optional<CUserProjectSettings> findByUserIdAndProjectId(@Param ("userId") Long userId, @Param ("projectId") Long projectId);
}
