package tech.derbent.api.components;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import com.vaadin.flow.component.html.Image;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;

/** Unit tests for CPictureSelector icon mode functionality. */
class CPictureSelectorIconModeTest {

	private EntityFieldInfo testFieldInfo;

	@BeforeEach
	void setUp() {
		testFieldInfo = createTestFieldInfo();
	}

	@Test
	void testCPictureSelector_DefaultMode_IsFullMode() throws Exception {
		// Create CPictureSelector with default constructor
		CPictureSelector selector = new CPictureSelector(testFieldInfo);
		// In full mode, should have upload controls visible
		assertNotNull(selector);
		assertEquals(3, selector.getContent().getComponentCount()); // image + upload + delete button
	}

	@Test
	void testCPictureSelector_IconMode_OnlyShowsImage() throws Exception {
		// Create CPictureSelector in icon mode
		CPictureSelector selector = new CPictureSelector(testFieldInfo, true);
		// In icon mode, should only show the image
		assertNotNull(selector);
		assertEquals(1, selector.getContent().getComponentCount()); // only image
		// Verify the image has the correct icon styling
		Image image = (Image) selector.getContent().getComponentAt(0);
		assertEquals("40px", image.getWidth());
		assertEquals("40px", image.getHeight());
		// Check that it has circular styling
		String borderRadius = image.getStyle().get("border-radius");
		assertEquals("50%", borderRadius);
	}

	@Test
	void testCPictureSelector_FullMode_ShowsAllControls() throws Exception {
		// Create CPictureSelector in full mode explicitly
		CPictureSelector selector = new CPictureSelector(testFieldInfo, false);
		// In full mode, should show image + upload + delete
		assertNotNull(selector);
		assertEquals(3, selector.getContent().getComponentCount());
		// Verify the image has the correct full mode styling
		Image image = (Image) selector.getContent().getComponentAt(0);
		assertEquals("100px", image.getWidth());
		assertEquals("100px", image.getHeight());
		// Check that it has rounded corners (not circular)
		String borderRadius = image.getStyle().get("border-radius");
		assertEquals("8px", borderRadius);
	}

	@Test
	void testCPictureSelector_ReadOnlyIconMode_ClickableButNoDialog() throws Exception {
		// Create read-only CPictureSelector in icon mode
		testFieldInfo.setReadOnly(true);
		CPictureSelector selector = new CPictureSelector(testFieldInfo, true);
		// Should still show the image
		assertEquals(1, selector.getContent().getComponentCount());
		// Verify it's marked as read-only
		assertTrue(selector.isReadOnly());
		// In read-only mode, cursor should be default
		Image image = (Image) selector.getContent().getComponentAt(0);
		assertEquals("default", image.getStyle().get("cursor"));
	}

	@Test
	void testCPictureSelector_SetValue_UpdatesImageInBothModes() throws Exception {
		// Test both modes
		CPictureSelector fullModeSelector = new CPictureSelector(testFieldInfo, false);
		CPictureSelector iconModeSelector = new CPictureSelector(testFieldInfo, true);
		// Create some test image data
		byte[] testImageData = "test image data".getBytes();
		// Set value on both
		fullModeSelector.setValue(testImageData);
		iconModeSelector.setValue(testImageData);
		// Both should have the same value
		assertArrayEquals(testImageData, fullModeSelector.getValue());
		assertArrayEquals(testImageData, iconModeSelector.getValue());
	}

	private EntityFieldInfo createTestFieldInfo() {
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
}
