package tech.derbent.api.ui.component;

import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import tech.derbent.api.utils.CAuxillaries;

/** CTextField - Enhanced base class for text fields in the application. Layer: View (MVC) Provides common initialization patterns, utility methods
 * for validation, sizing, and styling. Extends Vaadin TextField with application-specific enhancements. */
public class CTextField extends TextField {

	private static final long serialVersionUID = 1L;

	public CTextField() {
		super();
		initComponent();
	}

	public CTextField(final String label) {
		super(label);
		initComponent();
	}

	public static CTextField createEmail(final String label) {
		final CTextField field = new CTextField(label);
		field.setPlaceholder("user@example.com");
		field.setPrefixComponent(VaadinIcon.ENVELOPE.create());
		field.setPattern("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
		field.setErrorMessage("Please enter a valid email address");
		return field;
	}

	public static CTextField createNumeric(final String label) {
		final CTextField field = new CTextField(label);
		field.setPattern("[0-9]*");
		field.setErrorMessage("Please enter only numbers");
		field.setValueChangeMode(ValueChangeMode.EAGER);
		return field;
	}

	public static CTextField createSearch(final String label) {
		final CTextField field = new CTextField(label);
		field.setPlaceholder("Type to search...");
		field.setValueChangeMode(ValueChangeMode.EAGER);
		field.setClearButtonVisible(true);
		return field;
	}

	/** Common initialization for all CTextField instances. */
	private final void initComponent() {
		CAuxillaries.setId(this);
	}
}
