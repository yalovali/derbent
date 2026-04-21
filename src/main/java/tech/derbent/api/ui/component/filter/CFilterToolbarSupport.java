package tech.derbent.api.ui.component.filter;

import java.util.function.Consumer;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.value.ValueChangeMode;
import tech.derbent.api.ui.component.basic.CTextField;
import tech.derbent.api.utils.Check;

public final class CFilterToolbarSupport {

	public static final int DEFAULT_SEARCH_TIMEOUT_MS = 300;

	private CFilterToolbarSupport() {}

	public static void configureWrappingToolbar(final HorizontalLayout toolbar, final String className) {
		Check.notNull(toolbar, "toolbar cannot be null");
		toolbar.setSpacing(false);
		toolbar.setPadding(false);
		toolbar.setAlignItems(HorizontalLayout.Alignment.START);
		toolbar.setWidthFull();
		toolbar.getStyle().set("gap", "var(--lumo-space-s)");
		toolbar.getStyle().set("flex-wrap", "wrap");
		toolbar.getStyle().set("min-width", "0");
		if (className != null && !className.isBlank()) {
			toolbar.addClassName(className);
		}
	}

	public static CTextField createSearchField(final String label, final String placeholder, final VaadinIcon prefixIcon, final String width,
			final ValueChangeMode valueChangeMode, final int timeoutMs, final Consumer<String> onChange) {
		final CTextField field = label != null ? new CTextField(label) : new CTextField();
		field.setPlaceholder(placeholder);
		if (prefixIcon != null) {
			field.setPrefixComponent(prefixIcon.create());
		}
		field.setClearButtonVisible(true);
		field.setValueChangeMode(valueChangeMode != null ? valueChangeMode : ValueChangeMode.EAGER);
		field.setValueChangeTimeout(timeoutMs > 0 ? timeoutMs : DEFAULT_SEARCH_TIMEOUT_MS);
		if (width != null && !width.isBlank()) {
			field.setWidth(width);
		}
		if (onChange != null) {
			field.addValueChangeListener(event -> onChange.accept(event.getValue()));
		}
		return field;
	}
}
