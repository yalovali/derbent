package tech.derbent.api.components;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.components.CButton;
import tech.derbent.app.page.domain.CPageEntity;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.base.session.service.CWebSessionService;

/** CNavigableComboBox - A combobox component that includes a navigation button to navigate to the entity's page. Extends CustomField to provide a
 * composite component with combobox and navigation button. */
public class CNavigableComboBox<T extends CEntityDB<T>> extends CustomField<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CNavigableComboBox.class);
	private static final long serialVersionUID = 1L;
	private final CColorAwareComboBox<T> comboBox;
	private final EntityFieldInfo fieldInfo;
	private final HorizontalLayout layout;
	private CButton navigateButton;

	/** Constructor for CNavigableComboBox with entity field information.
	 * @param fieldInfo the field information for the combobox */
	public CNavigableComboBox(final EntityFieldInfo fieldInfo) {
		super();
		this.fieldInfo = fieldInfo;
		this.layout = new HorizontalLayout();
		layout.setSpacing(false);
		layout.setPadding(false);
		layout.setAlignItems(HorizontalLayout.Alignment.CENTER);
		// Create the combobox
		this.comboBox = new CColorAwareComboBox<>(fieldInfo);
		comboBox.setWidthFull();
		// Add value change listener to update navigation button visibility
		comboBox.addValueChangeListener(event -> updateNavigationButton());
		layout.add(comboBox);
		add(layout);
	}

	/** Constructor for CNavigableComboBox with content owner, field info and data provider resolver. Note: This is a CustomField, so binding should
	 * be done on the CNavigableComboBox itself, not on the internal combobox.
	 * @param contentOwner         the content owner (page) for context
	 * @param fieldInfo            the field information for the combobox
	 * @param dataProviderResolver the data provider resolver
	 * @throws Exception if creation fails */
	public CNavigableComboBox(IContentOwner contentOwner, final EntityFieldInfo fieldInfo,
			tech.derbent.api.annotations.CDataProviderResolver dataProviderResolver) throws Exception {
		super();
		this.fieldInfo = fieldInfo;
		this.layout = new HorizontalLayout();
		layout.setSpacing(false);
		layout.setPadding(false);
		layout.setAlignItems(HorizontalLayout.Alignment.CENTER);
		// Create the combobox with data provider - don't bind it, CustomField handles binding
		this.comboBox = new CColorAwareComboBox<>(contentOwner, fieldInfo, null, dataProviderResolver);
		comboBox.setWidthFull();
		// Add value change listener to update navigation button visibility and propagate changes
		comboBox.addValueChangeListener(event -> {
			updateNavigationButton();
			// Propagate value change to the CustomField
			updateValue();
		});
		layout.add(comboBox);
		add(layout);
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		updateNavigationButton();
	}

	/** Creates and returns the navigation button for the current entity value.
	 * @return the navigation button or null if navigation is not available */
	private CButton createNavigationButton() {
		try {
			T value = comboBox.getValue();
			if (value == null) {
				return null;
			}
			// Get the entity class
			Class<?> clazz = fieldInfo.getFieldTypeClass();
			if (!CEntityDB.class.isAssignableFrom(clazz)) {
				return null;
			}
			// Get the VIEW_NAME field
			String baseViewName = (String) clazz.getField("VIEW_NAME").get(null);
			// Get the page entity service and session
			CPageEntityService service = CSpringContext.getBean(CPageEntityService.class);
			CWebSessionService session = CSpringContext.getBean(CWebSessionService.class);
			// Find the page entity for this view
			CPageEntity pageEntity = service.findByNameAndProject(baseViewName, session.getActiveProject().orElseThrow()).orElse(null);
			if (pageEntity == null) {
				LOGGER.debug("No page entity found for view name: {}", baseViewName);
				return null;
			}
			// Create the navigation button
			CButton button = new CButton("", VaadinIcon.ARROW_RIGHT.create());
			button.addClickListener(event -> {
				try {
					String route = pageEntity.getRoute() + "&item:" + value.getId();
					UI.getCurrent().navigate(route);
				} catch (Exception e) {
					LOGGER.error("Error navigating to entity page '{}': {}", pageEntity.getName(), e.getMessage());
				}
			});
			return button;
		} catch (Exception e) {
			LOGGER.debug("Could not create navigation button: {}", e.getMessage());
			return null;
		}
	}

	@Override
	protected T generateModelValue() {
		return comboBox.getValue();
	}

	/** Gets the internal combobox component.
	 * @return the combobox */
	public CColorAwareComboBox<T> getComboBox() { return comboBox; }

	@Override
	protected void setPresentationValue(T newPresentationValue) {
		comboBox.setValue(newPresentationValue);
	}

	/** Sets the items for the combobox.
	 * @param items the items to set */
	public void setItems(List<T> items) {
		comboBox.setItems(items);
	}

	/** Updates the navigation button visibility based on current value and page availability. */
	private void updateNavigationButton() {
		// Remove existing navigation button if present
		if (navigateButton != null) {
			layout.remove(navigateButton);
			navigateButton = null;
		}
		// Create new navigation button if value is present
		T value = comboBox.getValue();
		if (value != null && value.getId() != null) {
			navigateButton = createNavigationButton();
			if (navigateButton != null) {
				layout.add(navigateButton);
			}
		}
	}
}
