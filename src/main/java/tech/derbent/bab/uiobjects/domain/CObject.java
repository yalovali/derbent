package tech.derbent.bab.uiobjects.domain;

import com.google.gson.JsonObject;

public abstract class CObject {
	protected abstract void fromJson(final JsonObject json);

	protected abstract String toJson();
}
