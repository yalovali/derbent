package tech.derbent.base.users.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.companies.service.CPageServiceUserCompanySetting;
import tech.derbent.api.entityOfProject.service.CAbstractEntityRelationService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.roles.domain.CUserCompanyRole;
import tech.derbent.api.utils.Check;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.domain.CUserCompanySetting;

/** Service class for managing user-company relationships with ownership capabilities. Handles CRUD operations for CUserCompanySetting entities and
 * provides business logic for company membership management. */
@Service
@Transactional (readOnly = true)
public class CUserCompanySettingsService extends CAbstractEntityRelationService<CUserCompanySetting> implements IEntityRegistrable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CUserCompanySettingsService.class);

	public CUserCompanySettingsService(final IUserCompanySettingsRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Add user to company with full configuration */
	@Transactional (readOnly = false)
	public CUserCompanySetting addUserToCompany(final CUser user, final CCompany company, final CUserCompanyRole role, final String ownershipLevel) {
		LOGGER.debug("Adding user {} to company {} with ownership level {} and role {}", user, company, ownershipLevel, role);
		Check.notNull(user, "User must not be null");
		Check.notNull(company, "Company must not be null");
		if (user.getId() == null || company.getId() == null || role.getId() == null) {
			throw new IllegalArgumentException("User,role and company must have valid IDs");
		}
		if (relationshipExists(user.getId(), company.getId())) {
			deleteByUserCompany(user, company);
			// throw new IllegalArgumentException("User is already a member of this company");
		}
		final CUserCompanySetting settings = new CUserCompanySetting(user, company, role, ownershipLevel);
		validateRelationship(settings);
		return save(settings);
	}

	@Override
	public String checkDeleteAllowed(final CUserCompanySetting entity) {
		return super.checkDeleteAllowed(entity);
	}

	// Implementation of abstract methods
	@Override
	protected CUserCompanySetting createRelationshipInstance(final Long userId, final Long company_id) {
		throw new UnsupportedOperationException("Use addUserToCompany(CUser, CCompany, String, String) method instead");
	}

	/** Delete all company settings for a company. Used for cleanup operations.
	 * @param company_id the company ID */
	@Transactional (readOnly = false)
	public void deleteAllByCompanyId(final Long company_id) {
		Check.notNull(company_id, "Company ID cannot be null");
		((IUserCompanySettingsRepository) repository).deleteByCompanyId(company_id);
	}

	/** Delete all company settings for a user. Used for cleanup operations.
	 * @param userId the user ID */
	@Transactional (readOnly = false)
	public void deleteAllByUserId(final Long userId) {
		Check.notNull(userId, "User ID cannot be null");
		LOGGER.debug("Deleting all company settings for user {}", userId);
		((IUserCompanySettingsRepository) repository).deleteByUserId(userId);
	}

	@Transactional (readOnly = false)
	public void deleteByUserCompany(CUser user, CCompany company) {
		Check.notNull(user, "User cannot be null");
		Check.notNull(company, "Company cannot be null");
		Check.notNull(user.getId(), "User must have a valid ID");
		Check.notNull(company.getId(), "Company must have a valid ID");
		((IUserCompanySettingsRepository) repository).deleteByUserIdAndCompanyId(user.getId(), company.getId());
	}

	@Override
	@Transactional (readOnly = true)
	public List<CUserCompanySetting> findByChildEntityId(final Long company_id) {
		return ((IUserCompanySettingsRepository) repository).findByCompany_Id(company_id);
	}

	/** Find user company settings by company */
	@Transactional (readOnly = true)
	public List<CUserCompanySetting> findByCompany(final CCompany company) {
		Check.notNull(company, "Company cannot be null");
		return findByChildEntityId(company.getId());
	}

	@Override
	@Transactional (readOnly = true)
	public List<CUserCompanySetting> findByParentEntityId(final Long userId) {
		return ((IUserCompanySettingsRepository) repository).findByUserId(userId);
	}

	/** Find user company settings by user */
	@Transactional (readOnly = true)
	public List<CUserCompanySetting> findByUser(final CUser user) {
		Check.notNull(user, "User cannot be null");
		return findByParentEntityId(user.getId());
	}

	@Override
	@Transactional (readOnly = true)
	public Optional<CUserCompanySetting> findRelationship(final Long userId, final Long company_id) {
		return ((IUserCompanySettingsRepository) repository).findByUserIdAndCompanyId(userId, company_id);
	}

	/** Find the single company setting for a user. Returns the first setting if multiple exist. This is used for single company setting scenarios.
	 * @param userId the user ID
	 * @return Optional containing the single company setting, or empty if none exists */
	@Transactional (readOnly = true)
	public Optional<CUserCompanySetting> findSingleByUserId(final Long userId) {
		Check.notNull(userId, "User ID cannot be null");
		final List<CUserCompanySetting> settings = ((IUserCompanySettingsRepository) repository).findSingleByUserId(userId);
		return settings.isEmpty() ? Optional.empty() : Optional.of(settings.get(0));
	}

	@Override
	public Class<CUserCompanySetting> getEntityClass() { return CUserCompanySetting.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceUserCompanySetting.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
	}

	@Override
	@Transactional (readOnly = true)
	public boolean relationshipExists(final Long userId, final Long company_id) {
		return ((IUserCompanySettingsRepository) repository).existsByUserIdAndCompanyId(userId, company_id);
	}

	@Override
	protected void validateRelationship(final CUserCompanySetting relationship) {
		super.validateRelationship(relationship);
		Check.notNull(relationship, "Relationship cannot be null");
		Check.notNull(relationship.getUser(), "User cannot be null");
		Check.notNull(relationship.getCompany(), "Company cannot be null");
		Check.notNull(relationship.getOwnershipLevel(), "Ownership level cannot be null");
	}
}
