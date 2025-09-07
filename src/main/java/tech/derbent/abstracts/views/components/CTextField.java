package tech.derbent.abstracts.views.components;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import tech.derbent.abstracts.utils.CAuxillaries;

/** CTextField - Enhanced base class for text fields in the application. Layer: View (MVC) Provides common initialization patterns, utility methods
 * for validation, sizing, and styling. Extends Vaadin TextField with application-specific enhancements. */
public class CTextField extends TextField {

	private static final long serialVersionUID = 1L;

	/** Default constructor. */
	public CTextField() {
		super();
		initializeTextField();
	}

	/** Constructor with label. */
	public CTextField(final String label) {
		super(label);
		initializeTextField();
	}

	/** Constructor with label and placeholder. */
	public CTextField(final String label, final String placeholder) {
		super(label, placeholder);
		initializeTextField();
	}

	/** Constructor with label, placeholder, and value. */
	public CTextField(final String label, final String placeholder, final String value) {
		super(label, placeholder);
		setValue(value);
		initializeTextField();
	}

	/** Creates a required text field with label.
	 * @param label the field label
	 * @return new CTextField marked as required */
	public static CTextField createRequired(final String label) {
		final CTextField field = new CTextField(label);
		field.setRequired(true);
		field.setRequiredIndicatorVisible(true);
		return field;
	}

	/** Creates a text field with full width.
	 * @param label the field label
	 * @return new CTextField with full width */
	public static CTextField createFullWidth(final String label) {
		final CTextField field = new CTextField(label);
		field.setWidthFull();
		return field;
	}

	/** Creates a text field for email input.
	 * @param label the field label
	 * @return new CTextField configured for email */
	public static CTextField createEmail(final String label) {
		final CTextField field = new CTextField(label);
		field.setPlaceholder("user@example.com");
		field.setPrefixComponent(createEmailIcon());
		field.setPattern("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
		field.setErrorMessage("Please enter a valid email address");
		return field;
	}

	/** Creates a text field for numeric input.
	 * @param label the field label
	 * @return new CTextField configured for numeric input */
	public static CTextField createNumeric(final String label) {
		final CTextField field = new CTextField(label);
		field.setPattern("[0-9]*");
		field.setErrorMessage("Please enter only numbers");
		field.setValueChangeMode(ValueChangeMode.EAGER);
		return field;
	}

	/** Creates a text field for search input with immediate value change.
	 * @param label the field label
	 * @return new CTextField configured for search */
	public static CTextField createSearch(final String label) {
		final CTextField field = new CTextField(label);
		field.setPlaceholder("Type to search...");
		field.setValueChangeMode(ValueChangeMode.EAGER);
		field.setClearButtonVisible(true);
		return field;
	}

	/** Fluent API for setting field as required.
	 * @param required whether the field is required
	 * @return this field for method chaining */
	public CTextField withRequired(final boolean required) {
		setRequired(required);
		setRequiredIndicatorVisible(required);
		return this;
	}

	/** Fluent API for setting width to full.
	 * @return this field for method chaining */
	public CTextField withWidthFull() {
		setWidthFull();
		return this;
	}

	/** Fluent API for setting placeholder text.
	 * @param placeholder the placeholder text
	 * @return this field for method chaining */
	public CTextField withPlaceholder(final String placeholder) {
		setPlaceholder(placeholder);
		return this;
	}

	/** Fluent API for setting helper text.
	 * @param helperText the helper text
	 * @return this field for method chaining */
	public CTextField withHelperText(final String helperText) {
		setHelperText(helperText);
		return this;
	}

	/** Fluent API for setting error message.
	 * @param errorMessage the error message
	 * @return this field for method chaining */
	public CTextField withErrorMessage(final String errorMessage) {
		setErrorMessage(errorMessage);
		return this;
	}

	/** Fluent API for setting value change mode.
	 * @param mode the value change mode
	 * @return this field for method chaining */
	public CTextField withValueChangeMode(final ValueChangeMode mode) {
		setValueChangeMode(mode);
		return this;
	}

	/** Fluent API for setting clear button visibility.
	 * @param visible whether clear button should be visible
	 * @return this field for method chaining */
	public CTextField withClearButton(final boolean visible) {
		setClearButtonVisible(visible);
		return this;
	}

	/** Fluent API for setting maximum length.
	 * @param maxLength the maximum length
	 * @return this field for method chaining */
	public CTextField withMaxLength(final int maxLength) {
		setMaxLength(maxLength);
		return this;
	}

	/** Fluent API for setting validation pattern.
	 * @param pattern the validation pattern
	 * @return this field for method chaining */
	public CTextField withPattern(final String pattern) {
		setPattern(pattern);
		return this;
	}

	/** Common initialization for all CTextField instances. */
	protected void initializeTextField() {
		CAuxillaries.setId(this);
	}

	/** Creates an email icon for email fields.
	 * @return email icon component */
	private static com.vaadin.flow.component.icon.Icon createEmailIcon() {
		return com.vaadin.flow.component.icon.VaadinIcon.ENVELOPE.create();
	}
}
