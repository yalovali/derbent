package tech.derbent.plm.meetings.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entity.domain.CPageServiceMeeting;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.plm.meetings.domain.CMeeting;
import tech.derbent.plm.sprints.domain.CSprintItem;

import java.util.Optional;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.validation.ValidationMessages;

@Service
@PreAuthorize ("isAuthenticated()")
public class CMeetingService extends CProjectItemService<CMeeting> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CMeetingService.class);
	private final CMeetingTypeService meetingTypeService;

	CMeetingService(final IMeetingRepository repository, final Clock clock, final ISessionService sessionService,
			final CMeetingTypeService meetingTypeService, final CProjectItemStatusService projectItemStatusService) {
		super(repository, clock, sessionService, projectItemStatusService);
		this.meetingTypeService = meetingTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CMeeting entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	protected void validateEntity(final CMeeting entity) {
		super.validateEntity(entity);
		
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getEntityType(), "Meeting type is required");
		
		// 2. Length Checks
		if (entity.getName().length() > CEntityConstants.MAX_LENGTH_NAME) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength(ValidationMessages.NAME_MAX_LENGTH, CEntityConstants.MAX_LENGTH_NAME));
		}
		if (entity.getLocation() != null && entity.getLocation().length() > CEntityConstants.MAX_LENGTH_DESCRIPTION) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Location cannot exceed %d characters", CEntityConstants.MAX_LENGTH_DESCRIPTION));
		}
		if (entity.getLinkedElement() != null && entity.getLinkedElement().length() > CEntityConstants.MAX_LENGTH_DESCRIPTION) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Linked Element cannot exceed %d characters", CEntityConstants.MAX_LENGTH_DESCRIPTION));
		}
		if (entity.getAgenda() != null && entity.getAgenda().length() > 4000) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Agenda cannot exceed %d characters", 4000));
		}
		if (entity.getMinutes() != null && entity.getMinutes().length() > 4000) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Minutes cannot exceed %d characters", 4000));
		}
		
		// 3. Unique Checks
		// Name must be unique within project
		final Optional<CMeeting> existingName = ((IMeetingRepository) repository).findByNameAndProject(entity.getName(), entity.getProject());
		if (existingName.isPresent() && !existingName.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_PROJECT);
		}
		
		// 4. Date Logic
		if (entity.getStartDate() != null && entity.getEndDate() != null) {
			if (entity.getEndDate().isBefore(entity.getStartDate())) {
				throw new IllegalArgumentException("End date cannot be before start date");
			}
		}
		if (entity.getStartTime() != null && entity.getEndTime() != null && entity.getStartDate() != null && entity.getEndDate() != null && entity.getStartDate().equals(entity.getEndDate())) {
			if (entity.getEndTime().isBefore(entity.getStartTime())) {
				throw new IllegalArgumentException("End time cannot be before start time on the same day");
			}
		}
	}

	@Override
	@Transactional
	public void delete(final CMeeting meeting) {
		Check.notNull(meeting, "Meeting cannot be null");
		Check.notNull(meeting.getId(), "Meeting ID cannot be null");
		// The OneToOne relationship with cascade = CascadeType.ALL and orphanRemoval =
		// true
		// will automatically delete the sprint item when the meeting is deleted.
		// No need to manually detach and save, which would violate @NotNull constraint.
		super.delete(meeting);
	}

	@Override
	@Transactional
	public void delete(final Long id) {
		Check.notNull(id, "Meeting ID cannot be null");
		final CMeeting meeting =
				repository.findById(id).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Meeting not found: " + id));
		delete(meeting);
	}

	@Override
	public Class<CMeeting> getEntityClass() { return CMeeting.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CMeetingInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceMeeting.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@SuppressWarnings ("null")
	@Override
	public void initializeNewEntity(final CMeeting entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new meeting entity");
		final CProject<?> currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize meeting"));
		final CUser currentUser = sessionService.getActiveUser()
				.orElseThrow(() -> new CInitializationException("No active user in session - cannot initialize meeting"));
		entity.initializeDefaults_IHasStatusAndWorkflow(currentProject, meetingTypeService, projectItemStatusService);
		
		// Contextual initialization (User, Project-specific)
		entity.setAssignedTo(currentUser);
		
		// Update sprint item with context-aware dates if needed
		// Note: basic structure is created in entity.initializeDefaults()
		CSprintItem sprintItem = entity.getSprintItem();
		if (sprintItem != null) {
			sprintItem.setStartDate(entity.getStartDate());
			sprintItem.setDueDate(entity.getEndDate());
		}
		
		LOGGER.debug("Meeting initialization complete");
	}

	/** Lists meetings by project ordered by sprintOrder for sprint-aware components. Items with null sprintOrder will appear last.
	 * @param project the project
	 * @return list of meetings ordered by sprintOrder ASC, id DESC */
	public java.util.List<CMeeting> listForProjectBacklog(final CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		return ((IMeetingRepository) repository).listForProjectBacklog(project);
	}
}
