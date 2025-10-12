package tech.derbent.api.views.components;

import java.util.List;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.domains.CEntityNamed;
import tech.derbent.api.services.CAbstractEntityRelationService;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.api.ui.dialogs.CConfirmationDialog;
import tech.derbent.api.ui.dialogs.CWarningDialog;
import tech.derbent.api.utils.Check;
import tech.derbent.session.service.ISessionService;

/** Generic base class for relationship panel components. This class provides common functionality for managing bidirectional entity relationships
 * (e.g., User-Project, User-Company) in both directions. It eliminates code duplication between similar relationship management patterns.
 * @param <MasterClass>     The main entity type (e.g., CUser, CProject, CCompany)
 * @param <RelationalClass> The relationship entity type (e.g., CUserProjectSettings, CUserCompanySetting) */
public abstract class CComponentRelationPanelBase<MasterClass extends CEntityNamed<MasterClass>, RelationalClass extends CEntityDB<RelationalClass>>
		extends CComponentRelationBase<MasterClass, RelationalClass> {

	private static final long serialVersionUID = 1L;
	private CButton addButton;
	private CButton deleteButton;
	private CButton editButton;
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	protected CAbstractEntityRelationService<RelationalClass> relationService;

	public CComponentRelationPanelBase(final String title, final Class<MasterClass> entityClass, final Class<RelationalClass> relationalClass,
			final CAbstractService<MasterClass> entityService, final CAbstractEntityRelationService<RelationalClass> relationService,
			ISessionService sessionService, final ApplicationContext applicationContext) {
		super(title, entityClass, relationalClass, sessionService, applicationContext);
		Check.notNull(entityService, "Entity service cannot be null - relational component requires a valid entity service");
		Check.notNull(relationService, "Relation service cannot be null - relational component requires a valid relation service");
		this.relationService = relationService;
		setupButtons();
		closePanel();
	}

	/** Helper method to create standard data accessors pattern.
	 * @param settingsSupplier Function to get the list of settings
	 * @param entitySaver      Function to save the entity */
	protected void createStandardDataAccessors(Supplier<List<RelationalClass>> settingsSupplier, Runnable entitySaver) {
		final Supplier<List<RelationalClass>> getterFunction = () -> {
			final MasterClass entity = getCurrentEntity();
			if (entity == null) {
				LOGGER.debug("No current entity available, returning empty list");
				return List.of();
			}
			try {
				final List<RelationalClass> settings = settingsSupplier.get();
				// Note: Lazy fields should be eagerly fetched by repository queries using LEFT JOIN FETCH
				// to avoid LazyInitializationException. Do not call initializeAllFields() here as it
				// happens outside the Hibernate session context.
				LOGGER.debug("Retrieved {} settings for entity: {}", settings.size(), entity.getName());
				return settings;
			} catch (final Exception e) {
				LOGGER.error("Error retrieving settings for entity: {}", e.getMessage(), e);
				return List.of();
			}
		};
		final Runnable saveEntityFunction = () -> {
			try {
				final MasterClass entity = getCurrentEntity();
				Check.notNull(entity, "Current entity cannot be null when saving");
				entitySaver.run();
			} catch (final Exception e) {
				LOGGER.error("Error saving entity: {}", e.getMessage(), e);
				throw new RuntimeException("Failed to save entity", e);
			}
		};
		setSettingsAccessors(getterFunction, saveEntityFunction);
	}

	/** Creates a consistently styled header with simple color coding. */
	protected com.vaadin.flow.component.html.Span createStyledHeader(String text, String color) {
		com.vaadin.flow.component.html.Span header = new com.vaadin.flow.component.html.Span(text);
		header.getStyle().set("color", color);
		header.getStyle().set("font-weight", "bold");
		header.getStyle().set("font-size", "14px");
		header.getStyle().set("text-transform", "uppercase");
		return header;
	}

	/** Delete the relation - subclasses must implement this. */
	protected abstract void deleteRelation(RelationalClass selected) throws Exception;

	/** Deletes the selected relationship. */
	protected void deleteSelected() {
		final RelationalClass selected = getSelectedSetting();
		Check.notNull(selected, "Please select a setting to delete.");
		try {
			final String confirmationMessage = getDeleteConfirmationMessage(selected);
			new CConfirmationDialog(confirmationMessage, () -> {
				try {
					deleteRelation(selected);
					populateForm();
					LOGGER.info("Deleted relation: {}", selected);
				} catch (final Exception e) {
					LOGGER.error("Error deleting relation: {}", e.getMessage(), e);
					new CWarningDialog("Failed to delete relation: " + e.getMessage()).open();
				}
			}).open();
		} catch (Exception e) {
			LOGGER.error("Failed to show delete confirmation: {}", e.getMessage(), e);
			new CWarningDialog("Failed to delete relation").open();
		}
	}

	/** Get delete confirmation message - subclasses can override. */
	protected abstract String getDeleteConfirmationMessage(RelationalClass selected);
	/** Gets display text for various field types - subclasses must implement. */
	protected abstract String getDisplayText(RelationalClass settings, String fieldType);

	@Override
	public void initPanel() throws Exception {
		try {
			super.initPanel();
			setupDataAccessors();
			openPanel();
		} catch (Exception e) {
			LOGGER.error("Failed to initialize panel: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to initialize panel", e);
		}
	}

	/** Abstract methods that subclasses must implement */
	protected abstract void onSettingsSaved(RelationalClass settings);
	protected abstract void openAddDialog() throws Exception;
	protected abstract void openEditDialog() throws Exception;

	/** Sets up the action buttons (Add, Edit, Delete) with common behavior. */
	private void setupButtons() {
		try {
			addButton = CButton.createPrimary("Add", VaadinIcon.PLUS.create(), e -> {
				try {
					openAddDialog();
				} catch (final Exception ex) {
					LOGGER.error("Error opening add dialog: {}", ex.getMessage(), ex);
					new CWarningDialog("Failed to open add dialog: " + ex.getMessage()).open();
				}
			});
			editButton = new CButton("Edit", VaadinIcon.EDIT.create(), e -> {
				try {
					openEditDialog();
				} catch (final Exception ex) {
					LOGGER.error("Error opening edit dialog: {}", ex.getMessage(), ex);
					new CWarningDialog("Failed to open edit dialog: " + ex.getMessage()).open();
				}
			});
			editButton.setEnabled(false);
			deleteButton = CButton.createError("Delete", VaadinIcon.TRASH.create(), e -> deleteSelected());
			deleteButton.setEnabled(false);
			final HorizontalLayout buttonLayout = new HorizontalLayout(addButton, editButton, deleteButton);
			buttonLayout.setSpacing(true);
			add(buttonLayout);
		} catch (Exception e) {
			LOGGER.error("Failed to setup buttons: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to setup buttons", e);
		}
	}

	/** Abstract method for setting up data accessors - subclasses provide specific implementations */
	protected abstract void setupDataAccessors();

	@Override
	protected void setupGrid(final Grid<RelationalClass> grid) {
		super.setupGrid(grid);
		grid.addSelectionListener(selection -> {
			final boolean hasSelection = !selection.getAllSelectedItems().isEmpty();
			editButton.setEnabled(hasSelection);
			deleteButton.setEnabled(hasSelection);
		});
	}
}
