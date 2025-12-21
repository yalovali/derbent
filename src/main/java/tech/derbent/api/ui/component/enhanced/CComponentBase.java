package tech.derbent.api.ui.component.enhanced;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.shared.Registration;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.utils.Check;

/** CComponentBase - Base class for value-bound components that can live in layouts and bind with Vaadin binders.
 * <p>
 * Pattern:
 * <ul>
 * <li>Build UI in the constructor or a dedicated initializer.</li>
 * <li>Override {@link #onValueChanged(Object, Object, boolean)} to refresh UI on value changes.</li>
 * <li>Call {@link #updateValueFromClient(Object)} when UI interactions change the value.</li>
 * </ul>
 * </p>
 * @param <ValueT> bound value type */
public abstract class CComponentBase<ValueT> extends CVerticalLayout implements HasValueAndElement<HasValue.ValueChangeEvent<ValueT>, ValueT> {

	private static final long serialVersionUID = 1L;
	private final List<ValueChangeListener<? super ValueChangeEvent<ValueT>>> valueChangeListeners = new ArrayList<>();
	private ValueT value;
	private boolean readOnly;
	private boolean requiredIndicatorVisible;

	protected CComponentBase() {
		super();
	}

	protected CComponentBase(final boolean padding, final boolean spacing, final boolean margin) {
		super(padding, spacing, margin);
	}

	@Override
	public Registration addValueChangeListener(final ValueChangeListener<? super ValueChangeEvent<ValueT>> listener) {
		Check.notNull(listener, "ValueChangeListener cannot be null");
		valueChangeListeners.add(listener);
		return () -> valueChangeListeners.remove(listener);
	}

	@Override
	public void clear() {
		setValue(null);
	}

	private void fireValueChangeEvent(final ValueT oldValue, final ValueT newValue, final boolean fromClient) {
		if (valueChangeListeners.isEmpty()) {
			return;
		}
		final ValueChangeEvent<ValueT> event = new ValueChangeEvent<ValueT>() {

			private static final long serialVersionUID = 1L;

			@Override
			public HasValue<?, ValueT> getHasValue() { return CComponentBase.this; }

			@Override
			public ValueT getOldValue() { return oldValue; }

			@Override
			public ValueT getValue() { return newValue; }

			@Override
			public boolean isFromClient() { return fromClient; }
		};
		for (final ValueChangeListener<? super ValueChangeEvent<ValueT>> listener : valueChangeListeners) {
			listener.valueChanged(event);
		}
	}

	@Override
	public ValueT getValue() { return value; }

	@Override
	public boolean isEmpty() { return isValueEmpty(value); }

	@Override
	public boolean isReadOnly() { return readOnly; }

	@Override
	public boolean isRequiredIndicatorVisible() { return requiredIndicatorVisible; }

	protected boolean isValueEmpty(final ValueT value) {
		return value == null;
	}

	protected void onReadOnlyChanged(final boolean readOnly) {}

	protected void onRequiredIndicatorVisibleChanged(final boolean requiredIndicatorVisible) {}

	protected void onValueChanged(final ValueT oldValue, final ValueT newValue, final boolean fromClient) {}

	@Override
	public void setReadOnly(final boolean readOnly) {
		this.readOnly = readOnly;
		onReadOnlyChanged(readOnly);
	}

	@Override
	public void setRequiredIndicatorVisible(final boolean requiredIndicatorVisible) {
		this.requiredIndicatorVisible = requiredIndicatorVisible;
		onRequiredIndicatorVisibleChanged(requiredIndicatorVisible);
	}

	@Override
	public void setValue(final ValueT value) {
		updateValue(value, false);
	}

	private void updateValue(final ValueT value, final boolean fromClient) {
		final ValueT oldValue = this.value;
		this.value = value;
		onValueChanged(oldValue, value, fromClient);
		if (!Objects.equals(oldValue, value)) {
			fireValueChangeEvent(oldValue, value, fromClient);
		}
	}

	protected final void updateValueFromClient(final ValueT value) {
		updateValue(value, true);
	}
}
