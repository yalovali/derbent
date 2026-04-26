package tech.derbent.plm.sprints.service;

import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.api.screens.service.CInitializerServiceProjectItem;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.users.service.CUserService;
import tech.derbent.plm.activities.domain.CActivity;
import tech.derbent.plm.activities.service.CActivityService;
import tech.derbent.plm.agile.domain.CFeature;
import tech.derbent.plm.agile.domain.CUserStory;
import tech.derbent.plm.agile.service.CFeatureService;
import tech.derbent.plm.agile.service.CUserStoryService;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.issues.issue.domain.CIssue;
import tech.derbent.plm.issues.issue.service.CIssueService;
import tech.derbent.plm.sprints.domain.CSprint;
import tech.derbent.plm.sprints.domain.CSprintType;

/** CSprintInitializerService - Initializer service for sprint management. Creates UI configuration and sample data for sprints. */
public class CSprintInitializerService extends CInitializerServiceProjectItem {

	static final Class<?> clazz = CSprint.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CSprintInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".3";
	private static final String menuTitle = MenuTitle_PROJECT + ".Sprints";
	private static final String pageDescription = "Sprint management for agile development";
	private static final String pageTitle = "Sprint Management";
	private static final boolean showInQuickToolbar = false;

	private static void addSprintItemIfPresent(final CSprint sprint, final List<? extends ISprintableItem> items, final int index,
			final Long storyPoints) {
		if (items == null || items.isEmpty()) {
			return;
		}
		if (index < 0 || index >= items.size()) {
			return;
		}
		final ISprintableItem item = items.get(index);
		if (item == null) {
			return;
		}
		sprint.addItem(item);
		if (item.getSprintItem() != null && storyPoints != null) {
			item.getSprintItem().setStoryPoint(storyPoints);
		}
		item.saveProjectItem();
	}

