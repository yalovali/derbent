package tech.derbent.bab.dashboard.dashboardproject_bab.view.dialog;
import java.util.function.Consumer;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.dashboard.dashboardproject_bab.dto.CDTONetworkInterface;
import tech.derbent.bab.dashboard.dashboardproject_bab.dto.CDTONetworkInterfaceIpConfiguration;
import tech.derbent.bab.dashboard.dashboardproject_bab.dto.CDTONetworkInterfaceIpUpdate;

/** Dialog for editing IPv4 settings for a Calimero network interface with DHCP support.
 * <p>
 * Features:
 * <ul>
 * <li>DHCP configuration (checkbox enables/disables manual fields)</li>
 * <li>Manual IP configuration with validation</li>
 * <li>Prefix length support (CIDR notation)</li>
 * </ul>
 */
public class CDialogEditInterfaceIp extends CBabDialogBase {

	private static final long serialVersionUID = 1L;

	private final CDTONetworkInterface targetInterface;
	private final Consumer<CDTONetworkInterfaceIpUpdate> onSave;
	
	// UI Components
	private TextField interfaceField;
	private Checkbox dhcpCheckbox;
	private TextField ipv4Field;
	private IntegerField prefixField;
	private CDiv validationSection;

	public CDialogEditInterfaceIp(final CDTONetworkInterface networkInterface, final Consumer<CDTONetworkInterfaceIpUpdate> onSave) {
		this.targetInterface = networkInterface;
		this.onSave = onSave;
		configureBabDialog("500px");
	}

	@Override
	public String getDialogTitleString() {
		return "Edit IP Address - " + targetInterface.getName();
	}

	@Override
	protected Icon getFormIcon() {
		return VaadinIcon.COG.create();
	}

	@Override
	protected String getFormTitleString() {
		return "Network Configuration";
	}

	@Override
	protected void setupButtons() {
		final CButton saveButton = CButton.createSaveButton("Apply", event -> attemptSave());
		final CButton cancelButton = CButton.createCancelButton("Cancel", event -> close());
		buttonLayout.add(saveButton, cancelButton);
	}

	@Override
	protected void setupContent() {
		// Apply BAB standard spacing
		applyCustomSpacing();
		
		// Interface Name (read-only)
		createInterfaceField();
		
		// DHCP Checkbox
		createDhcpCheckbox();
		
		// Manual Configuration Section
		createManualConfigSection();
		
		// Validation Info Section
		createValidationInfoSection();
		
		// Load current configuration
		loadCurrentConfiguration();
	}

	private void createInterfaceField() {
		interfaceField = new TextField("Interface");
		interfaceField.setReadOnly(true);
		interfaceField.setValue(targetInterface.getName());
		interfaceField.setWidthFull();
		mainLayout.add(interfaceField);
	}

	private void createDhcpCheckbox() {
		dhcpCheckbox = new Checkbox("Use DHCP (Dynamic IP)");
		dhcpCheckbox.addValueChangeListener(e -> {
			final boolean useDhcp = Boolean.TRUE.equals(e.getValue());
			ipv4Field.setEnabled(!useDhcp);
			prefixField.setEnabled(!useDhcp);
			
			if (useDhcp) {
				ipv4Field.setRequiredIndicatorVisible(false);
				prefixField.setRequiredIndicatorVisible(false);
			} else {
				ipv4Field.setRequiredIndicatorVisible(true);
				prefixField.setRequiredIndicatorVisible(true);
			}
			
			updateValidationDisplay();
		});
		dhcpCheckbox.getStyle().set("margin-top", "8px");
		dhcpCheckbox.getStyle().set("margin-bottom", "8px");
		mainLayout.add(dhcpCheckbox);
	}

	private void createManualConfigSection() {
		// IPv4 Address and Prefix in horizontal layout
		final CHorizontalLayout ipRow = new CHorizontalLayout();
		ipRow.setWidthFull();
		ipRow.setSpacing(true);
		ipRow.getStyle().set("gap", STYLE_GAP);
		
		// IPv4 Field
		ipv4Field = new TextField("IPv4 Address *");
		ipv4Field.setPlaceholder("192.168.1.100");
		ipv4Field.setWidth("100%");
		ipv4Field.setValueChangeMode(ValueChangeMode.LAZY);
		ipv4Field.addValueChangeListener(e -> updateValidationDisplay());
		
		// Prefix Length
		prefixField = new IntegerField("Prefix *");
		prefixField.setMin(1);
		prefixField.setMax(32);
		prefixField.setValue(24); // Default
		prefixField.setStepButtonsVisible(true);
		prefixField.setWidth("120px");
		prefixField.addValueChangeListener(e -> updateValidationDisplay());
		
		ipRow.add(ipv4Field, prefixField);
		ipRow.setFlexGrow(1, ipv4Field);
		ipRow.setFlexGrow(0, prefixField);
		
		mainLayout.add(ipRow);
		
		// Usage hint
		final CSpan ipHint = new CSpan("Example: 192.168.1.100/24 (Prefix: 24 = 255.255.255.0, 16 = 255.255.0.0)");
		ipHint.getStyle().set("font-size", STYLE_FONT_SIZE_XSMALL);
		ipHint.getStyle().set("color", "var(--lumo-secondary-text-color)");
		ipHint.getStyle().set("margin-bottom", STYLE_GAP);
		mainLayout.add(ipHint);
	}

