package tech.derbent.bab.dashboard.dashboardinterfaces.view;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.bab.dashboard.dashboardinterfaces.service.CInterfaceDataCalimeroClient;
import tech.derbent.bab.dashboard.dashboardproject_bab.service.CAbstractCalimeroClient;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.uiobjects.view.CComponentBabBase;
import tech.derbent.base.session.service.ISessionService;

/** CComponentInterfaceBase - Base class for dashboard interface components.
 * <p>
 * Provides shared functionality for BAB interface components:
 * <ul>
 * <li>Common Calimero client setup (CInterfaceDataCalimeroClient)</li>
 * <li>Shared error handling for interface data loading</li>
 * <li>Standard server availability checking</li>
 * <li>Consistent logging patterns</li>
 * </ul>
 * <p>
 * Subclasses implement specific interface type displays:
 * <ul>
 * <li>CComponentUsbInterfaces - USB device management</li>
 * <li>CComponentSerialInterfaces - Serial port configuration</li>
 * <li>CComponentInterfaceSummary - Complete interface overview</li>
 * </ul>
 * <p>
 * Pattern:
 *
 * <pre>
 * public class CComponentUsbInterfaces extends CComponentInterfaceBase {
 *
 * 	&#64;Override
 * 	protected void initializeComponents() {
 * 		configureComponent();
 * 		add(createHeader());
 * 		add(createStandardToolbar());
 * 		createGrid();
 * 		refreshComponent();
 * 	}
 *
 * 	private void refreshComponent() {
 * 		final Optional&lt;CInterfaceDataCalimeroClient&gt; clientOpt = getInterfaceDataClient();
 * 		if (clientOpt.isEmpty()) {
 * 			showCalimeroUnavailableWarning("Calimero service not available - USB data cannot be loaded");
 * 			return;
 * 		}
 * 		// Load and display data...
 * 	}
 * }
 * </pre>
 */
public abstract class CComponentInterfaceBase extends CComponentBabBase {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentInterfaceBase.class);
	private static final long serialVersionUID = 1L;
	// Shared Calimero client for interface data
	private CInterfaceDataCalimeroClient interfaceDataClient;

	protected CComponentInterfaceBase(final ISessionService sessionService) {
		super(sessionService);
	}

	@Override
	protected CAbstractCalimeroClient createCalimeroClient(final CClientProject clientProject) {
		// Create shared interface data client
		interfaceDataClient = new CInterfaceDataCalimeroClient(clientProject);
		LOGGER.debug("Created interface data Calimero client for {}", getClass().getSimpleName());
		return interfaceDataClient;
	}

	/** Get typed interface data client.
	 * <p>
	 * Convenience method for subclasses to access the interface-specific client without casting. Returns empty if Calimero is not available.
	 * @return Optional containing CInterfaceDataCalimeroClient or empty if unavailable */
	protected Optional<CInterfaceDataCalimeroClient> getInterfaceDataClient() {
		final Optional<CAbstractCalimeroClient> clientOpt = getCalimeroClient();
		if (clientOpt.isPresent() && clientOpt.get() instanceof CInterfaceDataCalimeroClient) {
			return Optional.of((CInterfaceDataCalimeroClient) clientOpt.get());
		}
		return Optional.empty();
	}

	/** Check if interface data is available from Calimero.
	 * <p>
	 * Utility method for subclasses to check connectivity before loading data. Useful for graceful degradation when server is unavailable.
	 * @return true if interface data can be loaded, false otherwise */
	protected boolean isInterfaceDataAvailable() {
		final Optional<CInterfaceDataCalimeroClient> clientOpt = getInterfaceDataClient();
		if (clientOpt.isEmpty()) {
			return false;
		}
		try {
			return clientOpt.get().isServerAvailable();
		} catch (final Exception e) {
			LOGGER.debug("Interface data availability check failed: {}", e.getMessage());
			return false;
		}
	}

	/** Show standard Calimero unavailable warning for interface components.
	 * <p>
	 * Displays a consistent warning message when Calimero service is not available. All interface components should use this method for consistent
	 * UX. */
	protected void showInterfaceDataUnavailableWarning() {
		showCalimeroUnavailableWarning("Calimero service not available - Interface data cannot be loaded. "
				+ "Please check that the BAB Gateway is running and accessible.");
	}

	/** Handle empty/missing interface JSON data.
	 * <p>
	 * Common error handling when interface JSON is not available. Shows warning and clears component display.
	 * @param componentName the component name for logging (e.g., "USB Devices", "Serial Ports") */
	protected void handleMissingInterfaceData(final String componentName) {
		LOGGER.warn("⚠️ No interface data available for {}", componentName);
		showInterfaceDataUnavailableWarning();
	}
}
