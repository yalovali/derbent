package tech.derbent.screens.view;

import java.util.function.Consumer;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.components.CBinderFactory;
import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.views.CDBEditDialog;
import tech.derbent.screens.domain.CScreen;
import tech.derbent.screens.domain.CScreenLines;
import tech.derbent.screens.service.CEntityFieldService;

/**
 * Dialog for editing screen field descriptions (CScreenLines entities). Extends
 * CDBEditDialog to provide a consistent dialog experience.
 */
public class CScreenLinesEditDialog extends CDBEditDialog<CScreenLines> {

	private static final long serialVersionUID = 1L;

	private final CEntityFieldService entityFieldService;

	private final CEnhancedBinder<CScreenLines> binder;

	private final CScreen screen;

	public CScreenLinesEditDialog(final CScreenLines screenLine,
		final Consumer<CScreenLines> onSave, final boolean isNew,
		final CEntityFieldService entityFieldService, final CScreen screen) {
		super(screenLine, onSave, isNew);
		this.entityFieldService = entityFieldService;
		this.binder = CBinderFactory.createEnhancedBinder(CScreenLines.class);
		this.screen = screen;
		setupDialog();
		createFormFields();
		populateForm();
	}

	private void createFormFields() {
		// Use CEntityFormBuilder to create the form automatically based on @MetaData annotations
		// The entity base type is fixed and comes from screen.getEntityType() parameter
		final Div formContent = CEntityFormBuilder.buildForm(CScreenLines.class, binder);
		formLayout.add(formContent);
	}

	@Override
	protected Icon getFormIcon() { return VaadinIcon.EDIT.create(); }

	@Override
	protected String getFormTitle() {
		return isNew ? "Add Screen Field" : "Edit Screen Field";
	}

	@Override
	public String getHeaderTitle() { return getFormTitle(); }

	@Override
	protected String getSuccessCreateMessage() {
		return "Screen field added successfully";
	}

	@Override
	protected String getSuccessUpdateMessage() {
		return "Screen field updated successfully";
	}

	@Override
	protected void populateForm() {
		if (data != null) {
			binder.readBean(data);
		}
	}

	@Override
	protected void setupDialog() {
		super.setupDialog(); // Call parent setup first
		setWidth("600px");
		setHeight("700px");
		setResizable(true);
	}

	@Override
	protected void validateForm() {
		if (!binder.isValid()) {
			throw new IllegalStateException(
				"Please fill in all required fields correctly");
		}
		// Write bean data back to entity
		binder.writeBeanIfValid(data);
	}
}