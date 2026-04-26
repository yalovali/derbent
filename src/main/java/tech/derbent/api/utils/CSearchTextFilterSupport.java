package tech.derbent.api.utils;

/**
 * Shared helper for free-text filtering.
 *
 * <p>Multiple boards (Gnnt + sprint planning) need the same null-safe, trim/lowercase match logic.
 * Centralising this avoids subtle divergence (e.g., trimming rules) between toolbars and services.</p>
 */
public final class CSearchTextFilterSupport {

	private CSearchTextFilterSupport() {
		// Utility class.
	}

	public static boolean matches(final String searchText, final String... candidates) {
		if (searchText == null || searchText.isBlank()) {
			return true;
		}
		final String needle = searchText.trim().toLowerCase();
		if (needle.isBlank()) {
			return true;
		}
		if (candidates == null || candidates.length == 0) {
			return false;
		}
		for (final String candidate : candidates) {
			if (candidate != null && candidate.toLowerCase().contains(needle)) {
				return true;
			}
		}
		return false;
	}
}
