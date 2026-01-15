package tech.derbent.app.testcases.testcase.domain;

import java.util.HashSet;
import java.util.Set;
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
import tech.derbent.app.testcases.testcasetype.domain.CTestCaseType;
import tech.derbent.app.testcases.teststep.domain.CTestStep;
import tech.derbent.app.testcases.testscenario.domain.CTestScenario;

@Entity
@Table (name = "ctestcase")
@AttributeOverride (name = "id", column = @Column (name = "testcase_id"))
public class CTestCase extends CProjectItem<CTestCase>
		implements IHasStatusAndWorkflow<CTestCase>, IHasAttachments, IHasComments {

	public static final String DEFAULT_COLOR = "#4169E1"; // RoyalBlue - testing and quality
	public static final String DEFAULT_ICON = "vaadin:clipboard-check";
	public static final String ENTITY_TITLE_PLURAL = "Test Cases";
	public static final String ENTITY_TITLE_SINGULAR = "Test Case";
	public static final String VIEW_NAME = "Test Cases View";

	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Test Case Type", required = false, readOnly = false, description = "Type category of the test case",
			hidden = false, dataProviderBean = "CTestCaseTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CTestCaseType entityType;

	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Preconditions", required = false, readOnly = false,
			description = "Prerequisites that must be met before test execution", hidden = false, maxLength = 2000
	)
	private String preconditions;

	@Enumerated (EnumType.STRING)
	@Column (name = "priority", nullable = true, length = 20)
	@AMetaData (
			displayName = "Priority", required = false, readOnly = false,
			description = "Test case priority level", hidden = false
	)
	private CTestPriority priority = CTestPriority.MEDIUM;

	@Enumerated (EnumType.STRING)
	@Column (name = "severity", nullable = true, length = 20)
	@AMetaData (
			displayName = "Severity", required = false, readOnly = false,
			description = "Impact level if test fails", hidden = false
	)
	private CTestSeverity severity = CTestSeverity.NORMAL;

	@Column (name = "automated", nullable = false)
	@AMetaData (
			displayName = "Automated", required = false, readOnly = false, defaultValue = "false",
			description = "Whether test is automated (e.g., Playwright)", hidden = false
	)
	private Boolean automated = false;

	@Column (name = "automated_test_path", nullable = true, length = 500)
	@Size (max = 500)
	@AMetaData (
			displayName = "Automated Test Path", required = false, readOnly = false,
			description = "Path to automated test file/method", hidden = false, maxLength = 500
	)
	private String automatedTestPath;

	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "testscenario_id", nullable = true)
	@AMetaData (
			displayName = "Test Scenario", required = false, readOnly = false,
			description = "Parent test scenario", hidden = false,
			dataProviderBean = "CTestScenarioService"
	)
	private CTestScenario testScenario;

	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "testCase")
	@AMetaData (
			displayName = "Test Steps", required = false, readOnly = false,
			description = "Ordered test steps", hidden = false,
			dataProviderBean = "CTestStepService", createComponentMethod = "createComponent"
	)
	private Set<CTestStep> testSteps = new HashSet<>();

	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "testcase_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "Test case attachments",
			hidden = false, dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();

	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "testcase_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Test case comments",
			hidden = false, dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();

	/** Default constructor for JPA. */
	public CTestCase() {
		super();
		initializeDefaults();
	}

	public CTestCase(final String name, final CProject project) {
		super(CTestCase.class, name, project);
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
		Check.notNull(entityType, "Entity type cannot be null when retrieving workflow");
		return entityType.getWorkflow();
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		if (priority == null) {
			priority = CTestPriority.MEDIUM;
		}
		if (severity == null) {
			severity = CTestSeverity.NORMAL;
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
		Check.instanceOf(typeEntity, CTestCaseType.class, "Type entity must be an instance of CTestCaseType");
		Check.notNull(getProject(), "Project must be set before assigning test case type");
		Check.notNull(getProject().getCompany(), "Project company must be set before assigning test case type");
		Check.notNull(typeEntity.getCompany(), "Type entity company must be set before assigning test case type");
		Check.isTrue(typeEntity.getCompany().getId().equals(getProject().getCompany().getId()),
				"Type entity company id " + typeEntity.getCompany().getId() + " does not match test case project company id "
						+ getProject().getCompany().getId());
		entityType = (CTestCaseType) typeEntity;
		updateLastModified();
	}

	public String getPreconditions() { return preconditions; }

	public void setPreconditions(final String preconditions) {
		this.preconditions = preconditions;
		updateLastModified();
	}

	public CTestPriority getPriority() { return priority; }

	public void setPriority(final CTestPriority priority) {
		this.priority = priority;
		updateLastModified();
	}

	public CTestSeverity getSeverity() { return severity; }

	public void setSeverity(final CTestSeverity severity) {
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

	public CTestScenario getTestScenario() { return testScenario; }

	public void setTestScenario(final CTestScenario testScenario) {
		this.testScenario = testScenario;
		updateLastModified();
	}

	public Set<CTestStep> getTestSteps() {
		if (testSteps == null) {
			testSteps = new HashSet<>();
		}
		return testSteps;
	}

	public void setTestSteps(final Set<CTestStep> testSteps) {
		this.testSteps = testSteps;
		updateLastModified();
	}
}
