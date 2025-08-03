package tech.derbent.users.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import tech.derbent.abstracts.domains.CTestBase;
import tech.derbent.users.domain.CUser;

@ExtendWith (MockitoExtension.class)
class CUserServiceTest extends CTestBase {

	@Override
	protected void setupForTest() {
		// TODO Auto-generated method stub
	}

	@Test
	void testCreateLoginUser() {
		// Given
		final String username = "newuser";
		final String plainPassword = "password123";
		final String name = "New User";
		final String email = "new@example.com";
		final String roles = "USER,ADMIN";
		final CUser savedUser =
			new CUser(username, "encodedPassword", name, email, roles);

		// Set ID via reflection to simulate saved entity
		try {
			final java.lang.reflect.Field idField =
				savedUser.getClass().getSuperclass().getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(savedUser, 1L);
		} catch (final Exception e) {
			// Ignore for test
		}
		when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
		when(userRepository.saveAndFlush(any(CUser.class))).thenReturn(savedUser);
		// When
		final CUser result =
			userService.createLoginUser(username, plainPassword, name, email, roles);
		// Then
		assertNotNull(result);
		verify(userRepository).findByUsername(username);
		verify(userRepository).saveAndFlush(any(CUser.class));
	}

	@Test
	void testCreateLoginUser_UsernameAlreadyExists() {
		// Given
		final String username = "existinguser";
		final CUser existingUser = new CUser(username);
		when(userRepository.findByUsername(username))
			.thenReturn(Optional.of(existingUser));
		// When/Then
		assertThrows(IllegalArgumentException.class, () -> userService
			.createLoginUser(username, "password", "name", "email", "roles"));
	}

	@Test
	void testLoadUserByUsername_UserExists() {
		// Given
		final String username = "testuser";
		final String encodedPassword = new BCryptPasswordEncoder().encode("password123");
		final CUser user =
			new CUser(username, encodedPassword, "Test User", "test@example.com", "USER");
		user.setEnabled(true);
		when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
		// When
		final UserDetails userDetails = userService.loadUserByUsername(username);
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
		final String username = "nonexistent";
		when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
		// When/Then
		assertThrows(UsernameNotFoundException.class,
			() -> userService.loadUserByUsername(username));
	}
}