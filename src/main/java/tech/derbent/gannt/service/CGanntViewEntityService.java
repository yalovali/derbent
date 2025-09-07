package tech.derbent.gannt.service;

import java.time.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.gannt.domain.CGanntViewEntity;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.CSessionService;

@Service
public class CGanntViewEntityService extends CEntityOfProjectService<CGanntViewEntity> {

	public static void createSample(final CGanntViewEntityService service, final CProject project) {
		final CGanntViewEntity entity = new CGanntViewEntity("Sample Gannt View", project);
		service.save(entity);
	}

	@Autowired
	public CGanntViewEntityService(final CGanntViewEntityRepository repository, final Clock clock, final CSessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	protected Class<CGanntViewEntity> getEntityClass() { return CGanntViewEntity.class; }
}
