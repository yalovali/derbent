package tech.derbent.bab.policybase.action.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import tech.derbent.api.utils.Check;
import tech.derbent.bab.policybase.action.domain.CBabPolicyAction;
import tech.derbent.bab.policybase.action.view.CComponentBabPolicyActionMaskDetails;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskBase;
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;

/** Page service for policy actions with destination-node-aware mask selection. */
public class CPageServiceBabPolicyAction extends CPageServiceDynamicPage<CBabPolicyAction> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceBabPolicyAction.class);
	private final Map<String, CBabPolicyActionMaskBase<?>> maskByDestination = new HashMap<>();

	public CPageServiceBabPolicyAction(final IPageServiceImplementer<CBabPolicyAction> view) {
		super(view);
	}

	@Override
	public void actionSave() throws Exception {
		final CBabPolicyAction currentAction = getValue();
		if (currentAction != null) {
			ensureMaskForSelectedDestination(currentAction, currentAction.getDestinationNode());
		}
		super.actionSave();
		// Clear cache on save to avoid stale masks being reused across different nodes after action changes.
		maskByDestination.clear();
	}

	private String buildActionOwnerKey(final CBabPolicyAction action) {
		if (action == null) {
			return "";
		}
		return action.getId() != null ? "aid:" + action.getId() : "amem:" + System.identityHashCode(action);
	}

	private String buildNodeOwnerKey(final CBabNodeEntity<?> destinationNode) {
		if (destinationNode == null) {
			return "";
		}
		final String nodeKey =
				destinationNode.getId() != null ? "nid:" + destinationNode.getId() : "nmem:" + System.identityHashCode(destinationNode);
		final String projectKey = destinationNode.getProject() != null && destinationNode.getProject().getId() != null
				? "pid:" + destinationNode.getProject().getId() : "pid:null";
		return projectKey + "|" + nodeKey;
	}

	private void cacheMaskForDestination(final CBabPolicyAction action, final CBabNodeEntity<?> destinationNode,
			final CBabPolicyActionMaskBase<?> mask) {
		if (action == null || destinationNode == null || mask == null) {
			return;
		}
		stampMaskOwnershipContext(action, destinationNode, mask);
		final String cacheKey = getDestinationCacheKey(action, destinationNode);
		maskByDestination.put(cacheKey, mask);
		LOGGER.debug("Cached action mask actionId={} nodeId={} maskId={}", action.getId(), destinationNode.getId(), mask.getId());
	}

	public Component createComponentActionMaskDetails() {
		try {
			final CComponentBabPolicyActionMaskDetails component = new CComponentBabPolicyActionMaskDetails(getSessionService());
			registerComponent(component.getComponentName(), component);
			return component;
		} catch (final Exception e) {
			LOGGER.error("Failed to create action mask details component actionId={} reason={}", getValue() != null ? getValue().getId() : null,
					e.getMessage());
			CNotificationService.showException("Failed to load action mask details component", e);
			return CDiv.errorDiv("Failed to load action mask details component: " + e.getMessage());
		}
	}

	private void ensureMaskForSelectedDestination(final CBabPolicyAction action, final CBabNodeEntity<?> destinationNode) {
		if (action == null) {
			return;
		}
		final CBabPolicyActionService actionService = CSpringContext.getBean(CBabPolicyActionService.class);
		if (destinationNode == null) {
			action.setActionMask(null);
			return;
		}
		final String destinationKey = getDestinationCacheKey(action, destinationNode);
		CBabPolicyActionMaskBase<?> selectedMask = action.getActionMask();
		// Reuse current mask only when it is already stamped for the selected node.
		// Checking compatibility via policyAction.destinationNode is not sufficient here
		// because destination node may already be switched on the action object.
		if (selectedMask != null && isMaskStampedFor(action, destinationNode, selectedMask)) {
			stampMaskOwnershipContext(action, destinationNode, selectedMask);
			maskByDestination.put(destinationKey, selectedMask);
			return;
		}
		selectedMask = maskByDestination.get(destinationKey);
		if (selectedMask != null && !isMaskStampedFor(action, destinationNode, selectedMask)) {
			selectedMask = null;
		}
		if (selectedMask == null) {
			selectedMask = actionService.createMaskForDestination(action, destinationNode);
			stampMaskOwnershipContext(action, destinationNode, selectedMask);
			if (action.getId() != null && selectedMask.getId() == null) {
				selectedMask = actionService.persistActionMaskForAction(action, selectedMask);
				stampMaskOwnershipContext(action, destinationNode, selectedMask);
			}
			maskByDestination.put(destinationKey, selectedMask);
			LOGGER.debug("Created action mask actionId={} nodeId={} maskId={}", action.getId(), destinationNode.getId(), selectedMask.getId());
		}
		action.setActionMask(selectedMask);
		Check.notNull(action.getActionMask(), "Action mask must not be null after destination node selection");
	}

	public List<CBabPolicyActionMaskBase<?>> getComboValuesOfActionMaskForDestinationNode() {
		CBabPolicyAction currentAction = null;
		try {
			currentAction = getValue();
			if (currentAction == null) {
				return List.of();
			}
			ensureMaskForSelectedDestination(currentAction, currentAction.getDestinationNode());
			return CSpringContext.getBean(CBabPolicyActionService.class).listMasksForAction(currentAction);
		} catch (final Exception e) {
			LOGGER.error("Failed to load action masks actionId={} reason={}", currentAction != null ? currentAction.getId() : null, e.getMessage());
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
			LOGGER.error("Failed to load destination nodes actionId={} reason={}", getValue() != null ? getValue().getId() : null, e.getMessage());
			return List.of();
		}
	}

	public List<Integer> getComboValuesOfTimeoutSeconds() { return List.of(1, 2, 5, 10, 15, 30, 60, 120, 300); }

	private String getDestinationCacheKey(final CBabPolicyAction action, final CBabNodeEntity<?> destinationNode) {
		if (action == null || destinationNode == null) {
			return "null";
		}
		final String actionKey = action.getId() != null ? "aid:" + action.getId() : "amem:" + System.identityHashCode(action);
		final String nodeKey =
				destinationNode.getId() != null ? "nid:" + destinationNode.getId() : "nmem:" + System.identityHashCode(destinationNode);
		return actionKey + "|" + nodeKey;
	}

	private boolean isMaskStampedFor(final CBabPolicyAction action, final CBabNodeEntity<?> destinationNode, final CBabPolicyActionMaskBase<?> mask) {
		if (action == null || destinationNode == null || mask == null) {
			return false;
		}
		return buildActionOwnerKey(action).equals(mask.getUiOwnerActionKey()) && buildNodeOwnerKey(destinationNode).equals(mask.getUiOwnerNodeKey());
	}

	private boolean isSameNode(final CBabNodeEntity<?> a, final CBabNodeEntity<?> b) {
		if (a == b) {
			return true;
		}
		if (a == null || b == null) {
			return false;
		}
		if (a.getId() != null && b.getId() != null) {
			return Objects.equals(a.getId(), b.getId());
		}
		return false;
	}

	public void on_actionMask_change(final Component component, final Object value) {
		final CBabPolicyAction currentAction = getValue();
		if (currentAction == null) {
			return;
		}
		// CNavigableComboBox can emit a transient null change during item refresh.
		// Treat that as UI noise and keep the current model value instead of clearing actionMask.
		if (value == null && currentAction.getActionMask() != null) {
			LOGGER.debug("Ignored transient null actionMask event actionId={} componentClass={}", currentAction.getId(),
					component != null ? component.getClass().getSimpleName() : null);
			return;
		}
		currentAction.setActionMask(value instanceof CBabPolicyActionMaskBase<?> ? (CBabPolicyActionMaskBase<?>) value : null);
		LOGGER.debug("Updated action mask actionId={} maskId={} valueClass={}", currentAction.getId(),
				currentAction.getActionMask() != null ? currentAction.getActionMask().getId() : null,
				value != null ? value.getClass().getSimpleName() : null);
	}

	public void on_destinationNode_change(final Component component, final Object value) {
		try {
			final CBabNodeEntity<?> selectedDestinationNode = value instanceof CBabNodeEntity<?> ? (CBabNodeEntity<?>) value : null;
			final CBabPolicyAction currentAction = getValue();
			if (currentAction != null) {
				final CBabNodeEntity<?> previousDestination = currentAction.getDestinationNode();
				final CBabPolicyActionMaskBase<?> previousMask = currentAction.getActionMask();
				final boolean maskMatchesSelectedNode = isMaskStampedFor(currentAction, selectedDestinationNode, previousMask);
				final boolean destinationChanged = !isSameNode(previousDestination, selectedDestinationNode) || !maskMatchesSelectedNode;
				LOGGER.debug("Destination change actionId={} previousNodeId={} selectedNodeId={} maskId={} destinationChanged={} componentClass={}",
						currentAction.getId(), previousDestination != null ? previousDestination.getId() : null,
						selectedDestinationNode != null ? selectedDestinationNode.getId() : null, previousMask != null ? previousMask.getId() : null,
						destinationChanged, component != null ? component.getClass().getSimpleName() : null);
				// Binder/UI may have already applied the selected destination on entity before this handler runs.
				// Cache old mask only when it is stamped for the currently observed destination to avoid cross-node cache corruption.
				if (isMaskStampedFor(currentAction, previousDestination, previousMask)) {
					cacheMaskForDestination(currentAction, previousDestination, previousMask);
				}
				if (destinationChanged) {
					// Force reassignment path for a truly different node; prevents stale mask from staying selected.
					currentAction.setActionMask(null);
				}
				currentAction.setDestinationNode(selectedDestinationNode);
				ensureMaskForSelectedDestination(currentAction, selectedDestinationNode);
				Check.isTrue(selectedDestinationNode == null || currentAction.getActionMask() != null,
						"Destination change produced null action mask for non-null destination");
			}
			syncActionMaskComboWithCurrentEntity();
			getView().populateForm();
		} catch (final Exception e) {
			LOGGER.error("Failed destination change actionId={} valueClass={} componentClass={} reason={}",
					getValue() != null ? getValue().getId() : null, value != null ? value.getClass().getSimpleName() : "null",
					component != null ? component.getClass().getSimpleName() : "null", e.getMessage());
			CNotificationService.showException("Failed to refresh allowed action masks", e);
		}
	}

	private void stampMaskOwnershipContext(final CBabPolicyAction action, final CBabNodeEntity<?> destinationNode,
			final CBabPolicyActionMaskBase<?> mask) {
		if (action == null || destinationNode == null || mask == null) {
			return;
		}
		mask.setUiOwnerActionKey(buildActionOwnerKey(action));
		mask.setUiOwnerNodeKey(buildNodeOwnerKey(destinationNode));
	}

	private void syncActionMaskComboWithCurrentEntity() {
		final CBabPolicyAction action = getValue();
		if (action == null) {
			return;
		}
		try {
			ensureMaskForSelectedDestination(action, action.getDestinationNode());
			final ComboBox<CBabPolicyActionMaskBase<?>> actionMaskCombo = getComboBox("actionMask");
			final List<CBabPolicyActionMaskBase<?>> items = CSpringContext.getBean(CBabPolicyActionService.class).listMasksForAction(action);
			actionMaskCombo.setItems(items);
			CBabPolicyActionMaskBase<?> selected = action.getActionMask();
			// When destination change yields exactly one compatible mask, force-select it so
			// UI and entity stay aligned without requiring extra user interaction.
			if (selected == null && items.size() == 1) {
				selected = items.get(0);
				action.setActionMask(selected);
				LOGGER.debug("Auto-selected single action mask actionId={} maskId={}", action.getId(), selected.getId());
			}
			if (selected != null) {
				actionMaskCombo.setValue(selected);
			} else {
				actionMaskCombo.clear();
			}
			Check.isTrue(action.getDestinationNode() == null || actionMaskCombo.getValue() != null,
					"Action mask combo value must not be null when destination node is selected");
			LOGGER.debug("Synchronized actionMask combo actionId={} itemCount={} comboValueId={} entityMaskId={}", action.getId(), items.size(),
					actionMaskCombo.getValue() != null ? actionMaskCombo.getValue().getId() : null,
					action.getActionMask() != null ? action.getActionMask().getId() : null);
		} catch (final Exception e) {
			LOGGER.error("Failed to sync actionMask combo actionId={} reason={}", action.getId(), e.getMessage());
			throw new IllegalStateException("Failed to synchronize actionMask combo for actionId=" + action.getId(), e);
		}
	}
}
