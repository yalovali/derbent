package tech.derbent.bab.node.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.bab.device.domain.CBabDevice;
import tech.derbent.bab.node.domain.CBabNode;
import tech.derbent.base.session.service.ISessionService;

/** Abstract service class for CBabNode entity hierarchy. Provides common business logic for all device communication node types. Following Derbent
 * pattern: Abstract service for abstract entity - NO @Service annotation. */
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public abstract class CBabNodeService<NodeType extends CBabNode<NodeType>> extends CEntityOfCompanyService<NodeType> {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabNodeService.class);

	protected CBabNodeService(final IBabNodeRepository<NodeType> repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Count nodes by device */
	public Long countByDevice(CBabDevice device) {
		return ((IBabNodeRepository<NodeType>) repository).countByDevice(device);
	}

	/** Find nodes by device */
	public java.util.List<NodeType> findByDevice(CBabDevice device) {
		return ((IBabNodeRepository<NodeType>) repository).findByDevice(device);
	}

	/** Find enabled nodes by device */
	public java.util.List<NodeType> findEnabledByDevice(CBabDevice device) {
		return ((IBabNodeRepository<NodeType>) repository).findEnabledByDevice(device);
	}

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
	}

	@Override
	protected void validateEntity(final NodeType entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notNull(entity.getDevice(), "Device is required");
		Check.notBlank(entity.getNodeType(), "Node Type is required");
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		// 2. Length Checks - USE STATIC HELPER
		validateStringLength(entity.getNodeStatus(), "Status", 50);
		validateStringLength(entity.getNodeType(), "Node type", 50);
		// 3. Business Logic Validation - USE NUMERIC HELPER
		validateNumericRange(entity.getPortNumber(), "Port number", 0, 65535);
	}
}
