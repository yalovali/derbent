package tech.derbent.api.authentication.service;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Custom authentication failure handler that preserves user input (username and company)
 * when authentication fails, allowing the user to see and modify their input.
 * 
 * This prevents the frustrating UX of having to re-enter username and company selection
 * after an authentication failure.
 * 
 * @author Derbent Team
 * @since 2026
 */
@Component
public class CAuthenticationFailureHandler implements AuthenticationFailureHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(CAuthenticationFailureHandler.class);

	/**
	 * Handles authentication failure by redirecting to login page with preserved parameters.
	 * 
	 * Extracts username and company from the failed authentication attempt and includes
	 * them as query parameters in the redirect URL, allowing the login form to restore
	 * the user's input.
	 * 
	 * @param request the servlet request
	 * @param response the servlet response
	 * @param exception the authentication exception that caused the failure
	 * @throws IOException if an I/O error occurs
	 * @throws ServletException if a servlet error occurs
	 */
	@Override
	public void onAuthenticationFailure(final HttpServletRequest request, final HttpServletResponse response,
			final AuthenticationException exception) throws IOException, ServletException {
		
		// Log the authentication failure for security monitoring
		LOGGER.warn("🔐 Authentication failed for user: {} from IP: {}", 
			request.getParameter("username"), 
			request.getRemoteAddr());

		// Extract original username and company parameters to preserve user input
		final String usernameParam = request.getParameter("username_param");
		final String companyParam = request.getParameter("company_param");
		
		// Build redirect URL with error parameter and preserved user input
		final UriComponentsBuilder redirectUrlBuilder = UriComponentsBuilder.fromPath("/login")
			.queryParam("error", "true");
		
		// Add username parameter if present (for field restoration)
		if (usernameParam != null && !usernameParam.trim().isEmpty()) {
			redirectUrlBuilder.queryParam("username", usernameParam);
			LOGGER.debug("Preserving username parameter for field restoration: {}", usernameParam);
		}
		
		// Add company parameter if present (for field restoration)
		if (companyParam != null && !companyParam.trim().isEmpty()) {
			redirectUrlBuilder.queryParam("company", companyParam);
			LOGGER.debug("Preserving company parameter for field restoration: {}", companyParam);
		}
		
		final String redirectUrl = redirectUrlBuilder.build().toUriString();
		
		LOGGER.debug("Redirecting to login page with preserved parameters: {}", redirectUrl);
		response.sendRedirect(redirectUrl);
	}
}