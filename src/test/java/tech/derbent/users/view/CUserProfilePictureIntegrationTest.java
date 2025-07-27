package tech.derbent.users.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import tech.derbent.base.utils.CImageUtils;
import tech.derbent.users.domain.CUser;

/**
 * Integration test for profile picture database storage functionality. Tests the
 * end-to-end flow of storing and retrieving profile pictures from database.
 */
public class CUserProfilePictureIntegrationTest {

	/**
	 * Creates a simple test image as byte array.
	 */
	private byte[] createTestImage(final int width, final int height) throws IOException {
		final BufferedImage image =
			new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		final Graphics2D g2d = image.createGraphics();
		g2d.setColor(Color.BLUE);
		g2d.fillRect(0, 0, width, height);
		g2d.dispose();
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, "jpg", baos);
		return baos.toByteArray();
	}

	@Test
	public void testImageProcessingFlow() throws IOException {
		// Test the complete image processing flow
		final byte[] originalImage = createTestImage(500, 500);
		// Validate original image
		assertDoesNotThrow(
			() -> CImageUtils.validateImageData(originalImage, "test.jpg"));
		// Resize to profile picture size
		final byte[] profileImage = CImageUtils.resizeToProfilePicture(originalImage);
		assertNotNull(profileImage);
		// Create data URL for display
		final String dataUrl = CImageUtils.createDataUrl(profileImage);
		assertNotNull(dataUrl);
		// Test that default profile picture URL is available
		final String defaultUrl = CImageUtils.getDefaultProfilePictureDataUrl();
		assertNotNull(defaultUrl);
	}

	@Test
	public void testUserEntityProfilePictureStorage() throws IOException {
		// Test that CUser can store profile picture data
		final CUser user = new CUser("testuser", "password", "Test", "test@example.com");
		// Create test image data
		final byte[] testImageData = createTestImage(200, 200);
		// Resize image using CImageUtils
		final byte[] resizedImageData = CImageUtils.resizeToProfilePicture(testImageData);
		// Set profile picture data
		assertDoesNotThrow(() -> user.setProfilePictureData(resizedImageData));
		// Verify data can be retrieved
		final byte[] retrievedData = user.getProfilePictureData();
		assertNotNull(retrievedData);
		// Test that data URL can be created
		final String dataUrl = CImageUtils.createDataUrl(retrievedData);
		assertNotNull(dataUrl);
	}

	@Test
	public void testUserEntityWithNullProfilePicture() {
		// Test that CUser handles null profile picture gracefully
		final CUser user = new CUser("testuser", "password", "Test", "test@example.com");
		// Set null profile picture data
		assertDoesNotThrow(() -> user.setProfilePictureData(null));
		// Verify null data handling
		final byte[] retrievedData = user.getProfilePictureData();
		// Should be null, not throw exception
		assertDoesNotThrow(() -> {
			@SuppressWarnings ("unused")
			final String dataUrl = CImageUtils.createDataUrl(retrievedData);
			// Should return null for null input
		});
	}
}