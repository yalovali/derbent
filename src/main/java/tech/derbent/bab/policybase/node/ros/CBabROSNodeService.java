package tech.derbent.bab.policybase.node.ros;

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
import tech.derbent.bab.policybase.node.service.CBabNodeService;
import tech.derbent.base.session.service.ISessionService;

/**
 * CBabROSNodeService - Service for ROS virtual network nodes.
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Entity service extending common node base service.
 * 
 * Provides ROS-specific business logic:
 * - ROS master URI validation
 * - ROS version validation (ROS1/ROS2)
 * - Topic and service validation
 */
@Service
@Profile("bab")
@PreAuthorize("isAuthenticated()")
public class CBabROSNodeService extends CBabNodeService<CBabROSNode> 
		implements IEntityRegistrable, IEntityWithView {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabROSNodeService.class);
	
	public CBabROSNodeService(
			final IROSNodeRepository repository,
			final Clock clock,
			final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}
	
	@Override
	public Class<CBabROSNode> getEntityClass() {
		return CBabROSNode.class;
	}
	
	@Override
	protected void validateEntity(final CBabROSNode entity) {
		super.validateEntity(entity);  // ✅ Common node validation (name, interface, uniqueness)
		
		LOGGER.debug("Validating ROS specific fields: {}", entity.getName());
		
		// ROS-specific validation
		Check.notBlank(entity.getRosMasterUri(), "ROS Master URI is required");
		if (!entity.getRosMasterUri().startsWith("http://") && !entity.getRosMasterUri().startsWith("https://")) {
			throw new IllegalArgumentException("ROS Master URI must start with http:// or https://");
		}
		
		// Port validation
		if (entity.getRosMasterPort() == null) {
			throw new IllegalArgumentException("ROS Master Port is required");
		}
		validateNumericField(entity.getRosMasterPort(), "ROS Master Port", 65535);
		if (entity.getRosMasterPort() < 1) {
			throw new IllegalArgumentException("ROS Master Port must be between 1 and 65535");
		}
		
		// ROS version validation (Yoda conditions for null safety)
		Check.notBlank(entity.getRosVersion(), "ROS Version is required");
		if (!"ROS1".equals(entity.getRosVersion()) && !"ROS2".equals(entity.getRosVersion())) {
			throw new IllegalArgumentException("ROS Version must be ROS1 or ROS2");
		}
		
		// Namespace validation
		Check.notBlank(entity.getNodeNamespace(), "Node Namespace is required");
		if (!entity.getNodeNamespace().startsWith("/")) {
			throw new IllegalArgumentException("Node Namespace must start with /");
		}
		
		// Topics validation
		Check.notBlank(entity.getTopics(), "Topics are required");
		
		// Services validation
		Check.notBlank(entity.getServices(), "Services are required");
		
		// Queue size validation
		if (entity.getQueueSize() != null) {
			validateNumericField(entity.getQueueSize(), "Queue Size", 1000);
			if (entity.getQueueSize() < 1) {
				throw new IllegalArgumentException("Queue Size must be at least 1");
			}
		}
		
		// Unique port per interface per project check
		final IROSNodeRepository repo = (IROSNodeRepository) repository;
		final var existingPort = repo.findByMasterPortAndInterfaceAndProject(
			entity.getRosMasterPort(), 
			entity.getPhysicalInterface(), 
			entity.getProject());
		if (existingPort.isPresent() && !existingPort.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(
				"ROS Master port %d is already used by another ROS node on interface %s in this project"
					.formatted(entity.getRosMasterPort(), entity.getPhysicalInterface()));
		}
		
		LOGGER.debug("ROS node validation passed: {}", entity.getName());
	}
	
	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		// ROS-specific initialization if needed
	}
	
	// IEntityRegistrable implementation
	
	@Override
	public Class<?> getServiceClass() {
		return CBabROSNodeService.class;
	}
	
	@Override
	public Class<?> getPageServiceClass() {
		return CPageServiceROSNode.class;
	}
	
	@Override
	public Class<?> getInitializerServiceClass() {
		return CBabROSNodeInitializerService.class;
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
	public void copyEntityFieldsTo(final CBabROSNode source, final CEntityDB<?> target,
			final CCloneOptions options) {
		// STEP 1: ALWAYS call parent first
		super.copyEntityFieldsTo(source, target, options);
		
		// STEP 2: Type-check target
		if (!(target instanceof CBabROSNode)) {
			return;
		}
		final CBabROSNode targetNode = (CBabROSNode) target;
		
		// STEP 3: Copy ROS-specific fields using DIRECT setter/getter
		targetNode.setRosMasterUri(source.getRosMasterUri());
		targetNode.setRosMasterPort(source.getRosMasterPort());
		targetNode.setRosVersion(source.getRosVersion());
		targetNode.setNodeNamespace(source.getNodeNamespace());
		targetNode.setTopics(source.getTopics());
		targetNode.setServices(source.getServices());
		targetNode.setQueueSize(source.getQueueSize());
		
		// STEP 4: Log completion
		LOGGER.debug("Copied {} '{}' with options: {}", getClass().getSimpleName(), source.getName(), options);
	}
}
