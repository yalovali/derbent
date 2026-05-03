package tech.derbent.api.config;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.companies.service.CCompanyInitializerService;
import tech.derbent.api.companies.service.CCompanyService;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusInitializerService;
import tech.derbent.api.imports.service.CDataImportInitializerService;
import tech.derbent.api.page.service.CPageEntityInitializerService;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.service.CProjectService;
import tech.derbent.api.projects.service.CProjectTypeInitializerService;
import tech.derbent.api.roles.service.CUserCompanyRoleInitializerService;
import tech.derbent.api.roles.service.CUserProjectRoleInitializerService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityInitializerService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CMasterInitializerService;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.users.service.CUserInitializerService;
import tech.derbent.api.users.service.CUserService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.service.CWorkflowEntityInitializerService;
import tech.derbent.plm.activities.service.CActivityInitializerService;
import tech.derbent.plm.activities.service.CActivityPriorityInitializerService;
import tech.derbent.plm.activities.service.CActivityTypeInitializerService;
import tech.derbent.plm.agile.service.CEpicInitializerService;
import tech.derbent.plm.agile.service.CEpicTypeInitializerService;
import tech.derbent.plm.agile.service.CFeatureInitializerService;
import tech.derbent.plm.agile.service.CFeatureTypeInitializerService;
import tech.derbent.plm.agile.service.CUserStoryInitializerService;
import tech.derbent.plm.agile.service.CUserStoryTypeInitializerService;
import tech.derbent.plm.assets.asset.service.CAssetInitializerService;
import tech.derbent.plm.assets.assettype.service.CAssetTypeInitializerService;
import tech.derbent.plm.budgets.budget.service.CBudgetInitializerService;
import tech.derbent.plm.budgets.budgettype.service.CBudgetTypeInitializerService;
import tech.derbent.plm.components.component.service.CProjectComponentInitializerService;
import tech.derbent.plm.components.componenttype.service.CProjectComponentTypeInitializerService;
import tech.derbent.plm.components.componentversion.service.CProjectComponentVersionInitializerService;
import tech.derbent.plm.components.componentversiontype.service.CProjectComponentVersionTypeInitializerService;
import tech.derbent.plm.decisions.service.CDecisionInitializerService;
import tech.derbent.plm.decisions.service.CDecisionTypeInitializerService;
import tech.derbent.plm.deliverables.deliverable.service.CDeliverableInitializerService;
import tech.derbent.plm.deliverables.deliverabletype.service.CDeliverableTypeInitializerService;
import tech.derbent.plm.gnnt.gnntviewentity.service.CGnntViewEntityInitializerService;
import tech.derbent.plm.issues.issue.service.CIssueInitializerService;
import tech.derbent.plm.issues.issuetype.service.CIssueTypeInitializerService;
import tech.derbent.plm.kanban.kanbanline.domain.CKanbanLine;
import tech.derbent.plm.kanban.kanbanline.service.CKanbanLineInitializerService;
import tech.derbent.plm.kanban.kanbanline.service.CKanbanLineService;
import tech.derbent.plm.meetings.service.CMeetingInitializerService;
import tech.derbent.plm.meetings.service.CMeetingTypeInitializerService;
import tech.derbent.plm.milestones.milestone.service.CMilestoneInitializerService;
import tech.derbent.plm.milestones.milestonetype.service.CMilestoneTypeInitializerService;
import tech.derbent.plm.orders.approval.service.CApprovalStatusInitializerService;
import tech.derbent.plm.orders.approval.service.COrderApprovalInitializerService;
import tech.derbent.plm.orders.currency.service.CCurrencyInitializerService;
import tech.derbent.plm.orders.order.service.COrderInitializerService;
import tech.derbent.plm.orders.type.service.COrderTypeInitializerService;
import tech.derbent.plm.products.product.service.CProductInitializerService;
import tech.derbent.plm.products.producttype.service.CProductTypeInitializerService;
import tech.derbent.plm.products.productversion.service.CProductVersionInitializerService;
import tech.derbent.plm.products.productversiontype.service.CProductVersionTypeInitializerService;
import tech.derbent.plm.project.domain.CProject_Derbent;
import tech.derbent.plm.project.service.CProject_DerbentInitializerService;
import tech.derbent.plm.projectexpenses.projectexpense.service.CProjectExpenseInitializerService;
import tech.derbent.plm.projectexpenses.projectexpensetype.service.CProjectExpenseTypeInitializerService;
import tech.derbent.plm.projectincomes.projectincome.service.CProjectIncomeInitializerService;
import tech.derbent.plm.projectincomes.projectincometype.service.CProjectIncomeTypeInitializerService;
import tech.derbent.plm.providers.provider.service.CProviderInitializerService;
import tech.derbent.plm.providers.providertype.service.CProviderTypeInitializerService;
import tech.derbent.plm.requirements.requirement.service.CRequirementInitializerService;
import tech.derbent.plm.requirements.requirementtype.service.CRequirementTypeInitializerService;
import tech.derbent.plm.risklevel.risklevel.service.CRiskLevelInitializerService;
import tech.derbent.plm.risks.risk.service.CRiskInitializerService;
import tech.derbent.plm.risks.risktype.service.CRiskTypeInitializerService;
import tech.derbent.plm.setup.service.CSystemSettings_DerbentInitializerService;
import tech.derbent.plm.sprints.planning.service.CSprintPlanningViewEntityInitializerService;
import tech.derbent.plm.sprints.service.CSprintInitializerService;
import tech.derbent.plm.sprints.service.CSprintTypeInitializerService;
import tech.derbent.plm.storage.storage.service.CStorageInitializerService;
import tech.derbent.plm.storage.storageitem.service.CStorageItemInitializerService;
import tech.derbent.plm.storage.storageitem.service.CStorageItemTypeInitializerService;
import tech.derbent.plm.storage.storagetype.service.CStorageTypeInitializerService;
import tech.derbent.plm.storage.transaction.service.CStorageTransactionInitializerService;
import tech.derbent.plm.teams.team.service.CTeamInitializerService;
import tech.derbent.plm.tickets.servicedepartment.service.CTicketServiceDepartmentInitializerService;
import tech.derbent.plm.tickets.ticket.service.CTicketInitializerService;
import tech.derbent.plm.tickets.tickettype.service.CTicketTypeInitializerService;
import tech.derbent.plm.validation.validationcase.service.CValidationCaseInitializerService;
import tech.derbent.plm.validation.validationcasetype.service.CValidationCaseTypeInitializerService;
import tech.derbent.plm.validation.validationsession.service.CValidationSessionInitializerService;
import tech.derbent.plm.validation.validationsuite.service.CValidationSuiteInitializerService;

