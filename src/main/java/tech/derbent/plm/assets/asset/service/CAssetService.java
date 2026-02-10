package tech.derbent.plm.assets.asset.service;

import java.math.BigDecimal;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.plm.assets.asset.domain.CAsset;
import tech.derbent.plm.assets.assettype.service.CAssetTypeService;

@Profile("derbent")
@Service
@PreAuthorize ("isAuthenticated()")
@PermitAll
public class CAssetService extends CProjectItemService<CAsset> implements IEntityRegistrable, IEntityWithView {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CAssetService.class);
	private final CAssetTypeService typeService;

	CAssetService(final IAssetRepository repository, final Clock clock, final ISessionService sessionService,
			final CAssetTypeService assetTypeService, final CProjectItemStatusService statusService) {
		super(repository, clock, sessionService, statusService);
		typeService = assetTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CAsset entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	public Class<CAsset> getEntityClass() { return CAsset.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CAssetInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceAsset.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		initializeNewEntity_IHasStatusAndWorkflow((IHasStatusAndWorkflow<?>) entity, sessionService.getActiveCompany().orElseThrow(), typeService,
				statusService);
	}

	@Override
	protected void validateEntity(final CAsset entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getEntityType(), "Asset type is required");
		
		// 2. Length Checks - Use validateStringLength helper
		validateStringLength(entity.getBrand(), "Brand", 255);
		validateStringLength(entity.getModel(), "Model", 255);
		validateStringLength(entity.getSerialNumber(), "Serial Number", 255);
		validateStringLength(entity.getInventoryNumber(), "Inventory Number", 255);
		validateStringLength(entity.getLocation(), "Location", 500);
		
		// 3. Unique Checks
		validateUniqueNameInProject((IAssetRepository) repository, entity, entity.getName(), entity.getProject());
		
		// 4. Numeric Checks - USE STATIC HELPER
		validateNumericField(entity.getFullAmount(), "Full Amount", new BigDecimal("99999999999.99"));
		validateNumericField(entity.getPurchaseValue(), "Purchase Value", new BigDecimal("99999999999.99"));
		validateNumericField(entity.getUntaxedAmount(), "Untaxed Amount", new BigDecimal("99999999999.99"));
		if (entity.getWarrantyDuration() != null && entity.getWarrantyDuration() < 0) {
			throw new IllegalArgumentException("Warranty Duration cannot be negative");
		}
		if (entity.getDepreciationPeriod() != null && entity.getDepreciationPeriod() < 0) {
			throw new IllegalArgumentException("Depreciation Period cannot be negative");
		}
	}
}
