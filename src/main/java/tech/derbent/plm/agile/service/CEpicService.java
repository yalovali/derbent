package tech.derbent.plm.agile.service;

import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.entityOfProject.service.IProjectItemRespository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.plm.activities.service.CActivityPriorityService;
import tech.derbent.plm.agile.domain.CEpic;

@Profile("derbent")
@Service
@PreAuthorize ("isAuthenticated()")
public class CEpicService extends CAgileEntityService<CEpic> implements IEntityRegistrable, IEntityWithView {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CEpicService.class);
	private final CEpicTypeService typeService;

	public CEpicService(final IEpicRepository repository, final Clock clock, final ISessionService sessionService,
			final CEpicTypeService epicTypeService, final CProjectItemStatusService statusService,
			final CActivityPriorityService activityPriorityService) {
		super(repository, clock, sessionService, statusService, activityPriorityService);
		typeService = epicTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CEpic epic) {
		return super.checkDeleteAllowed(epic);
	}

	@Override
	public Optional<CEpic> findByNameAndProject(final String name, final CProject<?> project) {
		return ((IEpicRepository) repository).findByNameAndProject(name, project);
	}

	@Override
	public Class<CEpic> getEntityClass() { return CEpic.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CEpicInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceEpic.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	protected IProjectItemRespository<CEpic> getTypedRepository() { return (IProjectItemRespository<CEpic>) repository; }

	@Override
	protected CTypeEntityService<?> getTypeService() { return typeService; }
}
