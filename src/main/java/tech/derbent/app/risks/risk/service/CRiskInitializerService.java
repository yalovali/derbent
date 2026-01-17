package tech.derbent.app.risks.risk.service;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.app.risks.risk.domain.CRisk;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.service.CUserService;
import tech.derbent.app.attachments.service.CAttachmentInitializerService;
import tech.derbent.app.comments.service.CCommentInitializerService;
import tech.derbent.app.risks.risk.domain.ERiskResponseStrategy;

public class CRiskInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CRisk.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CRiskInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".10";
	private static final String menuTitle = MenuTitle_PROJECT + ".Risks";
	private static final String pageDescription = "Risk assessment and mitigation management";
	private static final String pageTitle = "Risk Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, true);
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "riskSeverity"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "status"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "assignedTo"));
			
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
			CAttachmentInitializerService.addAttachmentsSection(detailSection, clazz);
			
			// Comments section - standard section for discussion entities
			CCommentInitializerService.addCommentsSection(detailSection, clazz);
			
			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating risk view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "riskSeverity", "status", "project", "assignedTo", "createdBy", "createdDate"));
		return grid;
	}

	public static void initialize(final CProject project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	public static void initializeSample(final CProject project, final boolean minimal) throws Exception {
		final String[][] nameAndDescriptions = {
				{
						"Data Breach Risk", "Risk of unauthorized access to sensitive customer data"
				}, {
						"Technical Debt Accumulation", "Risk of increasing code complexity and maintenance costs"
				}, {
						"Vendor Dependency Risk", "Risk of critical vendor going out of business or changing terms"
				}, {
						"Regulatory Compliance Risk", "Risk of non-compliance with GDPR, SOC2, or industry regulations"
				}, {
						"Key Personnel Loss", "Risk of losing critical team members with unique knowledge"
				}
		};
		initializeProjectEntity(nameAndDescriptions,
				(CEntityOfProjectService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)), project, minimal,
				(item, index) -> {
					final CRisk risk = (CRisk) item;
					final CUser user = CSpringContext.getBean(CUserService.class).getRandom(project.getCompany());
					risk.setAssignedTo(user);
					
					// ISO 31000:2018 - Add quantitative risk assessment samples
					switch (index) {
						case 0: // Data Breach - Critical Risk
							risk.setProbability(8);
							risk.setImpactScore(9);
							risk.setRiskResponseStrategy(ERiskResponseStrategy.MITIGATE);
							risk.setMitigation("Implement multi-factor authentication, encryption at rest and in transit, regular security audits");
							risk.setResidualRisk("Low probability of breach remains even with controls; insider threat risk persists");
							break;
						case 1: // Technical Debt - High Risk
							risk.setProbability(7);
							risk.setImpactScore(6);
							risk.setRiskResponseStrategy(ERiskResponseStrategy.MITIGATE);
							risk.setMitigation("Allocate 20% of sprint capacity to refactoring, implement code review standards");
							risk.setResidualRisk("Some legacy code will remain; requires ongoing attention");
							break;
						case 2: // Vendor Dependency - Medium Risk
							risk.setProbability(4);
							risk.setImpactScore(7);
							risk.setRiskResponseStrategy(ERiskResponseStrategy.TRANSFER);
							risk.setMitigation("Diversify vendor portfolio, maintain backup vendors, negotiate exit clauses in contracts");
							risk.setResidualRisk("Transition costs and time remain if vendor fails");
							break;
						case 3: // Regulatory Compliance - High Risk
							risk.setProbability(6);
							risk.setImpactScore(9);
							risk.setRiskResponseStrategy(ERiskResponseStrategy.AVOID);
							risk.setMitigation("Hire compliance officer, conduct quarterly audits, implement compliance management system");
							risk.setResidualRisk("Regulatory changes may introduce new requirements");
							break;
						case 4: // Key Personnel Loss - Medium Risk
							risk.setProbability(5);
							risk.setImpactScore(7);
							risk.setRiskResponseStrategy(ERiskResponseStrategy.ACCEPT);
							risk.setMitigation("Cross-training programs, documentation standards, competitive compensation");
							risk.setResidualRisk("Knowledge gaps may exist despite documentation efforts");
							break;
					}
				});
	}
}
