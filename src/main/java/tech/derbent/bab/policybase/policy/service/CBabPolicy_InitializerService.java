package tech.derbent.bab.policybase.policy.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailLines;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.policybase.policy.domain.CBabPolicy;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.service.CLinkInitializerService;

/** CBabPolicy_InitializerService - Initializer service for BAB policy entities. Creates views, grids, and sample data for BAB Actions Dashboard
 * policies. Policies contain rules for virtual network node communication and are exported as JSON to Calimero. Layer: Service (MVC) Active when:
 * 'bab' profile is active */
public final class CBabPolicy_InitializerService extends CInitializerServiceBase {

	static final Class<?> clazz = CBabPolicy.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicy_InitializerService.class);
	private static final String menuOrder = "20";
	private static final String menuTitle = "Policies";
	private static final String pageDescription = "BAB Actions Dashboard policy management and rule configuration";
	private static final String pageTitle = "BAB Policies";
	private static final boolean showInQuickToolbar = true;

	/** Create detail view for BAB policy entity.
	 * @param project the project context
	 * @return configured detail section
	 * @throws Exception if view creation fails */
	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		Check.notNull(project, "project cannot be null");
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			// Basic fields (name, description)
			CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, true);
			// Policy Rules Section (component-based display)
			detailSection.addScreenLine(CDetailLinesService.createSection("Policy Rules"));
			final CDetailLines line = CDetailLinesService.createLineFromDefaults(clazz, "placeHolder_createComponentPolicyBab");
			line.setIsCaptionVisible(false);
			detailSection.addScreenLine(line);
			// Policy Configuration Section
			detailSection.addScreenLine(CDetailLinesService.createSection("Policy Configuration"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isActive"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "priorityLevel"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "policyVersion"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastAppliedDate"));
			// Calimero Export Section
			detailSection.addScreenLine(CDetailLinesService.createSection("Calimero Export"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "policyJson"));
			// Standard composition sections
			CAttachmentInitializerService.addDefaultSection(detailSection, clazz);
			CLinkInitializerService.addDefaultSection(detailSection, clazz);
			CCommentInitializerService.addDefaultSection(detailSection, clazz);
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating BAB policy view.", e);
			throw e;
		}
	}

	private static CDetailSection createBasicView2(final CProject<?> project) throws Exception {
		final CDetailSection scr = createBaseScreenEntity(project, clazz);
		scr.addScreenLine(CDetailLinesService.createSection("Policy Rules"));
		final CDetailLines line = CDetailLinesService.createLineFromDefaults(clazz, "placeHolder_createComponentPolicyBab");
		line.setIsCaptionVisible(false);
		scr.addScreenLine(line);
		return scr;
	}

	/** Create grid configuration for BAB policy entity.
	 * @param project the project context
	 * @return configured grid entity */
	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "isActive", "priorityLevel", "policyVersion", "lastAppliedDate", "project",
				"createdBy", "createdDate", "lastModifiedDate"));
		return grid;
	}

	/** Initialize BAB policy views and grids.
	 * @param project              the project context
	 * @param gridEntityService    grid entity service
	 * @param detailSectionService detail section service
	 * @param pageEntityService    page entity service
	 * @throws Exception if initialization fails */
	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, MenuTitle_DEVELOPMENT + menuTitle,
				pageTitle, pageDescription, false, menuOrder);
		// second view
		final CDetailSection detailSection2 = createBasicView2(project);
		detailSection2.setName("Policy Setup");
		final CGridEntity grid2 = createGridEntity(project);
		grid2.setName("Policy Setup");
		grid2.setAttributeNone(true); // dont show grid
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection2, grid2, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	/** Initialize sample BAB policy data.
	 * @param project the project context
	 * @param minimal if true, create minimal sample data
	 * @throws Exception if initialization fails */
	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		LOGGER.info("Initializing BAB policy sample data for project: {}", project.getName());
		final CBabPolicyService policyService = (CBabPolicyService) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz));
		// Check if policies already exist
		final List<CBabPolicy> existingPolicies = policyService.listByProject(project);
		if (!existingPolicies.isEmpty()) {
			LOGGER.info("BAB policies already exist ({}), skipping sample creation", existingPolicies.size());
			return;
		}
		// Create sample policy 1: Vehicle CAN Bus Policy
		CBabPolicy vehiclePolicy = new CBabPolicy("Vehicle CAN Bus Communication", project);
		vehiclePolicy.setDescription("Default policy for vehicle CAN bus to Ethernet protocol conversion");
		vehiclePolicy.setIsActive(true);
		vehiclePolicy.setPriorityLevel(80);
		vehiclePolicy.setPolicyVersion("1.0");
		vehiclePolicy = policyService.save(vehiclePolicy);
		LOGGER.info("Created sample policy: {}", vehiclePolicy.getName());
		if (minimal) {
			return;
		}
		// Create sample policy 2: ROS Bridge Policy
		CBabPolicy rosPolicy = new CBabPolicy("ROS Bridge Policy", project);
		rosPolicy.setDescription("Policy for ROS message routing and topic management");
		rosPolicy.setIsActive(false);
		rosPolicy.setPriorityLevel(60);
		rosPolicy.setPolicyVersion("1.0");
		rosPolicy = policyService.save(rosPolicy);
		LOGGER.info("Created sample policy: {}", rosPolicy.getName());
		// Create sample policy 3: Modbus RTU Policy
		CBabPolicy modbusPolicy = new CBabPolicy("Industrial Modbus Policy", project);
		modbusPolicy.setDescription("Policy for Modbus RTU sensor data collection and forwarding");
		modbusPolicy.setIsActive(false);
		modbusPolicy.setPriorityLevel(40);
		modbusPolicy.setPolicyVersion("1.0");
		modbusPolicy = policyService.save(modbusPolicy);
		LOGGER.info("Created sample policy: {}", modbusPolicy.getName());
	}

	private CBabPolicy_InitializerService() {
		// Utility class - no instantiation
	}
}
