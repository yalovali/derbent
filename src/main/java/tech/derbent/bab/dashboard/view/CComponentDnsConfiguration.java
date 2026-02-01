package tech.derbent.bab.dashboard.view;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CH3;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.dashboard.service.CNetworkRoutingCalimeroClient;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.project.domain.CProject_Bab;
import tech.derbent.bab.uiobjects.view.CComponentBabBase;
import tech.derbent.base.session.service.ISessionService;

/**
 * CComponentDnsConfiguration - Component for displaying DNS server configuration from Calimero server.
 * <p>
 * Displays DNS configuration for BAB Gateway projects with real-time data from Calimero HTTP API.
 * Shows:
 * <ul>
 *   <li>List of configured DNS servers</li>
 *   <li>DNS server status (primary, secondary, etc.)</li>
 *   <li>Refresh button for manual updates</li>
 * </ul>
 * <p>
 * Calimero API: POST /api/request with type="network", operation="getDns"
 * <p>
 * Usage:
 * <pre>
 * CComponentDnsConfiguration component = new CComponentDnsConfiguration(sessionService);
 * layout.add(component);
 * </pre>
 */
public class CComponentDnsConfiguration extends CComponentBabBase {
	
	public static final String ID_ROOT = "custom-dns-component";
	public static final String ID_HEADER = "custom-dns-header";
	public static final String ID_REFRESH_BUTTON = "custom-dns-refresh-button";
	public static final String ID_DNS_LIST = "custom-dns-list";
	public static final String ID_TOOLBAR = "custom-dns-toolbar";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentDnsConfiguration.class);
	private static final long serialVersionUID = 1L;
	
	private CButton buttonRefresh;
	private CButton buttonFlushCache;
	private CNetworkRoutingCalimeroClient routingClient;
	private final ISessionService sessionService;
	private CVerticalLayout dnsListLayout;
	
	/**
	 * Constructor for DNS configuration component.
	 * @param sessionService the session service
	 */
	public CComponentDnsConfiguration(final ISessionService sessionService) {
		this.sessionService = sessionService;
		initializeComponents();
	}
	
	/** Factory method for refresh button. */
	protected CButton create_buttonRefresh() {
		final CButton button = new CButton("Refresh", VaadinIcon.REFRESH.create());
		button.setId(ID_REFRESH_BUTTON);
		button.addThemeVariants(ButtonVariant.LUMO_SMALL);
		button.addClickListener(e -> on_buttonRefresh_clicked());
		return button;
	}
	
	/** Factory method for flush cache button. */
	protected CButton create_buttonFlushCache() {
		final CButton button = new CButton("Flush Cache", VaadinIcon.TRASH.create());
		button.setId("custom-dns-flush-button");
		button.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
		button.addClickListener(e -> on_buttonFlushCache_clicked());
		return button;
	}
	
	/** Create DNS servers list layout. */
	private void createDnsList() {
		dnsListLayout = new CVerticalLayout();
		dnsListLayout.setId(ID_DNS_LIST);
		dnsListLayout.setPadding(false);
		dnsListLayout.setSpacing(false);
		dnsListLayout.getStyle()
				.set("gap", "12px")
				.set("padding", "16px")
				.set("border", "1px solid var(--lumo-contrast-10pct)")
				.set("border-radius", "8px")
				.set("background", "var(--lumo-base-color)");
		
		add(dnsListLayout);
	}
	
	/** Create header component. */
	private void createHeader() {
		final CH3 header = new CH3("DNS Configuration");
		header.setHeight(null);
		header.setId(ID_HEADER);
		header.getStyle().set("margin", "0");
		add(header);
	}
	
	/** Create toolbar with action buttons. */
	private void createToolbar() {
		final CHorizontalLayout layoutToolbar = new CHorizontalLayout();
		layoutToolbar.setId(ID_TOOLBAR);
		layoutToolbar.setSpacing(true);
		layoutToolbar.getStyle().set("gap", "8px");
		
		buttonRefresh = create_buttonRefresh();
		buttonFlushCache = create_buttonFlushCache();
		layoutToolbar.add(buttonRefresh, buttonFlushCache);
		
		add(layoutToolbar);
	}
	
	@Override
	protected void initializeComponents() {
		setId(ID_ROOT);
		configureComponent();
		createHeader();
		createToolbar();
		createDnsList();
		loadDnsServers();
	}
	
	/** Load DNS servers from Calimero server. */
	private void loadDnsServers() {
		try {
			LOGGER.debug("Loading DNS servers from Calimero server");
			buttonRefresh.setEnabled(false);
			buttonFlushCache.setEnabled(false);
			
			final Optional<CClientProject> clientOptional = resolveClientProject();
			if (clientOptional.isEmpty()) {
				updateDnsDisplay(Collections.emptyList());
				return;
			}
			
			routingClient = new CNetworkRoutingCalimeroClient(clientOptional.get());
			final List<String> dnsServers = routingClient.fetchDnsServers();
			
			updateDnsDisplay(dnsServers);
			LOGGER.info("Loaded {} DNS servers", dnsServers.size());
			CNotificationService.showSuccess("Loaded " + dnsServers.size() + " DNS servers");
			
		} catch (final Exception e) {
			LOGGER.error("Failed to load DNS servers: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to load DNS servers", e);
			updateDnsDisplay(Collections.emptyList());
		} finally {
			buttonRefresh.setEnabled(true);
			buttonFlushCache.setEnabled(true);
		}
	}
	
	/** Handle flush cache button click. */
	protected void on_buttonFlushCache_clicked() {
		LOGGER.debug("Flush DNS cache button clicked");
		// TODO: Implement flush cache operation when Calimero API supports it
		CNotificationService.showInfo("DNS cache flush - Feature coming soon");
	}
	
	/** Handle refresh button click. */
	protected void on_buttonRefresh_clicked() {
		LOGGER.debug("Refresh button clicked");
		refreshComponent();
	}
	
	@Override
	protected void refreshComponent() {
		loadDnsServers();
	}
	
	private Optional<CProject_Bab> resolveActiveBabProject() {
		return sessionService.getActiveProject()
				.filter(CProject_Bab.class::isInstance)
				.map(CProject_Bab.class::cast);
	}
	
	private Optional<CClientProject> resolveClientProject() {
		final Optional<CProject_Bab> projectOpt = resolveActiveBabProject();
		if (projectOpt.isEmpty()) {
			return Optional.empty();
		}
		
		final CProject_Bab babProject = projectOpt.get();
		CClientProject httpClient = babProject.getHttpClient();
		
		if (httpClient == null || !httpClient.isConnected()) {
			LOGGER.info("HTTP client not connected - connecting now");
			final var connectionResult = babProject.connectToCalimero();
			if (!connectionResult.isSuccess()) {
				// Graceful degradation - log warning but DON'T show error dialog
				// Connection refused is expected when Calimero server is not running
				LOGGER.warn("⚠️ Calimero connection failed (graceful degradation): {}", connectionResult.getMessage());
				return Optional.empty();
			}
			httpClient = babProject.getHttpClient();
		}
		
		return Optional.ofNullable(httpClient);
	}
	
	/** Update DNS display with server list. */
	private void updateDnsDisplay(final List<String> dnsServers) {
		// Clear existing DNS entries
		dnsListLayout.removeAll();
		
		if (dnsServers.isEmpty()) {
			final CSpan noDnsSpan = new CSpan("No DNS servers configured");
			noDnsSpan.getStyle().set("color", "var(--lumo-contrast-50pct)").set("font-style", "italic");
			dnsListLayout.add(noDnsSpan);
		} else {
			int index = 1;
			for (final String dnsServer : dnsServers) {
				final CHorizontalLayout dnsRow = new CHorizontalLayout();
				dnsRow.setSpacing(true);
				dnsRow.getStyle().set("gap", "12px").set("align-items", "center");
				
				// Server number badge
				final CSpan badge = new CSpan(String.valueOf(index));
				badge.getStyle()
						.set("background", index == 1 ? "var(--lumo-primary-color)" : "var(--lumo-contrast-20pct)")
						.set("color", index == 1 ? "white" : "var(--lumo-contrast-90pct)")
						.set("padding", "4px 8px")
						.set("border-radius", "4px")
						.set("font-weight", "bold")
						.set("font-size", "0.85rem")
						.set("min-width", "30px")
						.set("text-align", "center");
				
				// DNS address
				final CSpan dnsAddress = new CSpan(dnsServer);
				dnsAddress.getStyle()
						.set("font-family", "monospace")
						.set("font-size", "1rem")
						.set("color", "var(--lumo-contrast-90pct)");
				
				// Status indicator
				final CSpan statusIcon = new CSpan("●");
				statusIcon.getStyle()
						.set("color", "var(--lumo-success-color)")
						.set("font-size", "1.2rem");
				
				dnsRow.add(badge, statusIcon, dnsAddress);
				dnsListLayout.add(dnsRow);
				index++;
			}
		}
	}
}
