package tech.derbent.api.interfaces;

import java.util.Set;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.shared.Registration;

/** Interface for components that allow registration of selection change listeners.
 * <p>
 * Components implementing this interface notify registered listeners when the set of
 * selected items changes. Listeners receive a HasValue.ValueChangeEvent containing
 * the old and new selection sets.
 *
 * @param <T> the type of the selected items
 */
public interface IHasSelection<T> {

    /** Register a selection change listener.
     * @param listener listener to register
     * @return Registration token which can be used to remove the listener
     */
    Registration addValueChangeListener(HasValue.ValueChangeListener<? super HasValue.ValueChangeEvent<Set<T>>> listener);
}
