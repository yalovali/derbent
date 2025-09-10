package unit_tests.tech.derbent.login.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import tech.derbent.login.service.CAuthenticationSuccessHandler;
import tech.derbent.setup.service.CSystemSettingsService;

/** Unit tests for CAuthenticationSuccessHandler. Tests the post-login redirection logic including redirect parameter, session storage, and system
 * settings integration. */
@ExtendWith (MockitoExtension.class)
class CAuthenticationSuccessHandlerTest {

	@Mock
	private CSystemSettingsService systemSettingsService;
	@Mock
	private HttpServletRequest request;
	@Mock
	private HttpServletResponse response;
	@Mock
	private Authentication authentication;
	@Mock
	private HttpSession session;
	private CAuthenticationSuccessHandler handler;

	@BeforeEach
	void setUp() {
		handler = new CAuthenticationSuccessHandler(systemSettingsService);
		when(authentication.getName()).thenReturn("testuser");
	}

	@Test
	void shouldRedirectToRedirectParameterWhenProvided() throws IOException, ServletException {
		// Given
		when(request.getParameter("redirect")).thenReturn("cprojectsview");
		when(request.getSession(false)).thenReturn(session);
		// When
		handler.onAuthenticationSuccess(request, response, authentication);
		// Then
		verify(response).sendRedirect("/cprojectsview");
		verify(session).removeAttribute("requestedUrl");
	}

	@Test
	void shouldRedirectToSessionStoredUrlWhenNoRedirectParameter() throws IOException, ServletException {
		// Given
		when(request.getParameter("redirect")).thenReturn(null);
		when(request.getSession(false)).thenReturn(session);
		when(session.getAttribute("requestedUrl")).thenReturn("/cactivitiesview");
		// When
		handler.onAuthenticationSuccess(request, response, authentication);
		// Then
		verify(response).sendRedirect("/cactivitiesview");
		verify(session).removeAttribute("requestedUrl");
	}

	@Test
	void shouldRedirectToSystemSettingsDefaultView() throws IOException, ServletException {
		// Given
		when(request.getParameter("redirect")).thenReturn(null);
		when(request.getSession(false)).thenReturn(session);
		when(session.getAttribute("requestedUrl")).thenReturn(null);
		when(systemSettingsService.getDefaultLoginView()).thenReturn("cusersview");
		// When
		handler.onAuthenticationSuccess(request, response, authentication);
		// Then
		verify(response).sendRedirect("/cusersview");
	}

	@Test
	void shouldRedirectToDefaultHomeWhenAllElseFails() throws IOException, ServletException {
		// Given
		when(request.getParameter("redirect")).thenReturn(null);
		when(request.getSession(false)).thenReturn(session);
		when(session.getAttribute("requestedUrl")).thenReturn(null);
		when(systemSettingsService.getDefaultLoginView()).thenThrow(new RuntimeException("Database error"));
		// When
		handler.onAuthenticationSuccess(request, response, authentication);
		// Then
		verify(response).sendRedirect("/home");
	}

	@Test
	void shouldHandleEmptyRedirectParameter() throws IOException, ServletException {
		// Given
		when(request.getParameter("redirect")).thenReturn("  ");
		when(request.getSession(false)).thenReturn(session);
		when(session.getAttribute("requestedUrl")).thenReturn(null);
		when(systemSettingsService.getDefaultLoginView()).thenReturn("home");
		// When
		handler.onAuthenticationSuccess(request, response, authentication);
		// Then
		verify(response).sendRedirect("/home");
	}

	@Test
	void shouldMapViewNamesCorrectly() throws IOException, ServletException {
		// Test all view mappings
		String[][] testCases = {
				{
						"home", "/home"
				}, {
						"cdashboardview", "/home"
				}, {
						"cprojectsview", "/cprojectsview"
				}, {
						"cactivitiesview", "/cactivitiesview"
				}, {
						"cmeetingsview", "/cmeetingsview"
				}, {
						"cusersview", "/cusersview"
				}, {
						"cganttview", "/cganttview"
				}, {
						"cordersview", "/cordersview"
				}, {
						"unknown-view", "/unknown-view"
				}
		};
		for (String[] testCase : testCases) {
			// Given
			when(request.getParameter("redirect")).thenReturn(testCase[0]);
			when(request.getSession(false)).thenReturn(session);
			// When
			handler.onAuthenticationSuccess(request, response, authentication);
			// Then
			verify(response).sendRedirect(testCase[1]);
			// Reset mocks for next iteration
			reset(response);
		}
	}

	@Test
	void shouldHandleNullSession() throws IOException, ServletException {
		// Given
		when(request.getParameter("redirect")).thenReturn(null);
		when(request.getSession(false)).thenReturn(null);
		when(systemSettingsService.getDefaultLoginView()).thenReturn("home");
		// When
		handler.onAuthenticationSuccess(request, response, authentication);
		// Then
		verify(response).sendRedirect("/home");
		// Should not throw NPE when session is null
	}
}
