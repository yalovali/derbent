package tech.derbent.bab.dashboard.dashboardpolicy.domain;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;
import tech.derbent.plm.links.domain.CLink;
import tech.derbent.plm.links.domain.IHasLinks;

/** CDashboardActions - Main BAB Actions Dashboard entity for network node management. Layer: Domain (MVC) Active when: 'bab' profile is active
 * Following Derbent pattern: Concrete entity with @Entity annotation. Main entity for BAB Actions Dashboard system that manages: - Virtual network
 * entities (HTTP servers, vehicles, file inputs) - Policy rule configurations and management - Split-pane dashboard UI with node list and work area -
 * Calimero gateway integration for IoT policy application Contains BAB component placeholders for dashboard UI rendering. */
@Entity
@Table (name = "CDashboardPolicy", uniqueConstraints = {
		@UniqueConstraint (columnNames = {
				"project_id", "name"
		})
})
@AttributeOverride (name = "id", column = @Column (name = "dashboard_policy_id"))
@Profile ("bab")
public class CDashboardPolicy extends CEntityOfProject<CDashboardPolicy> implements IHasAttachments, IHasComments, IHasLinks, IEntityRegistrable {

	// Entity constants (MANDATORY)
	public static final String DEFAULT_COLOR = "#E91E63"; // Pink - Actions/Control
	public static final String DEFAULT_ICON = "vaadin:dashboard";
	public static final String ENTITY_TITLE_PLURAL = "BAB Actions Dashboards";
	public static final String ENTITY_TITLE_SINGULAR = "BAB Actions Dashboard";
	private static final Logger LOGGER = LoggerFactory.getLogger(CDashboardPolicy.class);
	public static final String VIEW_NAME = "BAB Actions Dashboard View";
	@Column (name = "active_nodes", nullable = false)
	@AMetaData (
			displayName = "Active Nodes", required = false, readOnly = true, description = "Number of currently active network nodes", hidden = false
	)
	private Integer activeNodes = 0;
	// Active policy relationship
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "active_policy_id")
	@Column(name = "active_policy_name", length = 255)
	@AMetaData (
			displayName = "Active Policy", required = false, readOnly = false, description = "Currently active policy for this dashboard",
			hidden = false, dataProviderBean = "CDashboardPolicyService", dataProviderMethod = "listByProject", maxLength = 255
	)
	private String activePolicyName;
	@Column (name = "active_rules", nullable = false)
	@AMetaData (
			displayName = "Active Rules", required = false, readOnly = true, description = "Number of currently active policy rules", hidden = false
	)
	private Integer activeRules = 0;
	// Standard composition fields - initialized at declaration (RULE 5)
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "dashboard_actions_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "File attachments for this dashboard", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	@Column (name = "auto_apply_policy", nullable = false)
	@AMetaData (
			displayName = "Auto Apply Policy", required = false, readOnly = false,
			description = "Automatically apply policy changes to Calimero gateway", hidden = false
	)
	private Boolean autoApplyPolicy = false;
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "dashboard_actions_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this dashboard", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponentComment"
	)
	private Set<CComment> comments = new HashSet<>();
	// Dashboard operational configuration - initialized at declaration (RULE 6)
	@Column (name = "dashboard_layout", length = 20, nullable = false)
	@AMetaData (
			displayName = "Dashboard Layout", required = false, readOnly = false, description = "Dashboard UI layout type (SPLIT_PANE, TABBED, GRID)",
			hidden = false, maxLength = 20
	)
	private String dashboardLayout = "SPLIT_PANE";
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "dashboard_actions_id")
	@AMetaData (
			displayName = "Links", required = false, readOnly = false, description = "Related links for this dashboard", hidden = false,
			dataProviderBean = "CLinkService", createComponentMethod = "createComponent"
	)
	private Set<CLink> links = new HashSet<>();
	@Column (name = "node_list_width", nullable = false)
	@AMetaData (
			displayName = "Node List Width (%)", required = false, readOnly = false,
			description = "Width percentage of node list panel in split layout", hidden = false
	)
	private Integer nodeListWidth = 30;
	@AMetaData (
			displayName = "Dashboard Split Layout", required = false, readOnly = false,
			description = "Main split layout container for node list and work area", hidden = false, dataProviderBean = "pageservice",
			createComponentMethod = "createComponentDashboardSplitLayout", captionVisible = false
	)
	@Transient
	private CDashboardPolicy placeHolder_createComponentDashboardSplitLayout = null;
	// BAB Component Placeholders (MANDATORY pattern: entity-typed, @Transient, = null, NO final)
	@AMetaData (
			displayName = "Node List Component", required = false, readOnly = false, description = "Left panel component for network node management",
			hidden = false, dataProviderBean = "pageservice", createComponentMethod = "createComponentNodeList", captionVisible = false
	)
	@Transient
	private CDashboardPolicy placeHolder_createComponentNodeList = null;
	@AMetaData (
			displayName = "Policy Status Monitor", required = false, readOnly = false,
			description = "Component for monitoring policy application status", hidden = false, dataProviderBean = "pageservice",
			createComponentMethod = "createComponentPolicyStatusMonitor", captionVisible = false
	)
	@Transient
	private CDashboardPolicy placeHolder_createComponentPolicyStatusMonitor = null;
	@AMetaData (
			displayName = "Work Area Component", required = false, readOnly = false, description = "Right panel component with tabbed work area",
			hidden = false, dataProviderBean = "pageservice", createComponentMethod = "createComponentWorkArea", captionVisible = false
	)
	@Transient
	private CDashboardPolicy placeHolder_createComponentWorkArea = null;
	@Column (name = "refresh_interval_seconds", nullable = false)
	@AMetaData (
			displayName = "Refresh Interval (seconds)", required = false, readOnly = false,
			description = "Automatic refresh interval for dashboard data", hidden = false
	)
	private Integer refreshIntervalSeconds = 60;
	@Column (name = "show_inactive_nodes", nullable = false)
	@AMetaData (
			displayName = "Show Inactive Nodes", required = false, readOnly = false, description = "Display inactive nodes in the node list",
			hidden = false
	)
	private Boolean showInactiveNodes = true;
	// Dashboard statistics and status
	@Column (name = "total_nodes", nullable = false)
	@AMetaData (
			displayName = "Total Nodes", required = false, readOnly = true, description = "Total number of network nodes in this dashboard",
			hidden = false
	)
	private Integer totalNodes = 0;
	@Column (name = "total_rules", nullable = false)
	@AMetaData (
			displayName = "Total Rules", required = false, readOnly = true, description = "Total number of policy rules configured", hidden = false
	)
	private Integer totalRules = 0;

	/** Default constructor for JPA. */
	protected CDashboardPolicy() {
		super();
		// JPA constructors do NOT call initializeDefaults() (RULE 1)
	}

	public CDashboardPolicy(final String name, final CProject<?> project) {
		super(CDashboardPolicy.class, name, project);
		initializeDefaults(); // Business constructors MUST call this (RULE 2)
	}

	public Integer getActiveNodes() { return activeNodes; }

	public String getActivePolicyName() { return activePolicyName; }
	
	/**
	 * Get the active policy entity by name lookup.
	 * @return CDashboardPolicy entity or null if not found
	 */
	public CDashboardPolicy getActivePolicy() {
		if (activePolicyName == null || activePolicyName.trim().isEmpty()) {
			return null;
		}
		try {
			final var policyService = CSpringContext.getBean(tech.derbent.bab.dashboard.dashboardpolicy.service.CDashboardPolicyService.class);
			final java.util.List<CDashboardPolicy> policies = policyService.listByProject(getProject());
			return policies.stream()
				.filter(p -> activePolicyName.trim().equals(p.getName()))
				.findFirst()
				.orElse(null);
		} catch (Exception e) {
			LOGGER.warn("Error looking up active policy by name '{}': {}", activePolicyName, e.getMessage());
			return null;
		}
	}

	public Integer getActiveRules() { return activeRules; }

	// Interface implementations
	@Override
	public Set<CAttachment> getAttachments() { return attachments; }

	public Boolean getAutoApplyPolicy() { return autoApplyPolicy; }
	// BAB placeholder getters - return entity itself for component binding (BAB pattern)

	@Override
	public Set<CComment> getComments() { return comments; }

	// Dashboard configuration getters and setters
	public String getDashboardLayout() { return dashboardLayout; }

	@Override
	public Set<CLink> getLinks() { return links; }

	public Integer getNodeListWidth() { return nodeListWidth; }
	// BAB placeholder setters - required by Vaadin Binder but do nothing (BAB pattern)

	/** Get the split layout configuration as a ratio.
	 * @return node list width as decimal ratio (e.g., 0.3 for 30%) */
	public double getNodeListWidthRatio() {
		return nodeListWidth != null ? nodeListWidth / 100.0 : 0.3;
	}

	@Override
	public Class<?> getPageServiceClass() { return Object.class; }

	/** Getter for transient placeholder field - returns entity itself for component binding.
	 * @return this entity (for CFormBuilder binding to CComponentDashboardSplitLayout) */
	public CDashboardPolicy getPlaceHolder_createComponentDashboardSplitLayout() {
		return this;
	}

	/** Getter for transient placeholder field - returns entity itself for component binding.
	 * @return this entity (for CFormBuilder binding to CComponentNodeList) */
	public CDashboardPolicy getPlaceHolder_createComponentNodeList() {
		return this;
	}

	/** Getter for transient placeholder field - returns entity itself for component binding.
	 * @return this entity (for CFormBuilder binding to CComponentPolicyStatusMonitor) */
	public CDashboardPolicy getPlaceHolder_createComponentPolicyStatusMonitor() {
		return this;
	}

	/** Getter for transient placeholder field - returns entity itself for component binding.
	 * @return this entity (for CFormBuilder binding to CComponentWorkArea) */
	public CDashboardPolicy getPlaceHolder_createComponentWorkArea() {
		return this;
	}

	public Integer getRefreshIntervalSeconds() { return refreshIntervalSeconds; }

	// IEntityRegistrable implementation
	@Override
	public Class<?> getServiceClass() { return Object.class; }

	public Boolean getShowInactiveNodes() { return showInactiveNodes; }

	// Statistics getters and setters (read-only from UI perspective)
	public Integer getTotalNodes() { return totalNodes; }

	public Integer getTotalRules() { return totalRules; }

	/** Check if dashboard has an active policy configured.
	 * @return true if active policy is set and active */
	public boolean hasActivePolicy() {
		return activePolicy != null && activePolicy.isActive();
	}

	/** Initialize intrinsic defaults (RULE 3). */
	private final void initializeDefaults() {
		// Initialize nullable=false fields with defaults (already done in field declarations)
		// Dashboard specific defaults
		if (dashboardLayout == null || dashboardLayout.isEmpty()) {
			dashboardLayout = "SPLIT_PANE";
		}
		if (nodeListWidth == null) {
			nodeListWidth = 30;
		}
		if (refreshIntervalSeconds == null) {
			refreshIntervalSeconds = 60;
		}
		// Initialize statistics
		if (totalNodes == null) {
			totalNodes = 0;
		}
		if (activeNodes == null) {
			activeNodes = 0;
		}
		if (totalRules == null) {
			totalRules = 0;
		}
		if (activeRules == null) {
			activeRules = 0;
		}
		// MANDATORY: Call service initialization at end (RULE 3)
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	/** Check if dashboard should automatically refresh.
	 * @return true if refresh interval is positive */
	public boolean isAutoRefreshEnabled() {
		return refreshIntervalSeconds != null && refreshIntervalSeconds > 0;
	}

	public void setActiveNodes(Integer activeNodes) {
		this.activeNodes = activeNodes;
		updateLastModified();
	}

	public void setActivePolicyName(String activePolicyName) { 
		this.activePolicyName = activePolicyName; 
		updateLastModified();
	}
	
	/**
	 * Set active policy by entity (sets name).
	 * @param activePolicy the policy entity
	 */
	public void setActivePolicy(CDashboardPolicy activePolicy) {
		if (activePolicy != null) {
			this.activePolicyName = activePolicy.getName();
		} else {
			this.activePolicyName = null;
		}
		updateLastModified();
	}

	public void setActiveRules(Integer activeRules) {
		this.activeRules = activeRules;
		updateLastModified();
	}

	@Override
	public void setAttachments(Set<CAttachment> attachments) { this.attachments = attachments; }

	public void setAutoApplyPolicy(Boolean autoApplyPolicy) {
		this.autoApplyPolicy = autoApplyPolicy;
		updateLastModified();
	}

	@Override
	public void setComments(Set<CComment> comments) { this.comments = comments; }

	public void setDashboardLayout(String dashboardLayout) {
		this.dashboardLayout = dashboardLayout;
		updateLastModified();
	}

	@Override
	public void setLinks(Set<CLink> links) { this.links = links; }

	public void setNodeListWidth(Integer nodeListWidth) {
		this.nodeListWidth = nodeListWidth;
		updateLastModified();
	}

	/** Setter for transient placeholder field - required by Vaadin Binder. */
	public void setPlaceHolder_createComponentDashboardSplitLayout(CDashboardPolicy value) {
		// Placeholder field setter - required by Binder but field is transient
	}

	/** Setter for transient placeholder field - required by Vaadin Binder. */
	public void setPlaceHolder_createComponentNodeList(CDashboardPolicy value) {
		// Placeholder field setter - required by Binder but field is transient
	}

	/** Setter for transient placeholder field - required by Vaadin Binder. */
	public void setPlaceHolder_createComponentPolicyStatusMonitor(CDashboardPolicy value) {
		// Placeholder field setter - required by Binder but field is transient
	}

	/** Setter for transient placeholder field - required by Vaadin Binder. */
	public void setPlaceHolder_createComponentWorkArea(CDashboardPolicy value) {
		// Placeholder field setter - required by Binder but field is transient
	}

	public void setRefreshIntervalSeconds(Integer refreshIntervalSeconds) {
		this.refreshIntervalSeconds = refreshIntervalSeconds;
		updateLastModified();
	}

	public void setShowInactiveNodes(Boolean showInactiveNodes) {
		this.showInactiveNodes = showInactiveNodes;
		updateLastModified();
	}

	public void setTotalNodes(Integer totalNodes) {
		this.totalNodes = totalNodes;
		updateLastModified();
	}

	public void setTotalRules(Integer totalRules) {
		this.totalRules = totalRules;
		updateLastModified();
	}

	/** Update dashboard statistics based on current network nodes and policies. Should be called when nodes or rules are added/modified/removed. */
	public void updateStatistics() {
		// This will be implemented by the service layer to query actual counts
		// For now, we'll leave these as placeholders
		updateLastModified();
	}
}
