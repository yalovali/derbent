package tech.derbent.api.pagequery.domain;

/** CPageViewQueryKeys - Standard filter keys for page/grid master queries.
 * <p>
 * These keys are intentionally string-based so UI components can evolve with dynamic fields
 * while services can still implement strongly-typed handling for well-known keys.
 * </p>
 */
public final class CPageViewQueryKeys {

	public static final String KEY_EPIC = "epic";
	public static final String KEY_FEATURE = "feature";
	public static final String KEY_RESPONSIBLE = "responsible";
	public static final String KEY_SPRINT = "sprint";
	public static final String KEY_USER_STORY = "userStory";

	private CPageViewQueryKeys() {
		// Utility class
	}
}
