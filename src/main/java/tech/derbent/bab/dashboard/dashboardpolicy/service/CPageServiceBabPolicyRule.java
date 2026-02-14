package tech.derbent.bab.dashboard.dashboardpolicy.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.bab.dashboard.dashboardpolicy.domain.CBabPolicyRule;
import tech.derbent.bab.policybase.action.domain.CBabPolicyAction;
import tech.derbent.bab.policybase.action.service.CBabPolicyActionService;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilter;
import tech.derbent.bab.policybase.filter.service.CBabPolicyFilterService;
import tech.derbent.bab.policybase.node.can.CBabCanNodeService;
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;
import tech.derbent.bab.policybase.node.file.CBabFileInputNodeService;
import tech.derbent.bab.policybase.node.ip.CBabHttpServerNodeService;
import tech.derbent.bab.policybase.node.modbus.CBabModbusNodeService;
import tech.derbent.bab.policybase.trigger.domain.CBabPolicyTrigger;
import tech.derbent.bab.policybase.trigger.service.CBabPolicyTriggerService;
import tech.derbent.api.session.service.ISessionService;

@Profile ("bab")
public class CPageServiceBabPolicyRule extends CPageServiceDynamicPage<CBabPolicyRule> {
	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceBabPolicyRule.class);

	public CPageServiceBabPolicyRule(final IPageServiceImplementer<CBabPolicyRule> view) {
		super(view);
	}

	/** Get available nodes for project - used by ComboBox data provider. Returns all nodes in the current project for source/destination node
	 * selection. Since CBabNodeEntity uses JOINED inheritance, we need to query each concrete type and combine the results (HTTP servers, vehicles,
	 * file inputs, CAN nodes, etc.).
	 * @return list of all BAB nodes in the active project */
	public List<CBabNodeEntity<?>> getAvailableNodesForProject() {
		try {
			// Get session service
			final ISessionService sessionService = CSpringContext.getBean(ISessionService.class);
			final Optional<CProject<?>> projectOpt = sessionService.getActiveProject();
			if (projectOpt.isEmpty()) {
				LOGGER.warn("No active project - returning empty node list");
				return List.of();
			}
			final CProject<?> project = projectOpt.get();
			final List<CBabNodeEntity<?>> allNodes = new ArrayList<>();
			// Get all concrete node service types and fetch their entities
			// HTTP Server nodes
			try {
				final CBabHttpServerNodeService httpService = CSpringContext.getBean(CBabHttpServerNodeService.class);
				allNodes.addAll(httpService.listByProject(project));
			} catch (final Exception e) {
				LOGGER.debug("HTTP server node service not available: {}", e.getMessage());
			}
			// File Input nodes
			try {
				final CBabFileInputNodeService fileService = CSpringContext.getBean(CBabFileInputNodeService.class);
				allNodes.addAll(fileService.listByProject(project));
			} catch (final Exception e) {
				LOGGER.debug("File input node service not available: {}", e.getMessage());
			}
			// CAN nodes
			try {
				final CBabCanNodeService canService = CSpringContext.getBean(CBabCanNodeService.class);
				allNodes.addAll(canService.listByProject(project));
			} catch (final Exception e) {
				LOGGER.debug("CAN node service not available: {}", e.getMessage());
			}
			// Modbus nodes
			try {
				final CBabModbusNodeService modbusService = CSpringContext.getBean(CBabModbusNodeService.class);
				allNodes.addAll(modbusService.listByProject(project));
			} catch (final Exception e) {
				LOGGER.debug("Modbus node service not available: {}", e.getMessage());
			}
			LOGGER.debug("Retrieved {} nodes for project {}", allNodes.size(), project.getName());
			return allNodes;
		} catch (final Exception e) {
			LOGGER.error("Error retrieving nodes for project: {}", e.getMessage(), e);
			return List.of();
		}
	}

	public List<CBabPolicyAction> getAvailablePolicyActions() {
		try {
			final Optional<CProject<?>> projectOpt = CSpringContext.getBean(ISessionService.class).getActiveProject();
			if (projectOpt.isEmpty()) {
				return List.of();
			}
			return CSpringContext.getBean(CBabPolicyActionService.class).listByProject(projectOpt.get());
		} catch (final Exception e) {
			LOGGER.error("Error retrieving available policy actions: {}", e.getMessage(), e);
			return List.of();
		}
	}

	public List<CBabPolicyFilter> getAvailablePolicyFilters() {
		try {
			final Optional<CProject<?>> projectOpt = CSpringContext.getBean(ISessionService.class).getActiveProject();
			if (projectOpt.isEmpty()) {
				return List.of();
			}
			return CSpringContext.getBean(CBabPolicyFilterService.class).listByProject(projectOpt.get());
		} catch (final Exception e) {
			LOGGER.error("Error retrieving available policy filters: {}", e.getMessage(), e);
			return List.of();
		}
	}

	public List<CBabPolicyTrigger> getAvailablePolicyTriggers() {
		try {
			final Optional<CProject<?>> projectOpt = CSpringContext.getBean(ISessionService.class).getActiveProject();
			if (projectOpt.isEmpty()) {
				return List.of();
			}
			return CSpringContext.getBean(CBabPolicyTriggerService.class).listByProject(projectOpt.get());
		} catch (final Exception e) {
			LOGGER.error("Error retrieving available policy triggers: {}", e.getMessage(), e);
			return List.of();
		}
	}
}
