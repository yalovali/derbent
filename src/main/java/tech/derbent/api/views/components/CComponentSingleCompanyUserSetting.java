package tech.derbent.api.views.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserCompanySetting;

/** Component for displaying and editing a user's single company setting. This component provides a nice visual layout with icons and colors for the
 * CUserCompanySettings field, allowing users to view and edit their company membership and role through an attractive interface. */
public class CComponentSingleCompanyUserSetting extends CComponentDBEntity<CUser> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentSingleCompanyUserSetting.class);
	private static final long serialVersionUID = 1L;
	private final CCompanyService companyService;
	private Div contentDiv;

	public CComponentSingleCompanyUserSetting(IContentOwner parentContent, ApplicationContext applicationContext) {
		super("Company Setting", parentContent, CUser.class, applicationContext);
		companyService = applicationContext.getBean(CCompanyService.class);
		Check.notNull(companyService, "Company service cannot be null");
		initComponent();
	}

	private void initComponent() {
		setSpacing(true);
		setPadding(true);
		setWidthFull();
		// Create content div that will be updated when data changes
		contentDiv = new Div();
		contentDiv.setWidthFull();
		add(contentDiv);
	}

	private void openEditDialog() {}

	@Override
	public void populateForm() {
		super.populateForm();
		contentDiv.removeAll();
		CUser currentUser = getCurrentEntity();
		try {
			if (currentUser == null) {
				showEmptyState("No user selected");
				return;
			}
			CUserCompanySetting companySetting = currentUser.getCompanySettings();
			if (companySetting == null || companySetting.getCompany() == null) {
				showEmptyState("No company assigned");
			} else {
				showCompanySettings(companySetting);
			}
		} catch (Exception e) {
			LOGGER.error("Error updating company settings display: {}", e.getMessage(), e);
			showErrorState("Error loading company settings");
		}
	}

	private void showCompanySettings(CUserCompanySetting settings) {
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

	@Override
	protected void updatePanelEntityFields() {
		// TODO Auto-generated method stub
	}
}
