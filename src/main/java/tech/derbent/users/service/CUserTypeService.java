package tech.derbent.users.service;

import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.api.utils.Check;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.ISessionService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserType;

/** CUserTypeService - Service layer for CUserType entity. Layer: Service (MVC) Handles business logic for project-aware user type operations. */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CUserTypeService extends CEntityOfProjectService<CUserType> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CUserTypeService.class);
	@Autowired
	private IUserRepository userRepository;

	/** Constructor for CUserTypeService.
	 * @param repository the CUserTypeRepository to use for data access
	 * @param clock      the Clock instance for time-related operations */
	public CUserTypeService(final IUserTypeRepository repository, final Clock clock, final ISessionService sessionService,
			final IUserRepository userRepository) {
		super(repository, clock, sessionService);
		this.userRepository = userRepository;
	}

	@Override
	protected Class<CUserType> getEntityClass() { return CUserType.class; }

	/** Checks dependencies before allowing user type deletion. Prevents deletion if the type is being used by any users.
	 * @param userType the user type entity to check
	 * @return null if type can be deleted, error message otherwise */
	@Override
	public String checkDependencies(final CUserType userType) {
		Check.notNull(userType, "User type cannot be null");
		Check.notNull(userType.getId(), "User type ID cannot be null");
		try {
			// Check if this type is marked as non-deletable
			if (userType.getAttributeNonDeletable()) {
				return "This user type is marked as non-deletable and cannot be removed from the system.";
			}
			// Check if any users are using this type
			final long usageCount = userRepository.countByUserType(userType);
			if (usageCount > 0) {
				return String.format("Cannot delete user type. It is being used by %d user%s.", usageCount, usageCount == 1 ? "" : "s");
			}
			return null; // Type can be deleted
		} catch (final Exception e) {
			LOGGER.error("Error checking dependencies for user type: {}", userType.getName(), e);
			return "Error checking dependencies: " + e.getMessage();
		}
	}

	/** Initializes a new user type with default values based on current session and available data. Sets: - Project from current session - User for
	 * creation tracking - Auto-generated name - Default color - Default sort order - Not marked as non-deletable
	 * @param userType the newly created user type to initialize
	 * @throws IllegalStateException if required fields cannot be initialized */
	@Override
	public void initializeNewEntity(final CUserType userType) {
		Check.notNull(userType, "User type cannot be null");
		Check.notNull(sessionService, "Session service is required for user type initialization");
		try {
			// Get current project from session
			Optional<CProject> activeProject = sessionService.getActiveProject();
			Check.isTrue(activeProject.isPresent(), "No active project in session - project context is required to create user types");
			CProject currentProject = activeProject.get();
			userType.setProject(currentProject);
			// Get current user from session for createdBy field
			Optional<CUser> currentUser = sessionService.getActiveUser();
			if (currentUser.isPresent()) {
				userType.setCreatedBy(currentUser.get());
			}
			// Auto-generate name based on count
			long typeCount = ((IUserTypeRepository) repository).countByProject(currentProject);
			String autoName = String.format("UserType%02d", typeCount + 1);
			userType.setName(autoName);
			// Set default description
			userType.setDescription("");
			// Set default color
			userType.setColor("#00546d");
			// Set default sort order
			userType.setSortOrder(100);
			// Set deletable by default (not system type)
			userType.setAttributeNonDeletable(false);
			LOGGER.debug("Initialized new user type with auto-generated name: {}", autoName);
		} catch (final Exception e) {
			LOGGER.error("Error initializing new user type", e);
			throw new IllegalStateException("Failed to initialize user type: " + e.getMessage(), e);
		}
	}
}
