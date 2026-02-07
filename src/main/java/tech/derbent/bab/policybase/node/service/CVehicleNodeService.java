package tech.derbent.bab.policybase.node.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.bab.policybase.node.domain.CVehicleNode;

/**
 * CVehicleNodeService - Service for Vehicle virtual network nodes.
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Entity service extending common node base service.
 * 
 * Provides Vehicle-specific business logic:
 * - Vehicle ID uniqueness validation
 * - CAN address validation
 * - Baud rate validation
 * - CAN protocol validation
 */
@Service
@Profile("bab")
@PreAuthorize("isAuthenticated()")
public class CVehicleNodeService extends CBabNodeService<CVehicleNode> 
		implements IEntityRegistrable, IEntityWithView {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CVehicleNodeService.class);
	
	public CVehicleNodeService(
			final IVehicleNodeRepository repository,
			final Clock clock,
			final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}
	
	@Override
	public Class<CVehicleNode> getEntityClass() {
		return CVehicleNode.class;
	}
	
	@Override
	protected void validateEntity(final CVehicleNode entity) {
		super.validateEntity(entity);  // ✅ Common node validation (name, interface, uniqueness)
		
		LOGGER.debug("Validating Vehicle specific fields: {}", entity.getName());
		
		// Vehicle-specific validation
		Check.notBlank(entity.getVehicleId(), "Vehicle ID is required");
		validateStringLength(entity.getVehicleId(), "Vehicle ID", 50);
		
		// Unique vehicle ID per project
		final IVehicleNodeRepository repo = (IVehicleNodeRepository) repository;
		final var existingVehicle = repo.findByVehicleIdAndProject(entity.getVehicleId(), entity.getProject());
		if (existingVehicle.isPresent() && !existingVehicle.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(
				"Vehicle ID '%s' is already used by another vehicle node in this project"
					.formatted(entity.getVehicleId()));
		}
		
		// CAN address validation
		if (entity.getCanAddress() == null) {
			throw new IllegalArgumentException("CAN Address is required");
		}
		if (entity.getCanAddress() < 0 || entity.getCanAddress() > 0x7FF) {
			throw new IllegalArgumentException("CAN Address must be between 0x000 and 0x7FF");
		}
		
		// Baud rate validation
		if (entity.getBaudRate() == null) {
			throw new IllegalArgumentException("Baud Rate is required");
		}
		final int[] validBaudRates = {125000, 250000, 500000, 1000000};
		boolean validBaudRate = false;
		for (int rate : validBaudRates) {
			if (entity.getBaudRate() == rate) {
				validBaudRate = true;
				break;
			}
		}
		if (!validBaudRate) {
			throw new IllegalArgumentException("Baud Rate must be one of: 125000, 250000, 500000, 1000000 bps");
		}
		
		LOGGER.debug("Vehicle node validation passed: {}", entity.getName());
	}
	
	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		// Vehicle-specific initialization if needed
	}
	
	// IEntityRegistrable implementation
	
	@Override
	public Class<?> getServiceClass() {
		return CVehicleNodeService.class;
	}
	
	@Override
	public Class<?> getPageServiceClass() {
		return CPageServiceVehicleNode.class;
	}
	
	@Override
	public Class<?> getInitializerServiceClass() {
		return CVehicleNodeInitializerService.class;
	}
}
