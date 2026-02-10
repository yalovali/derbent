package tech.derbent.bab.policybase.node.ip;

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
import tech.derbent.bab.policybase.node.modbus.CBabTCPModbusNode;
import tech.derbent.bab.policybase.node.modbus.ITCPModbusNodeRepository;
import tech.derbent.bab.policybase.node.service.CBabNodeService;
import tech.derbent.base.session.service.ISessionService;

/**
 * CBabTCPModbusNodeService - Service for Modbus TCP virtual network nodes.
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Entity service extending common node base service.
 * 
 * Provides Modbus TCP-specific business logic:
 * - Server port validation
 * - Unit ID validation
 * - TCP connection parameters validation
 */
@Service
@Profile("bab")
@PreAuthorize("isAuthenticated()")
public class CBabTCPModbusNodeService extends CBabNodeService<CBabTCPModbusNode> 
		implements IEntityRegistrable, IEntityWithView {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabTCPModbusNodeService.class);
	
	public CBabTCPModbusNodeService(
			final ITCPModbusNodeRepository repository,
			final Clock clock,
			final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}
	
	@Override
	public Class<CBabTCPModbusNode> getEntityClass() {
		return CBabTCPModbusNode.class;
	}
	
	@Override
	protected void validateEntity(final CBabTCPModbusNode entity) {
		super.validateEntity(entity);  // ✅ Common node validation (name, interface, uniqueness)
		
		LOGGER.debug("Validating TCP Modbus specific fields: {}", entity.getName());
		
		// TCP Modbus-specific validation
		if (entity.getServerPort() == null) {
			throw new IllegalArgumentException("Server Port is required");
		}
		validateNumericField(entity.getServerPort(), "Server Port", 65535);
		if (entity.getServerPort() < 1) {
			throw new IllegalArgumentException("Server Port must be between 1 and 65535");
		}
		
		// Unit ID validation
		if (entity.getUnitId() == null) {
			throw new IllegalArgumentException("Unit ID is required");
		}
		validateNumericField(entity.getUnitId(), "Unit ID", 255);
		
		// Server address validation
		Check.notBlank(entity.getServerAddress(), "Server Address is required");
		
		// Timeout validations
		if (entity.getConnectionTimeoutMs() != null) {
			validateNumericField(entity.getConnectionTimeoutMs(), "Connection Timeout", 60000);
			if (entity.getConnectionTimeoutMs() < 100) {
				throw new IllegalArgumentException("Connection Timeout must be at least 100ms");
			}
		}
		
		if (entity.getResponseTimeoutMs() != null) {
			validateNumericField(entity.getResponseTimeoutMs(), "Response Timeout", 60000);
			if (entity.getResponseTimeoutMs() < 100) {
				throw new IllegalArgumentException("Response Timeout must be at least 100ms");
			}
		}
		
		// Max connections validation
		if (entity.getMaxConnections() != null) {
			validateNumericField(entity.getMaxConnections(), "Max Connections", 100);
			if (entity.getMaxConnections() < 1) {
				throw new IllegalArgumentException("Max Connections must be at least 1");
			}
		}
		
		// Unique port+unitId per interface per project check
		final ITCPModbusNodeRepository repo = (ITCPModbusNodeRepository) repository;
		final var existingPort = repo.findByPortAndUnitIdAndInterfaceAndProject(
			entity.getServerPort(), 
			entity.getUnitId(),
			entity.getPhysicalInterface(), 
			entity.getProject());
		if (existingPort.isPresent() && !existingPort.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(
				"Server port %d with unit ID %d is already used by another TCP Modbus node on interface %s in this project"
					.formatted(entity.getServerPort(), entity.getUnitId(), entity.getPhysicalInterface()));
		}
		
		LOGGER.debug("TCP Modbus node validation passed: {}", entity.getName());
	}
	
	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		// TCP Modbus-specific initialization if needed
	}
	
	// IEntityRegistrable implementation
	
	@Override
	public Class<?> getServiceClass() {
		return CBabTCPModbusNodeService.class;
	}
	
	@Override
	public Class<?> getPageServiceClass() {
		return CPageServiceTCPModbusNode.class;
	}
	
	@Override
	public Class<?> getInitializerServiceClass() {
		return CBabTCPModbusNodeInitializerService.class;
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
	public void copyEntityFieldsTo(final CBabTCPModbusNode source, final CEntityDB<?> target,
			final CCloneOptions options) {
		// STEP 1: ALWAYS call parent first
		super.copyEntityFieldsTo(source, target, options);
		
		// STEP 2: Type-check target
		if (!(target instanceof CBabTCPModbusNode)) {
			return;
		}
		final CBabTCPModbusNode targetNode = (CBabTCPModbusNode) target;
		
		// STEP 3: Copy TCP Modbus-specific fields using DIRECT setter/getter
		targetNode.setServerPort(source.getServerPort());
		targetNode.setUnitId(source.getUnitId());
		targetNode.setServerAddress(source.getServerAddress());
		targetNode.setConnectionTimeoutMs(source.getConnectionTimeoutMs());
		targetNode.setResponseTimeoutMs(source.getResponseTimeoutMs());
		targetNode.setMaxConnections(source.getMaxConnections());
		targetNode.setKeepAlive(source.getKeepAlive());
		
		// STEP 4: Log completion
		LOGGER.debug("Copied {} '{}' with options: {}", getClass().getSimpleName(), source.getName(), options);
	}
}
