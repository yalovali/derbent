package tech.derbent.app.projectexpenses.projectexpensetype.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.projectexpenses.projectexpense.service.IProjectExpenseRepository;
import tech.derbent.app.projectexpenses.projectexpensetype.domain.CProjectExpenseType;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CProjectExpenseTypeService extends CTypeEntityService<CProjectExpenseType> implements IEntityRegistrable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectExpenseTypeService.class);
	@Autowired
	private IProjectExpenseRepository projectexpenseRepository;

	public CProjectExpenseTypeService(final IProjectExpenseTypeRepository repository, final Clock clock, final ISessionService sessionService,
			final IProjectExpenseRepository projectexpenseRepository) {
		super(repository, clock, sessionService);
		this.projectexpenseRepository = projectexpenseRepository;
	}

	@Override
	public String checkDeleteAllowed(final CProjectExpenseType entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		try {
			final long usageCount = projectexpenseRepository.countByType(entity);
			if (usageCount > 0) {
				return String.format("Cannot delete. It is being used by %d item%s.", usageCount, usageCount == 1 ? "" : "s");
			}
			return null;
		} catch (final Exception e) {
			LOGGER.error("Error checking dependencies for projectexpense type: {}", entity.getName(), e);
			return "Error checking dependencies: " + e.getMessage();
		}
	}

	@Override
	public Class<CProjectExpenseType> getEntityClass() { return CProjectExpenseType.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CProjectExpenseTypeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceProjectExpenseType.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final CProjectExpenseType entity) {
		super.initializeNewEntity(entity);
		CProject activeProject = sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project in session"));
		long typeCount = ((IProjectExpenseTypeRepository) repository).countByProject(activeProject);
		String autoName = String.format("ProjectExpenseType %02d", typeCount + 1);
		entity.setName(autoName);
	}
}
