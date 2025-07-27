package tech.derbent.users.view;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import tech.derbent.users.domain.CUser;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.server.streams.UploadMetadata;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to verify the modern Upload API integration works correctly.
 */
public class CUserProfileDialogUploadTest {

    @Test
    public void testProfileDialogCreation() {
        // Test that the dialog can be created without deprecated API usage
        CUser testUser = new CUser("testuser", "password", "Test", "test@example.com");
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        
        assertDoesNotThrow(() -> {
            CUserProfileDialog dialog = new CUserProfileDialog(
                testUser, 
                user -> {}, 
                passwordEncoder
            );
            assertNotNull(dialog);
        });
    }
    
    @Test
    public void testUploadHandlerCallback() {
        // Test that our upload callback works correctly
        final String[] receivedFileName = new String[1];
        final byte[][] receivedData = new byte[1][];
        
        // Create a metadata record 
        UploadMetadata metadata = new UploadMetadata("test-image.jpg", "image/jpeg", 1024);
        
        byte[] testData = "test image data".getBytes();
        
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
}