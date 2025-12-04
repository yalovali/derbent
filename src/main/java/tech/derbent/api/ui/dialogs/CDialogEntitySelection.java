package tech.derbent.api.ui.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.enhanced.CComponentEntitySelection;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;

/** CDialogEntitySelection - Dialog for selecting entities from a grid with search/filter capabilities.
 * <p>
 * Extends CDialog to follow the standard dialog pattern in the application. This dialog wraps the CComponentEntitySelection component to provide a
 * modal entity selection experience.
 * <p>
 * Features:
 * <ul>
 * <li>Entity type selection dropdown</li>
 * <li>Grid with colored status display</li>
 * <li>Search toolbar with ID, Name, Description, Status filters</li>
 * <li>Single or multi-select mode</li>
 * <li>Selected item count indicator</li>
 * <li>Reset button for clearing selection</li>
 * <li>Selected items persist across grid filtering</li>
 * <li>Support for already-selected items with two modes: hide or show as pre-selected</li>
 * </ul>
 * @param <EntityClass> The entity type being selected */
public class CDialogEntitySelection<EntityClass extends CEntityDB<?>> extends CDialog {

	/** Mode for handling already selected items - re-exported from CComponentEntitySelection for backward compatibility */
	public static enum AlreadySelectedMode {

		/** Hide already selected items from the available items list */
		HIDE_ALREADY_SELECTED,
		/** Show already selected items in the grid, pre-selected (visually marked) */
		SHOW_AS_SELECTED;

		/** Create from component enum */
		public static AlreadySelectedMode fromComponentMode(final CComponentEntitySelection.AlreadySelectedMode mode) {
			return AlreadySelectedMode.valueOf(mode.name());
		}

		/** Convert to component enum */
		public CComponentEntitySelection.AlreadySelectedMode toComponentMode() {
			return CComponentEntitySelection.AlreadySelectedMode.valueOf(name());
		}
	}

	/** Entity type configuration - re-exported from CComponentEntitySelection for backward compatibility */
	public static class EntityTypeConfig<E extends CEntityDB<E>> {

		private final CComponentEntitySelection.EntityTypeConfig<E> componentConfig;

		public EntityTypeConfig(final String displayName, final Class<E> entityClass,
				final tech.derbent.api.entity.service.CAbstractService<E> service) {
			componentConfig = new CComponentEntitySelection.EntityTypeConfig<>(displayName, entityClass, service);
		}

		public String getDisplayName() { return componentConfig.getDisplayName(); }

		public Class<E> getEntityClass() { return componentConfig.getEntityClass(); }

		public tech.derbent.api.entity.service.CAbstractService<E> getService() { return componentConfig.getService(); }

		/** Get the underlying component configuration */
		CComponentEntitySelection.EntityTypeConfig<E> toComponentConfig() {
			return componentConfig;
		}

		@Override
		public String toString() {
			return componentConfig.toString();
		}
	}

	/** Callback for getting items based on entity type - re-exported from CComponentEntitySelection for backward compatibility */
	@FunctionalInterface
	public interface ItemsProvider<T> {

		List<T> getItems(EntityTypeConfig<?> config);

		/** Convert to component provider */
		@SuppressWarnings ("unchecked")
		default CComponentEntitySelection.ItemsProvider<T> toComponentProvider() {
			return componentConfig -> {
				// Wrap component config back into dialog config for the callback
				// This is a bit awkward but maintains backward compatibility
				@SuppressWarnings ("rawtypes")
				final EntityTypeConfig dialogConfig =
						new EntityTypeConfig(componentConfig.getDisplayName(), componentConfig.getEntityClass(), componentConfig.getService());
				return this.getItems(dialogConfig);
			};
		}
	}

	private static final long serialVersionUID = 1L;
	private final AlreadySelectedMode alreadySelectedMode;
	private final ItemsProvider<EntityClass> alreadySelectedProvider;
	private CButton buttonCancel;
	private CButton buttonSelect;
	private CComponentEntitySelection<EntityClass> componentEntitySelection;
	// Dialog configuration
	private final String dialogTitle;
	private final List<EntityTypeConfig<?>> entityTypes;
	private final ItemsProvider<EntityClass> itemsProvider;
	// Configuration
	private final boolean multiSelect;
	private final Consumer<List<EntityClass>> onSelection;

	/** Creates an entity selection dialog.
	 * @param title         Dialog title
	 * @param entityTypes   Available entity types for selection
	 * @param itemsProvider Provider for loading items based on entity type
	 * @param onSelection   Callback when selection is confirmed
	 * @param multiSelect   True for multi-select, false for single-select */
	public CDialogEntitySelection(final String title, final List<EntityTypeConfig<?>> entityTypes, final ItemsProvider<EntityClass> itemsProvider,
			final Consumer<List<EntityClass>> onSelection, final boolean multiSelect) {
		this(title, entityTypes, itemsProvider, onSelection, multiSelect, null, AlreadySelectedMode.HIDE_ALREADY_SELECTED);
	}

