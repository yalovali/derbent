package tech.derbent.api.ui.component.basic;

import java.util.List;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import tech.derbent.api.annotations.CDataProviderResolver;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.page.domain.CPageEntity;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.api.session.service.CWebSessionService;
import tech.derbent.api.utils.Check;

/** CNavigableComboBox - A combobox component that includes a navigation button to navigate to the entity's page. Extends CustomField to provide a
 * composite component with combobox and navigation button. */
public class CNavigableComboBox<T extends CEntityDB<T>> extends CustomField<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CNavigableComboBox.class);
	private static final long serialVersionUID = 1L;
	private final CColorAwareComboBox<T> comboBox;
	private final EntityFieldInfo fieldInfo;
	private final HorizontalLayout layout;
	private CButton navigateButton;

	/** Constructor for CNavigableComboBox with content owner, field info and data provider resolver. Note: This is a CustomField, so binding should
	 * be done on the CNavigableComboBox itself, not on the internal combobox.
	 * @param contentOwner         the content owner (page) for context
	 * @param fieldInfo            the field information for the combobox
	 * @param dataProviderResolver the data provider resolver
	 * @throws Exception if creation fails */
	public CNavigableComboBox(final IContentOwner contentOwner, final EntityFieldInfo fieldInfo, final CDataProviderResolver dataProviderResolver)
			throws Exception {
		// Check.notNull(contentOwner, "Content owner cannot be null");
		Check.notNull(fieldInfo, "Field info cannot be null");
		Check.notNull(dataProviderResolver, "Data provider resolver cannot be null");
		this.fieldInfo = fieldInfo;
		layout = new HorizontalLayout();
		layout.setSpacing(false);
		layout.setPadding(false);
		layout.setAlignItems(HorizontalLayout.Alignment.CENTER);
		// Create the combobox with data provider - don't bind it, CustomField handles binding
		comboBox = new CColorAwareComboBox<>(contentOwner, fieldInfo, null, dataProviderResolver);
		comboBox.setWidthFull();
		// Add value change listener to update navigation button visibility and propagate changes
		comboBox.addValueChangeListener(event -> {
			// Propagate value change to the CustomField first
			updateValue();
			// Then update navigation button visibility
			updateNavigationButton();
		});
		layout.add(comboBox);
		add(layout);
	}

	/** Creates and returns the navigation button for the current entity value.
	 * @return the navigation button or null if navigation is not available */
	private CButton createNavigationButton() {
		try {
			final T value = comboBox.getValue();
			if (value == null) {
				return null;
			}
			// Prefer concrete selected value class for polymorphic fields (e.g. abstract base references).
			final Class<?> clazz = resolveNavigationClass(value);
			if (!CEntityDB.class.isAssignableFrom(clazz)) {
				return null;
			}
			final String baseViewName = resolveViewName(clazz);
			Check.notNull(baseViewName, "Base view name cannot be null for class: " + clazz.getName());
			final CPageEntityService service = CSpringContext.getBean(CPageEntityService.class);
			final CWebSessionService session = CSpringContext.getBean(CWebSessionService.class);
			final CPageEntity pageEntity = service.findByNameAndProject(baseViewName, session.getActiveProject().orElseThrow()).orElse(null);
			if (pageEntity == null) {
				LOGGER.debug("No page entity found for view name: {}", baseViewName);
				return null;
			}
			// Create the navigation button
			final CButton button = new CButton("", VaadinIcon.ARROW_RIGHT.create());
			button.addClickListener(event -> {
				try {
					final String route = pageEntity.getRoute() + "&item:" + value.getId();
					UI.getCurrent().navigate(route);
				} catch (final Exception e) {
					LOGGER.error("Error navigating to entity page '{}': {}", pageEntity.getName(), e.getMessage());
				}
			});
			return button;
		} catch (final Exception e) {
			LOGGER.debug("Could not create navigation button: {}", e.getMessage());
			return null;
		}
	}

	private Class<?> resolveNavigationClass(final T value) {
		if (value != null) {
			return value.getClass();
		}
		return fieldInfo.getFieldTypeClass();
	}

	private String resolveViewName(final Class<?> primaryClass) throws NoSuchFieldException, IllegalAccessException {
		Class<?> current = primaryClass;
		while (current != null && current != Object.class) {
			try {
				return (String) current.getField("VIEW_NAME").get(null);
			} catch (final NoSuchFieldException ignored) {
				current = current.getSuperclass();
			}
		}
		// Fallback for pre-existing behavior in case selected value class has no VIEW_NAME.
		return (String) fieldInfo.getFieldTypeClass().getField("VIEW_NAME").get(null);
	}

	/** Disables automatic persistence for the internal ComboBox.
	 * <p>
	 * This is a convenience method that delegates to the internal CColorAwareComboBox's disablePersistence method.
	 * </p>
	 * @see CColorAwareComboBox#disablePersistence() */
	public void disablePersistence() {
		comboBox.disablePersistence();
	}

	/** Enables automatic persistence for the internal ComboBox.
	 * <p>
	 * This is a convenience method that delegates to the internal CColorAwareComboBox's enablePersistence method.
	 * </p>
	 * @param storageKey The unique key to use for storing the value in session storage
	 * @param converter  Function to convert stored ID back to entity (return null if not found)
	 * @throws IllegalArgumentException if storageKey is null/blank or converter is null
	 * @see CColorAwareComboBox#enablePersistence(String, Function) */
	public void enablePersistence(final String storageKey, final Function<String, T> converter) {
		comboBox.enablePersistence(storageKey, converter);
	}

	@Override
	protected T generateModelValue() {
		return comboBox.getValue();
	}

	/** Gets the internal combobox component.
	 * @return the combobox */
	public CColorAwareComboBox<T> getComboBox() { return comboBox; }

	/** Checks if persistence is enabled for the internal ComboBox.
	 * @return true if persistence is enabled, false otherwise
	 * @see CColorAwareComboBox#isPersistenceEnabled() */
	public boolean isPersistenceEnabled() { return comboBox.isPersistenceEnabled(); }

	@Override
	protected void onAttach(final AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		updateNavigationButton();
	}

	/** Sets the items for the combobox.
	 * @param items the items to set */
	public void setItems(final List<T> items) {
		comboBox.setItems(items);
	}

	@Override
	protected void setPresentationValue(final T newPresentationValue) {
		comboBox.setValue(newPresentationValue);
	}

	/** Updates the navigation button visibility based on current value and page availability. */
	private void updateNavigationButton() {
		// Remove existing navigation button if present
		if (navigateButton != null) {
			layout.remove(navigateButton);
			navigateButton = null;
		}
		// Create new navigation button if value is present
		final T value = comboBox.getValue();
		if (!(value != null && value.getId() != null)) {
			return;
		}
		navigateButton = createNavigationButton();
		if (navigateButton != null) {
			layout.add(navigateButton);
		}
	}
}
