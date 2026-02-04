package tech.derbent.bab.dashboard.dashboardinterfaces.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.bab.dashboard.dashboardproject_bab.service.CAbstractCalimeroClient;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.uiobjects.view.CComponentBabBase;
import tech.derbent.base.session.service.ISessionService;

/** CComponentSerialInterfaces - Component for displaying and configuring Serial interface settings.
 * <p>
 * Displays Serial interfaces for BAB Gateway projects with configuration options. */
public class CComponentSerialInterfaces extends CComponentBabBase {
	public static final String ID_ROOT = "custom-serial-interfaces-component";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentSerialInterfaces.class);
	private static final long serialVersionUID = 1L;

	public CComponentSerialInterfaces(final ISessionService sessionService) {
		super(sessionService);
		initializeComponents();
	}

	@Override
	protected CAbstractCalimeroClient createCalimeroClient(final CClientProject clientProject) {
		return null;
	}

	@Override
	protected String getHeaderText() { return "Serial Interfaces"; }

	@Override
	public ISessionService getSessionService() { return sessionService; }

	@Override
	protected void initializeComponents() {
		setId(ID_ROOT);
		configureComponent();
		add(createHeader());
		add(createStandardToolbar());
		final CSpan placeholder = new CSpan("Serial interface configuration - implementation in progress");
		placeholder.getStyle().set("color", "var(--lumo-secondary-text-color)");
		add(placeholder);
		LOGGER.debug("Initialized Serial interfaces component (placeholder)");
	}

	@Override
	protected void refreshComponent() {
		LOGGER.debug("Refreshing Serial interfaces component");
	}
}