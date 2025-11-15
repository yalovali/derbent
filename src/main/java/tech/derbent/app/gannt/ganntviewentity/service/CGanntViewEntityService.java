package tech.derbent.app.gannt.ganntviewentity.service;

import java.time.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.app.gannt.ganntviewentity.domain.CGanntViewEntity;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.base.session.service.ISessionService;

@Service
public class CGanntViewEntityService extends CEntityOfProjectService<CGanntViewEntity> implements IEntityRegistrable {

	public static void createSample(final CGanntViewEntityService service, final CProject project) {
		final CGanntViewEntity entity = new CGanntViewEntity("Sample Gannt View", project);
		service.save(entity);
	}

	@Autowired
	public CGanntViewEntityService(final IGanntViewEntityRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final CGanntViewEntity entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	public Class<CGanntViewEntity> getEntityClass() { return CGanntViewEntity.class; }

	@Override
	public Class<?> getInitializerServiceClass() { // TODO Auto-generated method stub
		return CGanntViewEntityInitializerService.class;
	}

	@Override
	public Class<?> getPageServiceClass() { // TODO Auto-generated method stub
		return CPageServiceGanntViewEntity.class;
	}

	@Override
	public Class<?> getServiceClass() { // TODO Auto-generated method stub
		return this.getClass();
	}

	@Override
	public void initializeNewEntity(final CGanntViewEntity entity) {
		super.initializeNewEntity(entity);
		// Additional entity-specific initialization can be added here if needed
	}
}
