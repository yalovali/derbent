package tech.derbent.bab.policybase.policy.domain;

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
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
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
@Table (name = "CPolicy", uniqueConstraints = {
		@UniqueConstraint (columnNames = {
				"project_id", "name"
		})
})
@AttributeOverride (name = "id", column = @Column (name = "policy_id"))
@Profile ("bab")
public class CPolicy extends CEntityOfProject<CPolicy> implements IHasAttachments, IHasComments, IHasLinks, IEntityRegistrable {

	// Entity constants (MANDATORY)
	public static final String DEFAULT_COLOR = "#3F51B5"; // Indigo - Policy management
	public static final String DEFAULT_ICON = "vaadin:shield";
	public static final String ENTITY_TITLE_PLURAL = "BAB Policies";
	public static final String ENTITY_TITLE_SINGULAR = "BAB Policy";
	private static final Logger LOGGER = LoggerFactory.getLogger(CPolicy.class);
	public static final String VIEW_NAME = "BAB Policies View";
	@Column (name = "application_status", length = 20, nullable = false)
	@AMetaData (
			displayName = "Application Status", required = false, readOnly = true,
			description = "Status of last Calimero application (PENDING, APPLIED, FAILED)", hidden = false, maxLength = 20
	)
	private String applicationStatus = "PENDING";
	// Standard composition fields - initialized at declaration (RULE 5)
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "policy_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "File attachments for this policy", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	@Column (name = "auto_apply", nullable = false)
	@AMetaData (
			displayName = "Auto Apply", required = false, readOnly = false,
			description = "Automatically apply this policy to Calimero when changes are saved", hidden = false
	)
	private Boolean autoApply = false;
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "policy_id")
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
	private java.time.LocalDateTime lastAppliedDate;
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "policy_id")
	@AMetaData (
			displayName = "Links", required = false, readOnly = false, description = "Related links for this policy", hidden = false,
			dataProviderBean = "CLinkService", createComponentMethod = "createComponent"
	)
	private Set<CLink> links = new HashSet<>();
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
	// Policy rules relationship - core policy functionality
	@OneToMany (mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@AMetaData (
			displayName = "Policy Rules", required = false, readOnly = false, description = "Rules that define this policy's behavior",
			hidden = false, dataProviderBean = "CPolicyRuleService", createComponentMethod = "createComponent"
	)
	private Set<CPolicyRule> rules = new HashSet<>();

	/** Default constructor for JPA. */
	protected CPolicy() {
		super();
		// JPA constructors do NOT call initializeDefaults() (RULE 1)
	}

	public CPolicy(final String name, final CProject<?> project) {
		super(CPolicy.class, name, project);
		initializeDefaults(); // Business constructors MUST call this (RULE 2)
	}

	/** Generate initial empty policy JSON structure. */
	private void generateInitialPolicyJson() {
		policyJson = String.format("""
				{
				    "policyId": "%s",
				    "name": "%s",
				    "version": "%s",
				    "active": %s,
				    "priority": %d,
				    "autoApply": %s,
				    "rules": [],
				    "metadata": {
				        "created": "%s",
				        "description": "BAB Actions Dashboard Policy"
				    }
				}
				""", getId(), getName(), policyVersion, isActive, priorityLevel, autoApply, java.time.LocalDateTime.now().toString());
	}

	/** Get count of active rules in this policy.
	 * @return number of active rules */
	public long getActiveRuleCount() {
		return rules != null ? rules.stream().filter(rule -> rule.getIsActive() != null && rule.getIsActive()).count() : 0;
	}

	public String getApplicationStatus() { return applicationStatus; }

	// Interface implementations
	@Override
	public Set<CAttachment> getAttachments() { return attachments; }

	public Boolean getAutoApply() { return autoApply; }

	@Override
	public Set<CComment> getComments() { return comments; }

	// Policy specific getters and setters
	public Boolean getIsActive() { return isActive; }

	public java.time.LocalDateTime getLastAppliedDate() { return lastAppliedDate; }

	@Override
	public Set<CLink> getLinks() { return links; }

	@Override
	public Class<?> getPageServiceClass() { return Object.class; }

	public String getPolicyJson() { return policyJson; }

	public String getPolicyVersion() { return policyVersion; }

	public Integer getPriorityLevel() { return priorityLevel; }

	public Set<CPolicyRule> getRules() { return rules; }

	// IEntityRegistrable implementation
	@Override
	public Class<?> getServiceClass() { return Object.class; }

	/** Get count of total rules in this policy.
	 * @return total number of rules */
	public int getTotalRuleCount() { return rules != null ? rules.size() : 0; }

	/** Initialize intrinsic defaults (RULE 3). */
	private final void initializeDefaults() {
		// Initialize nullable=false fields with defaults (already done in field declarations)
		// Policy specific defaults
		if (policyVersion == null || policyVersion.isEmpty()) {
			policyVersion = "1.0";
		}
		if (autoApply == null) {
			autoApply = false;
		}
		if (priorityLevel == null) {
			priorityLevel = 50;
		}
		if (applicationStatus == null || applicationStatus.isEmpty()) {
			applicationStatus = "PENDING";
		}
		// Generate initial policy JSON structure
		if (policyJson == null || policyJson.isEmpty()) {
			generateInitialPolicyJson();
		}
		// MANDATORY: Call service initialization at end (RULE 3)
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	public boolean isActive() { return isActive != null && isActive; }

	/** Check if policy is ready for application to Calimero.
	 * @return true if policy has active rules and is valid */
	public boolean isReadyForApplication() { return isActive != null && isActive && getActiveRuleCount() > 0; }

	/** Check if policy was successfully applied to Calimero.
	 * @return true if last application was successful */
	public boolean isSuccessfullyApplied() { return "APPLIED".equals(applicationStatus) && lastAppliedDate != null; }

	/** Mark policy application as failed. */
	public void markAsApplicationFailed() {
		applicationStatus = "FAILED";
		updateLastModified();
	}

	/** Mark policy as successfully applied to Calimero. */
	public void markAsApplied() {
		applicationStatus = "APPLIED";
		lastAppliedDate = java.time.LocalDateTime.now();
		updateLastModified();
	}

	public void setApplicationStatus(String applicationStatus) {
		this.applicationStatus = applicationStatus;
		updateLastModified();
	}

	@Override
	public void setAttachments(Set<CAttachment> attachments) { this.attachments = attachments; }

	public void setAutoApply(Boolean autoApply) {
		this.autoApply = autoApply;
		updateLastModified();
	}

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

	public void setRules(Set<CPolicyRule> rules) {
		this.rules = rules;
		updateLastModified();
	}
}
