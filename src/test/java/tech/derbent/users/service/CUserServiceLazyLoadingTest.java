package tech.derbent.users.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserType;
import tech.derbent.companies.domain.CCompany;

/**
 * Test class for CUserService lazy loading functionality. Specifically tests
 * the fix for LazyInitializationException with CUserType.
 */
class CUserServiceLazyLoadingTest {

	@Mock
	private CUserRepository repository;
	@Mock
	private Clock clock;
	private CUserService userService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		userService = new CUserService(repository, clock);
	}

	@Test
	void testGetUsesEagerLoadingByDefault() {
		// Given
		final Long userId = 1L;
		final CUser user = new CUser();
		user.setName("Test User");
		user.setLogin("testuser");
		final CUserType userType = new CUserType();
		userType.setName("Test User Type");
		user.setUserType(userType);
		
		final CCompany company = new CCompany();
		company.setName("Test Company");
		user.setCompany(company);
		
		// Mock the repository to return the user with all relationships
		when(repository.findByIdWithAllRelationships(userId)).thenReturn(Optional.of(user));
		// When
		final Optional<CUser> result = userService.get(userId);
		// Then
		assertTrue(result.isPresent(), "User should be found");
		assertNotNull(result.get().getUserType(), "UserType should be loaded");
		assertNotNull(result.get().getCompany(), "Company should be loaded");
		// Test that accessing userType doesn't throw LazyInitializationException
		assertDoesNotThrow(() -> {
			final String userTypeName = result.get().getUserType().getName();
			assertNotNull(userTypeName, "UserType name should be accessible");
		});
		// Test that accessing company doesn't throw LazyInitializationException
		assertDoesNotThrow(() -> {
			final String companyName = result.get().getCompany().getName();
			assertNotNull(companyName, "Company name should be accessible");
		});
	}

	@Test
	void testInitializeLazyFieldsHandlesNullUser() {
		// When/Then - should not throw any exceptions
		assertDoesNotThrow(() -> userService.initializeLazyFields(null));
	}

	@Test
	void testInitializeLazyFieldsHandlesNullUserType() {
		// Given
		final CUser user = new CUser();
		user.setName("Test User");
		user.setUserType(null); // Null userType
		// When/Then - should not throw any exceptions
		assertDoesNotThrow(() -> userService.initializeLazyFields(user));
	}

	@Test
	void testInitializeLazyFieldsHandlesUserType() {
		// Given
		final CUser user = new CUser();
		user.setName("Test User");
		final CUserType userType = mock(CUserType.class);
		user.setUserType(userType);
		// When/Then - should not throw any exceptions
		assertDoesNotThrow(() -> userService.initializeLazyFields(user));
	}

	@Test
	void testInitializeLazyFieldsHandlesCompany() {
		// Given
		final CUser user = new CUser();
		user.setName("Test User");
		final CCompany company = mock(CCompany.class);
		user.setCompany(company);
		// When/Then - should not throw any exceptions
		assertDoesNotThrow(() -> userService.initializeLazyFields(user));
	}

	@Test
	void testInitializeLazyFieldsHandlesAllRelationships() {
		// Given
		final CUser user = new CUser();
		user.setName("Test User");
		final CUserType userType = mock(CUserType.class);
		final CCompany company = mock(CCompany.class);
		user.setUserType(userType);
		user.setCompany(company);
		// When/Then - should not throw any exceptions
		assertDoesNotThrow(() -> userService.initializeLazyFields(user));
	}
}