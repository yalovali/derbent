package tech.derbent.api.domains;

import java.lang.reflect.Method;
import com.vaadin.flow.component.Component;

public interface IEntityDBStatics {

	@SuppressWarnings ("unchecked")
	static Class<? extends Component> viewClassOf(final Class<?> entityClass) {
		try {
			final Method m = entityClass.getMethod("getViewClass");
			final Object out = m.invoke(null); // static ⇒ null target
			if (!(out instanceof final Class<?> cls)) {
				throw new IllegalStateException("getViewClass() must return Class<? extends Component>");
			}
			if (!Component.class.isAssignableFrom(cls)) {
				throw new IllegalStateException("Returned class " + cls.getName() + " is not a Vaadin Component");
			}
			return (Class<? extends Component>) cls;
		} catch (final NoSuchMethodException e) {
			throw new IllegalStateException(
					"Class " + entityClass.getName() + " must declare public static Class<? extends Component> getViewClass()", e);
		} catch (final Exception e) {
			throw new RuntimeException("Failed to obtain view class for " + entityClass.getName(), e);
		}
	}

	/** Optional instance convenience. */
	default Class<? extends Component> viewClass() {
		return viewClassOf(getClass());
	}
}
