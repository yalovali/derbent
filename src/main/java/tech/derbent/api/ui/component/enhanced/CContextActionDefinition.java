package tech.derbent.api.ui.component.enhanced;

import java.util.function.Consumer;
import java.util.function.Predicate;

import com.vaadin.flow.component.icon.VaadinIcon;

import tech.derbent.api.utils.Check;

/**
 * Shared UI action definition that can drive both quick-access buttons and row context-menu items.
 *
 * @param <ContextClass> action context type
 */
public final class CContextActionDefinition<ContextClass> {

	private final Predicate<ContextClass> enabledPredicate;
	private final Consumer<ContextClass> handler;
	private final VaadinIcon icon;
	private final String key;
	private final String label;
	private final Predicate<ContextClass> visiblePredicate;

	private CContextActionDefinition(final String key, final String label, final VaadinIcon icon,
			final Predicate<ContextClass> visiblePredicate, final Predicate<ContextClass> enabledPredicate,
			final Consumer<ContextClass> handler) {
		Check.notBlank(key, "Action key cannot be blank");
		Check.notBlank(label, "Action label cannot be blank");
		this.key = key;
		this.label = label;
		this.icon = icon;
		this.visiblePredicate = visiblePredicate != null ? visiblePredicate : context -> true;
		this.enabledPredicate = enabledPredicate != null ? enabledPredicate : context -> true;
		Check.notNull(handler, "Action handler cannot be null");
		this.handler = handler;
	}

	public static <ContextClass> CContextActionDefinition<ContextClass> of(final String key, final String label,
			final VaadinIcon icon, final Predicate<ContextClass> visiblePredicate, final Predicate<ContextClass> enabledPredicate,
			final Consumer<ContextClass> handler) {
		return new CContextActionDefinition<>(key, label, icon, visiblePredicate, enabledPredicate, handler);
	}

	public void execute(final ContextClass context) {
		handler.accept(context);
	}

	public VaadinIcon getIcon() { return icon; }

	public String getKey() { return key; }

	public String getLabel() { return label; }

	public boolean isEnabled(final ContextClass context) {
		return isVisible(context) && enabledPredicate.test(context);
	}

	public boolean isVisible(final ContextClass context) {
		return visiblePredicate.test(context);
	}
}
