package tech.derbent.plm.agile.service;

import java.time.Clock;
import java.util.Optional;
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
import tech.derbent.plm.agile.domain.CUserStory;

/**
 * Service for agile user stories.
 *
 * <p>User stories inherit most behavior from the shared agile service stack, so this class only
 * supplies the concrete repository and type service bindings.</p>
 */
@Profile({"derbent", "default"})
@Service
@PreAuthorize ("isAuthenticated()")
public class CUserStoryService extends CAgileEntityService<CUserStory> implements IEntityRegistrable, IEntityWithView {

	private final CUserStoryTypeService typeService;

	public CUserStoryService(final IUserStoryRepository repository, final Clock clock, final ISessionService sessionService,
			final CUserStoryTypeService userStoryTypeService, final CProjectItemStatusService statusService,
			final CActivityPriorityService activityPriorityService) {
		super(repository, clock, sessionService, statusService, activityPriorityService);
		typeService = userStoryTypeService;
	}

	@Override
	public Optional<CUserStory> findByNameAndProject(final String name, final CProject<?> project) {
		return ((IUserStoryRepository) repository).findByNameAndProject(name, project);
	}

	@Override
	public Class<CUserStory> getEntityClass() { return CUserStory.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CUserStoryInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceUserStory.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	protected IProjectItemRespository<CUserStory> getTypedRepository() { return (IProjectItemRespository<CUserStory>) repository; }

	@Override
	protected CTypeEntityService<?> getTypeService() { return typeService; }
}
