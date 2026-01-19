package tech.derbent.base.login.view;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.companies.service.CCompanyService;
import tech.derbent.api.config.CDataInitializer;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CColorAwareComboBox;
import tech.derbent.api.ui.component.basic.CComboBox;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.dialogs.CDialogProgress;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.config.CBabDataInitializer;
import tech.derbent.base.session.service.ISessionService;

/** Custom login view using basic Vaadin components instead of LoginOverlay. This provides an alternative login interface for testing purposes. */
@Route (value = "login", autoLayout = false)
@PageTitle ("Login")
@AnonymousAllowed
public class CCustomLoginView extends Main implements BeforeEnterObserver {

	private static final Logger LOGGER = LoggerFactory.getLogger(CCustomLoginView.class);
	private static final String SCHEMA_BAB_GATEWAY = "BAB Gateway";
	private static final String SCHEMA_DERBENT = "Derbent";
	private static final long serialVersionUID = 1L;

	private static HorizontalLayout createHorizontalField(final String labelText, final Component field) {
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setWidthFull();
		layout.setAlignItems(FlexComponent.Alignment.CENTER);
		final Paragraph label = new Paragraph(labelText);
		label.addClassNames(LumoUtility.FontWeight.MEDIUM);
		label.setWidth("120px");
		field.getElement().getStyle().set("flex", "1");
		layout.add(label, field);
		return layout;
	}

	// private final Button chartTestButton = new CButton("Chart Test", CColorUtils.createStyledIcon("vaadin:chart", CColorUtils.CRUD_UPDATE_COLOR));
	private final CColorAwareComboBox<CCompany> companyField = new CColorAwareComboBox<>(CCompany.class);
	private final CCompanyService companyService;
	private final Environment environment;
	private final Div errorMessage = new Div();
	private final Button loginButton = new CButton("Login", CColorUtils.createStyledIcon("vaadin:sign-in", CColorUtils.CRUD_SAVE_COLOR));
	private final PasswordField passwordField = new PasswordField();
	private final Button resetDbButton = new CButton("DB Full", CColorUtils.createStyledIcon("vaadin:refresh", CColorUtils.CRUD_UPDATE_COLOR));
	private final Button resetDbMinimalButton = new CButton("DB Min", CColorUtils.createStyledIcon("vaadin:refresh", CColorUtils.CRUD_UPDATE_COLOR));
	private final CComboBox<String> schemaSelector = new CComboBox<>();
	private final ISessionService sessionService;
	private final TextField usernameField = new TextField();

	/** Constructor sets up the custom login form with basic Vaadin components. */
	@Autowired
	public CCustomLoginView(ISessionService sessionService, CCompanyService companyService, Environment environment) {
		this.sessionService = sessionService;
		this.companyService = companyService;
		this.environment = environment;
		addClassNames("custom-login-view");
		setSizeFull();
		setupForm();
	}

