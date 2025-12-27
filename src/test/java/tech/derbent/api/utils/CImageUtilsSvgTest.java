package tech.derbent.api.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import tech.derbent.api.exceptions.CImageProcessingException;

/** Test class for SVG image handling in CImageUtils. Validates that SVG images are properly detected, validated, and handled during resize
 * operations. */
class CImageUtilsSvgTest {

	/** Creates an invalid SVG (missing closing tag).
	 * @return Invalid SVG data as byte array */
	private static byte[] createInvalidSvgImage() {
		final String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100\" height=\"100\">\n"
				+ "  <circle cx=\"50\" cy=\"50\" r=\"40\" fill=\"green\" />\n";
		// Missing </svg> closing tag
		return svg.getBytes();
	}

	/** Creates a simple SVG without XML declaration.
	 * @return SVG image data as byte array */
	private static byte[] createSimpleSvgImage() {
		final String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100\" height=\"100\">\n"
				+ "  <rect x=\"10\" y=\"10\" width=\"80\" height=\"80\" fill=\"red\" />\n" + "</svg>";
		return svg.getBytes();
	}

	/** Creates a simple valid SVG image as byte array.
	 * @return SVG image data as byte array */
	private static byte[] createValidSvgImage() {
		final String svg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100\" height=\"100\" viewBox=\"0 0 100 100\">\n"
				+ "  <circle cx=\"50\" cy=\"50\" r=\"40\" fill=\"blue\" />\n" + "</svg>";
		return svg.getBytes();
	}

	/** Tests that createDataUrl works correctly with SVG data. */
	@Test
	void testCreateDataUrl_WithSvg_GeneratesCorrectDataUrl() {
		// Given: A valid SVG image
		final byte[] svgData = createValidSvgImage();
		// When: Creating a data URL
		final String dataUrl = CImageUtils.createDataUrl(svgData);
		// Then: Should generate valid SVG data URL
		assertNotNull(dataUrl, "Data URL should not be null");
		assertTrue(dataUrl.startsWith("data:image/svg+xml;base64,"), "Data URL should start with correct SVG MIME type");
		assertTrue(dataUrl.length() > 30, "Data URL should contain base64-encoded SVG data");
	}

	/** Tests that resizeImage handles SVG without XML declaration. */
	@Test
	void testResizeImage_WithSimpleSvg_ReturnsOriginalData() throws IOException {
		// Given: A simple SVG without XML declaration
		final byte[] svgData = createSimpleSvgImage();
		// When: Attempting to resize the SVG
		final byte[] resizedData = CImageUtils.resizeImage(svgData, 100, 100);
		// Then: Should return original SVG data unchanged
		assertNotNull(resizedData, "Resized data should not be null");
		assertArrayEquals(svgData, resizedData, "SVG data should be returned unchanged");
	}

	/** Tests that resizeImage returns original SVG data without modification when input is SVG. Since SVG is vector-based, it doesn't need
	 * resizing. */
	@Test
	void testResizeImage_WithValidSvg_ReturnsOriginalData() throws IOException {
		// Given: A valid SVG image
		final byte[] svgData = createValidSvgImage();
		// When: Attempting to resize the SVG
		final byte[] resizedData = CImageUtils.resizeImage(svgData, 50, 50);
		// Then: Should return original SVG data unchanged
		assertNotNull(resizedData, "Resized data should not be null");
		assertArrayEquals(svgData, resizedData, "SVG data should be returned unchanged (vector format doesn't need resizing)");
	}

	/** Tests that resizeToProfilePicture works with SVG images. */
	@Test
	void testResizeToProfilePicture_WithSvg_ReturnsOriginalData() throws IOException {
		// Given: A valid SVG image
		final byte[] svgData = createValidSvgImage();
		// When: Resizing to profile picture size
		final byte[] resizedData = CImageUtils.resizeToProfilePicture(svgData);
		// Then: Should return original SVG data unchanged
		assertNotNull(resizedData, "Resized data should not be null");
		assertArrayEquals(svgData, resizedData, "SVG should be returned unchanged for profile pictures");
	}

	/** Tests that SVG format is included in supported formats array. */
	@Test
	void testSupportedFormats_IncludesSvg() {
		// Given: The SUPPORTED_FORMATS array
		final String[] supportedFormats = CImageUtils.SUPPORTED_FORMATS;
		// When: Checking for SVG format
		boolean svgSupported = false;
		for (final String format : supportedFormats) {
			if ("svg".equalsIgnoreCase(format)) {
				svgSupported = true;
				break;
			}
		}
		// Then: SVG should be in the supported formats
		assertTrue(svgSupported, "SVG should be in the SUPPORTED_FORMATS array");
	}

	/** Tests that validateImageData rejects invalid SVG data (missing closing tag). */
	@Test
	void testValidateImageData_WithInvalidSvg_ThrowsException() {
		// Given: Invalid SVG data (missing closing tag)
		final byte[] invalidSvgData = createInvalidSvgImage();
		// When/Then: Validation should fail with exception
		final CImageProcessingException exception = assertThrows(CImageProcessingException.class,
				() -> CImageUtils.validateImageData(invalidSvgData, "invalid.svg"), "Invalid SVG should fail validation");
		assertTrue(exception.getMessage().contains("SVG"), "Exception message should mention SVG");
	}

	/** Tests that validateImageData rejects non-SVG file with .svg extension. */
	@Test
	void testValidateImageData_WithNonSvgDataButSvgExtension_ThrowsException() {
		// Given: Non-SVG data with .svg extension
		final byte[] nonSvgData = "This is not SVG data".getBytes();
		// When/Then: Validation should fail
		assertThrows(CImageProcessingException.class, () -> CImageUtils.validateImageData(nonSvgData, "fake.svg"),
				"Non-SVG data with .svg extension should fail validation");
	}

	/** Tests that validateImageData accepts SVG files with .svg extension. */
	@Test
	void testValidateImageData_WithSvgExtension_Succeeds() {
		// Given: A valid SVG image
		final byte[] svgData = createSimpleSvgImage();
		// When/Then: Validation should succeed with .svg extension
		assertDoesNotThrow(() -> CImageUtils.validateImageData(svgData, "profile.svg"), "SVG with .svg extension should pass validation");
	}

	/** Tests that validateImageData accepts valid SVG files. */
	@Test
	void testValidateImageData_WithValidSvg_Succeeds() {
		// Given: A valid SVG image
		final byte[] svgData = createValidSvgImage();
		// When/Then: Validation should succeed without throwing exception
		assertDoesNotThrow(() -> CImageUtils.validateImageData(svgData, "test.svg"), "Valid SVG should pass validation");
	}
}
