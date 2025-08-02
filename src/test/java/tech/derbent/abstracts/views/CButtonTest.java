package tech.derbent.abstracts.views;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;

import tech.derbent.abstracts.domains.CTestBase;

/**
 * Test class for CButton to verify functionality and inheritance.
 */
class CButtonTest extends CTestBase {

	@Test
	void testBasicConstructor() {
		final CButton button = new CButton("Test Button");
		assertNotNull(button);
		assertEquals("Test Button", button.getText());
	}

	@Test
	void testCreateError() {
		final CButton button = CButton.createError("Error Button");
		assertNotNull(button);
		assertEquals("Error Button", button.getText());
		assertTrue(
			button.getThemeNames().contains(ButtonVariant.LUMO_ERROR.getVariantName()));
	}

	@Test
	void testCreatePrimary() {
		final CButton button = CButton.createPrimary("Primary Button");
		assertNotNull(button);
		assertEquals("Primary Button", button.getText());
		assertTrue(
			button.getThemeNames().contains(ButtonVariant.LUMO_PRIMARY.getVariantName()));
	}

	@Test
	void testCreateTertiary() {
		final CButton button = CButton.createTertiary("Tertiary Button");
		assertNotNull(button);
		assertEquals("Tertiary Button", button.getText());
		assertTrue(button.getThemeNames()
			.contains(ButtonVariant.LUMO_TERTIARY.getVariantName()));
	}

	@Test
	void testCreateWithIcon() {
		final CButton button = CButton.createPrimary("Save", VaadinIcon.PLUS.create());
		assertNotNull(button);
		assertEquals("Save", button.getText());
		assertNotNull(button.getIcon());
		assertTrue(
			button.getThemeNames().contains(ButtonVariant.LUMO_PRIMARY.getVariantName()));
	}

	@Override
	protected void setupForTest() {
		// TODO Auto-generated method stub
		
	}
}