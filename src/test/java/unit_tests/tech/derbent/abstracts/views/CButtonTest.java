package unit_tests.tech.derbent.abstracts.views;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;

import tech.derbent.abstracts.utils.Check;
import tech.derbent.abstracts.views.components.CButton;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/**
 * Test class for CButton to verify functionality and inheritance.
 */
class CButtonTest extends CTestBase {

	@Override
	protected void setupForTest() {
		// TODO Auto-generated method stub
	}

	@Test
	void testBasicConstructor() {
		final CButton button = new CButton("Test Button", null, null);
		Check.notNull(button);
		Check.equals("Test Button", button.getText());
	}

	@Test
	void testCreateError() {
		final CButton button = CButton.createError("Error Button", null, null);
		Check.notNull(button);
		Check.equals("Error Button", button.getText());
		Check.condition(
			button.getThemeNames().contains(ButtonVariant.LUMO_ERROR.getVariantName()));
	}

	@Test
	void testCreatePrimary() {
		final CButton button = CButton.createPrimary("Primary Button", null, null);
		Check.notNull(button);
		Check.equals("Primary Button", button.getText());
		Check.condition(
			button.getThemeNames().contains(ButtonVariant.LUMO_PRIMARY.getVariantName()));
	}

	@Test
	void testCreateTertiary() {
		final CButton button = CButton.createTertiary("Tertiary Button", null, null);
		Check.notNull(button);
		Check.equals("Tertiary Button", button.getText());
		Check.condition(button.getThemeNames()
			.contains(ButtonVariant.LUMO_TERTIARY.getVariantName()));
	}

	@Test
	void testCreateWithIcon() {
		final CButton button =
			CButton.createPrimary("Save", VaadinIcon.PLUS.create(), null);
		Check.notNull(button);
		Check.equals("Save", button.getText());
		Check.notNull(button.getIcon());
		Check.condition(
			button.getThemeNames().contains(ButtonVariant.LUMO_PRIMARY.getVariantName()));
	}
}