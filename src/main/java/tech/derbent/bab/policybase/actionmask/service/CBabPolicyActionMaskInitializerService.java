package tech.derbent.bab.policybase.actionmask.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.bab.policybase.node.can.CBabCanNode;
import tech.derbent.bab.policybase.node.can.CBabCanNodeService;
import tech.derbent.bab.policybase.node.file.CBabFileOutputNode;
import tech.derbent.bab.policybase.node.file.CBabFileOutputNodeService;
import tech.derbent.bab.policybase.node.ros.CBabROSNode;
import tech.derbent.bab.policybase.node.ros.CBabROSNodeService;

/** Coordinator for all policy action mask initializers and sample creation. */
@Service
@Profile ("bab")
public final class CBabPolicyActionMaskInitializerService extends CInitializerServiceBase {

	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyActionMaskInitializerService.class);

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		CBabPolicyActionMaskCANInitializerService.initialize(project, gridEntityService, detailSectionService, pageEntityService);
		CBabPolicyActionMaskFileInitializerService.initialize(project, gridEntityService, detailSectionService, pageEntityService);
		CBabPolicyActionMaskROSInitializerService.initialize(project, gridEntityService, detailSectionService, pageEntityService);
	}

	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		LOGGER.info("Initializing action mask samples for project: {}", project.getName());
		initializeCanMasks(project, minimal);
		initializeFileMasks(project, minimal);
		initializeRosMasks(project, minimal);
	}

	private static void initializeCanMasks(final CProject<?> project, final boolean minimal) throws Exception {
		final CBabCanNodeService service = CSpringContext.getBean(CBabCanNodeService.class);
		for (final CBabCanNode node : service.listByProject(project)) {
			CBabPolicyActionMaskCANInitializerService.createSampleForNode(node);
			if (minimal) {
				break;
			}
		}
	}

	private static void initializeFileMasks(final CProject<?> project, final boolean minimal) throws Exception {
		final CBabFileOutputNodeService service = CSpringContext.getBean(CBabFileOutputNodeService.class);
		for (final CBabFileOutputNode node : service.listByProject(project)) {
			CBabPolicyActionMaskFileInitializerService.createSampleForNode(node);
			if (minimal) {
				break;
			}
		}
	}

	private static void initializeRosMasks(final CProject<?> project, final boolean minimal) throws Exception {
		final CBabROSNodeService service = CSpringContext.getBean(CBabROSNodeService.class);
		for (final CBabROSNode node : service.listByProject(project)) {
			CBabPolicyActionMaskROSInitializerService.createSampleForNode(node);
			if (minimal) {
				break;
			}
		}
	}

	private CBabPolicyActionMaskInitializerService() {
		// Utility class
	}
}
