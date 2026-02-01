package tech.derbent.plm.activities.service;

import static org.mockito.Mockito.when;
import java.lang.reflect.Field;
import java.time.Clock;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.project.domain.CProject_Derbent;

/** Test class for verifying Activity parent-child relationship functionality. Tests parent assignment, validation, and hierarchy checks. */
@DisplayName ("Activity Parent-Child Relationship Tests")
class CActivityParentChildTest {

	@Mock
	private CActivityPriorityService activityPriorityService;
	@Mock
	private CActivityTypeService activityTypeService;
	@Mock
	private Clock clock;
	private CCompany company;
	private CProject_Derbent project;
	@Mock
	private IActivityRepository repository;
	@Mock
	private ISessionService sessionService;
	@Mock
	private CProjectItemStatusService statusService;

	/** Helper method to set entity ID using reflection since setId() is not public.
	 * @param entity The entity to set ID for
	 * @param id     The ID value to set
	 * @throws Exception if reflection fails */
	private void setEntityId(final Object entity, final Long id) throws Exception {
		final Field idField = entity.getClass().getSuperclass().getSuperclass().getDeclaredField("id");
		idField.setAccessible(true);
		idField.set(entity, id);
	}

	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.openMocks(this);
		// Setup company and project
		company = new CCompany("Test Company");
		setEntityId(company, 1L);
		project = new CProject_Derbent("Test Project", company);
		setEntityId(project, 1L);
		// Mock session service
		when(sessionService.getActiveProject()).thenReturn(Optional.of(project));
		when(sessionService.getActiveCompany()).thenReturn(Optional.of(company));
	}
}
