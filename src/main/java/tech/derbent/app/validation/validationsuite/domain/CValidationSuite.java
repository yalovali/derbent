package tech.derbent.app.validation.validationsuite.domain;

import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.app.attachments.domain.CAttachment;
import tech.derbent.app.attachments.domain.IHasAttachments;
import tech.derbent.app.comments.domain.CComment;
import tech.derbent.app.comments.domain.IHasComments;
import tech.derbent.app.validation.validationcase.domain.CValidationCase;

/** CValidationSuite - Entity representing a validation suite grouping multiple validation cases. A validation suite describes a business workflow or
 * user journey that requires multiple validation cases. */
@Entity
@Table (name = "cvalidationsuite")
@AttributeOverride (name = "id", column = @Column (name = "validationsuite_id"))
public class CValidationSuite extends CEntityOfProject<CValidationSuite> implements IHasAttachments, IHasComments {

	public static final String DEFAULT_COLOR = "#6495ED"; // CornflowerBlue - validation suites
	public static final String DEFAULT_ICON = "vaadin:folder-open";
	public static final String ENTITY_TITLE_PLURAL = "Validation Suites";
	public static final String ENTITY_TITLE_SINGULAR = "Validation Suite";
	public static final String VIEW_NAME = "Validation Suites View";
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "validationsuite_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "Suite attachments", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "validationsuite_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Suite comments", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();
	@Column (nullable = true, length = 5000)
	@Size (max = 5000)
	@AMetaData (
			displayName = "Description", required = false, readOnly = false, description = "Detailed description of the validation suite",
			hidden = false, maxLength = 5000
	)
	private String description;
	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Objective", required = false, readOnly = false, description = "Validation objective and goals", hidden = false,
			maxLength = 2000
	)
	private String objective;
	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Prerequisites", required = false, readOnly = false, description = "Prerequisites needed before executing the suite",
			hidden = false, maxLength = 2000
	)
	private String prerequisites;
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "validationsuite_id")
	@AMetaData (
			displayName = "Validation Cases", required = false, readOnly = false, description = "Validation cases in this suite", hidden = false,
			dataProviderBean = "CValidationCaseService", createComponentMethod = "createComponentListValidationCases"
	)
	private Set<CValidationCase> validationCases = new HashSet<>();

	/** Default constructor for JPA. */
	public CValidationSuite() {
		super(CValidationSuite.class, "New Validation Suite", null);
	}

	public CValidationSuite(final String name, final CProject<?> project) {
		super(CValidationSuite.class, name, project);
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
	public String getDescription() { return description; }

	public String getObjective() { return objective; }

	public String getPrerequisites() { return prerequisites; }

	public Set<CValidationCase> getValidationCases() {
		if (validationCases == null) {
			validationCases = new HashSet<>();
		}
		return validationCases;
	}

	@Override
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	@Override
	public void setComments(final Set<CComment> comments) { this.comments = comments; }

	@Override
	public void setDescription(final String description) {
		this.description = description;
		updateLastModified();
	}

	public void setObjective(final String objective) {
		this.objective = objective;
		updateLastModified();
	}

	public void setPrerequisites(final String prerequisites) {
		this.prerequisites = prerequisites;
		updateLastModified();
	}

	public void setValidationCases(final Set<CValidationCase> validationCases) {
		this.validationCases = validationCases;
		updateLastModified();
	}
}
