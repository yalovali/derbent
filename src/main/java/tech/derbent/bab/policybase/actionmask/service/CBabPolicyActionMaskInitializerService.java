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
import tech.derbent.bab.policybase.action.domain.CBabPolicyAction;
import tech.derbent.bab.policybase.action.service.CBabPolicyActionService;
import tech.derbent.bab.policybase.node.can.CBabCanNode;
import tech.derbent.bab.policybase.node.file.CBabFileOutputNode;
import tech.derbent.bab.policybase.node.ros.CBabROSNode;

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
		final CBabPolicyActionService actionService = CSpringContext.getBean(CBabPolicyActionService.class);
		for (final CBabPolicyAction action : actionService.listByProject(project)) {
			if (action.getDestinationNode() instanceof CBabCanNode) {
				CBabPolicyActionMaskCANInitializerService.createSampleForAction(action);
			} else if (action.getDestinationNode() instanceof CBabFileOutputNode) {
				CBabPolicyActionMaskFileInitializerService.createSampleForAction(action);
			} else if (action.getDestinationNode() instanceof CBabROSNode) {
				CBabPolicyActionMaskROSInitializerService.createSampleForAction(action);
			}
			if (minimal) {
				break;
			}
		}
	}

	private CBabPolicyActionMaskInitializerService() {
		// Utility class
	}
}
