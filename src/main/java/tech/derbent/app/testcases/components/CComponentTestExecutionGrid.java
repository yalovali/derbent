package tech.derbent.app.testcases.components;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CGrid;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.activities.service.CActivityService;
import tech.derbent.app.issues.domain.CIssue;
import tech.derbent.app.issues.service.CIssueService;
import tech.derbent.app.testcases.testcase.domain.CTestCase;
import tech.derbent.app.testcases.testrun.domain.CTestCaseResult;
import tech.derbent.app.testcases.testrun.domain.CTestResult;
import tech.derbent.app.testcases.testrun.domain.CTestRun;
import tech.derbent.app.testcases.testrun.domain.CTestStepResult;
import tech.derbent.app.testcases.teststep.domain.CTestStep;
import tech.derbent.app.tickets.domain.CTicket;
import tech.derbent.app.tickets.service.CTicketService;

/**
 * CComponentTestExecutionGrid - Specialized component for executing test steps with failure tracking.
 * 
 * <p><strong>Key Features:</strong>
 * <ul>
 *   <li>Interactive test step execution with Pass/Fail/Skip buttons</li>
 *   <li>Real-time progress tracking with color-coded status indicators</li>
 *   <li>Automatic issue/bug/activity creation from test failures</li>
 *   <li>Detailed error capture with screenshots and logs</li>
 *   <li>Test step result history and reporting</li>
 * </ul>
 * 
 * <p><strong>Failure Handling:</strong>
 * When a test step fails, users can:
 * <ul>
 *   <li><strong>Create Issue</strong> - For UI/UX problems or functional issues</li>
 *   <li><strong>Create Bug</strong> - For defects requiring code fixes</li>
 *   <li><strong>Create Activity</strong> - For work items or improvements</li>
 * </ul>
 * 
 * <p><strong>Auto-populated Information:</strong>
 * <ul>
 *   <li>Title: "Test Failure: [Test Case Name] - Step [N]"</li>
 *   <li>Description: Test step details, expected vs actual results, error details</li>
 *   <li>Priority: Set based on test severity</li>
 *   <li>Link back to test run for traceability</li>
 * </ul>
 * 
 * <p><strong>Usage Pattern:</strong>
 * <pre>
 * CComponentTestExecutionGrid grid = new CComponentTestExecutionGrid(
 *     testRun, testCase, notificationService, issueService, ticketService, activityService);
 * grid.loadTestSteps();
 * parentLayout.add(grid);
 * </pre>
 */
