package tech.derbent.plm.issues.issue.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.issues.issue.domain.CIssue;
import tech.derbent.plm.issues.issuetype.service.CIssueTypeService;
import tech.derbent.plm.sprints.domain.CSprint;

@Profile("derbent")
@Service
@PreAuthorize ("isAuthenticated()")
@PermitAll
public class CIssueService extends CProjectItemService<CIssue> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CIssueService.class);
	private final CIssueTypeService typeService;

	CIssueService(final IIssueRepository repository, final Clock clock, final ISessionService sessionService,
			final CIssueTypeService issueTypeService, final CProjectItemStatusService statusService) {
		super(repository, clock, sessionService, statusService);
		typeService = issueTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CIssue issue) {
		return super.checkDeleteAllowed(issue);
	}

	/**
	 * Copy CIssue-specific fields from source to target entity.
	 * Uses direct setter/getter calls for clarity.
	 * 
	 * @param source  the source entity to copy from
	 * @param target  the target entity to copy to
	 * @param options clone options controlling what fields to copy
	 */
	@Override
	public void copyEntityFieldsTo(final CIssue source, final CEntityDB<?> target, final CCloneOptions options) {
		super.copyEntityFieldsTo(source, target, options);

		if (!(target instanceof CIssue targetIssue)) {
			return;
		}

		// Copy basic fields using direct setter/getter
		targetIssue.setActualResult(source.getActualResult());
		targetIssue.setExpectedResult(source.getExpectedResult());
		targetIssue.setStepsToReproduce(source.getStepsToReproduce());
		targetIssue.setIssuePriority(source.getIssuePriority());
		targetIssue.setIssueSeverity(source.getIssueSeverity());
		targetIssue.setIssueResolution(source.getIssueResolution());
		targetIssue.setStoryPoint(source.getStoryPoint());

		// Conditional: dates
		if (!options.isResetDates()) {
			targetIssue.setDueDate(source.getDueDate());
			targetIssue.setResolvedDate(source.getResolvedDate());
		}

		// Conditional: relations
		if (options.includesRelations()) {
			targetIssue.setLinkedActivity(source.getLinkedActivity());
		}

		LOGGER.debug("Copied {} '{}' with options: {}", getClass().getSimpleName(), source.getName(), options);
	}

	@Override
	public Class<CIssue> getEntityClass() { return CIssue.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CIssueInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceIssue.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		initializeNewEntity_IHasStatusAndWorkflow((IHasStatusAndWorkflow<?>) entity, sessionService.getActiveCompany().orElseThrow(), typeService,
				statusService);
	}

	/** Get all issues in the project backlog (not assigned to any sprint).
	 * @param project the project
	 * @return list of backlog issues ordered by sprint order */
	public List<CIssue> listForProjectBacklog(final CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		return ((IIssueRepository) repository).listForProjectBacklog(project);
	}

	/** Get all issues assigned to a specific sprint.
	 * @param sprint the sprint
	 * @return list of issues in the sprint ordered by sprint item order */
	public List<CIssue> listForSprint(final CSprint sprint) {
		Check.notNull(sprint, "Sprint cannot be null");
		return ((IIssueRepository) repository).listForSprint(sprint);
	}

	@Override
	public CIssue save(final CIssue issue) {
		// Auto-set resolved date when status changes to Resolved/Closed
		final CProjectItemStatus status = issue.getStatus();
		final boolean condition =
				status != null && ("Resolved".equals(status.getName()) || "Closed".equals(status.getName())) && issue.getResolvedDate() == null;
		if (condition) {
			issue.setResolvedDate(LocalDate.now(clock));
			LOGGER.debug("Auto-setting resolved date for issue {}", issue.getId());
		}
		return super.save(issue);
	}

	@Override
	protected void validateEntity(final CIssue entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getEntityType(), "Issue type is required");
		Check.notNull(entity.getIssuePriority(), "Priority is required");
		Check.notNull(entity.getIssueSeverity(), "Severity is required");
		
		// 2. Length Checks - Use validateStringLength helper
		validateStringLength(entity.getActualResult(), "Actual Result", 2000);
		validateStringLength(entity.getExpectedResult(), "Expected Result", 2000);
		validateStringLength(entity.getStepsToReproduce(), "Steps to Reproduce", 4000);
		
		// 3. Unique Checks
		validateUniqueNameInProject((IIssueRepository) repository, entity, entity.getName(), entity.getProject());
	}
}
