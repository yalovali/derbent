package tech.derbent.base.users.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfProject.service.CAbstractEntityRelationService;
import tech.derbent.api.interfaces.ISearchable;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.utils.CPageableUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.domain.CUserProjectSettings;

/** Service class for managing user-project relationships. Handles CRUD operations for CUserProjectSettings entities. */
@Service
@Transactional (readOnly = true)
public class CUserProjectSettingsService extends CAbstractEntityRelationService<CUserProjectSettings> implements IEntityRegistrable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CUserProjectSettingsService.class);

	public CUserProjectSettingsService(final IUserProjectSettingsRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Add user to project with specific role and permissions */
	@Transactional
	public CUserProjectSettings addUserToProject(final CUser user, final CProject<?> project, final String permission) {
		// LOGGER.debug("Adding user {} to project {} with role {} and permission {}", user, project, role, permission);
		Check.notNull(user, "User must not be null");
		Check.notNull(project, "Project must not be null");
		if (user.getId() == null || project.getId() == null) {
			throw new IllegalArgumentException("User and project must have valid IDs");
		}
		if (relationshipExists(user.getId(), project.getId())) {
			throw new IllegalArgumentException("User is already assigned to this project");
		}
		final CUserProjectSettings settings = new CUserProjectSettings();
		settings.setProject(project);
		settings.setUser(user);
		settings.setPermission(permission);
		validateRelationship(settings);
		// Save the entity first
		final CUserProjectSettings savedSettings = save(settings);
		// dont need to save separately as cascade is set
		if (Hibernate.isInitialized(user.getProjectSettings())) {
			user.addProjectSettings(savedSettings);
		}
		if (Hibernate.isInitialized(project.getUserSettings())) {
			project.addUserSettings(savedSettings);
		}
		return savedSettings;
	}

	@Override
	public String checkDeleteAllowed(final CUserProjectSettings entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	protected CUserProjectSettings createRelationshipInstance(final Long userId, final Long projectId) {
		// Note: In a real implementation, you would fetch the actual entities from their
		// services This method should not be used directly - instead use the service
		// methods that accept entities
		throw new UnsupportedOperationException("Use addUserToProject(CUser, CProject, String, String) method instead");
	}

	/** Remove user from project */
	@Transactional
	public void deleteByUserProject(final CUser user, final CProject<?> project) {
		Check.notNull(user, "User cannot be null");
		Check.notNull(project, "Project cannot be null");
		Check.notNull(user.getId(), "User must have a valid ID");
		Check.notNull(project.getId(), "Project must have a valid ID");
		((IUserProjectSettingsRepository) repository).deleteByUserIdProjectId(user.getId(), project.getId());
		LOGGER.debug("Successfully removed user {} from project {}", user.getId(), project.getId());
	}

	@Override
	@Transactional (readOnly = true)
	public List<CUserProjectSettings> findByChildEntityId(final Long projectId) {
		return ((IUserProjectSettingsRepository) repository).findByProjectId(projectId);
	}

	@Override
	@Transactional (readOnly = true)
	public List<CUserProjectSettings> findByParentEntityId(final Long userId) {
		return ((IUserProjectSettingsRepository) repository).findByUserId(userId);
	}

	/** Find user project settings by project */
	@Transactional (readOnly = true)
	public List<CUserProjectSettings> findByProject(final CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		return findByChildEntityId(project.getId());
	}

	/** Find user project settings by user */
	@Transactional (readOnly = true)
	public List<CUserProjectSettings> findByUser(final CUser user) {
		Check.notNull(user, "User cannot be null");
		return findByParentEntityId(user.getId());
	}

	@Override
	@Transactional (readOnly = true)
	public Optional<CUserProjectSettings> findRelationship(final Long userId, final Long projectId) {
		return ((IUserProjectSettingsRepository) repository).findByUserIdAndProjectId(userId, projectId);
	}

	@Override
	public Class<CUserProjectSettings> getEntityClass() { return CUserProjectSettings.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceUserProjectSettings.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	/** Initialize lazy fields for a CUserProjectSettings entity within a transaction context. This method should be called when you need to access
	 * lazy-loaded fields outside of the original Hibernate session. The repository queries already eagerly fetch common fields (user, project, role),
	 * but this method can be used for additional fields if needed.
	 * @param settings the settings entity to initialize
	 * @return the initialized settings entity */
	@Override
	@Transactional (readOnly = true)
	public CUserProjectSettings initializeLazyFields(final CUserProjectSettings settings) {
		Check.notNull(settings, "Settings cannot be null");
		if (settings.getId() == null) {
			LOGGER.warn("Cannot initialize lazy fields for unsaved entity");
			return settings;
		}
		// Fetch the entity from database to ensure all lazy fields are available
		final CUserProjectSettings managed = repository.findById(settings.getId()).orElse(settings);
		// Access lazy fields to trigger loading within transaction
		managed.initializeAllFields();
		return managed;
	}

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
	}

	@Override
	@Transactional (readOnly = true)
	public Page<CUserProjectSettings> listForPageView(final Pageable pageable, final String searchText) throws Exception {
		final Pageable safePage = CPageableUtils.validateAndFix(pageable);
		final String term = searchText == null ? "" : searchText.trim();
		final Sort defaultSort = getDefaultSort();
		final List<CUserProjectSettings> all = ((IUserProjectSettingsRepository) repository).findAllForPageView(defaultSort);
		final boolean searchable = ISearchable.class.isAssignableFrom(getEntityClass());
		final List<CUserProjectSettings> filtered =
				term.isEmpty() || !searchable ? all : all.stream().filter(e -> ((ISearchable) e).matches(term)).toList();
		final int start = (int) Math.min(safePage.getOffset(), filtered.size());
		final int end = Math.min(start + safePage.getPageSize(), filtered.size());
		final List<CUserProjectSettings> content = filtered.subList(start, end);
		return new PageImpl<>(content, safePage, filtered.size());
	}

	@Override
	@Transactional (readOnly = true)
	public boolean relationshipExists(final Long userId, final Long projectId) {
		return ((IUserProjectSettingsRepository) repository).existsByUserIdAndProjectId(userId, projectId);
	}

	/** Update user role and permissions for a project */
	@Transactional
	public CUserProjectSettings updateUserProjectRole(final CUser user, final CProject<?> project, final String role, final String permission) {
		LOGGER.debug("Updating user {} project {} role to {} and permission to {}", user, project, role, permission);
		final Optional<CUserProjectSettings> settingsOpt = findRelationship(user.getId(), project.getId());
		if (settingsOpt.isEmpty()) {
			throw new IllegalArgumentException("User is not assigned to this project");
		}
		final CUserProjectSettings settings = settingsOpt.get();
		settings.setPermission(permission);
		return updateRelationship(settings);
	}

	@Override
	protected void validateRelationship(final CUserProjectSettings relationship) {
		super.validateRelationship(relationship);
		Check.notNull(relationship, "Relationship cannot be null");
		Check.notNull(relationship.getUser(), "User cannot be null");
		Check.notNull(relationship.getProject(), "Project cannot be null");
	}
}
