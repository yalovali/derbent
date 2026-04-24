package tech.derbent.plm.agile.view;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.IComponentTransientPlaceHolder;
import tech.derbent.api.interfaces.IPageServiceAutoRegistrable;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.page.view.CDialogDynamicPage;
import tech.derbent.api.parentrelation.service.CHierarchyNavigationService;
import tech.derbent.api.parentrelation.service.CParentRelationService;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.enhanced.CComponentBase;
import tech.derbent.api.ui.component.enhanced.CComponentEntitySelection;
import tech.derbent.api.ui.component.enhanced.CComponentEntitySelection.EntityTypeConfig;
import tech.derbent.api.ui.component.enhanced.CComponentItemDetails;
import tech.derbent.api.ui.dialogs.CDialogEntitySelection;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;

/**
 * Generic hierarchy children component that keeps the legacy agile placeholder contract intact.
 *
 * <p>The component now discovers valid child types from type levels, so new hierarchy modules can use
 * the same UI without introducing another specialized selector.</p>
 */
public class CComponentAgileChildren extends CComponentBase<Set<CProjectItem<?>>>
		implements IComponentTransientPlaceHolder<CProjectItem<?>>, IPageServiceAutoRegistrable {

	private static final String ALL_TYPES_DISPLAY_NAME = "All Types";
	public static final String ID_BUTTON_ADD_EXISTING = "custom-agile-children-add-existing-button";
	public static final String ID_BUTTON_ADD_NEW = "custom-agile-children-add-new-button";
	public static final String ID_BUTTON_EDIT = "custom-agile-children-edit-button";
	public static final String ID_BUTTON_REMOVE = "custom-agile-children-remove-button";
	public static final String ID_ROOT = "custom-agile-children-component";
	public static final String ID_SELECTION = "custom-agile-children-selection";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentAgileChildren.class);
	private static final long serialVersionUID = 1L;

	private final CParentRelationService parentRelationService;
	private final CHierarchyNavigationService hierarchyNavigationService;
	private CButton buttonAddExisting;
	private CButton buttonAddNew;
	private CButton buttonEdit;
	private CButton buttonRemove;
	private CComponentItemDetails componentChildDetails;
	private CComponentEntitySelection<CProjectItem<?>> componentEntitySelection;
	private CProjectItem<?> currentParent;
	private Div detailsPlaceholder;
	private Div infoDiv;
	private Div selectionContainer;
	private final ISessionService sessionService;

	public CComponentAgileChildren(final CParentRelationService parentRelationService, final CPageEntityService pageEntityService,
			final ISessionService sessionService) {
		Check.notNull(parentRelationService, "parentRelationService cannot be null");
		Check.notNull(pageEntityService, "pageEntityService cannot be null");
		Check.notNull(sessionService, "sessionService cannot be null");
		this.parentRelationService = parentRelationService;
		this.sessionService = sessionService;
		hierarchyNavigationService = CSpringContext.getBean(CHierarchyNavigationService.class);
		initializeComponents();
	}

	private void attachChildToParent(final CProjectItem<?> child) {
		Check.notNull(child, "child cannot be null");
		parentRelationService.setParent(child, currentParent);
		saveEntity(child);
	}

	@SuppressWarnings({
			"rawtypes", "unchecked"
	})
	private EntityTypeConfig<?> createAllTypesConfig(final EntityTypeConfig<?> firstType) {
		return new EntityTypeConfig(ALL_TYPES_DISPLAY_NAME, firstType.getEntityClass(), firstType.getService());
	}

	private EntityTypeConfig<?> createEntityTypeConfig(final Class<?> entityClass) {
		final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(entityClass);
		final CAbstractService<?> service = (CAbstractService<?>) CSpringContext.getBean(serviceClass);
		return createEntityTypeConfigUnchecked(entityClass, service);
	}

	@SuppressWarnings({
			"rawtypes", "unchecked"
	})
	private EntityTypeConfig<?> createEntityTypeConfigUnchecked(final Class<?> entityClass, final CAbstractService<?> service) {
		return EntityTypeConfig.createWithRegistryName((Class) entityClass, (CAbstractService) service);
	}

	private List<EntityTypeConfig<?>> createFilterableTypes(final List<Class<? extends CProjectItem<?>>> entityClasses) {
		final List<Class<? extends CProjectItem<?>>> sortedClasses = entityClasses.stream()
				.sorted(Comparator.comparing(entityClass -> {
					final String title = CEntityRegistry.getEntityTitleSingular(entityClass);
					return title != null ? title : entityClass.getSimpleName();
				}, String.CASE_INSENSITIVE_ORDER))
				.toList();
		final List<EntityTypeConfig<?>> entityTypes = new java.util.ArrayList<>();
		for (final Class<? extends CProjectItem<?>> entityClass : sortedClasses) {
			entityTypes.add(createEntityTypeConfig(entityClass));
		}
		if (entityTypes.size() <= 1) {
			return entityTypes;
		}
		final List<EntityTypeConfig<?>> filterableTypes = new java.util.ArrayList<>();
		filterableTypes.add(createAllTypesConfig(entityTypes.get(0)));
		filterableTypes.addAll(entityTypes);
		return filterableTypes;
	}

	@SuppressWarnings({
			"rawtypes", "unchecked"
	})
	private void createNewChildEntity(final EntityTypeConfig<?> config) {
		Check.notNull(config, "config cannot be null");
		try {
			final CAbstractService service = config.getService();
			final Object created = service.newEntity();
			Check.isTrue(created instanceof CProjectItem, "New entity is not a project item: " + created.getClass().getSimpleName());
			final CProjectItem<?> child = (CProjectItem<?>) created;
			final Object saved = service.save(child);
			Check.isTrue(saved instanceof CProjectItem, "Saved entity is not a project item");
			final CProjectItem<?> savedChild = (CProjectItem<?>) saved;
			parentRelationService.setParent(savedChild, currentParent);
			service.save(savedChild);
			openEditDialog(savedChild, this::refreshSelection, () -> {
				try {
					service.delete(savedChild);
					refreshSelection();
				} catch (final Exception e) {
					LOGGER.error("Failed to delete cancelled child entity reason={}", e.getMessage());
				}
			});
		} catch (final Exception e) {
			LOGGER.error("Failed to create new child entity reason={}", e.getMessage());
			CNotificationService.showException("Failed to create new child", e);
		}
	}

	private List<Class<? extends CProjectItem<?>>> getCreatableChildClasses() {
		return currentParent == null ? List.of() : hierarchyNavigationService.listCreatableChildEntityClasses(currentParent);
	}

	private List<Class<? extends CProjectItem<?>>> getExistingChildClasses() {
		// Explicit map type witness avoids wildcard-capture inference differences across compiler versions.
		return currentParent == null ? List.of()
				: hierarchyNavigationService.listSelectableChildCandidates(currentParent).stream().<Class<? extends CProjectItem<?>>>map(this::resolveProjectItemClass)
						.distinct().toList();
	}

	@SuppressWarnings("unchecked")
	private Class<? extends CProjectItem<?>> resolveProjectItemClass(final CProjectItem<?> item) {
		return (Class<? extends CProjectItem<?>>) item.getClass();
	}

	@Override
	public String getComponentName() { return "agileChildren"; }

	public Component getInternalSelectionComponent() { return componentEntitySelection; }

	private CProjectItem<?> getSingleSelectedItem() {
		if (componentEntitySelection == null) {
			return null;
		}
		return componentEntitySelection.getSelectedItems().stream().findFirst().orElse(null);
	}

	private void initializeComponents() {
		setId(ID_ROOT);
		setPadding(false);
		setSpacing(true);
		setWidthFull();

		buttonAddNew = new CButton("New", VaadinIcon.PLUS.create());
		buttonAddNew.setId(ID_BUTTON_ADD_NEW);
		buttonAddNew.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		buttonAddNew.addClickListener(event -> on_buttonAddNew_clicked());

		buttonAddExisting = new CButton("Add Existing", VaadinIcon.LIST_SELECT.create());
		buttonAddExisting.setId(ID_BUTTON_ADD_EXISTING);
		buttonAddExisting.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		buttonAddExisting.addClickListener(event -> on_buttonAddExisting_clicked());

		buttonEdit = new CButton("Edit", VaadinIcon.EDIT.create());
		buttonEdit.setId(ID_BUTTON_EDIT);
		buttonEdit.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		buttonEdit.addClickListener(event -> on_buttonEdit_clicked());

		buttonRemove = new CButton("Remove", VaadinIcon.TRASH.create());
		buttonRemove.setId(ID_BUTTON_REMOVE);
		buttonRemove.addThemeVariants(ButtonVariant.LUMO_ERROR);
		buttonRemove.addClickListener(event -> on_buttonRemove_clicked());

		final CHorizontalLayout toolbar = new CHorizontalLayout(buttonAddNew, buttonAddExisting, buttonEdit, buttonRemove);
		toolbar.setSpacing(true);
		toolbar.setPadding(false);
		toolbar.setWidthFull();

		infoDiv = new Div();
		infoDiv.getStyle().set("font-size", "var(--lumo-font-size-s)");
		infoDiv.getStyle().set("color", "var(--lumo-secondary-text-color)");
		infoDiv.setText("Select a parent item to manage children.");
		selectionContainer = new Div();
		selectionContainer.setId(ID_SELECTION);
		selectionContainer.setWidthFull();
		selectionContainer.setVisible(false);
		selectionContainer.getStyle().set("min-width", "0");
		add(toolbar, infoDiv, selectionContainer);

		initializeDetailsComponent();
		refreshButtonStates();
	}

	private void initializeDetailsComponent() {
		try {
			componentChildDetails = new CComponentItemDetails(sessionService);
			componentChildDetails.setWidthFull();
			componentChildDetails.setMinHeight("240px");
			componentChildDetails.setVisible(false);
			add(componentChildDetails);
		} catch (final Exception e) {
			LOGGER.error("Failed to initialize hierarchy child details component reason={}", e.getMessage());
			detailsPlaceholder = new Div();
			detailsPlaceholder.setText("Selected child details are currently unavailable.");
			detailsPlaceholder.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "var(--lumo-font-size-s)")
					.set("padding", "var(--lumo-space-s)");
			detailsPlaceholder.setVisible(false);
			add(detailsPlaceholder);
		}
	}

	private boolean isAllTypesConfig(final EntityTypeConfig<?> config) {
		return config != null && ALL_TYPES_DISPLAY_NAME.equals(config.getDisplayName());
	}

	private List<CProjectItem<?>> listAvailableItemsForSelection(final EntityTypeConfig<?> config) {
		if (currentParent == null || config == null) {
			return List.of();
		}
		final List<CProjectItem<?>> candidates = hierarchyNavigationService.listSelectableChildCandidates(currentParent);
		if (isAllTypesConfig(config)) {
			return candidates;
		}
		return candidates.stream().filter(candidate -> config.getEntityClass().isAssignableFrom(candidate.getClass())).toList();
	}

	private List<CProjectItem<?>> listChildrenForSelection(final EntityTypeConfig<?> config) {
		if (currentParent == null || config == null) {
			return List.of();
		}
		final List<CProjectItem<?>> children = parentRelationService.getChildren(currentParent);
		if (isAllTypesConfig(config)) {
			return children;
		}
		return children.stream().filter(child -> config.getEntityClass().isAssignableFrom(child.getClass())).toList();
	}

	private void on_buttonAddExisting_clicked() {
		try {
			Check.notNull(currentParent, "Parent cannot be null");
			Check.notNull(currentParent.getId(), "Parent must be saved before adding children");
			final List<EntityTypeConfig<?>> entityTypes = createFilterableTypes(getExistingChildClasses());
			if (entityTypes.isEmpty()) {
				CNotificationService.showWarning("No compatible existing child items were found in this project");
				return;
			}
			final CDialogEntitySelection<CProjectItem<?>> dialog =
					new CDialogEntitySelection<>("Add Existing Child", entityTypes, this::listAvailableItemsForSelection, items -> {
						items.forEach(this::attachChildToParent);
						refreshSelection();
					}, true);
			dialog.open();
		} catch (final Exception e) {
			LOGGER.error("Failed to open Add Existing dialog reason={}", e.getMessage());
			CNotificationService.showException("Failed to add existing items", e);
		}
	}

	private void on_buttonAddNew_clicked() {
		try {
			Check.notNull(currentParent, "Parent cannot be null");
			Check.notNull(currentParent.getId(), "Parent must be saved before creating children");
			final List<EntityTypeConfig<?>> entityTypes = createFilterableTypes(getCreatableChildClasses());
			if (entityTypes.isEmpty()) {
				CNotificationService.showWarning("No child-capable entity types match this parent's hierarchy level");
				return;
			}
			if (entityTypes.size() == 1) {
				createNewChildEntity(entityTypes.get(0));
				return;
			}
			final List<EntityTypeConfig<?>> concreteTypes = new java.util.ArrayList<>();
			for (final EntityTypeConfig<?> config : entityTypes) {
				if (!isAllTypesConfig(config)) {
					concreteTypes.add(config);
				}
			}
			final CDialogAgileChildTypeSelection dialog = new CDialogAgileChildTypeSelection(concreteTypes, this::createNewChildEntity);
			dialog.open();
		} catch (final Exception e) {
			LOGGER.error("Failed to create new child reason={}", e.getMessage());
			CNotificationService.showException("Failed to create child", e);
		}
	}

	private void on_buttonEdit_clicked() {
		try {
			final CProjectItem<?> selected = getSingleSelectedItem();
			if (selected == null) {
				CNotificationService.showWarning("Please select one item");
				return;
			}
			openEditDialog(selected, this::refreshSelection);
		} catch (final Exception e) {
			LOGGER.error("Failed to open edit dialog reason={}", e.getMessage());
			CNotificationService.showException("Failed to open edit", e);
		}
	}

	private void on_buttonRemove_clicked() {
		try {
			final CProjectItem<?> selected = getSingleSelectedItem();
			if (selected == null) {
				CNotificationService.showWarning("Please select one item");
				return;
			}
			parentRelationService.clearParent(selected);
			saveEntity(selected);
			refreshSelection();
		} catch (final Exception e) {
			LOGGER.error("Failed to remove child from parent reason={}", e.getMessage());
			CNotificationService.showException("Failed to remove child", e);
		}
	}

	private void openEditDialog(final CProjectItem<?> entity, final Runnable onSaveSuccess) throws Exception {
		openEditDialog(entity, onSaveSuccess, null);
	}

	private void openEditDialog(final CProjectItem<?> entity, final Runnable onSaveSuccess, final Runnable onCancel) throws Exception {
		final String route = CDialogDynamicPage.buildDynamicRouteForEntity(entity);
		final CDialogDynamicPage dialog = new CDialogDynamicPage(route);
		dialog.configureInlineSaveCancelMode(onSaveSuccess, onCancel);
		dialog.open();
	}

	private void refreshButtonStates() {
		final boolean hasParent = currentParent != null;
		final boolean parentSaved = hasParent && currentParent.getId() != null;
		final boolean hasSelection = componentEntitySelection != null && !componentEntitySelection.getSelectedItems().isEmpty();
		buttonAddNew.setEnabled(parentSaved && CHierarchyNavigationService.canHaveChildren(currentParent));
		buttonAddExisting.setEnabled(parentSaved && CHierarchyNavigationService.canHaveChildren(currentParent));
		buttonEdit.setEnabled(parentSaved && hasSelection);
		buttonRemove.setEnabled(parentSaved && hasSelection);
	}

	@Override
	protected void refreshComponent() {
		if (currentParent == null) {
			infoDiv.setText("Select a parent item to manage children.");
			selectionContainer.setVisible(false);
			setChildDetailsValue(null);
			refreshButtonStates();
			return;
		}
		if (currentParent.getId() == null) {
			infoDiv.setText("Please save '%s' before managing children.".formatted(currentParent.getName()));
			selectionContainer.setVisible(false);
			setChildDetailsValue(null);
			refreshButtonStates();
			return;
		}
		if (!CHierarchyNavigationService.canHaveChildren(currentParent)) {
			infoDiv.setText("This item's type is configured as a leaf, so no child items can be attached.");
			selectionContainer.setVisible(false);
			setChildDetailsValue(null);
			refreshButtonStates();
			return;
		}
		infoDiv.setText("Children of '%s' (level %s)".formatted(currentParent.getName(),
				CHierarchyNavigationService.getEntityLevel(currentParent)));
		ensureSelectionComponent();
		selectionContainer.setVisible(true);
		refreshSelection();
	}

	private void refreshSelection() {
		if (componentEntitySelection != null) {
			componentEntitySelection.refreshGrid();
		}
		syncChildDetails();
		refreshButtonStates();
	}

	@SuppressWarnings({
			"rawtypes", "unchecked"
	})
	private void saveEntity(final CProjectItem<?> entity) {
		final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(entity.getClass());
		final CAbstractService service = (CAbstractService) CSpringContext.getBean(serviceClass);
		service.save(entity);
	}

	private void ensureSelectionComponent() {
		if (componentEntitySelection != null) {
			return;
		}
		// The selection grid must represent both current children and creatable child types so filters stay stable after edits.
		// Explicit map type witness avoids wildcard-capture inference differences across compiler versions.
		final List<Class<? extends CProjectItem<?>>> supportedClasses = java.util.stream.Stream.concat(getCreatableChildClasses().stream(),
				parentRelationService.getChildren(currentParent).stream().<Class<? extends CProjectItem<?>>>map(this::resolveProjectItemClass)).distinct().toList();
		final List<EntityTypeConfig<?>> entityTypes = createFilterableTypes(supportedClasses);
		componentEntitySelection = new CComponentEntitySelection<>(entityTypes, this::listChildrenForSelection,
				selectedItems -> LOGGER.debug("Hierarchy children selection changed: {} item(s)", selectedItems.size()), false);
		// The wrapper keeps a stable DOM host even when the internal selection component rebuilds itself.
		selectionContainer.removeAll();
		selectionContainer.add(componentEntitySelection);
		componentEntitySelection.addValueChangeListener(event -> {
			refreshButtonStates();
			syncChildDetails();
		});
		setFlexGrow(1, selectionContainer);
	}

	private void setChildDetailsValue(final CEntityNamed<?> entity) {
		if (componentChildDetails != null) {
			componentChildDetails.setValue(entity);
			componentChildDetails.setVisible(entity != null);
		}
		if (detailsPlaceholder != null) {
			detailsPlaceholder.setVisible(entity != null && componentChildDetails == null);
		}
	}

	@Override
	public void setThis(final CProjectItem<?> value) {
		currentParent = value;
		refreshComponent();
	}

	private void syncChildDetails() {
		final CProjectItem<?> selectedItem = componentEntitySelection != null && componentEntitySelection.getSelectedItems().size() == 1
				? componentEntitySelection.getSelectedItems().iterator().next()
				: null;
		setChildDetailsValue(selectedItem);
	}
}
