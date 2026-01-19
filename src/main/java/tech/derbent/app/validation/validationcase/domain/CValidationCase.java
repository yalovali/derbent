package tech.derbent.app.validation.validationcase.domain;

import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.domain.CWorkflowEntity;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.app.attachments.domain.CAttachment;
import tech.derbent.app.attachments.domain.IHasAttachments;
import tech.derbent.app.comments.domain.CComment;
import tech.derbent.app.comments.domain.IHasComments;
import tech.derbent.app.validation.validationcasetype.domain.CValidationCaseType;
import tech.derbent.app.validation.validationstep.domain.CValidationStep;
import tech.derbent.app.validation.validationsuite.domain.CValidationSuite;

@Entity
@Table (name = "cvalidationcase")
@AttributeOverride (name = "id", column = @Column (name = "validationcase_id"))
public class CValidationCase extends CProjectItem<CValidationCase> implements IHasStatusAndWorkflow<CValidationCase>, IHasAttachments, IHasComments {

	public static final String DEFAULT_COLOR = "#4169E1"; // RoyalBlue - testing and quality
	public static final String DEFAULT_ICON = "vaadin:clipboard-check";
	public static final String ENTITY_TITLE_PLURAL = "Validation Cases";
	public static final String ENTITY_TITLE_SINGULAR = "Validation Case";
	private static final Logger LOGGER = LoggerFactory.getLogger(CValidationCase.class);
	public static final String VIEW_NAME = "Validation Cases View";
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "validationcase_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "Validation case attachments", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	@Column (name = "automated", nullable = false)
	@AMetaData (
			displayName = "Automated", required = false, readOnly = false, defaultValue = "false",
			description = "Whether validation is automated (e.g., Playwright)", hidden = false
	)
	private Boolean automated = false;
	@Column (name = "automated_test_path", nullable = true, length = 500)
	@Size (max = 500)
	@AMetaData (
			displayName = "Automated Validation Path", required = false, readOnly = false, description = "Path to automated validation file/method",
			hidden = false, maxLength = 500
	)
	private String automatedTestPath;
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "validationcase_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Validation case comments", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Validation Case Type", required = false, readOnly = false, description = "Type category of the validation case",
			hidden = false, dataProviderBean = "CValidationCaseTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CValidationCaseType entityType;
	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Preconditions", required = false, readOnly = false,
			description = "Prerequisites that must be met before validation execution", hidden = false, maxLength = 2000
	)
	private String preconditions;
	@Enumerated (EnumType.STRING)
	@Column (name = "priority", nullable = true, length = 20)
	@AMetaData (displayName = "Priority", required = false, readOnly = false, description = "Validation case priority level", hidden = false)
	private CValidationPriority priority = CValidationPriority.MEDIUM;
	@Enumerated (EnumType.STRING)
	@Column (name = "severity", nullable = true, length = 20)
	@AMetaData (displayName = "Severity", required = false, readOnly = false, description = "Impact level if validation fails", hidden = false)
	private CValidationSeverity severity = CValidationSeverity.NORMAL;
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "validationCase")
	@AMetaData (
			displayName = "Validation Steps", required = false, readOnly = false, description = "Ordered validation steps", hidden = false,
			dataProviderBean = "CValidationStepService", createComponentMethod = "createComponentListValidationSteps"
	)
	private Set<CValidationStep> validationSteps = new HashSet<>();
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "validationsuite_id", nullable = true)
	@AMetaData (
			displayName = "Validation Suite", required = false, readOnly = false,
			description = "Parent validation suite (group of related validation cases)", hidden = false, dataProviderBean = "CValidationSuiteService"
	)
	private CValidationSuite validationSuite;

	/** Default constructor for JPA. */
	public CValidationCase() {
		super();
		initializeDefaults();
	}

	public CValidationCase(final String name, final CProject<?> project) {
		super(CValidationCase.class, name, project);
		initializeDefaults();
	}

	@Override
	public Set<CAttachment> getAttachments() {
		if (attachments == null) {
			attachments = new HashSet<>();
		}
		return attachments;
	}

	public Boolean getAutomated() { return automated; }

	public String getAutomatedTestPath() { return automatedTestPath; }

	@Override
	public Set<CComment> getComments() {
		if (comments == null) {
			comments = new HashSet<>();
		}
		return comments;
	}

	@Override
	public CTypeEntity<?> getEntityType() { return entityType; }

	public String getPreconditions() { return preconditions; }

	public CValidationPriority getPriority() { return priority; }

	public CValidationSeverity getSeverity() { return severity; }

	public Set<CValidationStep> getValidationSteps() {
		if (validationSteps == null) {
			validationSteps = new HashSet<>();
		}
		return validationSteps;
	}

	public CValidationSuite getValidationSuite() { return validationSuite; }

	@Override
	public CWorkflowEntity getWorkflow() {
		if (entityType == null) {
			return null;
		}
		return entityType.getWorkflow();
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		if (priority == null) {
			priority = CValidationPriority.MEDIUM;
		}
		if (severity == null) {
			severity = CValidationSeverity.NORMAL;
		}
		if (automated == null) {
			automated = false;
		}
	}

	@Override
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	public void setAutomated(final Boolean automated) {
		this.automated = automated;
		updateLastModified();
	}

	public void setAutomatedTestPath(final String automatedTestPath) {
		this.automatedTestPath = automatedTestPath;
		updateLastModified();
	}

	@Override
	public void setComments(final Set<CComment> comments) { this.comments = comments; }

	@Override
	public void setEntityType(CTypeEntity<?> typeEntity) {
		Check.notNull(typeEntity, "Type entity must not be null");
		Check.instanceOf(typeEntity, CValidationCaseType.class, "Type entity must be an instance of CValidationCaseType");
		Check.notNull(getProject(), "Project must be set before assigning validation case type");
		Check.notNull(getProject().getCompany(), "Project company must be set before assigning validation case type");
		Check.notNull(typeEntity.getCompany(), "Type entity company must be set before assigning validation case type");
		Check.isTrue(typeEntity.getCompany().getId().equals(getProject().getCompany().getId()), "Type entity company id "
				+ typeEntity.getCompany().getId() + " does not match validation case project company id " + getProject().getCompany().getId());
		entityType = (CValidationCaseType) typeEntity;
		updateLastModified();
	}

	public void setPreconditions(final String preconditions) {
		this.preconditions = preconditions;
		updateLastModified();
	}

	public void setPriority(final CValidationPriority priority) {
		this.priority = priority;
		updateLastModified();
	}

	public void setSeverity(final CValidationSeverity severity) {
		this.severity = severity;
		updateLastModified();
	}

	public void setValidationSteps(final Set<CValidationStep> validationSteps) {
		this.validationSteps = validationSteps;
		updateLastModified();
	}

	public void setValidationSuite(final CValidationSuite validationSuite) {
		this.validationSuite = validationSuite;
		updateLastModified();
	}
}
