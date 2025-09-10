package unit_tests.tech.derbent.login.view;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.QueryParameters;
import tech.derbent.login.view.CCustomLoginView;
import tech.derbent.setup.service.CSystemSettingsService;

/** Unit tests for CCustomLoginView URL parameter handling. Tests the mapping between URLs and view names for proper redirection. */
@ExtendWith (MockitoExtension.class)
class CCustomLoginViewUrlMappingTest {

	@Mock
	private BeforeEnterEvent event;
	@Mock
	private Location location;
	@Mock
	private QueryParameters queryParameters;
	private CCustomLoginView loginView;
	private CSystemSettingsService systemSettingsService;

	@BeforeEach
	void setUp() {
		systemSettingsService = mock(CSystemSettingsService.class);
		loginView = new CCustomLoginView(systemSettingsService);
		// Inject the mock service using reflection since it's @Autowired
		try {
			var field = CCustomLoginView.class.getDeclaredField("systemSettingsService");
			field.setAccessible(true);
			field.set(loginView, systemSettingsService);
		} catch (Exception e) {
			fail("Failed to inject mock service: " + e.getMessage());
		}
	}

	@Test
	void shouldHandleErrorParameter() {
		// Given
		Map<String, List<String>> params = Map.of("error", List.of("true"));
		when(event.getLocation()).thenReturn(location);
		when(location.getQueryParameters()).thenReturn(queryParameters);
		when(queryParameters.getParameters()).thenReturn(params);
		// When
		loginView.beforeEnter(event);
		// This test verifies that the error handling doesn't throw exceptions
		// In a real UI test, we would check that the error message is displayed
	}

	@Test
	void shouldHandleContinueParameter() {
		// Given
		Map<String, List<String>> params = Map.of("continue", List.of("/cprojectsview"));
		when(event.getLocation()).thenReturn(location);
		when(location.getQueryParameters()).thenReturn(queryParameters);
		when(queryParameters.getParameters()).thenReturn(params);
		// When
		loginView.beforeEnter(event);
		// This test verifies that the continue parameter handling doesn't throw exceptions
		// In a real UI test, we would check that the combobox is set to the correct value
	}

	@Test
	void shouldHandleBothErrorAndContinueParameters() {
		// Given
		Map<String, List<String>> params = Map.of("error", List.of("true"), "continue", List.of("/cusersview"));
		when(event.getLocation()).thenReturn(location);
		when(location.getQueryParameters()).thenReturn(queryParameters);
		when(queryParameters.getParameters()).thenReturn(params);
		// When
		loginView.beforeEnter(event);
		// This test verifies that both parameters can be handled simultaneously
	}

	@Test
	void shouldHandleEmptyParameters() {
		// Given
		Map<String, List<String>> params = Map.of();
		when(event.getLocation()).thenReturn(location);
		when(location.getQueryParameters()).thenReturn(queryParameters);
		when(queryParameters.getParameters()).thenReturn(params);
		// When
		loginView.beforeEnter(event);
		// This test verifies that empty parameters don't cause issues
	}

	@Test
	void shouldHandleInvalidContinueParameter() {
		// Given
		Map<String, List<String>> params = Map.of("continue", List.of("/unknown-page"));
		when(event.getLocation()).thenReturn(location);
		when(location.getQueryParameters()).thenReturn(queryParameters);
		when(queryParameters.getParameters()).thenReturn(params);
		// When
		loginView.beforeEnter(event);
		// This test verifies that invalid URLs are handled gracefully
	}
}
