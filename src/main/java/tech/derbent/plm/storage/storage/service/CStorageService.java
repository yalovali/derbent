package tech.derbent.plm.storage.storage.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.storage.storage.domain.CStorage;
import tech.derbent.plm.storage.storagetype.service.CStorageTypeService;

@Service
@PreAuthorize ("isAuthenticated()")
@PermitAll
public class CStorageService extends CProjectItemService<CStorage> implements IEntityRegistrable, IEntityWithView {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CStorageService.class);
	private final CStorageTypeService typeService;

	public CStorageService(final IStorageRepository repository, final Clock clock, final ISessionService sessionService,
			final CStorageTypeService storageTypeService, final CProjectItemStatusService statusService) {
		super(repository, clock, sessionService, statusService);
		typeService = storageTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CStorage entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	public Class<CStorage> getEntityClass() { return CStorage.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CStorageInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceStorage.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		initializeNewEntity_IHasStatusAndWorkflow((IHasStatusAndWorkflow<?>) entity, sessionService.getActiveCompany().orElseThrow(), typeService,
				statusService);
	}

	@Override
	protected void validateEntity(final CStorage entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		if (entity.getAddress() != null && entity.getAddress().length() > 500) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Address cannot exceed %d characters", 500));
		}
		if (entity.getBuilding() != null && entity.getBuilding().length() > 255) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Building cannot exceed %d characters", 255));
		}
		if (entity.getFloor() != null && entity.getFloor().length() > 255) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Floor cannot exceed %d characters", 255));
		}
		if (entity.getZone() != null && entity.getZone().length() > 255) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Zone cannot exceed %d characters", 255));
		}
		if (entity.getBinCode() != null && entity.getBinCode().length() > 255) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Bin Code cannot exceed %d characters", 255));
		}
		if (entity.getCapacityUnit() != null && entity.getCapacityUnit().length() > 50) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Capacity Unit cannot exceed %d characters", 50));
		}
		if (entity.getTemperatureControl() != null && entity.getTemperatureControl().length() > 255) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Temperature Control cannot exceed %d characters", 255));
		}
		if (entity.getClimateControl() != null && entity.getClimateControl().length() > 255) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Climate Control cannot exceed %d characters", 255));
		}
		// 3. Unique Checks
		final Optional<CStorage> existingName = ((IStorageRepository) repository).findByNameAndProject(entity.getName(), entity.getProject());
		if (existingName.isPresent() && !existingName.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_PROJECT);
		}
		// 4. Numeric Checks
		if (entity.getCapacity() != null && entity.getCapacity().compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("Capacity must be positive");
		}
		if (entity.getCurrentUtilization() != null && entity.getCurrentUtilization().compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("Current Utilization cannot be negative");
		}
	}
}
