package tech.derbent.gannt.service;

import java.time.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.gannt.domain.CGanntViewEntity;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.ISessionService;

@Service
public class CGanntViewEntityService extends CEntityOfProjectService<CGanntViewEntity> {

	public static void createSample(final CGanntViewEntityService service, final CProject project) {
		final CGanntViewEntity entity = new CGanntViewEntity("Sample Gannt View", project);
		service.save(entity);
	}

	@Autowired
	public CGanntViewEntityService(final IGanntViewEntityRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	protected Class<CGanntViewEntity> getEntityClass() { return CGanntViewEntity.class; }

	@Override
	public String checkDependencies(final CGanntViewEntity entity) {
		return super.checkDependencies(entity);
	}

	@Override
	public void initializeNewEntity(final CGanntViewEntity entity) {
		super.initializeNewEntity(entity);
		// Additional entity-specific initialization can be added here if needed
	}
}
