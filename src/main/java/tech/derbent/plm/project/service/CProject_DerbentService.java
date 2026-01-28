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
import tech.derbent.api.utils.Check;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.project.domain.CProject_Derbent;

@Service
@Profile ({
		"derbent", "default"
})
@PreAuthorize ("isAuthenticated()")
public class CProject_DerbentService extends CProjectService<CProject_Derbent> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CProject_DerbentService.class);

	public CProject_DerbentService(final IProject_DerbentRepository repository, final Clock clock, final ISessionService sessionService,
			final ApplicationEventPublisher eventPublisher, final CProjectTypeService projectTypeService,
			final CProjectItemStatusService statusService) {
		super(repository, clock, sessionService, eventPublisher, projectTypeService, statusService);
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
		try {
			return new CProject_Derbent("New Project",
					sessionService.getActiveCompany().orElseThrow(() -> new IllegalStateException("No active company for Derbent project creation")));
		} catch (final Exception e) {
			LOGGER.error("Failed to create Derbent project entity: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to create Derbent project instance", e);
		}
	}

	@Override
	@Transactional
	public CProject_Derbent save(final CProject_Derbent entity) {
		// Validation is now handled in validateEntity called by super.save()
		return super.save(entity);
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
}
