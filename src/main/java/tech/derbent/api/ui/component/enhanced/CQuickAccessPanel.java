package tech.derbent.api.ui.component.enhanced;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;

import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.utils.Check;

/**
 * Reusable quick-access toolbar for board/tree-grid style views.
 *
 * <p>This component is designed to be placed into the "left header" slot of timeline grids
 * (see {@code CAbstractGnntGridBase#setLeftHeaderComponent}). It behaves like a mini CRUD toolbar:
 * grid implementations can add/remove controls and swap listeners without caring where the toolbar is hosted.</p>
 */
public class CQuickAccessPanel extends CHorizontalLayout {

	private static final long serialVersionUID = 1L;

	public static final String ID_SUFFIX_REFRESH = "refresh";
	public static final String ID_SUFFIX_TOGGLE_DETAILS = "toggle-details";

	private final String baseId;
	private final Map<String, Runnable> contextActionRefreshersByKey = new LinkedHashMap<>();
	private final Map<String, Component> customControlsByKey = new LinkedHashMap<>();

	private final CButton buttonRefresh;
	private final CButton buttonToggleDetails;
	private Runnable refreshHandler;
	private Runnable toggleDetailsHandler;
	private boolean showRefreshButton;
	private boolean showToggleDetailsButton;

	public CQuickAccessPanel(final String componentId) {
		Check.notBlank(componentId, "componentId cannot be blank");
		baseId = componentId;
		setId(componentId);
		setPadding(false);
		setSpacing(false);
		setAlignItems(Alignment.CENTER);
		setWidthFull();
		getStyle().set("gap", "var(--lumo-space-xs)");
		getStyle().set("flex-wrap", "wrap");
		// Ensure the header cell can shrink (important when hosted in Grid header joins).
		getStyle().set("min-width", "0");

		// Built-in buttons are optional; callers enable them via the handler setters or visibility flags.
		buttonToggleDetails = CButton.createTertiary("Show details", VaadinIcon.EYE.create(), event -> {
			if (toggleDetailsHandler != null) {
				toggleDetailsHandler.run();
			}
		});
		buttonToggleDetails.setId(baseId + "-" + ID_SUFFIX_TOGGLE_DETAILS);
		buttonToggleDetails.addThemeVariants(ButtonVariant.LUMO_SMALL);

		buttonRefresh = CButton.createTertiary("Refresh", VaadinIcon.REFRESH.create(), event -> {
			if (refreshHandler != null) {
				refreshHandler.run();
			}
		});
		buttonRefresh.setId(baseId + "-" + ID_SUFFIX_REFRESH);
		buttonRefresh.addThemeVariants(ButtonVariant.LUMO_SMALL);

		add(buttonToggleDetails, buttonRefresh);
		updateBuiltinButtonStates();
	}

	public final CButton getButtonRefresh() {
		return buttonRefresh;
	}

	public final CButton getButtonToggleDetails() {
		return buttonToggleDetails;
	}

	public final Optional<Component> getControl(final String key) {
		return Optional.ofNullable(customControlsByKey.get(key));
	}

	/**
	 * Enables/disables visibility of the built-in refresh button.
	 */
	public final void setShowRefreshButton(final boolean show) {
		showRefreshButton = show;
		updateBuiltinButtonStates();
	}

	/**
	 * Enables/disables visibility of the built-in details toggle button.
	 */
	public final void setShowToggleDetailsButton(final boolean show) {
		showToggleDetailsButton = show;
		updateBuiltinButtonStates();
	}

	public final void setToggleDetailsHandler(final Runnable handler) {
		toggleDetailsHandler = handler;
		updateBuiltinButtonStates();
	}

	public final void setRefreshHandler(final Runnable handler) {
		refreshHandler = handler;
		updateBuiltinButtonStates();
	}

	public final void setDetailsVisible(final boolean visible) {
		// Keep naming/icon consistent across all boards that reuse this toolbar.
		buttonToggleDetails.setText(visible ? "Hide details" : "Show details");
		buttonToggleDetails.setIcon(visible ? VaadinIcon.EYE_SLASH.create() : VaadinIcon.EYE.create());
	}

	private void updateBuiltinButtonStates() {
		// Default behavior: show built-ins only when explicitly enabled OR when a handler is configured.
		buttonRefresh.setVisible(showRefreshButton || refreshHandler != null);
		buttonRefresh.setEnabled(refreshHandler != null);
		buttonToggleDetails.setVisible(showToggleDetailsButton || toggleDetailsHandler != null);
		buttonToggleDetails.setEnabled(toggleDetailsHandler != null);
	}

