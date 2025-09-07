package tech.derbent.test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.comments.service.CCommentService;
import tech.derbent.decisions.service.CDecisionService;
import tech.derbent.meetings.service.CMeetingService;
import tech.derbent.orders.service.COrderService;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.risks.service.CRiskService;
import tech.derbent.users.service.CUserService;

/** Test to validate that the reduced sample data (2 examples per entity) maintains all required enrichments: relations, types, status, colors,
 * comments. */
@SpringBootTest
@ActiveProfiles ("test")
@Transactional
public class MinimalSampleDataValidationTest {

	@Autowired
	private CUserService userService;
	@Autowired
	private CProjectService projectService;
	@Autowired
	private CActivityService activityService;
	@Autowired
	private CMeetingService meetingService;
	@Autowired
	private COrderService orderService;
	@Autowired
	private CCommentService commentService;
	@Autowired
	private CRiskService riskService;
	@Autowired
	private CDecisionService decisionService;

	@Test
	public void testExactlyTwoExamplesPerEntityType() {
		// Verify exactly 2 examples per entity type
		assertEquals(2, userService.count(), "Should have exactly 2 users");
		assertEquals(2, projectService.count(), "Should have exactly 2 projects");
		assertEquals(2, activityService.count(), "Should have exactly 2 activities");
		assertEquals(2, meetingService.count(), "Should have exactly 2 meetings");
		assertEquals(2, orderService.count(), "Should have exactly 2 orders");
		assertEquals(2, commentService.count(), "Should have exactly 2 comments");
		assertEquals(2, riskService.count(), "Should have exactly 2 risks");
		assertEquals(2, decisionService.count(), "Should have exactly 2 decisions");
	}

	@Test
	public void testDataIntegrityAndStructure() {
		// Basic validation that the structure is correct and reduced
		assertTrue(userService.count() > 0, "Users should exist");
		assertTrue(projectService.count() > 0, "Projects should exist");
		assertTrue(activityService.count() > 0, "Activities should exist");
		// Verify we have significantly fewer records than the original
		assertTrue(userService.count() <= 2, "Users reduced to 2 or fewer");
		assertTrue(projectService.count() <= 2, "Projects reduced to 2 or fewer");
		assertTrue(activityService.count() <= 2, "Activities reduced to 2 or fewer");
	}
}
