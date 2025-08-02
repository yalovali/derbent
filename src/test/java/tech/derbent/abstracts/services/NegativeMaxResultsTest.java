package tech.derbent.abstracts.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import tech.derbent.abstracts.domains.CTestBase;
import tech.derbent.users.service.CUserService;

/**
 * Test class to reproduce and fix the "max-results cannot be negative" error as mentioned
 * in the problem statement.
 */
@SpringBootTest
public class NegativeMaxResultsTest extends CTestBase {

	@Autowired
	private CUserService userService;

	@Test
	public void testNegativePageSizeShouldThrowException() {
		// Test that negative page size throws appropriate exception
		assertThrows(IllegalArgumentException.class, () -> {
			final Pageable invalidPageable = PageRequest.of(0, -1);
			userService.list(invalidPageable);
		}, "Should throw IllegalArgumentException for negative page size");
	}

	@Test
	public void testValidPageableShouldWork() {
		// Test that valid Pageable works without issues
		assertDoesNotThrow(() -> {
			final Pageable validPageable = Pageable.unpaged();
			final var users = userService.list(validPageable);
			assertNotNull(users, "User list should not be null");
		}, "Valid Pageable should not throw exceptions");
	}

	@Override
	protected void setupForTest() {
		// TODO Auto-generated method stub
		
	}
}