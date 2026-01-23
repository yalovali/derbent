package tech.derbent.bab.node.service;

import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.bab.device.domain.CBabDevice;
import tech.derbent.bab.node.domain.CBabNodeCAN;

/**
 * Repository interface for CBabNodeCAN entities.
 * Following Derbent pattern: Concrete repository for concrete entity with HQL queries.
 */
@Profile("bab")
public interface IBabNodeCANRepository extends IBabNodeRepository<CBabNodeCAN> {
    
    @Override
    @Query("SELECT e FROM CBabNodeCAN e WHERE e.device = :device ORDER BY e.name ASC")
    List<CBabNodeCAN> findByDevice(@Param("device") CBabDevice device);

    @Override
    @Query("SELECT e FROM CBabNodeCAN e WHERE e.device.id = :deviceId ORDER BY e.name ASC")
    List<CBabNodeCAN> findByDeviceId(@Param("deviceId") Long deviceId);

    @Override
    @Query("SELECT e FROM CBabNodeCAN e WHERE e.device = :device AND e.enabled = true ORDER BY e.name ASC")
    List<CBabNodeCAN> findEnabledByDevice(@Param("device") CBabDevice device);

    @Override
    @Query("SELECT e FROM CBabNodeCAN e WHERE e.device = :device AND e.nodeType = :nodeType ORDER BY e.name ASC")
    List<CBabNodeCAN> findByDeviceAndType(@Param("device") CBabDevice device, @Param("nodeType") String nodeType);

    @Override
    @Query("SELECT COUNT(e) FROM CBabNodeCAN e WHERE e.device = :device")
    Long countByDevice(@Param("device") CBabDevice device);
    
}