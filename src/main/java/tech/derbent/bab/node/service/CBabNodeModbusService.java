package tech.derbent.bab.node.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.bab.node.domain.CBabNodeModbus;
import tech.derbent.base.session.service.ISessionService;

/** Service class for CBabNodeModbus entity. Provides business logic for Modbus communication node management. Following Derbent pattern: Concrete
 * service with @Service and interfaces. */
@Service
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public class CBabNodeModbusService extends CBabNodeService<CBabNodeModbus> implements IEntityRegistrable, IEntityWithView {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabNodeModbusService.class);

	public CBabNodeModbusService(final IBabNodeModbusRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final CBabNodeModbus entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		// Add Modbus-specific deletion checks here if needed
		return null;
	}

	@Override
	public Class<CBabNodeModbus> getEntityClass() { return CBabNodeModbus.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CBabNodeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceBabNode.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
	}

	@Override
	protected void validateEntity(final CBabNodeModbus entity) {
		super.validateEntity(entity);
		// Modbus-specific validation
		if (entity.getSlaveId() != null && (entity.getSlaveId() < 1 || entity.getSlaveId() > 247)) {
			throw new CValidationException("Modbus Slave ID must be between 1 and 247");
		}
		if (entity.getBaudRate() != null && entity.getBaudRate() <= 0) {
			throw new CValidationException("Baud rate must be positive");
		}
		if ("TCP".equals(entity.getProtocolType()) && entity.getHostAddress() == null) {
			throw new CValidationException("Host address is required for Modbus TCP");
		}
	}
}
