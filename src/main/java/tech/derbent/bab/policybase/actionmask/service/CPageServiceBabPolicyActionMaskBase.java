package tech.derbent.bab.policybase.actionmask.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.enhanced.CCrudToolbar;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskBase;
import tech.derbent.bab.policybase.rule.domain.CBabPolicyRule;
import tech.derbent.bab.utils.CJsonSerializer;
import tech.derbent.bab.utils.CJsonSerializer.EJsonScenario;

@Profile ("bab")
public abstract class CPageServiceBabPolicyActionMaskBase<MaskType extends CBabPolicyActionMaskBase<MaskType>>
		extends CPageServiceDynamicPage<MaskType> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceBabPolicyActionMaskBase.class);
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	protected CPageServiceBabPolicyActionMaskBase(final IPageServiceImplementer<MaskType> view) {
		super(view);
	}

	@Override
	public void actionCreate() {
		CNotificationService.showWarning("Action masks are created from Policy Actions. Please select a Policy Action to add a mask.");
	}

	@Override
	protected void configureToolbar(final CCrudToolbar toolbar) {
		toolbar.configureButtonVisibility(false, true, true, true);
		final CButton buttonToJson = new CButton("To JSON", VaadinIcon.PLAY.create());
		buttonToJson.addClickListener(event -> on_toJson_clicked());
		toolbar.addCustomComponent(buttonToJson);
		final CButton buttonProtocolOutput = new CButton("Input Structure", VaadinIcon.PLAY.create());
		buttonProtocolOutput.addClickListener(event -> on_buttonProtocolOutput_clicked());
		toolbar.addCustomComponent(buttonProtocolOutput);
	}

	private void on_buttonProtocolOutput_clicked() {
		if (getValue() == null) {
			LOGGER.warn("Input Structure button clicked but no action mask loaded");
			CNotificationService.showWarning("Please load an action mask before viewing input structure");
			return;
		}
		if (getValue().getPolicyAction() == null) {
			CNotificationService.showWarning("Current action mask is not attached to a policy action");
			return;
		}
		final CBabPolicyRule policyRule = getValue().getPolicyAction().getPolicyRule();
		if (policyRule == null) {
			CNotificationService.showWarning("Current action mask has no owning policy rule");
			return;
		}
		if (policyRule.getFilter() == null) {
			CNotificationService.showWarning("No active policy filter is selected for this rule");
			return;
		}
		try {
			final String outputStructureJson = OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
					.writeValueAsString(policyRule.getFilter().getOutputStructure());
			CNotificationService.showInfoDialog("Active Filter Output Structure", outputStructureJson);
		} catch (final Exception e) {
			LOGGER.error("Error retrieving filter output structure for maskId={}. reason={}",
					getValue() != null ? getValue().getId() : null, e.getMessage());
			CNotificationService.showException("Failed to retrieve filter output structure", e);
		}
	}

	private void on_toJson_clicked() {
		if (getValue() == null) {
			LOGGER.warn("ToJson button clicked but no action mask loaded");
			CNotificationService.showWarning("Please load an action mask before converting to JSON");
			return;
		}
		try {
			final String json = CJsonSerializer.toJson(getValue(), EJsonScenario.JSONSENARIO_BABPOLICY);
			CNotificationService.showInfoDialog("Action Mask JSON", json);
		} catch (final Exception e) {
			LOGGER.error("Error converting action mask to JSON: {}", e.getMessage());
			CNotificationService.showException("Failed to convert action mask to JSON", e);
		}
	}
}
