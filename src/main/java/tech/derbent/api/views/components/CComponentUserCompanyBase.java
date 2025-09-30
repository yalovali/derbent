package tech.derbent.api.views.components;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.domains.CEntityNamed;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.api.ui.dialogs.CConfirmationDialog;
import tech.derbent.api.ui.dialogs.CWarningDialog;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.users.domain.CUserCompanySetting;
import tech.derbent.users.service.CUserCompanySettingsService;

/** Base class for managing user-company relationships in both directions. This class provides common functionality for both user->company and
 * company->user panels. */
public abstract class CComponentUserCompanyBase<MasterClass extends CEntityNamed<MasterClass>, RelationalClass extends CEntityDB<RelationalClass>>
		extends CComponentRelationBase<MasterClass, CUserCompanySetting> {

	private static final long serialVersionUID = 1L;
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	protected CUserCompanySettingsService relationService;

	public CComponentUserCompanyBase(final String title, IContentOwner parentContent, final Class<MasterClass> entityClass,
			final CAbstractService<MasterClass> entityService, final ApplicationContext applicationContext) {
		super(title, parentContent, entityClass, CUserCompanySetting.class, applicationContext);
		// Enforce strict parameter validation - terminate with exceptions on any missing parameter
		Check.notNull(entityService, "Entity service cannot be null - relational component requires a valid entity service");
		relationService = applicationContext.getBean(CUserCompanySettingsService.class);
		setupGrid();
		setupButtons();
		closePanel();
	}

	protected String createDeleteConfirmationMessage(final CUserCompanySetting selected) {
		Check.notNull(selected, "Selected settings cannot be null");
		Check.notNull(selected.getCompany(), "Company cannot be null");
		final String companyName = selected.getCompany().getName();
		return String.format("Are you sure you want to delete the company setting for '%s'? This action cannot be undone.", companyName);
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

	/** Deletes the selected user-company relationship */
	protected void deleteSelected() {
		final CUserCompanySetting selected = grid.asSingleSelect().getValue();
		Check.notNull(selected, "Please select a project setting to delete.");
		try {
			final String confirmationMessage = createDeleteConfirmationMessage(selected);
			new CConfirmationDialog(confirmationMessage, () -> {
				try {
					relationService.deleteByUserCompany(selected.getUser(), selected.getCompany());
					populateForm();
					LOGGER.info("Deleted user project setting: {}", selected);
				} catch (final Exception e) {
					LOGGER.error("Error deleting user project setting: {}", e.getMessage(), e);
					new CWarningDialog("Failed to delete project setting: " + e.getMessage()).open();
				}
			}).open();
		} catch (Exception e) {
			LOGGER.error("Failed to show delete confirmation: {}", e.getMessage(), e);
			new CWarningDialog("Failed to delete project setting").open();
		}
	}

	protected String getDisplayText(final CUserCompanySetting settings, final String type) {
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
	protected abstract void onSettingsSaved(final CUserCompanySetting settings);
	/** Abstract method to open the add dialog
	 * @throws Exception */
	protected abstract void openAddDialog() throws Exception;
	/** Abstract method to open the edit dialog
	 * @throws Exception */
	protected abstract void openEditDialog() throws Exception;

	/** Sets up the action buttons (Add, Edit, Delete) */
	private void setupButtons() {
		try {
			final CButton addButton = CButton.createPrimary("Add", VaadinIcon.PLUS.create(), e -> {
				try {
					openAddDialog();
				} catch (final Exception ex) {
					LOGGER.error("Error opening add dialog: {}", ex.getMessage(), ex);
					new CWarningDialog("Failed to open add dialog: " + ex.getMessage()).open();
				}
			});
			final CButton editButton = new CButton("Edit", VaadinIcon.EDIT.create(), e -> {
				try {
					openEditDialog();
				} catch (final Exception ex) {
					LOGGER.error("Error opening edit dialog: {}", ex.getMessage(), ex);
					new CWarningDialog("Failed to open edit dialog: " + ex.getMessage()).open();
				}
			});
			editButton.setEnabled(false);
			final CButton deleteButton = CButton.createError("Delete", VaadinIcon.TRASH.create(), e -> deleteSelected());
			deleteButton.setEnabled(false);
			grid.addSelectionListener(selection -> {
				final boolean hasSelection = !selection.getAllSelectedItems().isEmpty();
				editButton.setEnabled(hasSelection);
				deleteButton.setEnabled(hasSelection);
			});
			final HorizontalLayout buttonLayout = new HorizontalLayout(addButton, editButton, deleteButton);
			buttonLayout.setSpacing(true);
			add(buttonLayout);
		} catch (Exception e) {
			LOGGER.error("Failed to setup buttons: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to setup buttons", e);
		}
	}

	/** Sets up the grid with enhanced visual styling including colors, avatars and consistent headers. Uses entity decorations with colors and icons
	 * for better visual representation. */
	protected void setupGrid() {
		// Add columns with enhanced styling and colors
		grid.addColumn(CUserCompanySetting::getId).setHeader(createStyledHeader("ID", "#424242")).setAutoWidth(true);
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
		add(grid);
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
		final CUserCompanySetting selected = grid.asSingleSelect().getValue();
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
