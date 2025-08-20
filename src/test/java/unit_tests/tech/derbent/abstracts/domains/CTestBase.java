package unit_tests.tech.derbent.abstracts.domains;

import java.time.Clock;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.vaadin.flow.spring.security.AuthenticationContext;

import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.activities.domain.CActivityType;
import tech.derbent.activities.service.CActivityRepository;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.activities.service.CActivityStatusRepository;
import tech.derbent.activities.service.CActivityStatusService;
import tech.derbent.activities.service.CActivityTypeRepository;
import tech.derbent.activities.service.CActivityTypeService;
import tech.derbent.comments.domain.CCommentPriority;
import tech.derbent.comments.service.CCommentPriorityService;
import tech.derbent.comments.service.CCommentRepository;
import tech.derbent.comments.service.CCommentService;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.decisions.service.CDecisionService;
import tech.derbent.decisions.service.CDecisionStatusService;
import tech.derbent.decisions.service.CDecisionTypeRepository;
import tech.derbent.decisions.service.CDecisionTypeService;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.domain.CMeetingStatus;
import tech.derbent.meetings.domain.CMeetingType;
import tech.derbent.meetings.service.CMeetingService;
import tech.derbent.meetings.service.CMeetingTypeService;
import tech.derbent.orders.service.COrderService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectRepository;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.risks.service.CRiskService;
import tech.derbent.risks.service.CRiskStatusRepository;
import tech.derbent.risks.service.CRiskStatusService;
import tech.derbent.session.service.CSessionService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserRepository;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.service.CUserTypeRepository;
import tech.derbent.users.service.CUserTypeService;

public abstract class CTestBase {

    @Mock
    protected CActivityRepository activityRepository;

    @Autowired
    protected CActivityService activityService;

    @Mock
    protected CActivityStatusRepository activityStatusRepository;

    @Autowired
    protected CActivityStatusService activityStatusService;

    @Mock
    protected CActivityTypeRepository activityTypeRepository;

    @Autowired
    protected CActivityTypeService activityTypeService;

    @Mock
    protected AuthenticationContext authenticationContext;

    @Mock
    protected Clock clock;

    @Autowired
    protected CCommentPriorityService commentPriorityService;

    @Mock
    protected CCommentRepository commentRepository;

    // Comment services
    @Autowired
    protected CCommentService commentService;

    // Company services
    @Autowired
    protected CCompanyService companyService;

    // Decision services
    @Autowired
    protected CDecisionService decisionService;

    @Autowired
    protected CDecisionStatusService decisionStatusService;

    @Mock
    protected CDecisionTypeRepository decisionTypeRepository;

    @Autowired
    protected CDecisionTypeService decisionTypeService;

    protected CMeeting meeting;

    @Autowired
    protected CMeetingService meetingService;

    protected CMeetingType meetingType;

    @Autowired
    protected CMeetingTypeService meetingTypeService;

    // Order services
    @Autowired
    protected COrderService orderService;

    protected CCommentPriority priority;

    @Mock
    protected CProject project;

    @Autowired
    protected CProjectRepository projectRepository;

    @Autowired
    protected CProjectService projectService;

    // Risk services
    @Autowired
    protected CRiskService riskService;

    @Mock
    protected CRiskStatusRepository riskStatusRepository;

    @Autowired
    protected CRiskStatusService riskStatusService;

    @Mock
    protected CSessionService sessionService;

    protected CMeetingStatus status;

    protected CActivity testActivity;

    private CActivityStatus testActivityStatus;

    private CActivityType testActivityType;

    protected CUser testUser;

    @Mock
    protected CUserRepository userRepository;

    @Autowired
    protected CUserService userService;

    @Mock
    private CUserTypeRepository userTypeRepository;

    @Autowired
    protected CUserTypeService userTypeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create test entities for use in tests
        project = new CProject("Test Project");
        testUser = new CUser("Test User");
        testUser.setLogin("testuser");
        testActivity = new CActivity("Test Activity", project);
        priority = new CCommentPriority("High", project);
        priority.setColor("#FF0000");
        meetingType = new CMeetingType("Test Meeting Type", project);
        status = new CMeetingStatus("SCHEDULED", project);
        meeting = new CMeeting("Test Meeting", project, meetingType);

        // Set up authentication context for session service
        final User authUser = new User("testuser", "password", java.util.Collections.emptyList());
        final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(testUser,
                null, authUser.getAuthorities());

        testActivityType = new CActivityType("Development", project);
        testActivity.setActivityType(testActivityType);
        testActivityStatus = new CActivityStatus("IN_PROGRESS", project);
        testActivity.setStatus(testActivityStatus);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        setupForTest();
    }

    protected abstract void setupForTest();
}