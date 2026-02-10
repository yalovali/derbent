package tech.derbent.bab.policybase.node.modbus;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.bab.policybase.node.domain.CBabModbusNode;
import tech.derbent.bab.policybase.node.ip.CPageServiceModbusNode;
import tech.derbent.bab.policybase.node.service.CBabNodeService;

/**
 * CBabModbusNodeService - Service for Modbus RTU/ASCII virtual network nodes.
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Entity service extending common node base service.
 * 
 * Provides Modbus-specific business logic:
 * - Slave ID validation
 * - Serial port configuration validation
 * - Modbus mode validation (RTU/ASCII)
 */
@Service
@Profile("bab")
@PreAuthorize("isAuthenticated()")
public class CBabModbusNodeService extends CBabNodeService<CBabModbusNode> 
		implements IEntityRegistrable, IEntityWithView {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabModbusNodeService.class);
	
	public CBabModbusNodeService(
			final IModbusNodeRepository repository,
			final Clock clock,
			final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}
	
	@Override
	public Class<CBabModbusNode> getEntityClass() {
		return CBabModbusNode.class;
	}
	
	@Override
	protected void validateEntity(final CBabModbusNode entity) {
		super.validateEntity(entity);  // ✅ Common node validation (name, interface, uniqueness)
		
		LOGGER.debug("Validating Modbus specific fields: {}", entity.getName());
		
		// Modbus-specific validation
		if (entity.getSlaveId() == null) {
			throw new IllegalArgumentException("Slave ID is required");
		}
		validateNumericField(entity.getSlaveId(), "Slave ID", 247);
		if (entity.getSlaveId() < 1) {
			throw new IllegalArgumentException("Slave ID must be between 1 and 247");
		}
		
		// Baudrate validation
		if (entity.getBaudrate() == null) {
			throw new IllegalArgumentException("Baudrate is required");
		}
		if (entity.getBaudrate() < 300 || entity.getBaudrate() > 115200) {
			throw new IllegalArgumentException("Baudrate must be between 300 and 115200");
		}
		
		// Data bits validation
		if (entity.getDataBits() != null && entity.getDataBits() != 7 && entity.getDataBits() != 8) {
			throw new IllegalArgumentException("Data bits must be 7 or 8");
		}
		
		// Stop bits validation
		if (entity.getStopBits() != null && entity.getStopBits() != 1 && entity.getStopBits() != 2) {
			throw new IllegalArgumentException("Stop bits must be 1 or 2");
		}
		
		// Parity validation (Yoda conditions for null safety)
		Check.notBlank(entity.getParity(), "Parity is required");
		if (!"NONE".equals(entity.getParity()) && !"EVEN".equals(entity.getParity()) && !"ODD".equals(entity.getParity())) {
			throw new IllegalArgumentException("Parity must be NONE, EVEN, or ODD");
		}
		
		// Modbus mode validation (Yoda conditions for null safety)
		Check.notBlank(entity.getModbusMode(), "Modbus Mode is required");
		if (!"RTU".equals(entity.getModbusMode()) && !"ASCII".equals(entity.getModbusMode())) {
			throw new IllegalArgumentException("Modbus Mode must be RTU or ASCII");
		}
		
		// Unique slave ID per interface per project check
		final IModbusNodeRepository repo = (IModbusNodeRepository) repository;
		final var existingSlaveId = repo.findBySlaveIdAndInterfaceAndProject(
			entity.getSlaveId(), 
			entity.getPhysicalInterface(), 
			entity.getProject());
		if (existingSlaveId.isPresent() && !existingSlaveId.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(
				"Slave ID %d is already used by another Modbus node on interface %s in this project"
					.formatted(entity.getSlaveId(), entity.getPhysicalInterface()));
		}
		
		LOGGER.debug("Modbus node validation passed: {}", entity.getName());
	}
	
	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		// Modbus-specific initialization if needed
	}
	
	// IEntityRegistrable implementation
	
	@Override
	public Class<?> getServiceClass() {
		return CBabModbusNodeService.class;
	}
	
	@Override
	public Class<?> getPageServiceClass() {
		return CPageServiceModbusNode.class;
	}
	
	@Override
	public Class<?> getInitializerServiceClass() {
		return CBabModbusNodeInitializerService.class;
	}
	
	/**
	 * Copy entity-specific fields from source to target.
	 * MANDATORY: All entity services must implement this method.
	 * 
	 * @param source  the source entity to copy from
	 * @param target  the target entity to copy to
	 * @param options clone options controlling what fields to copy
	 */
	@Override
	public void copyEntityFieldsTo(final CBabModbusNode source, final CEntityDB<?> target,
			final CCloneOptions options) {
		// STEP 1: ALWAYS call parent first
		super.copyEntityFieldsTo(source, target, options);
		
		// STEP 2: Type-check target
		if (!(target instanceof CBabModbusNode)) {
			return;
		}
		final CBabModbusNode targetNode = (CBabModbusNode) target;
		
		// STEP 3: Copy Modbus-specific fields using DIRECT setter/getter
		targetNode.setSlaveId(source.getSlaveId());
		targetNode.setBaudrate(source.getBaudrate());
		targetNode.setDataBits(source.getDataBits());
		targetNode.setStopBits(source.getStopBits());
		targetNode.setParity(source.getParity());
		targetNode.setModbusMode(source.getModbusMode());
		targetNode.setTimeoutMs(source.getTimeoutMs());
		
		// STEP 4: Log completion
		LOGGER.debug("Copied {} '{}' with options: {}", getClass().getSimpleName(), source.getName(), options);
	}
}
