package tech.derbent.bab.policybase.rule.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.enhanced.CCrudToolbar;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.policybase.action.domain.CBabPolicyAction;
import tech.derbent.bab.policybase.action.service.CBabPolicyActionService;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterBase;
import tech.derbent.bab.policybase.filter.service.CBabPolicyFilterCANService;
import tech.derbent.bab.policybase.filter.service.CBabPolicyFilterCSVService;
import tech.derbent.bab.policybase.filter.service.CBabPolicyFilterROSService;
import tech.derbent.bab.policybase.node.can.CBabCanNodeService;
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;
import tech.derbent.bab.policybase.node.file.CBabFileInputNodeService;
import tech.derbent.bab.policybase.node.file.CBabFileOutputNodeService;
import tech.derbent.bab.policybase.node.ip.CBabHttpServerNodeService;
import tech.derbent.bab.policybase.node.modbus.CBabModbusNodeService;
import tech.derbent.bab.policybase.node.ros.CBabROSNodeService;
import tech.derbent.bab.policybase.rule.domain.CBabPolicyRule;
import tech.derbent.bab.policybase.trigger.domain.CBabPolicyTrigger;
import tech.derbent.bab.policybase.trigger.service.CBabPolicyTriggerService;
import tech.derbent.bab.project.domain.CProject_Bab;
import tech.derbent.bab.project.service.CProject_BabService;
import tech.derbent.bab.utils.CJsonSerializer;

