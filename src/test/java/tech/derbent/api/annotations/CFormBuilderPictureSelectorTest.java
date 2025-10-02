package tech.derbent.api.annotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import com.vaadin.flow.component.Component;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.views.components.CPictureSelector;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;

/** Unit tests for CFormBuilder picture selector functionality. */
class CFormBuilderPictureSelectorTest {

	/** Test entity with an image data field. */
	public static class TestEntityWithImage extends CEntityDB<TestEntityWithImage> {

		@AMetaData (
				displayName = "Test Image", required = false, readOnly = false, description = "Test image field", hidden = false, order = 1,
				imageData = true
		)
		private byte[] testImage;

		public TestEntityWithImage() {
			super(TestEntityWithImage.class);
		}

		public byte[] getTestImage() { return testImage; }

		public void setTestImage(byte[] testImage) { this.testImage = testImage; }

		@Override
		public void initializeAllFields() {
			// TODO Auto-generated method stub
			
		}
	}

	@Test
	void testCreatePictureSelector_WithImageDataTrue_CreatesCPictureSelector() throws Exception {
		// Create a mock EntityFieldInfo for an image data field
		EntityFieldInfo fieldInfo = createImageDataFieldInfo();
		// Create picture selector directly
		CPictureSelector pictureSelector = new CPictureSelector(fieldInfo);
		assertNotNull(pictureSelector);
		// getValue() should return null for empty value
		assertEquals(null, pictureSelector.getValue());
		assertEquals(null, pictureSelector.getEmptyValue()); // getEmptyValue() returns null for byte[]
	}

	@Test
	void testCreateComponentForField_WithByteArrayAndImageData_CreatesCPictureSelector() throws Exception {
		// Create EntityFieldInfo for byte[] field with imageData=true
		EntityFieldInfo fieldInfo = createImageDataFieldInfo();
		fieldInfo.setFieldTypeClass(byte[].class);
		CEnhancedBinder<TestEntityWithImage> binder = new CEnhancedBinder<>(TestEntityWithImage.class);
		// Call createComponentForField using reflection
		Component component = createComponentForFieldViaReflection(fieldInfo, binder);
		assertNotNull(component);
		assertTrue(component instanceof CPictureSelector, "Component should be CPictureSelector");
	}

	private EntityFieldInfo createImageDataFieldInfo() {
		EntityFieldInfo fieldInfo = new EntityFieldInfo();
		fieldInfo.setFieldName("testImage");
		fieldInfo.setDisplayName("Test Image");
		fieldInfo.setDescription("Test image field");
		fieldInfo.setRequired(false);
		fieldInfo.setReadOnly(false);
		fieldInfo.setHidden(false);
		fieldInfo.setOrder(1);
		fieldInfo.setImageData(true);
		fieldInfo.setWidth("100px");
		fieldInfo.setDefaultValue("");
		fieldInfo.setPlaceholder("");
		return fieldInfo;
	}

	private Component createComponentForFieldViaReflection(EntityFieldInfo fieldInfo, CEnhancedBinder<?> binder) throws Exception {
		Method method = CFormBuilder.class.getDeclaredMethod("createComponentForField", EntityFieldInfo.class, CEnhancedBinder.class);
		method.setAccessible(true);
		return (Component) method.invoke(null, fieldInfo, binder);
	}
}
