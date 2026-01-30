package tech.derbent.api.screens.service;

import java.time.Clock;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.screens.domain.CMasterSection;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
public class CMasterSectionService extends CEntityOfProjectService<CMasterSection> implements IEntityRegistrable, IEntityWithView {

	public static List<String> getAvailableTypes() {
		return List.of("Grid Chart", "Gannt", "None"); // Replace with actual types
	}

	public CMasterSectionService(final IMasterSectionRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final CMasterSection entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	public Class<CMasterSection> getEntityClass() { return CMasterSection.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CMasterInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceMasterSection.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
	}
}
