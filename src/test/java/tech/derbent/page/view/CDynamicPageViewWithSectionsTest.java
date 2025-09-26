package tech.derbent.page.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.page.domain.CPageEntity;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.domain.CGridEntity;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CGridEntityService;
import tech.derbent.session.service.CSessionService;

/** Unit tests for CDynamicPageViewWithSections demonstrating how to configure dynamic pages with grid and detail sections for activity management. */
class CDynamicPageViewWithSectionsTest {

	private CDetailSection activityDetailSection;
	private CGridEntity activityGridEntity;
	private CPageEntity activityPageEntity;
	@Mock
	private CActivityService activityService;
	@Mock
	private ApplicationContext applicationContext;
	@Mock
	private CDetailSectionService detailSectionService;
	@Mock
	private CGridEntityService gridEntityService;
	@Mock
	private CSessionService sessionService;
	private CProject testProject;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		// Create a test project
		testProject = new CProject("Test Project");
		// Create a grid entity for activities
		activityGridEntity = new CGridEntity("ActivityGrid", testProject);
		activityGridEntity.setDataServiceBeanName("activityService");
		activityGridEntity.setSelectedFields("name:1,description:2,priority:3,status:4");
		// Create a detail section for activities
		activityDetailSection = new CDetailSection("ActivityDetails", testProject);
		activityDetailSection.setEntityType("tech.derbent.activities.domain.CActivity");
		activityDetailSection.setScreenTitle("Activity Details");
		activityDetailSection.setHeaderText("View and edit activity information");
		// Create a page entity that uses both grid and detail sections
		activityPageEntity = new CPageEntity("ActivityPage", testProject);
		activityPageEntity.setPageTitle("Activity Management");
		activityPageEntity.setDescription("Manage project activities with grid and detail views");
		activityPageEntity.setGridEntity(activityGridEntity);
		activityPageEntity.setDetailSection(activityDetailSection);
		activityPageEntity.setIsActive(true);
		activityPageEntity.setRequiresAuthentication(true);
	}

	@Test
	void testActivityEntityType() {
		// Test that the activity detail section is configured for the correct entity type
		assertEquals("tech.derbent.activities.domain.CActivity", activityPageEntity.getDetailSection().getEntityType());
		// Verify the grid entity is configured with the correct service
		assertEquals("activityService", activityPageEntity.getGridEntity().getDataServiceBeanName());
	}

	/** This test demonstrates how to create page entities for different entity types following the same pattern as activities. */
	@Test
	void testExampleConfigurationForOtherEntityTypes() {
		// Example: Meeting page entity
		CGridEntity meetingGridEntity = new CGridEntity("MeetingGrid", testProject);
		meetingGridEntity.setDataServiceBeanName("meetingService");
		meetingGridEntity.setSelectedFields("name:1,startDate:2,endDate:3,status:4");
		CDetailSection meetingDetailSection = new CDetailSection("MeetingDetails", testProject);
		meetingDetailSection.setEntityType("tech.derbent.meetings.domain.CMeeting");
		meetingDetailSection.setScreenTitle("Meeting Details");
		meetingDetailSection.setHeaderText("View and edit meeting information");
		CPageEntity meetingPageEntity = new CPageEntity("MeetingPage", testProject);
		meetingPageEntity.setPageTitle("Meeting Management");
		meetingPageEntity.setGridEntity(meetingGridEntity);
		meetingPageEntity.setDetailSection(meetingDetailSection);
		// Test the configuration
		assertNotNull(meetingPageEntity.getGridEntity());
		assertNotNull(meetingPageEntity.getDetailSection());
		assertEquals("meetingService", meetingPageEntity.getGridEntity().getDataServiceBeanName());
		assertEquals("tech.derbent.meetings.domain.CMeeting", meetingPageEntity.getDetailSection().getEntityType());
	}

	@Test
	void testGridAndDetailSectionReferences() {
		// Test that the page entity properly references grid and detail sections
		CGridEntity gridEntity = activityPageEntity.getGridEntity();
		CDetailSection detailSection = activityPageEntity.getDetailSection();
		assertNotNull(gridEntity, "Grid entity should be set");
		assertNotNull(detailSection, "Detail section should be set");
		// Test that both entities belong to the same project
		assertEquals(testProject.getName(), gridEntity.getProject().getName());
		assertEquals(testProject.getName(), detailSection.getProject().getName());
	}

	@Test
	void testPageEntityConfiguration() {
		// Test that the page entity is properly configured
		assertNotNull(activityPageEntity);
		assertEquals("Activity Management", activityPageEntity.getPageTitle());
		assertNotNull(activityPageEntity.getGridEntity());
		assertNotNull(activityPageEntity.getDetailSection());
		// Test grid entity configuration
		assertEquals("ActivityGrid", activityPageEntity.getGridEntity().getName());
		assertEquals("activityService", activityPageEntity.getGridEntity().getDataServiceBeanName());
		assertEquals("name:1,description:2,priority:3,status:4", activityPageEntity.getGridEntity().getSelectedFields());
		// Test detail section configuration
		assertEquals("ActivityDetails", activityPageEntity.getDetailSection().getName());
		assertEquals("tech.derbent.activities.domain.CActivity", activityPageEntity.getDetailSection().getEntityType());
		assertEquals("Activity Details", activityPageEntity.getDetailSection().getScreenTitle());
	}

	@Test
	void testPageEntityWithoutGridAndDetailSections() {
		// Create a page entity without grid and detail sections
		CPageEntity simplePageEntity = new CPageEntity("SimplePage", testProject);
		simplePageEntity.setPageTitle("Simple Page");
		simplePageEntity.setDescription("A simple page without grid and detail sections");
		simplePageEntity.setContent("<h1>Static Content</h1><p>This is static content.</p>");
		simplePageEntity.setIsActive(true);
		// Test that it doesn't have grid and detail configuration
		assertNull(simplePageEntity.getGridEntity());
		assertNull(simplePageEntity.getDetailSection());
	}
}
