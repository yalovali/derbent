package tech.derbent.plm.meetings.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
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

@Profile("derbent")
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
		
		// 2. Length Checks - Use validateStringLength helper
		validateStringLength(entity.getLocation(), "Location", CEntityConstants.MAX_LENGTH_DESCRIPTION);
		validateStringLength(entity.getLinkedElement(), "Linked Element", CEntityConstants.MAX_LENGTH_DESCRIPTION);
		validateStringLength(entity.getAgenda(), "Agenda", 4000);
		validateStringLength(entity.getMinutes(), "Minutes", 4000);
		
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
	
	/** Service-level method to copy CMeeting-specific fields using getters/setters.
	 * This method implements the service-based copy pattern for Meeting entities.
	 * 
	 * @param source  the source meeting to copy from
	 * @param target  the target entity to copy to
	 * @param options clone options controlling what fields to copy */
	@Override
	public void copyEntityFieldsTo(final CMeeting source, final tech.derbent.api.entity.domain.CEntityDB<?> target,
			final tech.derbent.api.interfaces.CCloneOptions options) {
		// Call parent to copy project item fields
		super.copyEntityFieldsTo(source, target, options);
		
		// Only copy if target is a Meeting
		if (!(target instanceof CMeeting)) {
			return;
		}
		final CMeeting targetMeeting = (CMeeting) target;
		
		// Copy basic meeting fields - direct setter/getter
		targetMeeting.setAgenda(source.getAgenda());
		targetMeeting.setLinkedElement(source.getLinkedElement());
		targetMeeting.setLocation(source.getLocation());
		targetMeeting.setMinutes(source.getMinutes());
		targetMeeting.setEntityType(source.getEntityType());
		
		// Handle date/time fields based on options
		if (!options.isResetDates()) {
			targetMeeting.setEndDate(source.getEndDate());
			targetMeeting.setEndTime(source.getEndTime());
			targetMeeting.setStartDate(source.getStartDate());
			targetMeeting.setStartTime(source.getStartTime());
		}
		
		// Copy related activity if relations are included
		if (options.includesRelations()) {
			targetMeeting.setRelatedActivity(source.getRelatedActivity());
			
			// Clone attendees and participants collections
			if (source.getAttendees() != null) {
				targetMeeting.setAttendees(new java.util.HashSet<>(source.getAttendees()));
			}
			if (source.getParticipants() != null) {
				targetMeeting.setParticipants(new java.util.HashSet<>(source.getParticipants()));
			}
		}
		
		// Note: Action items are not cloned to avoid creating duplicate tasks
		// Note: Sprint item relationship is not cloned - clone starts outside sprint
		// Note: Comments, attachments, and status/workflow are copied automatically by base class
		
		LOGGER.debug("Successfully copied meeting '{}' with options: {}", source.getName(), options);
	}
}
