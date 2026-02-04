package tech.derbent.bab.dashboard.dashboardinterfaces.domain;

import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.dashboard.domain.CDashboardProject;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.bab.dashboard.dashboardinterfaces.service.CDashboardInterfacesService;
import tech.derbent.bab.dashboard.dashboardinterfaces.service.CPageServiceDashboardInterfaces;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;
import tech.derbent.plm.links.domain.CLink;
import tech.derbent.plm.links.domain.IHasLinks;

/**
 * CDashboardInterfaces - BAB-specific dashboard for interface configuration and management.
 * <p>
 * Layer: Domain (MVC)
 * Active when: 'bab' profile is active
 * <p>
 * Following Derbent pattern: Concrete entity with @Entity annotation.
 * This is a PROJECT ENTITY (extends CProjectItem via CDashboardProject).
 * <p>
 * Used for BAB gateway interface configuration dashboard with specialized components for:
 * <ul>
 * <li>CAN Interface Management - CAN bus configuration and monitoring</li>
 * <li>Ethernet Interface Settings - Network interface configuration</li>
 * <li>Serial Interface Configuration - RS232/RS485 port settings</li>
 * <li>ROS Node Management - ROS communication node configuration</li>
 * <li>Interface Summary - Overview of all interface statuses</li>
 * </ul>
 * <p>
 * Follows the BAB @Transient placeholder pattern for component integration.
 */