/**
 * Spring service responsible for creating all CGridEntity / CDetailSection / CPageEntity records
 * for every project after a DB reset.
 *
 * WHY: previously this logic lived in CDataInitializer.loadEntityScreens() which was called
 * externally after bootstrapAfterReset(). Moving it here makes the bootstrap self-contained
 * and removes the need for callers to call a separate "load screens" step.
 */
@Service
public class CScreensInitializerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CScreensInitializerService.class);

	private final CCompanyService companyService;
	private final CDetailSectionService detailSectionService;
	private final CGridEntityService gridEntityService;
	private final CKanbanLineService kanbanLineService;
	private final CPageEntityService pageEntityService;
	@SuppressWarnings ("rawtypes")
	private final CProjectService projectService;
	private final ISessionService sessionService;
	private final CUserService userService;

	@SuppressWarnings ("rawtypes")
	public CScreensInitializerService(final CCompanyService companyService,
			final CDetailSectionService detailSectionService, final CGridEntityService gridEntityService,
			final CKanbanLineService kanbanLineService, final CPageEntityService pageEntityService,
			final CProjectService projectService, final ISessionService sessionService, final CUserService userService) {
		this.companyService = companyService;
		this.detailSectionService = detailSectionService;
		this.gridEntityService = gridEntityService;
		this.kanbanLineService = kanbanLineService;
		this.pageEntityService = pageEntityService;
		this.projectService = projectService;
		this.sessionService = sessionService;
		this.userService = userService;
	}

	@SuppressWarnings ("unchecked")
	private void assignDefaultKanbanLine(final CProject_Derbent project) {
		final CCompany company = project.getCompany();
		final java.util.Optional<CKanbanLine> line = kanbanLineService.findDefaultForCompany(company);
		if (line.isEmpty()) {
			LOGGER.warn("No KanbanLine found for company {}; skipping kanban assignment for project {}",
					company.getName(), project.getName());
			return;
		}
		project.setKanbanLine(line.get());
		projectService.save(project);
	}

	/** Creates CGridEntity / CDetailSection / CPageEntity records for every project of every company.
	 * Must be called after the Excel import so that status and user data are already present. */
	@SuppressWarnings ("unchecked")
	public void initializeScreensForAllProjects(final boolean minimal) throws Exception {
		LOGGER.info("Screen/grid/page initialization started");
		try {
			for (final CCompany company : companyService.list(Pageable.unpaged()).getContent()) {
				LOGGER.info("Initializing screens for company: id={}:{}", company.getId(), company.getName());
				final CUser user = userService.getRandomByCompany(company);
				Check.notNull(user, "No user found for company: " + company.getName());
				sessionService.setActiveCompany(company);
				sessionService.setActiveUser(user);
				ensureKanbanLines(company, minimal);
				final List<CProject_Derbent> projects = projectService.listByCompany(company);
				for (final CProject_Derbent project : projects) {
					LOGGER.info("Initializing screens for project: {}:{}", project.getId(), project.getName());
					sessionService.setActiveProject(project);
					assignDefaultKanbanLine(project);
					initializeProjectScreens(project);
				}
			}
			LOGGER.info("Screen/grid/page initialization completed");
		} catch (final Exception e) {
			LOGGER.error("Error during screen initialization: {}", e.getMessage());
			throw e;
		}
	}

	/** Seeds at least one KanbanLine for a company if none exist yet.
	 * Called after Excel import so project statuses are already present for column mapping. */
	private void ensureKanbanLines(final CCompany company, final boolean minimal) {
		if (!kanbanLineService.listByCompany(company).isEmpty()) {
			return;
		}
		try {
			CKanbanLineInitializerService.initializeSample(company, minimal);
			LOGGER.info("Seeded default KanbanLines for company: {}", company.getName());
		} catch (final Exception e) {
			LOGGER.warn("Could not seed KanbanLines for company {}: {}", company.getName(), e.getMessage());
		}
	}

	private void initializeProjectScreens(final CProject_Derbent p) throws Exception {
		CSystemSettings_DerbentInitializerService.initialize(p, gridEntityService, detailSectionService,
				pageEntityService);
		CActivityInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CEpicInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CUserStoryInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CFeatureInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CUserInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CCompanyInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CDecisionInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CMeetingInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		COrderInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CProject_DerbentInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CRiskInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CRiskLevelInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CAssetInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CMilestoneInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CTicketInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CTicketServiceDepartmentInitializerService.initialize(p, gridEntityService, detailSectionService,
				pageEntityService);
		CIssueInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CBudgetInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CProjectExpenseInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CProjectIncomeInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CDeliverableInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CRequirementInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CProviderInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CProductInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CProductVersionInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CProjectComponentInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CProjectComponentVersionInitializerService.initialize(p, gridEntityService, detailSectionService,
				pageEntityService);
		CStorageInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CStorageItemInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CStorageTransactionInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CTeamInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CUserProjectRoleInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CUserCompanyRoleInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		// Type / Status pages
		CProjectItemStatusInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CRiskTypeInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CAssetTypeInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CMilestoneTypeInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CTicketTypeInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CIssueTypeInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CBudgetTypeInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CProjectExpenseTypeInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CProjectIncomeTypeInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CDeliverableTypeInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CRequirementTypeInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CProviderTypeInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CProductTypeInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CProductVersionTypeInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CProjectComponentTypeInitializerService.initialize(p, gridEntityService, detailSectionService,
				pageEntityService);
		CProjectComponentVersionTypeInitializerService.initialize(p, gridEntityService, detailSectionService,
				pageEntityService);
		CStorageTypeInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CStorageItemTypeInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CProjectTypeInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CActivityTypeInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CEpicTypeInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CUserStoryTypeInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CFeatureTypeInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CActivityPriorityInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CApprovalStatusInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CCurrencyInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CDecisionTypeInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CMeetingTypeInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		COrderTypeInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CWorkflowEntityInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		COrderApprovalInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CGnntViewEntityInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CGridEntityInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CMasterInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CPageEntityInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CSprintTypeInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CSprintInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CSprintPlanningViewEntityInitializerService.initialize(p, gridEntityService, detailSectionService,
				pageEntityService);
		CValidationCaseTypeInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CValidationSuiteInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CValidationCaseInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CValidationSessionInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CKanbanLineInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
		CDataImportInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService);
	}
}
