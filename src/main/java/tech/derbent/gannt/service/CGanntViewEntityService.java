package tech.derbent.gannt.service;

import java.time.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.gannt.domain.CGanntViewEntity;

@Service
public class CGanntViewEntityService extends CEntityOfProjectService<CGanntViewEntity> {
	@Autowired
	public CGanntViewEntityService(final CGanntViewEntityRepository repository, final Clock clock) {
		super(repository, clock);
	}

	@Override
	protected Class<CGanntViewEntity> getEntityClass() { return CGanntViewEntity.class; }
}
