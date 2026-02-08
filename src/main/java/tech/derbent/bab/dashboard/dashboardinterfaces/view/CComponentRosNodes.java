package tech.derbent.bab.dashboard.dashboardinterfaces.view;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.base.session.service.ISessionService;

/** CComponentRosNodes - Component for displaying and configuring ROS node settings.
 * <p>
 * Displays ROS nodes for BAB Gateway projects with configuration options. Shows ROS node information including:
 * <ul>
 * <li>Node names and namespaces</li>
 * <li>Topic publishers and subscribers</li>
 * <li>Service providers and clients</li>
 * <li>Node status and health monitoring</li>
 * </ul>
 * <p>
 * Currently shows sample data structure - will be enhanced with real ROS API integration. */
public class CComponentRosNodes extends CComponentInterfaceBase {

	// Simple data structure for demonstration
	public static class RosNode {

		public String name;
		public String namespace;
		public Integer services;
		public String status;
		public Integer topics;

		public RosNode(String name, String namespace, String status, Integer topics, Integer services) {
			this.name = name;
			this.namespace = namespace;
			this.status = status;
			this.topics = topics;
			this.services = services;
		}
	}

	public static final String ID_GRID = "custom-ros-nodes-grid";
	public static final String ID_MANAGE_BUTTON = "custom-ros-manage-button";
	public static final String ID_REFRESH_BUTTON = "custom-ros-refresh-button";
	public static final String ID_ROOT = "custom-ros-nodes-component";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentRosNodes.class);
	private static final long serialVersionUID = 1L;
	// UI Components
	private CButton buttonManage;
	private CGrid<RosNode> grid;

	public CComponentRosNodes(final ISessionService sessionService) {
		super(sessionService);
		initializeComponents();
	}

	@Override
	protected void addAdditionalToolbarButtons(final CHorizontalLayout toolbarLayout) {
		buttonManage = new CButton("Manage Node", VaadinIcon.TOOLS.create());
		buttonManage.setId(ID_MANAGE_BUTTON);
		buttonManage.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
		buttonManage.setEnabled(false); // Enabled when node selected
		buttonManage.addClickListener(e -> on_buttonManage_clicked());
		toolbarLayout.add(buttonManage);
	}

	private void configureGridColumns() {
		// Node name column
		grid.addColumn(node -> node.name).setHeader("Node Name").setWidth("200px").setFlexGrow(1).setSortable(true).setResizable(true);
		// Namespace column
		grid.addColumn(node -> node.namespace).setHeader("Namespace").setWidth("150px").setFlexGrow(0).setSortable(true).setResizable(true);
		// Topics column
		grid.addColumn(node -> node.topics.toString()).setHeader("Topics").setWidth("80px").setFlexGrow(0).setSortable(true).setResizable(true);
		// Services column
		grid.addColumn(node -> node.services.toString()).setHeader("Services").setWidth("80px").setFlexGrow(0).setSortable(true).setResizable(true);
		// Status column with colored indicator
		grid.addComponentColumn(node -> {
			final CSpan statusSpan = new CSpan(node.status);
			statusSpan.getStyle().set("padding", "4px 8px");
			statusSpan.getStyle().set("border-radius", "12px");
			statusSpan.getStyle().set("font-size", "0.8em");
			statusSpan.getStyle().set("font-weight", "bold");
			if ("Running".equals(node.status)) {
				statusSpan.getStyle().set("background", "var(--lumo-success-color-10pct)");
				statusSpan.getStyle().set("color", "var(--lumo-success-color)");
			} else if ("Stopped".equals(node.status)) {
				statusSpan.getStyle().set("background", "var(--lumo-error-color-10pct)");
				statusSpan.getStyle().set("color", "var(--lumo-error-color)");
			} else {
				statusSpan.getStyle().set("background", "var(--lumo-warning-color-10pct)");
				statusSpan.getStyle().set("color", "var(--lumo-warning-color)");
			}
			return statusSpan;
		}).setHeader("Status").setWidth("100px").setFlexGrow(0).setSortable(false).setResizable(true);
	}

	private void createGrid() {
		grid = new CGrid<>(RosNode.class);
		grid.setId(ID_GRID);
		// Configure columns for ROS node display
		configureGridColumns();
		// Selection listener for Manage button
		grid.asSingleSelect().addValueChangeListener(e -> buttonManage.setEnabled(e.getValue() != null));
		add(grid);
	}

	@Override
	protected String getHeaderText() { return "ROS Nodes"; }

	@Override
	protected String getRefreshButtonId() { return ID_REFRESH_BUTTON; }

	@Override
	protected boolean hasRefreshButton() {
		return false; // Page-level refresh used
	}

	@Override
	protected void initializeComponents() {
		setId(ID_ROOT);
		configureComponent();
		add(createHeader());
		add(createStandardToolbar());
		createGrid();
		refreshComponent();
	}

	private void on_buttonManage_clicked() {
		final RosNode selectedNode = grid.asSingleSelect().getValue();
		if (selectedNode != null) {
			CNotificationService.showInfo("Node management for " + selectedNode.name + " - Feature coming soon");
			// TODO: Open ROS node management dialog
		}
	}

	@Override
	protected void refreshComponent() {
		LOGGER.debug("🔄 Refreshing ROS nodes component");
		try {
			// Sample data - will be replaced with real ROS API integration
			final List<RosNode> nodes = new ArrayList<>();
			nodes.add(new RosNode("camera_node", "/sensors", "Running", 3, 1));
			nodes.add(new RosNode("navigation_node", "/nav", "Running", 8, 4));
			nodes.add(new RosNode("lidar_driver", "/sensors", "Running", 2, 0));
			nodes.add(new RosNode("planner_node", "/planning", "Stopped", 0, 0));
			grid.setItems(nodes);
			final long runningNodes = nodes.stream().filter(node -> "Running".equals(node.status)).count();
			final long totalTopics = nodes.stream().mapToLong(node -> node.topics).sum();
			updateSummary("%d nodes (%d running, %d topics)".formatted(nodes.size(), runningNodes, totalTopics));
			LOGGER.debug("✅ ROS nodes component refreshed: {} nodes ({} running, {} topics)", nodes.size(), runningNodes, totalTopics);
		} catch (final Exception e) {
			LOGGER.error("Error loading ROS node data", e);
			CNotificationService.showException("Failed to load ROS nodes", e);
			grid.setItems();
			updateSummary(null);
		}
	}
}
