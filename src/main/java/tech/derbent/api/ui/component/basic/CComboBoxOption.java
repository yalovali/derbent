package tech.derbent.api.ui.component.basic;

/** Generic option model for color-aware ComboBox entries used by String-backed fields.
 * <p>
 * `name` is the user-visible label, while `value` is the persisted String value in the entity field.
 * </p>
 */
public final class CComboBoxOption {

	private final String color;
	private final String icon;
	private final String name;
	private final String value;

	public CComboBoxOption(final String name, final String color, final String icon) {
		this(name, name, color, icon);
	}

	public CComboBoxOption(final String name, final String value, final String color, final String icon) {
		this.name = name;
		this.value = value;
		this.color = color;
		this.icon = icon;
	}

	public String getColor() { return color; }

	public String getIcon() { return icon; }

	public String getName() { return name; }

	public String getValue() { return value; }

	@Override
	public String toString() { return value; }
}
