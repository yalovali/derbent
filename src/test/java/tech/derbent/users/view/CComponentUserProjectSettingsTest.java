package tech.derbent.users.view;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
	private CUser testUser;

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
	}
}
