package tech.derbent.api.annotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import tech.derbent.api.components.CColorPickerComboBox;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;

/** Unit tests for CFormBuilder color field functionality. */
class CFormBuilderColorFieldTest {

	@Test
	void testCreateColorPicker_WithDefaultValue_SetsValue() throws Exception {
		// Create a mock EntityFieldInfo for a color field
		EntityFieldInfo fieldInfo = createColorFieldInfo("#FF0000", "Test Color");
		@SuppressWarnings ("rawtypes")
		CEnhancedBinder<CTypeEntity> binder = new CEnhancedBinder<>(CTypeEntity.class);
		// Create color picker using reflection (since method is private)
		CColorPickerComboBox colorPicker = createColorPickerViaReflection(fieldInfo, binder);
		assertNotNull(colorPicker);
		assertEquals("#ff0000", colorPicker.getValue().toLowerCase()); // Our implementation converts to lowercase
	}

	@Test
	void testCreateColorPicker_WithInvalidColor_AddsHashPrefix() throws Exception {
		// Create field info with color missing hash prefix
		EntityFieldInfo fieldInfo = createColorFieldInfo("FF0000", "Test Color");
		@SuppressWarnings ("rawtypes")
		CEnhancedBinder<CTypeEntity> binder = new CEnhancedBinder<>(CTypeEntity.class);
		CColorPickerComboBox colorPicker = createColorPickerViaReflection(fieldInfo, binder);
		assertNotNull(colorPicker);
		assertEquals("#ff0000", colorPicker.getValue().toLowerCase());
	}

	@Test
	void testCreateColorPicker_WithNoDefaultValue_SetsBlackDefault() throws Exception {
		EntityFieldInfo fieldInfo = createColorFieldInfo("", "Test Color");
		@SuppressWarnings ("rawtypes")
		CEnhancedBinder<CTypeEntity> binder = new CEnhancedBinder<>(CTypeEntity.class);
		CColorPickerComboBox colorPicker = createColorPickerViaReflection(fieldInfo, binder);
		assertNotNull(colorPicker);
		assertEquals("#000000", colorPicker.getValue());
	}

	@Test
	void testIsLightColor_LightColors_ReturnsTrue() throws Exception {
		// Test the isLightColor helper method using reflection
		assertTrue(invokeIsLightColor("#FFFFFF")); // White
		assertTrue(invokeIsLightColor("#FFFF00")); // Yellow
		assertTrue(invokeIsLightColor("#00FFFF")); // Cyan
	}

	private EntityFieldInfo createColorFieldInfo(String defaultValue, String displayName) {
		return new EntityFieldInfo() {

			@Override
			public String getFieldName() { return "color"; }

			@Override
			public String getDisplayName() { return displayName; }

			@Override
			public String getDefaultValue() { return defaultValue; }

			@Override
			public String getPlaceholder() { return "Select color"; }

			@Override
			public boolean isReadOnly() { return false; }

			@Override
			public boolean isColorField() { return true; }

			@Override
			public String getWidth() { return ""; }

			// Add other required methods with default implementations
			@Override
			public Class<?> getFieldTypeClass() { return String.class; }

			@Override
			public String getJavaType() { return "String"; }

			@Override
			public boolean isRequired() { return false; }

			@Override
			public boolean isPasswordField() { return false; }

			@Override
			public int getMaxLength() { return 7; }
		};
	}

	private CColorPickerComboBox createColorPickerViaReflection(EntityFieldInfo fieldInfo, CEnhancedBinder<?> binder) throws Exception {
		// Access the private createColorPicker method using reflection
		java.lang.reflect.Method method = CFormBuilder.class.getDeclaredMethod("createColorPicker", EntityFieldInfo.class, CEnhancedBinder.class);
		method.setAccessible(true);
		return (CColorPickerComboBox) method.invoke(null, fieldInfo, binder);
	}

	private boolean invokeIsLightColor(String color) throws Exception {
		// Access the private isLightColor method using reflection
		java.lang.reflect.Method method = CFormBuilder.class.getDeclaredMethod("isLightColor", String.class);
		method.setAccessible(true);
		return (Boolean) method.invoke(null, color);
	}
}
