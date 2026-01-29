package tech.derbent.bab.dashboard.domain;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import tech.derbent.api.agileparentrelation.domain.CAgileParentRelation;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.dashboard.domain.CDashboardProject;
import tech.derbent.api.interfaces.IHasAgileParentRelation;
import tech.derbent.api.projects.domain.CProject;
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
		@jakarta.persistence.UniqueConstraint (columnNames = {
				"project_id", "name"
		})
})
@AttributeOverride (name = "id", column = @Column (name = "dashboard_project_id"))
public class CDashboardProject_Bab extends CDashboardProject<CDashboardProject_Bab>
		implements IHasAttachments, IHasComments, IHasLinks, IHasAgileParentRelation {

	// Entity constants (MANDATORY)
	public static final String DEFAULT_COLOR = "#009688"; // Teal - Dashboard/Monitoring
	public static final String DEFAULT_ICON = "vaadin:dashboard";
	public static final String ENTITY_TITLE_PLURAL = "BAB Dashboard Projects";
	public static final String ENTITY_TITLE_SINGULAR = "BAB Dashboard Project";
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CDashboardProject_Bab.class);
	public static final String VIEW_NAME = "BAB Dashboard Projects View";
	// Agile Parent Relation - REQUIRED for project items
	@OneToOne (fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn (name = "agile_parent_relation_id", nullable = false)
	@NotNull (message = "Agile parent relation is required for agile hierarchy")
	@AMetaData (
			displayName = "Agile Parent Relation", required = true, readOnly = true,
			description = "Agile hierarchy tracking for this dashboard project", hidden = true
	)
	private CAgileParentRelation agileParentRelation;
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
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();
	@Column (name = "dashboard_type", length = 50)
	@AMetaData (
			displayName = "Dashboard Type", required = false, readOnly = false,
			description = "Type of BAB dashboard (monitoring, control, reporting)", hidden = false, maxLength = 50
	)
	private String dashboardType = "monitoring";
	// BAB-specific minimal fields
	@Column (name = "is_active", nullable = false)
	@AMetaData (
			displayName = "Active", required = true, readOnly = false, description = "Whether this dashboard project is currently active",
			hidden = false
	)
	private Boolean isActive = true;
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "dashboard_project_id")
	@AMetaData (
			displayName = "Links", required = false, readOnly = false, description = "Related links for this dashboard project", hidden = false,
			dataProviderBean = "CLinkService", createComponentMethod = "createComponent"
	)
	private Set<CLink> links = new HashSet<>();

	/** Default constructor for JPA. */
	protected CDashboardProject_Bab() {
		// JPA constructors do NOT call initializeDefaults() (RULE 1)
	}

	public CDashboardProject_Bab(final String name, final CProject<?> project) {
		super(CDashboardProject_Bab.class, name, project);
		initializeDefaults(); // Business constructors MUST call this (RULE 2)
	}

	@Override
	public CAgileParentRelation getAgileParentRelation() { return agileParentRelation; }

	// Interface implementations
	@Override
	public Set<CAttachment> getAttachments() { return attachments; }

	@Override
	public Set<CComment> getComments() { return comments; }

	public String getDashboardType() { return dashboardType; }

	// Getters and setters
	public Boolean getIsActive() { return isActive; }

	@Override
	public Set<CLink> getLinks() { return links; }

	/** Initialize intrinsic defaults (RULE 3). */
	private final void initializeDefaults() {
		// Initialize nullable=false fields with defaults (RULE 6)
		isActive = true;
		// Initialize dashboard-specific defaults
		setDashboardWidget("bab_gateway_monitor");
		dashboardType = "monitoring";
		// Initialize composition objects (RULE 6)
		agileParentRelation = new tech.derbent.api.agileparentrelation.domain.CAgileParentRelation(this);
		agileParentRelation.setOwnerItem(this);
		// MANDATORY: Call service initialization at end (RULE 3)
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	public boolean isActive() { return isActive != null && isActive; }

	@Override
	public void setAgileParentRelation(CAgileParentRelation agileParentRelation) { this.agileParentRelation = agileParentRelation; }

	@Override
	public void setAttachments(Set<CAttachment> attachments) { this.attachments = attachments; }

	@Override
	public void setComments(Set<CComment> comments) { this.comments = comments; }

	public void setDashboardType(String dashboardType) {
		this.dashboardType = dashboardType;
		updateLastModified();
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
		updateLastModified();
	}

	@Override
	public void setLinks(Set<CLink> links) { this.links = links; }
}
