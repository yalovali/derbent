package tech.derbent.api.components;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.shared.Registration;
import tech.derbent.api.utils.CAuxillaries;

/** CSearchToolbar - A reusable search toolbar component with debounced text input. Layer: View (MVC) Provides a text field for live searching with
 * configurable debounce delay to avoid excessive search requests while the user is typing. Follows the project's coding guidelines for component
 * styling and behavior.
 * @author Derbent Framework
 * @since 1.0 */
public class CSearchToolbar extends HorizontalLayout {

	private static final long serialVersionUID = 1L;
	private final TextField searchField;
	private static final int DEFAULT_DEBOUNCE_DELAY_MS = 300;

	/** Creates a search toolbar with default settings. */
	public CSearchToolbar() {
		this("Search...", DEFAULT_DEBOUNCE_DELAY_MS);
	}

	/** Creates a search toolbar with custom placeholder text.
	 * @param placeholder placeholder text for the search field */
	public CSearchToolbar(final String placeholder) {
		this(placeholder, DEFAULT_DEBOUNCE_DELAY_MS);
	}

	/** Creates a search toolbar with custom placeholder and debounce delay.
	 * @param placeholder     placeholder text for the search field
	 * @param debounceDelayMs delay in milliseconds before triggering search */
	public CSearchToolbar(final String placeholder, final int debounceDelayMs) {
		super();
		// Initialize search field
		searchField = new TextField();
		searchField.setPlaceholder(placeholder);
		searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
		searchField.setClearButtonVisible(true);
		searchField.setValueChangeMode(ValueChangeMode.LAZY);
		searchField.setValueChangeTimeout(debounceDelayMs);
		searchField.setWidth("300px");
		// Set up the layout
		setSpacing(true);
		setPadding(true);
		setAlignItems(Alignment.CENTER);
		add(searchField);
		// Apply consistent styling
		addClassName("search-toolbar");
		CAuxillaries.setId(this);
	}

	/** Adds a listener for search text changes.
	 * @param listener the listener to be notified of search text changes
	 * @return registration for removing the listener */
	public Registration addSearchListener(final ComponentEventListener<SearchEvent> listener) {
		return searchField.addValueChangeListener(event -> {
			final SearchEvent searchEvent = new SearchEvent(this, event.getValue());
			listener.onComponentEvent(searchEvent);
		});
	}

	/** Gets the current search text.
	 * @return the current search text, or empty string if null */
	public String getSearchText() {
		final String value = searchField.getValue();
		return value != null ? value : "";
	}

	/** Sets the search text programmatically.
	 * @param searchText the search text to set */
	public void setSearchText(final String searchText) {
		searchField.setValue(searchText != null ? searchText : "");
	}

	/** Clears the search text. */
	public void clearSearch() {
		searchField.clear();
	}

	/** Sets whether the search field is enabled.
	 * @param enabled true to enable, false to disable */
	public void setEnabled(final boolean enabled) {
		searchField.setEnabled(enabled);
	}

	/** Gets the underlying search text field for advanced customization.
	 * @return the search text field */
	public TextField getSearchField() { return searchField; }

	/** Event fired when search text changes. */
	public static class SearchEvent extends com.vaadin.flow.component.ComponentEvent<CSearchToolbar> {

		private static final long serialVersionUID = 1L;
		private final String searchText;

		public SearchEvent(final CSearchToolbar source, final String searchText) {
			super(source, false);
			this.searchText = searchText;
		}

		public String getSearchText() { return searchText != null ? searchText : ""; }
	}
}
