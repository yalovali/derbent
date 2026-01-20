package tech.derbent.plm.gannt.ganntviewentity.service;

import java.time.Clock;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.gannt.ganntviewentity.domain.CGanntViewEntity;

@Service
public class CGanntViewEntityService extends CEntityOfProjectService<CGanntViewEntity> implements IEntityRegistrable, IEntityWithView {

	public static void createSample(final CGanntViewEntityService service, final CProject<?> project) {
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
	public Class<?> getInitializerServiceClass() { return CGanntViewEntityInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceGanntViewEntity.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final CGanntViewEntity entity) {
		super.initializeNewEntity(entity);
		// Additional entity-specific initialization can be added here if needed
	}

	@Override
	protected void validateEntity(final CGanntViewEntity entity) throws CValidationException {
		super.validateEntity(entity);
		final Optional<CGanntViewEntity> existing = repository.findByNameAndProject(entity.getName(), entity.getProject());
		if (existing.isPresent() && !existing.get().getId().equals(entity.getId())) {
			throw new CValidationException(String.format(ValidationMessages.DUPLICATE_NAME, entity.getName()));
		}
	}
}
