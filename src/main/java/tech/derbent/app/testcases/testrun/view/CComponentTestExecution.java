package tech.derbent.app.testcases.testrun.view;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.shared.Registration;
import tech.derbent.api.interfaces.IPageServiceAutoRegistrable;
import tech.derbent.api.services.pageservice.CPageService;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CH3;
import tech.derbent.api.ui.component.basic.CH4;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.dialogs.CDialogConfirmation;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.app.testcases.testrun.domain.CTestCaseResult;
import tech.derbent.app.testcases.testrun.domain.CTestResult;
import tech.derbent.app.testcases.testrun.domain.CTestRun;
import tech.derbent.app.testcases.testrun.domain.CTestStepResult;
import tech.derbent.app.testcases.testrun.service.CTestRunService;

/** CComponentTestExecution - Main test execution interface following ISO 29119 and ISTQB standards.
 * <p>
 * Provides a comprehensive test execution environment with:
 * <ul>
 * <li>Test session progress tracking with visual indicators</li>
 * <li>Step-by-step test execution with expected/actual result comparison</li>
 * <li>Pass/Fail/Skip/Block result recording per step</li>
 * <li>Auto-save functionality with periodic saves</li>
 * <li>Keyboard shortcuts for efficient testing (P/F/S/B for results, Alt+arrows for navigation)</li>
 * <li>Evidence attachment capabilities (screenshots, files)</li>
 * <li>Session completion with statistical summary</li>
 * </ul>
 * <p>
 * ISO 29119 Part 3 compliant test execution interface.
 * ISTQB usability standards for keyboard navigation and result recording. */
