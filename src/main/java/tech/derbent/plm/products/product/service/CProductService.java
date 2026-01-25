package tech.derbent.plm.products.product.service;

import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.products.product.domain.CProduct;
import tech.derbent.plm.products.producttype.service.CProductTypeService;

@Service
@PreAuthorize ("isAuthenticated()")
@Menu (icon = "vaadin:file-o", title = "Settings.Products")
@PermitAll
public class CProductService extends CProjectItemService<CProduct> implements IEntityRegistrable, IEntityWithView {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CProductService.class);
	private final CProductTypeService typeService;

	CProductService(final IProductRepository repository, final Clock clock, final ISessionService sessionService,
			final CProductTypeService productTypeService, final CProjectItemStatusService statusService) {
		super(repository, clock, sessionService, statusService);
		typeService = productTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CProduct entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	public Class<CProduct> getEntityClass() { return CProduct.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CProductInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceProduct.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		initializeNewEntity_IHasStatusAndWorkflow((IHasStatusAndWorkflow<?>) entity, sessionService.getActiveCompany().orElseThrow(), typeService,
				statusService);
	}

	@Override
	protected void validateEntity(final CProduct entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getEntityType(), "Product type is required");
		// 2. Length Checks
		if (entity.getName().length() > CEntityConstants.MAX_LENGTH_NAME) {
			throw new IllegalArgumentException(
					ValidationMessages.formatMaxLength(ValidationMessages.NAME_MAX_LENGTH, CEntityConstants.MAX_LENGTH_NAME));
		}
		// 3. Unique Checks
		// Name must be unique within project
		final Optional<CProduct> existingName = ((IProductRepository) repository).findByNameAndProject(entity.getName(), entity.getProject());
		if (existingName.isPresent() && !existingName.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_PROJECT);
		}
		// Product code unique in project (if set)
		if (entity.getProductCode() != null && !entity.getProductCode().isBlank()) {
			// Note: Assuming a custom query or stream filtering if repository method doesn't exist
			// For now, implementing via stream as IProductRepository structure isn't fully visible but likely standard
			// Ideally should be: repository.findByProductCodeAndProject(...)
			final boolean duplicateCode =
					repository.findAll().stream().anyMatch(p -> p.getProject().equals(entity.getProject()) && p.getProductCode() != null
							&& p.getProductCode().equalsIgnoreCase(entity.getProductCode()) && !p.getId().equals(entity.getId()));
			if (duplicateCode) {
				throw new IllegalArgumentException("Product code must be unique within the project");
			}
		}
	}
}
