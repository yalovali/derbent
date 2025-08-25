package unit_tests.tech.derbent.users.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserType;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/** Test class for CUserService lazy loading functionality. Specifically tests the fix for LazyInitializationException with CUserType. */
public class CUserServiceLazyLoadingTest extends CTestBase {

	@Override
	protected void setupForTest() {
		// TODO Auto-generated method stub
	}

	@Test
	void testGetUsesEagerLoadingByDefault() {
		// Given
		final Long userId = 1L;
		final CUser user = new CUser("Test User");
		user.setLogin("testuser");
		final CUserType userType = new CUserType("Test User Type", project);
		user.setUserType(userType);
		final CCompany company = new CCompany("Test Company");
		user.setCompany(company);
		// Mock the repository to return the user using the new eager loading method
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		// When
		final Optional<CUser> result = userService.getById(userId);
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
}
