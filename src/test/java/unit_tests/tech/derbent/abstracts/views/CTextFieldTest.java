package unit_tests.tech.derbent.abstracts.views;

import org.junit.jupiter.api.Test;
import com.vaadin.flow.data.value.ValueChangeMode;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.abstracts.views.components.CTextField;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/** Test class for CTextField to verify functionality and inheritance. */
class CTextFieldTest extends CTestBase {

	@Override
	protected void setupForTest() {
		// No specific setup required
	}

	@Test
	void testBasicConstructor() {
		final CTextField field = new CTextField();
		Check.notNull(field);
		Check.notNull(field.getId());
		Check.condition(field.getId().isPresent());
	}

	@Test
	void testConstructorWithLabel() {
		final CTextField field = new CTextField("Test Label");
		Check.notNull(field);
		Check.equals("Test Label", field.getLabel());
	}

	@Test
	void testConstructorWithLabelAndPlaceholder() {
		final CTextField field = new CTextField("Test Label", "Test Placeholder");
		Check.notNull(field);
		Check.equals("Test Label", field.getLabel());
		Check.equals("Test Placeholder", field.getPlaceholder());
	}

	@Test
	void testConstructorWithLabelPlaceholderAndValue() {
		final CTextField field = new CTextField("Test Label", "Test Placeholder", "Test Value");
		Check.notNull(field);
		Check.equals("Test Label", field.getLabel());
		Check.equals("Test Placeholder", field.getPlaceholder());
		Check.equals("Test Value", field.getValue());
	}

	@Test
	void testCreateRequiredField() {
		final CTextField field = CTextField.createRequired("Required Field");
		Check.notNull(field);
		Check.equals("Required Field", field.getLabel());
		Check.condition(field.isRequired());
		Check.condition(field.isRequiredIndicatorVisible());
	}

	@Test
	void testCreateFullWidthField() {
		final CTextField field = CTextField.createFullWidth("Full Width Field");
		Check.notNull(field);
		Check.equals("Full Width Field", field.getLabel());
		Check.equals("100%", field.getWidth());
	}

	@Test
	void testCreateEmailField() {
		final CTextField field = CTextField.createEmail("Email Field");
		Check.notNull(field);
		Check.equals("Email Field", field.getLabel());
		Check.equals("user@example.com", field.getPlaceholder());
		Check.notNull(field.getPrefixComponent());
		Check.equals("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", field.getPattern());
		Check.equals("Please enter a valid email address", field.getErrorMessage());
	}

	@Test
	void testCreateNumericField() {
		final CTextField field = CTextField.createNumeric("Numeric Field");
		Check.notNull(field);
		Check.equals("Numeric Field", field.getLabel());
		Check.equals("[0-9]*", field.getPattern());
		Check.equals("Please enter only numbers", field.getErrorMessage());
		Check.equals(ValueChangeMode.EAGER, field.getValueChangeMode());
	}

	@Test
	void testCreateSearchField() {
		final CTextField field = CTextField.createSearch("Search Field");
		Check.notNull(field);
		Check.equals("Search Field", field.getLabel());
		Check.equals("Type to search...", field.getPlaceholder());
		Check.equals(ValueChangeMode.EAGER, field.getValueChangeMode());
		Check.condition(field.isClearButtonVisible());
	}

	@Test
	void testFluentAPIMethods() {
		final CTextField field = new CTextField().withRequired(true).withWidthFull().withPlaceholder("Test placeholder").withHelperText("Helper text")
				.withErrorMessage("Error message").withValueChangeMode(ValueChangeMode.LAZY).withClearButton(true).withMaxLength(100)
				.withPattern("[a-zA-Z]*");
		Check.notNull(field);
		Check.condition(field.isRequired());
		Check.condition(field.isRequiredIndicatorVisible());
		Check.equals("100%", field.getWidth());
		Check.equals("Test placeholder", field.getPlaceholder());
		Check.equals("Helper text", field.getHelperText());
		Check.equals("Error message", field.getErrorMessage());
		Check.equals(ValueChangeMode.LAZY, field.getValueChangeMode());
		Check.condition(field.isClearButtonVisible());
		Check.equals(100, field.getMaxLength());
		Check.equals("[a-zA-Z]*", field.getPattern());
	}

	@Test
	void testMethodChaining() {
		// Test that fluent API methods return the same instance for chaining
		final CTextField field = new CTextField();
		final CTextField result = field.withRequired(true);
		Check.condition(field == result);
	}

	@Test
	void testIdGeneration() {
		final CTextField field = new CTextField();
		Check.notNull(field.getId());
		Check.condition(field.getId().isPresent());
	}

	@Test
	void testValueChangeMode() {
		final CTextField eagerField = new CTextField().withValueChangeMode(ValueChangeMode.EAGER);
		Check.equals(ValueChangeMode.EAGER, eagerField.getValueChangeMode());
		final CTextField lazyField = new CTextField().withValueChangeMode(ValueChangeMode.LAZY);
		Check.equals(ValueChangeMode.LAZY, lazyField.getValueChangeMode());
	}

	@Test
	void testValidationSettings() {
		final CTextField field = new CTextField().withPattern("\\d+").withErrorMessage("Only numbers allowed").withMaxLength(10);
		Check.equals("\\d+", field.getPattern());
		Check.equals("Only numbers allowed", field.getErrorMessage());
		Check.equals(10, field.getMaxLength());
	}
}
