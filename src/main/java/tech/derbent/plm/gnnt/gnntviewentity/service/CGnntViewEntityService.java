package tech.derbent.plm.gnnt.gnntviewentity.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.plm.gnnt.gnntviewentity.domain.CGnntViewEntity;

@Service
@Profile({"derbent", "default"})
@PreAuthorize("isAuthenticated()")
public class CGnntViewEntityService extends CEntityOfProjectService<CGnntViewEntity> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CGnntViewEntityService.class);

	public CGnntViewEntityService(final IGnntViewEntityRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final CGnntViewEntity entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	public void copyEntityFieldsTo(final CGnntViewEntity source, final CEntityDB<?> target, final CCloneOptions options) {
		super.copyEntityFieldsTo(source, target, options);
		if (!(target instanceof CGnntViewEntity)) {
			return;
		}
		LOGGER.debug("Copied CGnntViewEntity '{}' with options: {}", source.getName(), options);
	}

	@Override
	public Class<CGnntViewEntity> getEntityClass() {
		return CGnntViewEntity.class;
	}

	@Override
	public Class<?> getInitializerServiceClass() {
		return CGnntViewEntityInitializerService.class;
	}

	@Override
	public Class<?> getPageServiceClass() {
		return CPageServiceGnntViewEntity.class;
	}

	@Override
	public Class<?> getServiceClass() {
		return getClass();
	}

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
	}

	@Override
	protected void validateEntity(final CGnntViewEntity entity) {
		super.validateEntity(entity);
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		validateUniqueNameInProject((IGnntViewEntityRepository) repository, entity, entity.getName(), entity.getProject());
	}
}