@Profile ("bab")
public class CPageServiceBabPolicyRule extends CPageServiceDynamicPage<CBabPolicyRule> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceBabPolicyRule.class);
	private CButton buttonApply;
	private CButton buttonToJson;

	public CPageServiceBabPolicyRule(final IPageServiceImplementer<CBabPolicyRule> view) {
		super(view);
	}

	@Override
	protected void configureToolbar(CCrudToolbar toolbar) {
		buttonToJson = new CButton("To JSON", VaadinIcon.PLAY.create());
		buttonToJson.addClickListener(event -> on_toJson_clicked());
		toolbar.addCustomComponent(buttonToJson);
		buttonApply = new CButton("Apply Policy", VaadinIcon.PLAY.create());
		buttonApply.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
		buttonApply.getElement().setAttribute("title", "Execute this policy");
		buttonApply.addClickListener(event -> on_apply_clicked());
		toolbar.addCustomComponent(buttonApply);
		//
	}

	/** Get all nodes in the current project for source/destination filtering. */
	private List<CBabNodeEntity<?>> getAllNodesForProject() {
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
			// File Output nodes
			try {
				final CBabFileOutputNodeService fileOutputService = CSpringContext.getBean(CBabFileOutputNodeService.class);
				allNodes.addAll(fileOutputService.listByProject(project));
			} catch (final Exception e) {
				LOGGER.debug("File output node service not available: {}", e.getMessage());
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
			// ROS nodes
			try {
				final CBabROSNodeService rosService = CSpringContext.getBean(CBabROSNodeService.class);
				allNodes.addAll(rosService.listByProject(project));
			} catch (final Exception e) {
				LOGGER.debug("ROS node service not available: {}", e.getMessage());
			}
			allNodes.sort(Comparator.comparing(node -> node.getName(), Comparator.nullsLast(String::compareToIgnoreCase)));
			LOGGER.debug("Retrieved {} nodes for project {}", allNodes.size(), project.getName());
			return allNodes;
		} catch (final Exception e) {
			LOGGER.error("Error retrieving nodes for project: {}", e.getMessage(), e);
			return List.of();
		}
	}

	/** Data provider for legacy/general node selection. */
	public List<CBabNodeEntity<?>> getComboValuesOfNodeForProject() {
		return getAllNodesForProject();
	}

	/** Data provider for source-node selection. */
	public List<CBabNodeEntity<?>> getComboValuesOfSourceNodeForProject() {
		return getAllNodesForProject();
	}

	/** Data provider for destination-node selection. */
	public List<CBabNodeEntity<?>> getComboValuesOfDestinationNodeForProject() {
		return getAllNodesForProject();
	}

	public List<CBabPolicyAction> getComboValuesOfPolicyAction() {
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

	public List<CBabPolicyFilterBase<?>> getComboValuesOfPolicyFilter() {
		try {
			final Optional<CProject<?>> projectOpt = CSpringContext.getBean(ISessionService.class).getActiveProject();
			if (projectOpt.isEmpty()) {
				return List.of();
			}
			final CProject<?> project = projectOpt.get();
			final CBabNodeEntity<?> selectedSourceNode = resolveCurrentSourceNode(null);
			return getPolicyFiltersForSourceNode(project, selectedSourceNode);
		} catch (final Exception e) {
			LOGGER.error("Error retrieving available policy filters: {}", e.getMessage(), e);
			return List.of();
		}
	}

	public List<CBabPolicyTrigger> getComboValuesOfPolicyTrigger() {
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

	private void on_apply_clicked() {
		if (getValue() == null) {
			LOGGER.warn("Apply button clicked but no policy rule loaded");
			CNotificationService.showWarning("Please load a policy rule before applying");
			return;
		}
		try {
			final CProject_Bab project = (CProject_Bab) getSessionService().getActiveProject().orElseThrow();
			CProject_BabService.getCalculatedValueOfPolicyRules(project);
			final String json = CJsonSerializer.toPrettyJson(project);
			final File tempFile = new File(System.getProperty("java.io.tmpdir"), "bab_policy_rule.json");
			try (var writer = new java.io.FileWriter(tempFile)) {
				writer.write(json);
				writer.flush();
				CNotificationService.showInfoDialog("Policy Rule JSON",
						"Policy rule JSON has been written to temporary file:\n" + tempFile.getAbsolutePath());
			} catch (final Exception e) {
				LOGGER.error("Error writing policy rule JSON to temporary file: {}", e.getMessage(), e);
				CNotificationService.showError("Failed to write policy rule JSON to temporary file");
				return;
			}
		} catch (final Exception e) {
			LOGGER.error("Error converting policy rule to JSON: {}", e.getMessage());
			CNotificationService.showException("Failed to convert policy rule to JSON", e);
		}
	}

	public void on_sourceNode_change(final Component component, final Object value) {
		LOGGER.info("function: on_sourceNode_change for Component type: {}",
				component.getClass().getSimpleName() + " current value: " + value + " on page service:" + this.getClass().getSimpleName());
		try {
			final CBabNodeEntity<?> selectedSourceNode = resolveCurrentSourceNode(value);
			final CBabPolicyRule currentRule = getValue();
			if (currentRule != null) {
				currentRule.setSourceNode(selectedSourceNode);
			}
			refreshPolicyFilterCombo(selectedSourceNode);
		} catch (final Exception e) {
			LOGGER.error("Error handling source node change: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to refresh compatible policy filters", e);
		}
	}

	private boolean containsFilter(final List<CBabPolicyFilterBase<?>> filters, final CBabPolicyFilterBase<?> filter) {
		if (filter == null || filters == null || filters.isEmpty()) {
			return false;
		}
		return filters.stream().anyMatch(candidate -> {
			if (candidate == filter) {
				return true;
			}
			return candidate.getId() != null && filter.getId() != null && Objects.equals(candidate.getId(), filter.getId());
		});
	}

	private List<CBabPolicyFilterBase<?>> getAllPolicyFiltersForProject(final CProject<?> project) {
		final List<CBabPolicyFilterBase<?>> allFilters = new ArrayList<>();
		try {
			allFilters.addAll(CSpringContext.getBean(CBabPolicyFilterCSVService.class).listByProject(project));
		} catch (final Exception e) {
			LOGGER.debug("CSV filter service not available: {}", e.getMessage());
		}
		try {
			allFilters.addAll(CSpringContext.getBean(CBabPolicyFilterCANService.class).listByProject(project));
		} catch (final Exception e) {
			LOGGER.debug("CAN filter service not available: {}", e.getMessage());
		}
		try {
			allFilters.addAll(CSpringContext.getBean(CBabPolicyFilterROSService.class).listByProject(project));
		} catch (final Exception e) {
			LOGGER.debug("ROS filter service not available: {}", e.getMessage());
		}
		allFilters.sort(Comparator.comparing((CBabPolicyFilterBase<?> filter) -> filter.getExecutionOrder(), Comparator.nullsLast(Integer::compareTo))
				.thenComparing(filter -> filter.getName(), Comparator.nullsLast(String::compareToIgnoreCase)));
		return allFilters;
	}

	private String getNodeTypeLabel(final CBabNodeEntity<?> sourceNode) {
		if (sourceNode == null) {
			return "N/A";
		}
		final String simpleName = sourceNode.getClass().getSimpleName();
		return simpleName != null && !simpleName.isBlank() ? simpleName : "Unknown";
	}

	private List<CBabPolicyFilterBase<?>> getPolicyFiltersForSourceNode(final CProject<?> project, final CBabNodeEntity<?> sourceNode) {
		final List<CBabPolicyFilterBase<?>> allFilters = getAllPolicyFiltersForProject(project);
		if (sourceNode == null) {
			return allFilters;
		}
		return allFilters.stream().filter(filter -> {
			final Class<? extends CBabNodeEntity<?>> allowedNodeType = filter.getAllowedNodeType();
			return allowedNodeType != null && allowedNodeType.isAssignableFrom(sourceNode.getClass());
		}).toList();
	}

	private void refreshPolicyFilterCombo(final CBabNodeEntity<?> selectedSourceNode) {
		final Optional<CProject<?>> projectOpt = CSpringContext.getBean(ISessionService.class).getActiveProject();
		if (projectOpt.isEmpty()) {
			return;
		}
		final List<CBabPolicyFilterBase<?>> compatibleFilters = getPolicyFiltersForSourceNode(projectOpt.get(), selectedSourceNode);
		try {
			final ComboBox<CBabPolicyFilterBase<?>> filterCombo = getComboBox("filter");
			final CBabPolicyFilterBase<?> previousFilter = filterCombo.getValue();
			filterCombo.setItems(compatibleFilters);
			if (selectedSourceNode == null) {
				filterCombo.setPlaceholder("Select source node to narrow filter options");
				filterCombo.setHelperText("Showing all filter types. Pick source node for type-specific filtering.");
			} else if (compatibleFilters.isEmpty()) {
				filterCombo.setPlaceholder("No compatible filters for " + getNodeTypeLabel(selectedSourceNode));
				filterCombo.setHelperText("Create a matching filter type or change source node.");
			} else {
				filterCombo.setPlaceholder("Select filter for " + getNodeTypeLabel(selectedSourceNode));
				filterCombo.setHelperText("Showing " + compatibleFilters.size() + " compatible filter(s).");
			}
			if (previousFilter != null && !containsFilter(compatibleFilters, previousFilter)) {
				filterCombo.clear();
				final CBabPolicyRule currentRule = getValue();
				if (currentRule != null) {
					currentRule.setFilter(null);
				}
				CNotificationService.showInfo(
						"Selected filter was cleared because it is not compatible with source node type " + getNodeTypeLabel(selectedSourceNode) + ".");
			}
		} catch (final Exception e) {
			LOGGER.debug("Policy filter component is not available yet for refresh: {}", e.getMessage());
		}
		if (selectedSourceNode != null && compatibleFilters.isEmpty()) {
			CNotificationService.showWarning("No compatible policy filters found for source node type " + getNodeTypeLabel(selectedSourceNode) + ".");
		}
	}

	private CBabNodeEntity<?> resolveCurrentSourceNode(final Object changedValue) {
		if (changedValue instanceof CBabNodeEntity<?>) {
			return (CBabNodeEntity<?>) changedValue;
		}
		try {
			final Object sourceNodeValue = getComponentValue("sourceNode");
			if (sourceNodeValue instanceof CBabNodeEntity<?>) {
				return (CBabNodeEntity<?>) sourceNodeValue;
			}
		} catch (final Exception e) {
			LOGGER.debug("Could not resolve source node from component context: {}", e.getMessage());
		}
		final CBabPolicyRule currentRule = getValue();
		return currentRule != null ? currentRule.getSourceNode() : null;
	}

	private void on_toJson_clicked() {
		if (getValue() == null) {
			LOGGER.warn("ToJson button clicked but no policy rule loaded");
			CNotificationService.showWarning("Please load a policy rule before converting to JSON");
			return;
		}
		try {
			final String json = CJsonSerializer.toPrettyJson(getValue());
			LOGGER.debug("Policy rule JSON: {}", json);
			CNotificationService.showInfoDialog("Rule JSON", json);
		} catch (final Exception e) {
			LOGGER.error("Error converting policy rule to JSON: {}", e.getMessage(), e);
			CNotificationService.showError("Failed to convert policy rule to JSON");
		}
	}
}
