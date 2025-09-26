package tech.derbent.api.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Test class to validate the refactoring improvements made to utility classes. Tests the exception handling improvements and Check class usage. */
@DisplayName ("Utility Classes Refactoring Validation")
public class CUtilityRefactoringTest {

	@Test
	@DisplayName ("CImageUtils should throw exceptions instead of returning null")
	void testImageUtilsExceptionHandling() {
		// Test null input throws exception instead of returning null
		assertThrows(IllegalArgumentException.class, () -> {
			CImageUtils.createDataUrl(null);
		}, "CImageUtils.createDataUrl should throw exception for null input");
		// Test empty array throws exception
		assertThrows(IllegalArgumentException.class, () -> {
			CImageUtils.createDataUrl(new byte[0]);
		}, "CImageUtils.createDataUrl should throw exception for empty array");
	}

	@Test
	@DisplayName ("CColorUtils should throw exceptions for invalid operations")
	void testColorUtilsExceptionHandling() {
		// Test createStyledIcon with null input
		assertThrows(IllegalArgumentException.class, () -> {
			CColorUtils.createStyledIcon(null);
		}, "CColorUtils.createStyledIcon should throw exception for null input");
		// Test createStyledIcon with blank input
		assertThrows(IllegalArgumentException.class, () -> {
			CColorUtils.createStyledIcon("   ");
		}, "CColorUtils.createStyledIcon should throw exception for blank input");
	}

	@Test
	@DisplayName ("Check class should be used consistently for validation")
	void testCheckClassUsage() {
		// These should not throw exceptions for valid inputs
		assertDoesNotThrow(() -> {
			Check.notNull("valid string", "test parameter");
		}, "Check.notNull should accept valid strings");
		assertDoesNotThrow(() -> {
			Check.notBlank("valid string", "test parameter");
		}, "Check.notBlank should accept valid strings");
		assertDoesNotThrow(() -> {
			Check.notEmpty(new byte[] {
					1, 2, 3
			}, "test array");
		}, "Check.notEmpty should accept non-empty byte arrays");
	}

	@Test
	@DisplayName ("Base utility patterns should be available")
	void testBaseUtilityPatterns() {
		// Test that CBaseUtils provides common patterns
		String className = CBaseUtils.getSimpleClassName("tech.derbent.api.utils.CBaseUtils");
		assertEquals("CBaseUtils", className, "getSimpleClassName should extract simple class name");
		String errorMessage = CBaseUtils.createErrorMessage("test operation", "test context", null);
		assertTrue(errorMessage.contains("test operation"), "Error message should contain operation");
		assertTrue(errorMessage.contains("test context"), "Error message should contain context");
	}

	@Test
	@DisplayName ("Utility classes should have proper private constructors")
	void testUtilityClassConstructors() {
		// Verify utility classes cannot be instantiated
		assertThrows(Exception.class, () -> {
			CImageUtils.class.getDeclaredConstructor().newInstance();
		}, "CImageUtils should not be instantiable");
		assertThrows(Exception.class, () -> {
			CColorUtils.class.getDeclaredConstructor().newInstance();
		}, "CColorUtils should not be instantiable");
	}
}
