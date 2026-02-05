package tech.derbent.bab.policybase.policy.domain;

import java.time.LocalDateTime;
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
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.bab.dashboard.dashboardpolicy.domain.CBabPolicyRule;
import tech.derbent.bab.policybase.policy.service.CPageServiceBabPolicy;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;
import tech.derbent.plm.links.domain.CLink;
import tech.derbent.plm.links.domain.IHasLinks;

/** CPolicy - BAB Actions Dashboard policy entity for network node rule management. Layer: Domain (MVC) Active when: 'bab' profile is active Following
 * Derbent pattern: Concrete entity with @Entity annotation. Represents a policy containing multiple rules for virtual network node communication.
 * Used in BAB Actions Dashboard for managing IoT gateway policies that are exported as JSON to Calimero gateway system. A policy groups related
 * communication rules and can be activated/deactivated as a unit. */
@Entity
@Table (name = "cbab_policy", uniqueConstraints = {
		@UniqueConstraint (columnNames = {
				"project_id", "name"
		})
})
@AttributeOverride (name = "id", column = @Column (name = "bab_policy_id"))
@Profile ("bab")
public class CBabPolicy extends CEntityOfProject<CBabPolicy> implements IHasAttachments, IHasComments, IHasLinks, IEntityRegistrable {

	// Entity constants (MANDATORY)
	public static final String DEFAULT_COLOR = "#3F51B5"; // Indigo - Policy management
	public static final String DEFAULT_ICON = "vaadin:shield";
	public static final String ENTITY_TITLE_PLURAL = "BAB Policies";
	public static final String ENTITY_TITLE_SINGULAR = "BAB Policy";
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicy.class);
	public static final String VIEW_NAME = "BAB Policies View";
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "bab_policy_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "Attachments for this policy", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	// Policy rules relationship - core policy functionality
	@OneToMany (mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@AMetaData (
			displayName = "Policy Rules", required = false, readOnly = false, description = "Rules that define this policy's behavior",
			hidden = false, dataProviderBean = "CBabPolicyRuleService", createComponentMethod = "createComponent"
	)
	private Set<CBabPolicyRule> babPolicyRules = new HashSet<>();
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "bab_policy_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this policy", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponentComment"
	)
	private Set<CComment> comments = new HashSet<>();
	// Policy operational state - initialized at declaration (RULE 6)
	@Column (name = "is_active", nullable = false)
	@AMetaData (
			displayName = "Active", required = true, readOnly = false, description = "Whether this policy is currently active and enforced",
			hidden = false
	)
	private Boolean isActive = true;
	@Column (name = "last_applied_date")
	@AMetaData (
			displayName = "Last Applied Date", required = false, readOnly = true,
			description = "Timestamp when policy was last applied to Calimero gateway", hidden = false
	)
	private LocalDateTime lastAppliedDate;
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "bab_policy_id")
	@AMetaData (
			displayName = "Links", required = false, readOnly = false, description = "Related links for this policy", hidden = false,
			dataProviderBean = "CLinkService", createComponentMethod = "createComponent"
	)
	private Set<CLink> links = new HashSet<>();
	@AMetaData (
			displayName = "Policy UI", required = false, readOnly = false,
			description = "Policy user interface component for managing rules and settings", hidden = false, dataProviderBean = "pageservice",
			createComponentMethod = "createComponentPolicyBab", captionVisible = false
	)
	@Transient
	private CBabPolicy placeHolder_createComponentPolicyBab = null;
	// Calimero integration - JSON export configuration
	@Column (name = "policy_json", columnDefinition = "TEXT")
	@AMetaData (
			displayName = "Policy JSON Configuration", required = false, readOnly = true,
			description = "Generated JSON configuration for Calimero gateway export", hidden = false
	)
	private String policyJson;
	@Column (name = "policy_version", length = 20, nullable = false)
	@AMetaData (
			displayName = "Policy Version", required = false, readOnly = false, description = "Policy version for change tracking (e.g., 1.0, 1.1)",
			hidden = false, maxLength = 20
	)
	private String policyVersion = "1.0";
	@Column (name = "priority_level", nullable = false)
	@AMetaData (
			displayName = "Priority Level", required = false, readOnly = false,
			description = "Policy priority for conflict resolution (0-100, higher = more important)", hidden = false
	)
	private Integer priorityLevel = 50;

	/** Default constructor for JPA. */
	protected CBabPolicy() {
		// JPA constructors do NOT call initializeDefaults() (RULE 1)
	}

	public CBabPolicy(final String name, final CProject<?> project) {
		super(CBabPolicy.class, name, project);
		initializeDefaults(); // Business constructors MUST call this (RULE 2)
	}

	/** Generate initial empty policy JSON structure. */
	private void generateInitialPolicyJson() {
		policyJson = """
				{
				    "policyId": "%s",
				    "name": "%s",
				    "version": "%s",
				    "active": %s,
				    "priority": %d,
				    "rules": [],
				    "metadata": {
				        "created": "%s",
				        "description": "BAB Actions Dashboard Policy"
				    }
				}
				""".formatted(getId(), getName(), policyVersion, isActive, priorityLevel, java.time.LocalDateTime.now().toString());
	}

	@Override
	public Set<CAttachment> getAttachments() { return attachments; }

	@Override
	public Set<CComment> getComments() { return comments; }

	// Policy specific getters and setters
	public Boolean getIsActive() { return isActive; }

	public LocalDateTime getLastAppliedDate() { return lastAppliedDate; }

	@Override
	public Set<CLink> getLinks() { return links; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceBabPolicy.class; }

	public CBabPolicy getPlaceHolder_createComponentPolicyBab() { return placeHolder_createComponentPolicyBab; }

	public String getPolicyJson() { return policyJson; }

	public String getPolicyVersion() { return policyVersion; }

	public Integer getPriorityLevel() { return priorityLevel; }

	public Set<CBabPolicyRule> getRules() { return babPolicyRules; }

	// IEntityRegistrable implementation
	@Override
	public Class<?> getServiceClass() { return tech.derbent.bab.policybase.policy.service.CBabPolicyService.class; }

	/** Get count of total rules in this policy.
	 * @return total number of rules */
	public int getTotalRuleCount() { return babPolicyRules != null ? babPolicyRules.size() : 0; }

	/** Initialize intrinsic defaults (RULE 3). */
	private final void initializeDefaults() {
		// Initialize nullable=false fields with defaults (already done in field declarations)
		// Policy specific defaults
		if (policyVersion == null || policyVersion.isEmpty()) {
			policyVersion = "1.0";
		}
		if (priorityLevel == null) {
			priorityLevel = 50;
		}
		// Generate initial policy JSON structure
		if (policyJson == null || policyJson.isEmpty()) {
			generateInitialPolicyJson();
		}
		// MANDATORY: Call service initialization at end (RULE 3)
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	public boolean isActive() { return isActive != null && isActive; }

	/** Mark policy as successfully applied to Calimero. */
	public void markAsApplied() {
		lastAppliedDate = java.time.LocalDateTime.now();
		updateLastModified();
	}

	@Override
	public void setAttachments(Set<CAttachment> attachments) { this.attachments = attachments; }

	@Override
	public void setComments(Set<CComment> comments) { this.comments = comments; }

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
		updateLastModified();
	}

	public void setLastAppliedDate(java.time.LocalDateTime lastAppliedDate) {
		this.lastAppliedDate = lastAppliedDate;
		updateLastModified();
	}

	@Override
	public void setLinks(Set<CLink> links) { this.links = links; }

	public void setPlaceHolder_createComponentPolicyBab(CBabPolicy placeHolder_createComponentPolicyBab) {
		this.placeHolder_createComponentPolicyBab = placeHolder_createComponentPolicyBab;
	}

	public void setPolicyJson(String policyJson) {
		this.policyJson = policyJson;
		updateLastModified();
	}

	public void setPolicyVersion(String policyVersion) {
		this.policyVersion = policyVersion;
		updateLastModified();
	}

	public void setPriorityLevel(Integer priorityLevel) {
		this.priorityLevel = priorityLevel;
		updateLastModified();
	}

	public void setRules(Set<CBabPolicyRule> rules) {
		babPolicyRules = rules;
		updateLastModified();
	}
}
