package tech.derbent.base.login.view;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
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
import tech.derbent.api.services.CRouteDiscoveryService;
import tech.derbent.api.ui.dialogs.CInformationDialog;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.components.CButton;
import tech.derbent.api.views.components.CDiv;
import tech.derbent.api.views.components.CHorizontalLayout;
import tech.derbent.app.companies.domain.CCompany;
import tech.derbent.app.companies.service.CCompanyService;
import tech.derbent.api.config.CDataInitializer;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.setup.service.CSystemSettingsService;

/** Custom login view using basic Vaadin components instead of LoginOverlay. This provides an alternative login interface for testing purposes. */
@Route (value = "login", autoLayout = false)
@PageTitle ("Login")
@AnonymousAllowed
public class CCustomLoginView extends Main implements BeforeEnterObserver {

	private static final Logger LOGGER = LoggerFactory.getLogger(CCustomLoginView.class);
	private static final long serialVersionUID = 1L;
	private final ComboBox<CCompany> companyField = new ComboBox<CCompany>();
	private final CCompanyService companyService;
	private final Div errorMessage = new Div();
	private final Button loginButton = new CButton("Login", CColorUtils.createStyledIcon("vaadin:sign-in", CColorUtils.CRUD_SAVE_COLOR));
	private final PasswordField passwordField = new PasswordField();
	private final Button resetDbButton = new CButton("DB Full", CColorUtils.createStyledIcon("vaadin:refresh", CColorUtils.CRUD_UPDATE_COLOR));
	private final Button resetDbMinimalButton =
			new CButton("DB Minimal", CColorUtils.createStyledIcon("vaadin:refresh", CColorUtils.CRUD_UPDATE_COLOR));
	private final ISessionService sessionService;
	private final TextField usernameField = new TextField();

	/** Constructor sets up the custom login form with basic Vaadin components. */
	@Autowired
	public CCustomLoginView(CSystemSettingsService systemSettingsService, CRouteDiscoveryService routeDiscoveryService,
			ISessionService sessionService, CCompanyService companyService) {
		this.sessionService = sessionService;
		this.companyService = companyService;
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
		try {
			String username = usernameField.getValue();
			final String password = passwordField.getValue();
			final CCompany company = companyField.getValue();
			username = username + "@" + company.getId();
			errorMessage.setText("");
			// Basic validation
			Check.notBlank(username, "Please enter both username and password");
			Check.notBlank(password, "Please enter both username and password");
			// Get selected view for redirect
			String redirectView = "home";
			// Create form and submit to Spring Security endpoint with redirect parameter
			getElement().executeJs("const form = document.createElement('form');" + "form.method = 'POST';" + "form.action = 'login';"
					+ "const usernameInput = document.createElement('input');" + "usernameInput.type = 'hidden';" + "usernameInput.name = 'username';"
					+ "usernameInput.value = $0;" + "form.appendChild(usernameInput);" + "const passwordInput = document.createElement('input');"
					+ "passwordInput.type = 'hidden';" + "passwordInput.name = 'password';" + "passwordInput.value = $1;"
					+ "form.appendChild(passwordInput);" + "const redirectInput = document.createElement('input');" + "redirectInput.type = 'hidden';"
					+ "redirectInput.name = 'redirect';" + "redirectInput.value = $2;" + "form.appendChild(redirectInput);"
					+ "document.body.appendChild(form);" + "form.submit();", username, password, redirectView);
		} catch (final Exception e) {
			LOGGER.error("Login error.");
			showError(e.getMessage());
		}
	}

	private void populateForm() {
		try {
			List<CCompany> activeCompanies = companyService.findActiveCompanies();
			companyField.setItems(activeCompanies);
			// Auto-select first company as default
			if (!activeCompanies.isEmpty()) {
				companyField.setValue(activeCompanies.get(0));
			}
		} catch (Exception e) {
			LOGGER.error("Error loading companies.");
			showError("Error loading companies. Please contact administrator.");
		}
	}

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
		companyField.setItemLabelGenerator(company -> company.getName());
		// Load enabled companies from service
		// Username field setup
		usernameField.setWidthFull();
		usernameField.setRequired(true);
		usernameField.setRequiredIndicatorVisible(true);
		usernameField.setId("custom-username-input");
		// Password field setup
		passwordField.setWidthFull();
		passwordField.setRequired(true);
		passwordField.setRequiredIndicatorVisible(true);
		passwordField.setId("custom-password-input");
		// Add click listener to login button
		loginButton.addClickListener(_ -> handleLogin());
		// Add enter key listener to password field
		passwordField.addKeyPressListener(com.vaadin.flow.component.Key.ENTER, _ -> handleLogin());
		// Database reset button setup
		resetDbButton.addClickListener(_ -> {
			final ConfirmDialog dialog = new ConfirmDialog("Onay", "Veritabanı SIFIRLANACAK ve örnek veriler yeniden yüklenecek. Devam edilsin mi?",
					"Evet, sıfırla", _ -> {
						try {
							final CDataInitializer init = new CDataInitializer(sessionService);
							init.reloadForced(false); // veya empty check’li bir metod yaz
							Notification.show("Sample data yeniden yüklendi.", 4000, Notification.Position.MIDDLE);
							CInformationDialog info = new CInformationDialog("Örnek veriler ve varsayılan veriler yeniden oluşturuldu.");
							info.open();
							populateForm();
							// UI.getCurrent().getPage().reload();
						} catch (final Exception ex) {
							Notification.show("Hata: " + ex.getMessage(), 6000, Notification.Position.MIDDLE);
						}
					}, "Vazgeç", _ -> {});
			dialog.open();
		});
		resetDbMinimalButton.addClickListener(_ -> {
			final ConfirmDialog dialog = new ConfirmDialog("Onay", "Veritabanı SIFIRLANACAK ve örnek veriler yeniden yüklenecek. Devam edilsin mi?",
					"Evet, sıfırla", _ -> {
						try {
							final CDataInitializer init = new CDataInitializer(sessionService);
							init.reloadForced(true); // veya empty check’li bir metod yaz
							Notification.show("Sample data yeniden yüklendi.", 4000, Notification.Position.MIDDLE);
							CInformationDialog info = new CInformationDialog("Örnek veriler ve varsayılan veriler yeniden oluşturuldu.");
							info.open();
							populateForm();
							// UI.getCurrent().getPage().reload();
						} catch (final Exception ex) {
							Notification.show("Hata: " + ex.getMessage(), 6000, Notification.Position.MIDDLE);
						}
					}, "Vazgeç", _ -> {});
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
		passwordHint.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);
		passwordHint.setWidthFull();
		// Back to original login link
		final HorizontalLayout buttonsLayout = new CHorizontalLayout();
		buttonsLayout.setAlignItems(Alignment.CENTER);
		//
		buttonsLayout.add(passwordHint, resetDbMinimalButton, resetDbButton);
		final HorizontalLayout loginButtonLayout = new CHorizontalLayout();
		loginButtonLayout.setAlignItems(Alignment.END);
		loginButtonLayout.add(new CDiv(), loginButton);
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
