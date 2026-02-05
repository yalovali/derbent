package tech.derbent.bab.dashboard.dashboardpolicy.service;

import java.time.Clock;
import java.util.List;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.policybase.node.domain.CVehicleNode;
import tech.derbent.base.session.service.ISessionService;

/**
 * CVehicleNodeService - Service for Vehicle virtual network nodes.
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Concrete service with @Service annotation.
 * 
 * Provides business logic for vehicle node management:
 * - CAN bus configuration validation
 * - Vehicle ID uniqueness and format validation
 * - CAN address conflict detection
 * - Baud rate compatibility checking
 * - Vehicle type and manufacturer management
 * - Calimero CAN integration
 */
@Service
@Profile("bab")
@PreAuthorize("isAuthenticated()")
public class CVehicleNodeService extends CNodeEntityService<CVehicleNode> 
    implements IEntityRegistrable, IEntityWithView {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CVehicleNodeService.class);
    
    // Vehicle validation patterns
    private static final Pattern CAN_INTERFACE_PATTERN = Pattern.compile("^can[0-9]+$");
    private static final Pattern VEHICLE_ID_PATTERN = Pattern.compile("^[A-Z0-9_-]{3,50}$");
    
    // CAN address ranges
    private static final int MIN_CAN_ADDRESS = 0x000;
    private static final int MAX_CAN_ADDRESS = 0x7FF; // Standard CAN 11-bit
    private static final int MAX_CAN_EXTENDED_ADDRESS = 0x1FFFFFFF; // Extended CAN 29-bit
    
    // Standard CAN baud rates
    private static final int[] STANDARD_BAUD_RATES = {
        125000, 250000, 500000, 1000000  // 125K, 250K, 500K, 1M
    };
    
    public CVehicleNodeService(final IVehicleNodeRepository repository,
                              final Clock clock,
                              final ISessionService sessionService) {
        super(repository, clock, sessionService);
    }
    
    @Override
    public Class<CVehicleNode> getEntityClass() {
        return CVehicleNode.class;
    }
    
    @Override
    protected void validateNodeSpecificFields(final CVehicleNode entity) {
        // Vehicle specific validation
        
        // Vehicle ID validation
        if (entity.getVehicleId() == null || entity.getVehicleId().trim().isEmpty()) {
            throw new CValidationException("Vehicle ID is required");
        }
        
        validateStringLength(entity.getVehicleId(), "Vehicle ID", 50);
        
        if (!VEHICLE_ID_PATTERN.matcher(entity.getVehicleId()).matches()) {
            throw new CValidationException(
                "Vehicle ID must be 3-50 characters long and contain only uppercase letters, numbers, hyphens, and underscores");
        }
        
        // CAN address validation
        if (entity.getCanAddress() == null) {
            throw new CValidationException("CAN address is required");
        }
        
        validateNumericField(entity.getCanAddress(), "CAN Address", MAX_CAN_EXTENDED_ADDRESS);
        
        if (entity.getCanAddress() < MIN_CAN_ADDRESS) {
            throw new CValidationException(String.format(
                "CAN address must be greater than or equal to 0x%X", MIN_CAN_ADDRESS));
        }
        
        // Validate address range based on protocol
        final boolean isExtendedAddress = entity.getCanAddress() > MAX_CAN_ADDRESS;
        if (isExtendedAddress && !supportsExtendedAddressing(entity)) {
            throw new CValidationException(String.format(
                "CAN address 0x%X requires extended addressing (CAN-FD protocol)", entity.getCanAddress()));
        }
        
        // Baud rate validation
        if (entity.getBaudRate() == null) {
            throw new CValidationException("Baud rate is required");
        }
        
        validateNumericField(entity.getBaudRate(), "Baud Rate", 2000000);
        
        if (!isValidBaudRate(entity.getBaudRate())) {
            LOGGER.warn("Non-standard baud rate {} for vehicle '{}'. Standard rates are: 125K, 250K, 500K, 1M", 
                       entity.getBaudRate(), entity.getName());
        }
        
        // Vehicle type validation
        if (entity.getVehicleType() == null || entity.getVehicleType().trim().isEmpty()) {
            throw new CValidationException("Vehicle type is required");
        }
        
        validateStringLength(entity.getVehicleType(), "Vehicle Type", 30);
        validateVehicleType(entity.getVehicleType());
        
        // Model year validation
        if (entity.getModelYear() != null) {
            final int currentYear = java.time.Year.now().getValue();
            if (entity.getModelYear() < 1886 || entity.getModelYear() > (currentYear + 2)) {
                throw new CValidationException(String.format(
                    "Model year must be between 1886 and %d", currentYear + 2));
            }
        }
        
        // Manufacturer validation
        if (entity.getManufacturer() != null && !entity.getManufacturer().trim().isEmpty()) {
            validateStringLength(entity.getManufacturer(), "Manufacturer", 50);
        }
        
        // CAN protocol validation
        if (entity.getCanProtocol() == null || entity.getCanProtocol().trim().isEmpty()) {
            throw new CValidationException("CAN protocol is required");
        }
        
        validateStringLength(entity.getCanProtocol(), "CAN Protocol", 20);
        validateCanProtocol(entity.getCanProtocol());
        
        // Vehicle ID uniqueness validation
        validateVehicleIdUniqueness(entity);
        
        // CAN address conflict validation
        validateCanAddressConflict(entity);
    }
    
    @Override
    protected void validatePhysicalInterfaceFormat(final CVehicleNode entity, final String physicalInterface) {
        // Vehicles typically use CAN interfaces
        if (!CAN_INTERFACE_PATTERN.matcher(physicalInterface).matches()) {
            throw new CValidationException(String.format(
                "Physical interface '%s' is not a valid CAN interface. Expected format: can0, can1, etc.", 
                physicalInterface));
        }
        
        // Call parent for basic validation
        super.validatePhysicalInterfaceFormat(entity, physicalInterface);
    }
    
    /**
     * Validate that vehicle ID is unique within the project.
     */
    private void validateVehicleIdUniqueness(final CVehicleNode entity) {
        final IVehicleNodeRepository vehicleRepo = (IVehicleNodeRepository) repository;
        
        final boolean vehicleIdExists = vehicleRepo.existsByVehicleIdAndProject(
            entity.getVehicleId(), entity.getProject());
        
        if (vehicleIdExists) {
            // Check if it's the same entity (update scenario)
            final var existingVehicle = vehicleRepo.findByVehicleIdAndProject(
                entity.getVehicleId(), entity.getProject());
            
            if (existingVehicle.isPresent() && 
                (entity.getId() == null || !existingVehicle.get().getId().equals(entity.getId()))) {
                throw new CValidationException(String.format(
                    "Vehicle ID '%s' is already in use by vehicle '%s'",
                    entity.getVehicleId(), existingVehicle.get().getName()));
            }
        }
    }
    
    /**
     * Validate that CAN address is not already in use on the same interface.
     */
    private void validateCanAddressConflict(final CVehicleNode entity) {
        final IVehicleNodeRepository vehicleRepo = (IVehicleNodeRepository) repository;
        
        final boolean addressConflict = vehicleRepo.existsByCanAddressAndPhysicalInterfaceExcluding(
            entity.getCanAddress(),
            entity.getPhysicalInterface(),
            entity.getProject(),
            entity.getId());
        
        if (addressConflict) {
            throw new CValidationException(String.format(
                "CAN address 0x%X is already in use by another vehicle on interface '%s'",
                entity.getCanAddress(), entity.getPhysicalInterface()));
        }
    }
    
    /**
     * Validate vehicle type.
     */
    private void validateVehicleType(final String vehicleType) {
        final String[] validTypes = {"CAR", "TRUCK", "MOTORCYCLE", "BUS", "VAN", "OTHER"};
        final String upperType = vehicleType.toUpperCase();
        
        for (final String validType : validTypes) {
            if (validType.equals(upperType)) {
                return;
            }
        }
        
        LOGGER.warn("Unknown vehicle type '{}'. Valid types are: CAR, TRUCK, MOTORCYCLE, BUS, VAN, OTHER", vehicleType);
    }
    
    /**
     * Validate CAN protocol.
     */
    private void validateCanProtocol(final String canProtocol) {
        final String[] validProtocols = {"CAN 2.0A", "CAN 2.0B", "CAN-FD"};
        
        for (final String validProtocol : validProtocols) {
            if (validProtocol.equalsIgnoreCase(canProtocol)) {
                return;
            }
        }
        
        throw new CValidationException(String.format(
            "Invalid CAN protocol '%s'. Valid protocols are: CAN 2.0A, CAN 2.0B, CAN-FD", canProtocol));
    }
    
    /**
     * Check if vehicle supports extended CAN addressing.
     */
    private boolean supportsExtendedAddressing(final CVehicleNode entity) {
        return "CAN-FD".equalsIgnoreCase(entity.getCanProtocol());
    }
    
    /**
     * Check if baud rate is a standard CAN baud rate.
     */
    private boolean isValidBaudRate(final Integer baudRate) {
        for (final int standardRate : STANDARD_BAUD_RATES) {
            if (standardRate == baudRate) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    protected void validateJsonConfiguration(final CVehicleNode entity, final String configJson) {
        // Basic JSON validation for vehicle configuration
        if (!configJson.trim().startsWith("{") || !configJson.trim().endsWith("}")) {
            throw new CValidationException("Configuration must be a valid JSON object");
        }
        
        // Validate that required fields are present in JSON
        if (!configJson.contains("\"nodeType\"") || !configJson.contains("\"vehicleConfig\"")) {
            throw new CValidationException("Configuration JSON must contain 'nodeType' and 'vehicleConfig' fields");
        }
        
        // Additional vehicle-specific validation
        if (!configJson.contains("\"vehicleId\"") || !configJson.contains("\"canAddress\"")) {
            throw new CValidationException("Vehicle configuration must contain 'vehicleId' and 'canAddress' fields");
        }
    }
    
    @Override
    protected String generateDefaultNodeConfiguration(final CVehicleNode entity) {
        return String.format("""
            {
                "nodeId": "%s",
                "nodeType": "VEHICLE",
                "physicalInterface": "%s",
                "active": %s,
                "priority": %d,
                "vehicleConfig": {
                    "vehicleId": "%s",
                    "canAddress": "0x%X",
                    "canAddressHex": "0x%X",
                    "baudRate": %d,
                    "vehicleType": "%s",
                    "manufacturer": "%s",
                    "modelYear": %s,
                    "canProtocol": "%s",
                    "extendedAddressing": %s,
                    "canFdSupport": %s
                },
                "calimeroConfig": {
                    "enabled": true,
                    "exportFormat": "CAN_GATEWAY",
                    "monitoringEnabled": true,
                    "loggingLevel": "INFO"
                }
            }
            """, 
            entity.getId() != null ? entity.getId().toString() : "new",
            entity.getPhysicalInterface() != null ? entity.getPhysicalInterface() : "can1",
            entity.getIsActive() != null ? entity.getIsActive() : true,
            entity.getPriorityLevel() != null ? entity.getPriorityLevel() : 50,
            entity.getVehicleId() != null ? entity.getVehicleId() : "VEHICLE_001",
            entity.getCanAddress() != null ? entity.getCanAddress() : 0x100,
            entity.getCanAddress() != null ? entity.getCanAddress() : 0x100,
            entity.getBaudRate() != null ? entity.getBaudRate() : 500000,
            entity.getVehicleType() != null ? entity.getVehicleType() : "CAR",
            entity.getManufacturer() != null ? entity.getManufacturer() : "Unknown",
            entity.getModelYear() != null ? entity.getModelYear() : "null",
            entity.getCanProtocol() != null ? entity.getCanProtocol() : "CAN 2.0B",
            entity.getCanAddress() != null && entity.getCanAddress() > MAX_CAN_ADDRESS,
            entity.getCanProtocol() != null && "CAN-FD".equalsIgnoreCase(entity.getCanProtocol()));
    }
    
    @Override
    protected void initializeNodeSpecificDefaults(final CVehicleNode entity) {
        // Vehicle specific initialization
        if (entity.getVehicleId() == null || entity.getVehicleId().isEmpty()) {
            entity.setVehicleId("VEHICLE_" + System.currentTimeMillis() % 10000);
        }
        
        if (entity.getCanAddress() == null) {
            entity.setCanAddress(0x100); // Default CAN address
        }
        
        if (entity.getBaudRate() == null) {
            entity.setBaudRate(500000); // 500K - most common
        }
        
        if (entity.getVehicleType() == null || entity.getVehicleType().isEmpty()) {
            entity.setVehicleType("CAR");
        }
        
        if (entity.getCanProtocol() == null || entity.getCanProtocol().isEmpty()) {
            entity.setCanProtocol("CAN 2.0B");
        }
        
        // Set default physical interface for vehicles
        if (entity.getPhysicalInterface() == null || entity.getPhysicalInterface().isEmpty()) {
            entity.setPhysicalInterface("can1");
        }
        
        LOGGER.debug("Initialized vehicle node '{}' with ID '{}' on {} at 0x{}", 
                    entity.getName(), entity.getVehicleId(), 
                    entity.getPhysicalInterface(), Integer.toHexString(entity.getCanAddress()).toUpperCase());
    }
    
    @Override
    protected void copyNodeSpecificFields(final CVehicleNode source, final CVehicleNode target, final CCloneOptions options) {
        // Copy vehicle specific fields
        
        // Make vehicle ID unique for copy
        if (source.getVehicleId() != null) {
            target.setVehicleId(source.getVehicleId() + "_COPY");
        }
        
        // CAN address needs to be different to avoid conflicts
        if (source.getCanAddress() != null) {
            target.setCanAddress(source.getCanAddress() + 1);
        }
        
        target.setBaudRate(source.getBaudRate());
        target.setVehicleType(source.getVehicleType());
        target.setManufacturer(source.getManufacturer());
        target.setModelYear(source.getModelYear());
        target.setCanProtocol(source.getCanProtocol());
        
        LOGGER.debug("Copied vehicle specific fields from '{}' to '{}'", 
                    source.getName(), target.getName());
    }
    
    // Vehicle specific business methods
    
    /**
     * Find vehicles by vehicle ID and project.
     */
    @Transactional(readOnly = true)
    public List<CVehicleNode> findByVehicleId(final String vehicleId, final tech.derbent.api.projects.domain.CProject<?> project) {
        Check.notBlank(vehicleId, "Vehicle ID cannot be blank");
        Check.notNull(project, "Project cannot be null");
        
        final var result = ((IVehicleNodeRepository) repository).findByVehicleIdAndProject(vehicleId, project);
        return result.map(List::of).orElse(List.of());
    }
    
    /**
     * Find vehicles by baud rate.
     */
    @Transactional(readOnly = true)
    public List<CVehicleNode> findByBaudRate(final Integer baudRate, final tech.derbent.api.projects.domain.CProject<?> project) {
        Check.notNull(baudRate, "Baud rate cannot be null");
        Check.notNull(project, "Project cannot be null");
        
        return ((IVehicleNodeRepository) repository).findByBaudRateAndProject(baudRate, project);
    }
    
    /**
     * Find vehicles by type.
     */
    @Transactional(readOnly = true)
    public List<CVehicleNode> findByVehicleType(final String vehicleType, final tech.derbent.api.projects.domain.CProject<?> project) {
        Check.notBlank(vehicleType, "Vehicle type cannot be blank");
        Check.notNull(project, "Project cannot be null");
        
        return ((IVehicleNodeRepository) repository).findByVehicleTypeAndProject(vehicleType, project);
    }
    
    /**
     * Find vehicles by manufacturer.
     */
    @Transactional(readOnly = true)
    public List<CVehicleNode> findByManufacturer(final String manufacturer, final tech.derbent.api.projects.domain.CProject<?> project) {
        Check.notBlank(manufacturer, "Manufacturer cannot be blank");
        Check.notNull(project, "Project cannot be null");
        
        return ((IVehicleNodeRepository) repository).findByManufacturerAndProject(manufacturer, project);
    }
    
    /**
     * Find CAN-FD capable vehicles.
     */
    @Transactional(readOnly = true)
    public List<CVehicleNode> findCanFdVehicles(final tech.derbent.api.projects.domain.CProject<?> project) {
        Check.notNull(project, "Project cannot be null");
        
        return ((IVehicleNodeRepository) repository).findCanFdVehicles(project);
    }
    
    /**
     * Find commercial vehicles.
     */
    @Transactional(readOnly = true)
    public List<CVehicleNode> findCommercialVehicles(final tech.derbent.api.projects.domain.CProject<?> project) {
        Check.notNull(project, "Project cannot be null");
        
        return ((IVehicleNodeRepository) repository).findCommercialVehicles(project);
    }
    
    /**
     * Check if CAN address is available on a specific interface.
     */
    @Transactional(readOnly = true)
    public boolean isCanAddressAvailable(final String physicalInterface, final Integer canAddress, 
                                       final tech.derbent.api.projects.domain.CProject<?> project, final Long excludeVehicleId) {
        Check.notBlank(physicalInterface, "Physical interface cannot be blank");
        Check.notNull(canAddress, "CAN address cannot be null");
        Check.notNull(project, "Project cannot be null");
        
        return !((IVehicleNodeRepository) repository).existsByCanAddressAndPhysicalInterfaceExcluding(
            canAddress, physicalInterface, project, excludeVehicleId);
    }
    
    /**
     * Generate next available CAN address on interface.
     */
    @Transactional(readOnly = true)
    public Integer getNextAvailableCanAddress(final String physicalInterface, final tech.derbent.api.projects.domain.CProject<?> project) {
        Check.notBlank(physicalInterface, "Physical interface cannot be blank");
        Check.notNull(project, "Project cannot be null");
        
        // Start from 0x100 and find next available
        for (int address = 0x100; address <= MAX_CAN_ADDRESS; address++) {
            if (isCanAddressAvailable(physicalInterface, address, project, null)) {
                return address;
            }
        }
        
        throw new CValidationException(String.format(
            "No available CAN addresses on interface '%s'", physicalInterface));
    }
    
    /**
     * Update vehicle CAN configuration.
     */
    @Transactional
    public void updateCanConfiguration(final CVehicleNode vehicle, 
                                     final Integer canAddress, 
                                     final Integer baudRate,
                                     final String canProtocol) {
        Check.notNull(vehicle, "Vehicle cannot be null");
        
        // Update configuration
        if (canAddress != null) {
            vehicle.setCanAddress(canAddress);
        }
        if (baudRate != null) {
            vehicle.setBaudRate(baudRate);
        }
        if (canProtocol != null) {
            vehicle.setCanProtocol(canProtocol);
        }
        
        // Regenerate configuration JSON
        regenerateNodeConfiguration(vehicle);
        
        LOGGER.info("Updated CAN configuration for vehicle '{}'", vehicle.getName());
    }
    
    // IEntityRegistrable implementation
    @Override
    public Class<?> getInitializerServiceClass() { 
        return Object.class; // Placeholder - will be updated in Phase 8
    }
    
    @Override
    public Class<?> getPageServiceClass() { 
        return Object.class; // Placeholder - will be updated in Phase 8
    }
    
    @Override
    public Class<?> getServiceClass() { 
        return this.getClass(); 
    }
}