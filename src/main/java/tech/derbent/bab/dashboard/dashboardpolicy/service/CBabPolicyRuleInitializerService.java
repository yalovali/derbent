package tech.derbent.bab.dashboard.dashboardpolicy.service;

import java.util.HashSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.bab.dashboard.dashboardpolicy.domain.CBabPolicyRule;
import tech.derbent.bab.policybase.action.domain.CBabPolicyAction;
import tech.derbent.bab.policybase.action.service.CBabPolicyActionService;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilter;
import tech.derbent.bab.policybase.filter.service.CBabPolicyFilterService;
import tech.derbent.bab.policybase.trigger.domain.CBabPolicyTrigger;
import tech.derbent.bab.policybase.trigger.service.CBabPolicyTriggerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;

@Service
@Profile ("bab")
public final class CBabPolicyRuleInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CBabPolicyRule.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyRuleInitializerService.class);
	private static final String menuOrder = "20.0";
	private static final String menuTitle = "Policies.Policy Rules";
	private static final String pageDescription = "BAB policy rule configuration and management";
	private static final String pageTitle = "Policy Rules";
	private static final boolean showInQuickToolbar = true;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			final CDetailSection scr = createBaseScreenEntity(project, clazz);
			CInitializerServiceNamedEntity.createBasicView(scr, clazz, project, true);
			// Policy rule specific fields
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isActive", true, ""));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "rulePriority", true, ""));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "executionOrder", false, "100%"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "trigger"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "sourceNode", true, ""));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "destinationNode"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "filter"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "actions"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "logEnabled"));
			scr.addScreenLine(CDetailLinesService.createSection("Project Context"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "assignedTo", true, ""));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate", true, ""));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			CCommentInitializerService.addDefaultSection(scr, clazz);
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			LOGGER.error("Error creating policy rule view", e);
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "isActive", "rulePriority", "executionOrder", "sourceNode", "destinationNode", "project",
				"assignedTo", "createdBy", "createdDate"));
		return grid;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		final CBabPolicyTriggerService triggerService = CSpringContext.getBean(CBabPolicyTriggerService.class);
		final CBabPolicyActionService actionService = CSpringContext.getBean(CBabPolicyActionService.class);
		final CBabPolicyFilterService filterService = CSpringContext.getBean(CBabPolicyFilterService.class);
		final List<CBabPolicyTrigger> availableTriggers = triggerService.listByProject(project);
		final List<CBabPolicyAction> availableActions = actionService.listByProject(project);
		final List<CBabPolicyFilter> availableFilters = filterService.listByProject(project);
		final String[][] nameAndDescriptions = {
				{
						"Forward CAN to ROS", "Forward CAN bus messages to ROS topic"
				}, {
						"Log HTTP Requests", "Log all HTTP server requests to syslog"
				}, {
						"Modbus to File", "Write Modbus data to file output"
				}
		};
		initializeProjectEntity(nameAndDescriptions,
				(CEntityOfProjectService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)), project, minimal,
				(item, index) -> {
					// Type-safe cast
					if (item instanceof CBabPolicyRule) {
						final CBabPolicyRule rule = (CBabPolicyRule) item;
						// Set rule-specific defaults for sample data
						rule.setRulePriority(50 + index * 10);
						rule.setExecutionOrder(index);
						rule.setLogEnabled(true);
						rule.setIsActive(true);
						// Attach real policy component entities to each sample rule.
						if (!availableTriggers.isEmpty()) {
							rule.setTrigger(availableTriggers.get(index % availableTriggers.size()));
						}
						if (!availableActions.isEmpty()) {
							rule.setActions(new HashSet<>(List.of(availableActions.get(index % availableActions.size()))));
						}
						if (!availableFilters.isEmpty()) {
							rule.setFilter(availableFilters.get(index % availableFilters.size()));
						}
					}
				});
	}

	private CBabPolicyRuleInitializerService() {
		// Utility class - no instantiation
	}
}
