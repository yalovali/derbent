package tech.derbent.base.users.service;

import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.services.IUserRelationshipRepository;
import tech.derbent.base.users.domain.CUserCompanySetting;

/** Repository interface for CUserCompanySetting entity. Provides data access methods for user-company relationships. */
@Repository
public interface IUserCompanySettingsRepository extends IUserRelationshipRepository<CUserCompanySetting> {

	@Override
	@Query ("SELECT r FROM #{#entityName} r LEFT JOIN FETCH r.user LEFT JOIN FETCH r.company LEFT JOIN FETCH r.role WHERE r.id = :id")
	Optional<CUserCompanySetting> findById(Long id);
	@Override
	@Query ("SELECT r FROM #{#entityName} r LEFT JOIN FETCH r.user LEFT JOIN FETCH r.company LEFT JOIN FETCH r.role WHERE r.user.id = :userId")
	List<CUserCompanySetting> findByUserId(@Param ("userId") Long userId);
	/** Count users for a specific company using generic pattern */
	@Query ("SELECT COUNT(r) FROM #{#entityName} r WHERE r.company.id = :company_id")
	long countByCompany_Id(@Param ("company_id") Long company_id);
	@Modifying
	@Transactional
	@Query ("DELETE FROM #{#entityName} r WHERE r.user.id = :userId AND r.company.id = :company_id")
	void deleteByUserIdAndCompanyId(@Nullable Long userId, @Nullable Long company_id);
	/** Check if a relationship exists between user and company - concrete implementation */
	boolean existsByUserIdAndCompanyId(Long userId, Long company_id);

	/** Check if a relationship exists between user and company */
	@Override
	default boolean existsByUserIdAndEntityId(Long userId, Long entityId) {
		return existsByUserIdAndCompanyId(userId, entityId);
	}

	/** Find all user company settings for a specific company with eager loading */
	@Query ("SELECT r FROM #{#entityName} r LEFT JOIN FETCH r.company LEFT JOIN FETCH r.user LEFT JOIN FETCH r.role WHERE r.company.id = :company_id")
	List<CUserCompanySetting> findByCompany_Id(@Param ("company_id") Long company_id);
	/** Find a specific user company setting by user and company using generic pattern */
	@Query (
		"SELECT r FROM #{#entityName} r LEFT JOIN FETCH r.company LEFT JOIN FETCH r.user LEFT JOIN FETCH r.role WHERE r.user.id = :userId AND r.company.id = :company_id"
	)
	Optional<CUserCompanySetting> findByUserIdAndCompanyId(@Param ("userId") Long userId, @Param ("company_id") Long company_id);

	/** Find relationship by user and entity IDs - concrete implementation */
	@Override
	default Optional<CUserCompanySetting> findByUserIdAndEntityId(Long userId, Long entityId) {
		return findByUserIdAndCompanyId(userId, entityId);
	}

	/** Find the single company setting for a user (returns the first one if multiple exist). This is used for single company setting scenarios where
	 * a user should only have one company setting. */
	@Query (
		"SELECT r FROM #{#entityName} r LEFT JOIN FETCH r.user LEFT JOIN FETCH r.company LEFT JOIN FETCH r.role WHERE r.user.id = :userId ORDER BY r.id ASC"
	)
	List<CUserCompanySetting> findSingleByUserId(@Param ("userId") Long userId);
	/** Delete all settings for a specific company. Used for cleanup when a company is deleted. */
	@Modifying
	@Transactional
	@Query ("DELETE FROM #{#entityName} r WHERE r.company.id = :company_id")
	void deleteByCompanyId(@Param ("company_id") Long company_id);
	/** Delete all settings for a specific user. Used for cleanup when a user is deleted. */
	@Override
	@Modifying
	@Transactional
	@Query ("DELETE FROM #{#entityName} r WHERE r.user.id = :userId")
	void deleteByUserId(@Param ("userId") Long userId);
}
