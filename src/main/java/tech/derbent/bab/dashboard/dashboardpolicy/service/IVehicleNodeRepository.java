package tech.derbent.bab.dashboard.dashboardpolicy.service;

import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.policybase.node.domain.CVehicleNode;
import tech.derbent.bab.policybase.node.service.INodeEntityRepository;

/**
 * IVehicleNodeRepository - Repository interface for vehicle virtual network nodes.
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Concrete repository with HQL queries.
 * 
 * Handles vehicle node persistence with complete eager loading for UI performance.
 * Provides specialized queries for CAN bus vehicle configuration and monitoring.
 */
@Profile("bab")
public interface IVehicleNodeRepository extends INodeEntityRepository<CVehicleNode> {
    
    @Override
    @Query("""
	SELECT DISTINCT e FROM #{#entityName} e
	LEFT JOIN FETCH e.project
	LEFT JOIN FETCH e.assignedTo
	LEFT JOIN FETCH e.createdBy
	LEFT JOIN FETCH e.attachments
	LEFT JOIN FETCH e.comments
	LEFT JOIN FETCH e.links
	WHERE e.id = :id
	""")
    Optional<CVehicleNode> findById(@Param("id") Long id);
    
    @Override
    @Query("""
	SELECT DISTINCT e FROM #{#entityName} e
	LEFT JOIN FETCH e.project
	LEFT JOIN FETCH e.assignedTo
	LEFT JOIN FETCH e.createdBy
	LEFT JOIN FETCH e.attachments
	LEFT JOIN FETCH e.comments
	LEFT JOIN FETCH e.links
	WHERE e.project = :project
	ORDER BY e.id DESC
	""")
    List<CVehicleNode> listByProjectForPageView(@Param("project") CProject<?> project);
    
