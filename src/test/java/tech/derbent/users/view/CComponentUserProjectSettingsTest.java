package tech.derbent.users.view;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;
import tech.derbent.users.service.CUserProjectSettingsService;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.service.CUserTypeService;

/** Unit test for CComponentUserProjectSettings focusing on the populate form pattern and lazy loading issues. */
@ExtendWith (MockitoExtension.class)
public class CComponentUserProjectSettingsTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentUserProjectSettingsTest.class);
	@Mock
	private IContentOwner mockContentOwner;
	@Mock
	private CUserService mockUserService;
	@Mock
	private CUserTypeService mockUserTypeService;
	@Mock
	private CCompanyService mockCompanyService;
	@Mock
	private CProjectService mockProjectService;
	@Mock
	private CUserProjectSettingsService mockUserProjectSettingsService;
	private CComponentUserProjectSettings component;
	private CUser testUser;
	private CEnhancedBinder<CUser> testBinder;

	@BeforeEach
	void setUp() throws Exception {
		// Create test user with project settings
		testUser = new CUser();
		testUser.setName("Test User");
		testUser.setLogin("testuser");
		testUser.setEmail("test@example.com");
		// Create project settings for the user
		List<CUserProjectSettings> projectSettings = new ArrayList<>();
		CUserProjectSettings setting1 = new CUserProjectSettings();
		setting1.setUser(testUser);
		CProject project1 = new CProject();
		project1.setName("Test Project 1");
		setting1.setProject(project1);
		projectSettings.add(setting1);
		testUser.setProjectSettings(projectSettings);
		// Setup mock behavior - only mock what's actually called
		lenient().when(mockUserProjectSettingsService.findByUser(testUser)).thenReturn(projectSettings);
		lenient().when(mockUserService.getById(any())).thenReturn(Optional.of(testUser));
		lenient().when(mockProjectService.getAvailableProjectsForUser(any())).thenReturn(new ArrayList<>());
		// Create binder
		testBinder = new CEnhancedBinder<>(CUser.class);
		// Create component
		component = new CComponentUserProjectSettings(mockContentOwner, testUser, testBinder, mockUserService, mockUserTypeService,
				mockCompanyService, mockProjectService, mockUserProjectSettingsService);
	}

	@Test
	void testPopulateFormWithEntity() {
		LOGGER.info("🧪 Testing populateForm with entity");
		// Test populateForm(Object entity)
		assertDoesNotThrow(() -> {
			component.populateForm(testUser);
		}, "populateForm(entity) should not throw exception");
		// Verify the entity was set
		assertEquals(testUser, component.getCurrentEntity(), "Current entity should be set correctly");
	}

	@Test
	void testPopulateFormPattern() {
		LOGGER.info("🧪 Testing complete populateForm pattern");
		// Test the pattern works correctly
		assertDoesNotThrow(() -> {
			component.populateForm(testUser);
			component.populateForm();
		}, "populateForm pattern should work without issues");
	}
}
