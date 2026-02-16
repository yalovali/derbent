package tech.derbent.bab.dashboard.dashboardproject_bab.domain;

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
import tech.derbent.bab.dashboard.dashboardproject_bab.service.CDashboardProject_BabService;
import tech.derbent.bab.dashboard.dashboardproject_bab.service.CPageServiceDashboardProject_Bab;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;
import tech.derbent.plm.links.domain.CLink;
import tech.derbent.plm.links.domain.IHasLinks;

/** CDashboardProject_Bab - BAB-specific dashboard project entity. Layer: Domain (MVC) Active when: 'bab' profile is active Following Derbent pattern:
 * Concrete entity with @Entity annotation. This is a PROJECT ENTITY (extends CProjectItem via CDashboardProject). Used for BAB gateway dashboard
 * visualization and monitoring. */
@Entity
@Table (name = "cdashboard_project_bab", uniqueConstraints = {
		@UniqueConstraint (columnNames = {
				"project_id", "name"
		})
})
@AttributeOverride (name = "id", column = @Column (name = "dashboard_project_id"))
public class CDashboardProject_Bab extends CDashboardProject<CDashboardProject_Bab>
		implements IHasAttachments, IHasComments, IHasLinks, IEntityRegistrable {

	// Entity constants (MANDATORY)
	public static final String DEFAULT_COLOR = "#009688"; // Teal - Dashboard/Monitoring
	public static final String DEFAULT_ICON = "vaadin:dashboard";
	public static final String ENTITY_TITLE_PLURAL = "BAB System Management";
	public static final String ENTITY_TITLE_SINGULAR = "BAB System Management";
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CDashboardProject_Bab.class);
	public static final String VIEW_NAME = "BAB Dashboard Projects View";
	// Standard composition fields - initialized at declaration (RULE 5)
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "dashboard_project_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "File attachments for this dashboard project",
			hidden = false, dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "dashboard_project_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this dashboard project", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponentComment"
	)
	private Set<CComment> comments = new HashSet<>();
	@Column (name = "dashboard_type", length = 50)
	@AMetaData (
			displayName = "Dashboard Type", required = false, readOnly = false,
			description = "Type of BAB dashboard (monitoring, control, reporting)", hidden = false, maxLength = 50
	)
	private String dashboardType = "monitoring";
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "dashboard_project_id")
	@AMetaData (
			displayName = "Links", required = false, readOnly = false, description = "Related links for this dashboard project", hidden = false,
			dataProviderBean = "CLinkService", createComponentMethod = "createComponent"
	)
	private Set<CLink> links = new HashSet<>();
	@AMetaData (
			displayName = "Disk Usage", required = false, readOnly = false, description = "Disk space monitoring", hidden = false,
			dataProviderBean = "pageservice", createComponentMethod = "createComponentDiskUsage", captionVisible = false
	)
	@Transient
	private CDashboardProject_Bab placeHolder_createComponentDiskUsage = null;
	@AMetaData (
			displayName = "DNS Configuration", required = false, readOnly = false, description = "DNS resolver configuration", hidden = false,
			dataProviderBean = "pageservice", createComponentMethod = "createComponentDnsConfiguration", captionVisible = false
	)
	@Transient
	private CDashboardProject_Bab placeHolder_createComponentDnsConfiguration = null;
	// BAB Component Placeholders (MANDATORY pattern: entity-typed, @Transient, = null, NO final)
	@AMetaData (
			displayName = "Interface List", required = false, readOnly = false, description = "Network interface configuration for this dashboard",
			hidden = false, dataProviderBean = "pageservice", createComponentMethod = "createComponentInterfaceList", captionVisible = false
	)
	@Transient
	private final CDashboardProject_Bab placeHolder_createComponentInterfaceList = null;
	@AMetaData (
			displayName = "Routing Table", required = false, readOnly = false, description = "Active routing table display", hidden = false,
			dataProviderBean = "pageservice", createComponentMethod = "createComponentRoutingTable", captionVisible = false
	)
	@Transient
	private CDashboardProject_Bab placeHolder_createComponentRoutingTable = null;
	@AMetaData (
			displayName = "System Metrics", required = false, readOnly = false, description = "Real-time system performance metrics", hidden = false,
			dataProviderBean = "pageservice", createComponentMethod = "createComponentSystemMetrics", captionVisible = false
	)
	@Transient
	private CDashboardProject_Bab placeHolder_createComponentSystemMetrics = null;
	@AMetaData (
			displayName = "System Processes", required = false, readOnly = false, description = "Running processes monitoring", hidden = false,
			dataProviderBean = "pageservice", createComponentMethod = "createComponentSystemProcessList", captionVisible = false
	)
	@Transient
	private CDashboardProject_Bab placeHolder_createComponentSystemProcessList = null;
	@AMetaData (
			displayName = "System Services", required = false, readOnly = false, description = "Systemd services status and management",
			hidden = false, dataProviderBean = "pageservice", createComponentMethod = "createComponentSystemServices", captionVisible = false
	)
	@Transient
	private CDashboardProject_Bab placeHolder_createComponentSystemServices = null;
	@AMetaData (
			displayName = "Webservice API Discovery", required = false, readOnly = false, description = "Available Calimero HTTP API endpoints",
			hidden = false, dataProviderBean = "pageservice", createComponentMethod = "createComponentWebServiceDiscovery", captionVisible = false
	)
	@Transient
	private final CDashboardProject_Bab placeHolder_createComponentWebServiceDiscovery = null;

	/** Default constructor for JPA. */
	protected CDashboardProject_Bab() {
		// JPA constructors do NOT call initializeDefaults() (RULE 1)
	}

	public CDashboardProject_Bab(final String name, final CProject<?> project) {
		super(CDashboardProject_Bab.class, name, project);
		initializeDefaults(); // Business constructors MUST call this (RULE 2)
	}

	// Interface implementations
	@Override
	public Set<CAttachment> getAttachments() { return attachments; }

	@Override
	public Set<CComment> getComments() { return comments; }

	public String getDashboardType() { return dashboardType; }
	// Getters and setters

	@Override
	public Set<CLink> getLinks() { return links; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceDashboardProject_Bab.class; }

	/** Getter for transient placeholder field - returns entity itself for component binding.
	 * @return this entity (for CFormBuilder binding to CComponentDiskUsage) */
	public CDashboardProject_Bab getPlaceHolder_createComponentDiskUsage() {
		return this;
	}

	/** Getter for transient placeholder field - returns entity itself for component binding.
	 * @return this entity (for CFormBuilder binding to CComponentDnsConfiguration) */
	public CDashboardProject_Bab getPlaceHolder_createComponentDnsConfiguration() {
		return this;
	}

	/** Getter for transient placeholder field - returns entity itself for component binding. Following CDashboardProject_Bab pattern: transient field
	 * with getter returning 'this'.
	 * @return this entity (for CFormBuilder binding to CComponentInterfaceList) */
	public CDashboardProject_Bab getPlaceHolder_createComponentInterfaceList() {
		return this;
	}

	/** Getter for transient placeholder field - returns entity itself for component binding.
	 * @return this entity (for CFormBuilder binding to CComponentRoutingTable) */
	public CDashboardProject_Bab getPlaceHolder_createComponentRoutingTable() {
		return this;
	}

	/** Getter for transient placeholder field - returns entity itself for component binding.
	 * @return this entity (for CFormBuilder binding to CComponentSystemMetrics) */
	public CDashboardProject_Bab getPlaceHolder_createComponentSystemMetrics() {
		return this;
	}

	/** Getter for transient placeholder field - returns entity itself for component binding.
	 * @return this entity (for CFormBuilder binding to CComponentSystemProcessList) */
	public CDashboardProject_Bab getPlaceHolder_createComponentSystemProcessList() {
		return this;
	}

	/** Getter for transient placeholder field - returns entity itself for component binding.
	 * @return this entity (for CFormBuilder binding to CComponentSystemServices) */
	public CDashboardProject_Bab getPlaceHolder_createComponentSystemServices() {
		return this;
	}

	/** Getter for transient placeholder field - returns entity itself for component binding.
	 * @return this entity (for CFormBuilder binding to CComponentWebServiceDiscovery) */
	public CDashboardProject_Bab getPlaceHolder_createComponentWebServiceDiscovery() {
		return this;
	}

	@Override
	public Class<?> getServiceClass() { return CDashboardProject_BabService.class; }

	/** Initialize intrinsic defaults (RULE 3). */
	private final void initializeDefaults() {
		setDashboardWidget("bab_gateway_monitor");
		dashboardType = "monitoring";
		// MANDATORY: Call service initialization at end (RULE 3)
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	@Override
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	@Override
	public void setComments(final Set<CComment> comments) { this.comments = comments; }

	public void setDashboardType(final String dashboardType) {
		this.dashboardType = dashboardType;
		updateLastModified();
	}

	@Override
	public void setLinks(final Set<CLink> links) { this.links = links; }

	public void setPlaceHolder_createComponentDiskUsage(CDashboardProject_Bab placeHolder_createComponentDiskUsage) {
		this.placeHolder_createComponentDiskUsage = placeHolder_createComponentDiskUsage;
	}

	public void setPlaceHolder_createComponentDnsConfiguration(CDashboardProject_Bab placeHolder_createComponentDnsConfiguration) {
		this.placeHolder_createComponentDnsConfiguration = placeHolder_createComponentDnsConfiguration;
	}

	/** Setter for transient placeholder field - required by Vaadin Binder. Does nothing as field is transient and getter returns 'this'.
	 * @param value ignored (placeholder field is not used) */
	public void setPlaceHolder_createComponentInterfaceList(final CDashboardProject_Bab value) {
		// Placeholder field setter - required by Binder but field is transient
	}

	public void setPlaceHolder_createComponentRoutingTable(CDashboardProject_Bab placeHolder_createComponentRoutingTable) {
		this.placeHolder_createComponentRoutingTable = placeHolder_createComponentRoutingTable;
	}

	public void setPlaceHolder_createComponentSystemMetrics(CDashboardProject_Bab placeHolder_createComponentSystemMetrics) {
		this.placeHolder_createComponentSystemMetrics = placeHolder_createComponentSystemMetrics;
	}

	public void setPlaceHolder_createComponentSystemProcessList(CDashboardProject_Bab placeHolder_createComponentSystemProcessList) {
		this.placeHolder_createComponentSystemProcessList = placeHolder_createComponentSystemProcessList;
	}

	public void setPlaceHolder_createComponentSystemServices(CDashboardProject_Bab placeHolder_createComponentSystemServices) {
		this.placeHolder_createComponentSystemServices = placeHolder_createComponentSystemServices;
	}
}
