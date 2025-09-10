package unit_tests.tech.derbent.screens.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.screens.view.CFieldSelectionComponent;
import tech.derbent.screens.view.CFieldSelectionComponent.FieldSelection;

/** Unit tests for CFieldSelectionComponent to verify field selection and ordering functionality. */
class CFieldSelectionComponentTest {

	private CFieldSelectionComponent component;

	@BeforeEach
	void setUp() {
		component = new CFieldSelectionComponent();
	}

	@Test
	void testComponentInitialization() {
		assertNotNull(component, "Component should be initialized");
	}

	@Test
	void testGetSelectedFieldsAsStringWhenEmpty() {
		String result = component.getSelectedFieldsAsString();
		assertEquals("", result, "Empty selection should return empty string");
	}

	@Test
	void testSetSelectedFieldsFromStringWhenEmpty() {
		component.setSelectedFieldsFromString("");
		List<FieldSelection> selections = component.getSelectedFields();
		assertTrue(selections.isEmpty(), "Empty string should result in empty selections");
	}

	@Test
	void testSetSelectedFieldsFromStringWhenNull() {
		component.setSelectedFieldsFromString(null);
		List<FieldSelection> selections = component.getSelectedFields();
		assertTrue(selections.isEmpty(), "Null string should result in empty selections");
	}

	@Test
	void testFieldSelectionToString() {
		// Create mock field info
		EntityFieldInfo fieldInfo = new EntityFieldInfo();
		fieldInfo.setFieldName("testField");
		fieldInfo.setDisplayName("Test Field");
		FieldSelection selection = new FieldSelection(fieldInfo, 1);
		assertEquals("Test Field (testField)", selection.toString(), "FieldSelection toString should show display name and field name");
	}

	@Test
	void testFieldSelectionOrder() {
		EntityFieldInfo fieldInfo = new EntityFieldInfo();
		fieldInfo.setFieldName("testField");
		fieldInfo.setDisplayName("Test Field");
		FieldSelection selection = new FieldSelection(fieldInfo, 5);
		assertEquals(5, selection.getOrder(), "Field selection should maintain order");
		selection.setOrder(10);
		assertEquals(10, selection.getOrder(), "Field selection order should be updatable");
	}

	@Test
	void testFieldSelectionFieldInfo() {
		EntityFieldInfo fieldInfo = new EntityFieldInfo();
		fieldInfo.setFieldName("testField");
		fieldInfo.setDisplayName("Test Field");
		FieldSelection selection = new FieldSelection(fieldInfo, 1);
		assertEquals(fieldInfo, selection.getFieldInfo(), "Field selection should maintain field info reference");
		assertEquals("testField", selection.getFieldInfo().getFieldName(), "Field name should be accessible");
		assertEquals("Test Field", selection.getFieldInfo().getDisplayName(), "Display name should be accessible");
	}
}
