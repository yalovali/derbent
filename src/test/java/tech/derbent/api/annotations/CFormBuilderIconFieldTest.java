package tech.derbent.api.annotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.vaadin.flow.component.combobox.ComboBox;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;

/** Unit tests for CFormBuilder icon field functionality. */
class CFormBuilderIconFieldTest {

	@Test
	void testCreateIconComboBox_WithDefaultValue_SetsValue() throws Exception {
		// Create a mock EntityFieldInfo for an icon field
		EntityFieldInfo fieldInfo = createIconFieldInfo("vaadin:home", "Test Icon");
		CEnhancedBinder<TestEntityWithIcon> binder = new CEnhancedBinder<>(TestEntityWithIcon.class);
		// Create icon combobox using reflection (since method is private)
		ComboBox<String> iconComboBox = createIconComboBoxViaReflection(fieldInfo, binder);
		assertNotNull(iconComboBox);
		assertEquals("vaadin:home", iconComboBox.getValue());
	}

	@Test
	void testCreateIconComboBox_WithNoDefaultValue_NoSelection() throws Exception {
		EntityFieldInfo fieldInfo = createIconFieldInfo("", "Test Icon");
		CEnhancedBinder<TestEntityWithIcon> binder = new CEnhancedBinder<>(TestEntityWithIcon.class);
		ComboBox<String> iconComboBox = createIconComboBoxViaReflection(fieldInfo, binder);
		assertNotNull(iconComboBox);
		// Should be null when no default value and autoSelectFirst is false
		assertEquals(null, iconComboBox.getValue());
	}

	@Test
	void testCreateIconComboBox_WithAutoSelectFirst_SelectsFirstIcon() throws Exception {
		EntityFieldInfo fieldInfo = createIconFieldInfo("", "Test Icon");
		fieldInfo.setAutoSelectFirst(true);
		CEnhancedBinder<TestEntityWithIcon> binder = new CEnhancedBinder<>(TestEntityWithIcon.class);
		ComboBox<String> iconComboBox = createIconComboBoxViaReflection(fieldInfo, binder);
		assertNotNull(iconComboBox);
		assertNotNull(iconComboBox.getValue());
		assertTrue(iconComboBox.getValue().startsWith("vaadin:"));
	}

	@Test
	void testGetVaadinIconNames_ReturnsValidIcons() throws Exception {
		// Test the getVaadinIconNames helper method using reflection
		List<String> iconNames = invokeGetVaadinIconNames();
		assertNotNull(iconNames);
		assertFalse(iconNames.isEmpty());
		// Check that all icons start with "vaadin:"
		boolean allStartWithVaadin = iconNames.stream().allMatch(name -> name.startsWith("vaadin:"));
		assertTrue(allStartWithVaadin, "All icon names should start with 'vaadin:'");
		// Check for some common icons that we know exist
		assertTrue(iconNames.contains("vaadin:home"));
		assertTrue(iconNames.contains("vaadin:user"));
		assertTrue(iconNames.contains("vaadin:cog"));
		assertTrue(iconNames.size() > 50, "Should have many icons available");
	}

	@Test
	void testIconComboBox_HasCorrectLabel() throws Exception {
		EntityFieldInfo fieldInfo = createIconFieldInfo("", "My Icon Field");
		CEnhancedBinder<TestEntityWithIcon> binder = new CEnhancedBinder<>(TestEntityWithIcon.class);
		ComboBox<String> iconComboBox = createIconComboBoxViaReflection(fieldInfo, binder);
		assertEquals("My Icon Field", iconComboBox.getLabel());
	}

	private EntityFieldInfo createIconFieldInfo(String defaultValue, String displayName) {
		return new EntityFieldInfo() {

			private boolean autoSelectFirst = false;

			@Override
			public String getFieldName() { return "icon"; }

			@Override
			public String getDisplayName() { return displayName; }

			@Override
			public String getDefaultValue() { return defaultValue; }

			@Override
			public String getPlaceholder() { return "Select an icon"; }

			@Override
			public boolean isReadOnly() { return false; }

			@Override
			public boolean isUseIcon() { return true; }

			@Override
			public String getWidth() { return ""; }

			@Override
			public boolean isAllowCustomValue() { return false; }

			@Override
			public boolean isComboboxReadOnly() { return false; }

			@Override
			public boolean isAutoSelectFirst() { return autoSelectFirst; }

			@Override
			public void setAutoSelectFirst(boolean autoSelectFirst) { this.autoSelectFirst = autoSelectFirst; }

			// Add other required methods with default implementations
			@Override
			public Class<?> getFieldTypeClass() { return String.class; }

			@Override
			public String getJavaType() { return "String"; }

			@Override
			public boolean isRequired() { return false; }

			@Override
			public boolean isHidden() { return false; }

			@Override
			public boolean isPasswordField() { return false; }

			@Override
			public boolean isPasswordRevealButton() { return false; }

			@Override
			public boolean isColorField() { return false; }

			@Override
			public boolean isSetBackgroundFromColor() { return false; }

			@Override
			public boolean isUseRadioButtons() { return false; }

			@Override
			public boolean isClearOnEmptyData() { return false; }

			@Override
			public String getDescription() { return "Test icon field"; }

			@Override
			public String getFieldType() { return "String"; }

			@Override
			public int getMaxLength() { return 255; }

			@Override
			public int getOrder() { return 100; }

			@Override
			public String getDataProviderBean() { return ""; }

			@Override
			public String getDataProviderMethod() { return ""; }

			@Override
			public String getDataProviderParamMethod() { return ""; }

			@Override
			public String getDataUpdateMethod() { return ""; }
		};
	}

	@SuppressWarnings ("unchecked")
	private ComboBox<String> createIconComboBoxViaReflection(EntityFieldInfo fieldInfo, CEnhancedBinder<?> binder) throws Exception {
		Method method = CFormBuilder.class.getDeclaredMethod("createIconComboBox", EntityFieldInfo.class, CEnhancedBinder.class);
		method.setAccessible(true);
		return (ComboBox<String>) method.invoke(null, fieldInfo, binder);
	}

	@SuppressWarnings ("unchecked")
	private List<String> invokeGetVaadinIconNames() throws Exception {
		Method method = CFormBuilder.class.getDeclaredMethod("getVaadinIconNames");
		method.setAccessible(true);
		return (List<String>) method.invoke(null);
	}

	// Test entity with icon property for binding tests
	public static class TestEntityWithIcon {

		private String icon;

		public String getIcon() { return icon; }

		public void setIcon(String icon) { this.icon = icon; }
	}
}