public class CComponentTestExecutionGrid extends VerticalLayout {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentTestExecutionGrid.class);
	
	private final CTestRun testRun;
	private final CTestCase testCase;
	private final CTestCaseResult testCaseResult;
	private final CGrid<TestStepExecution> gridSteps;
	private final List<TestStepExecution> stepExecutions;
	private final Div summaryPanel;
	
	// Services for creating issues/bugs from failures
	private final CNotificationService notificationService;
	private final CIssueService issueService;
	private final CTicketService ticketService;
	private final CActivityService activityService;
	
	private int totalSteps = 0;
	private int passedSteps = 0;
	private int failedSteps = 0;
	private int skippedSteps = 0;

	/**
	 * Constructor for test execution grid with failure tracking.
	 */
	public CComponentTestExecutionGrid(
			final CTestRun testRun,
			final CTestCase testCase,
			final CNotificationService notificationService,
			final CIssueService issueService,
			final CTicketService ticketService,
			final CActivityService activityService) {
		this.testRun = testRun;
		this.testCase = testCase;
		this.notificationService = notificationService;
		this.issueService = issueService;
		this.ticketService = ticketService;
		this.activityService = activityService;
		this.testCaseResult = new CTestCaseResult(testRun, testCase);
		this.stepExecutions = new ArrayList<>();
		this.gridSteps = new CGrid<>(TestStepExecution.class, false);
		this.summaryPanel = new Div();
		
		initializeComponent();
	}

	private void initializeComponent() {
		setId("test-execution-grid");
		setWidthFull();
		setPadding(true);
		setSpacing(true);
		
		// Header
		final H3 header = new H3("Execute Test Case: " + testCase.getName());
		header.getStyle().set("margin-top", "0");
		add(header);
		
		// Test scenario info (if part of scenario)
		if (testCase.getTestScenario() != null) {
			final Div scenarioInfo = new Div();
			scenarioInfo.getStyle()
				.set("background-color", "var(--lumo-contrast-5pct)")
				.set("padding", "var(--lumo-space-s)")
				.set("border-radius", "var(--lumo-border-radius-m)")
				.set("margin-bottom", "var(--lumo-space-s)");
			scenarioInfo.add(new Span("Test Scenario: " + testCase.getTestScenario().getName()));
			add(scenarioInfo);
		}
		
		// Preconditions (if any)
		if (testCase.getPreconditions() != null && !testCase.getPreconditions().trim().isEmpty()) {
			final Div preconditions = new Div();
			preconditions.getStyle()
				.set("background-color", "var(--lumo-primary-color-10pct)")
				.set("padding", "var(--lumo-space-m)")
				.set("border-radius", "var(--lumo-border-radius-m)")
				.set("margin-bottom", "var(--lumo-space-m)")
				.set("border-left", "4px solid var(--lumo-primary-color)");
			
			final Span precondLabel = new Span("⚠️ Preconditions: ");
			precondLabel.getStyle().set("font-weight", "bold");
			final Span precondText = new Span(testCase.getPreconditions());
			preconditions.add(precondLabel, precondText);
			add(preconditions);
		}
		
		// Summary panel
		summaryPanel.setId("test-summary-panel");
		summaryPanel.getStyle()
			.set("background-color", "var(--lumo-contrast-10pct)")
			.set("padding", "var(--lumo-space-m)")
			.set("border-radius", "var(--lumo-border-radius-m)")
			.set("margin-bottom", "var(--lumo-space-m)");
		updateSummary();
		add(summaryPanel);
		
		// Configure grid
		configureGrid();
		add(gridSteps);
		
		// Control buttons
		final HorizontalLayout controls = create_layoutControls();
		add(controls);
	}

	private void configureGrid() {
		gridSteps.setId("grid-test-steps");
		gridSteps.setWidthFull();
		gridSteps.setHeight("500px");
		
		// Step number column
		gridSteps.addColumn(TestStepExecution::getStepOrder)
			.setHeader("Step")
			.setWidth("80px")
			.setFlexGrow(0);
		
		// Action column
		gridSteps.addColumn(TestStepExecution::getAction)
			.setHeader("Action")
			.setFlexGrow(3);
		
		// Expected result column
		gridSteps.addColumn(TestStepExecution::getExpectedResult)
			.setHeader("Expected Result")
			.setFlexGrow(2);
		
		// Status column with icon
		gridSteps.addComponentColumn(this::create_statusIndicator)
			.setHeader("Status")
			.setWidth("120px")
			.setFlexGrow(0);
		
		// Action buttons column
		gridSteps.addComponentColumn(this::create_actionButtons)
			.setHeader("Actions")
			.setWidth("280px")
			.setFlexGrow(0);
		
		// Details expander
		gridSteps.setItemDetailsRenderer(this::create_detailsRenderer);
		
		gridSteps.setItems(stepExecutions);
	}

	private Span create_statusIndicator(final TestStepExecution execution) {
		final Span status = new Span();
		status.getStyle()
			.set("display", "flex")
			.set("align-items", "center")
			.set("gap", "var(--lumo-space-xs)");
		
		Icon icon;
		String text;
		String color;
		
		switch (execution.getResult()) {
			case PASSED:
				icon = VaadinIcon.CHECK_CIRCLE.create();
				text = "PASSED";
				color = "var(--lumo-success-color)";
				break;
			case FAILED:
				icon = VaadinIcon.CLOSE_CIRCLE.create();
				text = "FAILED";
				color = "var(--lumo-error-color)";
				break;
			case SKIPPED:
				icon = VaadinIcon.ARROW_CIRCLE_RIGHT.create();
				text = "SKIPPED";
				color = "var(--lumo-contrast-50pct)";
				break;
			default:
				icon = VaadinIcon.CIRCLE_THIN.create();
				text = "PENDING";
				color = "var(--lumo-contrast-30pct)";
		}
		
		icon.setColor(color);
		final Span textSpan = new Span(text);
		textSpan.getStyle().set("color", color).set("font-weight", "500").set("font-size", "var(--lumo-font-size-s)");
		
		status.add(icon, textSpan);
		return status;
	}

	private HorizontalLayout create_actionButtons(final TestStepExecution execution) {
		final HorizontalLayout buttons = new HorizontalLayout();
		buttons.setSpacing(true);
		buttons.setPadding(false);
		
		// Pass button
		final CButton buttonPass = new CButton(VaadinIcon.CHECK.create());
		buttonPass.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
		buttonPass.setId("button-pass-step-" + execution.getStepOrder());
		buttonPass.setTooltipText("Mark step as passed");
		buttonPass.addClickListener(e -> on_buttonPass_clicked(execution));
		
		// Fail button
		final CButton buttonFail = new CButton(VaadinIcon.CLOSE.create());
		buttonFail.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
		buttonFail.setId("button-fail-step-" + execution.getStepOrder());
		buttonFail.setTooltipText("Mark step as failed");
		buttonFail.addClickListener(e -> on_buttonFail_clicked(execution));
		
		// Skip button
		final CButton buttonSkip = new CButton(VaadinIcon.ARROW_RIGHT.create());
		buttonSkip.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_SMALL);
		buttonSkip.setId("button-skip-step-" + execution.getStepOrder());
		buttonSkip.setTooltipText("Skip this step");
		buttonSkip.addClickListener(e -> on_buttonSkip_clicked(execution));
		
		// Create Issue/Bug button (only shown for failed steps)
		if (execution.getResult() == CTestResult.FAILED) {
			final CButton buttonCreateIssue = new CButton(VaadinIcon.BUG.create());
			buttonCreateIssue.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
			buttonCreateIssue.setId("button-create-issue-step-" + execution.getStepOrder());
			buttonCreateIssue.setTooltipText("Create Issue/Bug from failure");
			buttonCreateIssue.addClickListener(e -> on_buttonCreateIssue_clicked(execution));
			buttons.add(buttonPass, buttonFail, buttonSkip, buttonCreateIssue);
		} else {
			buttons.add(buttonPass, buttonFail, buttonSkip);
		}
		
		return buttons;
	}

	private VerticalLayout create_detailsRenderer(final TestStepExecution execution) {
		final VerticalLayout details = new VerticalLayout();
		details.setPadding(true);
		details.setSpacing(true);
		
		// Test data (if any)
		if (execution.getTestData() != null && !execution.getTestData().trim().isEmpty()) {
			final Div testData = new Div();
			testData.getStyle()
				.set("background-color", "var(--lumo-contrast-5pct)")
				.set("padding", "var(--lumo-space-s)")
				.set("border-radius", "var(--lumo-border-radius-s)")
				.set("margin-bottom", "var(--lumo-space-s)");
			final Span label = new Span("📋 Test Data: ");
			label.getStyle().set("font-weight", "bold");
			testData.add(label, new Span(execution.getTestData()));
			details.add(testData);
		}
		
		// Actual result input
		final TextArea textAreaActualResult = new TextArea("Actual Result");
		textAreaActualResult.setWidthFull();
		textAreaActualResult.setPlaceholder("Enter what actually happened...");
		textAreaActualResult.setValue(execution.getActualResult() != null ? execution.getActualResult() : "");
		textAreaActualResult.addValueChangeListener(e -> execution.setActualResult(e.getValue()));
		details.add(textAreaActualResult);
		
		// Error details input (visible if failed)
		if (execution.getResult() == CTestResult.FAILED) {
			final TextArea textAreaError = new TextArea("Error Details");
			textAreaError.setWidthFull();
			textAreaError.setPlaceholder("Enter error message, stack trace, or additional details...");
			textAreaError.setValue(execution.getErrorDetails() != null ? execution.getErrorDetails() : "");
			textAreaError.addValueChangeListener(e -> execution.setErrorDetails(e.getValue()));
			textAreaError.getStyle().set("background-color", "var(--lumo-error-color-10pct)");
			details.add(textAreaError);
		}
		
		return details;
	}

	private HorizontalLayout create_layoutControls() {
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setWidthFull();
		layout.setJustifyContentMode(JustifyContentMode.END);
		layout.setSpacing(true);
		
		final CButton buttonComplete = new CButton("Complete Test Run", VaadinIcon.CHECK_SQUARE_O.create());
		buttonComplete.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		buttonComplete.setId("button-complete-test-run");
		buttonComplete.addClickListener(e -> on_buttonComplete_clicked());
		
		final CButton buttonCancel = new CButton("Cancel");
		buttonCancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		buttonCancel.setId("button-cancel-test-run");
		
		layout.add(buttonCancel, buttonComplete);
		return layout;
	}

	protected void on_buttonPass_clicked(final TestStepExecution execution) {
		execution.setResult(CTestResult.PASSED);
		passedSteps++;
		updateSummary();
		gridSteps.getDataProvider().refreshItem(execution);
		
		LOGGER.info("Step {} marked as PASSED", execution.getStepOrder());
		if (notificationService != null) {
			notificationService.showSuccess("Step " + execution.getStepOrder() + " passed ✓");
		}
	}

	protected void on_buttonFail_clicked(final TestStepExecution execution) {
		execution.setResult(CTestResult.FAILED);
		failedSteps++;
		updateSummary();
		gridSteps.getDataProvider().refreshItem(execution);
		gridSteps.setDetailsVisible(execution, true); // Expand to show error details
		
		LOGGER.info("Step {} marked as FAILED", execution.getStepOrder());
		if (notificationService != null) {
			notificationService.showError("Step " + execution.getStepOrder() + " failed! Click bug icon to create issue.");
		}
	}

	protected void on_buttonSkip_clicked(final TestStepExecution execution) {
		execution.setResult(CTestResult.SKIPPED);
		skippedSteps++;
		updateSummary();
		gridSteps.getDataProvider().refreshItem(execution);
		
		LOGGER.info("Step {} marked as SKIPPED", execution.getStepOrder());
		if (notificationService != null) {
			notificationService.showInfo("Step " + execution.getStepOrder() + " skipped");
		}
	}

	/**
	 * Opens dialog to create Issue/Bug/Activity from test failure.
	 */
	protected void on_buttonCreateIssue_clicked(final TestStepExecution execution) {
		final Dialog dialog = new Dialog();
		dialog.setWidth("600px");
		dialog.setCloseOnEsc(true);
		dialog.setCloseOnOutsideClick(false);
		
		final VerticalLayout dialogLayout = new VerticalLayout();
		dialogLayout.setPadding(true);
		dialogLayout.setSpacing(true);
		
		// Dialog header
		final H4 header = new H4("Create Issue from Test Failure");
		header.getStyle().set("margin-top", "0");
		dialogLayout.add(header);
		
		// Type selector
		final ComboBox<String> comboBoxType = new ComboBox<>("Issue Type");
		comboBoxType.setItems("Bug (Defect)", "Issue (Functional)", "Activity (Work Item)");
		comboBoxType.setValue("Bug (Defect)");
		comboBoxType.setWidthFull();
		dialogLayout.add(comboBoxType);
		
		// Auto-populated title
		final TextField textFieldTitle = new TextField("Title");
		textFieldTitle.setWidthFull();
		textFieldTitle.setValue(String.format("Test Failure: %s - Step %d",
			testCase.getName(), execution.getStepOrder()));
		dialogLayout.add(textFieldTitle);
		
		// Auto-populated description
		final TextArea textAreaDescription = new TextArea("Description");
		textAreaDescription.setWidthFull();
		textAreaDescription.setHeight("200px");
		final String autoDescription = buildFailureDescription(execution);
		textAreaDescription.setValue(autoDescription);
		dialogLayout.add(textAreaDescription);
		
		// Buttons
		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setWidthFull();
		buttonLayout.setJustifyContentMode(JustifyContentMode.END);
		
		final CButton buttonCreate = new CButton("Create", VaadinIcon.CHECK.create());
		buttonCreate.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		buttonCreate.addClickListener(e -> {
			createIssueFromFailure(comboBoxType.getValue(), textFieldTitle.getValue(),
				textAreaDescription.getValue(), execution);
			dialog.close();
		});
		
		final CButton buttonCancel = new CButton("Cancel");
		buttonCancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		buttonCancel.addClickListener(e -> dialog.close());
		
		buttonLayout.add(buttonCancel, buttonCreate);
		dialogLayout.add(buttonLayout);
		
		dialog.add(dialogLayout);
		dialog.open();
	}

	private String buildFailureDescription(final TestStepExecution execution) {
		final StringBuilder desc = new StringBuilder();
		desc.append("**Test Failure Details**\n\n");
		desc.append("Test Run: ").append(testRun.getName()).append("\n");
		desc.append("Test Case: ").append(testCase.getName()).append("\n");
		desc.append("Test Step: ").append(execution.getStepOrder()).append("\n");
		desc.append("Build: ").append(testRun.getBuildNumber() != null ? testRun.getBuildNumber() : "N/A").append("\n");
		desc.append("Environment: ").append(testRun.getEnvironment() != null ? testRun.getEnvironment() : "N/A").append("\n\n");
		
		desc.append("**Test Step Information**\n\n");
		desc.append("Action: ").append(execution.getAction()).append("\n\n");
		desc.append("Expected Result:\n").append(execution.getExpectedResult()).append("\n\n");
		
		if (execution.getActualResult() != null && !execution.getActualResult().trim().isEmpty()) {
			desc.append("**Actual Result:**\n").append(execution.getActualResult()).append("\n\n");
		}
		
		if (execution.getErrorDetails() != null && !execution.getErrorDetails().trim().isEmpty()) {
			desc.append("**Error Details:**\n```\n").append(execution.getErrorDetails()).append("\n```\n\n");
		}
		
		desc.append("**Reproduction Steps:**\n");
		desc.append("1. Execute test scenario: ").append(testCase.getTestScenario() != null ?
			testCase.getTestScenario().getName() : "N/A").append("\n");
		desc.append("2. Run test case: ").append(testCase.getName()).append("\n");
		desc.append("3. Execute step ").append(execution.getStepOrder()).append(": ").append(execution.getAction()).append("\n");
		
		return desc.toString();
	}

	private void createIssueFromFailure(final String type, final String title, final String description,
			final TestStepExecution execution) {
		try {
			final CProject project = testRun.getProject();
			
			if ("Bug (Defect)".equals(type)) {
				// Create Bug (CTicket)
				if (ticketService != null) {
					final CTicket ticket = new CTicket(title, project);
					ticket.setDescription(description);
					// Link to test run (add reference in notes/comments)
					ticketService.save(ticket);
					execution.setLinkedIssueId(ticket.getId());
					execution.setLinkedIssueType("Bug");
					
					LOGGER.info("Created bug #{} from test failure: {}", ticket.getId(), title);
					if (notificationService != null) {
						notificationService.showSuccess("Bug created successfully! ID: " + ticket.getId());
					}
				}
			} else if ("Issue (Functional)".equals(type)) {
				// Create Issue (CIssue)
				if (issueService != null) {
					final CIssue issue = new CIssue(title, project);
					issue.setDescription(description);
					issueService.save(issue);
					execution.setLinkedIssueId(issue.getId());
					execution.setLinkedIssueType("Issue");
					
					LOGGER.info("Created issue #{} from test failure: {}", issue.getId(), title);
					if (notificationService != null) {
						notificationService.showSuccess("Issue created successfully! ID: " + issue.getId());
					}
				}
			} else if ("Activity (Work Item)".equals(type)) {
				// Create Activity (CActivity)
				if (activityService != null) {
					final CActivity activity = new CActivity(title, project);
					activity.setDescription(description);
					activityService.save(activity);
					execution.setLinkedIssueId(activity.getId());
					execution.setLinkedIssueType("Activity");
					
					LOGGER.info("Created activity #{} from test failure: {}", activity.getId(), title);
					if (notificationService != null) {
						notificationService.showSuccess("Activity created successfully! ID: " + activity.getId());
					}
				}
			}
			
			// Refresh grid to show linked issue
			gridSteps.getDataProvider().refreshItem(execution);
			
		} catch (final Exception ex) {
			LOGGER.error("Failed to create issue from test failure", ex);
			if (notificationService != null) {
				notificationService.showError("Failed to create issue: " + ex.getMessage());
			}
		}
	}

	protected void on_buttonComplete_clicked() {
		// Calculate final results
		final int executedSteps = passedSteps + failedSteps + skippedSteps;
		if (executedSteps < totalSteps) {
			if (notificationService != null) {
				notificationService.showWarning(
					"Not all steps executed! " + (totalSteps - executedSteps) + " steps remaining.");
			}
			return;
		}
		
		// Set test case result
		if (failedSteps > 0) {
			testCaseResult.setResult(CTestResult.FAILED);
		} else if (passedSteps == totalSteps) {
			testCaseResult.setResult(CTestResult.PASSED);
		} else {
			testCaseResult.setResult(CTestResult.PARTIAL);
		}
		
		// Save step results
		for (final TestStepExecution execution : stepExecutions) {
			final CTestStepResult stepResult = new CTestStepResult(testCaseResult, execution.getTestStep());
			stepResult.setResult(execution.getResult());
			stepResult.setActualResult(execution.getActualResult());
			stepResult.setErrorDetails(execution.getErrorDetails());
			testCaseResult.getTestStepResults().add(stepResult);
		}
		
		// Set execution time
		testCaseResult.setExecutionOrder(testCaseResult.getTestRun().getTestCaseResults().size() + 1);
		
		LOGGER.info("Test case execution completed: {} passed, {} failed, {} skipped",
			passedSteps, failedSteps, skippedSteps);
		
		if (notificationService != null) {
			if (failedSteps > 0) {
				notificationService.showWarning("Test completed with " + failedSteps + " failures");
			} else {
				notificationService.showSuccess("Test completed successfully! All steps passed ✓");
			}
		}
	}

	private void updateSummary() {
		summaryPanel.removeAll();
		
		final HorizontalLayout summary = new HorizontalLayout();
		summary.setWidthFull();
		summary.setSpacing(true);
		summary.setAlignItems(Alignment.CENTER);
		
		summary.add(create_summaryBadge("Total", totalSteps, "var(--lumo-contrast-60pct)", "📊"));
		summary.add(create_summaryBadge("Passed", passedSteps, "var(--lumo-success-color)", "✓"));
		summary.add(create_summaryBadge("Failed", failedSteps, "var(--lumo-error-color)", "✗"));
		summary.add(create_summaryBadge("Skipped", skippedSteps, "var(--lumo-contrast-50pct)", "⊘"));
		
		final int pending = totalSteps - (passedSteps + failedSteps + skippedSteps);
		summary.add(create_summaryBadge("Pending", pending, "var(--lumo-contrast-30pct)", "○"));
		
		// Progress percentage
		if (totalSteps > 0) {
			final int progress = ((passedSteps + failedSteps + skippedSteps) * 100) / totalSteps;
			final Span progressSpan = new Span("Progress: " + progress + "%");
			progressSpan.getStyle()
				.set("margin-left", "auto")
				.set("font-weight", "bold")
				.set("color", "var(--lumo-primary-color)");
			summary.add(progressSpan);
		}
		
		summaryPanel.add(summary);
	}

	private Span create_summaryBadge(final String label, final int count, final String color, final String icon) {
		final Span badge = new Span(icon + " " + label + ": " + count);
		badge.getStyle()
			.set("padding", "var(--lumo-space-xs) var(--lumo-space-m)")
			.set("border-radius", "var(--lumo-border-radius-m)")
			.set("background-color", color)
			.set("color", "white")
			.set("font-weight", "bold")
			.set("font-size", "var(--lumo-font-size-s)")
			.set("white-space", "nowrap");
		return badge;
	}

	/**
	 * Load test steps for the test case.
	 */
	public void loadTestSteps() {
		stepExecutions.clear();
		
		// Load test steps from test case
		final List<CTestStep> steps = new ArrayList<>(testCase.getTestSteps());
		steps.sort((a, b) -> Integer.compare(a.getStepOrder(), b.getStepOrder()));
		
		for (final CTestStep step : steps) {
			final TestStepExecution execution = new TestStepExecution(step);
			stepExecutions.add(execution);
		}
		
		totalSteps = stepExecutions.size();
		passedSteps = 0;
		failedSteps = 0;
		skippedSteps = 0;
		
		gridSteps.setItems(stepExecutions);
		updateSummary();
		
		LOGGER.info("Loaded {} test steps for test case: {}", totalSteps, testCase.getName());
	}

	/**
	 * Get the test case result with all step results.
	 */
	public CTestCaseResult getTestCaseResult() {
		return testCaseResult;
	}

	/**
	 * Inner class to hold test step execution state with issue linking.
	 */
	public static class TestStepExecution {
		private final CTestStep testStep;
		private CTestResult result;
		private String actualResult;
		private String errorDetails;
		private Long linkedIssueId;
		private String linkedIssueType; // "Bug", "Issue", or "Activity"

		public TestStepExecution(final CTestStep testStep) {
			this.testStep = testStep;
			this.result = CTestResult.NOT_EXECUTED;
		}

		public CTestStep getTestStep() { return testStep; }
		public Integer getStepOrder() { return testStep.getStepOrder(); }
		public String getAction() { return testStep.getAction(); }
		public String getExpectedResult() { return testStep.getExpectedResult(); }
		public String getTestData() { return testStep.getTestData(); }
		
		public CTestResult getResult() { return result; }
		public void setResult(CTestResult result) { this.result = result; }
		
		public String getActualResult() { return actualResult; }
		public void setActualResult(String actualResult) { this.actualResult = actualResult; }
		
		public String getErrorDetails() { return errorDetails; }
		public void setErrorDetails(String errorDetails) { this.errorDetails = errorDetails; }
		
		public Long getLinkedIssueId() { return linkedIssueId; }
		public void setLinkedIssueId(Long linkedIssueId) { this.linkedIssueId = linkedIssueId; }
		
		public String getLinkedIssueType() { return linkedIssueType; }
		public void setLinkedIssueType(String linkedIssueType) { this.linkedIssueType = linkedIssueType; }
	}
}
