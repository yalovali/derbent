package tech.derbent.plm.agile.view;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.IComponentTransientPlaceHolder;
import tech.derbent.api.interfaces.IHasParentRelation;
import tech.derbent.api.interfaces.IPageServiceAutoRegistrable;
import tech.derbent.api.page.view.CDialogDynamicPage;
import tech.derbent.api.parentrelation.service.CHierarchyNavigationService;
import tech.derbent.api.parentrelation.service.CParentRelationService;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.enhanced.CComponentBase;
import tech.derbent.api.ui.component.enhanced.CComponentEntitySelection.EntityTypeConfig;
import tech.derbent.api.ui.component.enhanced.CComponentItemDetails;
import tech.derbent.api.ui.dialogs.CDialogEntitySelection;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;

/**
 * Generic hierarchy parent selector backed by type level semantics.
 *
 * <p>The class name is kept for backward compatibility with existing screen metadata, but the
 * implementation no longer assumes agile-only entity classes.</p>
 */
public class CComponentAgileParentSelector extends CComponentBase<CProjectItem<?>>
		implements IComponentTransientPlaceHolder<CProjectItem<?>>, IPageServiceAutoRegistrable {

	public static final String ID_BUTTON_CLEAR = "custom-agile-parent-clear-button";
	public static final String ID_BUTTON_EDIT = "custom-agile-parent-edit-button";
	public static final String ID_BUTTON_SELECT = "custom-agile-parent-select-button";
	public static final String ID_ROOT = "custom-agile-parent-component";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentAgileParentSelector.class);
	private static final long serialVersionUID = 1L;

	private final CParentRelationService parentRelationService;
	private final CHierarchyNavigationService hierarchyNavigationService;
	private CButton buttonClear;
	private CButton buttonEdit;
	private CButton buttonSelect;
	private CComponentItemDetails componentParentDetails;
	private CProjectItem<?> currentEntity;
	private Div detailsPlaceholder;
	private Div infoDiv;

	public CComponentAgileParentSelector(final CParentRelationService parentRelationService) {
		Check.notNull(parentRelationService, "parentRelationService cannot be null");
		this.parentRelationService = parentRelationService;
		hierarchyNavigationService = CSpringContext.getBean(CHierarchyNavigationService.class);
		initializeComponents();
	}

	private List<EntityTypeConfig<?>> createAllowedParentTypes() {
		final List<CProjectItem<?>> candidates = hierarchyNavigationService.listParentCandidates(currentEntity);
		final List<Class<? extends CProjectItem<?>>> entityClasses = candidates.stream().map(this::resolveProjectItemClass).distinct().sorted(
				Comparator.comparing(entityClass -> {
			final String title = CEntityRegistry.getEntityTitleSingular(entityClass);
			return title != null ? title : entityClass.getSimpleName();
		}, String.CASE_INSENSITIVE_ORDER)).toList();
		final List<EntityTypeConfig<?>> entityTypes = new ArrayList<>();
		for (final Class<? extends CProjectItem<?>> entityClass : entityClasses) {
			entityTypes.add(createEntityTypeConfig(entityClass));
		}
		return entityTypes;
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

	@Override
	public String getComponentName() { return "agileParent"; }

	private CProjectItem<?> getCurrentParent() {
		if (!(currentEntity instanceof IHasParentRelation hasParentRelation)) {
			return null;
		}
		return hasParentRelation.getParentItem();
	}

	public Component getInfoComponent() { return infoDiv; }

	@SuppressWarnings("unchecked")
	private Class<? extends CProjectItem<?>> resolveProjectItemClass(final CProjectItem<?> item) {
		return (Class<? extends CProjectItem<?>>) item.getClass();
	}

	private void initializeComponents() {
		setId(ID_ROOT);
		setPadding(false);
		setSpacing(true);
		setWidthFull();

		// The compact toolbar keeps hierarchy actions near the summary so parent editing stays discoverable.
		buttonSelect = new CButton("Select", VaadinIcon.LIST_SELECT.create());
		buttonSelect.setId(ID_BUTTON_SELECT);
		buttonSelect.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		buttonSelect.addClickListener(event -> on_buttonSelect_clicked());

		buttonClear = new CButton("Clear", VaadinIcon.TRASH.create());
		buttonClear.setId(ID_BUTTON_CLEAR);
		buttonClear.addThemeVariants(ButtonVariant.LUMO_ERROR);
		buttonClear.addClickListener(event -> on_buttonClear_clicked());

		buttonEdit = new CButton("Edit", VaadinIcon.EDIT.create());
		buttonEdit.setId(ID_BUTTON_EDIT);
		buttonEdit.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		buttonEdit.addClickListener(event -> on_buttonEdit_clicked());

		final CHorizontalLayout toolbar = new CHorizontalLayout(buttonSelect, buttonEdit, buttonClear);
		toolbar.setPadding(false);
		toolbar.setSpacing(true);
		toolbar.setWidthFull();

		infoDiv = new Div();
		infoDiv.setWidthFull();
		infoDiv.getStyle().set("padding", "var(--lumo-space-m)").set("border-radius", "var(--lumo-border-radius-l)")
				.set("background", "var(--lumo-contrast-10pct)").set("border", "1px solid var(--lumo-contrast-20pct)")
				.set("box-shadow", "var(--lumo-box-shadow-xs)").set("box-sizing", "border-box").set("min-width", "0");
		add(toolbar, infoDiv);

		initializeParentDetailsComponent();
		refreshButtonStates();
	}

	private void initializeParentDetailsComponent() {
		try {
			componentParentDetails = new CComponentItemDetails(CSpringContext.getBean(ISessionService.class));
			componentParentDetails.setWidthFull();
			componentParentDetails.setMinHeight("240px");
			componentParentDetails.setVisible(false);
			add(componentParentDetails);
		} catch (final Exception e) {
			LOGGER.error("Failed to initialize hierarchy parent details component reason={}", e.getMessage());
			detailsPlaceholder = new Div();
			detailsPlaceholder.setText("Selected parent details are currently unavailable.");
			detailsPlaceholder.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "var(--lumo-font-size-s)")
					.set("padding", "var(--lumo-space-s)");
			detailsPlaceholder.setVisible(false);
			add(detailsPlaceholder);
		}
	}

	private List<CProjectItem<?>> listParentsForType(final EntityTypeConfig<?> config) {
		if (config == null || currentEntity == null) {
			return List.of();
		}
		final List<CProjectItem<?>> candidates = new ArrayList<>(hierarchyNavigationService.listParentCandidates(currentEntity));
		return candidates.stream().filter(candidate -> config.getEntityClass().isAssignableFrom(candidate.getClass())).toList();
	}

	private void on_buttonClear_clicked() {
		try {
			Check.notNull(currentEntity, "currentEntity cannot be null");
			Check.isTrue(currentEntity instanceof IHasParentRelation, "Entity does not support parent relation");
			parentRelationService.clearParent(currentEntity);
			saveEntity(currentEntity);
			updateValueFromClient(null);
			refreshComponent();
		} catch (final Exception e) {
			LOGGER.error("Failed to clear hierarchy parent reason={}", e.getMessage());
			CNotificationService.showException("Failed to clear parent", e);
		}
	}

	private void on_buttonEdit_clicked() {
		try {
			final CProjectItem<?> parent = getCurrentParent();
			if (parent == null) {
				CNotificationService.showWarning("No parent selected");
				return;
			}
			final String route = CDialogDynamicPage.buildDynamicRouteForEntity(parent);
			final CDialogDynamicPage dialog = new CDialogDynamicPage(route);
			dialog.open();
		} catch (final Exception e) {
			LOGGER.error("Failed to open parent edit dialog reason={}", e.getMessage());
			CNotificationService.showException("Failed to open parent", e);
		}
	}

	private void on_buttonSelect_clicked() {
		try {
			Check.notNull(currentEntity, "currentEntity cannot be null");
			Check.notNull(currentEntity.getId(), "Entity must be saved before selecting parent");
			final List<EntityTypeConfig<?>> parentTypes = createAllowedParentTypes();
			if (parentTypes.isEmpty()) {
				CNotificationService.showWarning("This item cannot have a parent with the current type configuration");
				return;
			}
			final CDialogEntitySelection<CProjectItem<?>> dialog =
					new CDialogEntitySelection<>("Select Parent", parentTypes, this::listParentsForType, selected -> {
						final CProjectItem<?> parent = selected != null && !selected.isEmpty() ? selected.get(0) : null;
						setParent(parent);
					}, false);
			dialog.open();
		} catch (final Exception e) {
			LOGGER.error("Failed to open parent selection dialog reason={}", e.getMessage());
			CNotificationService.showException("Failed to select parent", e);
		}
	}

	private void refreshButtonStates() {
		final boolean hasEntity = currentEntity != null;
		final boolean saved = hasEntity && currentEntity.getId() != null;
		final boolean canHaveParent = hasEntity && CHierarchyNavigationService.canEntityHaveParent(currentEntity);
		final boolean hasParent = getCurrentParent() != null;
		buttonSelect.setEnabled(saved && canHaveParent);
		buttonClear.setEnabled(saved && canHaveParent && hasParent);
		buttonEdit.setEnabled(saved && canHaveParent && hasParent);
	}

	@Override
	protected void refreshComponent() {
		if (currentEntity == null) {
			infoDiv.setText("Select an item to manage parent.");
			setParentDetailsValue(null);
			refreshButtonStates();
			return;
		}
		if (currentEntity.getId() == null) {
			infoDiv.setText("Please save '%s' before selecting parent.".formatted(currentEntity.getName()));
			setParentDetailsValue(null);
			refreshButtonStates();
			return;
		}
		if (!CHierarchyNavigationService.canEntityHaveParent(currentEntity)) {
			infoDiv.setText("This item is configured as a root-level hierarchy item.");
			setParentDetailsValue(null);
			refreshButtonStates();
			return;
		}
		final CProjectItem<?> parent = getCurrentParent();
		renderParentSummary(parent);
		setParentDetailsValue(parent);
		refreshButtonStates();
	}

	private void renderParentSummary(final CProjectItem<?> parent) {
		infoDiv.removeAll();
		if (parent == null) {
			final Span placeholderText = new Span("Parent: (none)");
			placeholderText.getStyle().set("font-size", "var(--lumo-font-size-m)").set("color", "var(--lumo-secondary-text-color)");
			infoDiv.add(placeholderText);
			return;
		}

		// The summary highlights both hierarchy depth and concrete entity type so mixed hierarchies stay readable.
		final Span sectionTitle = new Span("Current Parent");
		sectionTitle.getStyle().set("display", "block").set("font-size", "var(--lumo-font-size-xs)").set("font-weight", "700")
				.set("letter-spacing", "0.04em").set("text-transform", "uppercase").set("color", "var(--lumo-secondary-text-color)")
				.set("margin-bottom", "var(--lumo-space-xs)");
		infoDiv.add(sectionTitle);

		final String typeLabel = CEntityRegistry.getEntityTitleSingular(parent.getClass()) != null
				? CEntityRegistry.getEntityTitleSingular(parent.getClass())
				: parent.getClass().getSimpleName();
		final Span summary = new Span("#%s · %s · level %s".formatted(parent.getId(), typeLabel,
				CHierarchyNavigationService.getEntityLevel(parent)));
		summary.getStyle().set("display", "block").set("font-size", "var(--lumo-font-size-s)")
				.set("color", "var(--lumo-secondary-text-color)");
		infoDiv.add(summary);

		final Span name = new Span(parent.getName());
		name.getStyle().set("display", "block").set("font-size", "var(--lumo-font-size-l)").set("font-weight", "600");
		infoDiv.add(name);
	}

	@SuppressWarnings({
			"rawtypes", "unchecked"
	})
	private void saveEntity(final CProjectItem<?> entity) {
		final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(entity.getClass());
		final CAbstractService service = (CAbstractService) CSpringContext.getBean(serviceClass);
		service.save(entity);
	}

	private void setParent(final CProjectItem<?> parent) {
		try {
			Check.notNull(currentEntity, "currentEntity cannot be null");
			parentRelationService.setParent(currentEntity, parent);
			saveEntity(currentEntity);
			updateValueFromClient(parent);
			refreshComponent();
		} catch (final Exception e) {
			LOGGER.error("Failed to set hierarchy parent reason={}", e.getMessage());
			CNotificationService.showException("Failed to set parent", e);
		}
	}

	private void setParentDetailsValue(final CEntityNamed<?> entity) {
		if (componentParentDetails != null) {
			componentParentDetails.setValue(entity);
			componentParentDetails.setVisible(entity != null);
		}
		if (detailsPlaceholder != null) {
			detailsPlaceholder.setVisible(entity != null && componentParentDetails == null);
		}
	}

	@Override
	public void setThis(final CProjectItem<?> value) {
		currentEntity = value;
		setValue(getCurrentParent());
		refreshComponent();
	}
}
