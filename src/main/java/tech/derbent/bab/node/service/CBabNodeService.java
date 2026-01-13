package tech.derbent.bab.node.service;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entity.service.IAbstractRepository;
import tech.derbent.bab.device.domain.CBabDevice;
import tech.derbent.bab.node.domain.CBabNode;
import tech.derbent.bab.node.repository.IBabNodeRepository;
import tech.derbent.base.session.service.ISessionService;

/** Service class for CBabNode entity. Provides business logic for device communication node management. */
@Service
@Profile ("bab")
public class CBabNodeService extends CAbstractService<CBabNode> {

	private final IBabNodeRepository repository;
	private final ISessionService sessionService;

	public CBabNodeService(final IBabNodeRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
		this.repository = repository;
		this.sessionService = sessionService;
	}

	/** Count nodes by device.
	 * @param device the device
	 * @return node count */
	@Transactional (readOnly = true)
	public Long countByDevice(final CBabDevice device) {
		Objects.requireNonNull(device, "Device cannot be null");
		return repository.countByDevice(device);
	}

	/** Find all nodes by device.
	 * @param device the device
	 * @return list of nodes ordered by name */
	@Transactional (readOnly = true)
	public List<CBabNode> findByDevice(final CBabDevice device) {
		Objects.requireNonNull(device, "Device cannot be null");
		return repository.findByDevice(device);
	}

	/** Find nodes by device and type.
	 * @param device   the device
	 * @param nodeType the node type (CAN, Modbus, Ethernet, ROS)
	 * @return list of nodes ordered by name */
	@Transactional (readOnly = true)
	public List<CBabNode> findByDeviceAndType(final CBabDevice device, final String nodeType) {
		Objects.requireNonNull(device, "Device cannot be null");
		Objects.requireNonNull(nodeType, "Node type cannot be null");
		return repository.findByDeviceAndType(device, nodeType);
	}

	/** Find enabled nodes by device.
	 * @param device the device
	 * @return list of enabled nodes ordered by name */
	@Transactional (readOnly = true)
	public List<CBabNode> findEnabledByDevice(final CBabDevice device) {
		Objects.requireNonNull(device, "Device cannot be null");
		return repository.findEnabledByDevice(device);
	}

	@Override
	public Class<CBabNode> getEntityClass() {
		return CBabNode.class;
	}

	@Override
	public IAbstractRepository<CBabNode> getRepository() {
		return repository;
	}

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