	private static void addSprintItems(final CSprint sprint, final List<? extends ISprintableItem> items, final int[] indexes,
			final Long[] storyPoints) {
		if (indexes == null) {
			return;
		}
		for (int i = 0; i < indexes.length; i++) {
			final Long storyPoint = storyPoints != null && i < storyPoints.length ? storyPoints[i] : null;
			addSprintItemIfPresent(sprint, items, indexes[i], storyPoint);
		}
	}

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			final CDetailSection scr = createBaseScreenEntity(project, clazz);
			CInitializerServiceNamedEntity.createBasicView(scr, clazz, project, true);
			scr.addScreenLine(CDetailLinesService.createSection("Sprint Details"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "entityType"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "color"));
			// Scrum Guide 2020 - Sprint Goal & Definition of Done
			scr.addScreenLine(CDetailLinesService.createSection("Scrum Guide 2020 - Sprint Artifacts"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "sprintGoal"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "definitionOfDone"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "velocity"));
			scr.addScreenLine(CDetailLinesService.createSection("Schedule"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "startDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "endDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "status"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "assignedTo"));
			scr.addScreenLine(CDetailLinesService.createSection("Sprint Items"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "sprintItems"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "itemCount"));
			// Scrum Guide 2020 - Sprint Retrospective
			scr.addScreenLine(CDetailLinesService.createSection("Sprint Retrospective (Scrum Guide 2020)"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "retrospectiveNotes"));
			scr.addScreenLine(CDetailLinesService.createSection("Additional Information"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
			// Attachments section - standard section for ALL entities
			CAttachmentInitializerService.addDefaultSection(scr, clazz);
			// Comments section - standard section for discussion entities
			CCommentInitializerService.addDefaultSection(scr, clazz);
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			LOGGER.error("Error creating sprint view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "entityType", "description", "startDate", "endDate", "status", "color", "assignedTo", "itemCount",
				"project", "createdDate", "lastModifiedDate"));
		return grid;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, MenuTitle_DEVELOPMENT + menuTitle,
				pageTitle, pageDescription, showInQuickToolbar, menuOrder, null);
	}

	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		try {
			record SprintSeed(String name, String description, String sprintGoal, String definitionOfDone, String retrospectiveNotes,
					int startOffsetWeeks, int durationDays, Integer velocity, int[] userStoryIndexes, Long[] userStoryPoints, int[] activityIndexes,
					Long[] activityPoints, int[] issueIndexes, Long[] issuePoints, int[] featureIndexes, Long[] featurePoints) {}
			final CSprintService sprintService = CSpringContext.getBean(CSprintService.class);
			final CSprintTypeService sprintTypeService = CSpringContext.getBean(CSprintTypeService.class);
			final CUserService userService = CSpringContext.getBean(CUserService.class);
			final CActivityService activityService = CSpringContext.getBean(CActivityService.class);
			final CProjectItemStatusService statusService = CSpringContext.getBean(CProjectItemStatusService.class);
			final List<CSprintType> availableTypes = sprintTypeService.listByCompany(project.getCompany());
			final List<CUser> availableUsers = userService.listByCompany(project.getCompany());
			final LocalDate baseSprintStart = LocalDate.now().minusWeeks(6);
			final String standardDefinitionOfDone = "- Acceptance criteria verified\n- Peer review completed\n- Automated tests green\n"
					+ "- Demo notes prepared\n- Documentation updated where needed";
			if (minimal) {
				final CUserStoryService userStoryService = CSpringContext.getBean(CUserStoryService.class);
				final CIssueService issueService = CSpringContext.getBean(CIssueService.class);
				final List<CUserStory> userStories = userStoryService.listByProject(project);
				final List<CActivity> activities = activityService.listByProject(project);
				final List<CIssue> issues = issueService.listByProject(project);
				final CSprintType sprintType = availableTypes.isEmpty() ? sprintTypeService.getRandom(project.getCompany()) : availableTypes.get(0);
				final CUser assignedUser = availableUsers.isEmpty() ? userService.getRandom(project.getCompany()) : availableUsers.get(0);
				final CSprint sprint = new CSprint("Sprint 1 - Identity Kickoff", project);
				sprint.setDescription("Initial delivery sprint for the identity modernization stream.");
				sprint.setEntityType(sprintType);
				sprint.setAssignedTo(assignedUser);
				sprint.setColor(CSprint.DEFAULT_COLOR);
				sprint.setStartDate(baseSprintStart.plusWeeks(2));
				sprint.setEndDate(sprint.getStartDate().plusDays(13));
				sprint.setSprintGoal("Deliver the first working MFA enrollment slice and prove audit-ready session controls.");
				sprint.setDefinitionOfDone(standardDefinitionOfDone);
				sprint.setVelocity(13);
				if (sprintType != null && sprintType.getWorkflow() != null) {
					final List<CProjectItemStatus> initialStatuses = statusService.getValidNextStatuses(sprint);
					if (!initialStatuses.isEmpty()) {
						sprint.setStatus(initialStatuses.get(0));
					}
				}
				final CSprint savedSprint = sprintService.save(sprint);
				addSprintItems(savedSprint, userStories, new int[] {
						0
				}, new Long[] {
						8L
				});
				addSprintItems(savedSprint, activities, new int[] {
						0, 1
				}, new Long[] {
						3L, 5L
				});
				addSprintItems(savedSprint, issues, new int[] {
						0
				}, new Long[] {
						2L
				});
				sprintService.save(savedSprint);
			} else {
				final CUserStoryService userStoryService = CSpringContext.getBean(CUserStoryService.class);
				final CFeatureService featureService = CSpringContext.getBean(CFeatureService.class);
				final CIssueService issueService = CSpringContext.getBean(CIssueService.class);
				final List<CUserStory> userStories = userStoryService.listByProject(project);
				final List<CFeature> features = featureService.listByProject(project);
				final List<CActivity> activities = activityService.listByProject(project);
				final List<CIssue> issues = issueService.listByProject(project);
				final List<SprintSeed> sprintSeeds = List.of(new SprintSeed("Sprint 1 - Identity Foundation",
						"Focus the team on MFA enrollment, session protection, and early audit readiness.",
						"Deliver the first secure sign-in slice and prove session control workflows with stakeholders.", standardDefinitionOfDone,
						"WHAT WENT WELL:\n- Security and UX worked from one backlog\n- API contract stabilized early\n\nWHAT TO IMPROVE:\n- Recovery code copy needs UX polish\n- Audit search needs faster fixtures",
						0, 13, 21, new int[] {
								0, 1
						}, new Long[] {
								8L, 5L
						}, new int[] {
								0, 1, 2
						}, new Long[] {
								3L, 5L, 2L
						}, new int[] {
								0
						}, new Long[] {
								2L
						}, new int[] {
								0
						}, new Long[] {
								13L
						}),
						new SprintSeed("Sprint 2 - Workspace Flows", "Shift delivery to customer workspace profile and saved-view capabilities.",
								"Enable customers to manage contacts and save daily workspace filters without support help.",
								standardDefinitionOfDone,
								"WHAT WENT WELL:\n- Profile form validations were straightforward\n- Early pilot feedback improved saved filter naming\n\nACTION ITEMS:\n- Tighten cross-system sync monitoring\n- Split dashboard polish into future backlog",
								2, 13, 24, new int[] {
										2, 3
								}, new Long[] {
										5L, 8L
								}, new int[] {
										3, 4, 5
								}, new Long[] {
										3L, 5L, 5L
								}, new int[] {
										1
								}, new Long[] {
										3L
								}, new int[] {
										2
								}, new Long[] {
										8L
								}),
						new SprintSeed("Sprint 3 - Billing Operations",
								"Move dispute-handling work into execution while preserving room for release preparation.",
								"Establish dispute triage, evidence handling, and SLA visibility for finance operations.", standardDefinitionOfDone,
								"", 4, 13, 18, new int[] {
										4, 5
								}, new Long[] {
										8L, 5L
								}, new int[] {
										6
								}, new Long[] {
										3L
								}, new int[] {
										2
								}, new Long[] {
										2L
								}, new int[] {
										4
								}, new Long[] {
										8L
								}),
						new SprintSeed("Sprint 4 - Release Hardening",
								"Prepare the release command center and go-live controls while keeping a visible future backlog.",
								"Validate release blockers, launch checklist automation, and command center ownership.", standardDefinitionOfDone, "",
								6, 13, 13, new int[] {
										6
								}, new Long[] {
										3L
								}, new int[] {
										8
								}, new Long[] {
										5L
								}, new int[] {
										3
								}, new Long[] {
										2L
								}, new int[] {
										5
								}, new Long[] {
										5L
								}));
				int sprintIndex = 0;
				for (final SprintSeed seed : sprintSeeds) {
					final CSprintType sprintType = availableTypes.isEmpty() ? sprintTypeService.getRandom(project.getCompany())
							: availableTypes.get(sprintIndex % availableTypes.size());
					final CUser assignedUser = availableUsers.isEmpty() ? userService.getRandom(project.getCompany())
							: availableUsers.get(sprintIndex % availableUsers.size());
					final CSprint sprint = new CSprint(seed.name(), project);
					sprint.setDescription(seed.description());
					sprint.setEntityType(sprintType);
					sprint.setAssignedTo(assignedUser);
					sprint.setColor(CSprint.DEFAULT_COLOR);
					sprint.setStartDate(baseSprintStart.plusWeeks(seed.startOffsetWeeks()));
					sprint.setEndDate(sprint.getStartDate().plusDays(seed.durationDays()));
					sprint.setSprintGoal(seed.sprintGoal());
					sprint.setDefinitionOfDone(seed.definitionOfDone());
					sprint.setRetrospectiveNotes(seed.retrospectiveNotes());
					sprint.setVelocity(seed.velocity());
					if (sprintType != null && sprintType.getWorkflow() != null) {
						final List<CProjectItemStatus> initialStatuses = statusService.getValidNextStatuses(sprint);
						if (!initialStatuses.isEmpty()) {
							sprint.setStatus(initialStatuses.get(0));
						}
					}
					final CSprint savedSprint = sprintService.save(sprint);
					addSprintItems(savedSprint, userStories, seed.userStoryIndexes(), seed.userStoryPoints());
					addSprintItems(savedSprint, activities, seed.activityIndexes(), seed.activityPoints());
					addSprintItems(savedSprint, issues, seed.issueIndexes(), seed.issuePoints());
					addSprintItems(savedSprint, features, seed.featureIndexes(), seed.featurePoints());
					sprintService.save(savedSprint);
					sprintIndex++;
				}
			}
		} catch (final Exception e) {
			LOGGER.error("Error creating sample sprints reason={}", e.getMessage());
			throw e;
		}
	}
}