public class CComponentTestExecution extends CVerticalLayout
		implements HasValue<HasValue.ValueChangeEvent<CTestRun>, CTestRun>, IPageServiceAutoRegistrable {

	public static final String ID_ROOT = "custom-test-execution-component";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentTestExecution.class);
	private static final long serialVersionUID = 1L;
	private static final int AUTO_SAVE_INTERVAL_SECONDS = 30;

	private final CTestRunService testRunService;
	private CTestRun currentTestRun;
	private List<CTestStepResult> allSteps;
	private int currentStepIndex = 0;
	private boolean hasUnsavedChanges = false;

	private CH3 headerTitle;
	private CSpan sessionInfoSpan;
	private ProgressBar progressBar;
	private CSpan progressText;
	private Div statusBadge;

	private Div currentTestCard;
	private CH4 testCaseNameLabel;
	private CSpan testCaseDescriptionLabel;
	private CSpan stepNavigatorLabel;
	private CSpan expectedResultLabel;
	private TextArea actualResultArea;
	private TextArea notesArea;
	private CButton buttonPass;
	private CButton buttonFail;
	private CButton buttonSkip;
	private CButton buttonBlock;
	private CButton buttonScreenshot;
	private CButton buttonAttach;

	private CHorizontalLayout footerLayout;
	private CButton buttonPrevious;
	private CButton buttonNext;
	private ComboBox<String> jumpToComboBox;
	private CButton buttonSaveExit;
	private CButton buttonComplete;
	private CSpan saveIndicator;
	private CSpan keyboardHints;

	private ScheduledExecutorService autoSaveExecutor;
	private ScheduledFuture<?> autoSaveTask;
	private Registration shortcutRegistration;

	public CComponentTestExecution(final CTestRunService testRunService) {
		super();
		Check.notNull(testRunService, "TestRunService cannot be null");
		this.testRunService = testRunService;
		initializeComponent();
	}

	private void initializeComponent() {
		setId(ID_ROOT);
		setSizeFull();
		setPadding(true);
		setSpacing(true);

		createHeader();
		createTestCard();
		createFooter();

		LOGGER.debug("Test execution component initialized");
	}

	private void createHeader() {
		final CVerticalLayout headerLayout = new CVerticalLayout(false, true, false);
		headerLayout.setId("custom-test-execution-header");
		headerLayout.setWidthFull();

		headerTitle = new CH3("Test Execution");
		headerTitle.getStyle().set("margin", "0");

		sessionInfoSpan = new CSpan("No test session loaded");
		sessionInfoSpan.getStyle().set("color", "var(--lumo-secondary-text-color)");

		final CHorizontalLayout progressLayout = new CHorizontalLayout();
		progressLayout.setWidthFull();
		progressLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

		progressBar = new ProgressBar();
		progressBar.setId("custom-test-execution-progress-bar");
		progressBar.setWidthFull();
		progressBar.setValue(0);

		progressText = new CSpan("0 of 0 steps (0%)");
		progressText.setId("custom-test-execution-progress-text");
		progressText.setWidth("200px");
		progressText.getStyle().set("text-align", "right");
		progressText.getStyle().set("white-space", "nowrap");

		statusBadge = new Div();
		statusBadge.setId("custom-test-execution-status-badge");
		statusBadge.setText("NOT STARTED");
		statusBadge.getStyle().set("padding", "4px 12px");
		statusBadge.getStyle().set("border-radius", "12px");
		statusBadge.getStyle().set("background-color", "var(--lumo-contrast-10pct)");
		statusBadge.getStyle().set("font-size", "var(--lumo-font-size-s)");
		statusBadge.getStyle().set("font-weight", "600");
		statusBadge.setWidth("150px");
		statusBadge.getStyle().set("text-align", "center");

		progressLayout.add(progressBar, progressText, statusBadge);
		progressLayout.setFlexGrow(1, progressBar);

		headerLayout.add(headerTitle, sessionInfoSpan, progressLayout);
		add(headerLayout);
	}

	private void createTestCard() {
		currentTestCard = new Div();
		currentTestCard.setId("custom-test-execution-card");
		currentTestCard.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
		currentTestCard.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
		currentTestCard.getStyle().set("padding", "var(--lumo-space-l)");
		currentTestCard.getStyle().set("background-color", "var(--lumo-base-color)");
		currentTestCard.setWidthFull();

		testCaseNameLabel = new CH4("Test Case");
		testCaseNameLabel.getStyle().set("margin-top", "0");

		testCaseDescriptionLabel = new CSpan("");
		testCaseDescriptionLabel.getStyle().set("color", "var(--lumo-secondary-text-color)");
		testCaseDescriptionLabel.getStyle().set("display", "block");
		testCaseDescriptionLabel.getStyle().set("margin-bottom", "var(--lumo-space-m)");

		stepNavigatorLabel = new CSpan("Step 0 of 0");
		stepNavigatorLabel.setId("custom-test-execution-step-navigator");
		stepNavigatorLabel.getStyle().set("font-weight", "600");
		stepNavigatorLabel.getStyle().set("color", "var(--lumo-primary-color)");
		stepNavigatorLabel.getStyle().set("display", "block");
		stepNavigatorLabel.getStyle().set("margin-bottom", "var(--lumo-space-m)");

		final H3 expectedLabel = new H3("Expected Result");
		expectedLabel.getStyle().set("font-size", "var(--lumo-font-size-m)");
		expectedLabel.getStyle().set("margin", "var(--lumo-space-m) 0 var(--lumo-space-s) 0");

		expectedResultLabel = new CSpan("");
		expectedResultLabel.setId("custom-test-execution-expected-result");
		expectedResultLabel.getStyle().set("display", "block");
		expectedResultLabel.getStyle().set("padding", "var(--lumo-space-m)");
		expectedResultLabel.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
		expectedResultLabel.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
		expectedResultLabel.getStyle().set("border-left", "4px solid var(--lumo-primary-color)");
		expectedResultLabel.getStyle().set("white-space", "pre-wrap");
		expectedResultLabel.getStyle().set("min-height", "60px");

		final H3 actualLabel = new H3("Actual Result");
		actualLabel.getStyle().set("font-size", "var(--lumo-font-size-m)");
		actualLabel.getStyle().set("margin", "var(--lumo-space-m) 0 var(--lumo-space-s) 0");

		actualResultArea = new TextArea();
		actualResultArea.setId("custom-test-execution-actual-result");
		actualResultArea.setWidthFull();
		actualResultArea.setMinHeight("150px");
		actualResultArea.setPlaceholder("Enter what actually happened during test execution...");
		actualResultArea.addValueChangeListener(e -> {
			if (e.isFromClient()) {
				hasUnsavedChanges = true;
				updateSaveIndicator("Unsaved");
			}
		});

		final CHorizontalLayout resultButtonLayout = new CHorizontalLayout();
		resultButtonLayout.setSpacing(true);
		resultButtonLayout.getStyle().set("margin-top", "var(--lumo-space-m)");

		final Icon passIcon = CColorUtils.createStyledIcon("vaadin:check-circle", "#28a745");
		buttonPass = new CButton("PASS", passIcon, e -> on_result_clicked(CTestResult.PASSED));
		buttonPass.setId("custom-test-execution-button-pass");
		buttonPass.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
		buttonPass.setTooltipText("Mark step as PASSED (P)");

		final Icon failIcon = CColorUtils.createStyledIcon("vaadin:close-circle", "#dc3545");
		buttonFail = new CButton("FAIL", failIcon, e -> on_result_clicked(CTestResult.FAILED));
		buttonFail.setId("custom-test-execution-button-fail");
		buttonFail.addThemeVariants(ButtonVariant.LUMO_ERROR);
		buttonFail.setTooltipText("Mark step as FAILED (F)");

		final Icon skipIcon = CColorUtils.createStyledIcon("vaadin:forward", "#6c757d");
		buttonSkip = new CButton("SKIP", skipIcon, e -> on_result_clicked(CTestResult.SKIPPED));
		buttonSkip.setId("custom-test-execution-button-skip");
		buttonSkip.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
		buttonSkip.setTooltipText("Mark step as SKIPPED (S)");

		final Icon blockIcon = CColorUtils.createStyledIcon("vaadin:ban", "#ffc107");
		buttonBlock = new CButton("BLOCK", blockIcon, e -> on_result_clicked(CTestResult.BLOCKED));
		buttonBlock.setId("custom-test-execution-button-block");
		buttonBlock.getStyle().set("background-color", "#ffc107");
		buttonBlock.getStyle().set("color", "#000");
		buttonBlock.setTooltipText("Mark step as BLOCKED (B)");

		final Icon screenshotIcon = new Icon(VaadinIcon.CAMERA);
		buttonScreenshot = new CButton("Screenshot", screenshotIcon, e -> on_screenshot_clicked());
		buttonScreenshot.setId("custom-test-execution-button-screenshot");
		buttonScreenshot.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		buttonScreenshot.setTooltipText("Capture screenshot evidence");

		final Icon attachIcon = new Icon(VaadinIcon.PAPERCLIP);
		buttonAttach = new CButton("Attach", attachIcon, e -> on_attach_clicked());
		buttonAttach.setId("custom-test-execution-button-attach");
		buttonAttach.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		buttonAttach.setTooltipText("Attach file evidence");

		resultButtonLayout.add(buttonPass, buttonFail, buttonSkip, buttonBlock, buttonScreenshot, buttonAttach);

		final H3 notesLabel = new H3("Additional Notes");
		notesLabel.getStyle().set("font-size", "var(--lumo-font-size-m)");
		notesLabel.getStyle().set("margin", "var(--lumo-space-m) 0 var(--lumo-space-s) 0");

		notesArea = new TextArea();
		notesArea.setId("custom-test-execution-notes");
		notesArea.setWidthFull();
		notesArea.setMinHeight("80px");
		notesArea.setPlaceholder("Optional additional notes about this step...");
		notesArea.addValueChangeListener(e -> {
			if (e.isFromClient()) {
				hasUnsavedChanges = true;
				updateSaveIndicator("Unsaved");
			}
		});

		currentTestCard.add(testCaseNameLabel, testCaseDescriptionLabel, stepNavigatorLabel,
				expectedLabel, expectedResultLabel,
				actualLabel, actualResultArea,
				resultButtonLayout,
				notesLabel, notesArea);

		add(currentTestCard);
		setFlexGrow(1, currentTestCard);
	}

	private void createFooter() {
		footerLayout = new CHorizontalLayout();
		footerLayout.setId("custom-test-execution-footer");
		footerLayout.setWidthFull();
		footerLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
		footerLayout.setSpacing(true);

		final Icon prevIcon = new Icon(VaadinIcon.ANGLE_LEFT);
		buttonPrevious = new CButton("< Previous", prevIcon, e -> on_previous_clicked());
		buttonPrevious.setId("custom-test-execution-button-previous");
		buttonPrevious.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		buttonPrevious.setTooltipText("Previous step (Alt+Left)");

		final Icon nextIcon = new Icon(VaadinIcon.ANGLE_RIGHT);
		buttonNext = new CButton("Next >", nextIcon, e -> on_next_clicked());
		buttonNext.setId("custom-test-execution-button-next");
		buttonNext.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		buttonNext.setTooltipText("Next step (Alt+Right)");

		jumpToComboBox = new ComboBox<>("Jump to");
		jumpToComboBox.setId("custom-test-execution-jump-combo");
		jumpToComboBox.setWidth("300px");
		jumpToComboBox.setPlaceholder("Select test case/step...");
		jumpToComboBox.addValueChangeListener(e -> {
			if (e.getValue() != null && e.isFromClient()) {
				on_jumpTo_selected(e.getValue());
			}
		});

		final Icon saveIcon = new Icon(VaadinIcon.DOWNLOAD);
		buttonSaveExit = new CButton("Save & Exit", saveIcon, e -> on_saveExit_clicked());
		buttonSaveExit.setId("custom-test-execution-button-save-exit");
		buttonSaveExit.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
		buttonSaveExit.setTooltipText("Save and exit execution (Ctrl+S)");

		final Icon completeIcon = new Icon(VaadinIcon.CHECK);
		buttonComplete = new CButton("Complete Execution", completeIcon, e -> on_complete_clicked());
		buttonComplete.setId("custom-test-execution-button-complete");
		buttonComplete.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
		buttonComplete.setTooltipText("Complete test session");
		buttonComplete.setEnabled(false);

		saveIndicator = new CSpan("");
		saveIndicator.setId("custom-test-execution-save-indicator");
		saveIndicator.getStyle().set("color", "var(--lumo-success-color)");
		saveIndicator.getStyle().set("font-size", "var(--lumo-font-size-s)");
		saveIndicator.setWidth("80px");

		keyboardHints = new CSpan("Shortcuts: P=Pass F=Fail S=Skip B=Block Alt+←/→=Navigate Ctrl+S=Save");
		keyboardHints.setId("custom-test-execution-keyboard-hints");
		keyboardHints.getStyle().set("color", "var(--lumo-secondary-text-color)");
		keyboardHints.getStyle().set("font-size", "var(--lumo-font-size-xs)");

		footerLayout.add(buttonPrevious, buttonNext, jumpToComboBox, buttonSaveExit, buttonComplete, saveIndicator);
		add(keyboardHints);
		add(footerLayout);
	}

	@Override
	public void setValue(final CTestRun testRun) {
		Objects.requireNonNull(testRun, "TestRun cannot be null");
		LOGGER.debug("Loading test run {} for execution", testRun.getId());

		this.currentTestRun = testRun;
		this.allSteps = buildStepList();
		this.currentStepIndex = findFirstUncompletedStep();
		this.hasUnsavedChanges = false;

		updateHeader();
		updateTestCard();
		updateFooter();
		updateSaveIndicator("Loaded");

		LOGGER.debug("Test run loaded with {} steps, starting at index {}", allSteps.size(), currentStepIndex);
	}

	@Override
	public CTestRun getValue() {
		return currentTestRun;
	}

	@Override
	public Registration addValueChangeListener(final ValueChangeListener<? super ValueChangeEvent<CTestRun>> listener) {
		return null;
	}

	@Override
	public void setReadOnly(final boolean readOnly) {
		actualResultArea.setReadOnly(readOnly);
		notesArea.setReadOnly(readOnly);
		buttonPass.setEnabled(!readOnly);
		buttonFail.setEnabled(!readOnly);
		buttonSkip.setEnabled(!readOnly);
		buttonBlock.setEnabled(!readOnly);
		buttonScreenshot.setEnabled(!readOnly);
		buttonAttach.setEnabled(!readOnly);
		buttonPrevious.setEnabled(!readOnly);
		buttonNext.setEnabled(!readOnly);
		buttonSaveExit.setEnabled(!readOnly);
		buttonComplete.setEnabled(!readOnly && isAllStepsCompleted());
	}

	@Override
	public boolean isReadOnly() {
		return actualResultArea.isReadOnly();
	}

	@Override
	public void setRequiredIndicatorVisible(final boolean requiredIndicatorVisible) {
	}

	@Override
	public boolean isRequiredIndicatorVisible() {
		return false;
	}

	@Override
	public void registerWithPageService(final CPageService<?> pageService) {
		Check.notNull(pageService, "Page service cannot be null");
		pageService.registerComponent(getComponentName(), this);
		LOGGER.debug("Test execution component registered with page service as '{}'", getComponentName());
	}

	@Override
	public String getComponentName() {
		return "testExecution";
	}

	@Override
	protected void onAttach(final AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		startAutoSave();
		registerKeyboardShortcuts();
		LOGGER.debug("Test execution component attached, auto-save started");
	}

	@Override
	protected void onDetach(final DetachEvent detachEvent) {
		super.onDetach(detachEvent);
		stopAutoSave();
		if (shortcutRegistration != null) {
			shortcutRegistration.remove();
		}
		LOGGER.debug("Test execution component detached, auto-save stopped");
	}

	private void registerKeyboardShortcuts() {
		final Component listenOn = this;

		Shortcuts.addShortcutListener(listenOn, () -> on_result_clicked(CTestResult.PASSED), Key.KEY_P);
		Shortcuts.addShortcutListener(listenOn, () -> on_result_clicked(CTestResult.FAILED), Key.KEY_F);
		Shortcuts.addShortcutListener(listenOn, () -> on_result_clicked(CTestResult.SKIPPED), Key.KEY_S);
		Shortcuts.addShortcutListener(listenOn, () -> on_result_clicked(CTestResult.BLOCKED), Key.KEY_B);

		Shortcuts.addShortcutListener(listenOn, this::on_previous_clicked, Key.ARROW_LEFT, KeyModifier.ALT);
		Shortcuts.addShortcutListener(listenOn, this::on_next_clicked, Key.ARROW_RIGHT, KeyModifier.ALT);

		Shortcuts.addShortcutListener(listenOn, this::on_saveExit_clicked, Key.KEY_S, KeyModifier.CONTROL);

		LOGGER.debug("Keyboard shortcuts registered");
	}

	private List<CTestStepResult> buildStepList() {
		final List<CTestStepResult> steps = new ArrayList<>();
		if (currentTestRun == null || currentTestRun.getTestCaseResults() == null) {
			return steps;
		}

		final List<CTestCaseResult> caseResults = new ArrayList<>(currentTestRun.getTestCaseResults());
		caseResults.sort(Comparator.comparing(CTestCaseResult::getExecutionOrder, Comparator.nullsLast(Comparator.naturalOrder())));

		for (final CTestCaseResult caseResult : caseResults) {
			if (caseResult.getTestStepResults() != null) {
				final List<CTestStepResult> stepResults = new ArrayList<>(caseResult.getTestStepResults());
				stepResults.sort(Comparator.comparing(sr -> sr.getTestStep().getStepOrder(), Comparator.nullsLast(Comparator.naturalOrder())));
				steps.addAll(stepResults);
			}
		}

		return steps;
	}

	private int findFirstUncompletedStep() {
		for (int i = 0; i < allSteps.size(); i++) {
			if (allSteps.get(i).getResult() == CTestResult.NOT_EXECUTED) {
				return i;
			}
		}
		return allSteps.isEmpty() ? 0 : allSteps.size() - 1;
	}

	private void updateHeader() {
		if (currentTestRun == null) {
			sessionInfoSpan.setText("No test session loaded");
			progressBar.setValue(0);
			progressText.setText("0 of 0 steps (0%)");
			statusBadge.setText("NOT STARTED");
			return;
		}

		final String sessionInfo = String.format("Session: %s | Suite: %s | Tester: %s",
				currentTestRun.getName(),
				currentTestRun.getTestScenario() != null ? currentTestRun.getTestScenario().getName() : "N/A",
				currentTestRun.getExecutedBy() != null ? currentTestRun.getExecutedBy().getName() : "Unassigned");
		sessionInfoSpan.setText(sessionInfo);

		final int completed = (int) allSteps.stream().filter(s -> s.getResult() != CTestResult.NOT_EXECUTED).count();
		final int total = allSteps.size();
		final double progress = total > 0 ? (double) completed / total : 0;
		final int percentage = (int) (progress * 100);

		progressBar.setValue(progress);
		progressText.setText(String.format("%d of %d steps (%d%%)", completed, total, percentage));

		updateStatusBadge();
	}

	private void updateStatusBadge() {
		if (currentTestRun == null) {
			return;
		}

		final CTestResult status = currentTestRun.getResult();
		String color = "var(--lumo-contrast-10pct)";
		String text = "NOT STARTED";

		if (status != null) {
			switch (status) {
				case PASSED:
					color = "#28a745";
					text = "PASSED";
					break;
				case FAILED:
					color = "#dc3545";
					text = "FAILED";
					break;
				case PARTIAL:
					color = "#ffc107";
					text = "PARTIAL";
					break;
				case BLOCKED:
					color = "#e83e8c";
					text = "BLOCKED";
					break;
				case SKIPPED:
					color = "#6c757d";
					text = "SKIPPED";
					break;
				default:
					break;
			}
		}

		statusBadge.setText(text);
		statusBadge.getStyle().set("background-color", color);
		statusBadge.getStyle().set("color", "#fff");
	}

	private void updateTestCard() {
		if (allSteps.isEmpty() || currentStepIndex < 0 || currentStepIndex >= allSteps.size()) {
			testCaseNameLabel.setText("No test steps available");
			testCaseDescriptionLabel.setText("");
			stepNavigatorLabel.setText("Step 0 of 0");
			expectedResultLabel.setText("");
			actualResultArea.clear();
			notesArea.clear();
			setResultButtonsEnabled(false);
			return;
		}

		final CTestStepResult currentStep = allSteps.get(currentStepIndex);
		final CTestCaseResult caseResult = currentStep.getTestCaseResult();

		if (caseResult != null && caseResult.getTestCase() != null) {
			testCaseNameLabel.setText(caseResult.getTestCase().getName());
			testCaseDescriptionLabel.setText(caseResult.getTestCase().getDescription() != null
					? caseResult.getTestCase().getDescription()
					: "");
		} else {
			testCaseNameLabel.setText("Unknown test case");
			testCaseDescriptionLabel.setText("");
		}

		stepNavigatorLabel.setText(String.format("Step %d of %d", currentStepIndex + 1, allSteps.size()));

		final String expectedResult = currentStep.getTestStep() != null && currentStep.getTestStep().getExpectedResult() != null
				? currentStep.getTestStep().getExpectedResult()
				: "No expected result defined";
		expectedResultLabel.setText(expectedResult);

		actualResultArea.setValue(currentStep.getActualResult() != null ? currentStep.getActualResult() : "");
		notesArea.setValue(currentStep.getNotes() != null ? currentStep.getNotes() : "");

		highlightResultButton(currentStep.getResult());
		setResultButtonsEnabled(true);
	}

	private void updateFooter() {
		buttonPrevious.setEnabled(currentStepIndex > 0);
		buttonNext.setEnabled(currentStepIndex < allSteps.size() - 1);
		buttonComplete.setEnabled(isAllStepsCompleted());
		populateJumpToComboBox();
	}

	private void populateJumpToComboBox() {
		final List<String> items = new ArrayList<>();
		if (currentTestRun == null || allSteps.isEmpty()) {
			jumpToComboBox.setItems(items);
			return;
		}

		for (int i = 0; i < allSteps.size(); i++) {
			final CTestStepResult step = allSteps.get(i);
			final CTestCaseResult caseResult = step.getTestCaseResult();
			final String caseName = caseResult != null && caseResult.getTestCase() != null
					? caseResult.getTestCase().getName()
					: "Unknown";
			final int stepOrder = step.getTestStep() != null ? step.getTestStep().getStepOrder() : i + 1;
			final String status = step.getResult() != CTestResult.NOT_EXECUTED ? " [" + step.getResult().name() + "]" : "";
			items.add(String.format("Step %d: %s - Step %d%s", i + 1, caseName, stepOrder, status));
		}

		jumpToComboBox.setItems(items);
	}

	private void highlightResultButton(final CTestResult result) {
		buttonPass.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
		buttonFail.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
		buttonSkip.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
		buttonBlock.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);

		if (result == CTestResult.PASSED) {
			buttonPass.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		} else if (result == CTestResult.FAILED) {
			buttonFail.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		} else if (result == CTestResult.SKIPPED) {
			buttonSkip.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		} else if (result == CTestResult.BLOCKED) {
			buttonBlock.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		}
	}

	private void setResultButtonsEnabled(final boolean enabled) {
		buttonPass.setEnabled(enabled);
		buttonFail.setEnabled(enabled);
		buttonSkip.setEnabled(enabled);
		buttonBlock.setEnabled(enabled);
		buttonScreenshot.setEnabled(enabled);
		buttonAttach.setEnabled(enabled);
	}

	private boolean isAllStepsCompleted() {
		return allSteps.stream().allMatch(s -> s.getResult() != CTestResult.NOT_EXECUTED);
	}

	private void on_result_clicked(final CTestResult result) {
		try {
			if (allSteps.isEmpty() || currentStepIndex < 0 || currentStepIndex >= allSteps.size()) {
				return;
			}

			final CTestStepResult currentStep = allSteps.get(currentStepIndex);
			currentStep.setResult(result);
			currentStep.setActualResult(actualResultArea.getValue());
			currentStep.setNotes(notesArea.getValue());

			LOGGER.debug("Step {} marked as {}", currentStepIndex + 1, result);

			saveCurrentTestRun();
			highlightResultButton(result);
			updateHeader();
			updateFooter();

			if (result == CTestResult.FAILED) {
				CNotificationService.showWarning("Test step failed - consider attaching evidence");
			}

			if (currentStepIndex < allSteps.size() - 1) {
				on_next_clicked();
			}

		} catch (final Exception e) {
			LOGGER.error("Error recording test result: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to record result", e);
		}
	}

	private void on_previous_clicked() {
		try {
			if (currentStepIndex > 0) {
				saveCurrentStep();
				currentStepIndex--;
				updateTestCard();
				updateFooter();
				LOGGER.debug("Navigated to step {}", currentStepIndex + 1);
			}
		} catch (final Exception e) {
			LOGGER.error("Error navigating to previous step: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to navigate", e);
		}
	}

	private void on_next_clicked() {
		try {
			if (currentStepIndex < allSteps.size() - 1) {
				saveCurrentStep();
				currentStepIndex++;
				updateTestCard();
				updateFooter();
				LOGGER.debug("Navigated to step {}", currentStepIndex + 1);
			}
		} catch (final Exception e) {
			LOGGER.error("Error navigating to next step: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to navigate", e);
		}
	}

	private void on_jumpTo_selected(final String selection) {
		try {
			final String indexStr = selection.substring(5, selection.indexOf(":"));
			final int newIndex = Integer.parseInt(indexStr) - 1;

			if (newIndex >= 0 && newIndex < allSteps.size() && newIndex != currentStepIndex) {
				saveCurrentStep();
				currentStepIndex = newIndex;
				updateTestCard();
				updateFooter();
				LOGGER.debug("Jumped to step {}", currentStepIndex + 1);
			}
		} catch (final Exception e) {
			LOGGER.error("Error jumping to step: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to jump to step", e);
		}
	}

	private void on_saveExit_clicked() {
		try {
			saveCurrentStep();
			saveCurrentTestRun();
			CNotificationService.showSuccess("Test session saved successfully");
			LOGGER.debug("Test session saved and exiting");
		} catch (final Exception e) {
			LOGGER.error("Error saving test session: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to save", e);
		}
	}

	private void on_complete_clicked() {
		try {
			if (!isAllStepsCompleted()) {
				CNotificationService.showWarning("Please complete all test steps before finishing");
				return;
			}

			final Runnable onConfirm = () -> {
				try {
					saveCurrentStep();
					final CTestRun completed = testRunService.completeTestRun(currentTestRun);
					this.currentTestRun = completed;

					final String summary = String.format(
							"Test Execution Complete!\n\n" +
									"Total Test Cases: %d\n" +
									"Passed: %d\n" +
									"Failed: %d\n\n" +
									"Total Steps: %d\n" +
									"Passed: %d\n" +
									"Failed: %d\n\n" +
									"Overall Result: %s",
							completed.getTotalTestCases(),
							completed.getPassedTestCases(),
							completed.getFailedTestCases(),
							completed.getTotalTestSteps(),
							completed.getPassedTestSteps(),
							completed.getFailedTestSteps(),
							completed.getResult());

					CNotificationService.showInfoDialog(summary);
					updateHeader();
					updateFooter();

					LOGGER.debug("Test run completed: {}", completed.getResult());

				} catch (final Exception e) {
					LOGGER.error("Error completing test run: {}", e.getMessage(), e);
					CNotificationService.showException("Failed to complete test run", e);
				}
			};

			CNotificationService.showConfirmationDialog(
					"Are you sure you want to complete this test execution?\nThis will finalize all results and calculate statistics.",
					onConfirm);

		} catch (final Exception e) {
			LOGGER.error("Error in complete confirmation: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to complete", e);
		}
	}

	private void on_screenshot_clicked() {
		try {
			CNotificationService.showInfo("Screenshot capture functionality - to be implemented with file upload");
			LOGGER.debug("Screenshot button clicked");
		} catch (final Exception e) {
			LOGGER.error("Error capturing screenshot: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to capture screenshot", e);
		}
	}

	private void on_attach_clicked() {
		try {
			CNotificationService.showInfo("File attachment functionality - to be implemented with file upload");
			LOGGER.debug("Attach button clicked");
		} catch (final Exception e) {
			LOGGER.error("Error attaching file: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to attach file", e);
		}
	}

	private void saveCurrentStep() {
		if (allSteps.isEmpty() || currentStepIndex < 0 || currentStepIndex >= allSteps.size()) {
			return;
		}

		final CTestStepResult currentStep = allSteps.get(currentStepIndex);
		currentStep.setActualResult(actualResultArea.getValue());
		currentStep.setNotes(notesArea.getValue());
		hasUnsavedChanges = false;
	}

	private void saveCurrentTestRun() {
		if (currentTestRun == null) {
			return;
		}

		try {
			updateSaveIndicator("Saving...");
			testRunService.save(currentTestRun);
			hasUnsavedChanges = false;
			updateSaveIndicator("Saved");
			LOGGER.debug("Test run saved: {}", currentTestRun.getId());
		} catch (final Exception e) {
			LOGGER.error("Failed to save test run: {}", e.getMessage(), e);
			updateSaveIndicator("Error");
			throw e;
		}
	}

	private void startAutoSave() {
		if (autoSaveExecutor == null || autoSaveExecutor.isShutdown()) {
			autoSaveExecutor = Executors.newSingleThreadScheduledExecutor();
		}

		autoSaveTask = autoSaveExecutor.scheduleAtFixedRate(() -> {
			if (hasUnsavedChanges && currentTestRun != null) {
				final UI ui = getUI().orElse(null);
				if (ui != null) {
					ui.access(() -> {
						try {
							saveCurrentStep();
							saveCurrentTestRun();
							LOGGER.debug("Auto-save executed");
						} catch (final Exception e) {
							LOGGER.error("Auto-save failed: {}", e.getMessage(), e);
						}
					});
				}
			}
		}, AUTO_SAVE_INTERVAL_SECONDS, AUTO_SAVE_INTERVAL_SECONDS, TimeUnit.SECONDS);

		LOGGER.debug("Auto-save started with interval: {} seconds", AUTO_SAVE_INTERVAL_SECONDS);
	}

	private void stopAutoSave() {
		if (autoSaveTask != null) {
			autoSaveTask.cancel(false);
		}
		if (autoSaveExecutor != null) {
			autoSaveExecutor.shutdown();
		}
		LOGGER.debug("Auto-save stopped");
	}

	private void updateSaveIndicator(final String text) {
		if (saveIndicator == null) {
			return;
		}

		saveIndicator.setText(text);

		switch (text) {
			case "Saved":
				saveIndicator.getStyle().set("color", "var(--lumo-success-color)");
				break;
			case "Saving...":
				saveIndicator.getStyle().set("color", "var(--lumo-primary-color)");
				break;
			case "Unsaved":
				saveIndicator.getStyle().set("color", "var(--lumo-warning-color)");
				break;
			case "Error":
				saveIndicator.getStyle().set("color", "var(--lumo-error-color)");
				break;
			default:
				saveIndicator.getStyle().set("color", "var(--lumo-secondary-text-color)");
				break;
		}
	}
}
