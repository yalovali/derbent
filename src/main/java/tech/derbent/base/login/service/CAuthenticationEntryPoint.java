package tech.derbent.base.login.service;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Custom authentication entry point that saves the originally requested URL before redirecting to the login page. This ensures that after successful
 * authentication, users can be redirected back to the page they originally tried to access. */
@Component
public class CAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private static final Logger LOGGER = LoggerFactory.getLogger(CAuthenticationEntryPoint.class);

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
			throws IOException, ServletException {
		LOGGER.debug("Authentication required, saving requested URL and redirecting to login");
		// Save the originally requested URL using our success handler utility
		CAuthenticationSuccessHandler.saveRequestedUrl(request);
		// Redirect to login page
		String loginUrl = request.getContextPath() + "/login";
		response.sendRedirect(loginUrl);
	}
}
