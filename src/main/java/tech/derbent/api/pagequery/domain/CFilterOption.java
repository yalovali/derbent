package tech.derbent.api.pagequery.domain;

import org.jspecify.annotations.Nullable;

import tech.derbent.api.utils.Check;

/** CFilterOption - Wrapper for ComboBox items that need an explicit "Select All" option.
 *
 * @param <T> the underlying value type
 */
public final class CFilterOption<T> {

	public enum EType {
		ALL, NONE, VALUE;
	}

	private static final CFilterOption<?> SELECT_ALL = new CFilterOption<>(null, EType.ALL, "-- Select All --");

	public static <T> CFilterOption<T> selectAll() {
		@SuppressWarnings ("unchecked")
		final CFilterOption<T> casted = (CFilterOption<T>) SELECT_ALL;
		return casted;
	}

	public static <T> CFilterOption<T> none(final String label) {
		Check.notBlank(label, "label cannot be blank");
		return new CFilterOption<>(null, EType.NONE, label);
	}

	public static <T> CFilterOption<T> of(final T value) {
		Check.notNull(value, "value cannot be null");
		return of(value, String.valueOf(value));
	}

	public static <T> CFilterOption<T> of(final T value, final String label) {
		Check.notNull(value, "value cannot be null");
		Check.notBlank(label, "label cannot be blank");
		return new CFilterOption<>(value, EType.VALUE, label);
	}

	private final @Nullable T value;
	private final String label;
	private final EType type;

	private CFilterOption(final @Nullable T value, final EType type, final String label) {
		this.value = value;
		this.type = type;
		this.label = label;
	}

	public @Nullable T getValue() { return value; }

	public String getLabel() { return label; }

	public boolean isNone() { return type == EType.NONE; }

	public boolean isSelectAll() { return type == EType.ALL; }

	public EType getType() { return type; }

	@Override
	public String toString() {
		return label;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof final CFilterOption<?> other)) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		return value != null ? value.equals(other.value) : other.value == null;
	}

	@Override
	public int hashCode() {
		int result = type.hashCode();
		result = 31 * result + (value != null ? value.hashCode() : 0);
		return result;
	}
}
