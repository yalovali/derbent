package tech.derbent.app.components.componenttype.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.app.components.component.service.IProjectComponentRepository;
import tech.derbent.app.components.componenttype.domain.CProjectComponentType;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CProjectComponentTypeService extends CTypeEntityService<CProjectComponentType> implements IEntityRegistrable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectComponentTypeService.class);
	@Autowired
	private final IProjectComponentRepository componentRepository;

	public CProjectComponentTypeService(final IProjectComponentTypeRepository repository, final Clock clock, final ISessionService sessionService,
			final IProjectComponentRepository componentRepository) {
		super(repository, clock, sessionService);
		this.componentRepository = componentRepository;
	}

	@Override
	public String checkDeleteAllowed(final CProjectComponentType entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		try {
			final long usageCount = componentRepository.countByType(entity);
			if (usageCount > 0) {
				return String.format("Cannot delete. It is being used by %d item%s.", usageCount, usageCount == 1 ? "" : "s");
			}
			return null;
		} catch (final Exception e) {
			LOGGER.error("Error checking dependencies: {}", entity.getName(), e);
			return "Error checking dependencies: " + e.getMessage();
		}
	}

	@Override
	public Class<CProjectComponentType> getEntityClass() { return CProjectComponentType.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CProjectComponentTypeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceProjectComponentType.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final CProjectComponentType entity) {
		super.initializeNewEntity(entity);
		final CProject activeProject = sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project in session"));
		final long typeCount = ((IProjectComponentTypeRepository) repository).countByProject(activeProject);
		final String autoName = String.format("ComponentType %02d", typeCount + 1);
		entity.setName(autoName);
	}
}
