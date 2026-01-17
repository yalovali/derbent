package tech.derbent.bab.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import tech.derbent.api.utils.Check;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import tech.derbent.Application;


@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles({
		"test", "bab", "reset-db"
})
class CBabDataInitializerTest {

	@Autowired
	private CBabDataInitializer initializer;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void setUpAuthentication() {
		final var auth = new UsernamePasswordAuthenticationToken("test", "test", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
		SecurityContextHolder.getContext().setAuthentication(auth);
	}

	@AfterEach
	void clearAuthentication() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void reloadForced_createsMinimalBabSeed() throws Exception {
		initializer.reloadForced(true);

		final Long companyCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ccompany", Long.class);
		final Long roleCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM cusercompanyrole", Long.class);
		final Long userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM cuser", Long.class);
		final Long projectCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM cproject", Long.class);

		assertEquals(1L, companyCount, "Expected one company");
		assertEquals(1L, roleCount, "Expected one company role");
		assertEquals(1L, userCount, "Expected one user");
		assertEquals(1L, projectCount, "Expected one project for BAB seed");

		final Long companyId = jdbcTemplate.queryForObject("SELECT company_id FROM ccompany", Long.class);
		Check.notNull(companyId, "Company ID must be available");

		final String companyName = jdbcTemplate.queryForObject("SELECT name FROM ccompany WHERE company_id = ?", String.class, companyId);
		assertEquals("BAB Gateway", companyName, "Company name should match BAB Gateway");

		final String email = jdbcTemplate.queryForObject("SELECT email FROM cuser WHERE login = ? AND company_id = ?", String.class, "admin", companyId);
		assertNotNull(email, "Admin user should be created");
		assertEquals("admin@babgateway.local", email, "Admin email should match");

		final String projectName = jdbcTemplate.queryForObject("SELECT name FROM cproject", String.class);
		assertEquals("BAB Gateway Core", projectName, "Project name should match BAB Gateway Core");
	}
}
