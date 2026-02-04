package tech.derbent.bab.dashboard.dashboardproject_bab.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.dashboard.dashboardproject_bab.dto.CDTODnsConfigurationUpdate;
import tech.derbent.bab.dashboard.dashboardproject_bab.dto.CDTODnsServer;
import tech.derbent.bab.dashboard.dashboardproject_bab.service.CAbstractCalimeroClient;
import tech.derbent.bab.dashboard.dashboardproject_bab.service.CDnsConfigurationCalimeroClient;
import tech.derbent.bab.dashboard.dashboardproject_bab.view.dialog.CDialogEditDnsConfiguration;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.uiobjects.view.CComponentBabBase;
import tech.derbent.base.session.service.ISessionService;

/** CComponentDnsConfiguration - Component for displaying DNS server configuration from Calimero server.
 * <p>
 * Displays DNS configuration for BAB Gateway projects with real-time data from Calimero HTTP API. Shows:
 * <ul>
 * <li>List of configured DNS servers</li>
 * <li>DNS server status (primary, secondary, etc.)</li>
 * <li>Refresh button for manual updates</li>
 * </ul>
 * <p>
 * Calimero API: POST /api/request with type="network", operation="getDns"
 * <p>
 * Usage:
 *
 * <pre>
 * CComponentDnsConfiguration component = new CComponentDnsConfiguration(sessionService);
 * layout.add(component);
 * </pre>
 */
public class CComponentDnsConfiguration extends CComponentBabBase {

