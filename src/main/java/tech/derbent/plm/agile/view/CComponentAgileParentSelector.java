package tech.derbent.plm.agile.view;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.agileparentrelation.service.CAgileParentRelationService;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.interfaces.IComponentTransientPlaceHolder;
import tech.derbent.api.interfaces.IHasAgileParentRelation;
import tech.derbent.api.interfaces.IPageServiceAutoRegistrable;
import tech.derbent.api.page.view.CDialogDynamicPage;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.enhanced.CComponentBase;
import tech.derbent.api.ui.component.enhanced.CComponentEntitySelection.EntityTypeConfig;
import tech.derbent.api.ui.dialogs.CDialogEntitySelection;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.agile.domain.CEpic;
import tech.derbent.plm.agile.domain.CFeature;
import tech.derbent.plm.agile.domain.CUserStory;
import tech.derbent.plm.agile.service.CEpicService;
import tech.derbent.plm.agile.service.CFeatureService;
import tech.derbent.plm.agile.service.CUserStoryService;

/** Parent selector component for the agile hierarchy (Epic → Feature → UserStory → Activity/Meeting/Risk).
 * <p>
 * This component is designed for the transient-placeholder pattern:
 * - Entity holds a @Transient placeholder field with @AMetaData(createComponentMethod=...)
 * - CPageService.populateForm() calls {@link #setThis(CProjectItem)} with the current entity
 * - Component performs validated updates via {@link CAgileParentRelationService} and saves via entity registry.
 * </p> */
