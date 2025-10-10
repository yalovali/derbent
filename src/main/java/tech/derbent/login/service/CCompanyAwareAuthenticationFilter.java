package tech.derbent.login.service;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tech.derbent.api.utils.Check;

/** Custom authentication filter that extracts company ID from login form and creates company-aware authentication tokens. This filter intercepts the
 * login form submission and creates a CCompanyAwareAuthenticationToken that includes the company context for multi-tenant authentication. Form
 * Parameters: - username: user's login name - password: user's password - companyId: selected company ID for tenant isolation */
public class CCompanyAwareAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private static final String COMPANY_ID_PARAMETER = "companyId";
	private static final Logger LOGGER = LoggerFactory.getLogger(CCompanyAwareAuthenticationFilter.class);

	public CCompanyAwareAuthenticationFilter() {
		super();
	}

	public CCompanyAwareAuthenticationFilter(AuthenticationManager authenticationManager) {
		super(authenticationManager);
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
		if (!request.getMethod().equals("POST")) {
			throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
		}
		String username = obtainUsername(request);
		String password = obtainPassword(request);
		String companyIdStr = request.getParameter(COMPANY_ID_PARAMETER);
		if (username == null) {
			username = "";
		}
		if (password == null) {
			password = "";
		}
		username = username.trim();
		// Create company-aware authentication token if company ID is provided
		CCompanyAwareAuthenticationToken authRequest;
		if (companyIdStr != null && !companyIdStr.trim().isEmpty()) {
			try {
				Long companyId = Long.parseLong(companyIdStr.trim());
				LOGGER.debug("Creating company-aware authentication token for user '{}' and company ID: {}", username, companyId);
				authRequest = new CCompanyAwareAuthenticationToken(username, password, companyId);
			} catch (NumberFormatException e) {
				LOGGER.error("Invalid company ID format: {}", companyIdStr);
				throw new AuthenticationServiceException("Invalid company ID format");
			}
		} else {
			LOGGER.debug("Creating standard authentication token for user '{}'", username);
			authRequest = new CCompanyAwareAuthenticationToken(username, password, null);
		}
		// Set authentication details
		authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
		return this.getAuthenticationManager().authenticate(authRequest);
	}

	/** Sets the details property on the authentication request. Can be overridden by subclasses.
	 * @param request     the HTTP request
	 * @param authRequest the authentication request
	 * @deprecated No longer needed as details are set directly in attemptAuthentication */
	@Deprecated
	protected void setDetails(HttpServletRequest request, Authentication authRequest) {
		// This method is no longer used but kept for backward compatibility
	}
}