	public static final String ID_DNS_LIST = "custom-dns-list";
	public static final String ID_EDIT_BUTTON = "custom-dns-edit-button";
	public static final String ID_HEADER = "custom-dns-header";
	public static final String ID_REFRESH_BUTTON = "custom-dns-refresh-button";
	public static final String ID_ROOT = "custom-dns-component";
	public static final String ID_TOOLBAR = "custom-dns-toolbar";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentDnsConfiguration.class);
	private static final long serialVersionUID = 1L;
	// buttonRefresh and buttonEdit inherited from CComponentBabBase
	private CButton buttonFlushCache;
	private final List<String> currentDnsServers = new ArrayList<>();
	private CVerticalLayout dnsListLayout;

	/** Constructor for DNS configuration component.
	 * @param sessionService the session service */
	public CComponentDnsConfiguration(final ISessionService sessionService) {
		super(sessionService);
		initializeComponents();
	}

	/** Apply DNS configuration via Calimero HTTP API.
	 * @param update DNS configuration update object */
	private void applyDnsConfiguration(final CDTODnsConfigurationUpdate update) {
		try {
			LOGGER.info("Applying DNS configuration: {}", update);
			final Optional<CAbstractCalimeroClient> clientOpt = getCalimeroClient();
			if (clientOpt.isEmpty()) {
				showCalimeroUnavailableWarning("Calimero service not available for DNS update");
				return;
			}
			final CDnsConfigurationCalimeroClient dnsClient = (CDnsConfigurationCalimeroClient) clientOpt.get();
			final boolean success = dnsClient.applyDnsConfiguration(update);
			if (success) {
				LOGGER.info("✅ DNS configuration applied successfully");
				CNotificationService.showSuccess("DNS configuration applied: " + update.getServerCount() + " server(s)");
				refreshComponent();
			} else {
				LOGGER.warn("⚠️ Failed to apply DNS configuration");
				CNotificationService.showError("Failed to apply DNS configuration");
			}
		} catch (final Exception e) {
			LOGGER.error("❌ Error applying DNS configuration: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to apply DNS configuration", e);
		}
	}

	/** Factory method for flush cache button. */
	protected CButton create_buttonFlushCache() {
		final CButton button = new CButton("Flush Cache", VaadinIcon.TRASH.create());
		button.setId("custom-dns-flush-button");
		button.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
		button.addClickListener(e -> on_buttonFlushCache_clicked());
		return button;
	}

	@Override
	protected CAbstractCalimeroClient createCalimeroClient(final CClientProject clientProject) {
		return new CDnsConfigurationCalimeroClient(clientProject);
	}

	/** Create custom toolbar with Flush Cache button added. */
	private void createCustomToolbar() {
		final CHorizontalLayout toolbarLayout = createStandardToolbar();
		buttonFlushCache = create_buttonFlushCache();
		toolbarLayout.add(buttonFlushCache);
		add(toolbarLayout);
	}

	/** Create DNS entry display component. */
	private CHorizontalLayout createDnsEntry(final CDTODnsServer dnsServer) {
		final CHorizontalLayout entryLayout = new CHorizontalLayout();
		entryLayout.setSpacing(true);
		entryLayout.setPadding(false);
		entryLayout.getStyle().set("align-items", "center").set("padding", "8px 12px").set("border", "1px solid var(--lumo-contrast-20pct)")
				.set("border-radius", "6px").set("background", "var(--lumo-contrast-5pct)");
		// Priority indicator
		final CSpan prioritySpan = new CSpan(dnsServer.getPriorityDisplay());
		prioritySpan.getStyle().set("font-size", "0.75rem").set("font-weight", "600").set("padding", "2px 8px").set("border-radius", "12px")
				.set("background", dnsServer.getIsPrimary() ? "var(--lumo-primary-color)" : "var(--lumo-contrast-30pct)")
				.set("color", dnsServer.getIsPrimary() ? "white" : "var(--lumo-body-text-color)").set("min-width", "70px")
				.set("text-align", "center");
		// DNS server IP
		final CSpan serverSpan = new CSpan(dnsServer.getServer());
		serverSpan.getStyle().set("font-family", "monospace").set("font-weight", "600").set("color", "var(--lumo-primary-text-color)").set("flex",
				"0 0 140px");
		// Interface info
		final CSpan interfaceSpan = new CSpan(dnsServer.getInterfaceName().isEmpty() ? "Global" : dnsServer.getInterfaceName());
		interfaceSpan.getStyle().set("font-size", "0.875rem").set("color", "var(--lumo-secondary-text-color)").set("flex", "0 0 80px");
		// Domain info
		final CSpan domainSpan = new CSpan(dnsServer.getDomain().isEmpty() ? "-" : dnsServer.getDomain());
		domainSpan.getStyle().set("font-size", "0.875rem").set("color", "var(--lumo-secondary-text-color)").set("flex", "1");
		// Source indicator
		final CSpan sourceSpan = new CSpan(dnsServer.getSource());
		sourceSpan.getStyle().set("font-size", "0.75rem").set("color", "var(--lumo-disabled-text-color)").set("font-style", "italic").set("flex",
				"0 0 80px");
		entryLayout.add(prioritySpan, serverSpan, interfaceSpan, domainSpan, sourceSpan);
		return entryLayout;
	}

	/** Create DNS servers list layout. */
	private void createDnsList() {
		dnsListLayout = new CVerticalLayout();
		dnsListLayout.setId(ID_DNS_LIST);
		dnsListLayout.setPadding(false);
		dnsListLayout.setSpacing(false);
		dnsListLayout.getStyle().set("gap", "12px").set("padding", "16px").set("border", "1px solid var(--lumo-contrast-10pct)")
				.set("border-radius", "8px").set("background", "var(--lumo-base-color)");
		add(dnsListLayout);
	}

	/** Display message when no DNS data is available. */
	private void displayNoDnsData(final String message) {
		final CSpan noDataSpan = new CSpan(message);
		noDataSpan.getStyle().set("text-align", "center").set("color", "var(--lumo-disabled-text-color)").set("font-style", "italic").set("padding",
				"32px");
		dnsListLayout.removeAll();
		dnsListLayout.add(noDataSpan);
	}

	@Override
	protected String getEditButtonId() { return ID_EDIT_BUTTON; }

	@Override
	protected String getHeaderText() { return "DNS Configuration"; }

	@Override
	protected ISessionService getSessionService() { return sessionService; }

	@Override
	protected boolean hasEditButton() {
		return true;
	}

	@Override
	protected void initializeComponents() {
		setId(ID_ROOT);
		configureComponent();
		add(createHeader());
		createCustomToolbar();
		createDnsList();
		loadDnsConfiguration();
	}

	/** Load DNS configuration from Calimero server. */
	private void loadDnsConfiguration() {
		try {
			LOGGER.debug("Loading DNS configuration from Calimero server");
			buttonRefresh.setEnabled(false);
			buttonEdit.setEnabled(false);
			buttonFlushCache.setEnabled(false);
			dnsListLayout.removeAll();
			currentDnsServers.clear();
			final Optional<CAbstractCalimeroClient> clientOpt = getCalimeroClient();
			if (clientOpt.isEmpty()) {
				showCalimeroUnavailableWarning("Calimero service not available");
				displayNoDnsData("Calimero service not available");
				return;
			}
			hideCalimeroUnavailableWarning();
			final CDnsConfigurationCalimeroClient dnsClient = (CDnsConfigurationCalimeroClient) clientOpt.get();
			final List<CDTODnsServer> dnsServers = dnsClient.fetchDnsServers();
			if (dnsServers.isEmpty()) {
				displayNoDnsData("No DNS servers configured");
				return;
			}
			for (final CDTODnsServer dnsServer : dnsServers) {
				if (dnsServer.isValid()) {
					final CHorizontalLayout dnsEntry = createDnsEntry(dnsServer);
					dnsListLayout.add(dnsEntry);
					currentDnsServers.add(dnsServer.getServer());
				}
			}
			LOGGER.info("Loaded {} DNS servers", dnsServers.size());
			CNotificationService.showSuccess("Loaded " + dnsServers.size() + " DNS servers");
		} catch (final Exception e) {
			LOGGER.error("Failed to load DNS configuration: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to load DNS configuration", e);
			displayNoDnsData("Error loading DNS configuration: " + e.getMessage());
		} finally {
			buttonRefresh.setEnabled(true);
			buttonEdit.setEnabled(true);
			buttonFlushCache.setEnabled(true);
		}
	}

	@Override
	protected void on_buttonEdit_clicked() {
		LOGGER.debug("Edit DNS button clicked");
		openDnsEditDialog();
	}

	/** Handle flush cache button click. */
	protected void on_buttonFlushCache_clicked() {
		LOGGER.debug("Flush DNS cache button clicked");
		CNotificationService.showInfo("DNS cache flush - Feature coming soon");
	}

	/** Open DNS configuration edit dialog. */
	private void openDnsEditDialog() {
		try {
			final CDialogEditDnsConfiguration dialog = new CDialogEditDnsConfiguration(currentDnsServers, update -> applyDnsConfiguration(update));
			dialog.open();
		} catch (final Exception e) {
			LOGGER.error("Failed to open DNS edit dialog: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to open DNS editor", e);
		}
	}

	@Override
	protected void refreshComponent() {
		loadDnsConfiguration();
	}
}
