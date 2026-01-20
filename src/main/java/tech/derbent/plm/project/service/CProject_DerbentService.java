package tech.derbent.plm.project.service;

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
import tech.derbent.api.utils.Check;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.plm.project.domain.CProject_Derbent;

@Service
@Profile ({
		"derbent", "default"
})
@PreAuthorize ("isAuthenticated()")
public class CProject_DerbentService extends CProjectService<CProject_Derbent> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CProject_DerbentService.class);

	public CProject_DerbentService(final IProject_DerbentRepository repository, final Clock clock, final ISessionService sessionService,
			final ApplicationEventPublisher eventPublisher, final CProjectTypeService projectTypeService,
			final CProjectItemStatusService projectItemStatusService) {
		super(repository, clock, sessionService, eventPublisher, projectTypeService, projectItemStatusService);
	}

	@Override
	@Transactional
	public CProject_Derbent createEntity() {
		try {
			final CProject_Derbent entity = new CProject_Derbent();
			initializeNewEntity(entity);
			return entity;
		} catch (final Exception e) {
			LOGGER.error("Failed to create Derbent project entity: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to create Derbent project instance", e);
		}
	}

	@Override
	public Class<CProject_Derbent> getEntityClass() { return CProject_Derbent.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CProject_DerbentInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceProject_Derbent.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public CProject_Derbent newEntity() {
		final CProject_Derbent entity = new CProject_Derbent();
		try {
			initializeNewEntity(entity);
		} catch (final Exception e) {
			LOGGER.warn("Failed to initialize new Derbent project entity: {}", e.getMessage());
		}
		return entity;
	}

	@Override
	protected void validateEntity(final CProject_Derbent entity) {
		super.validateEntity(entity);
		
		// 1. Kanban Line Check
		if (entity.getKanbanLine() != null) {
			Check.isSameCompany(entity, entity.getKanbanLine());
		}
		
		// 2. Base Project Constraints (already handled by super, but explicit checks here if needed)
		// Name is checked in CProjectService -> CEntityNamedService
		
		// 3. Unique Checks (Project Name unique in company) - Handled in CProjectService
	}

	@Override
	@Transactional
	public CProject_Derbent save(final CProject_Derbent entity) {
		// Validation is now handled in validateEntity called by super.save()
		return super.save(entity);
	}
}