	/**
	 * Adds extra controls to the toolbar.
	 *
	 * <p>We accept raw Vaadin components so callers can reuse buttons/labels taken from other toolbars
	 * (Vaadin enforces single-parent ownership, so components should be removed from their original parent first).</p>
	 */
	public final void addControls(final List<? extends Component> controls) {
		if (controls == null || controls.isEmpty()) {
			return;
		}
		add(controls.toArray(Component[]::new));
	}

	// CRUD-toolbar-like aliases (same naming used by CCrudToolbar).
	public final void addCustomComponent(final Component component) {
		Check.notNull(component, "component cannot be null");
		add(component);
	}

	public final void addCustomComponent(final String key, final Component component) {
		addControl(key, component);
	}

	public final Optional<Component> removeCustomComponent(final String key) {
		return removeControl(key);
	}

	public final void clearCustomComponents() {
		clearControls();
	}

	public final CQuickAccessPanel setOnRefresh(final Runnable handler) {
		setRefreshHandler(handler);
		return this;
	}

	public final CQuickAccessPanel setOnToggleDetails(final Runnable handler) {
		setToggleDetailsHandler(handler);
		return this;
	}

	/**
	 * Adds a custom control that can later be removed by its key.
	 */
	public final void addControl(final String key, final Component component) {
		Check.notBlank(key, "key cannot be blank");
		Check.notNull(component, "component cannot be null");

		final Component existing = customControlsByKey.remove(key);
		if (existing != null) {
			remove(existing);
		}
		if (component.getId().isEmpty()) {
			component.setId(baseId + "-" + key);
		}
		customControlsByKey.put(key, component);
		add(component);
	}

	public final Optional<Component> removeControl(final String key) {
		if (key == null || key.isBlank()) {
			return Optional.empty();
		}
		final Component removed = customControlsByKey.remove(key);
		if (removed != null) {
			remove(removed);
		}
		return Optional.ofNullable(removed);
	}

	/**
	 * Removes all custom controls; built-in buttons are managed via their setters.
	 */
	public final void clearControls() {
		customControlsByKey.values().forEach(this::remove);
		customControlsByKey.clear();
		contextActionRefreshersByKey.clear();
	}

	public final void clearContextActions() {
		final List<String> keys = List.copyOf(contextActionRefreshersByKey.keySet());
		keys.forEach(this::removeControl);
		contextActionRefreshersByKey.clear();
	}

	private CButton createTertiaryButtonInternal(final String text, final VaadinIcon icon, final Runnable onClick) {
		Check.notBlank(text, "text cannot be blank");
		final CButton button = CButton.createTertiary(text, icon != null ? icon.create() : null, event -> {
			if (onClick != null) {
				onClick.run();
			}
		});
		button.addThemeVariants(ButtonVariant.LUMO_SMALL);
		return button;
	}

	public final CButton addTertiaryButton(final String text, final VaadinIcon icon, final Runnable onClick) {
		final CButton button = createTertiaryButtonInternal(text, icon, onClick);
		add(button);
		return button;
	}

	/**
	 * Convenience for adding a tracked tertiary button (stable key + stable ID).
	 */
	public final CButton addTertiaryButton(final String key, final String text, final VaadinIcon icon, final Runnable onClick) {
		Check.notBlank(key, "key cannot be blank");
		final CButton button = createTertiaryButtonInternal(text, icon, onClick);
		addControl(key, button);
		return button;
	}

	public final void refreshContextActionStates() {
		contextActionRefreshersByKey.values().forEach(Runnable::run);
	}

	public final <ContextClass> void setContextActions(final List<CContextActionDefinition<ContextClass>> actions,
			final Supplier<ContextClass> contextSupplier) {
		clearContextActions();
		if (actions == null || actions.isEmpty()) {
			return;
		}
		Check.notNull(contextSupplier, "contextSupplier cannot be null");
		for (final CContextActionDefinition<ContextClass> action : actions) {
			if (action == null) {
				continue;
			}
			final CButton button = createTertiaryButtonInternal(action.getLabel(), action.getIcon(), () -> action.execute(contextSupplier.get()));
			addControl(action.getKey(), button);
			contextActionRefreshersByKey.put(action.getKey(), () -> {
				final ContextClass context = contextSupplier.get();
				button.setVisible(action.isVisible(context));
				button.setEnabled(action.isEnabled(context));
			});
		}
		refreshContextActionStates();
	}
}
