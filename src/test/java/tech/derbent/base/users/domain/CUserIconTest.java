package tech.derbent.base.users.domain;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import com.vaadin.flow.component.icon.Icon;
import tech.derbent.api.utils.CImageUtils;

/** Test class for CUser icon functionality, specifically testing profile picture thumbnail generation. Validates that thumbnails are automatically
 * created when setting profile pictures and used correctly in getIcon() method. */
class CUserIconTest {

	/** Creates a simple test image as byte array in PNG format.
	 * @param width  Image width in pixels
	 * @param height Image height in pixels
	 * @return Image data as PNG byte array */
	private byte[] createTestImage(final int width, final int height) throws IOException {
		final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		final Graphics2D g2d = image.createGraphics();
		// Fill with a blue color so we can verify the image
		g2d.setColor(Color.BLUE);
		g2d.fillRect(0, 0, width, height);
		g2d.dispose();
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, "png", baos);
		return baos.toByteArray();
	}

	/** Tests that CUser.getIcon() returns default icon when no profile picture is set. */
	@Test
	void testGetIcon_NoProfilePicture_ReturnsDefaultIcon() {
		// Given: A user without profile picture
		final CUser user = new CUser();
		// When: Getting the icon
		final Icon icon = user.getIcon();
		// Then: Should return non-null default icon
		assertNotNull(icon, "Icon should not be null even without profile picture");
		assertEquals(CUser.DEFAULT_ICON, user.getIconString(), "Should return default icon string");
	}

	/** Tests that CUser.getIcon() returns custom icon when profile picture with thumbnail is set. */
	@Test
	void testGetIcon_WithProfilePicture_ReturnsCustomIcon() throws IOException {
		// Given: A user with profile picture
		final CUser user = new CUser();
		final byte[] profilePicture = createTestImage(100, 100);
		// When: Setting profile picture (should auto-generate thumbnail)
		user.setProfilePictureData(profilePicture);
		// Then: Should have generated thumbnail
		assertNotNull(user.getProfilePictureThumbnail(), "Thumbnail should be automatically generated");
		assertTrue(user.getProfilePictureThumbnail().length > 0, "Thumbnail should have content");
		// And: getIcon should return custom icon
		final Icon icon = user.getIcon();
		assertNotNull(icon, "Icon should not be null with profile picture");
		assertEquals("vaadin:user-card", user.getIconString(), "Should return user-card icon string");
	}

	/** Tests that thumbnail is automatically generated with correct size when setting profile picture. */
	@Test
	void testSetProfilePictureData_AutoGeneratesThumbnail() throws IOException {
		// Given: A user and a test image
		final CUser user = new CUser();
		final byte[] originalImage = createTestImage(200, 200);
		// When: Setting profile picture
		user.setProfilePictureData(originalImage);
		// Then: Thumbnail should be generated
		assertNotNull(user.getProfilePictureThumbnail(), "Thumbnail should be generated");
		assertTrue(user.getProfilePictureThumbnail().length > 0, "Thumbnail should not be empty");
		// And: Thumbnail should be resized to icon size
		final BufferedImage thumbnailImage = ImageIO.read(new java.io.ByteArrayInputStream(user.getProfilePictureThumbnail()));
		assertEquals(CUser.ICON_SIZE, thumbnailImage.getWidth(), "Thumbnail width should match ICON_SIZE");
		assertEquals(CUser.ICON_SIZE, thumbnailImage.getHeight(), "Thumbnail height should match ICON_SIZE");
	}

	/** Tests that clearing profile picture also clears thumbnail. */
	@Test
	void testSetProfilePictureData_Null_ClearsThumbnail() throws IOException {
		// Given: A user with profile picture and thumbnail
		final CUser user = new CUser();
		user.setProfilePictureData(createTestImage(100, 100));
		assertNotNull(user.getProfilePictureThumbnail(), "Thumbnail should exist initially");
		// When: Clearing profile picture
		user.setProfilePictureData(null);
		// Then: Thumbnail should be cleared
		assertNull(user.getProfilePictureThumbnail(), "Thumbnail should be null after clearing profile picture");
		// And: Should return default icon
		assertEquals(CUser.DEFAULT_ICON, user.getIconString(), "Should return default icon string after clearing");
	}

	/** Tests that setting empty byte array clears thumbnail. */
	@Test
	void testSetProfilePictureData_EmptyArray_ClearsThumbnail() throws IOException {
		// Given: A user with profile picture
		final CUser user = new CUser();
		user.setProfilePictureData(createTestImage(100, 100));
		assertNotNull(user.getProfilePictureThumbnail(), "Thumbnail should exist initially");
		// When: Setting empty array
		user.setProfilePictureData(new byte[0]);
		// Then: Thumbnail should be cleared
		assertNull(user.getProfilePictureThumbnail(), "Thumbnail should be null with empty profile picture");
	}

	/** Tests that thumbnail generation is resilient to image processing errors. This test verifies that if thumbnail generation fails, the user object
	 * remains valid with null thumbnail. */
	@Test
	void testSetProfilePictureData_InvalidImage_HandlesGracefully() {
		// Given: A user and invalid image data
		final CUser user = new CUser();
		final byte[] invalidImageData = "not an image".getBytes();
		// When: Setting invalid profile picture data
		user.setProfilePictureData(invalidImageData);
		// Then: Should handle gracefully
		assertArrayEquals(invalidImageData, user.getProfilePictureData(), "Original data should be stored");
		assertNull(user.getProfilePictureThumbnail(), "Thumbnail should be null for invalid image");
		// And: getIcon should still work, returning default icon
		final Icon icon = user.getIcon();
		assertNotNull(icon, "Icon should not be null even with invalid profile picture");
		assertEquals(CUser.DEFAULT_ICON, user.getIconString(), "Should return default icon string for invalid image");
	}

	/** Tests that CImageUtils correctly resizes images to icon size. This validates the core resizing functionality used by CUser. */
	@Test
	void testCImageUtils_ResizeImage_CreatesCorrectSize() throws IOException {
		// Given: A large test image
		final byte[] originalImage = createTestImage(200, 200);
		// When: Resizing to icon size
		final byte[] resizedImage = CImageUtils.resizeImage(originalImage, CUser.ICON_SIZE, CUser.ICON_SIZE);
		// Then: Should create correctly sized image
		assertNotNull(resizedImage, "Resized image should not be null");
		assertTrue(resizedImage.length > 0, "Resized image should have content");
		// And: Verify dimensions
		final BufferedImage resultImage = ImageIO.read(new java.io.ByteArrayInputStream(resizedImage));
		assertEquals(CUser.ICON_SIZE, resultImage.getWidth(), "Resized width should match target");
		assertEquals(CUser.ICON_SIZE, resultImage.getHeight(), "Resized height should match target");
	}
}
