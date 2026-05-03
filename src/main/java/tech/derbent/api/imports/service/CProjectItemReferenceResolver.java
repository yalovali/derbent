package tech.derbent.api.imports.service;

import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.plm.activities.domain.CActivity;
import tech.derbent.plm.activities.service.CActivityService;
import tech.derbent.plm.agile.domain.CEpic;
import tech.derbent.plm.agile.domain.CFeature;
import tech.derbent.plm.agile.domain.CUserStory;
import tech.derbent.plm.agile.service.CEpicService;
import tech.derbent.plm.agile.service.CFeatureService;
import tech.derbent.plm.agile.service.CUserStoryService;
import tech.derbent.plm.decisions.domain.CDecision;
import tech.derbent.plm.decisions.service.CDecisionService;
import tech.derbent.plm.issues.issue.domain.CIssue;
import tech.derbent.plm.issues.issue.service.CIssueService;
import tech.derbent.plm.meetings.domain.CMeeting;
import tech.derbent.plm.meetings.service.CMeetingService;
import tech.derbent.plm.sprints.domain.CSprint;
import tech.derbent.plm.sprints.service.CSprintService;
import tech.derbent.plm.tickets.ticket.domain.CTicket;
import tech.derbent.plm.tickets.ticket.service.CTicketService;

/**
 * Resolves project-scoped entities by a simple "Type + Name" reference.
 *
 * WHY: Excel init workbooks should be authorable by humans; referencing entities by (type, name)
 * is far more stable than database IDs in CI and test environments.
 */
@Service
@Profile({"derbent", "default"})
public class CProjectItemReferenceResolver {

	private final CActivityService activityService;
	private final CIssueService issueService;
	private final CMeetingService meetingService;
	private final CDecisionService decisionService;
	private final CEpicService epicService;
	private final CFeatureService featureService;
	private final CUserStoryService userStoryService;
	private final CTicketService ticketService;
	private final CSprintService sprintService;

	public CProjectItemReferenceResolver(final CActivityService activityService, final CIssueService issueService,
			final CMeetingService meetingService, final CDecisionService decisionService, final CEpicService epicService,
			final CFeatureService featureService, final CUserStoryService userStoryService, final CTicketService ticketService,
			final CSprintService sprintService) {
		this.activityService = activityService;
		this.issueService = issueService;
		this.meetingService = meetingService;
		this.decisionService = decisionService;
		this.epicService = epicService;
		this.featureService = featureService;
		this.userStoryService = userStoryService;
		this.ticketService = ticketService;
		this.sprintService = sprintService;
	}

	public Optional<CProjectItem<?, ?>> findByTypeAndName(final String typeToken, final String name,
			final CProject<?> project) {
		if (typeToken == null || typeToken.isBlank() || name == null || name.isBlank() || project == null) {
			return Optional.empty();
		}
		final String type = normalizeType(typeToken);
		final String normalizedName = name.trim();
		return switch (type) {
			case "activity" -> activityService.findByNameAndProject(normalizedName, project).map(a -> (CProjectItem<?, ?>) a);
			case "issue" -> issueService.findByNameAndProject(normalizedName, project).map(i -> (CProjectItem<?, ?>) i);
			case "meeting" -> meetingService.findByNameAndProject(normalizedName, project).map(m -> (CProjectItem<?, ?>) m);
			case "decision" -> decisionService.findByNameAndProject(normalizedName, project).map(d -> (CProjectItem<?, ?>) d);
			case "epic" -> epicService.findByNameAndProject(normalizedName, project).map(e -> (CProjectItem<?, ?>) e);
			case "feature" -> featureService.findByNameAndProject(normalizedName, project).map(f -> (CProjectItem<?, ?>) f);
			case "userstory" -> userStoryService.findByNameAndProject(normalizedName, project).map(u -> (CProjectItem<?, ?>) u);
			case "ticket" -> ticketService.findByNameAndProject(normalizedName, project).map(t -> (CProjectItem<?, ?>) t);
			case "sprint" -> sprintService.findByNameAndProject(normalizedName, project).map(s -> (CProjectItem<?, ?>) s);
			default -> Optional.empty();
		};
	}

	public void save(final CProjectItem<?, ?> item) {
		if (item == null) {
			return;
		}
		if (item instanceof final CActivity activity) {
			activityService.save(activity);
			return;
		}
		if (item instanceof final CIssue issue) {
			issueService.save(issue);
			return;
		}
		if (item instanceof final CMeeting meeting) {
			meetingService.save(meeting);
			return;
		}
		if (item instanceof final CDecision decision) {
			decisionService.save(decision);
			return;
		}
		if (item instanceof final CEpic epic) {
			epicService.save(epic);
			return;
		}
		if (item instanceof final CFeature feature) {
			featureService.save(feature);
			return;
		}
		if (item instanceof final CUserStory userStory) {
			userStoryService.save(userStory);
			return;
		}
		if (item instanceof final CTicket ticket) {
			ticketService.save(ticket);
			return;
		}
		if (item instanceof final CSprint sprint) {
			sprintService.save(sprint);
			return;
		}
		throw new IllegalArgumentException("Unsupported project item type: " + item.getClass().getSimpleName());
	}

	private static String normalizeType(final String value) {
		final String t = value.trim().toLowerCase().replaceAll("\\s+", "");
		return t.startsWith("c") ? t.substring(1) : t;
	}
}
