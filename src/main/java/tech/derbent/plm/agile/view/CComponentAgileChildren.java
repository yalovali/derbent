package tech.derbent.plm.agile.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.ProxyUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.agileparentrelation.service.CAgileParentRelationService;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.interfaces.IComponentTransientPlaceHolder;
import tech.derbent.api.interfaces.IHasAgileParentRelation;
import tech.derbent.api.interfaces.IPageServiceAutoRegistrable;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.page.view.CDialogDynamicPage;
import tech.derbent.api.projects.domain.CProject;
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
import tech.derbent.plm.activities.domain.CActivity;
import tech.derbent.plm.activities.service.CActivityService;
import tech.derbent.plm.agile.domain.CEpic;
import tech.derbent.plm.agile.domain.CFeature;
import tech.derbent.plm.agile.domain.CUserStory;
import tech.derbent.plm.agile.service.CFeatureService;
import tech.derbent.plm.agile.service.CUserStoryService;
import tech.derbent.plm.meetings.domain.CMeeting;
import tech.derbent.plm.meetings.service.CMeetingService;
import tech.derbent.plm.risks.risk.domain.CRisk;
import tech.derbent.plm.risks.risk.service.CRiskService;

/** Rich relation component for managing agile children of an agile parent item.
 * <p>
 * First iteration: provides the standard CRUD toolbar + filtered selection grid (reuses {@link CComponentEntitySelection}).
 * </p>
 * <p>
 * KEYWORDS: AgileHierarchy, AgileChildren, CAgileParentRelation, placeHolder_createComponentAgileChildren, AddExistingChild, CreateNewChild,
 * RemoveChild, CDialogEntitySelection, CDialogDynamicPage, CComponentEntitySelection, Playwright:CAgileChildrenCrudTest
 * </p>
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

	private static List<EntityTypeConfig<?>> createAllowedChildTypes(final CProjectItem<?> parent) {
		final List<EntityTypeConfig<?>> entityTypes = new ArrayList<>();
		// Services from Spring context (same pattern as CComponentBacklog)
		final CActivityService activityService = CSpringContext.getBean(CActivityService.class);
		final CMeetingService meetingService = CSpringContext.getBean(CMeetingService.class);
		final CRiskService riskService = CSpringContext.getBean(CRiskService.class);
		final CFeatureService featureService = CSpringContext.getBean(CFeatureService.class);
		final CUserStoryService userStoryService = CSpringContext.getBean(CUserStoryService.class);
		if (parent instanceof CEpic) {
			entityTypes.add(EntityTypeConfig.createWithRegistryName(CFeature.class, featureService));
		} else if (parent instanceof CFeature) {
			entityTypes.add(EntityTypeConfig.createWithRegistryName(CUserStory.class, userStoryService));
		} else if (parent instanceof CUserStory) {
			entityTypes.add(EntityTypeConfig.createWithRegistryName(CActivity.class, activityService));
			entityTypes.add(EntityTypeConfig.createWithRegistryName(CMeeting.class, meetingService));
			entityTypes.add(EntityTypeConfig.createWithRegistryName(CRisk.class, riskService));
		}
		if (entityTypes.isEmpty()) {
			entityTypes.add(EntityTypeConfig.createWithRegistryName(CActivity.class, activityService));
		}
		return entityTypes;
	}

	@SuppressWarnings ({
			"rawtypes", "unchecked"
	})
	private static EntityTypeConfig<?> createAllTypesConfig(final EntityTypeConfig<?> firstType) {
		return new EntityTypeConfig(ALL_TYPES_DISPLAY_NAME, firstType.getEntityClass(), firstType.getService());
	}

	private static List<EntityTypeConfig<?>> createFilterableChildTypes(final CProjectItem<?> parent) {
		final List<EntityTypeConfig<?>> entityTypes = new ArrayList<>(createAllowedChildTypes(parent));
		if (entityTypes.size() <= 1) {
			return entityTypes;
		}
		final EntityTypeConfig<?> firstType = entityTypes.get(0);
		entityTypes.add(0, createAllTypesConfig(firstType));
		return entityTypes;
	}

	private static boolean isAllTypesConfig(final EntityTypeConfig<?> config) {
		return config != null && ALL_TYPES_DISPLAY_NAME.equals(config.getDisplayName());
	}

	private final CAgileParentRelationService agileParentRelationService;
	private CButton buttonAddExisting;
	private CButton buttonAddNew;
	private CButton buttonEdit;
	private CButton buttonRemove;
	private CComponentItemDetails componentChildDetails;
	private CComponentEntitySelection<CProjectItem<?>> componentEntitySelection;
	private CProjectItem<?> currentParent;
	private Div detailsPlaceholder;
	private Div infoDiv;
	private final ISessionService sessionService;

	public CComponentAgileChildren(final CAgileParentRelationService agileParentRelationService, final CPageEntityService pageEntityService,
			final ISessionService sessionService) {
		Check.notNull(agileParentRelationService, "agileParentRelationService cannot be null");
		Check.notNull(pageEntityService, "pageEntityService cannot be null");
		Check.notNull(sessionService, "sessionService cannot be null");
		this.agileParentRelationService = agileParentRelationService;
		this.sessionService = sessionService;
		initializeComponents();
	}

	private void attachChildToParent(final CProjectItem<?> child) {
		Check.notNull(child, "child cannot be null");
		agileParentRelationService.setParent(child, currentParent);
		saveEntity(child);
		LOGGER.debug("Attached child '{}' ({}:{}) to parent '{}' ({}:{})", child.getName(), child.getClass().getSimpleName(), child.getId(),
				currentParent != null ? currentParent.getName() : null,
				currentParent != null ? ProxyUtils.getUserClass(currentParent.getClass()).getSimpleName() : null,
				currentParent != null ? currentParent.getId() : null);
	}

	@SuppressWarnings ({
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
			agileParentRelationService.setParent(savedChild, currentParent);
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

	private void ensureSelectionComponent() {
		if (componentEntitySelection != null) {
			return;
		}
		final List<EntityTypeConfig<?>> entityTypes = createFilterableChildTypes(currentParent);
		final CComponentEntitySelection.ItemsProvider<CProjectItem<?>> itemsProvider = this::listChildrenForSelection;
		componentEntitySelection = new CComponentEntitySelection<>(entityTypes, itemsProvider,
				selectedItems -> LOGGER.debug("Agile children selection changed: {} items selected", selectedItems.size()), false);
		componentEntitySelection.setId(ID_SELECTION);
		componentEntitySelection.addValueChangeListener(event -> {
			refreshButtonStates();
			syncChildDetails();
		});
		addComponentAtIndex(2, componentEntitySelection);
		setFlexGrow(1, componentEntitySelection);
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
		add(toolbar, infoDiv);
		initializeDetailsComponent(sessionService);
		refreshButtonStates();
	}

	private void initializeDetailsComponent(final ISessionService sessionService1) {
		try {
			componentChildDetails = new CComponentItemDetails(sessionService1);
			componentChildDetails.setWidthFull();
			componentChildDetails.setMinHeight("240px");
			componentChildDetails.setVisible(false);
			add(componentChildDetails);
		} catch (final Exception e) {
			LOGGER.error("Failed to initialize agile child details component reason={}", e.getMessage());
			detailsPlaceholder = new Div();
			detailsPlaceholder.setText("Selected child details are currently unavailable.");
			detailsPlaceholder.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "var(--lumo-font-size-s)").set("padding",
					"var(--lumo-space-s)");
			detailsPlaceholder.setVisible(false);
			add(detailsPlaceholder);
		}
	}

	private boolean isChildOfCurrentParent(final CProjectItem<?> item) {
		if (currentParent == null || currentParent.getId() == null || item == null || item.getId() == null) {
			return false;
		}
		if (!(item instanceof IHasAgileParentRelation)) {
			return false;
		}
		final IHasAgileParentRelation hasRelation = (IHasAgileParentRelation) item;
		final Long parentItemId = hasRelation.getAgileParentRelation() != null ? hasRelation.getAgileParentRelation().getParentItemId() : null;
		final String parentItemType = hasRelation.getAgileParentRelation() != null ? hasRelation.getAgileParentRelation().getParentItemType() : null;
		if (parentItemId == null || parentItemType == null) {
			return false;
		}
		return currentParent.getId().equals(parentItemId) && ProxyUtils.getUserClass(currentParent.getClass()).getSimpleName().equals(parentItemType);
	}

	private List<CProjectItem<?>> listAvailableItemsForSelection(final EntityTypeConfig<?> config) {
		try {
			final CProject<?> project = currentParent != null ? currentParent.getProject() : null;
			if (project == null) {
				return new ArrayList<>();
			}
			if (isAllTypesConfig(config)) {
				final List<CProjectItem<?>> items = new ArrayList<>();
				createAllowedChildTypes(currentParent).forEach(type -> items.addAll(listAvailableItemsForSelection(type)));
				return items;
			}
			final List<CProjectItem<?>> items = new ArrayList<>();
			final CAbstractService<?> service = config.getService();
			if (service instanceof CEntityOfProjectService<?>) {
				final CEntityOfProjectService<?> projectService = (CEntityOfProjectService<?>) service;
				projectService.listByProject(project).forEach(entity -> {
					if (entity instanceof final CProjectItem<?> item && item.getId() != null && !isChildOfCurrentParent(item)) {
						items.add(item);
					}
				});
			}
			return items;
		} catch (final Exception e) {
			LOGGER.error("Failed to list available items reason={}", e.getMessage());
			return new ArrayList<>();
		}
	}

	private List<CProjectItem<?>> listChildrenForSelection(final EntityTypeConfig<?> config) {
		try {
			if (currentParent == null || currentParent.getId() == null) {
				return new ArrayList<>();
			}
			if (isAllTypesConfig(config)) {
				final List<CProjectItem<?>> items = new ArrayList<>();
				createAllowedChildTypes(currentParent).forEach(type -> items.addAll(listChildrenForSelection(type)));
				return items;
			}
			final CProject<?> project = currentParent.getProject();
			final List<CProjectItem<?>> items = new ArrayList<>();
			final CAbstractService<?> service = config.getService();
			if (service instanceof CEntityOfProjectService<?>) {
				final CEntityOfProjectService<?> projectService = (CEntityOfProjectService<?>) service;
				projectService.listByProject(project).forEach(entity -> {
					if (entity instanceof final CProjectItem<?> item && item.getId() != null && isChildOfCurrentParent(item)) {
						items.add(item);
					}
				});
			}
			return items;
		} catch (final Exception e) {
			LOGGER.error("Failed to list children reason={}", e.getMessage());
			return new ArrayList<>();
		}
	}

	private void on_buttonAddExisting_clicked() {
		try {
			Check.notNull(currentParent, "Parent cannot be null");
			Check.notNull(currentParent.getId(), "Parent must be saved before adding children");
			final List<EntityTypeConfig<?>> entityTypes = createFilterableChildTypes(currentParent);
			final CDialogEntitySelection<CProjectItem<?>> dialog =
					new CDialogEntitySelection<>("Add Existing Child", entityTypes, config -> listAvailableItemsForSelection(config), items -> {
						LOGGER.debug("Add Existing selection confirmed: {} items", items != null ? items.size() : 0);
						items.forEach(item -> attachChildToParent(item));
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
			final List<EntityTypeConfig<?>> entityTypes = createAllowedChildTypes(currentParent);
			if (entityTypes.size() == 1) {
				createNewChildEntity(entityTypes.get(0));
				return;
			}
			final CDialogAgileChildTypeSelection dialog = new CDialogAgileChildTypeSelection(entityTypes, this::createNewChildEntity);
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
			agileParentRelationService.clearParent(selected);
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
		buttonAddNew.setEnabled(parentSaved);
		buttonAddExisting.setEnabled(parentSaved);
		buttonEdit.setEnabled(parentSaved && hasSelection);
		buttonRemove.setEnabled(parentSaved && hasSelection);
	}

	@Override
	protected void refreshComponent() {
		final String parentLabel = currentParent != null ? currentParent.getName() : null;
		if (currentParent == null) {
			infoDiv.setText("Select a parent item to manage children.");
			setChildDetailsValue(null);
			refreshButtonStates();
			return;
		}
		if (currentParent.getId() == null) {
			infoDiv.setText("Please save '%s' before managing children.".formatted(parentLabel));
			setChildDetailsValue(null);
			refreshButtonStates();
			return;
		}
		infoDiv.setText("Children of '%s'".formatted(parentLabel));
		ensureSelectionComponent();
		refreshSelection();
	}

	private void refreshSelection() {
		if (componentEntitySelection != null) {
			componentEntitySelection.refreshGrid();
		}
		syncChildDetails();
		refreshButtonStates();
	}

	@SuppressWarnings ({
			"rawtypes", "unchecked"
	})
	private void saveEntity(final CProjectItem<?> entity) {
		final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(entity.getClass());
		final CAbstractService service = (CAbstractService) CSpringContext.getBean(serviceClass);
		service.save(entity);
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
				? componentEntitySelection.getSelectedItems().iterator().next() : null;
		if (selectedItem != null) {
			setChildDetailsValue(selectedItem);
		} else {
			setChildDetailsValue(null);
		}
	}
}
