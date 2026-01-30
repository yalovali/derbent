package tech.derbent.plm.storage.storage.service;

import java.math.BigDecimal;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
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
import tech.derbent.plm.storage.storage.domain.CStorage;
import tech.derbent.plm.storage.storagetype.service.CStorageTypeService;

@Profile("derbent")
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

	/**
	 * Service-level method to copy CStorage-specific fields.
	 * Uses direct setter/getter calls for clarity.
	 * 
	 * @param source  the source entity to copy from
	 * @param target  the target entity to copy to
	 * @param options clone options controlling what fields to copy
	 */
	@Override
	public void copyEntityFieldsTo(final CStorage source, final CEntityDB<?> target, final CCloneOptions options) {
		super.copyEntityFieldsTo(source, target, options);
		
		if (!(target instanceof CStorage targetStorage)) {
			return;
		}
		// Copy basic fields
		targetStorage.setAddress(source.getAddress());
		targetStorage.setBuilding(source.getBuilding());
		targetStorage.setFloor(source.getFloor());
		targetStorage.setZone(source.getZone());
		targetStorage.setBinCode(source.getBinCode());
		targetStorage.setCapacityUnit(source.getCapacityUnit());
		targetStorage.setTemperatureControl(source.getTemperatureControl());
		targetStorage.setClimateControl(source.getClimateControl());
		
		// Copy numeric fields
		targetStorage.setCapacity(source.getCapacity());
		targetStorage.setCurrentUtilization(source.getCurrentUtilization());
		
		// Copy boolean flags
		targetStorage.setActive(source.getActive());
		targetStorage.setSecureStorage(source.getSecureStorage());
		
		// Copy type
		targetStorage.setEntityType(source.getEntityType());
		
		// Copy relations conditionally
		if (options.includesRelations()) {
			targetStorage.setParentStorage(source.getParentStorage());
			targetStorage.setResponsibleUser(source.getResponsibleUser());
		}
		
		LOGGER.debug("Copied CStorage '{}' with options: {}", source.getName(), options);
	}

	@Override
	protected void validateEntity(final CStorage entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		// 2. String Length Checks - USE STATIC HELPER
		validateStringLength(entity.getAddress(), "Address", 500);
		validateStringLength(entity.getBuilding(), "Building", 255);
		validateStringLength(entity.getFloor(), "Floor", 255);
		validateStringLength(entity.getZone(), "Zone", 255);
		validateStringLength(entity.getBinCode(), "Bin Code", 255);
		validateStringLength(entity.getCapacityUnit(), "Capacity Unit", 50);
		validateStringLength(entity.getTemperatureControl(), "Temperature Control", 255);
		validateStringLength(entity.getClimateControl(), "Climate Control", 255);
		// 3. Unique Name Check - USE STATIC HELPER
		validateUniqueNameInProject((IStorageRepository) repository, entity, entity.getName().trim(), entity.getProject());
		// 4. Numeric Checks - USE STATIC HELPER
		validateNumericField(entity.getCapacity(), "Capacity", new BigDecimal("999999999.99"));
		validateNumericField(entity.getCurrentUtilization(), "Current Utilization", new BigDecimal("999999999.99"));
	}
}
