package tech.derbent.bab.dashboard.view.dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.dashboard.dto.CRouteConfigurationUpdate;
import tech.derbent.bab.dashboard.dto.CRouteEntry;

/**
 * CDialogEditRouteConfiguration - Dialog for editing network route configuration.
 * <p>
 * Provides a clean UI for users to configure network routes with validation:
 * <ul>
 * <li>Default gateway field</li>
 * <li>Editable grid for static routes (network/netmask/gateway)</li>
 * <li>Add/Remove route functionality</li>
 * <li>IP address and netmask validation</li>
 * </ul>
 */
public class CDialogEditRouteConfiguration extends CBabDialogBase {
	
	public static final String ID_DIALOG = "custom-route-edit-dialog";
	public static final String ID_DEFAULT_GATEWAY = "custom-default-gateway-input";
	public static final String ID_ROUTES_GRID = "custom-routes-grid";
	public static final String ID_ADD_ROUTE_BUTTON = "custom-add-route-button";
	
	private static final long serialVersionUID = 1L;
	
	private TextField defaultGatewayField;
	private Grid<CRouteEntry> routesGrid;
	private final List<CRouteEntry> staticRoutes;
	private final String initialDefaultGateway;
	private final Consumer<CRouteConfigurationUpdate> onSave;
	
	public CDialogEditRouteConfiguration(final String defaultGateway, final List<CRouteEntry> currentRoutes,
			final Consumer<CRouteConfigurationUpdate> onSave) {
		initialDefaultGateway = defaultGateway != null ? defaultGateway : "";
		staticRoutes = currentRoutes != null ? new ArrayList<>(currentRoutes) : new ArrayList<>();
		this.onSave = onSave;
		
		setId(ID_DIALOG);
		configureBabDialog("700px");
	}
	
	@Override
	protected void setupContent() {
		applyCustomSpacing();
		
		// Default gateway with header
		mainLayout.add(createHeaderLayout("Default Gateway", true));
		
		defaultGatewayField = createDefaultGatewayField();
		mainLayout.add(defaultGatewayField);
		
		// Static routes grid with header and add button
		mainLayout.add(createRoutesHeaderLayout());
		
		routesGrid = createRoutesGrid();
		mainLayout.add(routesGrid);
		
		// Hint section
		mainLayout.add(createHintSection(
			"Configure default gateway and static routes. " +
			"Network: IP address (e.g., 192.168.2.0). " +
			"Netmask: CIDR notation (e.g., 24) or full mask (e.g., 255.255.255.0). " +
			"Gateway: Next hop IP address. " +
			"Click Edit to modify a route, or click + to add new routes."));
		
		// Set initial value and update validation
		defaultGatewayField.setValue(initialDefaultGateway);
		updateValidationDisplay();
	}
	
	private TextField createDefaultGatewayField() {
		final TextField field = new TextField();
		field.setId(ID_DEFAULT_GATEWAY);
		field.setPlaceholder("192.168.1.1");
		field.setWidthFull();
		field.setValueChangeMode(ValueChangeMode.EAGER);
		field.addValueChangeListener(event -> updateValidationDisplay());
		return field;
	}
	
	private CHorizontalLayout createRoutesHeaderLayout() {
		final CHorizontalLayout header = new CHorizontalLayout();
		header.setWidthFull();
		header.setJustifyContentMode(CHorizontalLayout.JustifyContentMode.BETWEEN);
		header.setAlignItems(CHorizontalLayout.Alignment.CENTER);
		header.setPadding(false);
		header.setSpacing(false);
		
		// Left: Label
		final var label = new com.vaadin.flow.component.html.Span("Static Routes");
		label.getStyle()
			.set("font-weight", "500")
			.set("font-size", STYLE_FONT_SIZE_SMALL);
		
		// Right: Add button
		final CButton addButton = new CButton("Add Route", VaadinIcon.PLUS.create());
		addButton.setId(ID_ADD_ROUTE_BUTTON);
		addButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
		addButton.addClickListener(e -> on_addRoute_clicked());
		
		header.add(label, addButton);
		
		return header;
	}
	
	private Grid<CRouteEntry> createRoutesGrid() {
		final Grid<CRouteEntry> grid = new Grid<>(CRouteEntry.class, false);
		grid.setId(ID_ROUTES_GRID);
		grid.setHeight("250px");
		grid.setWidthFull();
		
		// Configure editor
		final Editor<CRouteEntry> editor = grid.getEditor();
		final Binder<CRouteEntry> binder = new Binder<>(CRouteEntry.class);
		editor.setBinder(binder);
		editor.setBuffered(true);
		
		// Network column with editor
		final TextField networkField = new TextField();
		networkField.setPlaceholder("192.168.2.0");
		binder.forField(networkField).bind(CRouteEntry::getNetwork, CRouteEntry::setNetwork);
		grid.addColumn(CRouteEntry::getNetwork)
			.setHeader("Network")
			.setWidth("180px")
			.setFlexGrow(0)
			.setEditorComponent(networkField);
		
		// Netmask column with editor
		final TextField netmaskField = new TextField();
		netmaskField.setPlaceholder("24 or 255.255.255.0");
		binder.forField(netmaskField).bind(CRouteEntry::getNetmask, CRouteEntry::setNetmask);
		grid.addColumn(CRouteEntry::getNetmask)
			.setHeader("Netmask")
			.setWidth("160px")
			.setFlexGrow(0)
			.setEditorComponent(netmaskField);
		
		// Gateway column with editor
		final TextField gatewayField = new TextField();
		gatewayField.setPlaceholder("192.168.1.254");
		binder.forField(gatewayField).bind(CRouteEntry::getGateway, CRouteEntry::setGateway);
		grid.addColumn(CRouteEntry::getGateway)
			.setHeader("Gateway")
			.setWidth("180px")
			.setFlexGrow(0)
			.setEditorComponent(gatewayField);
		
		// Actions column with edit and delete buttons
		grid.addComponentColumn(route -> {
			final CHorizontalLayout actions = new CHorizontalLayout();
			actions.setSpacing(false);
			actions.setPadding(false);
			
			final CButton editButton = new CButton(VaadinIcon.EDIT.create());
			editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
			editButton.addClickListener(e -> {
				if (editor.isOpen()) {
					editor.cancel();
				}
				grid.getEditor().editItem(route);
			});
			
			final CButton deleteButton = new CButton(VaadinIcon.TRASH.create());
			deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
			deleteButton.addClickListener(e -> {
				staticRoutes.remove(route);
				grid.setItems(staticRoutes);
				updateValidationDisplay();
			});
			
			actions.add(editButton, deleteButton);
			return actions;
		}).setHeader("Actions").setWidth("120px").setFlexGrow(0);
		
		// Editor save listener
		editor.addSaveListener(event -> updateValidationDisplay());
		
		// Set initial data
		grid.setItems(staticRoutes);
		
		return grid;
	}
	
