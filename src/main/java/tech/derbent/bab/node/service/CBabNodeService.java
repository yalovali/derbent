package tech.derbent.bab.node.service;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entity.service.IAbstractRepository;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.bab.device.domain.CBabDevice;
import tech.derbent.bab.node.domain.CBabNode;
import tech.derbent.base.session.service.ISessionService;

import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;

/** Service class for CBabNode entity. Provides business logic for device communication node management. Following Derbent pattern: Service with
 * IEntityRegistrable and IEntityWithView. */
@Service
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public class CBabNodeService extends CAbstractService<CBabNode> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CBabNodeService.class);

	public CBabNodeService(final IBabNodeRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	protected void validateEntity(final CBabNode entity) {
		super.validateEntity(entity);
		
		// 1. Required Fields
		Check.notNull(entity.getDevice(), "Device is required");
		Check.notBlank(entity.getNodeType(), "Node Type is required");
		
		// 2. Length Checks
		if (entity.getName() != null && entity.getName().length() > 255) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength(ValidationMessages.NAME_MAX_LENGTH, 255));
		}
		if (entity.getNodeStatus() != null && entity.getNodeStatus().length() > 50) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Status cannot exceed %d characters", 50));
		}
		if (entity.getNodeType().length() > 50) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Node Type cannot exceed %d characters", 50));
		}
		
		// 3. Numeric Checks
		if (entity.getPortNumber() != null && entity.getPortNumber() < 0) {
			throw new IllegalArgumentException("Port Number cannot be negative");
		}
	}

	/** Count nodes by device.
	 * @param device the device
	 * @return node count */
	@Transactional (readOnly = true)
	public Long countByDevice(final CBabDevice device) {
		Objects.requireNonNull(device, "Device cannot be null");
		return ((IBabNodeRepository) repository).countByDevice(device);
	}

	/** Find all nodes by device.
	 * @param device the device
	 * @return list of nodes ordered by name */
	@Transactional (readOnly = true)
	public List<CBabNode> findByDevice(final CBabDevice device) {
		Objects.requireNonNull(device, "Device cannot be null");
		return ((IBabNodeRepository) repository).findByDevice(device);
	}

	/** Find nodes by device and type.
	 * @param device   the device
	 * @param nodeType the node type (CAN, Modbus, Ethernet, ROS)
	 * @return list of nodes ordered by name */
	@Transactional (readOnly = true)
	public List<CBabNode> findByDeviceAndType(final CBabDevice device, final String nodeType) {
		Objects.requireNonNull(device, "Device cannot be null");
		Objects.requireNonNull(nodeType, "Node type cannot be null");
		return ((IBabNodeRepository) repository).findByDeviceAndType(device, nodeType);
	}

	/** Find enabled nodes by device.
	 * @param device the device
	 * @return list of enabled nodes ordered by name */
	@Transactional (readOnly = true)
	public List<CBabNode> findEnabledByDevice(final CBabDevice device) {
		Objects.requireNonNull(device, "Device cannot be null");
		return ((IBabNodeRepository) repository).findEnabledByDevice(device);
	}

	@Override
	public Class<CBabNode> getEntityClass() { return CBabNode.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CBabNodeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceBabNode.class; }

	@Override
	public IAbstractRepository<CBabNode> getRepository() { return repository; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	/** Enable or disable a node.
	 * @param node    the node
	 * @param enabled true to enable, false to disable */
	@Transactional
	public void setNodeEnabled(final CBabNode node, final boolean enabled) {
		Objects.requireNonNull(node, "Node cannot be null");
		node.setEnabled(enabled);
		node.setNodeStatus(enabled ? "Active" : "Inactive");
		save(node);
	}
}
