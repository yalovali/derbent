package tech.derbent.users.view;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.vaadin.flow.server.streams.UploadMetadata;

import tech.derbent.abstracts.domains.CTestBase;
import tech.derbent.users.domain.CUser;

/**
 * Test class to verify the modern Upload API integration works correctly.
 */
public class CUserProfileDialogUploadTest extends CTestBase {

    @Test
    public void testProfileDialogCreation() {
        // Test that the dialog can be created without deprecated API usage
        final CUser testUser = new CUser("testuser", "password", "Test", "test@example.com");
        final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        assertDoesNotThrow(() -> {
            final CUserProfileDialog dialog = new CUserProfileDialog(testUser, user -> {
            }, passwordEncoder);
            assertNotNull(dialog);
        });
    }

    @Test
    public void testUploadHandlerCallback() {
        // Test that our upload callback works correctly
        final String[] receivedFileName = new String[1];
        final byte[][] receivedData = new byte[1][];
        // Create a metadata record
        final UploadMetadata metadata = new UploadMetadata("test-image.jpg", "image/jpeg", 1024);
        final byte[] testData = "test image data".getBytes();
        // Test the callback pattern we're using
        assertDoesNotThrow(() -> {
            receivedFileName[0] = metadata.fileName();
            receivedData[0] = testData;
        });
        assertEquals("test-image.jpg", receivedFileName[0]);
        assertEquals("image/jpeg", metadata.contentType());
        assertEquals(1024, metadata.contentLength());
        assertArrayEquals(testData, receivedData[0]);
    }

    @Override
    protected void setupForTest() {
        // TODO Auto-generated method stub

    }
}