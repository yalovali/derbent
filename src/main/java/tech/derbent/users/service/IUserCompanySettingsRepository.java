package tech.derbent.users.service;

import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.services.IUserRelationshipRepository;
import tech.derbent.users.domain.CUserCompanySetting;

/** Repository interface for CUserCompanySetting entity. Provides data access methods for user-company relationships. */
@Repository
public interface IUserCompanySettingsRepository extends IUserRelationshipRepository<CUserCompanySetting> {

	/** Count users for a specific company using generic pattern */
	@Query ("SELECT COUNT(r) FROM #{#entityName} r WHERE r.company.id = :companyId")
	long countByCompanyId(@Param ("companyId") Long companyId);
	@Modifying
	@Transactional
	@Query ("DELETE FROM #{#entityName} r WHERE r.user.id = :userId AND r.company.id = :companyId")
	void deleteByUserIdAndCompanyId(@Nullable Long userId, @Nullable Long companyId);
	/** Check if a relationship exists between user and company - concrete implementation */
	boolean existsByUserIdAndCompanyId(Long userId, Long companyId);

	/** Check if a relationship exists between user and company */
	@Override
	default boolean existsByUserIdAndEntityId(Long userId, Long entityId) {
		return existsByUserIdAndCompanyId(userId, entityId);
	}

	/** Find all user company settings for a specific company with eager loading */
	@Query ("SELECT r FROM #{#entityName} r LEFT JOIN FETCH r.company LEFT JOIN FETCH r.user WHERE r.company.id = :companyId")
	List<CUserCompanySetting> findByCompanyId(@Param ("companyId") Long companyId);
	/** Find users by role in a company using generic pattern */
	@Query ("SELECT r FROM #{#entityName} r LEFT JOIN FETCH r.user WHERE r.company.id = :companyId AND r.role = :role AND r.isActive = true")
	List<CUserCompanySetting> findByCompanyIdAndRole(@Param ("companyId") Long companyId, @Param ("role") String role);
	/** Find all settings by ownership level using generic pattern */
	@Query ("SELECT r FROM #{#entityName} r LEFT JOIN FETCH r.company LEFT JOIN FETCH r.user WHERE r.ownershipLevel = :ownershipLevel")
	List<CUserCompanySetting> findByOwnershipLevel(@Param ("ownershipLevel") String ownershipLevel);
	/** Find a specific user company setting by user and company using generic pattern */
	@Query ("SELECT r FROM #{#entityName} r LEFT JOIN FETCH r.company LEFT JOIN FETCH r.user WHERE r.user.id = :userId AND r.company.id = :companyId")
	Optional<CUserCompanySetting> findByUserIdAndCompanyId(@Param ("userId") Long userId, @Param ("companyId") Long companyId);

	/** Find relationship by user and entity IDs - concrete implementation */
	@Override
	default Optional<CUserCompanySetting> findByUserIdAndEntityId(Long userId, Long entityId) {
		return findByUserIdAndCompanyId(userId, entityId);
	}

	/** Find all company admins for a company using generic pattern */
	@Query (
		"SELECT r FROM #{#entityName} r LEFT JOIN FETCH r.user WHERE r.company.id = :companyId AND (r.ownershipLevel = 'OWNER' OR r.ownershipLevel = 'ADMIN') AND r.isActive = true"
	)
	List<CUserCompanySetting> findCompanyAdminsByCompanyId(@Param ("companyId") Long companyId);
	/** Find primary company for a user using generic pattern */
	@Query ("SELECT r FROM #{#entityName} r LEFT JOIN FETCH r.company WHERE r.user.id = :userId AND r.isPrimaryCompany = true")
	Optional<CUserCompanySetting> findPrimaryCompanyByUserId(@Param ("userId") Long userId);
}
