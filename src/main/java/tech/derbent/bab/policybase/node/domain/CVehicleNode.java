package tech.derbent.bab.policybase.node.domain;

import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;
import tech.derbent.plm.links.domain.CLink;
import tech.derbent.plm.links.domain.IHasLinks;

/**
 * CVehicleNode - Vehicle virtual network node entity for CAN bus communication.
 * 
 * Layer: Domain (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Concrete entity with @Entity annotation.
 * 
 * Represents vehicle virtual nodes mapped to physical CAN bus interfaces.
 * Example: vehicleX mapped to can1 interface for automotive communication.
 * 
 * Used in BAB Actions Dashboard policy rule engine for vehicle data routing
 * and IoT automotive gateway integration.
 */
@Entity
@Table(name = "cnode_vehicle", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"project_id", "name"}),
    @UniqueConstraint(columnNames = {"project_id", "vehicle_id"}),
    @UniqueConstraint(columnNames = {"project_id", "physical_interface", "can_address"})
})
@AttributeOverride(name = "id", column = @Column(name = "vehicle_node_id"))
@Profile("bab")
public class CVehicleNode extends CNodeEntity<CVehicleNode> 
    implements IHasAttachments, IHasComments, IHasLinks, IEntityRegistrable {
    
    // Entity constants (MANDATORY - overriding base class constants)
    public static final String DEFAULT_COLOR = "#FF9800"; // Orange - Vehicle/Automotive
    public static final String DEFAULT_ICON = "vaadin:car";
    public static final String ENTITY_TITLE_PLURAL = "Vehicle Nodes";
    public static final String ENTITY_TITLE_SINGULAR = "Vehicle Node";
    private static final Logger LOGGER = LoggerFactory.getLogger(CVehicleNode.class);
    public static final String VIEW_NAME = "Vehicle Nodes View";
    
    // Vehicle specific fields
    @Column(name = "vehicle_id", length = 50, nullable = false)
    @AMetaData(
        displayName = "Vehicle ID",
        required = true,
        readOnly = false,
        description = "Unique identifier for the vehicle (e.g., VIN, fleet number)",
        hidden = false,
        maxLength = 50
    )
    private String vehicleId;
    
    @Column(name = "can_address", nullable = false)
    @AMetaData(
        displayName = "CAN Address",
        required = true,
        readOnly = false,
        description = "CAN bus address for this vehicle node (hexadecimal)",
        hidden = false
    )
    private Integer canAddress;
    
    @Column(name = "baud_rate", nullable = false)
    @AMetaData(
        displayName = "Baud Rate",
        required = false,
        readOnly = false,
        description = "CAN bus communication baud rate (e.g., 500000, 250000)",
        hidden = false
    )
    private Integer baudRate = 500000;
    
    @Column(name = "vehicle_type", length = 30, nullable = false)
    @AMetaData(
        displayName = "Vehicle Type",
        required = false,
        readOnly = false,
        description = "Type of vehicle (CAR, TRUCK, MOTORCYCLE, BUS, etc.)",
        hidden = false,
        maxLength = 30
    )
    private String vehicleType = "CAR";
    
    @Column(name = "manufacturer", length = 50)
    @AMetaData(
        displayName = "Manufacturer",
        required = false,
        readOnly = false,
        description = "Vehicle manufacturer (Ford, Toyota, etc.)",
        hidden = false,
        maxLength = 50
    )
    private String manufacturer;
    
    @Column(name = "model_year")
    @AMetaData(
        displayName = "Model Year",
        required = false,
        readOnly = false,
        description = "Vehicle model year",
        hidden = false
    )
    private Integer modelYear;
    
    @Column(name = "can_protocol", length = 20, nullable = false)
    @AMetaData(
        displayName = "CAN Protocol",
        required = false,
        readOnly = false,
        description = "CAN protocol version (CAN 2.0A, CAN 2.0B, CAN-FD)",
        hidden = false,
        maxLength = 20
    )
    private String canProtocol = "CAN 2.0B";
    
    // Standard composition fields - initialized at declaration (RULE 5)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_node_id")
    @AMetaData(
        displayName = "Attachments",
        required = false,
        readOnly = false,
        description = "File attachments for this vehicle node",
        hidden = false,
        dataProviderBean = "CAttachmentService",
        createComponentMethod = "createComponent"
    )
    private Set<CAttachment> attachments = new HashSet<>();
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_node_id")
    @AMetaData(
        displayName = "Comments",
        required = false,
        readOnly = false,
        description = "Comments for this vehicle node",
        hidden = false,
        dataProviderBean = "CCommentService",
        createComponentMethod = "createComponentComment"
    )
    private Set<CComment> comments = new HashSet<>();
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_node_id")
    @AMetaData(
        displayName = "Links",
        required = false,
        readOnly = false,
        description = "Related links for this vehicle node",
        hidden = false,
        dataProviderBean = "CLinkService",
        createComponentMethod = "createComponent"
    )
    private Set<CLink> links = new HashSet<>();
    
    /** Default constructor for JPA. */
    protected CVehicleNode() {
        super();
        // JPA constructors do NOT call initializeDefaults() (RULE 1)
    }
    
    public CVehicleNode(final String name, final CProject<?> project) {
        super(CVehicleNode.class, name, project, "VEHICLE");
        initializeDefaults(); // Business constructors MUST call this (RULE 2)
    }
    
    public CVehicleNode(final String name, final CProject<?> project, 
                       final String vehicleId, final Integer canAddress) {
        super(CVehicleNode.class, name, project, "VEHICLE");
        this.vehicleId = vehicleId;
        this.canAddress = canAddress;
        initializeDefaults(); // Business constructors MUST call this (RULE 2)
    }
    
    /** Initialize intrinsic defaults (RULE 3). */
    private final void initializeDefaults() {
        // Initialize nullable=false fields with defaults (already done in field declarations)
        
        // Vehicle specific defaults
        if (vehicleId == null || vehicleId.isEmpty()) {
            vehicleId = "VEHICLE_" + System.currentTimeMillis() % 10000;
        }
        if (canAddress == null) {
            canAddress = 0x100; // Default CAN address
        }
        if (baudRate == null) {
            baudRate = 500000;
        }
        if (vehicleType == null || vehicleType.isEmpty()) {
            vehicleType = "CAR";
        }
        if (canProtocol == null || canProtocol.isEmpty()) {
            canProtocol = "CAN 2.0B";
        }
        
        // Set default physical interface if not set
        if (getPhysicalInterface() == null || getPhysicalInterface().isEmpty()) {
            setPhysicalInterface("can1");
        }
        
        // Generate initial node configuration JSON
        setNodeConfigJson(generateDefaultNodeConfig());
        
        // MANDATORY: Call service initialization at end (RULE 3)
        CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
    }
    
    @Override
    protected String generateDefaultNodeConfig() {
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
                    "baudRate": %d,
                    "vehicleType": "%s",
                    "manufacturer": "%s",
                    "modelYear": %s,
                    "canProtocol": "%s"
                }
            }
            """, getId(), getPhysicalInterface(), getIsActive(), getPriorityLevel(),
                vehicleId, canAddress, baudRate, vehicleType, 
                manufacturer != null ? manufacturer : "Unknown",
                modelYear != null ? modelYear : "null",
                canProtocol);
    }
    
    @Override
    public String getEntityColor() {
        return DEFAULT_COLOR;
    }
    
    /**
     * Get the CAN address in hexadecimal format.
     * @return CAN address as hex string
     */
    public String getCanAddressHex() {
        return canAddress != null ? String.format("0x%X", canAddress) : "0x000";
    }
    
    /**
     * Check if this vehicle supports CAN-FD protocol.
     * @return true if CAN-FD is supported
     */
    public boolean supportsCanFd() {
        return "CAN-FD".equalsIgnoreCase(canProtocol);
    }
    
    /**
     * Check if this vehicle is a commercial vehicle.
     * @return true if vehicle is truck or bus
     */
    public boolean isCommercialVehicle() {
        return "TRUCK".equalsIgnoreCase(vehicleType) || "BUS".equalsIgnoreCase(vehicleType);
    }
    
    // Interface implementations
    @Override
    public Set<CAttachment> getAttachments() { return attachments; }
    
    @Override
    public void setAttachments(Set<CAttachment> attachments) { this.attachments = attachments; }
    
    @Override
    public Set<CComment> getComments() { return comments; }
    
    @Override
    public void setComments(Set<CComment> comments) { this.comments = comments; }
    
    @Override
    public Set<CLink> getLinks() { return links; }
    
    @Override
    public void setLinks(Set<CLink> links) { this.links = links; }
    
    // IEntityRegistrable implementation
    @Override
    public Class<?> getServiceClass() { 
        return Object.class; 
    }
    
    @Override
    public Class<?> getPageServiceClass() { 
        return Object.class; 
    }
    
    // Vehicle specific getters and setters
    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { 
        this.vehicleId = vehicleId;
        updateLastModified();
    }
    
    public Integer getCanAddress() { return canAddress; }
    public void setCanAddress(Integer canAddress) { 
        this.canAddress = canAddress;
        updateLastModified();
    }
    
    public Integer getBaudRate() { return baudRate; }
    public void setBaudRate(Integer baudRate) { 
        this.baudRate = baudRate;
        updateLastModified();
    }
    
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { 
        this.vehicleType = vehicleType;
        updateLastModified();
    }
    
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { 
        this.manufacturer = manufacturer;
        updateLastModified();
    }
    
    public Integer getModelYear() { return modelYear; }
    public void setModelYear(Integer modelYear) { 
        this.modelYear = modelYear;
        updateLastModified();
    }
    
    public String getCanProtocol() { return canProtocol; }
    public void setCanProtocol(String canProtocol) { 
        this.canProtocol = canProtocol;
        updateLastModified();
    }
}