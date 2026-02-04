package tech.derbent.bab.dashboard.dashboardproject_bab.view.dialog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.value.ValueChangeMode;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.dashboard.dashboardproject_bab.dto.CDTODnsConfigurationUpdate;

/** CDialogEditDnsConfiguration - Dialog for editing DNS server configuration.
 * <p>
 * Provides a clean UI for users to configure DNS servers with validation:
 * <ul>
 * <li>DHCP DNS configuration support</li>
 * <li>Manual DNS server list (one IP per line)</li>
 * <li>Automatic IP address validation</li>
 * <li>Real-time validation feedback</li>
 * </ul>
 */
public class CDialogEditDnsConfiguration extends CBabDialogBase {

	public static final String ID_DIALOG = "custom-dns-edit-dialog";
	public static final String ID_DNS_INPUT = "custom-dns-input";
	
	private static final long serialVersionUID = 1L;
	private TextArea dnsInput;
	private Checkbox dhcpCheckbox;
	private final List<String> initialDnsServers;
	private final Consumer<CDTODnsConfigurationUpdate> onSave;

	public CDialogEditDnsConfiguration(final List<String> currentDnsServers, final Consumer<CDTODnsConfigurationUpdate> onSave) {
		initialDnsServers = currentDnsServers != null ? currentDnsServers : new ArrayList<>();
		this.onSave = onSave;
		setId(ID_DIALOG);
		configureBabDialog("600px");
	}

	@Override
	protected void setupContent() {
		applyCustomSpacing();
		
		// DHCP Checkbox
		createDhcpCheckbox();
		
		// DNS Server Header and Input
		mainLayout.add(createHeaderLayout("DNS Servers", true));
		
		dnsInput = createDnsInputField();
		mainLayout.add(dnsInput);
		
		// Hint section
		mainLayout.add(createHintSection("Enter one DNS server IP address per line. First server will be primary. Example: 8.8.8.8"));
		
		// Initial validation
		updateValidationDisplay(dnsInput.getValue());
	}

	private void createDhcpCheckbox() {
		dhcpCheckbox = new Checkbox("Use DHCP DNS (Automatic)");
		dhcpCheckbox.addValueChangeListener(e -> {
			final boolean useDhcp = Boolean.TRUE.equals(e.getValue());
			dnsInput.setEnabled(!useDhcp);
			
			if (useDhcp) {
				dnsInput.setRequiredIndicatorVisible(false);
				setValidationSuccess("✅ DHCP DNS");
			} else {
				dnsInput.setRequiredIndicatorVisible(true);
				updateValidationDisplay(dnsInput.getValue());
			}
		});
		dhcpCheckbox.getStyle().set("margin-bottom", STYLE_GAP);
		mainLayout.add(dhcpCheckbox);
	}

	private TextArea createDnsInputField() {
		final TextArea input = new TextArea();
		input.setId(ID_DNS_INPUT);
		input.setPlaceholder("8.8.8.8\n8.8.4.4\n1.1.1.1");
		input.setWidthFull();
		input.setHeight("200px");
		input.setRequiredIndicatorVisible(true);
		input.setValueChangeMode(ValueChangeMode.EAGER);
		
		// Set initial value
		if (!initialDnsServers.isEmpty()) {
			input.setValue(String.join("\n", initialDnsServers));
		}
		
		// Real-time validation
		input.addValueChangeListener(event -> updateValidationDisplay(event.getValue()));
		
		// Style
		input.getStyle().set("font-family", "monospace").set("font-size", "1rem");
		
		return input;
	}

	private void updateValidationDisplay(final String inputValue) {
		if (inputValue == null || inputValue.trim().isEmpty()) {
			setValidationWarning("⚠️ Required");
			return;
		}
		
		final List<String> lines = Arrays.stream(inputValue.split("\n"))
			.map(String::trim)
			.filter(line -> !line.isEmpty())
			.collect(Collectors.toList());
		
		if (lines.isEmpty()) {
			setValidationWarning("⚠️ Required");
			return;
		}
		
		// Validate each IP address
		final List<String> validIps = new ArrayList<>();
		final List<String> invalidIps = new ArrayList<>();
		for (final String line : lines) {
			if (isValidIpAddress(line)) {
				validIps.add(line);
			} else {
				invalidIps.add(line);
			}
		}
		
		// Display compact validation count
		if (invalidIps.isEmpty()) {
			setValidationSuccess("✅ " + validIps.size() + " valid");
		} else {
			setValidationError("❌ " + invalidIps.size() + " invalid, ✅ " + validIps.size() + " valid");
		}
	}

	@Override
	public String getDialogTitleString() {
		return "DNS Server Configuration";
	}

	@Override
	protected Icon getFormIcon() {
		return VaadinIcon.SERVER.create();
	}

	@Override
	protected String getFormTitleString() {
		return "Edit DNS Servers";
	}

	@Override
	protected void setupButtons() {
		final CButton saveButton = CButton.createSaveButton("Apply", event -> on_save_clicked());
		final CButton cancelButton = CButton.createCancelButton("Cancel", event -> close());
		buttonLayout.add(saveButton, cancelButton);
	}

	private void on_save_clicked() {
		final boolean useDhcp = Boolean.TRUE.equals(dhcpCheckbox.getValue());
		
		if (useDhcp) {
			// DHCP mode - send empty list to indicate DHCP
			final CDTODnsConfigurationUpdate update = new CDTODnsConfigurationUpdate(new ArrayList<>(), true);
			try {
				onSave.accept(update);
				close();
			} catch (final Exception e) {
				CNotificationService.showException("Failed to apply DHCP DNS configuration", e);
			}
			return;
		}
		
		// Manual mode validation
		final String inputValue = dnsInput.getValue();
		if (inputValue == null || inputValue.trim().isEmpty()) {
			CNotificationService.showWarning("DNS configuration cannot be empty in manual mode");
			dnsInput.focus();
			return;
		}
		
		// Parse DNS servers
		final List<String> dnsServers = Arrays.stream(inputValue.split("\n"))
			.map(String::trim)
			.filter(line -> !line.isEmpty())
			.collect(Collectors.toList());
		
		if (dnsServers.isEmpty()) {
			CNotificationService.showWarning("At least one DNS server is required in manual mode");
			dnsInput.focus();
			return;
		}
		
		// Validate all entries
		final List<String> invalidIps = new ArrayList<>();
		for (final String dns : dnsServers) {
			if (!isValidIpAddress(dns)) {
				invalidIps.add(dns);
			}
		}
		
		if (!invalidIps.isEmpty()) {
			CNotificationService.showWarning("Invalid IP address format: " + String.join(", ", invalidIps));
			dnsInput.focus();
			return;
		}
		
		// Create update object
		final CDTODnsConfigurationUpdate update = new CDTODnsConfigurationUpdate(dnsServers, false);
		
		// Notify caller
		try {
			onSave.accept(update);
			close();
		} catch (final Exception e) {
			CNotificationService.showException("Failed to apply DNS configuration", e);
		}
	}
}
