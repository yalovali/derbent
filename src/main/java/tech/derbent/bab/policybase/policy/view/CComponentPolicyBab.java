package tech.derbent.bab.policybase.policy.view;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.interfaces.IPageServiceAutoRegistrable;
import tech.derbent.bab.dashboard.dashboardpolicy.domain.CBabPolicyRule;
import tech.derbent.bab.dashboard.dashboardpolicy.service.CBabPolicyRuleService;
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;
import tech.derbent.bab.policybase.policy.service.CBabPolicyService;
import tech.derbent.base.session.service.ISessionService;

/** CComponentPolicyDashboard - Main BAB dashboard widget for policy management. Complete dashboard widget containing: - Left panel: Virtual network
 * node interface list - Right panel: Tabbed workspace with policy rules - Toolbar: Apply policy, export, refresh actions - Split pane layout for
 * optimal workflow Displays data from policy rules and virtual nodes (FileInput, HttpServer, Vehicle). */
public class CComponentPolicyBab extends CVerticalLayout implements IPageServiceAutoRegistrable {

	public static final String ID_APPLY_BUTTON = "custom-policy-apply-button";
	public static final String ID_EXPORT_BUTTON = "custom-policy-export-button";
	public static final String ID_NODE_GRID = "custom-policy-node-grid";
	public static final String ID_REFRESH_BUTTON = "custom-policy-refresh-button";
	public static final String ID_ROOT = "custom-policy-dashboard-widget";
	public static final String ID_RULE_GRID = "custom-policy-rule-grid";
	public static final String ID_SPLIT_LAYOUT = "custom-policy-split-layout";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentPolicyBab.class);
	private static final long serialVersionUID = 1L;
	// UI Components
	private CButton buttonApply;
	private CButton buttonExport;
	private CButton buttonRefresh;
	private CGrid<CBabNodeEntity<?>> gridNodes;
	private CGrid<CBabPolicyRule> gridRules;
	private transient CBabPolicyRuleService ruleService;
	// Dependencies
	private final ISessionService sessionService;
	private SplitLayout splitLayout;
	private TabSheet workArea;

	/** Constructor for policy dashboard widget.
	 * @param sessionService the session service */
	public CComponentPolicyBab(final ISessionService sessionService) {
		this.sessionService = sessionService;
		initializeComponents();
	}

	private void configureComponent() {
		setId(ID_ROOT);
		setSpacing(false);
		setPadding(false);
		getStyle().set("gap", "12px");
		setSizeFull();
	}

	private CVerticalLayout createConfigurationTab() {
		final CVerticalLayout tab = new CVerticalLayout();
		tab.setSpacing(false);
		tab.setPadding(false);
		tab.getStyle().set("gap", "8px");
		// Placeholder for future configuration UI
		final Button placeholderButton = new Button("Policy Configuration");
		placeholderButton.setEnabled(false);
		tab.add(placeholderButton);
		return tab;
	}

	private CVerticalLayout createNodeListPanel() {
		final CVerticalLayout panel = new CVerticalLayout();
		panel.setSpacing(false);
		panel.setPadding(false);
		panel.getStyle().set("gap", "8px");
		// Node list grid
		gridNodes = new CGrid<>(CBabNodeEntity.class);
		gridNodes.setId(ID_NODE_GRID);
		gridNodes.removeAllColumns();
		// Configure columns
		gridNodes.addColumn(CBabNodeEntity::getName).setHeader("Node Name").setFlexGrow(1);
		gridNodes.addColumn(CBabNodeEntity::getNodeType).setHeader("Type").setWidth("100px").setFlexGrow(0);
		panel.add(gridNodes);
		return panel;
	}

	private CVerticalLayout createNodeSetupTab() {
		final CVerticalLayout tab = new CVerticalLayout();
		tab.setSpacing(false);
		tab.setPadding(false);
		tab.getStyle().set("gap", "8px");
		// Placeholder for future configuration UI
		final Button placeholderButton = new Button("Node Setup");
		placeholderButton.setEnabled(false);
		tab.add(placeholderButton);
		return tab;
	}

	private CVerticalLayout createPolicyRulesTab() {
		final CVerticalLayout tab = new CVerticalLayout();
		tab.setSpacing(false);
		tab.setPadding(false);
		tab.getStyle().set("gap", "8px");
		// Policy rules grid
		gridRules = new CGrid<>(CBabPolicyRule.class);
		gridRules.setId(ID_RULE_GRID);
		gridRules.removeAllColumns();
		// Configure columns
		gridRules.addColumn(CBabPolicyRule::getName).setHeader("Rule Name").setFlexGrow(1);
		gridRules.addColumn(CBabPolicyRule::getSourceNodeName).setHeader("Source Node").setWidth("150px").setFlexGrow(0);
		gridRules.addColumn(CBabPolicyRule::getDestinationNodeName).setHeader("Destination Node").setWidth("150px").setFlexGrow(0);
		tab.add(gridRules);
		return tab;
	}

	private void createSplitLayout() {
		splitLayout = new SplitLayout();
		splitLayout.setSizeFull();
		splitLayout.setId(ID_SPLIT_LAYOUT);
		splitLayout.setOrientation(SplitLayout.Orientation.HORIZONTAL);
		splitLayout.setSplitterPosition(30); // 30% for node list
		// Left panel: Virtual node list
		final CVerticalLayout leftPanel = createNodeListPanel();
		splitLayout.addToPrimary(leftPanel);
		// Right panel: Tabbed work area
		final CVerticalLayout rightPanel = createWorkAreaPanel();
		splitLayout.addToSecondary(rightPanel);
		add(splitLayout);
	}

	private void createToolbar() {
		buttonRefresh = new CButton("Refresh", VaadinIcon.REFRESH.create());
		buttonRefresh.setId(ID_REFRESH_BUTTON);
		buttonRefresh.addThemeVariants(ButtonVariant.LUMO_SMALL);
		buttonRefresh.addClickListener(e -> on_buttonRefresh_clicked());
		buttonApply = new CButton("Apply Policy", VaadinIcon.PLAY.create());
		buttonApply.setId(ID_APPLY_BUTTON);
		buttonApply.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
		buttonApply.setEnabled(false);
		buttonApply.addClickListener(e -> on_buttonApply_clicked());
		buttonExport = new CButton("Export", VaadinIcon.DOWNLOAD.create());
		buttonExport.setId(ID_EXPORT_BUTTON);
		buttonExport.addThemeVariants(ButtonVariant.LUMO_SMALL);
		buttonExport.setEnabled(false);
		buttonExport.addClickListener(e -> on_buttonExport_clicked());
		final CHorizontalLayout toolbar = new CHorizontalLayout(buttonRefresh, buttonApply, buttonExport);
		add(toolbar);
	}

	private CVerticalLayout createWorkAreaPanel() {
		final CVerticalLayout panel = new CVerticalLayout();
		panel.setSizeFull();
		panel.setSpacing(false);
		panel.setPadding(false);
		panel.getStyle().set("gap", "8px");
		// Create tabbed work area
		workArea = new TabSheet();
		workArea.setSizeFull();
		// Tab 1: Policy Rules
		final CVerticalLayout rulesTab = createPolicyRulesTab();
		workArea.add("Policy Rules", rulesTab);
		// Tab 2: Policy Configuration (future)
		final CVerticalLayout configTab = createConfigurationTab();
		workArea.add("Configuration", configTab);
		panel.add(workArea);
		// note edit setup page
		final CVerticalLayout setupTab = createNodeSetupTab();
		workArea.add("Node Setup", setupTab);
		panel.add(workArea);
		return panel;
	}

	protected void initializeComponents() {
		CSpringContext.getBean(CBabPolicyService.class);
		ruleService = CSpringContext.getBean(CBabPolicyRuleService.class);
		// STEP 1: Configure root component
		configureComponent();
		// STEP 2: Create toolbar
		createToolbar();
		// STEP 3: Create split layout
		createSplitLayout();
		// STEP 4: Load initial data
		refreshComponent();
	}

	private void on_buttonApply_clicked() {
		try {
			// Future: Apply policy to Calimero gateway
			CNotificationService.showInfo("Policy apply functionality - coming soon");
			LOGGER.info("Policy apply requested");
		} catch (final Exception e) {
			LOGGER.error("Error applying policy", e);
			CNotificationService.showException("Failed to apply policy", e);
		}
	}

	private void on_buttonExport_clicked() {
		try {
			// Future: Export policy as JSON
			CNotificationService.showInfo("Policy export functionality - coming soon");
			LOGGER.info("Policy export requested");
		} catch (final Exception e) {
			LOGGER.error("Error exporting policy", e);
			CNotificationService.showException("Failed to export policy", e);
		}
	}

	private void on_buttonRefresh_clicked() {
		refreshComponent();
		CNotificationService.showSuccess("Dashboard refreshed");
	}

	protected void refreshComponent() {
		try {
			// Get active project
			final Optional<CProject<?>> projectOpt = sessionService.getActiveProject();
			if (projectOpt.isEmpty()) {
				LOGGER.warn("No active project - cannot load policy dashboard data");
				return;
			}
			final CProject<?> project = projectOpt.get();
			// Load virtual nodes - TODO: Implement when node services are available
			gridNodes.setItems(List.of());
			// Load policy rules
			final List<CBabPolicyRule> rules = ruleService.listByProject(project);
			gridRules.setItems(rules);
			// Enable buttons if data available
			buttonApply.setEnabled(!rules.isEmpty());
			buttonExport.setEnabled(!rules.isEmpty());
			LOGGER.debug("Loaded {} rules for policy dashboard", rules.size());
		} catch (final Exception e) {
			LOGGER.error("Error loading policy dashboard data", e);
			CNotificationService.showException("Failed to load policy dashboard data", e);
		}
	}

	@Override
	public String getComponentName() {
		return "policyBab";
	}
}
