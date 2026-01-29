package tech.derbent.plm.products.productversion.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.products.productversion.domain.CProductVersion;
import tech.derbent.plm.products.productversiontype.service.CProductVersionTypeService;

@Service
@PreAuthorize ("isAuthenticated()")
@PermitAll
public class CProductVersionService extends CProjectItemService<CProductVersion> implements IEntityRegistrable, IEntityWithView {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CProductVersionService.class);
	private final CProductVersionTypeService typeService;

	CProductVersionService(final IProductVersionRepository repository, final Clock clock, final ISessionService sessionService,
			final CProductVersionTypeService productversionTypeService, final CProjectItemStatusService statusService) {
		super(repository, clock, sessionService, statusService);
		typeService = productversionTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CProductVersion entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	public Class<CProductVersion> getEntityClass() { return CProductVersion.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CProductVersionInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceProductVersion.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	/**
	 * Copy CProductVersion-specific fields from source to target entity.
	 * Uses direct setter/getter calls for clarity.
	 * 
	 * @param source  the source entity to copy from
	 * @param target  the target entity to copy to
	 * @param options clone options controlling what fields to copy
	 */
	@Override
	public void copyEntityFieldsTo(final CProductVersion source, final CEntityDB<?> target,
			final CCloneOptions options) {
		super.copyEntityFieldsTo(source, target, options);
		
		if (!(target instanceof CProductVersion)) {
			return;
		}
		final CProductVersion targetVersion = (CProductVersion) target;
		
		// Copy basic fields
		targetVersion.setVersionNumber(source.getVersionNumber());
		
		// Copy relations conditionally
		if (options.includesRelations()) {
			targetVersion.setProduct(source.getProduct());
		}
		
		LOGGER.debug("Copied {} '{}' with options: {}", getClass().getSimpleName(), source.getName(), options);
	}

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		initializeNewEntity_IHasStatusAndWorkflow((IHasStatusAndWorkflow<?>) entity, sessionService.getActiveCompany().orElseThrow(), typeService,
				statusService);
	}

	@Override
	protected void validateEntity(final CProductVersion entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getProduct(), "Product is required");
		Check.notNull(entity.getEntityType(), "Version Type is required");
		
		// 2. Length Checks - Use validateStringLength helper
		validateStringLength(entity.getVersionNumber(), "Version Number", 50);
		
		// 3. Unique Checks
		validateUniqueNameInProject((IProductVersionRepository) repository, entity, entity.getName(), entity.getProject());
	}
}
