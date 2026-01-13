package tech.derbent.bab.node.repository;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tech.derbent.api.entity.service.IAbstractRepository;
import tech.derbent.bab.device.domain.CBabDevice;
import tech.derbent.bab.node.domain.CBabNode;

/**
 * Repository interface for CBabNode entities.
 * Provides data access methods for device communication nodes.
 */
@Repository
@Profile("bab")
public interface IBabNodeRepository extends IAbstractRepository<CBabNode> {

	/**
	 * Find all nodes by device.
	 * 
	 * @param device the device
	 * @return list of nodes ordered by name
	 */
	@Query("SELECT e FROM #{#entityName} e WHERE e.device = :device ORDER BY e.name ASC")
	List<CBabNode> findByDevice(@Param("device") CBabDevice device);

	/**
	 * Find all nodes by device ID.
	 * 
	 * @param deviceId the device ID
	 * @return list of nodes ordered by name
	 */
	@Query("SELECT e FROM #{#entityName} e WHERE e.device.id = :deviceId ORDER BY e.name ASC")
	List<CBabNode> findByDeviceId(@Param("deviceId") Long deviceId);

	/**
	 * Find enabled nodes by device.
	 * 
	 * @param device the device
	 * @return list of enabled nodes ordered by name
	 */
	@Query("SELECT e FROM #{#entityName} e WHERE e.device = :device AND e.enabled = true ORDER BY e.name ASC")
	List<CBabNode> findEnabledByDevice(@Param("device") CBabDevice device);

	/**
	 * Find nodes by device and node type.
	 * 
	 * @param device the device
	 * @param nodeType the node type (CAN, Modbus, Ethernet, ROS)
	 * @return list of nodes ordered by name
	 */
	@Query("SELECT e FROM #{#entityName} e WHERE e.device = :device AND e.nodeType = :nodeType ORDER BY e.name ASC")
	List<CBabNode> findByDeviceAndType(@Param("device") CBabDevice device, @Param("nodeType") String nodeType);

	/**
	 * Count nodes by device.
	 * 
	 * @param device the device
	 * @return node count
	 */
	@Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.device = :device")
	Long countByDevice(@Param("device") CBabDevice device);
}
