package tech.derbent.plm.assets.asset.service;

import java.math.BigDecimal;
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
import tech.derbent.plm.assets.asset.domain.CAsset;
import tech.derbent.plm.assets.assettype.service.CAssetTypeService;

@Service
@PreAuthorize ("isAuthenticated()")
@Menu (icon = "vaadin:file-o", title = "Settings.Assets")
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
		// 2. Length Checks
		if (entity.getName().length() > CEntityConstants.MAX_LENGTH_NAME) {
			throw new IllegalArgumentException(
					ValidationMessages.formatMaxLength(ValidationMessages.NAME_MAX_LENGTH, CEntityConstants.MAX_LENGTH_NAME));
		}
		if (entity.getBrand() != null && entity.getBrand().length() > 255) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Brand cannot exceed %d characters", 255));
		}
		if (entity.getModel() != null && entity.getModel().length() > 255) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Model cannot exceed %d characters", 255));
		}
		if (entity.getSerialNumber() != null && entity.getSerialNumber().length() > 255) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Serial Number cannot exceed %d characters", 255));
		}
		if (entity.getInventoryNumber() != null && entity.getInventoryNumber().length() > 255) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Inventory Number cannot exceed %d characters", 255));
		}
		if (entity.getLocation() != null && entity.getLocation().length() > 500) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Location cannot exceed %d characters", 500));
		}
		// 3. Unique Checks
		final Optional<CAsset> existingName = ((IAssetRepository) repository).findByNameAndProject(entity.getName(), entity.getProject());
		if (existingName.isPresent() && !existingName.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_PROJECT);
		}
		// 4. Numeric Checks
		validateNumericField(entity.getFullAmount(), "Full Amount");
		validateNumericField(entity.getPurchaseValue(), "Purchase Value");
		validateNumericField(entity.getUntaxedAmount(), "Untaxed Amount");
		if (entity.getWarrantyDuration() != null && entity.getWarrantyDuration() < 0) {
			throw new IllegalArgumentException("Warranty Duration cannot be negative");
		}
		if (entity.getDepreciationPeriod() != null && entity.getDepreciationPeriod() < 0) {
			throw new IllegalArgumentException("Depreciation Period cannot be negative");
		}
	}

	private void validateNumericField(BigDecimal value, String fieldName) {
		if (value != null && value.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException(fieldName + " must be positive");
		}
	}
}
