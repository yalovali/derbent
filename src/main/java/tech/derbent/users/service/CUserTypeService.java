package tech.derbent.users.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.api.utils.Check;
import tech.derbent.session.service.ISessionService;
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
}
