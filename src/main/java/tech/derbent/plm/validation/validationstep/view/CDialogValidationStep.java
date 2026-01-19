package tech.derbent.plm.validation.validationstep.view;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.annotations.CFormBuilder;
import tech.derbent.api.components.CBinderFactory;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.ui.component.basic.CTextArea;
import tech.derbent.api.ui.component.basic.CTextField;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.dialogs.CDialogDBEdit;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.validation.validationstep.domain.CValidationStep;
import tech.derbent.plm.validation.validationstep.service.CValidationStepService;
import tech.derbent.base.session.service.ISessionService;
import com.vaadin.flow.component.html.Span;

/** CDialogValidationStep - Dialog for adding or editing validation steps.
 * <p>
 * Add mode (isNew = true):
 * - Creates new validation step with order number
 * - Fields: action, expected result, validation data, notes
 * <p>
 * Edit mode (isNew = false):
 * - Edits existing validation step
 * - All fields editable except step order (auto-managed)
 */
public class CDialogValidationStep extends CDialogDBEdit<CValidationStep> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDialogValidationStep.class);
	private static final long serialVersionUID = 1L;

	private final CValidationStepService validationStepService;
	private final ISessionService sessionService;
	private final CEnhancedBinder<CValidationStep> binder;
	private final CFormBuilder<CValidationStep> formBuilder;

	private CTextArea textAreaAction;
	private CTextArea textAreaExpectedResult;
	private CTextField textFieldTestData;
	private CTextArea textAreaNotes;

	/** Constructor for both new and edit modes.
	 * @param validationStepService the validation step service
	 * @param sessionService  the session service
	 * @param step            the validation step entity (new or existing)
	 * @param onSave          callback for save action
	 * @param isNew           true if creating new step, false if editing */
	public CDialogValidationStep(final CValidationStepService validationStepService, final ISessionService sessionService, final CValidationStep step,
			final Consumer<CValidationStep> onSave, final boolean isNew) throws Exception {
		super(step, onSave, isNew);
		Check.notNull(validationStepService, "ValidationStepService cannot be null");
		Check.notNull(sessionService, "SessionService cannot be null");
		Check.notNull(step, "Validation step cannot be null");

		this.validationStepService = validationStepService;
		this.sessionService = sessionService;
		this.binder = CBinderFactory.createEnhancedBinder(CValidationStep.class);
		this.formBuilder = new CFormBuilder<>();

		setupDialog();
		populateForm();
	}

	private void createFormFields() throws Exception {
		Check.notNull(getDialogLayout(), "Dialog layout must be initialized");

		final CVerticalLayout formLayout = new CVerticalLayout();
		formLayout.setPadding(false);
		formLayout.setSpacing(true);

		// Step order display (read-only)
		final Span orderLabel = new Span(
				"Step Order: " + getEntity().getStepOrder());
		orderLabel.getStyle().set("font-size", "0.875rem").set("color", "var(--lumo-secondary-text-color)")
				.set("font-weight", "bold").set("margin-bottom", "1rem");
		formLayout.add(orderLabel);

		// Action text area
		textAreaAction = new CTextArea("Action");
		textAreaAction.setWidthFull();
		textAreaAction.setHeight("120px");
		textAreaAction.setMaxLength(2000);
		textAreaAction.setPlaceholder("Enter the action to perform in this validation step...");
		textAreaAction.setHelperText("Maximum 2000 characters");
		binder.forField(textAreaAction).bind(CValidationStep::getAction, CValidationStep::setAction);
		formLayout.add(textAreaAction);

		// Expected result text area
		textAreaExpectedResult = new CTextArea("Expected Result");
		textAreaExpectedResult.setWidthFull();
		textAreaExpectedResult.setHeight("120px");
		textAreaExpectedResult.setMaxLength(2000);
		textAreaExpectedResult.setPlaceholder("Enter the expected outcome after performing the action...");
		textAreaExpectedResult.setHelperText("Maximum 2000 characters");
		binder.forField(textAreaExpectedResult).bind(CValidationStep::getExpectedResult, CValidationStep::setExpectedResult);
		formLayout.add(textAreaExpectedResult);

		// Validation data text field
		textFieldTestData = new CTextField("Validation Data");
		textFieldTestData.setWidthFull();
		textFieldTestData.setMaxLength(1000);
		textFieldTestData.setPlaceholder("Enter validation data to use (optional)...");
		textFieldTestData.setHelperText("Maximum 1000 characters");
		binder.forField(textFieldTestData).bind(CValidationStep::getTestData, CValidationStep::setTestData);
		formLayout.add(textFieldTestData);

		// Notes text area
		textAreaNotes = new CTextArea("Notes");
		textAreaNotes.setWidthFull();
		textAreaNotes.setHeight("100px");
		textAreaNotes.setMaxLength(2000);
		textAreaNotes.setPlaceholder("Additional notes (optional)...");
		textAreaNotes.setHelperText("Maximum 2000 characters");
		binder.forField(textAreaNotes).bind(CValidationStep::getNotes, CValidationStep::setNotes);
		formLayout.add(textAreaNotes);

		getDialogLayout().add(formLayout);
	}

	@Override
	public String getDialogTitleString() {
		return isNew ? "Add Validation Step" : "Edit Validation Step";
	}

	@Override
	protected Icon getFormIcon() throws Exception {
		return isNew ? VaadinIcon.PLUS.create() : VaadinIcon.EDIT.create();
	}

	@Override
	protected String getFormTitleString() {
		return isNew ? "New Validation Step" : "Edit Validation Step";
	}

	@Override
	protected String getSuccessCreateMessage() {
		return "Validation step added successfully";
	}

	@Override
	protected String getSuccessUpdateMessage() {
		return "Validation step updated successfully";
	}

	@Override
	protected void populateForm() {
		try {
			createFormFields();
			binder.readBean(getEntity());
			LOGGER.debug("Form populated for validation step: {}", getEntity().getId() != null ? getEntity().getId() : "new");
		} catch (final Exception e) {
			LOGGER.error("Error populating form", e);
			CNotificationService.showException("Error loading validation step data", e);
		}
	}

	@Override
	protected void setupContent() throws Exception {
		super.setupContent();
		setWidth("700px");
	}

	@Override
	protected void validateForm() {
		// Validate using binder
		if (!binder.writeBeanIfValid(getEntity())) {
			throw new IllegalStateException("Please correct validation errors");
		}

		// Save validation step
		validationStepService.save(getEntity());

		LOGGER.debug("Validation step validated and saved: {}", getEntity().getId());
	}
}
