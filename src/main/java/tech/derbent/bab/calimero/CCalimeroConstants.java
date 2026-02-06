package tech.derbent.bab.calimero;
/** Central constants for Calimero BAB functionality. Single source of truth for all Calimero-related constants.
 * @author Derbent Framework
 * @since 2026-02-04 */
public final class CCalimeroConstants {

	/** Environment variable name for HTTP settings file path. Matches C++ HTTP_SETTINGS_FILE constant used in CNodeHttp::CNodeHttp() constructor.
	 * When set, Calimero will use this path instead of the default "config/http_server.json". */
	public static final String ENV_HTTP_SETTINGS_FOLDER = "	";
	/** Session key for storing user preference to autostart Calimero Gateway on login. Used across login view, post-login listener, and process
	 * manager. */
	public static final String SESSION_KEY_AUTOSTART_CALIMERO = "autostartCalimero";

	/** Private constructor to prevent instantiation of utility class. */
	private CCalimeroConstants() {
		throw new UnsupportedOperationException("Utility class cannot be instantiated");
	}
}
