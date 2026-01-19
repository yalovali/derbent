package tech.derbent.plm.products.product.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.products.product.domain.CProduct;
import tech.derbent.plm.products.producttype.service.CProductTypeService;

@Service
@PreAuthorize ("isAuthenticated()")
@Menu (icon = "vaadin:file-o", title = "Settings.Products")
@PermitAll
public class CProductService extends CProjectItemService<CProduct> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CProductService.class);
	private final CProductTypeService productTypeService;

	CProductService(final IProductRepository repository, final Clock clock, final ISessionService sessionService,
			final CProductTypeService productTypeService, final CProjectItemStatusService projectItemStatusService) {
		super(repository, clock, sessionService, projectItemStatusService);
		this.productTypeService = productTypeService;
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

	@SuppressWarnings ("null")
	@Override
	public void initializeNewEntity(final CProduct entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new product entity");
		final CProject<?> currentProject = sessionService.getActiveProject().orElseThrow(() -> new CInitializationException("No active project"));
		IHasStatusAndWorkflowService.initializeNewEntity(entity, currentProject, productTypeService, projectItemStatusService);
		LOGGER.debug("Product initialization complete");
	}
}
