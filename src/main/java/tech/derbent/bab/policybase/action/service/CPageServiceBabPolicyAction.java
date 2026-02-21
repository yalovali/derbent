package tech.derbent.bab.policybase.action.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.policybase.action.domain.CBabPolicyAction;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskBase;
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;

/** Page service for policy actions with destination-node-aware mask selection. */
public class CPageServiceBabPolicyAction extends CPageServiceDynamicPage<CBabPolicyAction> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceBabPolicyAction.class);

	public CPageServiceBabPolicyAction(final IPageServiceImplementer<CBabPolicyAction> view) {
		super(view);
	}

	public List<CBabPolicyActionMaskBase<?>> getComboValuesOfActionMaskForDestinationNode() {
		try {
			final CBabNodeEntity<?> destinationNode = resolveCurrentDestinationNode(null);
			if (destinationNode == null) {
				return List.of();
			}
			return CSpringContext.getBean(CBabPolicyActionService.class).listMasksForDestinationNode(destinationNode);
		} catch (final Exception e) {
			LOGGER.error("Error retrieving action masks: {}", e.getMessage(), e);
			return List.of();
		}
	}

	public List<CBabNodeEntity<?>> getComboValuesOfDestinationNodeForProject() {
		try {
			final Optional<CProject<?>> projectOpt = CSpringContext.getBean(ISessionService.class).getActiveProject();
			if (projectOpt.isEmpty()) {
				return List.of();
			}
			return CSpringContext.getBean(CBabPolicyActionService.class).listSupportedDestinationNodes(projectOpt.get());
		} catch (final Exception e) {
			LOGGER.error("Error retrieving destination nodes for action page: {}", e.getMessage(), e);
			return List.of();
		}
	}

	public List<Integer> getComboValuesOfTimeoutSeconds() {
		return List.of(1, 2, 5, 10, 15, 30, 60, 120, 300);
	}

	public void on_destinationNode_change(final Component component, final Object value) {
		LOGGER.info("function: on_destinationNode_change for Component type: {}", component.getClass().getSimpleName());
		try {
			final CBabNodeEntity<?> selectedDestinationNode = resolveCurrentDestinationNode(value);
			final CBabPolicyAction currentAction = getValue();
			if (currentAction != null) {
				currentAction.setDestinationNode(selectedDestinationNode);
			}
			refreshActionMaskCombo(selectedDestinationNode);
		} catch (final Exception e) {
			LOGGER.error("Error handling destination node change: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to refresh allowed action masks", e);
		}
	}

	private void refreshActionMaskCombo(final CBabNodeEntity<?> selectedDestinationNode) {
		final List<CBabPolicyActionMaskBase<?>> allowedMasks = selectedDestinationNode == null ? List.of()
				: CSpringContext.getBean(CBabPolicyActionService.class).listMasksForDestinationNode(selectedDestinationNode);
		try {
			final ComboBox<CBabPolicyActionMaskBase<?>> actionMaskCombo = getComboBox("actionMask");
			final CBabPolicyActionMaskBase<?> previousValue = actionMaskCombo.getValue();
			actionMaskCombo.setItems(allowedMasks);
			if (selectedDestinationNode == null) {
				actionMaskCombo.setPlaceholder("Select destination node first");
				actionMaskCombo.setHelperText("Action masks depend on destination node.");
				actionMaskCombo.clear();
				return;
			}
			if (allowedMasks.isEmpty()) {
				actionMaskCombo.setPlaceholder("No masks available for selected node");
				actionMaskCombo.setHelperText("Create action mask entities for this destination node first.");
				actionMaskCombo.clear();
				return;
			}
			actionMaskCombo.setPlaceholder("Select action mask");
			actionMaskCombo.setHelperText("Available masks: " + allowedMasks.size());
			if (previousValue != null && allowedMasks.stream().anyMatch(mask -> Objects.equals(mask.getId(), previousValue.getId()))) {
				actionMaskCombo.setValue(previousValue);
			} else {
				actionMaskCombo.setValue(allowedMasks.get(0));
			}
		} catch (final Exception e) {
			LOGGER.debug("Action mask combo is not available yet for refresh: {}", e.getMessage());
		}
	}

	private CBabNodeEntity<?> resolveCurrentDestinationNode(final Object changedValue) {
		if (changedValue instanceof CBabNodeEntity<?>) {
			return (CBabNodeEntity<?>) changedValue;
		}
		try {
			final Object destinationNodeValue = getComponentValue("destinationNode");
			if (destinationNodeValue instanceof CBabNodeEntity<?>) {
				return (CBabNodeEntity<?>) destinationNodeValue;
			}
		} catch (final Exception e) {
			LOGGER.debug("Could not resolve destination node from component context: {}", e.getMessage());
		}
		final CBabPolicyAction currentAction = getValue();
		return currentAction != null ? currentAction.getDestinationNode() : null;
	}
}
