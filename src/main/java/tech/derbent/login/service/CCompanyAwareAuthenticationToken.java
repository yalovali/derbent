package tech.derbent.login.service;

import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/** Custom authentication token that includes company ID for multi-tenant authentication. This token extends UsernamePasswordAuthenticationToken to
 * carry additional company context during the authentication process. In the Derbent system, users can have the same username across different
 * companies (unique constraint is login + company_id), so the company context is required to identify the correct user during authentication. */
public class CCompanyAwareAuthenticationToken extends UsernamePasswordAuthenticationToken {

	private static final long serialVersionUID = 1L;
	private final Long companyId;
	private static final Logger LOGGER = LoggerFactory.getLogger(CCompanyAwareAuthenticationToken.class);

	/** Creates an unauthenticated token with company context. Used before authentication.
	 * @param principal   the username
	 * @param credentials the password
	 * @param companyId   the company ID for tenant isolation */
	public CCompanyAwareAuthenticationToken(Object principal, Object credentials, Long companyId) {
		super(principal, credentials);
		this.companyId = companyId;
		LOGGER.debug("Creating unauthenticated token for user '{}' with company ID: {}", principal, companyId);
		setAuthenticated(false);
	}

	/** Creates an authenticated token with company context. Used after successful authentication.
	 * @param principal   the username
	 * @param credentials the password
	 * @param companyId   the company ID for tenant isolation
	 * @param authorities the granted authorities */
	public CCompanyAwareAuthenticationToken(Object principal, Object credentials, Long companyId,
			Collection<? extends GrantedAuthority> authorities) {
		super(principal, credentials, authorities);
		LOGGER.debug("Creating authenticated token for user '{}' with company ID: {}", principal, companyId);
		this.companyId = companyId;
		// Note: Do NOT call setAuthenticated(true) here - the super constructor with authorities already marks it as authenticated
	}

	/** Gets the company ID for tenant isolation.
	 * @return the company ID */
	public Long getCompanyId() {
		LOGGER.debug("Retrieving company ID: {}", companyId);
		return companyId;
	}
}
