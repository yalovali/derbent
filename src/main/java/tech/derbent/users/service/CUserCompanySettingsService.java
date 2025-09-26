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
import tech.derbent.users.domain.CUserCompanySettings;

/** Service class for managing user-company relationships with ownership capabilities. Handles CRUD operations for CUserCompanySettings entities and
 * provides business logic for company membership management. */
@Service
@Transactional (readOnly = true)
public class CUserCompanySettingsService extends CAbstractEntityRelationService<CUserCompanySettings> {

	private final CUserCompanySettingsRepository repository;

	@Autowired
	public CUserCompanySettingsService(final CUserCompanySettingsRepository repository, final Clock clock, final CSessionService sessionService) {
		super(repository, clock, sessionService);
		this.repository = repository;
	}

	/** Add user to company with specific ownership level and role */
	@Transactional
	public CUserCompanySettings addUserToCompany(final CUser user, final CCompany company, final String ownershipLevel, final String role) {
		return addUserToCompany(user, company, ownershipLevel, role, null, false);
	}

	/** Add user to company with full configuration */
	@Transactional
	public CUserCompanySettings addUserToCompany(final CUser user, final CCompany company, final String ownershipLevel, final String role,
			final String department, final boolean isPrimary) {
		LOGGER.debug("Adding user {} to company {} with ownership level {} and role {}", user, company, ownershipLevel, role);
		Check.notNull(user, "User must not be null");
		Check.notNull(company, "Company must not be null");
		if ((user.getId() == null) || (company.getId() == null)) {
			throw new IllegalArgumentException("User and company must have valid IDs");
		}
		if (relationshipExists(user.getId(), company.getId())) {
			throw new IllegalArgumentException("User is already a member of this company");
		}
		final CUserCompanySettings settings = new CUserCompanySettings(user, company);
		settings.setOwnershipLevel(ownershipLevel != null ? ownershipLevel : "MEMBER");
		settings.setRole(role);
		settings.setDepartment(department);
		settings.setIsPrimaryCompany(isPrimary);
		validateRelationship(settings);
		// Save the entity first
		final CUserCompanySettings savedSettings = save(settings);
		// Maintain bidirectional relationships
		user.addCompanySettings(savedSettings);
		if (company.getUsers() != null && !company.getUsers().contains(user)) {
			company.getUsers().add(user);
		}
		return savedSettings;
	}

	/** Remove user from company */
	@Transactional
	public void removeUserFromCompany(final CUser user, final CCompany company) {
		LOGGER.debug("Removing user {} from company {}", user, company);
		if ((user == null) || (company == null)) {
			throw new IllegalArgumentException("User and company cannot be null");
		}
		if ((user.getId() == null) || (company.getId() == null)) {
			throw new IllegalArgumentException("User and company must have valid IDs");
		}
		// Find the relationship first to maintain bidirectional collections
		final Optional<CUserCompanySettings> settingsOpt = findRelationship(user.getId(), company.getId());
		if (settingsOpt.isPresent()) {
			final CUserCompanySettings settings = settingsOpt.get();
			// Remove from bidirectional collections
			user.removeCompanySettings(settings);
			if (company.getUsers() != null) {
				company.getUsers().remove(user);
			}
		}
		// Delete the relationship using the parent method
		deleteRelationship(user.getId(), company.getId());
	}

	/** Update user's role and ownership in a company */
	@Transactional
	public CUserCompanySettings updateUserCompanyRole(final CUser user, final CCompany company, final String ownershipLevel, final String role,
			final String department) {
		LOGGER.debug("Updating user {} company {} ownership to {} and role to {}", user, company, ownershipLevel, role);
		final Optional<CUserCompanySettings> settingsOpt = findRelationship(user.getId(), company.getId());
		if (settingsOpt.isEmpty()) {
			throw new IllegalArgumentException("User is not a member of this company");
		}
		final CUserCompanySettings settings = settingsOpt.get();
		if (ownershipLevel != null) {
			settings.setOwnershipLevel(ownershipLevel);
		}
		if (role != null) {
			settings.setRole(role);
		}
		if (department != null) {
			settings.setDepartment(department);
		}
		return updateRelationship(settings);
	}

