package tech.derbent.api.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tech.derbent.api.domains.CEntity;
import tech.derbent.api.domains.CProjectItemStatus;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.domain.CMasterSection;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CGridInitializerService;
import tech.derbent.api.screens.service.CMasterInitializerService;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.activities.domain.CActivityPriority;
import tech.derbent.app.activities.domain.CActivityType;
import tech.derbent.app.activities.service.CActivityInitializerService;
import tech.derbent.app.activities.service.CActivityPriorityInitializerService;
import tech.derbent.app.activities.service.CActivityPriorityService;
import tech.derbent.app.activities.service.CActivityService;
import tech.derbent.app.activities.service.CActivityTypeInitializerService;
import tech.derbent.app.activities.service.CActivityTypeService;
import tech.derbent.app.activities.service.CProjectItemStatusInitializerService;
import tech.derbent.app.activities.service.CProjectItemStatusService;
import tech.derbent.app.comments.domain.CComment;
import tech.derbent.app.comments.domain.CCommentPriority;
import tech.derbent.app.comments.service.CCommentInitializerService;
import tech.derbent.app.comments.service.CCommentPriorityService;
import tech.derbent.app.comments.service.CCommentService;
import tech.derbent.app.comments.view.CCommentPriorityInitializerService;
import tech.derbent.app.companies.domain.CCompany;
import tech.derbent.app.companies.service.CCompanyInitializerService;
import tech.derbent.app.companies.service.CCompanyService;
import tech.derbent.app.decisions.domain.CDecision;
import tech.derbent.app.decisions.domain.CDecisionType;
import tech.derbent.app.decisions.service.CDecisionInitializerService;
import tech.derbent.app.decisions.service.CDecisionService;
import tech.derbent.app.decisions.service.CDecisionTypeInitializerService;
import tech.derbent.app.decisions.service.CDecisionTypeService;
import tech.derbent.app.gannt.domain.CGanntViewEntity;
import tech.derbent.app.gannt.service.CGanntInitializerService;
import tech.derbent.app.gannt.service.CGanntViewEntityService;
import tech.derbent.app.meetings.domain.CMeeting;
import tech.derbent.app.meetings.domain.CMeetingType;
import tech.derbent.app.meetings.service.CMeetingInitializerService;
import tech.derbent.app.meetings.service.CMeetingService;
import tech.derbent.app.meetings.service.CMeetingTypeInitializerService;
import tech.derbent.app.meetings.service.CMeetingTypeService;
import tech.derbent.app.orders.domain.CApprovalStatus;
import tech.derbent.app.orders.domain.CCurrency;
import tech.derbent.app.orders.domain.COrder;
import tech.derbent.app.orders.domain.COrderApproval;
import tech.derbent.app.orders.domain.COrderType;
import tech.derbent.app.orders.service.CApprovalStatusInitializerService;
import tech.derbent.app.orders.service.CApprovalStatusService;
import tech.derbent.app.orders.service.CCurrencyInitializerService;
import tech.derbent.app.orders.service.CCurrencyService;
import tech.derbent.app.orders.service.COrderApprovalService;
import tech.derbent.app.orders.service.COrderInitializerService;
import tech.derbent.app.orders.service.COrderService;
import tech.derbent.app.orders.service.COrderTypeInitializerService;
import tech.derbent.app.orders.service.COrderTypeService;
import tech.derbent.app.page.domain.CPageEntity;
import tech.derbent.app.page.service.CPageEntityInitializerService;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.projects.service.CProjectInitializerService;
import tech.derbent.app.projects.service.CProjectService;
import tech.derbent.app.risks.domain.CRisk;
import tech.derbent.app.risks.domain.CRiskType;
import tech.derbent.app.risks.service.CRiskInitializerService;
import tech.derbent.app.risks.service.CRiskService;
import tech.derbent.app.risks.service.CRiskTypeService;
import tech.derbent.app.roles.domain.CUserCompanyRole;
import tech.derbent.app.roles.domain.CUserProjectRole;
import tech.derbent.app.roles.service.CUserCompanyRoleInitializerService;
import tech.derbent.app.roles.service.CUserCompanyRoleService;
import tech.derbent.app.roles.service.CUserProjectRoleInitializerService;
import tech.derbent.app.roles.service.CUserProjectRoleService;
import tech.derbent.app.workflow.domain.CWorkflowEntity;
import tech.derbent.app.workflow.domain.CWorkflowStatusRelation;
import tech.derbent.app.workflow.service.CWorkflowEntityService;
import tech.derbent.app.workflow.service.CWorkflowStatusRelationService;
import tech.derbent.base.setup.domain.CSystemSettings;
import tech.derbent.base.setup.service.CSystemSettingsInitializerService;
import tech.derbent.base.setup.service.CSystemSettingsService;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.domain.CUserCompanySetting;
import tech.derbent.base.users.domain.CUserProjectSettings;
import tech.derbent.base.users.service.CUserCompanySettingsService;
import tech.derbent.base.users.service.CUserInitializerService;
import tech.derbent.base.users.service.CUserProjectSettingsService;
import tech.derbent.base.users.service.CUserService;

