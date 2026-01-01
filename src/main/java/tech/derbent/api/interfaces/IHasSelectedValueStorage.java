package tech.derbent.api.interfaces;

import java.util.Optional;

/**
 * IHasSelectedValueStorage - Interface for components that need session-based value persistence.
 * <p>
 * Components that implement this interface can store and restore their current selection values
 * across refreshes and page reloads. The values are stored in the user's VaadinSession using
 * a unique identifier.
 * </p>
 * <h3>Use Cases:</h3>
 * <ul>
 * <li>ComboBox selections in filter toolbars (entity type, status, etc.)</li>
 * <li>TextField filter values that should persist during grid refreshes</li>
 * <li>Any UI component where user selections should be maintained across component refreshes</li>
 * </ul>
 * <h3>Usage Pattern:</h3>
 * <pre>
 * public class CComponentEntitySelection implements IHasSelectedValueStorage {
 *     private ComboBox&lt;EntityTypeConfig&lt;?&gt;&gt; comboBoxEntityType;
 *
 *     &#64;Override
 *     public String getStorageId() {
 *         return "entitySelection_" + getId().orElse("default");
 *     }
 *
 *     &#64;Override
 *     public void saveCurrentValue() {
 *         EntityTypeConfig&lt;?&gt; currentValue = comboBoxEntityType.getValue();
 *         if (currentValue != null) {
 *             CValueStorageService.storeValue(getStorageId(), currentValue.getDisplayName());
 *         }
 *     }
 *
 *     &#64;Override
 *     public void restoreCurrentValue() {
 *         Optional&lt;String&gt; storedValue = CValueStorageService.retrieveValue(getStorageId());
 *         if (storedValue.isPresent()) {
 *             // Find matching entity type and set it
 *             entityTypes.stream()
 *                 .filter(config -&gt; config.getDisplayName().equals(storedValue.get()))
 *                 .findFirst()
 *                 .ifPresent(comboBoxEntityType::setValue);
 *         }
 *     }
 * }
 * </pre>
 * 
 * @see tech.derbent.api.services.CValueStorageService
 */
public interface IHasSelectedValueStorage {

	/**
	 * Gets the unique storage identifier for this component's values.
	 * <p>
	 * The storage ID should be unique within the session to avoid conflicts between
	 * different components. Best practice is to use a combination of component type
	 * and component ID:
	 * <ul>
	 * <li>"entitySelection_backlog" - for backlog entity selection</li>
	 * <li>"statusFilter_kanbanColumn1" - for status filter in kanban column 1</li>
	 * <li>"nameFilter_projectGrid" - for name filter in project grid</li>
	 * </ul>
	 * 
	 * @return The unique storage identifier (never null or blank)
	 */
	String getStorageId();

	/**
	 * Restores the component's value from session storage.
	 * <p>
	 * This method should be called:
	 * <ul>
	 * <li>After component initialization</li>
	 * <li>Before loading data for the first time</li>
	 * <li>When the component needs to recover its previous state</li>
	 * </ul>
	 * <p>
	 * The implementation should:
	 * <ol>
	 * <li>Retrieve the stored value using the storage ID</li>
	 * <li>Validate the value is still valid (e.g., option still exists in dropdown)</li>
	 * <li>Set the component's value (e.g., ComboBox.setValue())</li>
	 * <li>Handle the case where no stored value exists (use default)</li>
	 * </ol>
	 */
	void restoreCurrentValue();

	/**
	 * Saves the component's current value to session storage.
	 * <p>
	 * This method should be called:
	 * <ul>
	 * <li>When the component's value changes (value change listener)</li>
	 * <li>Before the component is refreshed or reloaded</li>
	 * <li>Before navigation away from the page</li>
	 * </ul>
	 * <p>
	 * The implementation should:
	 * <ol>
	 * <li>Get the current value from the component</li>
	 * <li>Convert it to a storable format (String, ID, etc.)</li>
	 * <li>Store it using the storage ID</li>
	 * <li>Handle null values appropriately</li>
	 * </ol>
	 */
	void saveCurrentValue();

	/**
	 * Convenience method to save and restore value in a single operation.
	 * <p>
	 * Default implementation saves current value first, then restores it.
	 * This can be useful when you want to preserve the value across a refresh
	 * operation.
	 */
	default void preserveValue() {
		saveCurrentValue();
		restoreCurrentValue();
	}
}
