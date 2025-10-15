package tech.derbent.meetings.service;

import java.time.Clock;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.services.CTypeEntityService;
import tech.derbent.meetings.domain.CMeetingType;
import tech.derbent.session.service.ISessionService;

/** CMeetingTypeService - Service layer for CMeetingType entity. Layer: Service (MVC) Handles business logic for project-aware meeting type
 * operations. */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CMeetingTypeService extends CTypeEntityService<CMeetingType> {
	CMeetingTypeService(final IMeetingTypeRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Checks dependencies before allowing meeting type deletion. Prevents deletion if the type is being used by any meetings. Always calls
	 * super.checkDeleteAllowed() first to ensure all parent-level checks (null validation, non-deletable flag) are performed.
	 * @param entity the meeting type entity to check
	 * @return null if type can be deleted, error message otherwise */
	@Override
	public String checkDeleteAllowed(final CMeetingType entity) {
		// Call super class first to check common dependencies
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		// No specific dependencies to check yet - stub for future implementation
		return null;
	}

	@Override
	protected Class<CMeetingType> getEntityClass() { return CMeetingType.class; }

	/** Initializes a new meeting type with default values based on current session and available data. Sets: - Project from current session - User
	 * for creation tracking - Auto-generated name - Default color and icon - Default sort order - Not marked as non-deletable
	 * @param entity the newly created meeting type to initialize
	 * @throws IllegalStateException if required fields cannot be initialized */
	@Override
	public void initializeNewEntity(final CMeetingType entity) {
		super.initializeNewEntity(entity);
		setNameOfEntity(entity, "Meeting Type");
	}
}
