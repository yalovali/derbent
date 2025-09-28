package tech.derbent.api.views;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.domains.CEntityNamed;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.api.ui.dialogs.CConfirmationDialog;
import tech.derbent.api.ui.dialogs.CWarningDialog;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.components.CButton;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserCompanySettings;
import tech.derbent.users.service.CUserCompanySettingsService;

/** Base class for managing user-company relationships in both directions. This class provides common functionality for both user->company and
 * company->user panels. */
public abstract class CComponentUserCompanyBase<MasterClass extends CEntityNamed<MasterClass>, RelationalClass extends CEntityDB<RelationalClass>>
		extends CPanelRelationalBase<MasterClass, CUserCompanySettings> {

	private static final long serialVersionUID = 1L;
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	protected CUserCompanySettingsService userCompanySettingsService;

	/** Constructor with enforced parameter validation using Check.XXX functions. Ensures all required dependencies are provided and terminates with
	 * exceptions otherwise.
	 * @param title                      Panel title - must not be null or blank
	 * @param parentContent              Parent content owner - must not be null
	 * @param beanValidationBinder       Entity binder - must not be null
	 * @param entityClass                Entity class - must not be null
	 * @param entityService              Entity service - must not be null
	 * @param userCompanySettingsService Relationship service - must not be null
	 * @throws IllegalArgumentException if any required parameter is null or invalid */
	public CComponentUserCompanyBase(final String title, IContentOwner parentContent, final CEnhancedBinder<MasterClass> beanValidationBinder,
			final Class<MasterClass> entityClass, final CAbstractService<MasterClass> entityService,
			final CUserCompanySettingsService userCompanySettingsService) {
		super(title, parentContent, beanValidationBinder, entityClass, entityService, CUserCompanySettings.class);
		// Enforce strict parameter validation - terminate with exceptions on any missing parameter
		Check.notBlank(title, "Panel title cannot be null or blank - relational component requires a valid title");
		Check.notNull(parentContent, "Parent content cannot be null - relational component requires a parent content owner");
		Check.notNull(beanValidationBinder, "Bean validation binder cannot be null - relational component requires a valid binder");
		Check.notNull(entityClass, "Entity class cannot be null - relational component requires a valid entity class");
		Check.notNull(entityService, "Entity service cannot be null - relational component requires a valid entity service");
		Check.notNull(userCompanySettingsService,
				"User company settings service cannot be null - relational component requires a relationship service");
		this.userCompanySettingsService = userCompanySettingsService;
		setupGrid();
		setupButtons();
		closePanel();
	}

	protected String createDeleteConfirmationMessage(final CUserCompanySettings selected) {
		Check.notNull(selected, "Selected settings cannot be null");
		Check.notNull(selected.getCompany(), "Company cannot be null");
		final String companyName = selected.getCompany().getName();
		return String.format("Are you sure you want to delete the company setting for '%s'? This action cannot be undone.", companyName);
	}

	/** Deletes the selected user-company relationship */
	protected void deleteSelected() {
		final CUserCompanySettings selected = grid.asSingleSelect().getValue();
		if (selected == null) {
			new CWarningDialog("Please select a relationship to delete.").open();
			return;
		}
		if ((getSettings == null)) {
			new CWarningDialog("Settings handlers are not available. Please refresh the page.").open();
			return;
		}
		final String confirmMessage = createDeleteConfirmationMessage(selected);
		new CConfirmationDialog(confirmMessage, () -> {
			// Use service layer to properly handle lazy-loaded collections and transactions
			final CCompany company = selected.getCompany();
			final CUser user = selected.getUser();
			try {
				userCompanySettingsService.removeUserFromCompany(user, company);
				LOGGER.debug("Successfully removed user {} from company {}", user, company);
			} catch (Exception e) {
				LOGGER.error("Error removing user from company: {}", e.getMessage(), e);
				new CWarningDialog("Failed to remove user from company: " + e.getMessage()).open();
				return;
			}
			// Refresh the view after successful deletion
			if (saveEntity != null) {
				saveEntity.run();
			}
			refresh();
		}).open();
	}

	protected String getDisplayText(final CUserCompanySettings settings, final String type) {
		Check.notNull(settings, "Settings cannot be null when getting display text");
		try {
			switch (type) {
			case "company":
				Check.notNull(settings.getCompany(), "Company cannot be null");
				return CColorUtils.getDisplayTextFromEntity(settings.getCompany());
			case "user":
				Check.notNull(settings.getUser(), "User cannot be null");
				return CColorUtils.getDisplayTextFromEntity(settings.getUser());
			case "role":
				return settings.getRole() != null ? settings.getRole() : "";
			case "department":
				return settings.getDepartment() != null ? settings.getDepartment() : "";
			case "ownership":
				return settings.getOwnershipLevel() != null ? settings.getOwnershipLevel() : "";
			default:
				return "";
			}
		} catch (Exception e) {
			LOGGER.error("Failed to get display text for type {}: {}", type, e.getMessage(), e);
			return "";
		}
	}

	/** Abstract method to handle settings save events */
	protected abstract void onSettingsSaved(final CUserCompanySettings settings);
	/** Abstract method to open the add dialog
	 * @throws Exception */
	protected abstract void openAddDialog() throws Exception;
	/** Abstract method to open the edit dialog
	 * @throws Exception */
	protected abstract void openEditDialog() throws Exception;

	/** Sets up the action buttons (Add, Edit, Delete) */
	private void setupButtons() {
		final CButton addButton = CButton.createPrimary("Add", VaadinIcon.PLUS.create(), e -> {
			try {
				openAddDialog();
			} catch (final Exception e1) {
				LOGGER.error("Error opening add dialog: {}", e1.getMessage(), e1);
				throw new RuntimeException("Failed to open add dialog", e1);
			}
		});
		final CButton editButton = new CButton("Edit", VaadinIcon.EDIT.create(), e -> {
			try {
				openEditDialog();
			} catch (final Exception e1) {
				LOGGER.error("Error opening edit dialog: {}", e1.getMessage(), e1);
				throw new RuntimeException("Failed to open edit dialog", e1);
			}
		});
		editButton.setEnabled(false);
		final CButton deleteButton = CButton.createError("Delete", VaadinIcon.TRASH.create(), e -> deleteSelected());
		deleteButton.setEnabled(false);
		// Enable/disable edit and delete buttons based on selection
		grid.addSelectionListener(selection -> {
			final boolean hasSelection = !selection.getAllSelectedItems().isEmpty();
			editButton.setEnabled(hasSelection);
			deleteButton.setEnabled(hasSelection);
		});
		final HorizontalLayout buttonLayout = new HorizontalLayout(addButton, editButton, deleteButton);
		buttonLayout.setSpacing(true);
		addToContent(buttonLayout);
	}

	/** Sets up the grid with enhanced visual styling including colors, avatars and consistent headers. Uses entity decorations with colors and icons
	 * for better visual representation. */
	protected void setupGrid() {
		// Add columns with enhanced styling and colors
		grid.addColumn(CUserCompanySettings::getId).setHeader(createStyledHeader("ID", "#424242")).setAutoWidth(true);
		grid.addComponentColumn(settings -> {
			try {
				return CColorUtils.getEntityWithIcon(settings.getUser());
			} catch (Exception e) {
				LOGGER.error("Failed to create user component: {}", e.getMessage(), e);
				return new com.vaadin.flow.component.html.Span(getDisplayText(settings, "user"));
			}
		}).setHeader(createStyledHeader("User", "#1565C0")).setAutoWidth(true);
		grid.addComponentColumn(settings -> {
			try {
				return CColorUtils.getEntityWithIcon(settings.getCompany());
			} catch (Exception e) {
				LOGGER.error("Failed to create company component: {}", e.getMessage(), e);
				return new com.vaadin.flow.component.html.Span(getDisplayText(settings, "company"));
			}
		}).setHeader(createStyledHeader("Company", "#D32F2F")).setAutoWidth(true).setSortable(true);
		grid.addColumn(settings -> getDisplayText(settings, "role")).setHeader(createStyledHeader("Role", "#F57F17")).setAutoWidth(true);
		grid.addColumn(settings -> getDisplayText(settings, "department")).setHeader(createStyledHeader("Department", "#388E3C")).setAutoWidth(true);
		grid.addColumn(settings -> getDisplayText(settings, "ownership")).setHeader(createStyledHeader("Ownership", "#8E24AA")).setAutoWidth(true);
		// Apply consistent grid styling
		grid.setSelectionMode(com.vaadin.flow.component.grid.Grid.SelectionMode.SINGLE);
		grid.getStyle().set("border-radius", "8px");
		grid.getStyle().set("border", "1px solid #E0E0E0");
		addToContent(grid);
	}

	/** Creates a consistently styled header with simple color coding.
	 * @param text  Header text
	 * @param color Header color in hex format
	 * @return Styled header component */
	private com.vaadin.flow.component.html.Span createStyledHeader(String text, String color) {
		com.vaadin.flow.component.html.Span header = new com.vaadin.flow.component.html.Span(text);
		header.getStyle().set("color", color);
		header.getStyle().set("font-weight", "bold");
		header.getStyle().set("font-size", "14px");
		header.getStyle().set("text-transform", "uppercase");
		return header;
	}

	@Override
	protected void updatePanelEntityFields() {
		// No specific entity fields to update for this component
		setEntityFields(List.of());
	}

	/** Common validation helper for entity selection in grid. Shows warning dialog and returns false if no item is selected.
	 * @param actionName Name of the action for error message (e.g., "edit", "delete")
	 * @return true if an item is selected, false otherwise */
	protected boolean validateGridSelection(final String actionName) {
		final CUserCompanySettings selected = grid.asSingleSelect().getValue();
		if (selected == null) {
			final String message = String.format("Please select an item to %s.", actionName);
			new CWarningDialog(message).open();
			return false;
		}
		return true;
	}

	/** Common validation helper for service availability. Shows warning dialog and returns false if service is unavailable.
	 * @param serviceName Name of the service for error message
	 * @return true if service is available, false otherwise */
	protected boolean validateServiceAvailability(final String serviceName) {
		if (entityService == null) {
			new CWarningDialog(serviceName + " service is not available. Please try again later.").open();
			return false;
		}
		return true;
	}
}
