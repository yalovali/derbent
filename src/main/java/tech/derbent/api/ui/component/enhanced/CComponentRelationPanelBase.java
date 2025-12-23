package tech.derbent.api.ui.component.enhanced;

import java.util.List;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfProject.service.CAbstractEntityRelationService;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.base.session.service.ISessionService;

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
	protected CAbstractService<MasterClass> entityService;
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	protected CAbstractEntityRelationService<RelationalClass> relationService;

	public CComponentRelationPanelBase(final Class<MasterClass> entityClass, final Class<RelationalClass> relationalClass,
			final CAbstractService<MasterClass> entityService, final CAbstractEntityRelationService<RelationalClass> relationService,
			final ISessionService sessionService) {
		super(entityClass, relationalClass, sessionService);
		Check.notNull(entityService, "Entity service cannot be null - relational component requires a valid entity service");
		Check.notNull(relationService, "Relation service cannot be null - relational component requires a valid relation service");
		this.relationService = relationService;
		this.entityService = entityService;
		setupButtons();
		closePanel();
	}

	/** Helper method to create standard data accessors pattern.
	 * @param settingsSupplier Function to get the list of settings
	 * @param entitySaver      Function to save the entity */
	protected void createStandardDataAccessors(final Supplier<List<RelationalClass>> settingsSupplier, final Runnable entitySaver) {
		final Supplier<List<RelationalClass>> getterFunction = () -> {
			final MasterClass entity = getValue();
			if (entity == null) {
				LOGGER.debug("No current entity available, returning empty list");
				return List.of();
			}
			try {
				final List<RelationalClass> relations = settingsSupplier.get();
				// Note: Lazy fields should be eagerly fetched by repository queries using LEFT JOIN FETCH
				// to avoid LazyInitializationException. Do not call initializeAllFields() here as it
				// happens outside the Hibernate session context.
				LOGGER.debug("Retrieved {} settings for entity: {}", relations.size(), entity.getName());
				return relations;
			} catch (@SuppressWarnings ("unused") final Exception e) {
				LOGGER.error("Error retrieving settings for entity.");
				return List.of();
			}
		};
		final Runnable saveEntityFunction = () -> {
			try {
				final MasterClass entity = getValue();
				Check.notNull(entity, "Current entity cannot be null when saving");
				entitySaver.run();
			} catch (final Exception e) {
				LOGGER.error("Error saving entity.");
				throw e;
			}
		};
		setSettingsAccessors(getterFunction, saveEntityFunction);
	}

	/** Delete the relation - subclasses must implement this. */
	protected abstract void deleteRelation(RelationalClass selected) throws Exception;

	/** Deletes the selected relationship.
	 * @throws Exception */
	protected void deleteSelected() {
		final RelationalClass selected = getSelectedSetting();
		Check.notNull(selected, "Please select a setting to delete.");
		try {
			final String confirmationMessage = getDeleteConfirmationMessage(selected);
			CNotificationService.showConfirmationDialog(confirmationMessage, () -> {
				try {
					deleteRelation(selected);
					populateForm();
					LOGGER.info("Deleted relation: {}", selected);
				} catch (final Exception e) {
					LOGGER.error("Error deleting relation.");
					CNotificationService.showWarning("Failed to delete relation: " + e.getMessage());
				}
			});
		} catch (@SuppressWarnings ("unused") final Exception e) {
			LOGGER.error("Failed to show delete confirmation.");
			CNotificationService.showWarning("Failed to delete relation");
		}
	}

	/** Get delete confirmation message - subclasses can override. */
	protected abstract String getDeleteConfirmationMessage(RelationalClass selected);
	/** Gets display text for various field types - subclasses must implement. */
	protected abstract String getDisplayText(RelationalClass settings, String fieldType);

	@Override
	public CAbstractService<MasterClass> getEntityService() { return entityService; }

	@Override
	public void initPanel() throws Exception {
		try {
			super.initPanel();
			setupDataAccessors();
			openPanel();
		} catch (final Exception e) {
			LOGGER.error("Failed to initialize panel.");
			throw e;
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
					CNotificationService.showWarning("Failed to open add dialog: " + ex.getMessage());
				}
			});
			editButton = new CButton("Edit", VaadinIcon.EDIT.create(), e -> {
				try {
					openEditDialog();
				} catch (final Exception ex) {
					LOGGER.error("Error opening edit dialog: {}", ex.getMessage(), ex);
					CNotificationService.showWarning("Failed to open edit dialog: " + ex.getMessage());
				}
			});
			editButton.setEnabled(false);
			deleteButton = CButton.createError("Delete", VaadinIcon.TRASH.create(), e -> {
				try {
					deleteSelected();
				} catch (final Exception e1) {
					LOGGER.error("Error deleting: {}", e1.getMessage(), e1);
				}
			});
			deleteButton.setEnabled(false);
			final HorizontalLayout buttonLayout = new HorizontalLayout(addButton, editButton, deleteButton);
			buttonLayout.setSpacing(true);
			add(buttonLayout);
		} catch (final Exception e) {
			LOGGER.error("Failed to setup buttons.");
			throw e;
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
		// Add double-click listener to open edit dialog
		grid.addItemDoubleClickListener(e -> {
			if (e.getItem() != null) {
				try {
					openEditDialog();
				} catch (final Exception ex) {
					LOGGER.error("Error opening edit dialog on double-click: {}", ex.getMessage(), ex);
					CNotificationService.showWarning("Failed to open edit dialog: " + ex.getMessage());
				}
			}
		});
	}
}