	private void createValidationInfoSection() {
		validationSection = new CDiv();
		validationSection.getStyle().set("margin-top", STYLE_GAP);
		validationSection.getStyle().set("padding", "8px");
		validationSection.getStyle().set("background", "var(--lumo-contrast-5pct)");
		validationSection.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
		validationSection.getStyle().set("font-size", "var(--lumo-font-size-s)");
		mainLayout.add(validationSection);
		updateValidationDisplay();
	}

	private void updateValidationDisplay() {
		if (validationSection == null) {
			return; // Not initialized yet
		}
		
		validationSection.removeAll();
		
		final boolean useDhcp = Boolean.TRUE.equals(dhcpCheckbox.getValue());
		
		if (useDhcp) {
			final CSpan dhcpInfo = new CSpan("✅ DHCP mode: IP address will be automatically assigned");
			dhcpInfo.getStyle().set("color", "var(--lumo-success-text-color)");
			validationSection.add(dhcpInfo);
			return;
		}
		
		// Manual mode validation
		final String ip = ipv4Field.getValue();
		if (ip != null && !ip.isBlank() && isValidIpAddress(ip)) {
			final CSpan ipValid = new CSpan("✅ Valid IP address");
			ipValid.getStyle().set("color", "var(--lumo-success-text-color)");
			ipValid.getStyle().set("display", "block");
			validationSection.add(ipValid);
		} else {
			final CSpan ipInvalid = new CSpan("❌ Invalid or missing IP address");
			ipInvalid.getStyle().set("color", "var(--lumo-error-text-color)");
			ipInvalid.getStyle().set("display", "block");
			validationSection.add(ipInvalid);
		}
		
		final Integer prefix = prefixField.getValue();
		if (prefix != null && prefix >= 1 && prefix <= 32) {
			final CSpan prefixValid = new CSpan("✅ Valid prefix length");
			prefixValid.getStyle().set("color", "var(--lumo-success-text-color)");
			prefixValid.getStyle().set("display", "block");
			validationSection.add(prefixValid);
		} else {
			final CSpan prefixInvalid = new CSpan("❌ Invalid prefix length");
			prefixInvalid.getStyle().set("color", "var(--lumo-error-text-color)");
			prefixInvalid.getStyle().set("display", "block");
			validationSection.add(prefixInvalid);
		}
	}

	private void loadCurrentConfiguration() {
		final CDTONetworkInterfaceIpConfiguration ipConfig = targetInterface.getIpConfiguration();
		if (ipConfig != null) {
			// Check if DHCP is enabled
			final Boolean dhcp4 = targetInterface.getDhcp4();
			if (Boolean.TRUE.equals(dhcp4)) {
				dhcpCheckbox.setValue(true);
			} else {
				dhcpCheckbox.setValue(false);
				
				// Load manual configuration
				if (ipConfig.getIpv4Address() != null) {
					ipv4Field.setValue(ipConfig.getIpv4Address());
				} else {
					ipConfig.getIpv4AddressDisplay().ifPresent(ipv4Field::setValue);
				}
				
				if (ipConfig.getIpv4PrefixLength() != null) {
					prefixField.setValue(ipConfig.getIpv4PrefixLength());
				}
			}
		}
	}

	private void attemptSave() {
		final boolean useDhcp = Boolean.TRUE.equals(dhcpCheckbox.getValue());
		
		if (useDhcp) {
			// DHCP mode - no IP validation needed
			final CDTONetworkInterfaceIpUpdate update =
					new CDTONetworkInterfaceIpUpdate(targetInterface.getName(), null, null, null, true);
			onSave.accept(update);
			close();
			return;
		}
		
		// Manual mode - validate all fields
		final String ipValue = ipv4Field.getValue() != null ? ipv4Field.getValue().trim() : "";
		if (ipValue.isBlank()) {
			CNotificationService.showWarning("IPv4 address is required in manual mode");
			return;
		}
		if (!isValidIpAddress(ipValue)) {
			CNotificationService.showWarning("Invalid IPv4 format: " + ipValue);
			return;
		}
		final Integer prefix = prefixField.getValue();
		if ((prefix == null) || (prefix < 1) || (prefix > 32)) {
			CNotificationService.showWarning("Prefix length must be between 1 and 32");
			return;
		}
		final CDTONetworkInterfaceIpUpdate update =
				new CDTONetworkInterfaceIpUpdate(targetInterface.getName(), ipValue, prefix, null, false);
		onSave.accept(update);
		close();
	}
}
