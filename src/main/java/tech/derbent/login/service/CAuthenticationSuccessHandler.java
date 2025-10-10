package tech.derbent.login.service;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import tech.derbent.setup.service.CSystemSettingsService;

/** Custom authentication success handler that manages post-login navigation. This handler processes the redirect parameter from the login form or
 * retrieves the originally requested URL before login redirect, then navigates the user to the appropriate page after successful authentication. */
@Component
public class CAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private static final String DEFAULT_SUCCESS_URL = "/home";
	private static final Logger LOGGER = LoggerFactory.getLogger(CAuthenticationSuccessHandler.class);
	private static final String REQUESTED_URL_SESSION_KEY = "requestedUrl";

	/** Constructs the full request URL from the request. */
	private static String getFullRequestUrl(HttpServletRequest request) {
		String requestUrl = request.getRequestURL().toString();
		String queryString = request.getQueryString();
		if (queryString != null) {
			requestUrl += "?" + queryString;
		}
		return requestUrl;
	}

	/** Stores the originally requested URL in the session. This method should be called when a user is redirected to login. */
	public static void saveRequestedUrl(HttpServletRequest request) {
		String requestedUrl = getFullRequestUrl(request);
		// Don't save login URLs or static resources
		if (!shouldSaveUrl(requestedUrl)) {
			return;
		}
		HttpSession session = request.getSession(true);
		session.setAttribute(REQUESTED_URL_SESSION_KEY, requestedUrl);
		LOGGER.debug("Saved requested URL in session: {}", requestedUrl);
	}

	/** Determines if a URL should be saved as the requested URL. Excludes login pages, static resources, etc. */
	private static boolean shouldSaveUrl(String url) {
		if (url == null) {
			return false;
		}
		String lowerUrl = url.toLowerCase();
		// Don't save login-related URLs
		if (lowerUrl.contains("/login")) {
			return false;
		}
		// Don't save static resources
		if (lowerUrl.contains("/vaadin/") || lowerUrl.contains("/static/") || lowerUrl.contains("/css/") || lowerUrl.contains("/js/")
				|| lowerUrl.contains("/images/") || lowerUrl.endsWith(".css") || lowerUrl.endsWith(".js") || lowerUrl.endsWith(".png")
				|| lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".ico")) {
			return false;
		}
		return true;
	}

	private final CSystemSettingsService systemSettingsService;

	public CAuthenticationSuccessHandler(CSystemSettingsService systemSettingsService) {
		this.systemSettingsService = systemSettingsService;
	}

	/** Determines the target URL for post-login redirection. Priority order: 1. 'redirect' parameter from login form 2. Originally requested URL
	 * stored in session 3. Default view from system settings 4. Fallback to '/home' */
	private String determineTargetUrl(HttpServletRequest request) {
		// First, check for redirect parameter from login form
		String redirectParam = request.getParameter("redirect");
		if (redirectParam != null && !redirectParam.trim().isEmpty()) {
			String url = mapViewNameToUrl(redirectParam.trim());
			LOGGER.debug("Using redirect parameter: {} -> {}", redirectParam, url);
			return url;
		}
		// Second, check for originally requested URL in session
		HttpSession session = request.getSession(false);
		if (session != null) {
			String requestedUrl = (String) session.getAttribute(REQUESTED_URL_SESSION_KEY);
			if (requestedUrl != null && !requestedUrl.trim().isEmpty()) {
				LOGGER.debug("Using originally requested URL from session: {}", requestedUrl);
				return requestedUrl;
			}
		}
		// Third, try to get default view from system settings
		try {
			String defaultView = systemSettingsService.getDefaultLoginView();
			if (defaultView != null && !defaultView.trim().isEmpty()) {
				String url = mapViewNameToUrl(defaultView);
				LOGGER.debug("Using default view from settings: {} -> {}", defaultView, url);
				return url;
			}
		} catch (Exception e) {
			LOGGER.error("Error retrieving default login view from settings: {}", e.getMessage());
			throw e;
		}
		// Fallback to default
		LOGGER.debug("Using fallback default URL: {}", DEFAULT_SUCCESS_URL);
		return DEFAULT_SUCCESS_URL;
	}

	/** Maps view names from the combobox to actual URLs. This should match the mapping used in CCustomLoginView. */
	private String mapViewNameToUrl(String viewName) {
		switch (viewName.toLowerCase()) {
		case "home":
		case "cdashboardview":
			return "/home";
		case "cprojectsview":
			return "/cprojectsview";
		case "cactivitiesview":
			return "/cactivitiesview";
		case "cmeetingsview":
			return "/cmeetingsview";
		case "cusersview":
			return "/cusersview";
		case "cganttview":
			return "/cganttview";
		case "cordersview":
			return "/cordersview";
		default:
			// If unknown view name, return as-is but ensure it starts with /
			return viewName.startsWith("/") ? viewName : "/" + viewName;
		}
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		LOGGER.debug("Authentication successful for user: {}", authentication.getName());
		// Get the target URL for redirection
		String targetUrl = determineTargetUrl(request);
		LOGGER.info("Redirecting user {} to: {}", authentication.getName(), targetUrl);
		// Clear the requested URL from session since we're about to redirect
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.removeAttribute(REQUESTED_URL_SESSION_KEY);
		}
		// Perform the redirect
		response.sendRedirect(targetUrl);
	}
}
