package tech.derbent.plm.meetings.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.entity.domain.CPageServiceMeeting;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.api.agileparentrelation.domain.CAgileParentRelation;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.meetings.domain.CMeeting;
import tech.derbent.plm.sprints.domain.CSprintItem;

@Service
@PreAuthorize ("isAuthenticated()")
public class CMeetingService extends CProjectItemService<CMeeting> implements IEntityRegistrable, IEntityWithView {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CMeetingService.class);
	private final CMeetingTypeService typeService;

	CMeetingService(final IMeetingRepository repository, final Clock clock, final ISessionService sessionService,
			final CMeetingTypeService meetingTypeService, final CProjectItemStatusService statusService) {
		super(repository, clock, sessionService, statusService);
		typeService = meetingTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CMeeting entity) {
		return super.checkDeleteAllowed(entity);
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

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		final CMeeting meeting = (CMeeting) entity;
		initializeNewEntity_IHasStatusAndWorkflow((IHasStatusAndWorkflow<?>) entity, sessionService.getActiveCompany().orElseThrow(), typeService,
				statusService);
		if (meeting.getAgileParentRelation() == null) {
			meeting.setAgileParentRelation(new CAgileParentRelation(meeting));
		} else {
			meeting.getAgileParentRelation().setOwnerItem(meeting);
		}
		final CSprintItem sprintItem = meeting.getSprintItem();
		if (sprintItem == null) {
			return;
		}
		sprintItem.setStartDate(meeting.getStartDate());
		sprintItem.setDueDate(meeting.getEndDate());
	}

	/** Lists meetings by project ordered by sprintOrder for sprint-aware components. Items with null sprintOrder will appear last.
	 * @param project the project
	 * @return list of meetings ordered by sprintOrder ASC, id DESC */
	public java.util.List<CMeeting> listForProjectBacklog(final CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		return ((IMeetingRepository) repository).listForProjectBacklog(project);
	}

	@Override
	protected void validateEntity(final CMeeting entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getEntityType(), "Meeting type is required");
		if (entity.getLocation() != null && entity.getLocation().length() > CEntityConstants.MAX_LENGTH_DESCRIPTION) {
			throw new IllegalArgumentException(
					ValidationMessages.formatMaxLength("Location cannot exceed %d characters", CEntityConstants.MAX_LENGTH_DESCRIPTION));
		}
		if (entity.getLinkedElement() != null && entity.getLinkedElement().length() > CEntityConstants.MAX_LENGTH_DESCRIPTION) {
			throw new IllegalArgumentException(
					ValidationMessages.formatMaxLength("Linked Element cannot exceed %d characters", CEntityConstants.MAX_LENGTH_DESCRIPTION));
		}
		if (entity.getAgenda() != null && entity.getAgenda().length() > 4000) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Agenda cannot exceed %d characters", 4000));
		}
		if (entity.getMinutes() != null && entity.getMinutes().length() > 4000) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Minutes cannot exceed %d characters", 4000));
		}
		// 3. Unique Checks
		// Name must be unique within project
		validateUniqueNameInProject((IMeetingRepository) repository, entity, entity.getName(), entity.getProject());
		final boolean condition = entity.getStartDate() != null && entity.getEndDate() != null && entity.getEndDate().isBefore(entity.getStartDate());
		// 4. Date Logic
		if (condition) {
			throw new IllegalArgumentException("End date cannot be before start date");
		}
		final boolean condition1 = entity.getStartTime() != null && entity.getEndTime() != null && entity.getStartDate() != null && entity.getEndDate() != null
				&& entity.getStartDate().equals(entity.getEndDate()) && entity.getEndTime().isBefore(entity.getStartTime());
		if (condition1) {
			throw new IllegalArgumentException("End time cannot be before start time on the same day");
		}
	}
}
