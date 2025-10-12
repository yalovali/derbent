package tech.derbent.screens.service;

import java.time.Clock;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.screens.domain.CMasterSection;
import tech.derbent.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
public class CMasterSectionService extends CEntityOfProjectService<CMasterSection> {

	public CMasterSectionService(final IMasterSectionRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	public List<String> getAvailableTypes() {
		return List.of("Grid Chart", "Gannt", "None"); // Replace with actual types
	}

	@Override
	protected Class<CMasterSection> getEntityClass() { return CMasterSection.class; }

	@Override
	public String checkDependencies(final CMasterSection entity) {
		final String superCheck = super.checkDependencies(entity);
		if (superCheck != null) {
			return superCheck;
		}
		return null;
	}

	@Override
	public void initializeNewEntity(final CMasterSection entity) {
		super.initializeNewEntity(entity);
		tech.derbent.api.utils.Check.notNull(entity, "Entity cannot be null");
		// Stub for future implementation
	}
}
