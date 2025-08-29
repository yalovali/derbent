package tech.derbent.users.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.abstracts.services.CAbstractEntityRelationService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.CSessionService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;

/** Service class for managing user-project relationships. Handles CRUD operations for CUserProjectSettings entities. */
@Service
@Transactional (readOnly = true)
public class CUserProjectSettingsService extends CAbstractEntityRelationService<CUserProjectSettings> {
	private final CUserProjectSettingsRepository repository;

	@Autowired
	public CUserProjectSettingsService(final CUserProjectSettingsRepository repository, final Clock clock, final CSessionService sessionService) {
		super(repository, clock, sessionService);
		this.repository = repository;
	}

	/** Add user to project with specific role and permissions */
	@Transactional
	public CUserProjectSettings addUserToProject(final CUser user, final CProject project, final String role, final String permission) {
		LOGGER.debug("Adding user {} to project {} with role {} and permission {}", user, project, role, permission);
		if ((user == null) || (project == null)) {
			throw new IllegalArgumentException("User and project cannot be null");
		}
		if (relationshipExists(user.getId(), project.getId())) {
			throw new IllegalArgumentException("User is already assigned to this project");
		}
		final CUserProjectSettings settings = new CUserProjectSettings();
		settings.setUser(user);
		settings.setProject(project);
		settings.setRole(role);
		settings.setPermission(permission);
		validateRelationship(settings);
		return save(settings);
	}

	@Override
	protected CUserProjectSettings createRelationshipInstance(final Long userId, final Long projectId) {
		// Note: In a real implementation, you would fetch the actual entities from their
		// services This method should not be used directly - instead use the service
		// methods that accept entities
		throw new UnsupportedOperationException("Use addUserToProject(CUser, CProject, String, String) method instead");
	}

	@Override
	@Transactional (readOnly = true)
	public List<CUserProjectSettings> findByChildEntityId(final Long projectId) {
		LOGGER.debug("Finding user project settings for project ID: {}", projectId);
		return repository.findByProjectId(projectId);
	}

	@Override
	@Transactional (readOnly = true)
	public List<CUserProjectSettings> findByParentEntityId(final Long userId) {
		LOGGER.debug("Finding user project settings for user ID: {}", userId);
		return repository.findByUserId(userId);
	}

	/** Find user project settings by project */
	@Transactional (readOnly = true)
	public List<CUserProjectSettings> findByProject(final CProject project) {
		LOGGER.debug("Finding user project settings for project: {}", project);
		if ((project == null) || (project.getId() == null)) {
			throw new IllegalArgumentException("Project and project ID cannot be null");
		}
		return findByChildEntityId(project.getId());
	}

	/** Find user project settings by user */
	@Transactional (readOnly = true)
	public List<CUserProjectSettings> findByUser(final CUser user) {
		LOGGER.debug("Finding user project settings for user: {}", user);
		if ((user == null) || (user.getId() == null)) {
			throw new IllegalArgumentException("User and user ID cannot be null");
		}
		return findByParentEntityId(user.getId());
	}

	@Override
	@Transactional (readOnly = true)
	public Optional<CUserProjectSettings> findRelationship(final Long userId, final Long projectId) {
		return repository.findByUserIdAndProjectId(userId, projectId);
	}

	@Override
	protected Class<CUserProjectSettings> getEntityClass() { return CUserProjectSettings.class; }

	@Override
	@Transactional (readOnly = true)
	public boolean relationshipExists(final Long userId, final Long projectId) {
		return repository.existsByUserIdAndProjectId(userId, projectId);
	}

	/** Remove user from project */
	@Transactional
	public void removeUserFromProject(final CUser user, final CProject project) {
		LOGGER.debug("Removing user {} from project {}", user, project);
		if ((user == null) || (project == null)) {
			throw new IllegalArgumentException("User and project cannot be null");
		}
		deleteRelationship(user.getId(), project.getId());
	}

	/** Update user role and permissions for a project */
	@Transactional
	public CUserProjectSettings updateUserProjectRole(final CUser user, final CProject project, final String role, final String permission) {
		LOGGER.debug("Updating user {} project {} role to {} and permission to {}", user, project, role, permission);
		final Optional<CUserProjectSettings> settingsOpt = findRelationship(user.getId(), project.getId());
		if (settingsOpt.isEmpty()) {
			throw new IllegalArgumentException("User is not assigned to this project");
		}
		final CUserProjectSettings settings = settingsOpt.get();
		settings.setRole(role);
		settings.setPermission(permission);
		return updateRelationship(settings);
	}

	@Override
	protected void validateRelationship(final CUserProjectSettings relationship) {
		super.validateRelationship(relationship);
		if (relationship.getUser() == null) {
			throw new IllegalArgumentException("User is required for user project settings");
		}
		if (relationship.getProject() == null) {
			throw new IllegalArgumentException("Project is required for user project settings");
		}
		if ((relationship.getRole() == null) || relationship.getRole().trim().isEmpty()) {
			throw new IllegalArgumentException("Role is required for user project settings");
		}
		if ((relationship.getPermission() == null) || relationship.getPermission().trim().isEmpty()) {
			throw new IllegalArgumentException("Permission is required for user project settings");
		}
	}
}
