package unit_tests.tech.derbent.base.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import tech.derbent.base.utils.CImageUtils;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/** Test class for CImageUtils utility methods. Tests image processing, validation, and data URL creation functionality. */
public class CImageUtilsTest extends CTestBase {

	/** Creates a simple test image as byte array. */
	private byte[] createTestImage(final int width, final int height) throws IOException {
		final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		final Graphics2D g2d = image.createGraphics();
		g2d.setColor(Color.BLUE);
		g2d.fillRect(0, 0, width, height);
		g2d.dispose();
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, "jpg", baos);
		return baos.toByteArray();
	}

	@Override
	protected void setupForTest() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testConstants() {
		assertEquals(100, CImageUtils.PROFILE_PICTURE_WIDTH);
		assertEquals(100, CImageUtils.PROFILE_PICTURE_HEIGHT);
		assertEquals(5 * 1024 * 1024, CImageUtils.MAX_IMAGE_SIZE);
		assertArrayEquals(new String[] {
				"jpg", "jpeg", "png", "gif"
		}, CImageUtils.SUPPORTED_FORMATS);
	}

	@Test
	public void testCreateDataUrl() throws IOException {
		// Test with valid image data
		final byte[] testImage = createTestImage(50, 50);
		final String dataUrl = CImageUtils.createDataUrl(testImage);
		assertNotNull(dataUrl);
		assertTrue(dataUrl.startsWith("data:image/jpeg;base64,"));
		// Test with null/empty data
		assertEquals(null, CImageUtils.createDataUrl(null));
		assertEquals(null, CImageUtils.createDataUrl(new byte[0]));
	}

	@Test
	public void testGetDefaultProfilePictureDataUrl() {
		final String defaultUrl = CImageUtils.getDefaultProfilePictureDataUrl();
		assertNotNull(defaultUrl);
		assertTrue(defaultUrl.startsWith("data:image/svg+xml;base64,"));
	}

	@Test
	public void testResizeImage() throws IOException {
		// Create a larger test image
		final byte[] originalImage = createTestImage(200, 200);
		// Resize to smaller dimensions
		final byte[] resizedImage = CImageUtils.resizeImage(originalImage, 100, 100);
		assertNotNull(resizedImage);
		assertTrue(resizedImage.length > 0);
		// Resized image should generally be smaller in file size
		assertTrue(resizedImage.length < originalImage.length);
		// Test invalid parameters
		assertThrows(IllegalArgumentException.class, () -> CImageUtils.resizeImage(null, 100, 100));
		assertThrows(IllegalArgumentException.class, () -> CImageUtils.resizeImage(originalImage, 0, 100));
		assertThrows(IllegalArgumentException.class, () -> CImageUtils.resizeImage(originalImage, 100, -1));
	}

	@Test
	public void testResizeToProfilePicture() throws IOException {
		final byte[] originalImage = createTestImage(300, 300);
		final byte[] profileImage = CImageUtils.resizeToProfilePicture(originalImage);
		assertNotNull(profileImage);
		assertTrue(profileImage.length > 0);
	}

	@Test
	public void testValidateImageData() throws IOException {
		final byte[] validImage = createTestImage(100, 100);
		// Test valid image
		assertDoesNotThrow(() -> CImageUtils.validateImageData(validImage, "test.jpg"));
		assertDoesNotThrow(() -> CImageUtils.validateImageData(validImage, "test.png"));
		assertDoesNotThrow(() -> CImageUtils.validateImageData(validImage, "test.gif"));
		assertDoesNotThrow(() -> CImageUtils.validateImageData(validImage, "test.jpeg"));
		// Test null/empty data
		assertThrows(IllegalArgumentException.class, () -> CImageUtils.validateImageData(null, "test.jpg"));
		assertThrows(IllegalArgumentException.class, () -> CImageUtils.validateImageData(new byte[0], "test.jpg"));
		// Test null/empty filename
		assertThrows(IllegalArgumentException.class, () -> CImageUtils.validateImageData(validImage, null));
		assertThrows(IllegalArgumentException.class, () -> CImageUtils.validateImageData(validImage, ""));
		// Test unsupported format
		assertThrows(IllegalArgumentException.class, () -> CImageUtils.validateImageData(validImage, "test.bmp"));
		assertThrows(IllegalArgumentException.class, () -> CImageUtils.validateImageData(validImage, "test.txt"));
	}

	@Test
	public void testValidateImageDataSize() throws IOException {
		// Create a very small image to test size validation
		final byte[] smallImage = createTestImage(10, 10);
		// Should pass size validation
		assertDoesNotThrow(() -> CImageUtils.validateImageData(smallImage, "test.jpg"));
	}
}
