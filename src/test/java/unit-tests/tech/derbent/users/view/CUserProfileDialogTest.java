package tech.derbent.users.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import tech.derbent.abstracts.domains.CTestBase;
import tech.derbent.users.domain.CUser;

/**
 * Unit tests for CUserProfileDialog. Tests the dialog creation and basic functionality.
 */
@ExtendWith(MockitoExtension.class)
class CUserProfileDialogTest extends CTestBase {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Consumer<CUser> onSave;

    private CUser testUser;

    @Override
    protected void setupForTest() {
        // TODO Auto-generated method stub
    }

    @Test
    void testDialogCreation() {
        // Test that dialog can be created without throwing exceptions
        assertDoesNotThrow(() -> {
            new CUserProfileDialog(testUser, onSave, passwordEncoder);
        });
    }

    @Test
    void testDialogCreationWithNullPasswordEncoder() {
        // Test that dialog creation fails with null password encoder
        assertThrows(IllegalArgumentException.class, () -> {
            new CUserProfileDialog(testUser, onSave, null);
        });
    }

    @Test
    void testDialogCreationWithNullUser() {
        // Test that dialog can be created with null user (handled gracefully)
        assertDoesNotThrow(() -> {
            new CUserProfileDialog(null, onSave, passwordEncoder);
        });
    }

    @Test
    void testFormTitle() {
        final CUserProfileDialog dialog = new CUserProfileDialog(testUser, onSave, passwordEncoder);
        assertEquals("User Profile", dialog.getFormTitle());
    }

    @Test
    void testHeaderTitle() {
        final CUserProfileDialog dialog = new CUserProfileDialog(testUser, onSave, passwordEncoder);
        assertEquals("Edit Profile", dialog.getHeaderTitle());
    }

    @Test
    void testSuccessMessage() {
        final CUserProfileDialog dialog = new CUserProfileDialog(testUser, onSave, passwordEncoder);
        assertEquals("Profile updated successfully", dialog.getSuccessUpdateMessage());
    }
}