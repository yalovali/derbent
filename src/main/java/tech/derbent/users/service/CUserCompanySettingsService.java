package tech.derbent.users.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.services.CAbstractEntityRelationService;
import tech.derbent.api.utils.Check;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.session.service.CSessionService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserCompanySetting;

/** Service class for managing user-company relationships with ownership capabilities. Handles CRUD operations for CUserCompanySetting entities and
 * provides business logic for company membership management. */
@Service
@Transactional (readOnly = true)
public class CUserCompanySettingsService extends CAbstractEntityRelationService<CUserCompanySetting> {

	private final IUserCompanySettingsRepository repository;

	@Autowired
	public CUserCompanySettingsService(final IUserCompanySettingsRepository repository, final Clock clock, final CSessionService sessionService) {
		super(repository, clock, sessionService);
		this.repository = repository;
	}

	/** Add user to company with full configuration */
	@Transactional
	public CUserCompanySetting addUserToCompany(final CUser user, final CCompany company, final String ownershipLevel, final String role) {
		LOGGER.debug("Adding user {} to company {} with ownership level {} and role {}", user, company, ownershipLevel, role);
		Check.notNull(user, "User must not be null");
		Check.notNull(company, "Company must not be null");
		if ((user.getId() == null) || (company.getId() == null)) {
			throw new IllegalArgumentException("User and company must have valid IDs");
		}
		if (relationshipExists(user.getId(), company.getId())) {
			throw new IllegalArgumentException("User is already a member of this company");
		}
		final CUserCompanySetting settings = new CUserCompanySetting(user, company);
		settings.setOwnershipLevel(ownershipLevel != null ? ownershipLevel : "MEMBER");
		settings.setRole(role);
		validateRelationship(settings);
		// Save the entity first
		final CUserCompanySetting savedSettings = save(settings);
		// Maintain bidirectional relationships
		if (company.getUsers() != null && !company.getUsers().contains(user)) {
			company.getUsers().add(user);
		}
		return savedSettings;
	}

	// Implementation of abstract methods
	@Override
	protected CUserCompanySetting createRelationshipInstance(final Long userId, final Long companyId) {
		throw new UnsupportedOperationException("Use addUserToCompany(CUser, CCompany, String, String) method instead");
	}

	public void deleteByUserCompany(CUser user, CCompany company) {
		Check.notNull(user, "User cannot be null");
		Check.notNull(company, "Company cannot be null");
		Check.notNull(user.getId(), "User must have a valid ID");
		Check.notNull(company.getId(), "Company must have a valid ID");
		repository.deleteByUserIdAndCompanyId(user.getId(), company.getId());
	}

	@Override
	@Transactional (readOnly = true)
	public List<CUserCompanySetting> findByChildEntityId(final Long companyId) {
		LOGGER.debug("Finding user company settings for company ID: {}", companyId);
		return repository.findByCompanyId(companyId);
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
		return repository.findByUserId(userId);
	}

	/** Find user company settings by user */
	@Transactional (readOnly = true)
	public List<CUserCompanySetting> findByUser(final CUser user) {
		Check.notNull(user, "User cannot be null");
		return findByParentEntityId(user.getId());
	}

	@Override
	@Transactional (readOnly = true)
	public Optional<CUserCompanySetting> findRelationship(final Long userId, final Long companyId) {
		return repository.findByUserIdAndCompanyId(userId, companyId);
	}

	@Override
	protected Class<CUserCompanySetting> getEntityClass() { return CUserCompanySetting.class; }

	@Override
	@Transactional (readOnly = true)
	public boolean relationshipExists(final Long userId, final Long companyId) {
		return repository.existsByUserIdAndCompanyId(userId, companyId);
	}

	@Override
	protected void validateRelationship(final CUserCompanySetting relationship) {
		super.validateRelationship(relationship);
		Check.notNull(relationship, "Relationship cannot be null");
		Check.notNull(relationship.getUser(), "User cannot be null");
		Check.notNull(relationship.getCompany(), "Company cannot be null");
		Check.notNull(relationship.getOwnershipLevel(), "Ownership level cannot be null");
	}

	/** Find the single company setting for a user. Returns the first setting if multiple exist. This is used for single company setting scenarios.
	 * @param userId the user ID
	 * @return Optional containing the single company setting, or empty if none exists */
	@Transactional (readOnly = true)
	public Optional<CUserCompanySetting> findSingleByUserId(final Long userId) {
		Check.notNull(userId, "User ID cannot be null");
		List<CUserCompanySetting> settings = repository.findSingleByUserId(userId);
		return settings.isEmpty() ? Optional.empty() : Optional.of(settings.get(0));
	}

	/** Set or replace the single company setting for a user. Removes any existing settings first.
	 * @param user           the user
	 * @param company        the company
	 * @param ownershipLevel the ownership level
	 * @param role           the role
	 * @return the saved setting */
	@Transactional
	public CUserCompanySetting setOrReplaceSingleSetting(final CUser user, final CCompany company, final String ownershipLevel, final String role) {
		LOGGER.debug("Setting/replacing single company setting for user {} with company {}", user, company);
		Check.notNull(user, "User must not be null");
		Check.notNull(company, "Company must not be null");
		Check.notNull(user.getId(), "User must have a valid ID");
		Check.notNull(company.getId(), "Company must have a valid ID");
		// Remove any existing settings for this user
		List<CUserCompanySetting> existingSettings = repository.findByUserId(user.getId());
		if (!existingSettings.isEmpty()) {
			LOGGER.debug("Removing {} existing company settings for user {}", existingSettings.size(), user.getId());
			for (CUserCompanySetting existing : existingSettings) {
				repository.delete(existing);
			}
			repository.flush(); // Ensure deletion is executed before creating new one
		}
		// Create the new setting
		return addUserToCompany(user, company, ownershipLevel, role);
	}

	/** Delete all company settings for a user. Used for cleanup operations.
	 * @param userId the user ID */
	@Transactional
	public void deleteAllByUserId(final Long userId) {
		Check.notNull(userId, "User ID cannot be null");
		LOGGER.debug("Deleting all company settings for user {}", userId);
		repository.deleteByUserId(userId);
	}

	/** Delete all company settings for a company. Used for cleanup operations.
	 * @param companyId the company ID */
	@Transactional
	public void deleteAllByCompanyId(final Long companyId) {
		Check.notNull(companyId, "Company ID cannot be null");
		LOGGER.debug("Deleting all company settings for company {}", companyId);
		repository.deleteByCompanyId(companyId);
	}
}
