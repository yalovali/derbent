package tech.derbent.companies.view;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.domains.CInterfaceIconSet;
import tech.derbent.abstracts.views.CAbstractNamedEntityPage;
import tech.derbent.abstracts.views.CAccordionDBEntity;
import tech.derbent.abstracts.views.CButton;
import tech.derbent.base.ui.dialogs.CWarningDialog;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.session.service.CSessionService;

/**
 * CCompanyView - View for managing companies Layer: View (MVC) Provides CRUD operations
 * for CCompany entities with master-detail layout
 */
@Route ("ccompanyview/:company_id?/:action?(edit)")
@PageTitle ("Company Master Detail")
@Menu (
	order = 3.4, icon = "class:tech.derbent.companies.view.CCompanyView",
	title = "Settings.Companies"
)
@PermitAll // When security is enabled, allow all authenticated users
public class CCompanyView extends CAbstractNamedEntityPage<CCompany>
	implements CInterfaceIconSet {

	private static final long serialVersionUID = 1L;

	public static String getIconColorCode() {
		return CCompany.getIconColorCode(); // Use the static method from CCompany
	}

	public static String getIconFilename() { return CCompany.getIconFilename(); }

	/**
	 * Navigates to the company view
	 * @param companyId the ID of the company to view (null for list view)
	 */
	public static void navigateToCompany(final Long companyId) {

		if (companyId == null) {
			UI.getCurrent().navigate(CCompanyView.class);
		}
		else {
			UI.getCurrent().navigate("ccompanyview/" + companyId);
		}
	}

	/**
	 * Navigates to the company edit view
	 * @param companyId the ID of the company to edit
	 */
	public static void navigateToCompanyEdit(final Long companyId) {

		if (companyId != null) {
			UI.getCurrent().navigate("ccompanyview/" + companyId + "/edit");
		}
	}

	private final String ENTITY_ID_FIELD = "company_id";

	private final String ENTITY_ROUTE_TEMPLATE_EDIT = "ccompanyview/%s/edit";

	/**
	 * Constructor for CCompanyView Annotated with @Autowired to let Spring inject
	 * dependencies
	 * @param entityService the CCompanyService instance
	 */
	@Autowired
	public CCompanyView(final CCompanyService entityService,
		final CSessionService sessionService) {
		super(CCompany.class, entityService, sessionService);
		LOGGER.info("CCompanyView constructor called with entityService: {}",
			entityService.getClass().getSimpleName());
		addClassNames("companies-view");
	}

	@Override
	protected void createDetailsLayout() {
		CAccordionDBEntity<CCompany> panel;
		panel = new CPanelCompanyDescription(getCurrentEntity(), getBinder(),
			(CCompanyService) entityService);
		addAccordionPanel(panel);
		panel = new CPanelCompanySystemStatus(getCurrentEntity(), getBinder(),
			(CCompanyService) entityService);
		addAccordionPanel(panel);
		panel = new CPanelCompanyUsers(getCurrentEntity(), getBinder(),
			(CCompanyService) entityService);
		addAccordionPanel(panel);
		panel = new CPanelCompanyContactDetails(getCurrentEntity(), getBinder(),
			(CCompanyService) entityService);
		// final var formLayout = CEntityFormBuilder.buildForm(CCompany.class,
		// getBinder()); getBaseDetailsLayout().add(formLayout);
	}

	@Override
	protected Div createDetailsTabLeftContent() {
		// Create custom tab content for companies view
		final Div detailsTabLabel = new Div();
		detailsTabLabel.setText("Company Details");
		detailsTabLabel.setClassName("details-tab-label");
		return detailsTabLabel;
	}

	@Override
	protected void createGridForEntity() {
		LOGGER.info("Creating grid for companies with appropriate field widths");
		// Add columns for key company information
		grid.addShortTextColumn(CCompany::getName, "Company Name", "name");
		grid.addLongTextColumn(CCompany::getDescription, "Description", "description");
		grid.addShortTextColumn(CCompany::getAddress, "Address", "address");
		grid.addShortTextColumn(CCompany::getPhone, "Phone", "phone");
		grid.addShortTextColumn(CCompany::getEmail, "Email", "email");
		grid.addBooleanColumn(CCompany::isEnabled, "Status", "Active", "Inactive");
		grid.addShortTextColumn(CCompany::getWebsite, "Website", "website");
	}

	@Override
	protected CButton createSaveButton(final String buttonText) {
		LOGGER.debug("createSaveButton called with buttonText: {}", buttonText);
		return CButton.createPrimary(buttonText, event -> {
			LOGGER.debug("Save button clicked for company");

			try {

				// Validate current entity selection
				if (getCurrentEntity() == null) {
					LOGGER.warn("Save attempted with no entity selected");
					new CWarningDialog("Please select a company to save.").open();
					return;
				}

				// Additional validation for company-specific rules
				if (!validateCompanyData()) {
					LOGGER.warn("Company data validation failed");
					new CWarningDialog(
						"Please check the company data and fix any validation errors.")
						.open();
					return;
				}
				// Write form data to entity and save
				getBinder().writeBean(getCurrentEntity());
				entityService.save(getCurrentEntity());
				// Clear form and refresh grid
				clearForm();
				refreshGrid();
				LOGGER.info("Company saved successfully: {}",
					getCurrentEntity().getName());
				Notification.show("Company saved successfully!", 3000,
					Notification.Position.TOP_CENTER);
				// Navigate back to list view
				UI.getCurrent().navigate(getClass());
			} catch (final ValidationException e) {
				LOGGER.error("Validation error while saving company", e);
				new CWarningDialog(
					"Failed to save the data. Please check that all required fields are filled and values are valid.")
					.open();
			} catch (final Exception e) {
				LOGGER.error("Error saving company", e);
				new CWarningDialog(
					"An unexpected error occurred while saving. Please try again.")
					.open();
			}
		});
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	protected String getEntityRouteTemplateEdit() { return ENTITY_ROUTE_TEMPLATE_EDIT; }

	/**
	 * Validates email format using simple regex
	 * @param email the email to validate
	 * @return true if email format is valid
	 */
	private boolean isValidEmail(final String email) {

		if ((email == null) || email.trim().isEmpty()) {
			return true; // Empty email is valid (optional field)
		}
		final String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
		return email.matches(emailRegex);
	}

	/**
	 * Validates URL format
	 * @param url the URL to validate
	 * @return true if URL format is valid
	 */
	private boolean isValidUrl(final String url) {

		if ((url == null) || url.trim().isEmpty()) {
			return true; // Empty URL is valid (optional field)
		}
		final String urlRegex = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$";
		return url.matches(urlRegex);
	}

	@Override
	protected void setupToolbar() {
		// Empty implementation as required by abstract parent
	}

	/**
	 * Shows an error notification to the user
	 * @param message the error message to display
	 */
	private void showErrorNotification(final String message) {
		LOGGER.debug("showErrorNotification called with message: {}", message);

		if ((message == null) || message.trim().isEmpty()) {
			LOGGER.warn("Attempt to show error notification with null or empty message");
			return;
		}
		final Notification notification =
			Notification.show(message, 5000, Notification.Position.TOP_CENTER);
		notification.addThemeVariants(
			com.vaadin.flow.component.notification.NotificationVariant.LUMO_ERROR);
	}

	/**
	 * Validates company-specific business rules
	 * @return true if validation passes, false otherwise
	 */
	private boolean validateCompanyData() {
		LOGGER.debug("validateCompanyData called for entity: {}",
			getCurrentEntity() != null ? getCurrentEntity().getId() : "null");

		if (getCurrentEntity() == null) {
			LOGGER.warn("Cannot validate null entity");
			return false;
		}

		// Check if company name is provided
		if ((getCurrentEntity().getName() == null)
			|| getCurrentEntity().getName().trim().isEmpty()) {
			LOGGER.warn("Company name is required but not provided");
			showErrorNotification("Company name is required");
			return false;
		}
		// Check name uniqueness
		final CCompanyService companyService = (CCompanyService) entityService;

		if (!companyService.isNameUnique(getCurrentEntity().getName(),
			getCurrentEntity().getId())) {
			LOGGER.warn("Company name is not unique: {}", getCurrentEntity().getName());
			showErrorNotification(
				"Company name already exists. Please choose a different name.");
			return false;
		}

		// Validate email format if provided
		if ((getCurrentEntity().getEmail() != null)
			&& !getCurrentEntity().getEmail().trim().isEmpty()) {

			if (!isValidEmail(getCurrentEntity().getEmail())) {
				LOGGER.warn("Invalid email format: {}", getCurrentEntity().getEmail());
				showErrorNotification("Please enter a valid email address");
				return false;
			}
		}

		// Validate website URL format if provided
		if ((getCurrentEntity().getWebsite() != null)
			&& !getCurrentEntity().getWebsite().trim().isEmpty()) {

			if (!isValidUrl(getCurrentEntity().getWebsite())) {
				LOGGER.warn("Invalid website URL format: {}",
					getCurrentEntity().getWebsite());
				showErrorNotification(
					"Please enter a valid website URL (e.g., https://example.com)");
				return false;
			}
		}
		LOGGER.debug("Company data validation passed");
		return true;
	}
}