package tech.derbent.plm.risks.risk.service;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.parentrelation.service.CParentRelationInitializerService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CEntityOfProjectInitializerService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CProjectItemInitializerService;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.users.service.CUserService;
import tech.derbent.plm.agile.domain.CUserStory;
import tech.derbent.plm.agile.service.CUserStoryService;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.risks.risk.domain.CRisk;
import tech.derbent.plm.risks.risk.domain.ERiskCriticality;
import tech.derbent.plm.risks.risk.domain.ERiskLikelihood;
import tech.derbent.plm.risks.risk.domain.ERiskResponseStrategy;
import tech.derbent.plm.risks.risk.domain.ERiskSeverity;
import tech.derbent.plm.risks.risktype.domain.CRiskType;
import tech.derbent.plm.risks.risktype.service.CRiskTypeService;

public class CRiskInitializerService extends CProjectItemInitializerService {

	private static final Class<?> clazz = CRisk.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CRiskInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".10";
	private static final String menuTitle = MenuTitle_PROJECT + ".Risks";
	private static final String pageDescription = "Risk assessment and mitigation management";
	private static final String pageTitle = "Risk Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			final CDetailSection detailSection = CEntityOfProjectInitializerService.createBasicView(project, clazz);
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "riskSeverity"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "status"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "assignedTo"));
			CParentRelationInitializerService.addDefaultSection(detailSection, clazz, project);
			// ISO 31000:2018 Risk Assessment Section
			detailSection.addScreenLine(CDetailLinesService.createSection("Risk Assessment (ISO 31000)"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "riskLikelihood"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "probability"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "impact"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "impactScore"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "riskCriticality"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "cause"));
			// ISO 31000:2018 Risk Treatment Section
			detailSection.addScreenLine(CDetailLinesService.createSection("Risk Treatment (ISO 31000)"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "riskResponseStrategy"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "mitigation"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "plan"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "residualRisk"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "result"));
			// Attachments section - standard section for ALL entities
			CAttachmentInitializerService.addDefaultSection(detailSection, clazz);
			// Comments section - standard section for discussion entities
			CCommentInitializerService.addDefaultSection(detailSection, clazz);
			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating risk view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "riskSeverity", "status", "project", "assignedTo",
				"createdBy", "createdDate"));
		grid.setEditableColumnFields(List.of("name", "assignedTo", "status"));
		return grid;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService)
			throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid,
				menuTitle, pageTitle, pageDescription, showInQuickToolbar, menuOrder, null);
	}

	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		record RiskSeed(String name, String description, int parentUserStoryIndex, ERiskSeverity severity,
				ERiskLikelihood likelihood, ERiskCriticality criticality, ERiskResponseStrategy responseStrategy,
				int probability, int impactScore, String cause, String impact, String mitigation, String plan,
				String residualRisk, String result) {}
		final List<RiskSeed> seeds = List.of(new RiskSeed("MFA Enrollment Drop-off",
				"Users may abandon MFA rollout if enrollment is confusing or recovery instructions are unclear.", 0,
				ERiskSeverity.HIGH, ERiskLikelihood.LIKELY, ERiskCriticality.HIGH, ERiskResponseStrategy.MITIGATE, 7, 8,
				"Enrollment flow includes too many steps and limited contextual help.",
				"Pilot adoption could miss security goals and launch dates if admins do not complete setup.",
				"Run guided onboarding, improve inline help, and monitor funnel conversion during the pilot.",
				"Add UX review checkpoints, pilot metrics dashboard, and support playbook before broad rollout.",
				"Some adoption variance will remain across customer segments with strict device policies.",
				"Tracked as top security-readiness risk for the identity stream."),
				new RiskSeed("Session Audit Storage Growth",
						"Security audit events may grow faster than planned once suspicious-session review is enabled.",
						1, ERiskSeverity.MEDIUM, ERiskLikelihood.POSSIBLE, ERiskCriticality.MODERATE,
						ERiskResponseStrategy.MITIGATE, 5, 6,
						"Expanded audit capture and long retention windows increase storage pressure.",
						"Reporting queries and export jobs could slow down during peak usage periods.",
						"Partition audit tables, introduce retention policies, and benchmark export queries.",
						"Finalize storage forecast, archive strategy, and operational alert thresholds.",
						"Unexpected customer retention obligations may still increase long-term storage needs.",
						"Requires observability checks before compliance sign-off."),
				new RiskSeed("Billing Contact Data Sync Drift",
						"Customer profile updates might not propagate correctly to billing systems and notification services.",
						2, ERiskSeverity.HIGH, ERiskLikelihood.POSSIBLE, ERiskCriticality.HIGH,
						ERiskResponseStrategy.MITIGATE, 6, 8,
						"Multiple downstream services depend on customer contact records with inconsistent validation rules.",
						"Invoices, reminders, or escalation notices could be delivered to outdated recipients.",
						"Introduce contract tests, event replay validation, and cross-system reconciliation jobs.",
						"Complete integration test matrix and dry-run sync validation for top customer accounts.",
						"Manual override procedures are still needed for outlier legacy customers.",
						"Flagged for finance and support stakeholders before self-service rollout."),
				new RiskSeed("Saved Filter Scope Creep",
						"Workspace search enhancements may expand beyond the current sprint into dashboard personalization and sharing.",
						3, ERiskSeverity.MEDIUM, ERiskLikelihood.LIKELY, ERiskCriticality.MODERATE,
						ERiskResponseStrategy.ACCEPT, 6, 5,
						"Customer feedback quickly expands the definition of reusable views and filter sharing.",
						"Sprint focus could diffuse and delay committed workspace deliverables.",
						"Gate new ideas behind backlog triage and keep the current release focused on personal saved views only.",
						"Review incoming requests in backlog refinement and track out-of-scope asks separately.",
						"Some pressure from pilot customers will remain until collaboration features are planned.",
						"Managed as a product-scope risk rather than a technical blocker."),
				new RiskSeed("Dispute SLA Breach During Launch",
						"Operational load could exceed invoice dispute handling capacity when new dispute intake goes live.",
						4, ERiskSeverity.CRITICAL, ERiskLikelihood.POSSIBLE, ERiskCriticality.CRITICAL,
						ERiskResponseStrategy.ESCALATE, 6, 9,
						"Launch campaign may increase dispute volume before triage automation is fully stable.",
						"Missed SLAs would affect customer trust and finance operations during the release window.",
						"Escalate staffing plan, define overflow support rota, and monitor queue depth daily.",
						"Secure executive approval for temporary support coverage and launch-day staffing.",
						"Unexpected marketing volume can still create short-term response delays.",
						"Executive risk for release go/no-go review."),
				new RiskSeed("Release Gate Automation Gaps",
						"Launch approval may proceed with incomplete checklist coverage if automation misses a gate.",
						5, ERiskSeverity.HIGH, ERiskLikelihood.POSSIBLE, ERiskCriticality.HIGH,
						ERiskResponseStrategy.AVOID, 5, 8,
						"Manual gates are still being converted into automated launch checks.",
						"Incomplete automation could allow a release with unresolved blockers or missing rollback readiness.",
						"Require manual sign-off until every critical gate has an automated signal and owner.",
						"Audit launch checklist coverage, assign owners, and block release until critical gates are automated.",
						"Some lower-priority checks may remain manual for the first production cut.",
						"Tracked directly in the release readiness steering group."));
		final CRiskService riskService =
				(CRiskService) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz));
		if (!riskService.listByProject(project).isEmpty()) {
			LOGGER.info("Risks already exist for project '{}', skipping initialization", project.getName());
			return;
		}
		final CRiskTypeService riskTypeService = CSpringContext.getBean(CRiskTypeService.class);
		final CUserService userService = CSpringContext.getBean(CUserService.class);
		final CUserStoryService userStoryService = CSpringContext.getBean(CUserStoryService.class);
		final List<CRiskType> availableTypes = riskTypeService.listByCompany(project.getCompany());
		final List<CUser> availableUsers = userService.listByCompany(project.getCompany());
		final List<CUserStory> availableUserStories = new ArrayList<>(userStoryService.listByProject(project));
		int createdCount = 0;
		for (final RiskSeed seed : seeds) {
			final CRisk risk = new CRisk(seed.name(), project);
			risk.setDescription(seed.description());
			if (!availableTypes.isEmpty()) {
				risk.setEntityType(availableTypes.get(createdCount % availableTypes.size()));
			}
			if (!availableUsers.isEmpty()) {
				risk.setAssignedTo(availableUsers.get(createdCount % availableUsers.size()));
			}
			if (!availableUserStories.isEmpty()) {
				risk.setParentItem(availableUserStories.get(seed.parentUserStoryIndex() % availableUserStories.size()));
			}
			risk.setRiskSeverity(seed.severity());
			risk.setRiskLikelihood(seed.likelihood());
			risk.setRiskCriticality(seed.criticality());
			risk.setRiskResponseStrategy(seed.responseStrategy());
			risk.setProbability(seed.probability());
			risk.setImpactScore(seed.impactScore());
			risk.setCause(seed.cause());
			risk.setImpact(seed.impact());
			risk.setMitigation(seed.mitigation());
			risk.setPlan(seed.plan());
			risk.setResidualRisk(seed.residualRisk());
			risk.setResult(seed.result());
			riskService.save(risk);
			createdCount++;
			if (minimal) {
				break;
			}
		}
	}
}