	private void updateValidationDisplay() {
		final String gateway = defaultGatewayField.getValue();
		
		// Validate default gateway (optional)
		final boolean gatewayValid = gateway == null || gateway.trim().isEmpty() || isValidIpAddress(gateway);
		
		// Validate all routes - count valid and invalid
		final long validRoutesCount = staticRoutes.stream()
			.filter(route -> 
				route.getNetwork() != null && !route.getNetwork().trim().isEmpty()
				&& route.getNetmask() != null && !route.getNetmask().trim().isEmpty()
				&& route.getGateway() != null && !route.getGateway().trim().isEmpty()
				&& isValidIpAddress(route.getNetwork())
				&& isValidNetmask(route.getNetmask())
				&& isValidIpAddress(route.getGateway()))
			.count();
		final long invalidRoutesCount = staticRoutes.size() - validRoutesCount;
		
		// Display validation
		if (!gatewayValid) {
			setValidationError("⚠️ Invalid gateway");
		} else if (invalidRoutesCount > 0) {
			setValidationError("❌ " + invalidRoutesCount + " invalid, ✅ " + validRoutesCount + " valid");
		} else if (validRoutesCount > 0) {
			setValidationSuccess("✅ " + validRoutesCount + " route" + (validRoutesCount > 1 ? "s" : ""));
		} else {
			setValidationWarning("No static routes");
		}
	}
	
	private boolean isValidNetmask(final String netmask) {
		if (netmask == null || netmask.trim().isEmpty()) {
			return false;
		}
		final String trimmed = netmask.trim();
		
		// Check CIDR format (0-32)
		if (trimmed.matches("^\\d{1,2}$")) {
			final int cidr = Integer.parseInt(trimmed);
			return cidr >= 0 && cidr <= 32;
		}
		
		// Check full mask format
		return isValidIpAddress(trimmed);
	}
	
	private void on_addRoute_clicked() {
		final CRouteEntry newRoute = new CRouteEntry("", "", "");
		staticRoutes.add(newRoute);
		routesGrid.setItems(staticRoutes);
		routesGrid.getEditor().editItem(newRoute);
		updateValidationDisplay();
	}
	
	@Override
	public String getDialogTitleString() {
		return "Route Configuration";
	}
	
	@Override
	protected Icon getFormIcon() {
		return VaadinIcon.ROAD.create();
	}
	
	@Override
	protected String getFormTitleString() {
		return "Edit Network Routes";
	}
	
	@Override
	protected void setupButtons() {
		final CButton saveButton = CButton.createSaveButton("Apply", event -> on_save_clicked());
		final CButton cancelButton = CButton.createCancelButton("Cancel", event -> close());
		buttonLayout.add(saveButton, cancelButton);
	}
	
	private void on_save_clicked() {
		// Close editor if open
		if (routesGrid.getEditor().isOpen()) {
			routesGrid.getEditor().save();
		}
		
		final String gateway = defaultGatewayField.getValue();
		
		// Validation 1: Gateway format (if provided)
		if (gateway != null && !gateway.trim().isEmpty() && !isValidIpAddress(gateway)) {
			CNotificationService.showWarning("Invalid default gateway IP address format");
			defaultGatewayField.focus();
			return;
		}
		
		// Validation 2: All routes must be valid
		for (final CRouteEntry route : staticRoutes) {
			if (!route.isValid()) {
				CNotificationService.showWarning("All route fields must be filled");
				return;
			}
			
			if (!isValidIpAddress(route.getNetwork())) {
				CNotificationService.showWarning("Invalid network IP address: " + route.getNetwork());
				return;
			}
			
			if (!isValidNetmask(route.getNetmask())) {
				CNotificationService.showWarning("Invalid netmask: " + route.getNetmask());
				return;
			}
			
			if (!isValidIpAddress(route.getGateway())) {
				CNotificationService.showWarning("Invalid gateway IP address: " + route.getGateway());
				return;
			}
		}
		
		// Create update object
		final CRouteConfigurationUpdate update = new CRouteConfigurationUpdate(
			gateway != null && !gateway.trim().isEmpty() ? gateway : null,
			staticRoutes
		);
		
		// Notify caller
		try {
			onSave.accept(update);
			close();
		} catch (final Exception e) {
			CNotificationService.showException("Failed to apply route configuration", e);
		}
	}
}
