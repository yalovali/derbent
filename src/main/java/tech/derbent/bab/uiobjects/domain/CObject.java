package tech.derbent.bab.uiobjects.domain;

import java.io.Serializable;
import com.google.gson.JsonObject;

/** CObject - Base class for BAB UI data objects with JSON serialization support.
 * 
 * All CObject subclasses are Serializable for Vaadin session storage and cluster support.
 * Provides bidirectional JSON conversion for Calimero HTTP API communication. */
public abstract class CObject implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/** Parse JSON from Calimero API response into this object.
	 * @param json JSON object from Calimero HTTP API */
	protected abstract void fromJson(final JsonObject json);

	/** Convert this object to JSON for Calimero API requests.
	 * @return JSON string representation for API communication */
	protected abstract String toJson();
}
