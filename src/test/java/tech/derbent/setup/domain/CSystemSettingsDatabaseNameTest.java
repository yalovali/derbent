package tech.derbent.setup.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import tech.derbent.abstracts.domains.CTestBase;

/**
 * Test class to verify database name field validation in CSystemSettings
 */
public class CSystemSettingsDatabaseNameTest extends CTestBase {

	private Validator validator;

	@BeforeEach
	public void setUp() {
		final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testDatabaseNameCannotBeBlank() {
		// Create system settings with blank database name
		final CSystemSettings settings = new CSystemSettings();
		settings.setDatabaseName("   "); // Just whitespace
		// Validate the entity
		final Set<ConstraintViolation<CSystemSettings>> violations =
			validator.validate(settings);
		// Should have validation error for blank database name
		final boolean hasBlankViolation = violations.stream()
			.anyMatch(v -> v.getPropertyPath().toString().equals("databaseName")
				&& v.getMessage().contains("cannot be blank"));
		assertTrue(hasBlankViolation,
			"Should have validation error for blank database name");
	}

	@Test
	public void testDatabaseNameCannotBeNull() {
		// Create system settings with null database name
		final CSystemSettings settings = new CSystemSettings();
		settings.setDatabaseName(null);
		// Validate the entity
		final Set<ConstraintViolation<CSystemSettings>> violations =
			validator.validate(settings);
		// Should have validation error for null database name
		final boolean hasNullViolation = violations.stream()
			.anyMatch(v -> v.getPropertyPath().toString().equals("databaseName")
				&& v.getMessage().contains("cannot be null"));
		assertTrue(hasNullViolation,
			"Should have validation error for null database name");
	}

	@Test
	public void testDefaultDatabaseNameIsSet() {
		// Create new system settings instance
		final CSystemSettings settings = new CSystemSettings();
		// Should have default database name
		assertEquals("derbent", settings.getDatabaseName(),
			"Default database name should be 'derbent'");
	}

	@Test
	public void testValidDatabaseNamePassesValidation() {
		// Create system settings with valid database name
		final CSystemSettings settings = new CSystemSettings();
		settings.setDatabaseName("derbent");
		// Validate the entity
		final Set<ConstraintViolation<CSystemSettings>> violations =
			validator.validate(settings);
		// Should not have validation errors for database name
		final boolean hasDatabaseNameViolation = violations.stream()
			.anyMatch(v -> v.getPropertyPath().toString().equals("databaseName"));
		assertFalse(hasDatabaseNameViolation,
			"Should not have validation error for valid database name");
	}

	@Override
	protected void setupForTest() {
		// TODO Auto-generated method stub
		
	}
}