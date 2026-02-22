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
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.policybase.action.domain.CBabPolicyAction;
import tech.derbent.bab.policybase.action.view.CComponentBabPolicyActionMaskDetails;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskBase;
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;

/** Page service for policy actions with destination-node-aware mask selection. */
public class CPageServiceBabPolicyAction extends CPageServiceDynamicPage<CBabPolicyAction> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceBabPolicyAction.class);

	public CPageServiceBabPolicyAction(final IPageServiceImplementer<CBabPolicyAction> view) {
		super(view);
	}

	public Component createComponentActionMaskDetails() {
		try {
			final CComponentBabPolicyActionMaskDetails component = new CComponentBabPolicyActionMaskDetails(getSessionService());
			registerComponent(component.getComponentName(), component);
			return component;
		} catch (final Exception e) {
			LOGGER.error("Failed to create action mask details component for actionId={}. reason={}",
					getValue() != null ? getValue().getId() : null, e.getMessage());
			CNotificationService.showException("Failed to load action mask details component", e);
			return CDiv.errorDiv("Failed to load action mask details component: " + e.getMessage());
		}
	}

	public List<CBabPolicyActionMaskBase<?>> getComboValuesOfActionMaskForDestinationNode() {
		CBabNodeEntity<?> destinationNode = null;
		try {
			destinationNode = resolveCurrentDestinationNode(null);
			if (destinationNode == null) {
				return List.of();
			}
			return CSpringContext.getBean(CBabPolicyActionService.class).listMasksForDestinationNode(destinationNode);
		} catch (final Exception e) {
			LOGGER.error("Failed to load action masks for destinationNodeId={}. reason={}",
					destinationNode != null ? destinationNode.getId() : null, e.getMessage());
			return List.of();
		}
	}

	public List<CBabNodeEntity<?>> getComboValuesOfDestinationNodeForProject() {
		try {
			final CBabPolicyAction currentAction = getValue();
			if (currentAction != null && currentAction.getPolicyRule() != null && currentAction.getPolicyRule().getProject() != null) {
				return CSpringContext.getBean(CBabPolicyActionService.class)
						.listSupportedDestinationNodes(currentAction.getPolicyRule().getProject());
			}
			final Optional<CProject<?>> projectOpt = CSpringContext.getBean(ISessionService.class).getActiveProject();
			if (projectOpt.isEmpty()) {
				return List.of();
			}
			return CSpringContext.getBean(CBabPolicyActionService.class).listSupportedDestinationNodes(projectOpt.get());
		} catch (final Exception e) {
			LOGGER.error("Failed to load destination nodes for actionId={}. reason={}",
					getValue() != null ? getValue().getId() : null, e.getMessage());
			return List.of();
		}
	}

	public List<Integer> getComboValuesOfTimeoutSeconds() { return List.of(1, 2, 5, 10, 15, 30, 60, 120, 300); }

	public void on_actionMask_change(final Component component, final Object value) {
		LOGGER.info("function: on_actionMask_change for Component type: {}", component.getClass().getSimpleName());
		final CBabPolicyAction currentAction = getValue();
		if (currentAction != null) {
			currentAction.setActionMask(value instanceof CBabPolicyActionMaskBase<?> ? (CBabPolicyActionMaskBase<?>) value : null);
		}
		refreshActionMaskDetailsComponent();
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
			refreshActionMaskDetailsComponent();
		} catch (final Exception e) {
			LOGGER.error("Failed to handle destination node change for actionId={} valueType={}. reason={}",
					getValue() != null ? getValue().getId() : null, value != null ? value.getClass().getSimpleName() : "null", e.getMessage());
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
			refreshActionMaskDetailsComponent();
		} catch (final Exception e) {
			LOGGER.debug("Action mask combo is not available yet for refresh: {}", e.getMessage());
		}
	}

	private void refreshActionMaskDetailsComponent() {
		final Component component = getComponentByName(CComponentBabPolicyActionMaskDetails.COMPONENT_NAME);
		if (component instanceof CComponentBabPolicyActionMaskDetails) {
			((CComponentBabPolicyActionMaskDetails) component).setValue(getValue());
		}
	}

	private CBabNodeEntity<?> resolveCurrentDestinationNode(final Object changedValue) {
		if (changedValue instanceof CBabNodeEntity<?>) {
			return (CBabNodeEntity<?>) changedValue;
		}
		final CBabPolicyAction currentAction = getValue();
		return currentAction != null ? currentAction.getDestinationNode() : null;
	}
}