	/** Creates an entity selection dialog with support for already-selected items.
	 * @param title                   Dialog title
	 * @param entityTypes             Available entity types for selection
	 * @param itemsProvider           Provider for loading items based on entity type
	 * @param onSelection             Callback when selection is confirmed
	 * @param multiSelect             True for multi-select, false for single-select
	 * @param alreadySelectedProvider Provider for already-selected items (can be null). Returns items that are already members of the container.
	 * @param alreadySelectedMode     Mode for handling already-selected items */
	public CDialogEntitySelection(final String title, final List<EntityTypeConfig<?>> entityTypes, final ItemsProvider<EntityClass> itemsProvider,
			final Consumer<List<EntityClass>> onSelection, final boolean multiSelect, final ItemsProvider<EntityClass> alreadySelectedProvider,
			final AlreadySelectedMode alreadySelectedMode) {
		super();
		Check.notBlank(title, "Dialog title cannot be blank");
		Check.notEmpty(entityTypes, "Entity types cannot be empty");
		Check.notNull(itemsProvider, "Items provider cannot be null");
		Check.notNull(onSelection, "Selection callback cannot be null");
		Check.notNull(alreadySelectedMode, "Already selected mode cannot be null");
		dialogTitle = title;
		this.entityTypes = entityTypes;
		this.itemsProvider = itemsProvider;
		this.onSelection = onSelection;
		this.multiSelect = multiSelect;
		this.alreadySelectedProvider = alreadySelectedProvider;
		this.alreadySelectedMode = alreadySelectedMode;
		try {
			setupDialog();
			// Override default width from CDialog
			setWidth("900px");
			setHeight("700px");
			setResizable(true);
		} catch (final Exception e) {
			LOGGER.error("Error setting up entity selection dialog", e);
			CNotificationService.showException("Error creating dialog", e);
		}
	}

	/** Factory method for cancel button. */
	protected CButton create_buttonCancel() {
		return CButton.createTertiary("Cancel", VaadinIcon.CLOSE.create(), e -> on_buttonCancel_clicked());
	}

	/** Factory method for select button. */
	protected CButton create_buttonSelect() {
		final CButton button = CButton.createPrimary("Select", VaadinIcon.CHECK.create(), e -> on_buttonSelect_clicked());
		button.setEnabled(false);
		return button;
	}

	/** Returns the list of already selected items.
	 * @return List of already selected items (can be empty, never null) */
	public List<EntityClass> getAlreadySelectedItems() {
		return componentEntitySelection != null ? componentEntitySelection.getAlreadySelectedItems() : new ArrayList<>();
	}

	/** Returns the already selected mode configured for this dialog.
	 * @return The AlreadySelectedMode */
	public AlreadySelectedMode getAlreadySelectedMode() { return alreadySelectedMode; }

	@Override
	public String getDialogTitleString() { return dialogTitle; }

	@Override
	protected Icon getFormIcon() { return VaadinIcon.LIST_SELECT.create(); }

	@Override
	protected String getFormTitleString() { return "Select Items"; }

	/** Returns the currently selected items.
	 * @return List of selected items */
	public List<EntityClass> getSelectedItems() {
		return componentEntitySelection != null ? new ArrayList<>(componentEntitySelection.getSelectedItems()) : new ArrayList<>();
	}

	/** Returns whether the dialog is configured for multi-select.
	 * @return true if multi-select mode */
	public boolean isMultiSelect() { return multiSelect; }

	/** Handle cancel button click. */
	protected void on_buttonCancel_clicked() {
		close();
	}

	/** Handle select button click. */
	protected void on_buttonSelect_clicked() {
		try {
			Check.notNull(componentEntitySelection, "Component entity selection cannot be null");
			final Set<EntityClass> selected = componentEntitySelection.getSelectedItems();
			Check.notNull(selected, "Selected items set cannot be null");
			if (selected.isEmpty()) {
				CNotificationService.showWarning("Please select at least one item");
				return;
			}
			LOGGER.debug("Confirming selection of {} items", selected.size());
			onSelection.accept(new ArrayList<>(selected));
			close();
		} catch (final Exception e) {
			LOGGER.error("Error confirming selection", e);
			CNotificationService.showException("Error confirming selection", e);
		}
	}

	/** Handle selection change from component. */
	protected void on_componentEntitySelection_selectionChanged(final Set<EntityClass> selectedItems) {
		Check.notNull(selectedItems, "Selected items set cannot be null");
		final boolean hasSelection = !selectedItems.isEmpty();
		buttonSelect.setEnabled(hasSelection);
		LOGGER.debug("Selection changed - {} items selected, button enabled: {}", selectedItems.size(), hasSelection);
	}

	@Override
	protected void setupButtons() {
		buttonSelect = create_buttonSelect();
		buttonCancel = create_buttonCancel();
		buttonLayout.add(buttonSelect, buttonCancel);
	}

	@Override
	protected void setupContent() {
		// Convert dialog entity types to component entity types
		final List<CComponentEntitySelection.EntityTypeConfig<?>> componentEntityTypes = new ArrayList<>();
		for (final EntityTypeConfig<?> config : entityTypes) {
			componentEntityTypes.add(config.toComponentConfig());
		}
		// Convert item providers to component providers
		final CComponentEntitySelection.ItemsProvider<EntityClass> componentItemsProvider = itemsProvider.toComponentProvider();
		final CComponentEntitySelection.ItemsProvider<EntityClass> componentAlreadySelectedProvider =
				alreadySelectedProvider != null ? alreadySelectedProvider.toComponentProvider() : null;
		// Convert already selected mode
		final CComponentEntitySelection.AlreadySelectedMode componentMode = alreadySelectedMode.toComponentMode();
		// Create the entity selection component with all configuration
		componentEntitySelection = new CComponentEntitySelection<EntityClass>(componentEntityTypes, componentItemsProvider,
				this::on_componentEntitySelection_selectionChanged, multiSelect, componentAlreadySelectedProvider, componentMode);
		// Add component to main layout
		mainLayout.add(componentEntitySelection);
		mainLayout.setFlexGrow(1, componentEntitySelection);
		// Make the layout fill available space
		mainLayout.setSizeFull();
	}
}