	/** Handles navigation events before entering the view. Checks for authentication failure indicators and displays error messages. Also handles the
	 * 'continue' parameter to set the default view selection. */
	@Override
	public void beforeEnter(final BeforeEnterEvent event) {
		// Check if the URL contains an error parameter
		if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
			showError("Invalid username or password");
		}
	}

	private void handleLogin() {
		try {
			String username = usernameField.getValue();
			final String password = passwordField.getValue();
			final CCompany company = companyField.getValue();
			if (company == null) {
				showError("Please select a company. If none are listed, reset sample data first.");
				LOGGER.warn("Login attempted without a selected company; profile(s): {}", (Object) environment.getActiveProfiles());
				return;
			}
			username = username + "@" + company.getId();
			errorMessage.setText("");
			// Basic validation
			Check.notBlank(username, "Please enter both username and password");
			Check.notBlank(password, "Please enter both username and password");
			// Get selected view for redirect
			final String redirectView = "home";
			// Create form and submit to Spring Security endpoint with redirect parameter
			getElement().executeJs("const form = document.createElement('form');" + "form.method = 'POST';" + "form.action = 'login';"
					+ "const usernameInput = document.createElement('input');" + "usernameInput.type = 'hidden';" + "usernameInput.name = 'username';"
					+ "usernameInput.value = $0;" + "form.appendChild(usernameInput);" + "const passwordInput = document.createElement('input');"
					+ "passwordInput.type = 'hidden';" + "passwordInput.name = 'password';" + "passwordInput.value = $1;"
					+ "form.appendChild(passwordInput);" + "const redirectInput = document.createElement('input');" + "redirectInput.type = 'hidden';"
					+ "redirectInput.name = 'redirect';" + "redirectInput.value = $2;" + "form.appendChild(redirectInput);"
					+ "document.body.appendChild(form);" + "form.submit();", username, password, redirectView);
		} catch (final Exception e) {
			LOGGER.error("Login error.", e);
			showError(e.getMessage());
		}
	}

	/** Handle reset database button click. */
	private void on_buttonResetDb_clicked() {
		try {
			LOGGER.info("🔄 Showing DB Full reset confirmation dialog...");
			CNotificationService.showConfirmationDialog("Veritabanı SIFIRLANACAK ve örnek veriler yeniden yüklenecek. Devam edilsin mi?",
					"Evet, sıfırla", () -> {
						runDatabaseReset(false, "Sample data yeniden yüklendi.", "Örnek veriler ve varsayılan veriler yeniden oluşturuldu.");
					});
		} catch (final Exception e) {
			CNotificationService.showException("Error showing confirmation dialog", e);
		}
	}

	/** Handle reset database minimal button click. */
	private void on_buttonResetDbMinimal_clicked() {
		try {
			LOGGER.info("🔄 Showing DB Min reset confirmation dialog...");
			CNotificationService.showConfirmationDialog("Veritabanı SIFIRLANACAK ve minimum örnek veriler yeniden yüklenecek. Devam edilsin mi?",
					"Evet, sıfırla", () -> {
						runDatabaseReset(true, "Minimum örnek veri yeniden yüklendi.",
								"Minimum örnek veriler ve varsayılan veriler yeniden oluşturuldu.");
					});
		} catch (final Exception e) {
			CNotificationService.showException("Error showing confirmation dialog", e);
		}
	}

	@SuppressWarnings ("unused")
	private void populateForm() {
		try {
			final List<CCompany> activeCompanies = companyService.findActiveCompanies();
			companyField.setItems(activeCompanies);
			// Auto-select first company as default
			if (!activeCompanies.isEmpty()) {
				loginButton.setEnabled(true);
				companyField.setValue(activeCompanies.get(0));
				errorMessage.setText("");
			} else {
				loginButton.setEnabled(false);
				showError("No active companies found. Please reset the database to load sample data.");
			}
		} catch (final Exception e) {
			LOGGER.error("Error loading companies.", e);
			loginButton.setEnabled(false);
			showError("Error loading companies. Please contact administrator.");
		}
	}

	private void runDatabaseReset(final boolean minimal, final String successMessage, final String infoMessage) {
		final UI ui = getUI().orElse(null);
		Check.notNull(ui, "UI must be available to run database reset");
		final VaadinSession session = ui.getSession();
		Check.notNull(session, "Vaadin session must not be null");
		final String schemaSelection = schemaSelector.getValue();
		LOGGER.info("✅ DB reset confirmed - starting database initialization...");
		final CDialogProgress progressDialog = CNotificationService.showProgressDialog("Database Reset", "Veritabanı yeniden hazırlanıyor...");
		CompletableFuture.runAsync(() -> {
			Exception failure = null;
			try {
				runDatabaseResetInSession(session, ui, minimal, schemaSelection);
				LOGGER.info("🗄️ DB reset completed successfully");
			} catch (final Exception ex) {
				failure = ex;
				LOGGER.error("❌ DB reset failed", ex);
			} finally {
				final Exception capturedFailure = failure;
				try {
					ui.access(() -> {
						progressDialog.close();
						if (capturedFailure == null) {
							CNotificationService.showSuccess(successMessage);
							CNotificationService.showInfoDialog(infoMessage);
							populateForm();
						} else {
							CNotificationService.showException("Hata", capturedFailure);
						}
					});
				} catch (final Exception accessError) {
					LOGGER.error("Error closing progress dialog after DB reset", accessError);
				}
			}
		});
	}

	private void runDatabaseResetInSession(final VaadinSession session, final UI ui, final boolean minimal, final String schemaSelection)
			throws Exception {
		session.lock();
		try {
			VaadinSession.setCurrent(session);
			UI.setCurrent(ui);
			// Auto-detect profile if schema not explicitly selected
			String resolvedSchema = schemaSelection;
			if (resolvedSchema == null) {
				// Check if BAB profile is active
				if (environment.acceptsProfiles(Profiles.of("bab"))) {
					resolvedSchema = SCHEMA_BAB_GATEWAY;
					LOGGER.info("🔧 Auto-detected BAB profile - using BAB Gateway initializer");
				} else {
					resolvedSchema = SCHEMA_DERBENT;
					LOGGER.info("🔧 Using default Derbent initializer");
				}
			}
			if (SCHEMA_BAB_GATEWAY.equals(resolvedSchema)) {
				final Map<String, CBabDataInitializer> initializers = CSpringContext.getBeansOfType(CBabDataInitializer.class);
				Check.isTrue(!initializers.isEmpty(), "BAB initializer bean is not available. Activate the bab profile.");
				final CBabDataInitializer init = initializers.values().iterator().next();
				LOGGER.info("🔧 Using BAB Gateway data initializer");
				init.reloadForced(minimal);
			} else {
				final CDataInitializer init = new CDataInitializer(sessionService);
				LOGGER.info("🔧 Using Derbent data initializer");
				init.reloadForced(minimal);
			}
		} finally {
			UI.setCurrent(null);
			VaadinSession.setCurrent(null);
			session.unlock();
		}
	}

	@SuppressWarnings ("unused")
	private void setupForm() {
		// Create main container
		final VerticalLayout container = new VerticalLayout();
		container.addClassNames(LumoUtility.JustifyContent.CENTER, LumoUtility.AlignItems.CENTER, LumoUtility.Height.FULL, LumoUtility.Padding.SMALL);
		container.setSpacing(false);
		// Create form card with minimal design
		final VerticalLayout formCard = new VerticalLayout();
		formCard.addClassNames(LumoUtility.Background.BASE, LumoUtility.BorderRadius.MEDIUM, LumoUtility.BoxShadow.SMALL, LumoUtility.Padding.SMALL);
		formCard.setSpacing(false);
		formCard.setWidth("550px");
		// Application icon - using a simple Vaadin icon instead of image
		final HorizontalLayout headerlayout = new CHorizontalLayout();
		final var icon = VaadinIcon.BUILDING.create();
		icon.setSize("48px");
		icon.addClassNames(LumoUtility.Margin.Bottom.MEDIUM, LumoUtility.TextColor.PRIMARY);
		// Title
		final H1 title = new H1("Derbent");
		title.addClassNames(LumoUtility.TextAlignment.CENTER, LumoUtility.Margin.Bottom.SMALL);
		headerlayout.add(icon, title);
		// Setup form fields horizontally
		final HorizontalLayout companyLayout = createHorizontalField("Company:", companyField);
		final HorizontalLayout usernameLayout = createHorizontalField("Username:", usernameField);
		final HorizontalLayout passwordLayout = createHorizontalField("Password:", passwordField);
		// Setup form fields
		// Company field setup
		companyField.setWidthFull();
		companyField.setRequired(true);
		companyField.setRequiredIndicatorVisible(true);
		companyField.setId("custom-company-input");
		// Add enter key listener to company field using addEventListener
		companyField.getElement().addEventListener("keydown", event -> {
			handleLogin();
		}).setFilter("event.key === 'Enter'");
		// Load enabled companies from service
		// Username field setup
		usernameField.setWidthFull();
		usernameField.setRequired(true);
		usernameField.setRequiredIndicatorVisible(true);
		usernameField.setId("custom-username-input");
		// Add enter key listener to username field
		usernameField.addKeyPressListener(Key.ENTER, event -> handleLogin());
		// Password field setup
		passwordField.setWidthFull();
		passwordField.setRequired(true);
		passwordField.setRequiredIndicatorVisible(true);
		passwordField.setId("custom-password-input");
		// Add enter key listener to password field
		passwordField.addKeyPressListener(Key.ENTER, event -> handleLogin());
		// Add click listener to login button
		loginButton.addClickListener(event -> handleLogin());
		loginButton.setMinWidth("120px");
		loginButton.setId("cbutton-login");
		// Database reset button setup
		resetDbButton.addClickListener(event -> on_buttonResetDb_clicked());
		resetDbButton.setId("cbutton-db-full");
		resetDbMinimalButton.addClickListener(event -> on_buttonResetDbMinimal_clicked());
		resetDbMinimalButton.setId("cbutton-db-min");
		// Chart test button setup
		// chartTestButton.addClickListener(e -> { getUI().ifPresent(ui -> ui.navigate("chart"));});
		// chartTestButton.setMinWidth("120px");
		// Error message display
		errorMessage.setId("custom-error-message");
		errorMessage.addClassNames(LumoUtility.TextColor.ERROR, LumoUtility.TextAlignment.RIGHT, LumoUtility.FontSize.SMALL);
		errorMessage.setVisible(true);
		errorMessage.setText("");
		// set full width for error message
		errorMessage.setWidthFull();
		// set min height for error message
		errorMessage.getStyle().set("min-height", "20px");
		resetDbButton.setMinWidth("120px");
		resetDbButton.setMinWidth("120px");
		// Get active profile(s) to display
		String activeProfiles = String.join(", ", environment.getActiveProfiles());
		if (activeProfiles.isEmpty()) {
			activeProfiles = "default";
		}
		final Paragraph passwordHint = new Paragraph("Default: admin/test123 | Profile: " + activeProfiles);
		passwordHint.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);
		passwordHint.setWidthFull();
		// Back to original login link
		final HorizontalLayout buttonsLayout = new CHorizontalLayout();
		buttonsLayout.setAlignItems(Alignment.CENTER);
		//
		final boolean isBabProfile = environment.acceptsProfiles(Profiles.of("bab"));
		final List<String> schemaOptions = isBabProfile ? List.of(SCHEMA_BAB_GATEWAY) : List.of(SCHEMA_DERBENT);
		schemaSelector.setItems(schemaOptions);
		schemaSelector.setValue(schemaOptions.get(0));
		final boolean showSchemaSelector = schemaOptions.size() > 1;
		schemaSelector.setVisible(showSchemaSelector);
		schemaSelector.setId("custom-schema-selector");
		if (showSchemaSelector) {
			buttonsLayout.add(schemaSelector);
		}
		buttonsLayout.add(new CDiv(), resetDbMinimalButton, resetDbButton/* , chartTestButton */);
		final HorizontalLayout loginButtonLayout = new CHorizontalLayout();
		loginButtonLayout.setAlignItems(Alignment.END);
		loginButtonLayout.add(passwordHint, new CDiv(), loginButton);
		// Add components to form card
		formCard.add(headerlayout, usernameLayout, passwordLayout, companyLayout, errorMessage, loginButtonLayout, buttonsLayout);
		container.add(formCard);
		add(container);
		populateForm();
	}

	private void showError(final String message) {
		errorMessage.setText(message);
	}
}
