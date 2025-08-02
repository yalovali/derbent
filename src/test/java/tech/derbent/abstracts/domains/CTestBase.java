package tech.derbent.abstracts.domains;

import java.time.Clock;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import tech.derbent.activities.service.CActivityRepository;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.activities.service.CActivityStatusRepository;
import tech.derbent.activities.service.CActivityStatusService;
import tech.derbent.activities.service.CActivityTypeRepository;
import tech.derbent.activities.service.CActivityTypeService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserRepository;
import tech.derbent.users.service.CUserService;

public abstract class CTestBase {

	@Mock
	protected Clock clock;

	@Mock
	protected CUserService userService;

	@Mock
	protected CActivityStatusRepository activityStatusRepository;

	@Mock
	protected CActivityStatusService activityStatusService;

	protected CUser testUser;

	@Mock
	protected CProject project;

	@Autowired
	protected CProjectService projectService;

	@Mock
	protected CActivityTypeService activityTypeService;

	@Mock
	protected CActivityTypeRepository activityTypeRepository;

	@Mock
	public CUserRepository userRepository;

	@Mock
	protected CActivityRepository activityRepository;

	protected CActivityService activityService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		testUser = new CUser("Test User");
		testUser.setLogin("testuser");
		activityService = new CActivityService(activityRepository, clock);
		userService = new CUserService(userRepository, clock);
		project = new CProject("Test Project");
		activityTypeService = new CActivityTypeService(activityTypeRepository, clock);
		activityStatusService =
			new CActivityStatusService(activityStatusRepository, clock);
		project = new CProject("Test Project");
		setupForTest();
	}

	protected abstract void setupForTest();
}