@Entity
@Table(name = "cdashboard_interfaces", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"project_id", "name"})
})
@AttributeOverride(name = "id", column = @Column(name = "dashboard_interfaces_id"))
public class CDashboardInterfaces extends CDashboardProject<CDashboardInterfaces>
        implements IHasAttachments, IHasComments, IHasLinks, IEntityRegistrable {

    // Entity constants (MANDATORY)
    public static final String DEFAULT_COLOR = "#FF5722"; // Deep Orange - Interface/Hardware
    public static final String DEFAULT_ICON = "vaadin:connect";
    public static final String ENTITY_TITLE_PLURAL = "Interface Configuration";
    public static final String ENTITY_TITLE_SINGULAR = "Interface Configuration";
    public static final String VIEW_NAME = "Interface Configuration Dashboard";
    
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(CDashboardInterfaces.class);

    // Standard composition fields - initialized at declaration (RULE 5)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "dashboard_interfaces_id")
    @AMetaData(
        displayName = "Attachments",
        required = false,
        readOnly = false,
        description = "File attachments for interface configuration",
        hidden = false,
        dataProviderBean = "CAttachmentService",
        createComponentMethod = "createComponent"
    )
    private Set<CAttachment> attachments = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "dashboard_interfaces_id")
    @AMetaData(
        displayName = "Comments",
        required = false,
        readOnly = false,
        description = "Comments for interface configuration",
        hidden = false,
        dataProviderBean = "CCommentService",
        createComponentMethod = "createComponentComment"
    )
    private Set<CComment> comments = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "dashboard_interfaces_id")
    @AMetaData(
        displayName = "Links",
        required = false,
        readOnly = false,
        description = "Related links for interface configuration",
        hidden = false,
        dataProviderBean = "CLinkService",
        createComponentMethod = "createComponent"
    )
    private Set<CLink> links = new HashSet<>();

    // Interface-specific fields
    @Column(name = "is_active", nullable = false)
    @AMetaData(
        displayName = "Active",
        required = true,
        readOnly = false,
        description = "Whether this interface dashboard is currently active",
        hidden = false
    )
    private Boolean isActive = true;

    @Column(name = "configuration_mode", length = 50)
    @AMetaData(
        displayName = "Configuration Mode",
        required = false,
        readOnly = false,
        description = "Interface configuration mode (automatic, manual, hybrid)",
        hidden = false,
        maxLength = 50
    )
    private String configurationMode = "automatic";

    // BAB Interface Component Placeholders (MANDATORY pattern: entity-typed, @Transient, = null, NO final)
    
    // Summary Section
    @AMetaData(
        displayName = "Interface Summary",
        required = false,
        readOnly = false,
        description = "Overview of all interface types and their status",
        hidden = false,
        dataProviderBean = "pageservice",
        createComponentMethod = "createComponentInterfaceSummary",
        captionVisible = false
    )
    @Transient
    private CDashboardInterfaces placeHolder_createComponentInterfaceSummary = null;

    // CAN Interface Section
    @AMetaData(
        displayName = "CAN Interfaces",
        required = false,
        readOnly = false,
        description = "CAN bus interface configuration and monitoring",
        hidden = false,
        dataProviderBean = "pageservice",
        createComponentMethod = "createComponentCanInterfaces",
        captionVisible = false
    )
    @Transient
    private CDashboardInterfaces placeHolder_createComponentCanInterfaces = null;

    // Ethernet Interface Section
    @AMetaData(
        displayName = "Ethernet Interfaces",
        required = false,
        readOnly = false,
        description = "Network interface configuration and settings",
        hidden = false,
        dataProviderBean = "pageservice",
        createComponentMethod = "createComponentEthernetInterfaces",
        captionVisible = false
    )
    @Transient
    private CDashboardInterfaces placeHolder_createComponentEthernetInterfaces = null;

    // Serial Interface Section
    @AMetaData(
        displayName = "Serial Interfaces",
        required = false,
        readOnly = false,
        description = "RS232/RS485 serial port configuration",
        hidden = false,
        dataProviderBean = "pageservice",
        createComponentMethod = "createComponentSerialInterfaces",
        captionVisible = false
    )
    @Transient
    private CDashboardInterfaces placeHolder_createComponentSerialInterfaces = null;

    // ROS Node Section
    @AMetaData(
        displayName = "ROS Nodes",
        required = false,
        readOnly = false,
        description = "ROS communication node configuration and management",
        hidden = false,
        dataProviderBean = "pageservice",
        createComponentMethod = "createComponentRosNodes",
        captionVisible = false
    )
    @Transient
    private CDashboardInterfaces placeHolder_createComponentRosNodes = null;

    // Modbus Interface Section
    @AMetaData(
        displayName = "Modbus Interfaces",
        required = false,
        readOnly = false,
        description = "Modbus TCP/RTU interface configuration",
        hidden = false,
        dataProviderBean = "pageservice",
        createComponentMethod = "createComponentModbusInterfaces",
        captionVisible = false
    )
    @Transient
    private CDashboardInterfaces placeHolder_createComponentModbusInterfaces = null;

    /** Default constructor for JPA. */
    protected CDashboardInterfaces() {
        // JPA constructors do NOT call initializeDefaults() (RULE 1)
    }

    public CDashboardInterfaces(final String name, final CProject<?> project) {
        super(CDashboardInterfaces.class, name, project);
        initializeDefaults(); // Business constructors MUST call this (RULE 2)
    }

    private final void initializeDefaults() {
        // Initialize default values
        isActive = true;
        configurationMode = "automatic";
        
        // Initialize service
        CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
    }

    // Interface implementations
    @Override
    public Set<CAttachment> getAttachments() {
        return attachments;
    }

    @Override
    public void setAttachments(Set<CAttachment> attachments) {
        this.attachments = attachments;
    }

    @Override
    public Set<CComment> getComments() {
        return comments;
    }

    @Override
    public void setComments(Set<CComment> comments) {
        this.comments = comments;
    }

    @Override
    public Set<CLink> getLinks() {
        return links;
    }

    @Override
    public void setLinks(Set<CLink> links) {
        this.links = links;
    }

    @Override
    public Class<?> getPageServiceClass() {
        return CPageServiceDashboardInterfaces.class;
    }

    @Override
    public Class<?> getServiceClass() {
        return CDashboardInterfacesService.class;
    }

    // Getters and setters
    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getConfigurationMode() {
        return configurationMode;
    }

    public void setConfigurationMode(String configurationMode) {
        this.configurationMode = configurationMode;
    }

    // BAB Component Placeholder Getters (MANDATORY pattern: return this entity)
    
    /**
     * Getter for transient placeholder field - returns entity itself for component binding.
     * Following CDashboardProject_Bab pattern: transient entity-typed field with getter returning 'this'.
     * 
     * @return this entity (for CFormBuilder binding to CComponentInterfaceSummary)
     */
    public CDashboardInterfaces getPlaceHolder_createComponentInterfaceSummary() {
        return this;
    }

    /**
     * Getter for transient placeholder field - returns entity itself for component binding.
     * 
     * @return this entity (for CFormBuilder binding to CComponentCanInterfaces)
     */
    public CDashboardInterfaces getPlaceHolder_createComponentCanInterfaces() {
        return this;
    }

    /**
     * Getter for transient placeholder field - returns entity itself for component binding.
     * 
     * @return this entity (for CFormBuilder binding to CComponentEthernetInterfaces)
     */
    public CDashboardInterfaces getPlaceHolder_createComponentEthernetInterfaces() {
        return this;
    }

    /**
     * Getter for transient placeholder field - returns entity itself for component binding.
     * 
     * @return this entity (for CFormBuilder binding to CComponentSerialInterfaces)
     */
    public CDashboardInterfaces getPlaceHolder_createComponentSerialInterfaces() {
        return this;
    }

    /**
     * Getter for transient placeholder field - returns entity itself for component binding.
     * 
     * @return this entity (for CFormBuilder binding to CComponentRosNodes)
     */
    public CDashboardInterfaces getPlaceHolder_createComponentRosNodes() {
        return this;
    }

    /**
     * Getter for transient placeholder field - returns entity itself for component binding.
     * 
     * @return this entity (for CFormBuilder binding to CComponentModbusInterfaces)
     */
    public CDashboardInterfaces getPlaceHolder_createComponentModbusInterfaces() {
        return this;
    }
}