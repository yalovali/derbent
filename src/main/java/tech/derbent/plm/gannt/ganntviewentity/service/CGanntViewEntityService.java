package tech.derbent.plm.gannt.ganntviewentity.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.gannt.ganntviewentity.domain.CGanntViewEntity;

@Service
public class CGanntViewEntityService extends CEntityOfProjectService<CGanntViewEntity> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CGanntViewEntityService.class);

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

	/**
	 * Service-level method to copy CGanntViewEntity-specific fields.
	 * Uses direct setter/getter calls for clarity.
	 * 
	 * @param source  the source entity to copy from
	 * @param target  the target entity to copy to
	 * @param options clone options controlling what fields to copy
	 */
	@Override
	public void copyEntityFieldsTo(final CGanntViewEntity source, final CEntityDB<?> target, final CCloneOptions options) {
		super.copyEntityFieldsTo(source, target, options);
		
		if (!(target instanceof CGanntViewEntity)) {
			return;
		}
		// CGanntViewEntity has no additional fields beyond base class
		
		LOGGER.debug("Copied CGanntViewEntity '{}' with options: {}", source.getName(), options);
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
		
		// Use validation helper for unique name check
		validateUniqueNameInProject((IGanntViewEntityRepository) repository, entity, entity.getName(), entity.getProject());
	}
}
