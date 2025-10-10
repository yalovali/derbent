package tech.derbent.login.service;

import java.util.Collection;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/** Custom authentication token that includes company ID for multi-tenant authentication. This token extends UsernamePasswordAuthenticationToken to
 * carry additional company context during the authentication process. In the Derbent system, users can have the same username across different
 * companies (unique constraint is login + company_id), so the company context is required to identify the correct user during authentication. */
public class CCompanyAwareAuthenticationToken extends UsernamePasswordAuthenticationToken {

	private static final long serialVersionUID = 1L;
	private final Long companyId;

	/** Creates an unauthenticated token with company context. Used before authentication.
	 * @param principal   the username
	 * @param credentials the password
	 * @param companyId   the company ID for tenant isolation */
	public CCompanyAwareAuthenticationToken(Object principal, Object credentials, Long companyId) {
		super(principal, credentials);
		this.companyId = companyId;
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
		this.companyId = companyId;
		// Note: Do NOT call setAuthenticated(true) here - the super constructor with authorities already marks it as authenticated
	}

	/** Gets the company ID for tenant isolation.
	 * @return the company ID */
	public Long getCompanyId() { return companyId; }
}