    @Override
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.physicalInterface = :physicalInterface AND e.project = :project
	ORDER BY e.name ASC
	""")
    List<CVehicleNode> findByPhysicalInterface(@Param("physicalInterface") String physicalInterface);
    
    @Override
    @Query("""
	SELECT DISTINCT e FROM #{#entityName} e
	LEFT JOIN FETCH e.project
	LEFT JOIN FETCH e.assignedTo
	LEFT JOIN FETCH e.createdBy
	WHERE e.isActive = true AND e.project = :project
	ORDER BY e.name ASC
	""")
    List<CVehicleNode> findActiveByProject(@Param("project") CProject<?> project);
    
    @Override
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.connectionStatus = :connectionStatus AND e.project = :project
	ORDER BY e.name ASC
	""")
    List<CVehicleNode> findByConnectionStatus(@Param("connectionStatus") String connectionStatus);
    
    @Override
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.nodeType = :nodeType AND e.project = :project
	ORDER BY e.name ASC
	""")
    List<CVehicleNode> findByNodeTypeAndProject(@Param("nodeType") String nodeType, @Param("project") CProject<?> project);
    
    @Override
    @Query("SELECT COUNT(e) > 0 FROM #{#entityName} e WHERE e.physicalInterface = :physicalInterface AND e.project = :project")
    boolean existsByPhysicalInterfaceAndProject(@Param("physicalInterface") String physicalInterface, @Param("project") CProject<?> project);
    
    @Override
    @Query("SELECT e FROM #{#entityName} e WHERE e.physicalInterface = :physicalInterface AND e.project = :project")
    Optional<CVehicleNode> findByPhysicalInterfaceAndProject(@Param("physicalInterface") String physicalInterface, @Param("project") CProject<?> project);
    
    @Override
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.isActive = true AND e.project = :project")
    long countActiveByProject(@Param("project") CProject<?> project);
    
    @Override
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.connectionStatus = :connectionStatus AND e.project = :project")
    long countByConnectionStatusAndProject(@Param("connectionStatus") String connectionStatus, @Param("project") CProject<?> project);
    
    // Vehicle-specific queries
    
    /**
     * Find vehicle by vehicle ID and project.
     * Ensures unique vehicle identification.
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.vehicleId = :vehicleId AND e.project = :project")
    Optional<CVehicleNode> findByVehicleIdAndProject(@Param("vehicleId") String vehicleId, @Param("project") CProject<?> project);
    
    /**
     * Check if vehicle ID is already used.
     * Critical for vehicle ID uniqueness validation.
     */
    @Query("SELECT COUNT(e) > 0 FROM #{#entityName} e WHERE e.vehicleId = :vehicleId AND e.project = :project")
    boolean existsByVehicleIdAndProject(@Param("vehicleId") String vehicleId, @Param("project") CProject<?> project);
    
    /**
     * Find vehicles by CAN address and interface.
     * Critical for CAN address conflict detection.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.canAddress = :canAddress 
	AND e.physicalInterface = :physicalInterface 
	AND e.project = :project
	ORDER BY e.name ASC
	""")
    List<CVehicleNode> findByCanAddressAndPhysicalInterface(
        @Param("canAddress") Integer canAddress, 
        @Param("physicalInterface") String physicalInterface, 
        @Param("project") CProject<?> project);
    
    /**
     * Check if CAN address is already used on same interface.
     * Critical for CAN bus conflict prevention.
     */
    @Query("""
	SELECT COUNT(e) > 0 FROM #{#entityName} e
	WHERE e.canAddress = :canAddress 
	AND e.physicalInterface = :physicalInterface 
	AND e.project = :project
	AND (:excludeId IS NULL OR e.id != :excludeId)
	""")
    boolean existsByCanAddressAndPhysicalInterfaceExcluding(
        @Param("canAddress") Integer canAddress,
        @Param("physicalInterface") String physicalInterface,
        @Param("project") CProject<?> project,
        @Param("excludeId") Long excludeId);
    
    /**
     * Find vehicles by baud rate.
     * Useful for CAN bus configuration grouping.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.baudRate = :baudRate AND e.project = :project
	ORDER BY e.name ASC
	""")
    List<CVehicleNode> findByBaudRateAndProject(@Param("baudRate") Integer baudRate, @Param("project") CProject<?> project);
    
    /**
     * Find vehicles by type.
     * Useful for vehicle type filtering (CAR, TRUCK, MOTORCYCLE, BUS).
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.vehicleType = :vehicleType AND e.project = :project
	ORDER BY e.name ASC
	""")
    List<CVehicleNode> findByVehicleTypeAndProject(@Param("vehicleType") String vehicleType, @Param("project") CProject<?> project);
    
    /**
     * Find vehicles by manufacturer.
     * Useful for fleet management grouping.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.manufacturer = :manufacturer AND e.project = :project
	ORDER BY e.name ASC
	""")
    List<CVehicleNode> findByManufacturerAndProject(@Param("manufacturer") String manufacturer, @Param("project") CProject<?> project);
    
    /**
     * Find vehicles by CAN protocol.
     * Useful for protocol compatibility checking (CAN 2.0A, CAN 2.0B, CAN-FD).
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.canProtocol = :canProtocol AND e.project = :project
	ORDER BY e.name ASC
	""")
    List<CVehicleNode> findByCanProtocolAndProject(@Param("canProtocol") String canProtocol, @Param("project") CProject<?> project);
    
    /**
     * Find CAN-FD capable vehicles.
     * Useful for advanced CAN protocol features.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.canProtocol = 'CAN-FD' AND e.project = :project
	ORDER BY e.name ASC
	""")
    List<CVehicleNode> findCanFdVehicles(@Param("project") CProject<?> project);
    
    /**
     * Find commercial vehicles (trucks and buses).
     * Useful for fleet management categorization.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.vehicleType IN ('TRUCK', 'BUS') AND e.project = :project
	ORDER BY e.name ASC
	""")
    List<CVehicleNode> findCommercialVehicles(@Param("project") CProject<?> project);
    
    /**
     * Find vehicles by model year range.
     * Useful for fleet age analysis.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.modelYear BETWEEN :minYear AND :maxYear 
	AND e.project = :project
	ORDER BY e.modelYear DESC, e.name ASC
	""")
    List<CVehicleNode> findByModelYearRange(@Param("minYear") Integer minYear, @Param("maxYear") Integer maxYear, @Param("project") CProject<?> project);
    
    /**
     * Get distinct manufacturers in project.
     * Useful for manufacturer filter dropdown.
     */
    @Query("SELECT DISTINCT e.manufacturer FROM #{#entityName} e WHERE e.manufacturer IS NOT NULL AND e.project = :project ORDER BY e.manufacturer")
    List<String> findDistinctManufacturersByProject(@Param("project") CProject<?> project);
    
    /**
     * Get distinct vehicle types in project.
     * Useful for vehicle type filter dropdown.
     */
    @Query("SELECT DISTINCT e.vehicleType FROM #{#entityName} e WHERE e.vehicleType IS NOT NULL AND e.project = :project ORDER BY e.vehicleType")
    List<String> findDistinctVehicleTypesByProject(@Param("project") CProject<?> project);
}