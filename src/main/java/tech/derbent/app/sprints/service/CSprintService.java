package tech.derbent.app.sprints.service;

import java.time.Clock;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.sprints.domain.CSprint;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.base.session.service.ISessionService;

/**
 * CSprintService - Service class for managing sprints.
 * Provides business logic for sprint operations.
 */
@Service
@PreAuthorize("isAuthenticated()")
public class CSprintService extends CProjectItemService<CSprint> implements IEntityRegistrable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CSprintService.class);
	private final CSprintTypeService entityTypeService;

	public CSprintService(final ISprintRepository repository, final Clock clock, final ISessionService sessionService,
			final CSprintTypeService sprintTypeService, final CProjectItemStatusService projectItemStatusService) {
		super(repository, clock, sessionService, projectItemStatusService);
		this.entityTypeService = sprintTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CSprint sprint) {
		return super.checkDeleteAllowed(sprint);
	}

	@Override
	public Class<CSprint> getEntityClass() {
		return CSprint.class;
	}

	@Override
	public Class<?> getInitializerServiceClass() {
		return CSprintInitializerService.class;
	}

	@Override
	public Class<?> getPageServiceClass() {
		return CPageServiceSprint.class;
	}

	@Override
	public Class<?> getServiceClass() {
		return this.getClass();
	}

	@Override
	public void initializeNewEntity(final CSprint entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new sprint entity");

		// Get current project from session
		final CProject currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize sprint"));

		// Initialize workflow-based status and type
		IHasStatusAndWorkflowService.initializeNewEntity(entity, currentProject, (tech.derbent.api.entityOfProject.domain.CTypeEntityService<?>) entityTypeService, projectItemStatusService);

		// Date defaults: start today, end in 2 weeks (standard sprint duration)
		entity.setStartDate(LocalDate.now(clock));
		entity.setEndDate(LocalDate.now(clock).plusWeeks(2));
		entity.setColor(CSprint.DEFAULT_COLOR);

		LOGGER.debug("Sprint initialization complete with default values");
	}

	// ====================================
	// Data Provider Methods for Calculated Fields
	// ====================================
	// These methods are called automatically by the form builder when fields
	// are annotated with @AMetaData(dataProviderBean="CSprintService", dataProviderMethod="methodName")
	// This pattern allows calculated/derived fields to be computed dynamically
	// instead of being stored in the database.
	//
	// Example usage in entity:
	// @Transient
	// @AMetaData(
	//     displayName = "Total Story Points",
	//     dataProviderBean = "CSprintService",
	//     dataProviderMethod = "getTotalStoryPoints"
	// )
	// private Long totalStoryPoints;

	/** Data provider callback: Calculates the total number of items in a sprint.
	 * This method is invoked automatically by the form builder when displaying
	 * the itemCount field.
	 * 
	 * @param sprint the sprint entity to calculate item count for
	 * @return total number of sprint items */
	public Integer getItemCount(final CSprint sprint) {
		if (sprint == null) {
			LOGGER.warn("getItemCount called with null sprint");
			return 0;
		}
		return sprint.getItemCount(); // Delegates to entity method
	}

	/** Data provider callback: Calculates the total story points for all items in a sprint.
	 * This method is invoked automatically by the form builder when displaying
	 * the totalStoryPoints field.
	 * 
	 * @param sprint the sprint entity to calculate story points for
	 * @return sum of story points for all sprint items */
	public Long getTotalStoryPoints(final CSprint sprint) {
		if (sprint == null) {
			LOGGER.warn("getTotalStoryPoints called with null sprint");
			return 0L;
		}
		return sprint.getTotalStoryPoints(); // Delegates to entity method
	}
}
