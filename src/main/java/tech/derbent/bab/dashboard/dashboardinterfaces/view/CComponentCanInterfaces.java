package tech.derbent.bab.dashboard.dashboardinterfaces.view;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.dashboard.dashboardproject_bab.service.CAbstractCalimeroClient;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.node.domain.CBabNodeCAN;
import tech.derbent.bab.node.service.CBabNodeCANService;
import tech.derbent.bab.project.domain.CProject_Bab;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.projects.domain.CProject;

/** CComponentCanInterfaces - Component for displaying and configuring CAN interface settings.
 * <p>
 * Displays CAN bus interfaces for BAB Gateway projects with configuration options. Shows CAN interface information including:
 * <ul>
 * <li>Interface name and description</li>
 * <li>CAN bus speed (bitrate)</li>
 * <li>Interface status (enabled/disabled)</li>
 * <li>Port number assignment</li>
 * <li>Interface configuration actions</li>
 * </ul>
 * <p>
 * Uses CBabNodeCANService to fetch and manage CAN interface data. */
public class CComponentCanInterfaces extends CComponentInterfaceBase {

	public static final String ID_ADD_BUTTON = "custom-can-interfaces-add-button";
	public static final String ID_EDIT_BUTTON = "custom-can-interfaces-edit-button";
	public static final String ID_GRID = "custom-can-interfaces-grid";
	public static final String ID_REFRESH_BUTTON = "custom-can-interfaces-refresh-button";
	public static final String ID_ROOT = "custom-can-interfaces-component";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentCanInterfaces.class);
	private static final long serialVersionUID = 1L;
	// UI Components (buttonRefresh and buttonEdit inherited from CComponentBabBase)
	private CButton buttonAdd;
	// Services
	private CBabNodeCANService canNodeService;
	private CGrid<CBabNodeCAN> grid;

	/** Constructor for CAN interfaces component.
	 * @param sessionService the session service */
	public CComponentCanInterfaces(final ISessionService sessionService) {
		super(sessionService);
		initializeComponents();
	}

	@Override
	protected void addAdditionalToolbarButtons(final CHorizontalLayout toolbarLayout) {
		buttonAdd = new CButton("Add Interface", VaadinIcon.PLUS.create());
		buttonAdd.setId(ID_ADD_BUTTON);
		buttonAdd.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
		buttonAdd.addClickListener(e -> on_buttonAdd_clicked());
		toolbarLayout.add(buttonAdd);
	}

	@Override
	protected void configureComponent() {
		super.configureComponent();
		createGrid();
	}

	private void configureGridColumns() {
		// Name column
		grid.addColumn(CBabNodeCAN::getName).setHeader("Interface Name").setWidth("200px").setFlexGrow(0).setSortable(true).setResizable(true);
		// Node Type column
		grid.addColumn(CBabNodeCAN::getNodeType).setHeader("Type").setWidth("100px").setFlexGrow(0).setSortable(true).setResizable(true);
		// Port Number column
		grid.addColumn(node -> node.getPortNumber() != null ? node.getPortNumber().toString() : "").setHeader("Port").setWidth("80px").setFlexGrow(0)
				.setSortable(true).setResizable(true);
		// This would need to be added to CBabNodeCAN entity or accessed differently
		// Placeholder
		// Bitrate column (CAN-specific)
		grid.addColumn(node -> "500 kbps").setHeader("Bitrate").setWidth("100px").setFlexGrow(0).setSortable(false).setResizable(true);
		// Status column
		grid.addComponentColumn(node -> {
			final CSpan statusSpan = new CSpan(Boolean.TRUE.equals(node.getEnabled()) ? "Enabled" : "Disabled");
			statusSpan.getStyle().set("padding", "4px 8px");
			statusSpan.getStyle().set("border-radius", "12px");
			statusSpan.getStyle().set("font-size", "0.8em");
			statusSpan.getStyle().set("font-weight", "bold");
			if (Boolean.TRUE.equals(node.getEnabled())) {
				statusSpan.getStyle().set("background", "var(--lumo-success-color-10pct)");
				statusSpan.getStyle().set("color", "var(--lumo-success-color)");
			} else {
				statusSpan.getStyle().set("background", "var(--lumo-error-color-10pct)");
				statusSpan.getStyle().set("color", "var(--lumo-error-color)");
			}
			return statusSpan;
		}).setHeader("Status").setWidth("100px").setFlexGrow(0).setSortable(true).setResizable(true);
		// Description column
		grid.addColumn(CBabNodeCAN::getDescription).setHeader("Description").setFlexGrow(1).setSortable(false).setResizable(true);
	}

	@Override
	protected CAbstractCalimeroClient createCalimeroClient(CClientProject clientProject) {
		return null; // CAN interfaces don't need direct Calimero client
	}

	private void createGrid() {
		grid = new CGrid<>(CBabNodeCAN.class);
		grid.setId(ID_GRID);
		// Configure columns for CAN interface display
		configureGridColumns();
		// Selection listener for Edit button
		grid.asSingleSelect().addValueChangeListener(e -> buttonEdit.setEnabled(e.getValue() != null));
		add(grid);
	}

	@Override
	protected String getEditButtonId() { return ID_EDIT_BUTTON; }

	@Override
	protected String getHeaderText() { return "CAN Interfaces"; }

	@Override
	protected String getID_ROOT() { // TODO Auto-generated method stub
		return ID_ROOT;
	}

	@Override
	protected String getRefreshButtonId() { return ID_REFRESH_BUTTON; }

	@Override
	protected boolean hasEditButton() {
		return true;
	}

	@Override
	protected boolean hasRefreshButton() {
		return false; // Page-level refresh used
	}

	private void on_buttonAdd_clicked() {
		// TODO: Open dialog to add new CAN interface
		CNotificationService.showInfo("Add CAN interface - functionality to be implemented");
	}

	@Override
	protected void on_buttonEdit_clicked() {
		final CBabNodeCAN selectedNode = grid.asSingleSelect().getValue();
		if (selectedNode != null) {
			// TODO: Open dialog to edit CAN interface settings
			CNotificationService.showInfo("Edit CAN interface '" + selectedNode.getName() + "' - functionality to be implemented");
		}
	}

	@Override
	protected void on_buttonRefresh_clicked() {
		refreshComponent();
		CNotificationService.showSuccess("CAN interfaces refreshed");
	}

	@Override
	protected void refreshComponent() {
		LOGGER.debug("🔄 Refreshing CAN interfaces component");
		try {
			// Get active BAB project
			final Optional<CProject<?>> projectOpt = sessionService.getActiveProject();
			if (projectOpt.isEmpty()) {
				LOGGER.warn("No active project - cannot load CAN interfaces");
				grid.setItems(Collections.emptyList());
				return;
			}
			// Cast to BAB project
			if (!(projectOpt.get() instanceof CProject_Bab)) {
				LOGGER.warn("Active project is not a BAB project");
				grid.setItems(Collections.emptyList());
				return;
			}
			// Fetch CAN interfaces from service
			List<CBabNodeCAN> canNodes = Collections.emptyList();
			if (canNodeService != null) {
				try {
					// canNodes = canNodeService.findByProject(babProject); // This method would need to be implemented
					// For now, create placeholder data
					canNodes = Collections.emptyList();
				} catch (final Exception e) {
					LOGGER.warn("Error fetching CAN nodes from service: {}", e.getMessage());
				}
			}
			grid.setItems(canNodes);
			LOGGER.debug("✅ CAN interfaces component refreshed: {} nodes", canNodes.size());
		} catch (final Exception e) {
			LOGGER.error("Error loading CAN interface data", e);
			CNotificationService.showException("Failed to load CAN interfaces", e);
			grid.setItems(Collections.emptyList());
		}
	}
}
