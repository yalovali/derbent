package tech.derbent.bab.dashboard.view.dialog;

import java.util.function.Consumer;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.dialogs.CDialog;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.dashboard.view.CNetworkInterface;
import tech.derbent.bab.dashboard.view.CNetworkInterfaceIpConfiguration;
import tech.derbent.bab.dashboard.view.CNetworkInterfaceIpUpdate;

/** Dialog for editing IPv4 settings for a Calimero network interface. */
public class CDialogEditInterfaceIp extends CDialog {

	private static final long serialVersionUID = 1L;

	private final CNetworkInterface targetInterface;
	private final Consumer<CNetworkInterfaceIpUpdate> onSave;
	private final TextField interfaceField = new TextField("Interface");
	private final TextField ipv4Field = new TextField("IPv4 Address");
	private final IntegerField prefixField = new IntegerField("Prefix Length");
	private final TextField gatewayField = new TextField("Gateway (optional)");
	private final Checkbox validationOnlyField = new Checkbox("Validation only (do not apply)");

	public CDialogEditInterfaceIp(final CNetworkInterface networkInterface, final Consumer<CNetworkInterfaceIpUpdate> onSave) {
		this.targetInterface = networkInterface;
		this.onSave = onSave;
		setWidth("420px");
		configureFields();
		try {
			setupDialog();
		} catch (final Exception e) {
			CNotificationService.showException("Failed to open interface editor", e);
		}
	}

	private void configureFields() {
		interfaceField.setReadOnly(true);
		interfaceField.setValue(targetInterface.getName());
		ipv4Field.setRequiredIndicatorVisible(true);
		ipv4Field.setValueChangeMode(ValueChangeMode.LAZY);
		prefixField.setMin(1);
		prefixField.setMax(32);
		prefixField.setStepButtonsVisible(true);
		gatewayField.setValueChangeMode(ValueChangeMode.LAZY);
		final CNetworkInterfaceIpConfiguration ipConfig = targetInterface.getIpConfiguration();
		if (ipConfig != null) {
			if (ipConfig.getIpv4Address() != null) {
				ipv4Field.setValue(ipConfig.getIpv4Address());
			} else {
				ipConfig.getIpv4AddressDisplay().ifPresent(ipv4Field::setValue);
			}
			if (ipConfig.getIpv4PrefixLength() != null) {
				prefixField.setValue(ipConfig.getIpv4PrefixLength());
			}
			if (ipConfig.getIpv4Gateway() != null) {
				gatewayField.setValue(ipConfig.getIpv4Gateway());
			}
		}
	}

	@Override
	public String getDialogTitleString() { return "Edit IP Address"; }

	@Override
	protected Icon getFormIcon() { return VaadinIcon.EDIT.create(); }

	@Override
	protected String getFormTitleString() { return "Interface Address"; }

	@Override
	protected void setupButtons() {
		final var saveButton = CButton.createSaveButton("Save", event -> attemptSave());
		final var cancelButton = CButton.createCancelButton("Cancel", event -> close());
		buttonLayout.add(saveButton, cancelButton);
		buttonLayout.setWidthFull();
		buttonLayout.setJustifyContentMode(com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.BETWEEN);
	}

	@Override
	protected void setupContent() {
		mainLayout.add(interfaceField, ipv4Field, prefixField, gatewayField, validationOnlyField);
	}

	private void attemptSave() {
		final String ipValue = ipv4Field.getValue() != null ? ipv4Field.getValue().trim() : "";
		if (ipValue.isBlank()) {
			CNotificationService.showWarning("IPv4 address is required");
			return;
		}
		if (!ipValue.matches("^(?:\\d{1,3}\\.){3}\\d{1,3}$")) {
			CNotificationService.showWarning("Invalid IPv4 format: " + ipValue);
			return;
		}
		final Integer prefix = prefixField.getValue();
		if ((prefix == null) || (prefix < 1) || (prefix > 32)) {
			CNotificationService.showWarning("Prefix length must be between 1 and 32");
			return;
		}
		final String gatewayValue = gatewayField.getValue() != null ? gatewayField.getValue().trim() : "";
		if (!gatewayValue.isBlank() && !gatewayValue.matches("^(?:\\d{1,3}\\.){3}\\d{1,3}$")) {
			CNotificationService.showWarning("Invalid gateway format: " + gatewayValue);
			return;
		}
		final CNetworkInterfaceIpUpdate update =
				new CNetworkInterfaceIpUpdate(targetInterface.getName(), ipValue, prefix, gatewayValue, validationOnlyField.getValue());
		onSave.accept(update);
		close();
	}
}
