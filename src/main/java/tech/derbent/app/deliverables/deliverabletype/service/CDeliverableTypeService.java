package tech.derbent.app.deliverables.deliverabletype.service;

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
import tech.derbent.app.deliverables.deliverable.service.IDeliverableRepository;
import tech.derbent.app.deliverables.deliverabletype.domain.CDeliverableType;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CDeliverableTypeService extends CTypeEntityService<CDeliverableType> implements IEntityRegistrable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDeliverableTypeService.class);
	@Autowired
	private IDeliverableRepository deliverableRepository;

	public CDeliverableTypeService(final IDeliverableTypeRepository repository, final Clock clock, final ISessionService sessionService,
			final IDeliverableRepository deliverableRepository) {
		super(repository, clock, sessionService);
		this.deliverableRepository = deliverableRepository;
	}

	@Override
	public String checkDeleteAllowed(final CDeliverableType entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		try {
			final long usageCount = deliverableRepository.countByType(entity);
			if (usageCount > 0) {
				return String.format("Cannot delete. It is being used by %d item%s.", usageCount, usageCount == 1 ? "" : "s");
			}
			return null;
		} catch (final Exception e) {
			LOGGER.error("Error checking dependencies for deliverable type: {}", entity.getName(), e);
			return "Error checking dependencies: " + e.getMessage();
		}
	}

	@Override
	public Class<CDeliverableType> getEntityClass() { return CDeliverableType.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CDeliverableTypeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceDeliverableType.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final CDeliverableType entity) {
		super.initializeNewEntity(entity);
		CProject activeProject = sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project in session"));
		long typeCount = ((IDeliverableTypeRepository) repository).countByProject(activeProject);
		String autoName = String.format("DeliverableType %02d", typeCount + 1);
		entity.setName(autoName);
	}
}
