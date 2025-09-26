package tech.derbent.users.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.derbent.api.services.CAbstractRepository;
import tech.derbent.users.domain.CUserCompanySettings;

/**
 * Repository interface for CUserCompanySettings entity.
 * Provides data access methods for user-company relationships.
 */
@Repository
public interface CUserCompanySettingsRepository extends CAbstractRepository<CUserCompanySettings> {

    /** Find all user company settings for a specific user */
    @Query("SELECT ucs FROM CUserCompanySettings ucs LEFT JOIN FETCH ucs.company LEFT JOIN FETCH ucs.user WHERE ucs.user.id = :userId")
    List<CUserCompanySettings> findByUserId(@Param("userId") Long userId);

    /** Find all user company settings for a specific company */
    @Query("SELECT ucs FROM CUserCompanySettings ucs LEFT JOIN FETCH ucs.company LEFT JOIN FETCH ucs.user WHERE ucs.company.id = :companyId")
    List<CUserCompanySettings> findByCompanyId(@Param("companyId") Long companyId);

    /** Find a specific user company setting by user and company */
    @Query("SELECT ucs FROM CUserCompanySettings ucs LEFT JOIN FETCH ucs.company LEFT JOIN FETCH ucs.user WHERE ucs.user.id = :userId AND ucs.company.id = :companyId")
    Optional<CUserCompanySettings> findByUserIdAndCompanyId(@Param("userId") Long userId, @Param("companyId") Long companyId);

    /** Check if a relationship exists between user and company */
    boolean existsByUserIdAndCompanyId(Long userId, Long companyId);

    /** Find all settings by ownership level */
    @Query("SELECT ucs FROM CUserCompanySettings ucs LEFT JOIN FETCH ucs.company LEFT JOIN FETCH ucs.user WHERE ucs.ownershipLevel = :ownershipLevel")
    List<CUserCompanySettings> findByOwnershipLevel(@Param("ownershipLevel") String ownershipLevel);

    /** Find primary company for a user */
    @Query("SELECT ucs FROM CUserCompanySettings ucs LEFT JOIN FETCH ucs.company WHERE ucs.user.id = :userId AND ucs.isPrimaryCompany = true")
    Optional<CUserCompanySettings> findPrimaryCompanyByUserId(@Param("userId") Long userId);

    /** Find all active company relationships for a user */
    @Query("SELECT ucs FROM CUserCompanySettings ucs LEFT JOIN FETCH ucs.company WHERE ucs.user.id = :userId AND ucs.isActive = true")
    List<CUserCompanySettings> findActiveCompaniesByUserId(@Param("userId") Long userId);

    /** Find all company admins for a company */
    @Query("SELECT ucs FROM CUserCompanySettings ucs LEFT JOIN FETCH ucs.user WHERE ucs.company.id = :companyId AND (ucs.ownershipLevel = 'OWNER' OR ucs.ownershipLevel = 'ADMIN') AND ucs.isActive = true")
    List<CUserCompanySettings> findCompanyAdminsByCompanyId(@Param("companyId") Long companyId);

    /** Count users for a specific company */
    long countByCompanyId(Long companyId);

    /** Count companies for a specific user */
    long countByUserId(Long userId);

    /** Find users by role in a company */
    @Query("SELECT ucs FROM CUserCompanySettings ucs LEFT JOIN FETCH ucs.user WHERE ucs.company.id = :companyId AND ucs.role = :role AND ucs.isActive = true")
    List<CUserCompanySettings> findByCompanyIdAndRole(@Param("companyId") Long companyId, @Param("role") String role);
}