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
import tech.derbent.api.page.view.CDialogDynamicPage;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.api.session.service.CWebSessionService;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;

/** CNavigableComboBox - A combobox component that includes a navigation button to navigate to the entity's page. Extends CustomField to provide a
 * composite component with combobox and navigation button. */
public class CNavigableComboBox<T extends CEntityDB<T>> extends CustomField<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CNavigableComboBox.class);
	private static final long serialVersionUID = 1L;
	private final CColorAwareComboBox<T> comboBox;
	private final IContentOwner contentOwner;
	private final CDataProviderResolver dataProviderResolver;
	private CButton editButton;
	private final EntityFieldInfo fieldInfo;
	private final HorizontalLayout layout;
	private CButton navigateButton;
	private CPageEntity pageEntityForActions;

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
		this.contentOwner = contentOwner;
		this.dataProviderResolver = dataProviderResolver;
		this.fieldInfo = fieldInfo;
		layout = new HorizontalLayout();
		layout.setSpacing(false);
		layout.setPadding(false);
		// Keep stable horizontal alignment when ComboBox helper text appears/disappears.
		layout.setAlignItems(HorizontalLayout.Alignment.START);
		// Create the combobox with data provider - don't bind it, CustomField handles binding
		comboBox = new CColorAwareComboBox<>(contentOwner, fieldInfo, null, dataProviderResolver);
		comboBox.setWidthFull();
		// Add value change listener to update navigation button visibility and propagate changes
		comboBox.addValueChangeListener(event -> {
			// Propagate value change to the CustomField first
			updateValue();
			// Then update action button states
			updateNavigationButton();
		});
		navigateButton = createNavigationButton();
		editButton = createEditButton();
		Check.notNull(navigateButton, "Navigation button must be initialized");
		Check.notNull(editButton, "Edit button must be initialized");
		layout.add(comboBox, navigateButton, editButton);
		add(layout);
	}
	
	private CButton createEditButton() {
		final CButton button = new CButton("", VaadinIcon.EDIT.create());
		button.addClickListener(event -> {
			try {
				Check.notNull(pageEntityForActions, "Page entity is not resolved for edit action");
				final T value = comboBox.getValue();
				final Long selectedId = value != null ? value.getId() : null;
				final String route = CDialogDynamicPage.buildDynamicRoute(pageEntityForActions.getId(), selectedId);
				final CDialogDynamicPage dialog = CDialogDynamicPage.fromRoute(route);
				dialog.addOpenedChangeListener(openedEvent -> {
					if (!openedEvent.isOpened()) {
						refreshComboBoxItemsAfterDialogClose();
					}
				});
				dialog.open();
			} catch (final Exception e) {
				LOGGER.error("Error opening dialog for entity page '{}': {}",
						pageEntityForActions != null ? pageEntityForActions.getName() : "<none>", e.getMessage());
				CNotificationService.showException("Error opening dynamic page dialog for entity", e);
			}
		});
		return button;
	}

	private void refreshComboBoxItemsAfterDialogClose() {
		try {
			final T previousValue = comboBox.getValue();
			final Long previousId = previousValue != null ? previousValue.getId() : null;
			final List<T> refreshedItems = dataProviderResolver.<T>resolveDataList(contentOwner, fieldInfo);
			Check.notNull(refreshedItems, "Resolved items cannot be null while refreshing CNavigableComboBox");
			comboBox.setItems(refreshedItems);
			if (previousId != null) {
				final T refreshedSelection = refreshedItems.stream().filter(item -> previousId.equals(item.getId())).findFirst().orElse(null);
				comboBox.setValue(refreshedSelection);
			}
		} catch (final Exception e) {
			LOGGER.warn("Could not refresh ComboBox items after dialog close: {}", e.getMessage());
		}
	}

	/** Creates and returns the navigation button for the current entity value.
	 * @return the navigation button or null if navigation is not available */
	private CButton createNavigationButton() {
		try {
			// Create the navigation button
			final CButton button = new CButton("", VaadinIcon.ARROW_RIGHT.create());
			button.addClickListener(event -> {
				try {
					Check.notNull(pageEntityForActions, "Page entity is not resolved for navigation action");
					final T value = comboBox.getValue();
					Check.notNull(value, "Selected entity cannot be null for navigation");
					Check.notNull(value.getId(), "Selected entity id cannot be null for dialog navigation");
					UI.getCurrent().navigate(pageEntityForActions.getRoute() + "&item:" + value.getId());
				} catch (final Exception e) {
					LOGGER.error("Error navigating to entity page '{}': {}",
							pageEntityForActions != null ? pageEntityForActions.getName() : "<none>", e.getMessage());
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

	private CPageEntity resolveCurrentPageEntity(final T value) {
		try {
			final Class<?> clazz = resolveNavigationClass(value);
			if (!CEntityDB.class.isAssignableFrom(clazz)) {
				return null;
			}
			final String baseViewName = resolveViewName(clazz);
			final CPageEntityService service = CSpringContext.getBean(CPageEntityService.class);
			final CWebSessionService session = CSpringContext.getBean(CWebSessionService.class);
			return service.findByNameAndProject(baseViewName, session.getActiveProject().orElseThrow()).orElse(null);
		} catch (final Exception e) {
			LOGGER.debug("Could not resolve page entity for combo actions: {}", e.getMessage());
			return null;
		}
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

	/** Updates action button states based on current value and page availability. Buttons are always present in layout. */
	private void updateNavigationButton() {
		final T value = comboBox.getValue();
		pageEntityForActions = resolveCurrentPageEntity(value);
		final boolean hasPage = pageEntityForActions != null;
		final boolean hasSelectedEntity = value != null && value.getId() != null;
		Check.notNull(navigateButton, "Navigation button must be initialized");
		Check.notNull(editButton, "Edit button must be initialized");
		navigateButton.setEnabled(hasPage && hasSelectedEntity);
		// Keep edit enabled even when no selected value to allow creating new item in dialog.
		editButton.setEnabled(hasPage);
	}
}
