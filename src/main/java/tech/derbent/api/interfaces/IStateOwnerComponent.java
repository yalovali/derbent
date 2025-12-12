package tech.derbent.api.interfaces;

import elemental.json.Json;

public interface IStateOwnerComponent {

	default void addStateInformation(Json information, String id, String info) {
		information.put(id, info);
	}

	default void clearStateInformation() {}

	default Json getStateInformation() { return new Json(); }

	default void insertStateInformation(String id, Json state) {}

	default void restoreStateInformation(Json state) {}
}
