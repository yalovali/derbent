package tech.derbent.app.testcases.testscenario.domain;

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
import tech.derbent.app.testcases.testcase.domain.CTestCase;

/** CTestScenario - Entity representing a test scenario grouping multiple test cases.
 * A test scenario describes a business workflow or user journey that requires multiple test cases. */
@Entity
@Table (name = "ctestscenario")
@AttributeOverride (name = "id", column = @Column (name = "testscenario_id"))
public class CTestScenario extends CEntityOfProject<CTestScenario> implements IHasAttachments, IHasComments {

	public static final String DEFAULT_COLOR = "#6495ED"; // CornflowerBlue - test suites
	public static final String DEFAULT_ICON = "vaadin:folder-open";
	public static final String ENTITY_TITLE_PLURAL = "Test Suites";
	public static final String ENTITY_TITLE_SINGULAR = "Test Suite";
	public static final String VIEW_NAME = "Test Suites View";

	@Column (nullable = true, length = 5000)
	@Size (max = 5000)
	@AMetaData (
			displayName = "Description", required = false, readOnly = false,
			description = "Detailed description of the test scenario", hidden = false, maxLength = 5000
	)
	private String description;

	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Objective", required = false, readOnly = false,
			description = "Testing objective and goals", hidden = false, maxLength = 2000
	)
	private String objective;

	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Prerequisites", required = false, readOnly = false,
			description = "Prerequisites needed before executing scenario", hidden = false, maxLength = 2000
	)
	private String prerequisites;

	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "testscenario_id")
	@AMetaData (
			displayName = "Test Cases", required = false, readOnly = false,
			description = "Test cases in this scenario", hidden = false,
			dataProviderBean = "CTestCaseService", createComponentMethod = "createComponentListTestCases"
	)
	private Set<CTestCase> testCases = new HashSet<>();

	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "testscenario_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false,
			description = "Scenario attachments", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();

	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "testscenario_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false,
			description = "Scenario comments", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();

	/** Default constructor for JPA. */
	public CTestScenario() {
		super(CTestScenario.class, "New Test Scenario", null);
	}

	public CTestScenario(final String name, final CProject project) {
		super(CTestScenario.class, name, project);
	}

	@Override
	public Set<CAttachment> getAttachments() {
		if (attachments == null) {
			attachments = new HashSet<>();
		}
		return attachments;
	}

	@Override
	public void setAttachments(final Set<CAttachment> attachments) {
		this.attachments = attachments;
	}

	@Override
	public Set<CComment> getComments() {
		if (comments == null) {
			comments = new HashSet<>();
		}
		return comments;
	}

	@Override
	public void setComments(final Set<CComment> comments) {
		this.comments = comments;
	}

	public String getDescription() { return description; }

	public void setDescription(final String description) {
		this.description = description;
		updateLastModified();
	}

	public String getObjective() { return objective; }

	public void setObjective(final String objective) {
		this.objective = objective;
		updateLastModified();
	}

	public String getPrerequisites() { return prerequisites; }

	public void setPrerequisites(final String prerequisites) {
		this.prerequisites = prerequisites;
		updateLastModified();
	}

	public Set<CTestCase> getTestCases() {
		if (testCases == null) {
			testCases = new HashSet<>();
		}
		return testCases;
	}

	public void setTestCases(final Set<CTestCase> testCases) {
		this.testCases = testCases;
		updateLastModified();
	}
}
