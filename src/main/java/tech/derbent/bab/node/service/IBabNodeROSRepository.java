package tech.derbent.bab.node.service;

import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.bab.device.domain.CBabDevice;
import tech.derbent.bab.node.domain.CBabNodeROS;

/**
 * Repository interface for CBabNodeROS entities.
 * Following Derbent pattern: Concrete repository for concrete entity with HQL queries.
 */
@Profile("bab")
public interface IBabNodeROSRepository extends IBabNodeRepository<CBabNodeROS> {
    
    @Override
    @Query("SELECT e FROM CBabNodeROS e WHERE e.device = :device ORDER BY e.name ASC")
    List<CBabNodeROS> findByDevice(@Param("device") CBabDevice device);

    @Override
    @Query("SELECT e FROM CBabNodeROS e WHERE e.device.id = :deviceId ORDER BY e.name ASC")
    List<CBabNodeROS> findByDeviceId(@Param("deviceId") Long deviceId);

    @Override
    @Query("SELECT e FROM CBabNodeROS e WHERE e.device = :device AND e.enabled = true ORDER BY e.name ASC")
    List<CBabNodeROS> findEnabledByDevice(@Param("device") CBabDevice device);

    @Override
    @Query("SELECT e FROM CBabNodeROS e WHERE e.device = :device AND e.nodeType = :nodeType ORDER BY e.name ASC")
    List<CBabNodeROS> findByDeviceAndType(@Param("device") CBabDevice device, @Param("nodeType") String nodeType);

    @Override
    @Query("SELECT COUNT(e) FROM CBabNodeROS e WHERE e.device = :device")
    Long countByDevice(@Param("device") CBabDevice device);
    
}