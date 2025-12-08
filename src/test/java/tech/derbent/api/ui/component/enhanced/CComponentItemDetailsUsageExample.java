package tech.derbent.api.ui.component.enhanced;

import org.springframework.beans.factory.annotation.Autowired;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.base.session.service.ISessionService;

/** CComponentItemDetailsUsageExample - Example demonstrating how to use CComponentItemDetails component.
 * <p>
 * CComponentItemDetails is a standard Vaadin component that implements HasValue interface, making it compatible with Vaadin binders and forms. It
 * automatically displays entity details in a CDynamicPageRouter when a value is set.
 * </p>
 * @author Derbent Framework
 * @since 1.0 */
public class CComponentItemDetailsUsageExample {

	@Autowired
	private CDetailSectionService detailSectionService;

	@Autowired
	private ISessionService sessionService;

	@Autowired
	private CPageEntityService pageEntityService;

	/** Example 1: Basic usage - Create component and set value */
	public void example1_basicUsage() throws Exception {
		// Create the component
		CComponentItemDetails itemDetails = new CComponentItemDetails(sessionService, pageEntityService, detailSectionService);

		// Set full width/height to expand with container
		itemDetails.setWidthFull();
		itemDetails.setHeightFull();

		// Later, when you have an entity to display
		CProjectItem<?> entity = getSelectedEntity(); // your method to get entity

		// Simply set the value - component will automatically display entity details
		itemDetails.setValue(entity);

		// Clear the display
		itemDetails.clear(); // or itemDetails.setValue(null);
	}

	/** Example 2: Using with value change listeners */
	public void example2_valueChangeListener() throws Exception {
		CComponentItemDetails itemDetails = new CComponentItemDetails(sessionService, pageEntityService, detailSectionService);

		// Add a value change listener to react to changes
		itemDetails.addValueChangeListener(event -> {
			CProjectItem<?> oldValue = event.getOldValue();
			CProjectItem<?> newValue = event.getValue();
			boolean fromClient = event.isFromClient();

			if (newValue != null) {
				System.out.println("Entity changed: " + newValue.getName());
				System.out.println("Changed from client: " + fromClient);
			} else {
				System.out.println("Entity cleared");
			}
		});

		// Set a value - listener will be notified
		itemDetails.setValue(getSelectedEntity());
	}

	/** Example 3: Using with binders (standard Vaadin pattern) */
	public void example3_binderIntegration() throws Exception {
		CComponentItemDetails itemDetails = new CComponentItemDetails(sessionService, pageEntityService, detailSectionService);

		// CComponentItemDetails implements HasValue, so it can be used with binders
		// Example: Bind to a field in a form
		// binder.forField(itemDetails)
		//     .bind(FormBean::getSelectedEntity, FormBean::setSelectedEntity);

		// Or use with direct binding to form fields
		// This allows the component to be part of a larger form with validation
	}

	/** Example 4: Read-only mode */
	public void example4_readOnlyMode() throws Exception {
		CComponentItemDetails itemDetails = new CComponentItemDetails(sessionService, pageEntityService, detailSectionService);

		// Set read-only mode (prevents programmatic setValue calls)
		itemDetails.setReadOnly(true);

		// Check if read-only
		if (itemDetails.isReadOnly()) {
			System.out.println("Component is in read-only mode");
		}

		// Attempting to set value in read-only mode will be ignored
		itemDetails.setValue(getSelectedEntity()); // Will log warning and do nothing
	}

	/** Example 5: Checking state */
	public void example5_checkingState() throws Exception {
		CComponentItemDetails itemDetails = new CComponentItemDetails(sessionService, pageEntityService, detailSectionService);

		// Check if component has a value
		if (itemDetails.isEmpty()) {
			System.out.println("No entity selected");
		}

		// Get current value
		CProjectItem<?> currentValue = itemDetails.getValue();
		if (currentValue != null) {
			System.out.println("Current entity: " + currentValue.getName());
		}
	}

	/** Example 6: Multiple listeners */
	public void example6_multipleListeners() throws Exception {
		CComponentItemDetails itemDetails = new CComponentItemDetails(sessionService, pageEntityService, detailSectionService);

		// Add multiple listeners
		var registration1 = itemDetails.addValueChangeListener(event -> {
			System.out.println("Listener 1: Entity changed");
		});

		var registration2 = itemDetails.addValueChangeListener(event -> {
			System.out.println("Listener 2: Entity changed");
		});

		// Remove a listener when no longer needed
		registration1.remove();

		// Set value - only listener 2 will be notified
		itemDetails.setValue(getSelectedEntity());
	}

	/** Example 7: Integration with grids - display selected grid item details */
	public void example7_gridIntegration() throws Exception {
		CComponentItemDetails itemDetails = new CComponentItemDetails(sessionService, pageEntityService, detailSectionService);

		// Typical use case: Display details of selected grid item
		// grid.asSingleSelect().addValueChangeListener(event -> {
		//     CProjectItem<?> selectedItem = event.getValue();
		//     itemDetails.setValue(selectedItem);
		// });

		// When grid selection changes, itemDetails automatically displays the entity's detail page
	}

	/** Example 8: Error handling */
	public void example8_errorHandling() throws Exception {
		// The component handles errors internally and shows notifications
		CComponentItemDetails itemDetails = new CComponentItemDetails(sessionService, pageEntityService, detailSectionService);

		// If entity doesn't have a VIEW_NAME field or page is not found,
		// component will log error and show notification to user
		itemDetails.setValue(getSelectedEntity());

		// Setting null is safe and clears the display
		itemDetails.setValue(null);
	}

	/** Helper method to simulate getting a selected entity */
	private CProjectItem<?> getSelectedEntity() {
		// In real code, this would get the entity from a grid selection,
		// form field, database query, etc.
		return null; // Placeholder
	}
}