/**
 * Initializes the entity registry at application startup.
 * This class registers all entities, services, and their metadata.
 * Order is set to run early in the startup process.
 */
@Component
@Order(1)
public class CEntityRegistryInitializer implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(CEntityRegistryInitializer.class);

	@Override
	public void run(final String... args) throws Exception {
		LOGGER.info("Initializing entity registry...");
		
		try {
			registerAllEntities();
			CEntityRegistry.markInitialized();
			LOGGER.info("Entity registry initialized successfully with {} entities", 
					CEntityRegistry.getRegisteredCount());
		} catch (final Exception e) {
			LOGGER.error("Failed to initialize entity registry", e);
			throw e;
		}
	}

	private void registerAllEntities() {
		// Register core entities
		register("CEntity", CEntity.class, null, null, null, null);
		
		// Register domain entities with full metadata
		registerActivity();
		registerMeeting();
		registerOrder();
		registerRisk();
		registerCompany();
		registerProject();
		registerDecision();
		registerComment();
		registerUser();
		
		// Register type and status entities
		registerActivityType();
		registerRiskType();
		registerProjectItemStatus();
		registerActivityPriority();
		registerMeetingType();
		registerCommentPriority();
		registerCurrency();
		registerDecisionType();
		registerOrderType();
		registerApprovalStatus();
		
		// Register system entities
		registerPageEntity();
		registerDetailSection();
		registerGridEntity();
		registerMasterSection();
		registerSystemSettings();
		registerUserProjectRole();
		registerUserCompanyRole();
		registerWorkflowEntity();
		registerWorkflowStatusRelation();
		registerOrderApproval();
		registerUserProjectSettings();
		registerUserCompanySetting();
		registerGanntViewEntity();
	}

	private void registerActivity() {
		register("CActivity", CActivity.class, CActivityService.class, 
				CActivityInitializerService.class, 
				"vaadin:tasks", "#DC143C");
	}

	private void registerMeeting() {
		register("CMeeting", CMeeting.class, CMeetingService.class, 
				CMeetingInitializerService.class, 
				"vaadin:calendar", "#fd7e14");
	}

	private void registerOrder() {
		register("COrder", COrder.class, COrderService.class, 
				COrderInitializerService.class, 
				"vaadin:money", "#28a745");
	}

	private void registerRisk() {
		register("CRisk", CRisk.class, CRiskService.class, 
				CRiskInitializerService.class, 
				"vaadin:warning", "#dc3545");
	}

	private void registerCompany() {
		register("CCompany", CCompany.class, CCompanyService.class, 
				CCompanyInitializerService.class, 
				"vaadin:building", "#6c757d");
	}

	private void registerProject() {
		register("CProject", CProject.class, CProjectService.class, 
				CProjectInitializerService.class, 
				"vaadin:folder-open", "#007bff");
	}

	private void registerDecision() {
		register("CDecision", CDecision.class, CDecisionService.class, 
				CDecisionInitializerService.class, 
				"vaadin:check-circle", "#17a2b8");
	}

	private void registerComment() {
		register("CComment", CComment.class, CCommentService.class, 
				CCommentInitializerService.class, 
				"vaadin:comment", "#ffc107");
	}

	private void registerUser() {
		register("CUser", CUser.class, CUserService.class, 
				CUserInitializerService.class, 
				"vaadin:user", "#6610f2");
	}

	private void registerActivityType() {
		register("CActivityType", CActivityType.class, CActivityTypeService.class, 
				CActivityTypeInitializerService.class, 
				"vaadin:tag", "#e83e8c");
	}

	private void registerRiskType() {
		register("CRiskType", CRiskType.class, CRiskTypeService.class, 
				null, "vaadin:warning", "#dc3545");
	}

	private void registerProjectItemStatus() {
		register("CProjectItemStatus", CProjectItemStatus.class, CProjectItemStatusService.class, 
				CProjectItemStatusInitializerService.class, 
				"vaadin:check", "#20c997");
	}

	private void registerActivityPriority() {
		register("CActivityPriority", CActivityPriority.class, CActivityPriorityService.class, 
				CActivityPriorityInitializerService.class, 
				"vaadin:flag", "#fd7e14");
	}

	private void registerMeetingType() {
		register("CMeetingType", CMeetingType.class, CMeetingTypeService.class, 
				CMeetingTypeInitializerService.class, 
				"vaadin:calendar-clock", "#fd7e14");
	}

	private void registerCommentPriority() {
		register("CCommentPriority", CCommentPriority.class, CCommentPriorityService.class, 
				CCommentPriorityInitializerService.class, 
				"vaadin:flag", "#ffc107");
	}

	private void registerCurrency() {
		register("CCurrency", CCurrency.class, CCurrencyService.class, 
				CCurrencyInitializerService.class, 
				"vaadin:dollar", "#28a745");
	}

	private void registerDecisionType() {
		register("CDecisionType", CDecisionType.class, CDecisionTypeService.class, 
				CDecisionTypeInitializerService.class, 
				"vaadin:check-circle-o", "#17a2b8");
	}

	private void registerOrderType() {
		register("COrderType", COrderType.class, COrderTypeService.class, 
				COrderTypeInitializerService.class, 
				"vaadin:tag", "#28a745");
	}

	private void registerApprovalStatus() {
		register("CApprovalStatus", CApprovalStatus.class, CApprovalStatusService.class, 
				CApprovalStatusInitializerService.class, 
				"vaadin:check-square", "#20c997");
	}

	private void registerPageEntity() {
		register("CPageEntity", CPageEntity.class, CPageEntityService.class, 
				CPageEntityInitializerService.class, 
				"vaadin:file-text", "#6c757d");
	}

	private void registerDetailSection() {
		register("CDetailSection", CDetailSection.class, CDetailSectionService.class, 
				CDetailSectionService.class, 
				"vaadin:grid-small", "#95a5a6");
	}

	private void registerGridEntity() {
		register("CGridEntity", CGridEntity.class, CGridEntityService.class, 
				CGridInitializerService.class, 
				"vaadin:grid-big", "#95a5a6");
	}

	private void registerMasterSection() {
		register("CMasterSection", CMasterSection.class, null, 
				CMasterInitializerService.class, 
				"vaadin:grid", "#95a5a6");
	}

	private void registerSystemSettings() {
		register("CSystemSettings", CSystemSettings.class, CSystemSettingsService.class, 
				CSystemSettingsInitializerService.class, 
				"vaadin:cog", "#6c757d");
	}

	private void registerUserProjectRole() {
		register("CUserProjectRole", CUserProjectRole.class, CUserProjectRoleService.class, 
				CUserProjectRoleInitializerService.class, 
				"vaadin:user-check", "#6610f2");
	}

	private void registerUserCompanyRole() {
		register("CUserCompanyRole", CUserCompanyRole.class, CUserCompanyRoleService.class, 
				CUserCompanyRoleInitializerService.class, 
				"vaadin:user-star", "#6610f2");
	}

	private void registerWorkflowEntity() {
		register("CWorkflowEntity", CWorkflowEntity.class, CWorkflowEntityService.class, 
				CWorkflowEntityService.class, 
				"vaadin:flow-tree", "#17a2b8");
	}

	private void registerWorkflowStatusRelation() {
		register("CWorkflowStatusRelation", CWorkflowStatusRelation.class, CWorkflowStatusRelationService.class, 
				CWorkflowStatusRelationService.class, 
				"vaadin:connect", "#17a2b8");
	}

	private void registerOrderApproval() {
		register("COrderApproval", COrderApproval.class, COrderApprovalService.class, 
				COrderApprovalService.class, 
				"vaadin:check-circle", "#28a745");
	}

	private void registerUserProjectSettings() {
		register("CUserProjectSettings", CUserProjectSettings.class, CUserProjectSettingsService.class, 
				CUserProjectSettingsService.class, 
				"vaadin:cogs", "#6c757d");
	}

	private void registerUserCompanySetting() {
		register("CUserCompanySetting", CUserCompanySetting.class, CUserCompanySettingsService.class, 
				CUserCompanySettingsService.class, 
				"vaadin:cog-o", "#6c757d");
	}

	private void registerGanntViewEntity() {
		register("CGanntViewEntity", CGanntViewEntity.class, CGanntViewEntityService.class, 
				CGanntInitializerService.class, 
				"vaadin:timeline", "#fd7e14");
	}

	private void register(final String simpleName, final Class<?> entityClass, 
			final Class<?> serviceClass, final Class<?> initializerClass,
			final String icon, final String color) {
		CEntityRegistry.register(new IEntityRegistrable() {
			@Override
			public Class<?> getEntityClass() {
				return entityClass;
			}

			@Override
			public Class<?> getServiceClass() {
				return serviceClass;
			}

			@Override
			public Class<?> getInitializerServiceClass() {
				return initializerClass;
			}

			@Override
			public String getDefaultIcon() {
				return icon;
			}

			@Override
			public String getDefaultColor() {
				return color;
			}

			@Override
			public String getSimpleName() {
				return simpleName;
			}
		});
	}
}
