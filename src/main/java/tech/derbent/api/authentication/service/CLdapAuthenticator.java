package tech.derbent.api.authentication.service;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
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

	/** LDAP Test Result - Structured result for testing operations. */
	public static class CLdapTestResult {

		public static CLdapTestResult failure(final String message, final String details, final long durationMs) {
			return new CLdapTestResult(false, message, details, null, durationMs);
		}

		public static CLdapTestResult success(final String message, final List<String> userData, final long durationMs) {
			return new CLdapTestResult(true, message, null, userData, durationMs);
		}

		private final String details;
		private final long durationMs;
		private final String message;
		private final boolean success;
		private final List<String> userData;

		public CLdapTestResult(final boolean success, final String message, final String details, final List<String> userData,
				final long durationMs) {
			this.success = success;
			this.message = Objects.requireNonNull(message, "Message cannot be null");
			this.details = details;
			this.userData = userData != null ? new ArrayList<>(userData) : new ArrayList<>();
			this.durationMs = durationMs;
		}

		public String getDetails() { return details; }

		public long getDurationMs() { return durationMs; }

		public String getMessage() { return message; }

		public List<String> getUserData() { return new ArrayList<>(userData); }

		public boolean isSuccess() { return success; }

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append(success ? "✅ SUCCESS" : "❌ FAILURE");
			sb.append(": ").append(message);
			if (details != null) {
				sb.append(" (").append(details).append(")");
			}
			sb.append(" [").append(durationMs).append("ms]");
			if (!userData.isEmpty()) {
				sb.append(" - Found ").append(userData.size()).append(" users");
			}
			return sb.toString();
		}
	}

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
		LOGGER.debug("🔐 Starting LDAP authentication for user: {}", username);
		final long startTime = System.currentTimeMillis();
		// Fail-fast validation with detailed logging
		Objects.requireNonNull(username, "Username cannot be null");
		Objects.requireNonNull(password, "Password cannot be null");
		Objects.requireNonNull(settings, "System settings cannot be null");
		if (username.isBlank()) {
			LOGGER.warn("❌ LDAP authentication failed: username is blank");
			return false;
		}
		if (password.isBlank()) {
			LOGGER.warn("❌ LDAP authentication failed for user '{}': password is blank", username);
			return false;
		}
		if (!Boolean.TRUE.equals(settings.getEnableLdapAuthentication())) {
			LOGGER.warn("❌ LDAP authentication disabled in system settings for user '{}'", username);
			return false;
		}
		LOGGER.debug("🔍 LDAP authentication validation passed for user: {}", username);
		// Get LDAP configuration with fail-fast validation
		final String serverUrl = settings.getLdapServerUrl();
		final String searchBase = settings.getLdapSearchBase();
		final String userFilter = settings.getLdapUserFilter();
		// Validate configuration with detailed error messages
		Objects.requireNonNull(serverUrl, "LDAP Server URL cannot be null");
		Objects.requireNonNull(searchBase, "LDAP Search Base cannot be null");
		Objects.requireNonNull(userFilter, "LDAP User Filter cannot be null");
		if (serverUrl.isBlank()) {
			LOGGER.error("❌ LDAP authentication failed: server URL is blank");
			return false;
		}
		if (searchBase.isBlank()) {
			LOGGER.error("❌ LDAP authentication failed: search base is blank");
			return false;
		}
		if (userFilter.isBlank()) {
			LOGGER.error("❌ LDAP authentication failed: user filter is blank");
			return false;
		}
		// Log LDAP configuration (without sensitive data)
		LOGGER.debug("🔧 LDAP Configuration - Server: {}, SearchBase: {}, UserFilter: {}", serverUrl, searchBase, userFilter);
		LOGGER.debug("🚀 Proceeding with LDAP authentication for user '{}' against server: {}", username, serverUrl);
		DirContext ctx = null;
		try {
			// Build user DN from filter
			final String userDn = buildUserDn(username, searchBase, userFilter);
			LOGGER.debug("🔗 Attempting LDAP bind for user DN: {}", userDn);
			// Attempt to bind with user credentials
			final long bindStart = System.currentTimeMillis();
			ctx = createContext(serverUrl, userDn, password, settings);
			final long bindDuration = System.currentTimeMillis() - bindStart;
			// If we reach here, bind was successful
			final long totalDuration = System.currentTimeMillis() - startTime;
			LOGGER.info("✅ LDAP authentication SUCCESS for user '{}' (bind: {}ms, total: {}ms)", username, bindDuration, totalDuration);
			return true;
		} catch (final AuthenticationException e) {
			// Authentication failed - wrong password or user doesn't exist
			final long totalDuration = System.currentTimeMillis() - startTime;
			LOGGER.warn("❌ LDAP authentication FAILED for user '{}': Invalid credentials ({}ms)", username, totalDuration);
			LOGGER.debug("LDAP authentication exception details", e);
			return false;
		} catch (final CommunicationException e) {
			// Connection to LDAP server failed
			final long totalDuration = System.currentTimeMillis() - startTime;
			LOGGER.error("❌ LDAP authentication ERROR for user '{}': Cannot connect to LDAP server '{}' ({}ms)", username, serverUrl, totalDuration);
			LOGGER.debug("LDAP communication exception details", e);
			return false;
		} catch (final NamingException e) {
			// Other LDAP errors (invalid DN format, search base not found, etc.)
			final long totalDuration = System.currentTimeMillis() - startTime;
			LOGGER.error("❌ LDAP authentication ERROR for user '{}': LDAP operation failed ({}ms) - {}", username, totalDuration, e.getMessage());
			LOGGER.debug("LDAP naming exception details", e);
			return false;
		} catch (final Exception e) {
			// Unexpected errors
			final long totalDuration = System.currentTimeMillis() - startTime;
			LOGGER.error("❌ LDAP authentication ERROR for user '{}': Unexpected error ({}ms)", username, totalDuration, e);
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

	/** Create anonymous LDAP context for testing.
	 * @param serverUrl LDAP server URL
	 * @param settings  system settings for additional LDAP configuration
	 * @return initialized directory context
	 * @throws NamingException if connection fails */
	private DirContext createAnonymousContext(final String serverUrl, final CSystemSettings<?> settings) throws NamingException {
		final Hashtable<String, String> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, serverUrl);
		env.put(Context.SECURITY_AUTHENTICATION, "none");
		
		// LDAP version support
		final Integer ldapVersion = settings.getLdapVersion();
		if (ldapVersion != null && (ldapVersion == 2 || ldapVersion == 3)) {
			env.put("java.naming.ldap.version", ldapVersion.toString());
			LOGGER.trace("Using LDAP version: {}", ldapVersion);
		}
		
		// SSL/TLS support  
		final Boolean useSslTls = settings.getLdapUseSslTls();
		if (Boolean.TRUE.equals(useSslTls)) {
			if (serverUrl.startsWith("ldap://")) {
				env.put(Context.SECURITY_PROTOCOL, "ssl");
				LOGGER.debug("Enabling StartTLS for anonymous LDAP connection");
			}
		}
		
		env.put("com.sun.jndi.ldap.connect.timeout", CONNECTION_TIMEOUT);
		env.put("com.sun.jndi.ldap.read.timeout", READ_TIMEOUT);
		env.put("com.sun.jndi.ldap.connect.pool", "false");
		LOGGER.debug("Creating anonymous LDAP context for server: {} (SSL/TLS: {}, Version: {})", 
			serverUrl, useSslTls, ldapVersion);
		return new InitialDirContext(env);
	}

	/** Create LDAP context with specified credentials.
	 * @param serverUrl LDAP server URL
	 * @param userDn    user distinguished name
	 * @param password  user password
	 * @param settings  system settings for additional LDAP configuration
	 * @return initialized directory context
	 * @throws NamingException if connection or authentication fails */
	private DirContext createContext(final String serverUrl, final String userDn, final String password, final CSystemSettings<?> settings) throws NamingException {
		final Hashtable<String, String> env = new Hashtable<>();
		// LDAP context factory
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		// LDAP server URL
		env.put(Context.PROVIDER_URL, serverUrl);
		// LDAP version (new field support)
		final Integer ldapVersion = settings.getLdapVersion();
		if (ldapVersion != null && (ldapVersion == 2 || ldapVersion == 3)) {
			env.put("java.naming.ldap.version", ldapVersion.toString());
			LOGGER.trace("Using LDAP version: {}", ldapVersion);
		}
		
		// SSL/TLS support (new field support)
		final Boolean useSslTls = settings.getLdapUseSslTls();
		if (Boolean.TRUE.equals(useSslTls)) {
			if (serverUrl.startsWith("ldap://")) {
				env.put(Context.SECURITY_PROTOCOL, "ssl");
				LOGGER.debug("Enabling StartTLS for LDAP connection");
			}
			// For ldaps:// URLs, SSL is automatic
		}
		
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
		LOGGER.debug("Creating LDAP context for server: {} (SSL/TLS: {}, Version: {})", 
			serverUrl, useSslTls, ldapVersion);
		return new InitialDirContext(env);
	}

	/** Fetch all users from LDAP directory for testing.
	 * @param settings system settings containing LDAP configuration
	 * @return test result with list of found users */
	public CLdapTestResult fetchAllUsers(final CSystemSettings<?> settings) {
		LOGGER.info("🧪 Fetching all LDAP users...");
		final long startTime = System.currentTimeMillis();
		try {
			// Validate configuration
			Objects.requireNonNull(settings, "System settings cannot be null");
			if (!Boolean.TRUE.equals(settings.getEnableLdapAuthentication())) {
				final long duration = System.currentTimeMillis() - startTime;
				return CLdapTestResult.failure("LDAP authentication is disabled", null, duration);
			}
			final String serverUrl = settings.getLdapServerUrl();
			final String bindDn = settings.getLdapBindDn();
			final String bindPassword = settings.getLdapBindPassword();
			final String searchBase = settings.getLdapSearchBase();
			final String userFilter = settings.getLdapUserFilter();
			// Validate required fields
			if (serverUrl == null || serverUrl.isBlank()) {
				final long duration = System.currentTimeMillis() - startTime;
				return CLdapTestResult.failure("LDAP Server URL is not configured", null, duration);
			}
			if (searchBase == null || searchBase.isBlank()) {
				final long duration = System.currentTimeMillis() - startTime;
				return CLdapTestResult.failure("LDAP Search Base is not configured", null, duration);
			}
			LOGGER.debug("🔍 Searching for users in LDAP base: {}", searchBase);
			DirContext ctx = null;
			try {
				// Connect to LDAP
				if (bindDn != null && !bindDn.isBlank()) {
					ctx = createContext(serverUrl, bindDn, bindPassword != null ? bindPassword : "", settings);
					LOGGER.debug("✅ LDAP bind successful for user search");
				} else {
					ctx = createAnonymousContext(serverUrl, settings);
					LOGGER.debug("✅ LDAP anonymous connection for user search");
				}
				// Prepare search
				final SearchControls searchControls = new SearchControls();
				searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
				searchControls.setReturningAttributes(new String[] {
						"cn", "uid", "sAMAccountName", "mail", "displayName"
				});
				// Build search filter (remove {0} placeholder and make it more general)
				String searchFilter = "(objectClass=person)";
				if (userFilter != null && !userFilter.isBlank()) {
					// Extract attribute from user filter for general search
					if (userFilter.contains("uid=")) {
						searchFilter = "(objectClass=posixAccount)";
					} else if (userFilter.contains("sAMAccountName=")) {
						searchFilter = "(objectClass=user)";
					}
				}
				LOGGER.debug("🔍 Using search filter: {} in base: {}", searchFilter, searchBase);
				// Perform search
				final NamingEnumeration<SearchResult> results = ctx.search(searchBase, searchFilter, searchControls);
				final List<String> users = new ArrayList<>();
				while (results.hasMore()) {
					final SearchResult result = results.next();
					final Attributes attrs = result.getAttributes();
					// Extract user information
					final String cn = getAttributeValue(attrs, "cn");
					final String uid = getAttributeValue(attrs, "uid");
					final String sAMAccountName = getAttributeValue(attrs, "sAMAccountName");
					final String mail = getAttributeValue(attrs, "mail");
					final String displayName = getAttributeValue(attrs, "displayName");
					// Build user info string
					final StringBuilder userInfo = new StringBuilder();
					if (uid != null) {
						userInfo.append("uid=").append(uid);
					} else if (sAMAccountName != null) {
						userInfo.append("sAM=").append(sAMAccountName);
					}
					if (cn != null && !cn.equals(uid) && !cn.equals(sAMAccountName)) {
						if (userInfo.length() > 0) {
							userInfo.append(", ");
						}
						userInfo.append("cn=").append(cn);
					}
					if (displayName != null) {
						if (userInfo.length() > 0) {
							userInfo.append(", ");
						}
						userInfo.append("name=").append(displayName);
					}
					if (mail != null) {
						if (userInfo.length() > 0) {
							userInfo.append(", ");
						}
						userInfo.append("mail=").append(mail);
					}
					users.add(userInfo.toString());
				}
				results.close();
				final long duration = System.currentTimeMillis() - startTime;
				LOGGER.info("✅ Found {} LDAP users in {}ms", users.size(), duration);
				return CLdapTestResult.success("Found " + users.size() + " users", users, duration);
			} finally {
				closeContext(ctx);
			}
		} catch (final NamingException e) {
			final long duration = System.currentTimeMillis() - startTime;
			LOGGER.error("❌ LDAP user search failed: {}", e.getMessage());
			return CLdapTestResult.failure("LDAP user search failed", e.getMessage(), duration);
		} catch (final Exception e) {
			final long duration = System.currentTimeMillis() - startTime;
			LOGGER.error("❌ LDAP user fetch failed with unexpected error", e);
			return CLdapTestResult.failure("Unexpected error", e.getMessage(), duration);
		}
	}

	/** Get attribute value from LDAP attributes.
	 * @param attrs         LDAP attributes
	 * @param attributeName attribute name to get
	 * @return attribute value or null if not found */
	private String getAttributeValue(final Attributes attrs, final String attributeName) {
		try {
			final var attr = attrs.get(attributeName);
			if (attr != null && attr.get() != null) {
				return attr.get().toString();
			}
		} catch (final NamingException e) {
			LOGGER.debug("Failed to get attribute '{}': {}", attributeName, e.getMessage());
		}
		return null;
	}

	/** Test LDAP connection and configuration.
	 * @param settings system settings containing LDAP configuration
	 * @return test result with detailed information */
	public CLdapTestResult testConnection(final CSystemSettings<?> settings) {
		LOGGER.info("🧪 Testing LDAP connection...");
		final long startTime = System.currentTimeMillis();
		
		// Declare variables that will be used in catch blocks
		String serverUrl = null;
		String bindDn = null;
		
		try {
			// Fail-fast validation
			Objects.requireNonNull(settings, "System settings cannot be null");
			if (!Boolean.TRUE.equals(settings.getEnableLdapAuthentication())) {
				final long duration = System.currentTimeMillis() - startTime;
				return CLdapTestResult.failure("LDAP authentication is disabled", "Enable LDAP Authentication in system settings", duration);
			}
			serverUrl = settings.getLdapServerUrl();
			bindDn = settings.getLdapBindDn();
			final String bindPassword = settings.getLdapBindPassword();
			final String searchBase = settings.getLdapSearchBase();
			// Validate required fields
			if (serverUrl == null || serverUrl.isBlank()) {
				final long duration = System.currentTimeMillis() - startTime;
				return CLdapTestResult.failure("LDAP Server URL is not configured", null, duration);
			}
			if (searchBase == null || searchBase.isBlank()) {
				final long duration = System.currentTimeMillis() - startTime;
				return CLdapTestResult.failure("LDAP Search Base is not configured", null, duration);
			}
			LOGGER.debug("🔧 Testing LDAP connection to server: {} with base: {}", serverUrl, searchBase);
			// Test connection with bind DN (if provided) or anonymous
			DirContext ctx = null;
			try {
				if (bindDn != null && !bindDn.isBlank()) {
					// Test with bind DN
					ctx = createContext(serverUrl, bindDn, bindPassword != null ? bindPassword : "", settings);
					LOGGER.debug("✅ LDAP bind successful with DN: {}", bindDn);
				} else {
					// Test anonymous connection
					ctx = createAnonymousContext(serverUrl, settings);
					LOGGER.debug("✅ LDAP anonymous connection successful");
				}
				final long duration = System.currentTimeMillis() - startTime;
				return CLdapTestResult.success("LDAP connection successful", null, duration);
			} finally {
				closeContext(ctx);
			}
		} catch (final CommunicationException e) {
			final long duration = System.currentTimeMillis() - startTime;
			final String errorMsg = String.format("Cannot connect to LDAP server %s - %s", 
				serverUrl != null ? serverUrl : "unknown", e.getMessage());
			LOGGER.error("❌ LDAP connection failed - Server: {}, Error: {}", 
				serverUrl != null ? serverUrl : "unknown", e.getMessage());
			return CLdapTestResult.failure("Cannot connect to LDAP server", errorMsg, duration);
		} catch (final AuthenticationException e) {
			final long duration = System.currentTimeMillis() - startTime;
			final String errorMsg = String.format("Authentication failed for user %s - %s", 
				bindDn != null ? bindDn : "unknown", e.getMessage());
			LOGGER.error("❌ LDAP bind failed - User: {}, Error: {}", 
				bindDn != null ? bindDn : "unknown", e.getMessage());
			return CLdapTestResult.failure("LDAP authentication failed", errorMsg, duration);
		} catch (final NamingException e) {
			final long duration = System.currentTimeMillis() - startTime;
			LOGGER.error("❌ LDAP configuration test failed: {}", e.getMessage());
			return CLdapTestResult.failure("LDAP configuration error", e.getMessage(), duration);
		} catch (final Exception e) {
			final long duration = System.currentTimeMillis() - startTime;
			LOGGER.error("❌ LDAP test failed with unexpected error", e);
			return CLdapTestResult.failure("Unexpected error", e.getMessage(), duration);
		}
	}

	/** Test user authentication against LDAP server.
	 * @param username username to test
	 * @param password password to test
	 * @param settings system settings containing LDAP configuration
	 * @return test result with authentication outcome */
	public CLdapTestResult testUserAuthentication(final String username, final String password, final CSystemSettings<?> settings) {
		LOGGER.info("🧪 Testing LDAP user authentication for: {}", username);
		final long startTime = System.currentTimeMillis();
		try {
			// Validate inputs
			Objects.requireNonNull(username, "Username cannot be null");
			Objects.requireNonNull(password, "Password cannot be null");
			Objects.requireNonNull(settings, "System settings cannot be null");
			if (username.isBlank()) {
				final long duration = System.currentTimeMillis() - startTime;
				return CLdapTestResult.failure("Username cannot be blank", null, duration);
			}
			if (password.isBlank()) {
				final long duration = System.currentTimeMillis() - startTime;
				return CLdapTestResult.failure("Password cannot be blank", null, duration);
			}
			// Perform authentication
			final boolean authResult = authenticate(username, password, settings);
			final long duration = System.currentTimeMillis() - startTime;
			if (authResult) {
				return CLdapTestResult.success("User authentication successful", null, duration);
			}
			return CLdapTestResult.failure("User authentication failed", "Check username/password or LDAP configuration", duration);
		} catch (final Exception e) {
			final long duration = System.currentTimeMillis() - startTime;
			LOGGER.error("❌ LDAP user authentication test failed", e);
			return CLdapTestResult.failure("Authentication test error", e.getMessage(), duration);
		}
	}
}
