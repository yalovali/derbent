package tech.derbent.bab.policybase.action.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.interfaces.IComponentTransientPlaceHolder;
import tech.derbent.api.interfaces.IPageServiceAutoRegistrable;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.component.enhanced.CComponentBase;
import tech.derbent.api.ui.component.enhanced.CComponentItemDetails;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.policybase.action.domain.CBabPolicyAction;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskBase;

/** Displays the selected action mask dynamic detail view under the action mask selector. */
public class CComponentBabPolicyActionMaskDetails extends CComponentBase<CBabPolicyAction>
		implements IPageServiceAutoRegistrable, IComponentTransientPlaceHolder<CBabPolicyAction> {

	public static final String COMPONENT_NAME = "policyActionMaskDetails";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentBabPolicyActionMaskDetails.class);
	private static final long serialVersionUID = 1L;
	private final CComponentItemDetails componentItemDetails;
	private final CSpan labelStatus;

	public CComponentBabPolicyActionMaskDetails(final ISessionService sessionService) throws Exception {
		Check.notNull(sessionService, "ISessionService cannot be null");
		setPadding(false);
		setSpacing(false);
		setWidthFull();
		labelStatus = new CSpan("Select an action mask to view its details.");
		labelStatus.getStyle().set("color", "var(--lumo-secondary-text-color)");
		componentItemDetails = new CComponentItemDetails(sessionService);
		componentItemDetails.setWidthFull();
		add(labelStatus, componentItemDetails);
		refreshComponent();
	}

	@Override
	public String getComponentName() { return COMPONENT_NAME; }

	@Override
	protected void onValueChanged(final CBabPolicyAction oldValue, final CBabPolicyAction newValue, final boolean fromClient) {
		super.onValueChanged(oldValue, newValue, fromClient);
		refreshComponent();
	}

	@Override
	protected void refreshComponent() {
		final CBabPolicyAction action = getValue();
		if (action == null) {
			labelStatus.setText("No action selected.");
			labelStatus.getStyle().set("color", "var(--lumo-secondary-text-color)");
			componentItemDetails.setValue(null);
			return;
		}
		final CBabPolicyActionMaskBase<?> actionMask = action.getActionMask();
		if (actionMask == null) {
			labelStatus.setText("Select an action mask to view its details.");
			labelStatus.getStyle().set("color", "var(--lumo-secondary-text-color)");
			componentItemDetails.setValue(null);
			return;
		}
		try {
			componentItemDetails.setValue(actionMask);
			labelStatus.setText("Showing details for action mask: " + actionMask.getName());
			labelStatus.getStyle().set("color", "var(--lumo-success-text-color)");
		} catch (final Exception e) {
			LOGGER.error("Failed to render action mask details for actionId={} maskId={}. reason={}",
					action.getId(), actionMask.getId(), e.getMessage());
			labelStatus.setText("Unable to render action mask details.");
			labelStatus.getStyle().set("color", "var(--lumo-error-text-color)");
			componentItemDetails.setValue(null);
		}
	}

	@Override
	public void setThis(final CBabPolicyAction value) {
		setValue(value);
	}
}