	/** Set a company as the user's primary company */
	@Transactional
	public void setPrimaryCompany(final CUser user, final CCompany company) {
		LOGGER.debug("Setting company {} as primary for user {}", company, user);
		Check.notNull(user, "User cannot be null");
		Check.notNull(company, "Company cannot be null");
		// First, remove primary status from all other companies
		List<CUserCompanySettings> userCompanies = findByUser(user);
		for (CUserCompanySettings settings : userCompanies) {
			if (settings.isPrimaryCompany()) {
				settings.setIsPrimaryCompany(false);
				save(settings);
			}
		}
		// Set the specified company as primary
		Optional<CUserCompanySettings> targetSettings = findRelationship(user.getId(), company.getId());
		if (targetSettings.isPresent()) {
			CUserCompanySettings settings = targetSettings.get();
			settings.setIsPrimaryCompany(true);
			save(settings);
		} else {
			throw new IllegalArgumentException("User is not a member of this company");
		}
	}

	/** Get user's primary company */
	@Transactional (readOnly = true)
	public Optional<CCompany> getPrimaryCompany(final CUser user) {
		Check.notNull(user, "User cannot be null");
		if (user.getId() == null) {
			return Optional.empty();
		}
		Optional<CUserCompanySettings> primarySettings = repository.findPrimaryCompanyByUserId(user.getId());
		return primarySettings.map(CUserCompanySettings::getCompany);
	}

	/** Find all companies where user has admin privileges */
	@Transactional (readOnly = true)
	public List<CUserCompanySettings> findAdminCompanies(final CUser user) {
		Check.notNull(user, "User cannot be null");
		List<CUserCompanySettings> allSettings = findByUser(user);
		return allSettings.stream().filter(CUserCompanySettings::isCompanyAdmin).toList();
	}

	// Implementation of abstract methods
	@Override
	protected CUserCompanySettings createRelationshipInstance(final Long userId, final Long companyId) {
		throw new UnsupportedOperationException("Use addUserToCompany(CUser, CCompany, String, String) method instead");
	}

	@Override
	@Transactional (readOnly = true)
	public List<CUserCompanySettings> findByChildEntityId(final Long companyId) {
		LOGGER.debug("Finding user company settings for company ID: {}", companyId);
		return repository.findByCompanyId(companyId);
	}

	@Override
	@Transactional (readOnly = true)
	public List<CUserCompanySettings> findByParentEntityId(final Long userId) {
		return repository.findByUserId(userId);
	}

	/** Find user company settings by company */
	@Transactional (readOnly = true)
	public List<CUserCompanySettings> findByCompany(final CCompany company) {
		Check.notNull(company, "Company cannot be null");
		return findByChildEntityId(company.getId());
	}

	/** Find user company settings by user */
	@Transactional (readOnly = true)
	public List<CUserCompanySettings> findByUser(final CUser user) {
		Check.notNull(user, "User cannot be null");
		return findByParentEntityId(user.getId());
	}

	@Override
	@Transactional (readOnly = true)
	public Optional<CUserCompanySettings> findRelationship(final Long userId, final Long companyId) {
		return repository.findByUserIdAndCompanyId(userId, companyId);
	}

	@Override
	protected Class<CUserCompanySettings> getEntityClass() { return CUserCompanySettings.class; }

	@Override
	@Transactional (readOnly = true)
	public boolean relationshipExists(final Long userId, final Long companyId) {
		return repository.existsByUserIdAndCompanyId(userId, companyId);
	}

	@Override
	protected void validateRelationship(final CUserCompanySettings relationship) {
		super.validateRelationship(relationship);
		Check.notNull(relationship, "Relationship cannot be null");
		Check.notNull(relationship.getUser(), "User cannot be null");
		Check.notNull(relationship.getCompany(), "Company cannot be null");
		Check.notNull(relationship.getOwnershipLevel(), "Ownership level cannot be null");
	}
}
