package tech.derbent.api.ui.component.basic;

import com.vaadin.flow.component.combobox.ComboBox;

/**
 * CComboBox - Custom ComboBox component following C-prefix naming convention.
 * <p>
 * This is a thin wrapper around Vaadin's ComboBox that follows the project's
 * naming convention where all custom UI components are prefixed with "C".
 * </p>
 * 
 * <p>
 * <b>Usage:</b> Use this instead of direct Vaadin {@code ComboBox} for consistency
 * with project standards. For entity-based ComboBoxes that need color awareness
 * and icon support, use {@link CColorAwareComboBox} instead.
 * </p>
 * 
 * <p>
 * <b>Example:</b>
 * <pre>
 * CComboBox&lt;String&gt; comboBox = new CComboBox&lt;&gt;("Select Option");
 * comboBox.setItems("Option 1", "Option 2", "Option 3");
 * </pre>
 * </p>
 * 
 * @param <T> the type of items in the ComboBox
 */
public class CComboBox<T> extends ComboBox<T> {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates an empty ComboBox.
	 */
	public CComboBox() {
		super();
	}

	/**
	 * Creates a ComboBox with the given label.
	 * 
	 * @param label the label text to set
	 */
	public CComboBox(final String label) {
		super(label);
	}

	/**
	 * Creates a ComboBox with the given label and items.
	 * 
	 * @param label the label text to set
	 * @param items the items to set
	 */
	@SafeVarargs
	public CComboBox(final String label, final T... items) {
		super(label, items);
	}
}
