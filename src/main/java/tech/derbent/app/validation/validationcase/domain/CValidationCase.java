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
import tech.derbent.api.interfaces.CCloneOptions;
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
public class CValidationCase extends CProjectItem<CValidationCase>
		implements IHasStatusAndWorkflow<CValidationCase>, IHasAttachments, IHasComments {

	public static final String DEFAULT_COLOR = "#4169E1"; // RoyalBlue - testing and quality
	public static final String DEFAULT_ICON = "vaadin:clipboard-check";
	public static final String ENTITY_TITLE_PLURAL = "Validation Cases";
	public static final String ENTITY_TITLE_SINGULAR = "Validation Case";
	private static final Logger LOGGER = LoggerFactory.getLogger(CValidationCase.class);
	public static final String VIEW_NAME = "Validation Cases View";

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
	@AMetaData (
			displayName = "Priority", required = false, readOnly = false,
			description = "Validation case priority level", hidden = false
	)
	private CValidationPriority priority = CValidationPriority.MEDIUM;

	@Enumerated (EnumType.STRING)
	@Column (name = "severity", nullable = true, length = 20)
	@AMetaData (
			displayName = "Severity", required = false, readOnly = false,
			description = "Impact level if validation fails", hidden = false
	)
	private CValidationSeverity severity = CValidationSeverity.NORMAL;

	@Column (name = "automated", nullable = false)
	@AMetaData (
			displayName = "Automated", required = false, readOnly = false, defaultValue = "false",
			description = "Whether validation is automated (e.g., Playwright)", hidden = false
	)
	private Boolean automated = false;

	@Column (name = "automated_test_path", nullable = true, length = 500)
	@Size (max = 500)
	@AMetaData (
			displayName = "Automated Validation Path", required = false, readOnly = false,
			description = "Path to automated validation file/method", hidden = false, maxLength = 500
	)
	private String automatedTestPath;

	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "validationsuite_id", nullable = true)
	@AMetaData (
			displayName = "Validation Suite", required = false, readOnly = false,
			description = "Parent validation suite (group of related validation cases)", hidden = false,
			dataProviderBean = "CValidationSuiteService"
	)
	private CValidationSuite validationSuite;

	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "validationCase")
	@AMetaData (
			displayName = "Validation Steps", required = false, readOnly = false,
			description = "Ordered validation steps", hidden = false,
			dataProviderBean = "CValidationStepService", createComponentMethod = "createComponentListValidationSteps"
	)
	private Set<CValidationStep> validationSteps = new HashSet<>();

	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "validationcase_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "Validation case attachments",
			hidden = false, dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();

	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "validationcase_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Validation case comments",
			hidden = false, dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();

	/** Default constructor for JPA. */
	public CValidationCase() {
		super();
		initializeDefaults();
	}

	public CValidationCase(final String name, final CProject project) {
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

	@Override
	public Set<CComment> getComments() {
		if (comments == null) {
			comments = new HashSet<>();
		}
		return comments;
	}

	@Override
	public CTypeEntity<?> getEntityType() { return entityType; }

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
	public void setAttachments(final Set<CAttachment> attachments) {
		this.attachments = attachments;
	}

	@Override
	public void setComments(final Set<CComment> comments) {
		this.comments = comments;
	}

	@Override
	public void setEntityType(CTypeEntity<?> typeEntity) {
		Check.notNull(typeEntity, "Type entity must not be null");
		Check.instanceOf(typeEntity, CValidationCaseType.class, "Type entity must be an instance of CValidationCaseType");
		Check.notNull(getProject(), "Project must be set before assigning validation case type");
		Check.notNull(getProject().getCompany(), "Project company must be set before assigning validation case type");
		Check.notNull(typeEntity.getCompany(), "Type entity company must be set before assigning validation case type");
		Check.isTrue(typeEntity.getCompany().getId().equals(getProject().getCompany().getId()),
				"Type entity company id " + typeEntity.getCompany().getId() + " does not match validation case project company id "
						+ getProject().getCompany().getId());
		entityType = (CValidationCaseType) typeEntity;
		updateLastModified();
	}

	public String getPreconditions() { return preconditions; }

	public void setPreconditions(final String preconditions) {
		this.preconditions = preconditions;
		updateLastModified();
	}

	public CValidationPriority getPriority() { return priority; }

	public void setPriority(final CValidationPriority priority) {
		this.priority = priority;
		updateLastModified();
	}

	public CValidationSeverity getSeverity() { return severity; }

	public void setSeverity(final CValidationSeverity severity) {
		this.severity = severity;
		updateLastModified();
	}

	public Boolean getAutomated() { return automated; }

	public void setAutomated(final Boolean automated) {
		this.automated = automated;
		updateLastModified();
	}

	public String getAutomatedTestPath() { return automatedTestPath; }

	public void setAutomatedTestPath(final String automatedTestPath) {
		this.automatedTestPath = automatedTestPath;
		updateLastModified();
	}

	public CValidationSuite getValidationSuite() { return validationSuite; }

	public void setValidationSuite(final CValidationSuite validationSuite) {
		this.validationSuite = validationSuite;
		updateLastModified();
	}

	public Set<CValidationStep> getValidationSteps() {
		if (validationSteps == null) {
			validationSteps = new HashSet<>();
		}
		return validationSteps;
	}

	public void setValidationSteps(final Set<CValidationStep> validationSteps) {
		this.validationSteps = validationSteps;
		updateLastModified();
	}

	/**
	 * Creates a clone of this validation case with the specified options.
	 * This implementation follows the recursive cloning pattern:
	 * 1. Calls parent's createClone() to handle inherited fields (CProjectItem)
	 * 2. Clones validation case-specific fields based on options
	 * 3. Recursively clones collections (comments, attachments, validation steps) if requested
	 * 
	 * Cloning behavior:
	 * - Basic fields (strings, numbers, enums) are always cloned
	 * - Workflow field is cloned only if options.isCloneWorkflow()
	 * - Comments collection is recursively cloned if options.includesComments()
	 * - Attachments collection is recursively cloned if options.includesAttachments()
	 * - Validation steps collection is always cloned (validation case-specific children)
	 * 
	 * @param options the cloning options determining what to clone
	 * @return a new instance of the validation case with cloned data
	 * @throws CloneNotSupportedException if cloning fails
	 */
	@Override
	public CValidationCase createClone(final CCloneOptions options) throws Exception {
		// Get parent's clone (CProjectItem -> CEntityOfProject -> CEntityNamed -> CEntityDB)
		final CValidationCase clone = super.createClone(options);

		// Clone entity type (validation case type)
		clone.entityType = this.entityType;
		
		// Clone basic fields
		clone.preconditions = this.preconditions;
		clone.priority = this.priority;
		clone.severity = this.severity;
		clone.automated = this.automated;
		clone.automatedTestPath = this.automatedTestPath;
		clone.validationSuite = this.validationSuite;
		
		// Clone workflow if requested
		if (options.isCloneWorkflow() && this.getWorkflow() != null) {
			// Workflow is obtained via entityType.getWorkflow() - already cloned via entityType
		}
		
		// Clone validation steps (validation case-specific children)
		if (this.validationSteps != null && !this.validationSteps.isEmpty()) {
			clone.validationSteps = new HashSet<>();
			for (final CValidationStep validationStep : this.validationSteps) {
				try {
					final CValidationStep validationStepClone = validationStep.createClone(options);
					validationStepClone.setValidationCase(clone);
					clone.validationSteps.add(validationStepClone);
				} catch (final Exception e) {
					LOGGER.warn("Could not clone validation step: {}", e.getMessage());
				}
			}
		}
		
		// Clone comments if requested
		if (options.includesComments() && this.comments != null && !this.comments.isEmpty()) {
			clone.comments = new HashSet<>();
			for (final CComment comment : this.comments) {
				try {
					final CComment commentClone = comment.createClone(options);
					clone.comments.add(commentClone);
				} catch (final Exception e) {
					LOGGER.warn("Could not clone comment: {}", e.getMessage());
				}
			}
		}
		
		// Clone attachments if requested
		if (options.includesAttachments() && this.attachments != null && !this.attachments.isEmpty()) {
			clone.attachments = new HashSet<>();
			for (final CAttachment attachment : this.attachments) {
				try {
					final CAttachment attachmentClone = attachment.createClone(options);
					clone.attachments.add(attachmentClone);
				} catch (final Exception e) {
					LOGGER.warn("Could not clone attachment: {}", e.getMessage());
				}
			}
		}
		
		LOGGER.debug("Successfully cloned validation case '{}' with options: {}", this.getName(), options);
		return clone;
	}
}
