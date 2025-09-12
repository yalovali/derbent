package tech.derbent.login.view;

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import tech.derbent.base.service.CRouteDiscoveryService;
import tech.derbent.base.ui.dialogs.CInformationDialog;
import tech.derbent.config.CSampleDataInitializer;
import tech.derbent.setup.service.CSystemSettingsService;

/** Custom login view using basic Vaadin components instead of LoginOverlay. This provides an alternative login interface for testing purposes. */
@Route (value = "login", autoLayout = false)
@PageTitle ("Login")
@AnonymousAllowed
public class CCustomLoginView extends Main implements BeforeEnterObserver {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CCustomLoginView.class);
	private final TextField usernameField = new TextField();
	private final PasswordField passwordField = new PasswordField();
	private final Button loginButton = new Button("Login");
	private final Button resetDbButton = new Button("Reset Database");
	private final Div errorMessage = new Div();
	private final Checkbox autoLoginCheckbox = new Checkbox("Auto-login after 2 seconds");
	private final ComboBox<String> defaultViewComboBox = new ComboBox<>("Go to view after login");
	private final CSystemSettingsService systemSettingsService;
	private final CRouteDiscoveryService routeDiscoveryService;

	/** Constructor sets up the custom login form with basic Vaadin components. */
	@Autowired
	public CCustomLoginView(CSystemSettingsService systemSettingsService, CRouteDiscoveryService routeDiscoveryService) {
		this.systemSettingsService = systemSettingsService;
		this.routeDiscoveryService = routeDiscoveryService;
		addClassNames("custom-login-view");
		setSizeFull();
		setupForm();
		initializeComponents();
	}

	/** Handles navigation events before entering the view. Checks for authentication failure indicators and displays error messages. Also handles the
	 * 'continue' parameter to set the default view selection. */
	@Override
	public void beforeEnter(final BeforeEnterEvent event) {
		// Check if the URL contains an error parameter
		if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
			showError("Invalid username or password");
		}
		// Check for 'continue' parameter (originally requested page)
		var continueParams = event.getLocation().getQueryParameters().getParameters().get("continue");
		if (continueParams != null && !continueParams.isEmpty()) {
			String requestedPage = continueParams.get(0);
			if (requestedPage != null && !requestedPage.trim().isEmpty()) {
				// tring viewName = mapUrlToViewName(requestedPage);
				// if (viewName != null) {
				defaultViewComboBox.setValue(requestedPage);
				// Save this selection to system settings
				saveAutoLoginSettings();
				LOGGER.debug("Set default view from continue parameter: {}", requestedPage);
				// }
			}
		}
	}

	private HorizontalLayout createHorizontalField(final String labelText, final Component field) {
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setWidthFull();
		layout.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
		final Paragraph label = new Paragraph(labelText);
		label.addClassNames(LumoUtility.FontWeight.MEDIUM);
		label.setWidth("120px");
		field.getElement().getStyle().set("flex", "1");
		layout.add(label, field);
		return layout;
	}

	private void handleLogin() {
		final String username = usernameField.getValue();
		final String password = passwordField.getValue();
		errorMessage.setText("");
		// Basic validation
		if (username.isEmpty() || password.isEmpty()) {
			showError("Please enter both username and password");
			return;
		}
		// Save settings before login
		saveAutoLoginSettings();
		// Get selected view for redirect
		String redirectView = defaultViewComboBox.getValue();
		if (redirectView == null || redirectView.isEmpty()) {
			redirectView = "home";
		}
		// Create form and submit to Spring Security endpoint with redirect parameter
		getElement().executeJs("const form = document.createElement('form');" + "form.method = 'POST';" + "form.action = 'login';"
				+ "const usernameInput = document.createElement('input');" + "usernameInput.type = 'hidden';" + "usernameInput.name = 'username';"
				+ "usernameInput.value = $0;" + "form.appendChild(usernameInput);" + "const passwordInput = document.createElement('input');"
				+ "passwordInput.type = 'hidden';" + "passwordInput.name = 'password';" + "passwordInput.value = $1;"
				+ "form.appendChild(passwordInput);" + "const redirectInput = document.createElement('input');" + "redirectInput.type = 'hidden';"
				+ "redirectInput.name = 'redirect';" + "redirectInput.value = $2;" + "form.appendChild(redirectInput);"
				+ "document.body.appendChild(form);" + "form.submit();", username, password, redirectView);
	}

	private void setupForm() {
		// Create main container
		final VerticalLayout container = new VerticalLayout();
		container.addClassNames(LumoUtility.JustifyContent.CENTER, LumoUtility.AlignItems.CENTER, LumoUtility.Height.FULL, LumoUtility.Padding.LARGE);
		container.setSpacing(false);
		// Create form card with minimal design
		final VerticalLayout formCard = new VerticalLayout();
		formCard.addClassNames(LumoUtility.Background.BASE, LumoUtility.BorderRadius.MEDIUM, LumoUtility.BoxShadow.SMALL, LumoUtility.Padding.LARGE);
		formCard.setSpacing(true);
		formCard.setWidth("500px");
		// Application icon - using a simple Vaadin icon instead of image
		final HorizontalLayout headerlayout = new HorizontalLayout();
		final var icon = VaadinIcon.BUILDING.create();
		icon.setSize("48px");
		icon.addClassNames(LumoUtility.Margin.Bottom.MEDIUM, LumoUtility.TextColor.PRIMARY);
		// Title
		final H1 title = new H1("Derbent");
		title.addClassNames(LumoUtility.TextAlignment.CENTER, LumoUtility.Margin.Bottom.SMALL);
		headerlayout.add(icon, title);
		// Subtitle
		final Paragraph subtitle = new Paragraph("Control the progress");
		subtitle.addClassNames(LumoUtility.TextAlignment.CENTER, LumoUtility.TextColor.SECONDARY);
		// Setup form fields horizontally
		final HorizontalLayout usernameLayout = createHorizontalField("Username:", usernameField);
		final HorizontalLayout passwordLayout = createHorizontalField("Password:", passwordField);
		// Setup auto-login components
		autoLoginCheckbox.setId("auto-login-checkbox");
		autoLoginCheckbox.addClassNames(LumoUtility.Margin.Top.SMALL);
		// Setup form fields
		setupFormFields();
		// Database reset button setup
		resetDbButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
		resetDbButton.addClickListener(e -> {
			final ConfirmDialog dialog = new ConfirmDialog("Onay", "Veritabanı SIFIRLANACAK ve örnek veriler yeniden yüklenecek. Devam edilsin mi?",
					"Evet, sıfırla", ev -> {
						try {
							final CSampleDataInitializer init = new CSampleDataInitializer();
							init.reloadForced(); // veya empty check’li bir metod yaz
							Notification.show("Sample data yeniden yüklendi.", 4000, Notification.Position.MIDDLE);
							CInformationDialog info = new CInformationDialog("Örnek veriler ve varsayılan veriler yeniden oluşturuldu.");
							info.open();
							// UI.getCurrent().getPage().reload();
						} catch (final Exception ex) {
							Notification.show("Hata: " + ex.getMessage(), 6000, Notification.Position.MIDDLE);
						}
					}, "Vazgeç", ev -> {});
			dialog.open();
		});
		// Error message display
		errorMessage.setId("custom-error-message");
		errorMessage.addClassNames(LumoUtility.TextColor.ERROR, LumoUtility.TextAlignment.RIGHT, LumoUtility.FontSize.SMALL);
		errorMessage.setVisible(true);
		errorMessage.setText("");
		// set full width for error message
		errorMessage.setWidthFull();
		// set min height for error message
		errorMessage.getStyle().set("min-height", "20px");
		// Password hint
		final Paragraph passwordHint = new Paragraph("Default: admin/test123");
		passwordHint.addClassNames(LumoUtility.TextAlignment.CENTER, LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);
		// Back to original login link
		final HorizontalLayout buttonsLayout = new HorizontalLayout();
		//
		buttonsLayout.add(resetDbButton);
		// Add components to form card
		formCard.add(headerlayout, subtitle, usernameLayout, passwordLayout, autoLoginCheckbox, defaultViewComboBox, errorMessage, loginButton,
				passwordHint, buttonsLayout);
		container.add(formCard);
		add(container);
	}

	private void setupFormFields() {
		// Username field setup
		usernameField.setWidthFull();
		usernameField.setRequired(true);
		usernameField.setRequiredIndicatorVisible(true);
		usernameField.setId("custom-username-input");
		// Add value change listener to trigger auto-login timer
		usernameField.addValueChangeListener(e -> checkAutoLoginConditions());
		// Password field setup
		passwordField.setWidthFull();
		passwordField.setRequired(true);
		passwordField.setRequiredIndicatorVisible(true);
		passwordField.setId("custom-password-input");
		// Add value change listener to trigger auto-login timer
		passwordField.addValueChangeListener(e -> checkAutoLoginConditions());
		// Login button setup
		loginButton.setWidthFull();
		loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		loginButton.setId("custom-submit-button");
		// Add click listener to login button
		loginButton.addClickListener(e -> handleLogin());
		// Add enter key listener to password field
		passwordField.addKeyPressListener(com.vaadin.flow.component.Key.ENTER, e -> handleLogin());
	}

	private void checkAutoLoginConditions() {
		// Start auto-login timer if checkbox is checked and both fields are filled
		if (autoLoginCheckbox.getValue() && !usernameField.getValue().trim().isEmpty() && !passwordField.getValue().trim().isEmpty()) {
			startAutoLoginTimer();
		}
	}

	private void showError(final String message) {
		errorMessage.setText(message);
	}

	/** Initialize auto-login and view selection components */
	private void initializeComponents() {
		// Initialize default view combobox with dynamically discovered routes
		List<CRouteDiscoveryService.RouteInfo> availableRoutes = routeDiscoveryService.discoverAllRoutes();
		defaultViewComboBox.setItems(availableRoutes.stream().map(CRouteDiscoveryService.RouteInfo::getRoute).collect(Collectors.toList()));
		defaultViewComboBox.setItemLabelGenerator(this::getViewDisplayName);
		defaultViewComboBox.setValue("home"); // Default value
		defaultViewComboBox.setWidthFull();
		// Load settings from database
		loadSettingsFromDatabase();
		// Add value change listeners to save settings
		autoLoginCheckbox.addValueChangeListener(e -> saveAutoLoginSettings());
		defaultViewComboBox.addValueChangeListener(e -> saveAutoLoginSettings());
		// Handle auto-login checkbox state change
		autoLoginCheckbox.addValueChangeListener(e -> {
			if (e.getValue()) {
				startAutoLoginTimer();
			} else {
				stopAutoLoginTimer();
			}
		});
	}

	private String getViewDisplayName(String route) {
		CRouteDiscoveryService.RouteInfo routeInfo = routeDiscoveryService.getRouteInfo(route);
		if (routeInfo != null) {
			return routeInfo.getDisplayName();
		}
		
		// Fallback for routes not found in discovery service
		switch (route) {
		case "home":
			return "Home/Dashboard";
		case "cgridentityview":
			return "Grid Entities";
		case "cdashboardview":
			return "Dashboard";
		case "cprojectsview":
			return "Projects";
		case "cactivitiesview":
			return "Activities";
		case "cmeetingsview":
			return "Meetings";
		case "cusersview":
			return "Users";
		case "cganntview":
		case "cprojectganntview":
			return "Gantt Chart";
		case "cordersview":
			return "Orders";
		default:
			// Convert route to display name as fallback
			return route.replaceAll("([a-z])([A-Z])", "$1 $2")
					.replaceAll("^c", "")
					.replaceAll("view$", "")
					.trim();
		}
	}

	/** Maps URLs back to view names for the combobox. This is the reverse of the mapping used in CAuthenticationSuccessHandler. */
	@Deprecated
	private String mapUrlToViewNameXXX(String url) {
		if (url == null) {
			return null;
		}
		// Remove leading slash and any query parameters
		String cleanUrl = url.startsWith("/") ? url.substring(1) : url;
		int queryIndex = cleanUrl.indexOf('?');
		if (queryIndex > 0) {
			cleanUrl = cleanUrl.substring(0, queryIndex);
		}
		switch (cleanUrl.toLowerCase()) {
		case "":
		case "home":
			return "home";
		case "cprojectsview":
			return "cprojectsview";
		case "cactivitiesview":
			return "cactivitiesview";
		case "cmeetingsview":
			return "cmeetingsview";
		case "cusersview":
			return "cusersview";
		case "cganttview":
			return "cganttview";
		case "cordersview":
			return "cordersview";
		case "cgridentityview":
			return "cgridentityview";
		default:
			// For unknown URLs, return null so we don't change the selection
			return null;
		}
	}

	private void loadSettingsFromDatabase() {
		try {
			if (systemSettingsService != null) {
				boolean autoLoginEnabled = systemSettingsService.isAutoLoginEnabled();
				String defaultView = systemSettingsService.getDefaultLoginView();
				autoLoginCheckbox.setValue(autoLoginEnabled);
				if (defaultView != null && !defaultView.isEmpty()) {
					defaultViewComboBox.setValue(defaultView);
				}
			}
		} catch (Exception e) {
			// Log error but don't break the UI
			System.err.println("Error loading auto-login settings: " + e.getMessage());
		}
	}

	private void saveAutoLoginSettings() {
		try {
			if (systemSettingsService != null) {
				boolean autoLoginEnabled = autoLoginCheckbox.getValue();
				String defaultView = defaultViewComboBox.getValue();
				if (defaultView == null) {
					defaultView = "home";
				}
				systemSettingsService.updateAutoLoginSettings(autoLoginEnabled, defaultView);
			}
		} catch (Exception e) {
			// Log error but don't break the UI
			System.err.println("Error saving auto-login settings: " + e.getMessage());
		}
	}

	private void startAutoLoginTimer() {
		// Only start timer if both username and password are filled
		if (!usernameField.getValue().trim().isEmpty() && !passwordField.getValue().trim().isEmpty()) {
			// Use JavaScript to start a 2-second timer
			getElement().executeJs("setTimeout(() => { " + "  const loginBtn = document.getElementById('custom-submit-button'); "
					+ "  if (loginBtn && document.getElementById('auto-login-checkbox').checked) { " + "    loginBtn.click(); " + "  } "
					+ "}, 2000);");
		}
	}

	private void stopAutoLoginTimer() {
		// Clear any existing timer (this is a simple approach)
		// In a more complex implementation, you might want to track the timer ID
	}
}
