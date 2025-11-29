package tech.derbent.api.interfaces;

import java.util.List;
import java.util.function.Consumer;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.ui.dialogs.CDialogEntitySelection;

/** Interface for components that support entity selection through the CDialogEntitySelection dialog. This interface defines methods for providing
 * available items, already-selected items, entity type configurations, and selection handling.
 * <p>
 * Implementing this interface allows components to leverage the generic entity selection dialog with consistent behavior for:
 * <ul>
 * <li>Loading available items for selection</li>
 * <li>Determining which items are already selected (for hide/show modes)</li>
 * <li>Defining supported entity types</li>
 * <li>Handling item selection callbacks</li>
 * </ul>
 * @param <ItemType> The type of items being selected (e.g., CProjectItem) */
public interface IEntitySelectionDialogSupport<ItemType extends CEntityDB<?>> {

	/** Returns the list of entity type configurations supported by this component.
	 * @return List of EntityTypeConfig objects defining available entity types */
	List<CDialogEntitySelection.EntityTypeConfig<?>> getDialogEntityTypes();

	/** Returns a provider for available items based on entity type.
	 * @return ItemsProvider that returns items available for selection */
	CDialogEntitySelection.ItemsProvider<ItemType> getItemsProvider();

	/** Returns a provider for already-selected items based on entity type. This is used by the dialog to either hide already-selected items or
	 * pre-select them, depending on the configured mode.
	 * @return ItemsProvider that returns already-selected items, or null if not applicable */
	default CDialogEntitySelection.ItemsProvider<ItemType> getAlreadySelectedProvider() { return null; }

	/** Returns the mode for handling already-selected items in the dialog.
	 * @return AlreadySelectedMode to use (defaults to HIDE_ALREADY_SELECTED) */
	default CDialogEntitySelection.AlreadySelectedMode getAlreadySelectedMode() {
		return CDialogEntitySelection.AlreadySelectedMode.HIDE_ALREADY_SELECTED;
	}

	/** Returns a consumer that handles the selection of items from the dialog.
	 * @return Consumer that processes the list of selected items */
	Consumer<List<ItemType>> getSelectionHandler();

	/** Returns the title for the entity selection dialog.
	 * @return Dialog title string */
	default String getDialogTitle() { return "Select Items"; }

	/** Returns whether the dialog should support multi-select.
	 * @return true for multi-select, false for single-select */
	default boolean isMultiSelect() { return true; }
}
