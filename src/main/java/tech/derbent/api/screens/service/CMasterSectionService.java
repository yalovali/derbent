package tech.derbent.api.screens.service;

import java.time.Clock;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.screens.domain.CMasterSection;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
public class CMasterSectionService extends CEntityOfProjectService<CMasterSection> {

	public CMasterSectionService(final IMasterSectionRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final CMasterSection entity) {
		return super.checkDeleteAllowed(entity);
	}

	public List<String> getAvailableTypes() {
		return List.of("Grid Chart", "Gannt", "None"); // Replace with actual types
	}

	@Override
	protected Class<CMasterSection> getEntityClass() { return CMasterSection.class; }

	@Override
	public void initializeNewEntity(final CMasterSection entity) {
		super.initializeNewEntity(entity);
		// Additional entity-specific initialization can be added here if needed
	}
}
