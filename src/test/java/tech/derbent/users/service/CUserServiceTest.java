package tech.derbent.users.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import tech.derbent.users.domain.CUser;

@ExtendWith(MockitoExtension.class)
class CUserServiceTest {

    @Mock
    private CUserRepository repository;

    @Mock
    private Clock clock;

    private CUserService userService;

    @BeforeEach
    void setUp() {
        userService = new CUserService(repository, clock);
    }

    @Test
    void testLoadUserByUsername_UserExists() {
        // Given
        String username = "testuser";
        String encodedPassword = new BCryptPasswordEncoder().encode("password123");
        CUser user = new CUser(username, encodedPassword, "Test User", "test@example.com", "USER");
        user.setEnabled(true);

        when(repository.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        UserDetails userDetails = userService.loadUserByUsername(username);

        // Then
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals(encodedPassword, userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        // Given
        String username = "nonexistent";
        when(repository.findByUsername(username)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(UsernameNotFoundException.class, 
                () -> userService.loadUserByUsername(username));
    }

    @Test
    void testCreateLoginUser() {
        // Given
        String username = "newuser";
        String plainPassword = "password123";
        String name = "New User";
        String email = "new@example.com";
        String roles = "USER,ADMIN";

        CUser savedUser = new CUser(username, "encodedPassword", name, email, roles);
        // Set ID via reflection to simulate saved entity
        try {
            java.lang.reflect.Field idField = savedUser.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(savedUser, 1L);
        } catch (Exception e) {
            // Ignore for test
        }

        when(repository.findByUsername(username)).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any(CUser.class))).thenReturn(savedUser);

        // When
        CUser result = userService.createLoginUser(username, plainPassword, name, email, roles);

        // Then
        assertNotNull(result);
        verify(repository).findByUsername(username);
        verify(repository).saveAndFlush(any(CUser.class));
    }

    @Test
    void testCreateLoginUser_UsernameAlreadyExists() {
        // Given
        String username = "existinguser";
        CUser existingUser = new CUser();
        when(repository.findByUsername(username)).thenReturn(Optional.of(existingUser));

        // When/Then
        assertThrows(IllegalArgumentException.class, 
                () -> userService.createLoginUser(username, "password", "name", "email", "roles"));
    }
}