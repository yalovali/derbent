package tech.derbent.plm.agile.service;

import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.agileparentrelation.service.CAgileParentRelationInitializerService;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.api.screens.service.CInitializerServiceProjectItem;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.service.CUserService;
import tech.derbent.plm.activities.domain.CActivityPriority;
import tech.derbent.plm.activities.service.CActivityPriorityService;
import tech.derbent.plm.agile.domain.CEpic;
import tech.derbent.plm.agile.domain.CFeature;
import tech.derbent.plm.agile.domain.CFeatureType;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.service.CLinkInitializerService;

public class CFeatureInitializerService extends CInitializerServiceProjectItem {

	static final Class<?> clazz = CFeature.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CFeatureInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".12";
	private static final String menuTitle = MenuTitle_PROJECT + ".Features";
	private static final String pageDescription = "Feature management for projects";
	private static final String pageTitle = "Feature Management";
	private static final boolean showInQuickToolbar = true;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			final CDetailSection scr = createBaseScreenEntity(project, clazz);
			CInitializerServiceNamedEntity.createBasicView(scr, clazz, project, true);
			scr.addScreenLine(CDetailLinesService.createSection("System Access"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "entityType"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "assignedTo"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));
			scr.addScreenLine(CDetailLinesService.createSection("Schedule"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "startDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "dueDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "completionDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "progressPercentage"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "estimatedHours"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "actualHours"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "remainingHours"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "status"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "priority"));
			scr.addScreenLine(CDetailLinesService.createSection("Financials"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "estimatedCost"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "actualCost"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "hourlyRate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "acceptanceCriteria"));
			// Attachments section - standard section for ALL entities
			CAttachmentInitializerService.addDefaultSection(scr, clazz);
			// Links section - standard section for entities that can be linked
			CLinkInitializerService.addDefaultSection(scr, clazz);
			// Comments section - standard section for discussion entities
			CCommentInitializerService.addDefaultSection(scr, clazz);
			scr.addScreenLine(CDetailLinesService.createSection("Additional Information"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "notes"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "results"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "parentId"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "parentType"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
			CAgileParentRelationInitializerService.addDefaultSection(scr, clazz, project);
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			LOGGER.error("Error creating feature view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "componentWidget", "entityType", "assignedTo", "createdBy", "startDate", "dueDate",
				"completionDate", "progressPercentage", "estimatedHours", "actualHours", "remainingHours", "status", "priority", "project",
				"createdDate", "lastModifiedDate"));
		return grid;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	/** Initialize sample features for a project.
	 * @param project     the project to create features for
	 * @param minimal     if true, creates only 1 feature; if false, creates 2 features
	 * @param sampleEpic1 the first epic to link features to (can be null)
	 * @param sampleEpic2 the second epic to link second feature to (can be null)
	 * @return array of created features [feature1, feature2] where feature2 may be null if minimal is true */
	public static CFeature[] initializeSample(final CProject<?> project, final boolean minimal, final CEpic sampleEpic1, final CEpic sampleEpic2)
			throws Exception {
		// Seed data for sample features with parent epic index
		record FeatureSeed(String name, String description, int parentEpicIndex) {}
		final List<FeatureSeed> seeds = List.of(
				new FeatureSeed("Real-time Notifications System", "Implement real-time notification system with push, email, and in-app delivery", 0),
				new FeatureSeed("Advanced Search and Filtering", "Add advanced search capabilities with filters, sorting, and saved searches", 1));
		try {
			final CFeatureService featureService = CSpringContext.getBean(CFeatureService.class);
			final CFeatureTypeService featureTypeService = CSpringContext.getBean(CFeatureTypeService.class);
			final CActivityPriorityService activityPriorityService = CSpringContext.getBean(CActivityPriorityService.class);
			final CUserService userService = CSpringContext.getBean(CUserService.class);
			final CProjectItemStatusService statusService = CSpringContext.getBean(CProjectItemStatusService.class);
			final CEpic[] parentEpics = {
					sampleEpic1, sampleEpic2
			};
			final CFeature[] createdFeatures = new CFeature[2];
			int index = 0;
			for (final FeatureSeed seed : seeds) {
				final CFeatureType type = featureTypeService.getRandom(project.getCompany());
				final CActivityPriority priority = activityPriorityService.getRandom(project.getCompany());
				final CUser user = userService.getRandom(project.getCompany());
				CFeature feature = new CFeature(seed.name(), project);
				feature.setDescription(seed.description());
				feature.setEntityType(type);
				feature.setPriority(priority);
				feature.setAssignedTo(user);
				feature.setStartDate(LocalDate.now().plusDays((int) (Math.random() * 120)));
				feature.setDueDate(feature.getStartDate().plusDays((long) (Math.random() * 90)));
				if (type != null && type.getWorkflow() != null) {
					final List<CProjectItemStatus> initialStatuses = statusService.getValidNextStatuses(feature);
					if (!initialStatuses.isEmpty()) {
						feature.setStatus(initialStatuses.get(0));
					}
				}
				// Link Feature to Epic parent
				final CEpic parentEpic = parentEpics[seed.parentEpicIndex()];
				if (parentEpic != null) {
					feature.setParentEpic(parentEpic);
				} else if (sampleEpic1 != null) {
					// Fallback to first epic if specified parent not available
					feature.setParentEpic(sampleEpic1);
				}
				feature = featureService.save(feature);
				createdFeatures[index++] = feature;
				// LOGGER.info("Created Feature '{}' (ID: {}) with parent Epic '{}'", feature.getName(), feature.getId(),feature.getParentEpic() !=
				// null ? feature.getParentEpic().getName() : "NONE");
				if (minimal) {
					break;
				}
			}
			LOGGER.debug("Created {} sample feature(s) for project: {}", index, project.getName());
			return createdFeatures;
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample features for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize sample features for project: " + project.getName(), e);
		}
	}
}
