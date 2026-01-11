package tech.derbent.app.projects.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.projects.domain.CProjectType;
import tech.derbent.base.session.service.ISessionService;

/** CProjectTypeService - Service layer for CProjectType entity. 
 * Layer: Service (MVC) 
 * Handles business logic for project-aware project type operations. */
@Service
@PreAuthorize("isAuthenticated()")
@Transactional(readOnly = true)
public class CProjectTypeService extends CTypeEntityService<CProjectType> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectTypeService.class);
	@Autowired
	private final IProjectRepository projectRepository;

	public CProjectTypeService(final IProjectTypeRepository repository, final Clock clock, final ISessionService sessionService,
			final IProjectRepository projectRepository) {
		super(repository, clock, sessionService);
		this.projectRepository = projectRepository;
	}

	/** Checks dependencies before allowing project type deletion. 
	 * Prevents deletion if the type is being used by any projects. 
	 * Always calls super.checkDeleteAllowed() first to ensure all parent-level checks 
	 * (null validation, non-deletable flag) are performed.
	 * @param entity the project type entity to check
	 * @return null if type can be deleted, error message otherwise */
	@Override
	public String checkDeleteAllowed(final CProjectType entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		try {
			// Check if any projects are using this type
			final long usageCount = projectRepository.countByType(entity);
			if (usageCount > 0) {
				return String.format("Cannot delete. It is being used by %d project%s.", usageCount, usageCount == 1 ? "" : "s");
			}
			return null; // Type can be deleted
		} catch (final Exception e) {
			LOGGER.error("Error checking dependencies for project type: {}", entity.getName(), e);
			return "Error checking dependencies: " + e.getMessage();
		}
	}

	@Override
	public Class<CProjectType> getEntityClass() {
		return CProjectType.class;
	}

	@Override
	public Class<?> getInitializerServiceClass() {
		return CProjectTypeInitializerService.class;
	}

	@Override
	public Class<?> getPageServiceClass() {
		return CPageServiceProjectType.class;
	}

	@Override
	public Class<?> getServiceClass() {
		return this.getClass();
	}

	/** Initializes a new project type. Most common fields are initialized by super class.
	 * @param entity the newly created project type to initialize */
	@Override
	public void initializeNewEntity(final CProjectType entity) {
		super.initializeNewEntity(entity);
		final CProject activeProject = sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project in session"));
		final long typeCount = ((IProjectTypeRepository) repository).countByProject(activeProject);
		final String autoName = String.format("ProjectType %02d", typeCount + 1);
		entity.setName(autoName);
	}
}
