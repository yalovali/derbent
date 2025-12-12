package tech.derbent.api.interfaces;

import elemental.json.Json;
import elemental.json.JsonObject;

/** Interface for components that can save and restore their state to/from JSON.
 * <p>
 * This interface enables components to preserve their UI state across operations like grid refresh or page navigation. State is stored in JSON format
 * for easy serialization and deserialization.
 * </p>
 * <p>
 * Components implementing this interface should:
 * <ul>
 * <li>Save relevant UI state (selections, scroll positions, expanded/collapsed states, etc.) in getStateInformation()</li>
 * <li>Restore state from JSON in restoreStateInformation()</li>
 * <li>Clear state when appropriate in clearStateInformation()</li>
 * <li>Recursively save state of child components that also implement this interface</li>
 * </ul>
 * </p> */
public interface IStateOwnerComponent {

	/** Adds a single state value to the JSON object.
	 * @param information The JSON object to add to
	 * @param id          The key/identifier for the state value
	 * @param info        The state value as a string */
	default void addStateInformation(final JsonObject information, final String id, final String info) {
		if (information != null && id != null && info != null) {
			information.put(id, info);
		}
	}

	/** Clears any stored state information. Should be called when the component is disposed or when state should be reset. */
	default void clearStateInformation() {
		// Default: no state to clear
	}

	/** Gets the current state of this component as a JSON object. Components should override this to save their specific state.
	 * @return JsonObject containing the component's state */
	default JsonObject getStateInformation() {
		return Json.createObject();
	}

	/** Inserts state information for a child component into this component's state.
	 * @param id    The identifier for the child component
	 * @param state The child component's state as JSON */
	default void insertStateInformation(final String id, final JsonObject state) {
		// Default: no child state to insert
	}

	/** Restores this component's state from a JSON object. Components should override this to restore their specific state.
	 * @param state The JSON object containing the saved state */
	default void restoreStateInformation(final JsonObject state) {
		// Default: no state to restore
	}
}
