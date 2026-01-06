package tech.derbent.api.interfaces;

/**
 * IHasSelectedValueStorage - Interface for components that need session-based value persistence.
 * <p>
 * Components that implement this interface can store and restore their current selection values
 * across refreshes and page reloads. The values are stored in the user's VaadinSession using
 * a unique identifier.
 * </p>
 * 
 * <h3>When to Use This Interface:</h3>
 * <p>
 * <b>Use IHasSelectedValueStorage when:</b>
 * <ul>
 * <li>You need manual control over save/restore logic</li>
 * <li>You have complex state that can't be reduced to a single value</li>
 * <li>You need custom timing for save/restore operations</li>
 * </ul>
 * </p>
 * <p>
 * <b>Use {@link tech.derbent.api.utils.CValueStorageHelper} instead when:</b>
 * <ul>
 * <li>You have a standard Vaadin component (ComboBox, TextField)</li>
 * <li>You want automatic save on change, restore on attach</li>
 * <li>You can convert your value to/from a String</li>
 * </ul>
 * <b>Note:</b> Most filter components should use CValueStorageHelper rather than implementing
 * this interface directly, as it provides automatic persistence with less code.
 * </p>
 * 
 * <h3>Use Cases for This Interface:</h3>
 * <ul>
 * <li>Complex components with multiple sub-components that need coordinated persistence</li>
 * <li>Components that need to persist non-standard state (e.g., expanded tree nodes)</li>
 * <li>Components where you need to customize when persistence happens</li>
 * </ul>
 * 
 * <h3>Usage Pattern (Manual Implementation):</h3>
 * <pre>
 * public class CComplexComponent implements IHasSelectedValueStorage {
 *     &#64;Autowired
 *     private ISessionService sessionService;
 *     
 *     private ComboBox&lt;EntityType&gt; comboBoxEntityType;
 *     private TextField textFieldFilter;
 *
 *     &#64;Override
 *     public String getStorageId() {
 *         return "complexComponent_" + getId().orElse("default");
 *     }
 *
 *     &#64;Override
 *     public void saveCurrentValue() {
 *         // Save all component state as JSON or delimited string
 *         EntityType type = comboBoxEntityType.getValue();
 *         String filter = textFieldFilter.getValue();
 *         if (type != null) {
 *             String state = type.getName() + "|" + filter;
 *             sessionService.setSessionValue(getStorageId(), state);
 *         }
 *     }
 *
 *     &#64;Override
 *     public void restoreCurrentValue() {
 *         Optional&lt;String&gt; storedValue = sessionService.getSessionValue(getStorageId());
 *         if (storedValue.isPresent()) {
 *             String[] parts = storedValue.get().split("\\|");
 *             // Restore each component's state
 *             findEntityType(parts[0]).ifPresent(comboBoxEntityType::setValue);
 *             if (parts.length &gt; 1) {
 *                 textFieldFilter.setValue(parts[1]);
 *             }
 *         }
 *     }
 * }
 * </pre>
 * 
 * <h3>Usage Pattern (Using CValueStorageHelper - Preferred):</h3>
 * <pre>
 * public class CSimpleFilter extends CAbstractFilterComponent&lt;EntityType&gt; {
 *     private ComboBox&lt;EntityType&gt; comboBox;
 *     
 *     &#64;Override
 *     public void enableValuePersistence(String storageId) {
 *         // Much simpler - no need to implement IHasSelectedValueStorage
 *         CValueStorageHelper.valuePersist_enable(
 *             comboBox,
 *             storageId + "_entityType",
 *             name -&gt; findEntityType(name).orElse(null)
 *         );
 *     }
 * }
 * </pre>
 * 
 * @see tech.derbent.api.utils.CValueStorageHelper
 * @see tech.derbent.base.session.service.ISessionService
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
	String getValuePersistId();

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