public class CComponentAgileParentSelector extends CComponentBase<CProjectItem<?>> implements IComponentTransientPlaceHolder<CProjectItem<?>>,
		IPageServiceAutoRegistrable {

	public static final String ID_ROOT = "custom-agile-parent-component";
	public static final String ID_BUTTON_SELECT = "custom-agile-parent-select-button";
	public static final String ID_BUTTON_CLEAR = "custom-agile-parent-clear-button";
	public static final String ID_BUTTON_EDIT = "custom-agile-parent-edit-button";

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentAgileParentSelector.class);
	private static final long serialVersionUID = 1L;

	private final CAgileParentRelationService agileParentRelationService;

	private CButton buttonClear;
	private CButton buttonEdit;
	private CButton buttonSelect;
	private Div infoDiv;
	private CProjectItem<?> currentEntity;

	public CComponentAgileParentSelector(final CAgileParentRelationService agileParentRelationService) {
		Check.notNull(agileParentRelationService, "agileParentRelationService cannot be null");
		this.agileParentRelationService = agileParentRelationService;
		initializeComponents();
	}

	@Override
	public String getComponentName() { return "agileParent"; }

	@Override
	public void setThis(final CProjectItem<?> value) {
		currentEntity = value;
		refreshComponent();
	}

	private void initializeComponents() {
		setId(ID_ROOT);
		setPadding(false);
		setSpacing(true);
		setWidthFull();

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
		infoDiv.getStyle().set("font-size", "var(--lumo-font-size-s)");
		infoDiv.getStyle().set("color", "var(--lumo-secondary-text-color)");

		add(toolbar, infoDiv);
		refreshButtonStates();
	}

	private void on_buttonClear_clicked() {
		try {
			Check.notNull(currentEntity, "currentEntity cannot be null");
			Check.isTrue(currentEntity instanceof IHasAgileParentRelation, "Entity does not support agile parent");
			agileParentRelationService.clearParent(currentEntity);
			saveEntity(currentEntity);
			refreshComponent();
		} catch (final Exception e) {
			LOGGER.error("Failed to clear agile parent reason={}", e.getMessage());
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
			final String route = CDialogDynamicPage.buildDynamicRouteForEntity((CEntityDB<?>) parent);
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

			final List<EntityTypeConfig<?>> parentTypes = createAllowedParentTypes(currentEntity);
			if (parentTypes.isEmpty()) {
				CNotificationService.showWarning("This item cannot have a parent");
				return;
			}

			final CDialogEntitySelection<CProjectItem<?>> dialog = new CDialogEntitySelection<>("Select Parent", parentTypes,
					config -> listParentsForType(config), selected -> {
						final CProjectItem<?> parent = selected != null && !selected.isEmpty() ? selected.get(0) : null;
						setParent(parent);
					}, false);
			dialog.open();
		} catch (final Exception e) {
			LOGGER.error("Failed to open parent selection dialog reason={}", e.getMessage());
			CNotificationService.showException("Failed to select parent", e);
		}
	}

	private void setParent(final CProjectItem<?> parent) {
		try {
			Check.notNull(currentEntity, "currentEntity cannot be null");
			agileParentRelationService.setParent(currentEntity, parent);
			saveEntity(currentEntity);
			refreshComponent();
		} catch (final Exception e) {
			LOGGER.error("Failed to set agile parent reason={}", e.getMessage());
			CNotificationService.showException("Failed to set parent", e);
		}
	}

	private List<CProjectItem<?>> listParentsForType(final EntityTypeConfig<?> config) {
		try {
			final List<CProjectItem<?>> items = new ArrayList<>();
			final CAbstractService<?> service = config.getService();
			if (service instanceof CEntityOfProjectService<?>) {
				final CEntityOfProjectService<?> projectService = (CEntityOfProjectService<?>) service;
				projectService.listByProject(currentEntity.getProject()).forEach(entity -> {
					if (entity instanceof CProjectItem<?> item && item.getId() != null && !item.getId().equals(currentEntity.getId())) {
						items.add(item);
					}
				});
			}
			return items;
		} catch (final Exception e) {
			LOGGER.error("Failed to list parent candidates reason={}", e.getMessage());
			return new ArrayList<>();
		}
	}

	private static List<EntityTypeConfig<?>> createAllowedParentTypes(final CProjectItem<?> entity) {
		final List<EntityTypeConfig<?>> types = new ArrayList<>();
		if (entity instanceof CEpic) {
			return types;
		}
		if (entity instanceof CFeature) {
			types.add(EntityTypeConfig.createWithRegistryName(CEpic.class, CSpringContext.getBean(CEpicService.class)));
		} else if (entity instanceof CUserStory) {
			types.add(EntityTypeConfig.createWithRegistryName(CFeature.class, CSpringContext.getBean(CFeatureService.class)));
		} else {
			types.add(EntityTypeConfig.createWithRegistryName(CUserStory.class, CSpringContext.getBean(CUserStoryService.class)));
		}
		return types;
	}

	private CProjectItem<?> getCurrentParent() {
		if (!(currentEntity instanceof IHasAgileParentRelation)) {
			return null;
		}
		return ((IHasAgileParentRelation) currentEntity).getParentItem();
	}

	@SuppressWarnings ({
			"rawtypes", "unchecked"
	})
	private void saveEntity(final CProjectItem<?> entity) {
		final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(entity.getClass());
		final CAbstractService service = (CAbstractService) CSpringContext.getBean(serviceClass);
		service.save((CEntityDB<?>) entity);
	}

	private void refreshButtonStates() {
		final boolean hasEntity = currentEntity != null;
		final boolean saved = hasEntity && currentEntity.getId() != null;
		final boolean canHaveParent = hasEntity && !(currentEntity instanceof CEpic);
		final boolean hasParent = getCurrentParent() != null;

		buttonSelect.setEnabled(saved && canHaveParent);
		buttonClear.setEnabled(saved && canHaveParent && hasParent);
		buttonEdit.setEnabled(saved && canHaveParent && hasParent);
	}

	@Override
	protected void refreshComponent() {
		if (currentEntity == null) {
			infoDiv.setText("Select an item to manage parent.");
			refreshButtonStates();
			return;
		}
		if (currentEntity.getId() == null) {
			infoDiv.setText("Please save '%s' before selecting parent.".formatted(currentEntity.getName()));
			refreshButtonStates();
			return;
		}
		if (currentEntity instanceof CEpic) {
			infoDiv.setText("Epic is a root item (no parent).");
			refreshButtonStates();
			return;
		}
		final CProjectItem<?> parent = getCurrentParent();
		infoDiv.setText(parent != null ? "Parent: %s".formatted(parent.getName()) : "Parent: (none)");
		refreshButtonStates();
	}

	public Component getInfoComponent() { return infoDiv; }
}
