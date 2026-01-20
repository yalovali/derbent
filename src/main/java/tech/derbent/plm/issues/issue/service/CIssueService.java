package tech.derbent.plm.issues.issue.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.issues.issue.domain.CIssue;
import tech.derbent.plm.issues.issue.domain.EIssuePriority;
import tech.derbent.plm.issues.issue.domain.EIssueResolution;
import tech.derbent.plm.issues.issue.domain.EIssueSeverity;
import tech.derbent.plm.issues.issuetype.service.CIssueTypeService;
import tech.derbent.plm.sprints.domain.CSprint;

import java.util.Optional;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.validation.ValidationMessages;

@Service
@PreAuthorize ("isAuthenticated()")
@Menu (icon = "vaadin:bug", title = "Settings.Issues")
@PermitAll
public class CIssueService extends CProjectItemService<CIssue> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CIssueService.class);
	private final CIssueTypeService issueTypeService;

	CIssueService(final IIssueRepository repository, final Clock clock, final ISessionService sessionService,
			final CIssueTypeService issueTypeService, final CProjectItemStatusService projectItemStatusService) {
		super(repository, clock, sessionService, projectItemStatusService);
		this.issueTypeService = issueTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CIssue issue) {
		return super.checkDeleteAllowed(issue);
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
		
		// 2. Length Checks
		if (entity.getName().length() > CEntityConstants.MAX_LENGTH_NAME) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength(ValidationMessages.NAME_MAX_LENGTH, CEntityConstants.MAX_LENGTH_NAME));
		}
		if (entity.getActualResult() != null && entity.getActualResult().length() > 2000) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Actual Result cannot exceed %d characters", 2000));
		}
		if (entity.getExpectedResult() != null && entity.getExpectedResult().length() > 2000) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Expected Result cannot exceed %d characters", 2000));
		}
		if (entity.getStepsToReproduce() != null && entity.getStepsToReproduce().length() > 4000) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Steps to Reproduce cannot exceed %d characters", 4000));
		}
		
		// 3. Unique Checks
		final Optional<CIssue> existingName = ((IIssueRepository) repository).findByNameAndProject(entity.getName(), entity.getProject());
		if (existingName.isPresent() && !existingName.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_PROJECT);
		}
	}

	@Override
	public Class<CIssue> getEntityClass() { return CIssue.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CIssueInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceIssue.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@SuppressWarnings ("null")
	@Override
	public void initializeNewEntity(final CIssue entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new issue entity");
		final CProject<?> currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize issue"));
		// Initialize workflow-based status and type
		IHasStatusAndWorkflowService.initializeNewEntity(entity, currentProject, issueTypeService, projectItemStatusService);
		// Initialize issue-specific fields with sensible defaults
		entity.setIssueSeverity(EIssueSeverity.MINOR);
		entity.setIssuePriority(EIssuePriority.MEDIUM);
		entity.setIssueResolution(EIssueResolution.NONE);
		LOGGER.debug("Issue initialization complete with defaults: severity=MINOR, priority=MEDIUM, resolution=NONE");
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
		if (status != null && (status.getName().equals("Resolved") || status.getName().equals("Closed"))) {
			if (issue.getResolvedDate() == null) {
				issue.setResolvedDate(LocalDate.now(clock));
				LOGGER.debug("Auto-setting resolved date for issue {}", issue.getId());
			}
		}
		return super.save(issue);
	}
}
