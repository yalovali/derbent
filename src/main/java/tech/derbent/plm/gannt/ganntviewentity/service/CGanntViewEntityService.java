package tech.derbent.plm.gannt.ganntviewentity.service;

import java.time.Clock;
import java.util.Optional;
import org.springframework.stereotype.Service;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.gannt.ganntviewentity.domain.CGanntViewEntity;

@Service
public class CGanntViewEntityService extends CEntityOfProjectService<CGanntViewEntity> implements IEntityRegistrable, IEntityWithView {

	public static void createSample(final CGanntViewEntityService service, final CProject<?> project) {
		final CGanntViewEntity entity = new CGanntViewEntity("Sample Gannt View", project);
		service.save(entity);
	}

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
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
	}

	@Override
	protected void validateEntity(final CGanntViewEntity entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		// 2. Length Check
		if (entity.getName().length() > CEntityConstants.MAX_LENGTH_NAME) {
			throw new CValidationException(ValidationMessages.formatMaxLength(ValidationMessages.NAME_MAX_LENGTH, CEntityConstants.MAX_LENGTH_NAME));
		}
		// 3. Unique Name Check
		final Optional<CGanntViewEntity> existing =
				((IGanntViewEntityRepository) repository).findByNameAndProject(entity.getName(), entity.getProject());
		if (existing.isPresent() && !existing.get().getId().equals(entity.getId())) {
			throw new CValidationException(ValidationMessages.DUPLICATE_NAME.formatted(entity.getName()));
		}
	}
}
