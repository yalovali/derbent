package tech.derbent.bab.node.service;

import org.springframework.context.annotation.Profile;
import org.springframework.data.repository.NoRepositoryBean;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.bab.device.domain.CBabDevice;
import tech.derbent.bab.node.domain.CBabNode;

/**
 * Abstract repository interface for CBabNode hierarchy.
 * Following Derbent pattern: Abstract repository with @NoRepositoryBean - no queries.
 * Concrete repositories provide the actual HQL queries.
 */
@Profile("bab")
@NoRepositoryBean
public interface IBabNodeRepository<NodeType extends CBabNode<NodeType>> extends IEntityOfCompanyRepository<NodeType> {

	/**
	 * Find all nodes by device.
	 * Implemented by concrete repositories with entity-specific HQL.
	 */
	java.util.List<NodeType> findByDevice(CBabDevice device);

	/**
	 * Find all nodes by device ID.
	 * Implemented by concrete repositories with entity-specific HQL.
	 */
	java.util.List<NodeType> findByDeviceId(Long deviceId);

	/**
	 * Find enabled nodes by device.
	 * Implemented by concrete repositories with entity-specific HQL.
	 */
	java.util.List<NodeType> findEnabledByDevice(CBabDevice device);

	/**
	 * Find nodes by device and node type.
	 * Implemented by concrete repositories with entity-specific HQL.
	 */
	java.util.List<NodeType> findByDeviceAndType(CBabDevice device, String nodeType);

	/**
	 * Count nodes by device.
	 * Implemented by concrete repositories with entity-specific HQL.
	 */
	Long countByDevice(CBabDevice device);
}
