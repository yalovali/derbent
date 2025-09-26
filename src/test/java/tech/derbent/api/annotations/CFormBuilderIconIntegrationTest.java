package tech.derbent.api.annotations;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import com.vaadin.flow.component.combobox.ComboBox;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.page.domain.CPageEntity;
import tech.derbent.screens.service.CEntityFieldService;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;

/** Integration test for icon field functionality with real entity. */
class CFormBuilderIconIntegrationTest {

	@Test
	void testCPageEntity_IconField_CreatesIconComboBox() throws Exception {
		// Get the icon field from CPageEntity
		java.lang.reflect.Field iconField = CPageEntity.class.getDeclaredField("icon");
		assertNotNull(iconField);
		// Create EntityFieldInfo from the field
		EntityFieldInfo fieldInfo = CEntityFieldService.createFieldInfo(iconField);
		assertNotNull(fieldInfo);
		// Verify it's marked as useIcon
		assertTrue(fieldInfo.isUseIcon(), "CPageEntity icon field should be marked as useIcon=true");
		// Create a binder for CPageEntity
		CEnhancedBinder<CPageEntity> binder = new CEnhancedBinder<>(CPageEntity.class);
		// Create the component using CFormBuilder's private method via reflection
		java.lang.reflect.Method createComponentMethod =
				CFormBuilder.class.getDeclaredMethod("createComponentForField", EntityFieldInfo.class, CEnhancedBinder.class);
		createComponentMethod.setAccessible(true);
		com.vaadin.flow.component.Component component = (com.vaadin.flow.component.Component) createComponentMethod.invoke(null, fieldInfo, binder);
		// Verify it's a ComboBox with String type
		assertNotNull(component);
		assertTrue(component instanceof ComboBox, "Icon field should create a ComboBox component");
		@SuppressWarnings ("unchecked")
		ComboBox<String> iconComboBox = (ComboBox<String>) component;
		// Verify it has the correct label
		assertTrue(iconComboBox.getLabel().equals("Icon"));
		// Verify it has a default value
		assertNotNull(iconComboBox.getValue());
		assertTrue(iconComboBox.getValue().startsWith("vaadin:"));
	}
}
