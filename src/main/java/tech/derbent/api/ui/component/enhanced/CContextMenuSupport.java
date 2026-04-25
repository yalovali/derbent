package tech.derbent.api.ui.component.enhanced;

import java.util.function.Supplier;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;

import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.utils.Check;

/**
 * Shared menu helper for component and grid context actions.
 *
 * <p>Boards reuse the same action definitions in quick access buttons, row context menus, and
 * component-level right-click menus. This helper keeps icon rendering and visible/enabled state
 * handling consistent across those different menu hosts.</p>
 */
public final class CContextMenuSupport {

	private CContextMenuSupport() {}

	public static Component createActionContent(final CContextActionDefinition<?> action) {
		Check.notNull(action, "action cannot be null");
		final CHorizontalLayout content = new CHorizontalLayout();
		content.setPadding(false);
		content.setSpacing(false);
		content.setAlignItems(CHorizontalLayout.Alignment.CENTER);
		content.getStyle().set("gap", "var(--lumo-space-s)");
		if (action.getIcon() != null) {
			final Icon icon = action.getIcon().create();
			icon.setSize("var(--lumo-icon-size-s)");
			content.add(icon);
		}
		content.add(new Span(action.getLabel()));
		return content;
	}

	public static <ContextClass> MenuItem registerComponentAction(final ContextMenu contextMenu,
			final CContextActionDefinition<ContextClass> action, final Supplier<ContextClass> contextSupplier) {
		Check.notNull(contextMenu, "contextMenu cannot be null");
		Check.notNull(action, "action cannot be null");
		Check.notNull(contextSupplier, "contextSupplier cannot be null");
		return contextMenu.addItem(createActionContent(action), event -> action.execute(contextSupplier.get()));
	}

	public static <ContextClass> GridMenuItem<ContextClass> registerGridAction(final GridContextMenu<ContextClass> contextMenu,
			final CContextActionDefinition<ContextClass> action, final Supplier<ContextClass> contextSupplier) {
		Check.notNull(contextMenu, "contextMenu cannot be null");
		Check.notNull(action, "action cannot be null");
		Check.notNull(contextSupplier, "contextSupplier cannot be null");
		return contextMenu.addItem(createActionContent(action), event -> action.execute(contextSupplier.get()));
	}

	public static <ContextClass> void refreshComponentActionState(final MenuItem menuItem,
			final CContextActionDefinition<ContextClass> action, final ContextClass context) {
		Check.notNull(menuItem, "menuItem cannot be null");
		Check.notNull(action, "action cannot be null");
		menuItem.setVisible(action.isVisible(context));
		menuItem.setEnabled(action.isEnabled(context));
	}

	public static <ContextClass> void refreshGridActionState(final GridMenuItem<ContextClass> menuItem,
			final CContextActionDefinition<ContextClass> action, final ContextClass context) {
		Check.notNull(menuItem, "menuItem cannot be null");
		Check.notNull(action, "action cannot be null");
		menuItem.setVisible(action.isVisible(context));
		menuItem.setEnabled(action.isEnabled(context));
	}
}
