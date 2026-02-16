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

/** CDashboardInterfaces - BAB-specific dashboard for interface configuration and management.
 * <p>
 * Layer: Domain (MVC) Active when: 'bab' profile is active
 * <p>
 * Following Derbent pattern: Concrete entity with @Entity annotation. This is a PROJECT ENTITY (extends CProjectItem via CDashboardProject).
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
 * Follows the BAB @Transient placeholder pattern for component integration. */
@Entity
@Table (name = "cdashboard_interfaces", uniqueConstraints = {
		@UniqueConstraint (columnNames = {
				"project_id", "name"
		})
})
@AttributeOverride (name = "id", column = @Column (name = "dashboard_interfaces_id"))
public class CDashboardInterfaces extends CDashboardProject<CDashboardInterfaces>
		implements IHasAttachments, IHasComments, IHasLinks, IEntityRegistrable {

	// Entity constants (MANDATORY)
	public static final String DEFAULT_COLOR = "#FF5722"; // Deep Orange - Interface/Hardware
	public static final String DEFAULT_ICON = "vaadin:connect";
	public static final String ENTITY_TITLE_PLURAL = "Interface Configuration";
	public static final String ENTITY_TITLE_SINGULAR = "Interface Configuration";
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CDashboardInterfaces.class);
	public static final String VIEW_NAME = "Interface Configuration Dashboard";
	// Standard composition fields - initialized at declaration (RULE 5)
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "dashboard_interfaces_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "File attachments for interface configuration",
			hidden = false, dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "dashboard_interfaces_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for interface configuration", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponentComment"
	)
	private Set<CComment> comments = new HashSet<>();
	@Column (name = "configuration_mode", length = 50)
	@AMetaData (
			displayName = "Configuration Mode", required = false, readOnly = false,
			description = "Interface configuration mode (automatic, manual, hybrid)", hidden = false, maxLength = 50
	)
	private String configurationMode = "automatic";
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "dashboard_interfaces_id")
	@AMetaData (
			displayName = "Links", required = false, readOnly = false, description = "Related links for interface configuration", hidden = false,
			dataProviderBean = "CLinkService", createComponentMethod = "createComponent"
	)
	private Set<CLink> links = new HashSet<>();
	// Audio Device Section (NEW)
	@AMetaData (
			displayName = "Audio Devices", required = false, readOnly = false, description = "Audio device information and configuration",
			hidden = false, dataProviderBean = "pageservice", createComponentMethod = "createComponentAudioDevices", captionVisible = false
	)
	@Transient
	private CDashboardInterfaces placeHolder_createComponentAudioDevices = null;
	// BAB Interface Component Placeholders (MANDATORY pattern: entity-typed, @Transient, = null, NO final)
	// CAN Interface Section
	@AMetaData (
			displayName = "CAN Interfaces", required = false, readOnly = false, description = "CAN bus interface configuration and monitoring",
			hidden = false, dataProviderBean = "pageservice", createComponentMethod = "createComponentCanInterfaces", captionVisible = false
	)
	@Transient
	private CDashboardInterfaces placeHolder_createComponentCanInterfaces = null;
	// Ethernet Interface Section
	@AMetaData (
			displayName = "Ethernet Interfaces", required = false, readOnly = false, description = "Network interface configuration and settings",
			hidden = false, dataProviderBean = "pageservice", createComponentMethod = "createComponentEthernetInterfaces", captionVisible = false
	)
	@Transient
	private CDashboardInterfaces placeHolder_createComponentEthernetInterfaces = null;
	// Summary Section
	@AMetaData (
			displayName = "Interface Summary", required = false, readOnly = false, description = "Overview of all interface types and their status",
			hidden = false, dataProviderBean = "pageservice", createComponentMethod = "createComponentInterfaceSummary", captionVisible = false
	)
	@Transient
	private CDashboardInterfaces placeHolder_createComponentInterfaceSummary = null;
	// Modbus Interface Section
	@AMetaData (
			displayName = "Modbus Interfaces", required = false, readOnly = false, description = "Modbus TCP/RTU interface configuration",
			hidden = false, dataProviderBean = "pageservice", createComponentMethod = "createComponentModbusInterfaces", captionVisible = false
	)
	@Transient
	private CDashboardInterfaces placeHolder_createComponentModbusInterfaces = null;
	// ROS Node Section
	@AMetaData (
			displayName = "ROS Nodes", required = false, readOnly = false, description = "ROS communication node configuration and management",
			hidden = false, dataProviderBean = "pageservice", createComponentMethod = "createComponentRosNodes", captionVisible = false
	)
	@Transient
	private CDashboardInterfaces placeHolder_createComponentRosNodes = null;
	// Serial Interface Section
	@AMetaData (
			displayName = "Serial Interfaces", required = false, readOnly = false, description = "RS232/RS485 serial port configuration",
			hidden = false, dataProviderBean = "pageservice", createComponentMethod = "createComponentSerialInterfaces", captionVisible = false
	)
	@Transient
	private CDashboardInterfaces placeHolder_createComponentSerialInterfaces = null;
	// USB Interface Section
	@AMetaData (
			displayName = "USB Devices", required = false, readOnly = false, description = "USB device information and management", hidden = false,
			dataProviderBean = "pageservice", createComponentMethod = "createComponentUsbInterfaces", captionVisible = false
	)
	@Transient
	private CDashboardInterfaces placeHolder_createComponentUsbInterfaces = null;

	/** Default constructor for JPA. */
	protected CDashboardInterfaces() {
		// JPA constructors do NOT call initializeDefaults() (RULE 1)
	}

	public CDashboardInterfaces(final String name, final CProject<?> project) {
		super(CDashboardInterfaces.class, name, project);
		initializeDefaults(); // Business constructors MUST call this (RULE 2)
	}

	// Interface implementations
	@Override
	public Set<CAttachment> getAttachments() { return attachments; }

	@Override
	public Set<CComment> getComments() { return comments; }

	public String getConfigurationMode() { return configurationMode; }

	@Override
	public Set<CLink> getLinks() { return links; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceDashboardInterfaces.class; }

	/** Getter for transient placeholder field - returns entity itself for component binding.
	 * @return this entity (for CFormBuilder binding to CComponentAudioDevices) */
	public CDashboardInterfaces getPlaceHolder_createComponentAudioDevices() {
		return this;
	}

	/** Getter for transient placeholder field - returns entity itself for component binding.
	 * @return this entity (for CFormBuilder binding to CComponentCanInterfaces) */
	public CDashboardInterfaces getPlaceHolder_createComponentCanInterfaces() {
		return this;
	}

	/** Getter for transient placeholder field - returns entity itself for component binding.
	 * @return this entity (for CFormBuilder binding to CComponentEthernetInterfaces) */
	public CDashboardInterfaces getPlaceHolder_createComponentEthernetInterfaces() {
		return this;
	}

	/** Getter for transient placeholder field - returns entity itself for component binding. Following CDashboardProject_Bab pattern: transient
	 * entity-typed field with getter returning 'this'.
	 * @return this entity (for CFormBuilder binding to CComponentInterfaceSummary) */
	public CDashboardInterfaces getPlaceHolder_createComponentInterfaceSummary() {
		return this;
	}

	/** Getter for transient placeholder field - returns entity itself for component binding.
	 * @return this entity (for CFormBuilder binding to CComponentModbusInterfaces) */
	public CDashboardInterfaces getPlaceHolder_createComponentModbusInterfaces() {
		return this;
	}

	/** Getter for transient placeholder field - returns entity itself for component binding.
	 * @return this entity (for CFormBuilder binding to CComponentRosNodes) */
	public CDashboardInterfaces getPlaceHolder_createComponentRosNodes() {
		return this;
	}

	/** Getter for transient placeholder field - returns entity itself for component binding.
	 * @return this entity (for CFormBuilder binding to CComponentSerialInterfaces) */
	public CDashboardInterfaces getPlaceHolder_createComponentSerialInterfaces() {
		return this;
	}

	/** Getter for transient placeholder field - returns entity itself for component binding.
	 * @return this entity (for CFormBuilder binding to CComponentUsbInterfaces) */
	public CDashboardInterfaces getPlaceHolder_createComponentUsbInterfaces() {
		return this;
	}

	@Override
	public Class<?> getServiceClass() { return CDashboardInterfacesService.class; }
	// BAB Component Placeholder Getters (MANDATORY pattern: return this entity)

	private final void initializeDefaults() {
		// Initialize default values
		configurationMode = "automatic";
		// Initialize service
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	@Override
	public void setAttachments(Set<CAttachment> attachments) { this.attachments = attachments; }

	@Override
	public void setComments(Set<CComment> comments) { this.comments = comments; }

	public void setConfigurationMode(String configurationMode) { this.configurationMode = configurationMode; }

	@Override
	public void setLinks(Set<CLink> links) { this.links = links; }

	public void setPlaceHolder_createComponentAudioDevices(CDashboardInterfaces placeHolder_createComponentAudioDevices) {
		this.placeHolder_createComponentAudioDevices = placeHolder_createComponentAudioDevices;
	}

	public void setPlaceHolder_createComponentCanInterfaces(CDashboardInterfaces placeHolder_createComponentCanInterfaces) {
		this.placeHolder_createComponentCanInterfaces = placeHolder_createComponentCanInterfaces;
	}

	public void setPlaceHolder_createComponentEthernetInterfaces(CDashboardInterfaces placeHolder_createComponentEthernetInterfaces) {
		this.placeHolder_createComponentEthernetInterfaces = placeHolder_createComponentEthernetInterfaces;
	}

	public void setPlaceHolder_createComponentInterfaceSummary(CDashboardInterfaces placeHolder_createComponentInterfaceSummary) {
		this.placeHolder_createComponentInterfaceSummary = placeHolder_createComponentInterfaceSummary;
	}

	public void setPlaceHolder_createComponentModbusInterfaces(CDashboardInterfaces placeHolder_createComponentModbusInterfaces) {
		this.placeHolder_createComponentModbusInterfaces = placeHolder_createComponentModbusInterfaces;
	}

	public void setPlaceHolder_createComponentRosNodes(CDashboardInterfaces placeHolder_createComponentRosNodes) {
		this.placeHolder_createComponentRosNodes = placeHolder_createComponentRosNodes;
	}

	public void setPlaceHolder_createComponentSerialInterfaces(CDashboardInterfaces placeHolder_createComponentSerialInterfaces) {
		this.placeHolder_createComponentSerialInterfaces = placeHolder_createComponentSerialInterfaces;
	}

	public void setPlaceHolder_createComponentUsbInterfaces(CDashboardInterfaces placeHolder_createComponentUsbInterfaces) {
		this.placeHolder_createComponentUsbInterfaces = placeHolder_createComponentUsbInterfaces;
	}
}
