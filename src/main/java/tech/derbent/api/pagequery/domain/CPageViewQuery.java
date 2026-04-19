package tech.derbent.api.pagequery.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import tech.derbent.api.utils.Check;

/** CPageViewQuery - Unified query container for page/grid master lists.
 * <p>
 * This is intentionally lightweight and string-keyed to support future dynamic field filters.
 * Services can interpret well-known keys (see {@link CPageViewQueryKeys}) and ignore unknown keys.
 * </p>
 */
public final class CPageViewQuery {

	private String searchText = "";
	private final Map<String, Object> filters = new HashMap<>();

	public CPageViewQuery() {
		// Default
	}

	public @Nullable Object getFilterValue(final String key) {
		Check.notBlank(key, "key cannot be blank");
		return filters.get(key);
	}

	public Map<String, Object> getFilters() {
		return Collections.unmodifiableMap(filters);
	}

	public String getSearchText() { return searchText; }

	public boolean hasAnyFilter() { return !filters.isEmpty() || !searchText.isBlank(); }

	public CPageViewQuery clearFilters() {
		filters.clear();
		return this;
	}

	public CPageViewQuery putFilter(final String key, final @Nullable Object value) {
		Check.notBlank(key, "key cannot be blank");
		if (value == null) {
			filters.remove(key);
			return this;
		}
		filters.put(key, value);
		return this;
	}

	public CPageViewQuery setSearchText(final @Nullable String searchText) {
		this.searchText = searchText != null ? searchText : "";
		return this;
	}

	@Override
	public String toString() {
		return "CPageViewQuery{searchText='%s', filters=%s}".formatted(searchText, filters);
	}
}
