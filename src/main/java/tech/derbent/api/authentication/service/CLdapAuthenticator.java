package tech.derbent.api.authentication.service;

import java.util.Hashtable;
import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tech.derbent.api.setup.domain.CSystemSettings;

/** CLdapAuthenticator - LDAP authentication utility using JNDI. Provides LDAP bind authentication for users marked as LDAP users. Uses javax.naming
 * JNDI (built into Java - no external dependencies). Authentication Flow: 1. Read LDAP configuration from system settings 2. Connect to LDAP server
 * 3. Build user DN from username 4. Attempt bind with user credentials 5. Return success/failure Security: - Supports both ldap:// and ldaps://
 * protocols - Connection timeout prevents hanging - Comprehensive logging for troubleshooting - No password logging (security)
 * @author Derbent Team
 * @since 2026-02-10 */
@Component
public class CLdapAuthenticator {

	/** Connection timeout in milliseconds (5 seconds). */
	private static final String CONNECTION_TIMEOUT = "5000";
	private static final Logger LOGGER = LoggerFactory.getLogger(CLdapAuthenticator.class);
	/** Read timeout in milliseconds (5 seconds). */
	private static final String READ_TIMEOUT = "5000";

	/** Authenticate user against LDAP server using bind authentication.
	 * @param username username to authenticate
	 * @param password password to check
	 * @param settings system settings containing LDAP configuration
	 * @return true if authentication successful, false otherwise */
	public boolean authenticate(final String username, final String password, final CSystemSettings<?> settings) {
		// Validation
		if (username == null || username.isBlank()) {
			LOGGER.warn("LDAP authentication failed: username is null or empty");
			return false;
		}
		if (password == null || password.isBlank()) {
			LOGGER.warn("LDAP authentication failed for user '{}': password is null or empty", username);
			return false;
		}
		if (settings == null) {
			LOGGER.error("LDAP authentication failed: system settings is null");
			return false;
		}
		if (!Boolean.TRUE.equals(settings.getEnableLdapAuthentication())) {
			LOGGER.warn("LDAP authentication disabled in system settings for user '{}'", username);
			return false;
		}
		// Get LDAP configuration
		final String serverUrl = settings.getLdapServerUrl();
		final String searchBase = settings.getLdapSearchBase();
		final String userFilter = settings.getLdapUserFilter();
		// Validate configuration
		if (serverUrl == null || serverUrl.isBlank()) {
			LOGGER.error("LDAP authentication failed: server URL not configured");
			return false;
		}
		if (searchBase == null || searchBase.isBlank()) {
			LOGGER.error("LDAP authentication failed: search base not configured");
			return false;
		}
		if (userFilter == null || userFilter.isBlank()) {
			LOGGER.error("LDAP authentication failed: user filter not configured");
			return false;
		}
		LOGGER.debug("Starting LDAP authentication for user '{}' against server: {}", username, serverUrl);
		DirContext ctx = null;
		try {
			// Build user DN from filter
			final String userDn = buildUserDn(username, searchBase, userFilter);
			LOGGER.debug("Attempting LDAP bind for user DN: {}", userDn);
			// Attempt to bind with user credentials
			ctx = createContext(serverUrl, userDn, password);
			// If we reach here, bind was successful
			LOGGER.info("✅ LDAP authentication SUCCESS for user '{}'", username);
			return true;
		} catch (final AuthenticationException e) {
			// Authentication failed - wrong password or user doesn't exist
			LOGGER.warn("❌ LDAP authentication FAILED for user '{}': Invalid credentials", username);
			LOGGER.debug("LDAP authentication exception details", e);
			return false;
		} catch (final CommunicationException e) {
			// Connection to LDAP server failed
			LOGGER.error("❌ LDAP authentication ERROR for user '{}': Cannot connect to LDAP server '{}'", username, serverUrl);
			LOGGER.debug("LDAP communication exception details", e);
			return false;
		} catch (final NamingException e) {
			// Other LDAP errors (invalid DN format, search base not found, etc.)
			LOGGER.error("❌ LDAP authentication ERROR for user '{}': LDAP operation failed", username);
			LOGGER.debug("LDAP naming exception details", e);
			return false;
		} catch (final Exception e) {
			// Unexpected errors
			LOGGER.error("❌ LDAP authentication ERROR for user '{}': Unexpected error", username, e);
			return false;
		} finally {
			// Always close the context
			closeContext(ctx);
		}
	}

	/** Build user DN from username and filter pattern. Examples: - Filter "(uid={0})" + username "john" + base "ou=users,dc=company,dc=com" →
	 * "uid=john,ou=users,dc=company,dc=com" - Filter "(sAMAccountName={0})" + username "john" + base "ou=users,dc=company,dc=com" →
	 * "sAMAccountName=john,ou=users,dc=company,dc=com"
	 * @param username   username to bind
	 * @param searchBase LDAP search base DN
	 * @param userFilter LDAP user filter with {0} placeholder
	 * @return complete user DN */
	private String buildUserDn(final String username, final String searchBase, final String userFilter) {
		// Extract attribute name from filter: (uid={0}) → uid
		String attributeName = "uid"; // Default
		if (userFilter.contains("=")) {
			final int start = userFilter.indexOf('(') + 1;
			final int end = userFilter.indexOf('=');
			if (start > 0 && end > start) {
				attributeName = userFilter.substring(start, end).trim();
			}
		}
		// Build DN: attribute=username,searchBase
		final String userDn = attributeName + "=" + username + "," + searchBase;
		LOGGER.debug("Built user DN from filter '{}': {}", userFilter, userDn);
		return userDn;
	}

	/** Safely close LDAP context.
	 * @param ctx directory context to close (can be null) */
	private void closeContext(final DirContext ctx) {
		if (ctx != null) {
			try {
				ctx.close();
				LOGGER.debug("LDAP context closed successfully");
			} catch (final NamingException e) {
				LOGGER.warn("Failed to close LDAP context", e);
			}
		}
	}

	/** Create LDAP context with specified credentials.
	 * @param serverUrl LDAP server URL
	 * @param userDn    user distinguished name
	 * @param password  user password
	 * @return initialized directory context
	 * @throws NamingException if connection or authentication fails */
	private DirContext createContext(final String serverUrl, final String userDn, final String password) throws NamingException {
		final Hashtable<String, String> env = new Hashtable<>();
		// LDAP context factory
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		// LDAP server URL
		env.put(Context.PROVIDER_URL, serverUrl);
		// Authentication type
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		// User credentials
		env.put(Context.SECURITY_PRINCIPAL, userDn);
		env.put(Context.SECURITY_CREDENTIALS, password);
		// Timeout settings
		env.put("com.sun.jndi.ldap.connect.timeout", CONNECTION_TIMEOUT);
		env.put("com.sun.jndi.ldap.read.timeout", READ_TIMEOUT);
		// Connection pooling (disabled for security)
		env.put("com.sun.jndi.ldap.connect.pool", "false");
		LOGGER.debug("Creating LDAP context for server: {}", serverUrl);
		return new InitialDirContext(env);
	}
}
