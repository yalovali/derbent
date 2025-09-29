package tech.derbent.users.view;

import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.companies.view.CCompanyUserSettingsDialog;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserCompanySettings;
import tech.derbent.users.service.CUserCompanySettingsService;
import tech.derbent.users.service.CUserService;

/** Component for displaying and editing a user's single company setting. This component provides a nice visual layout with icons and colors for the
 * CUserCompanySettings field, allowing users to view and edit their company membership and role through an attractive interface. */
public class CComponentSingleCompanyUserSetting extends VerticalLayout {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentSingleCompanyUserSetting.class);
	private final IContentOwner parentContent;
	private final CEnhancedBinder<CUser> binder;
	private final CUserService userService;
	private final CCompanyService companyService;
	private final CUserCompanySettingsService userCompanySettingsService;
	private CUser currentUser;
	private Div contentDiv;
	@Autowired
	private CNotificationService notificationService;

	public CComponentSingleCompanyUserSetting(IContentOwner parentContent, CUser currentEntity, CEnhancedBinder<CUser> beanValidationBinder,
			CUserService userService, CCompanyService companyService, CUserCompanySettingsService userCompanySettingsService) {
		Check.notNull(userService, "User service cannot be null");
		Check.notNull(companyService, "Company service cannot be null");
		Check.notNull(userCompanySettingsService, "User company settings service cannot be null");
		this.parentContent = parentContent;
		this.binder = beanValidationBinder;
		this.userService = userService;
		this.companyService = companyService;
		this.userCompanySettingsService = userCompanySettingsService;
		this.currentUser = currentEntity;
		initComponent();
		setupBindings();
	}

	private void initComponent() {
		setSpacing(true);
		setPadding(true);
		setWidthFull();
		// Create content div that will be updated when data changes
		contentDiv = new Div();
		contentDiv.setWidthFull();
		add(contentDiv);
		updateDisplay();
	}

	private void setupBindings() {
		if (binder != null) {
			// Bind to the companySettings field
			binder.addValueChangeListener(event -> {
				currentUser = binder.getBean();
				updateDisplay();
			});
		}
	}

	private void updateDisplay() {
		contentDiv.removeAll();
		try {
			if (currentUser == null) {
				showEmptyState("No user selected");
				return;
			}
			CUserCompanySettings companySettings = currentUser.getCompanySettings();
			if (companySettings == null || companySettings.getCompany() == null) {
				showEmptyState("No company assigned");
			} else {
				showCompanySettings(companySettings);
			}
		} catch (Exception e) {
			LOGGER.error("Error updating company settings display: {}", e.getMessage(), e);
			showErrorState("Error loading company settings");
		}
	}

	private void showEmptyState(String message) {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setWidthFull();
		layout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
		Icon icon = VaadinIcon.BUILDING.create();
		icon.setColor("#9E9E9E");
		icon.setSize("24px");
		Span messageSpan = new Span(message);
		messageSpan.getStyle().set("color", "#9E9E9E");
		messageSpan.getStyle().set("font-style", "italic");
		Button assignButton = new Button("Assign Company", VaadinIcon.PLUS.create());
		assignButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
		assignButton.addClickListener(e -> openEditDialog());
		layout.add(icon, messageSpan);
		layout.setFlexGrow(1, messageSpan);
		layout.add(assignButton);
		contentDiv.add(layout);
	}

	private void showCompanySettings(CUserCompanySettings settings) {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setWidthFull();
		layout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
		layout.setSpacing(true);
		// Company info with icon and color
		Component companyComponent = CColorUtils.getEntityWithIcon(settings.getCompany());
		// Role information
		VerticalLayout roleLayout = new VerticalLayout();
		roleLayout.setSpacing(false);
		roleLayout.setPadding(false);
		if (settings.getRole() != null && !settings.getRole().trim().isEmpty()) {
			Span roleSpan = new Span("Role: " + settings.getRole());
			roleSpan.getStyle().set("font-size", "0.9em");
			roleSpan.getStyle().set("color", "#666");
			roleLayout.add(roleSpan);
		}
		if (settings.getOwnershipLevel() != null && !settings.getOwnershipLevel().trim().isEmpty()) {
			Span ownershipSpan = new Span("Level: " + settings.getOwnershipLevel());
			ownershipSpan.getStyle().set("font-size", "0.9em");
			ownershipSpan.getStyle().set("color", "#666");
			roleLayout.add(ownershipSpan);
		}
		// Edit button
		Button editButton = new Button("Edit", VaadinIcon.EDIT.create());
		editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
		editButton.addClickListener(e -> openEditDialog());
		layout.add(companyComponent, roleLayout);
		layout.setFlexGrow(1, roleLayout);
		layout.add(editButton);
		contentDiv.add(layout);
	}

	private void showErrorState(String message) {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setWidthFull();
		layout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
		Icon icon = VaadinIcon.WARNING.create();
		icon.setColor("#F44336");
		icon.setSize("24px");
		Span messageSpan = new Span(message);
		messageSpan.getStyle().set("color", "#F44336");
		layout.add(icon, messageSpan);
		contentDiv.add(layout);
	}

	private void openEditDialog() {
		try {
			LOGGER.debug("Opening company settings dialog for user");
			// Create or get existing settings
			CUserCompanySettings settings = currentUser.getCompanySettings();
			if (settings == null) {
				settings = new CUserCompanySettings();
				settings.setUser(currentUser);
			}
			// Use a dummy company for the dialog (the dialog will handle company selection)
			CCompany dummyCompany = new CCompany();
			Consumer<CUserCompanySettings> onSave = this::onSettingsSaved;
			CCompanyUserSettingsDialog dialog =
					new CCompanyUserSettingsDialog(parentContent, companyService, userService, userCompanySettingsService, settings, dummyCompany, // The
																																					// dialog
																																					// will
																																					// handle
																																					// company
																																					// selection
							onSave);
			dialog.open();
		} catch (Exception e) {
			LOGGER.error("Failed to open company settings dialog: {}", e.getMessage(), e);
			if (notificationService != null) {
				notificationService.showError("Failed to open company settings dialog: " + e.getMessage());
			}
		}
	}

	private void onSettingsSaved(CUserCompanySettings savedSettings) {
		try {
			LOGGER.debug("Saving company settings for user: {}", savedSettings);
			// Update the user's company settings
			currentUser.setCompanySettings(savedSettings);
			// Save the user
			CUser savedUser = userService.save(currentUser);
			// Update the display
			currentUser = savedUser;
			updateDisplay();
			if (notificationService != null) {
				notificationService.showSaveSuccess();
			}
			LOGGER.info("Successfully saved company settings for user: {}", savedUser.getName());
		} catch (Exception e) {
			LOGGER.error("Error saving company settings: {}", e.getMessage(), e);
			if (notificationService != null) {
				notificationService.showSaveError();
			}
		}
	}

	public void setCurrentUser(CUser user) {
		this.currentUser = user;
		updateDisplay();
	}

	public CUser getCurrentUser() { return currentUser; }
}
