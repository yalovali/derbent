package unit_tests.tech.derbent.login.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/**
 * Utility test to generate BCrypt hashes for initial user passwords. This is not a real
 * test but a utility to generate the hashes needed for data.sql
 */
public class PasswordHashGeneratorTest extends CTestBase {

	@Test
	public void generatePasswordHashes() {
		final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		System.out.println("Password hashes for data.sql:");
		System.out.println("'admin' -> " + encoder.encode("admin"));
		System.out.println("'user' -> " + encoder.encode("user"));
		System.out.println("'test123' -> " + encoder.encode("test123"));
	}

	@Override
	protected void setupForTest() {
		// TODO Auto-generated method stub
	}
}