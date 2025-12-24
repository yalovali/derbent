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
 * @param <EntityClass> bound value type */
public abstract class CComponentBase<EntityClass> extends CVerticalLayout
		implements HasValueAndElement<HasValue.ValueChangeEvent<EntityClass>, EntityClass> {

	private static final long serialVersionUID = 1L;
	private boolean readOnly;
	private boolean requiredIndicatorVisible;
	private EntityClass value;
	private final List<ValueChangeListener<? super ValueChangeEvent<EntityClass>>> valueChangeListeners = new ArrayList<>();

	protected CComponentBase() {
		super();
	}

	@Override
	public Registration addValueChangeListener(final ValueChangeListener<? super ValueChangeEvent<EntityClass>> listener) {
		Check.notNull(listener, "ValueChangeListener cannot be null");
		valueChangeListeners.add(listener);
		return () -> valueChangeListeners.remove(listener);
	}

	@Override
	public void clear() {
		setValue(null);
	}

	private void fireValueChangeEvent(final EntityClass oldValue, final EntityClass newValue, final boolean fromClient) {
		if (valueChangeListeners.isEmpty()) {
			return;
		}
		final ValueChangeEvent<EntityClass> event = new ValueChangeEvent<EntityClass>() {

			private static final long serialVersionUID = 1L;

			@Override
			public HasValue<?, EntityClass> getHasValue() { return CComponentBase.this; }

			@Override
			public EntityClass getOldValue() { return oldValue; }

			@Override
			public EntityClass getValue() { return newValue; }

			@Override
			public boolean isFromClient() { return fromClient; }
		};
		for (final ValueChangeListener<? super ValueChangeEvent<EntityClass>> listener : valueChangeListeners) {
			listener.valueChanged(event);
		}
	}

	@Override
	public EntityClass getValue() { return value; }

	@Override
	public boolean isEmpty() { return value == null; }

	@Override
	public boolean isReadOnly() { return readOnly; }

	@Override
	public boolean isRequiredIndicatorVisible() { return requiredIndicatorVisible; }

	/* onValueChanged() must NEVER call setValue() */
	protected void onValueChanged(@SuppressWarnings ("unused") final EntityClass oldValue, @SuppressWarnings ("unused") final EntityClass newValue,
			@SuppressWarnings ("unused") final boolean fromClient) {
		/*****/
		// Override in subclasses to update UI when value changes, LET IT EMPTY
	}

	@Override
	public void setReadOnly(final boolean readOnly) { this.readOnly = readOnly; }

	@Override
	public void setRequiredIndicatorVisible(final boolean requiredIndicatorVisible) { this.requiredIndicatorVisible = requiredIndicatorVisible; }

	@Override
	public void setValue(final EntityClass value) {
		updateValue(value, false);
	}

	private void updateValue(final EntityClass value1, final boolean fromClient) {
		final EntityClass oldValue = this.value;
		this.value = value1;
		onValueChanged(oldValue, value1, fromClient);
		if (!Objects.equals(oldValue, value1)) {
			fireValueChangeEvent(oldValue, value1, fromClient);
		}
	}

	protected final void updateValueFromClient(EntityClass newValue) {
		updateValue(newValue, true);
	}
}
