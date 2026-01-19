package tech.derbent.bab.project.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.projects.service.CProjectService;
import tech.derbent.api.projects.service.CProjectTypeService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.bab.project.domain.CProject_Bab;
import tech.derbent.base.session.service.ISessionService;

@Service
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public class CProject_BabService extends CProjectService<CProject_Bab> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CProject_BabService.class);

	public CProject_BabService(final IProject_BabRepository repository, final Clock clock, final ISessionService sessionService,
			final ApplicationEventPublisher eventPublisher, final CProjectTypeService projectTypeService,
			final CProjectItemStatusService projectItemStatusService) {
		super(repository, clock, sessionService, eventPublisher, projectTypeService, projectItemStatusService);
	}

	@Override
	@Transactional
	public CProject_Bab createEntity() {
		try {
			final CProject_Bab entity = new CProject_Bab();
			initializeNewEntity(entity);
			return entity;
		} catch (final Exception e) {
			LOGGER.error("Failed to create BAB project entity: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to create BAB project instance", e);
		}
	}

	@Override
	public Class<CProject_Bab> getEntityClass() { return CProject_Bab.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CProject_BabInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceProject_Bab.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public CProject_Bab newEntity() {
		final CProject_Bab entity = new CProject_Bab();
		try {
			initializeNewEntity(entity);
		} catch (final Exception e) {
			LOGGER.warn("Failed to initialize new BAB project entity: {}", e.getMessage());
		}
		return entity;
	}
}
