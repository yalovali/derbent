package tech.derbent.plm.gnnt.gnntviewentity.view.components;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import org.springframework.data.util.ProxyUtils;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.data.value.ValueChangeMode;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.parentrelation.service.CHierarchyNavigationService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CComboBox;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CTextField;
import tech.derbent.api.ui.component.filter.CFilterToolbarSupport;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.users.service.CUserService;
import tech.derbent.plm.agile.view.CAgileToolbarSupport;
import tech.derbent.plm.gnnt.gnntitem.domain.CGnntItem;
import tech.derbent.plm.gnnt.gnntviewentity.domain.CGnntBoardFilterCriteria;
import tech.derbent.plm.sprints.domain.CSprint;
import tech.derbent.plm.sprints.service.CSprintService;

/**
 * Gnnt toolbar with hierarchy anchors based on type levels instead of hard-coded agile classes.
 *
 * <p>The field names remain stable for compatibility, but the UI labels now describe the actual level
 * semantics that drive filtering.</p>
 */
public class CGnntBoardFilterToolbar extends CHorizontalLayout {

	public static final String ID_TOOLBAR = "custom-gnnt-filter-toolbar";
	private static final long serialVersionUID = 1L;

	private final CButton buttonClear;
	private final CComboBox<CProjectItem<?>> comboBoxEpic;
	private final CComboBox<Class<?>> comboBoxEntityType;
	private final CComboBox<CProjectItem<?>> comboBoxFeature;
	private final CComboBox<CUser> comboBoxResponsible;
	private final CComboBox<CSprint> comboBoxSprint;
	private final CComboBox<CProjectItem<?>> comboBoxUserStory;
	private CProject<?> currentProject;
	private final List<Consumer<CGnntBoardFilterCriteria>> filterChangeListeners = new ArrayList<>();
	private final CHierarchyNavigationService hierarchyNavigationService;
	private boolean internalUpdate;
	private final CTextField searchField;
	private final CSprintService sprintService;
	private final CUserService userService;

	public CGnntBoardFilterToolbar() {
		hierarchyNavigationService = CSpringContext.getBean(CHierarchyNavigationService.class);
		userService = CSpringContext.getBean(CUserService.class);
		sprintService = CSpringContext.getBean(CSprintService.class);
		setId(ID_TOOLBAR);
		CFilterToolbarSupport.configureWrappingToolbar(this, "crud-toolbar");

		searchField = CFilterToolbarSupport.createSearchField("Search", "Search...", null, null, ValueChangeMode.EAGER, 250,
				value -> notifyFilterChangeListeners());

		comboBoxEpic = createHierarchyComboBox("Level 0");
		comboBoxEntityType = new CComboBox<>("Type");
		comboBoxEntityType.setClearButtonVisible(true);
		comboBoxEntityType.setWidth("180px");
		comboBoxEntityType.setItemLabelGenerator(entityClass -> entityClass != null
				? CEntityRegistry.getEntityTitleSingular(entityClass)
				: "");

		comboBoxFeature = createHierarchyComboBox("Level 1");
		comboBoxUserStory = createHierarchyComboBox("Level 2");
		comboBoxResponsible = createEntityComboBox("Responsible");
		comboBoxSprint = createEntityComboBox("Sprint");

		comboBoxEpic.addValueChangeListener(event -> {
			if (internalUpdate) {
				return;
			}
			refreshFeatureOptions();
			refreshUserStoryOptions();
			notifyFilterChangeListeners();
		});
		comboBoxFeature.addValueChangeListener(event -> {
			if (internalUpdate) {
				return;
			}
			syncParentsFromFeature();
			refreshUserStoryOptions();
			notifyFilterChangeListeners();
		});
		comboBoxUserStory.addValueChangeListener(event -> {
			if (internalUpdate) {
				return;
			}
			syncParentsFromUserStory();
			notifyFilterChangeListeners();
		});
		comboBoxEntityType.addValueChangeListener(event -> notifyFilterChangeListeners());
		comboBoxResponsible.addValueChangeListener(event -> notifyFilterChangeListeners());
		comboBoxSprint.addValueChangeListener(event -> notifyFilterChangeListeners());

		buttonClear = CButton.createTertiary("Clear", null, event -> clearFilters());
		buttonClear.addThemeVariants(ButtonVariant.LUMO_SMALL);
		add(searchField, comboBoxEntityType, comboBoxEpic, comboBoxFeature, comboBoxUserStory, comboBoxResponsible, comboBoxSprint, buttonClear);
	}

	public void addFilterChangeListener(final Consumer<CGnntBoardFilterCriteria> listener) {
		if (listener != null) {
			filterChangeListeners.add(listener);
		}
	}

	public void clearFilters() {
		internalUpdate = true;
		try {
			searchField.clear();
			comboBoxEntityType.clear();
			comboBoxEpic.clear();
			comboBoxFeature.clear();
			comboBoxUserStory.clear();
			comboBoxResponsible.clear();
			comboBoxSprint.clear();
			refreshFeatureOptions();
			refreshUserStoryOptions();
		} finally {
			internalUpdate = false;
		}
		notifyFilterChangeListeners();
	}

	private <T> CComboBox<T> createEntityComboBox(final String label) {
		final CComboBox<T> comboBox = new CComboBox<>(label);
		comboBox.setClearButtonVisible(true);
		comboBox.setWidth("220px");
		return comboBox;
	}

	private CComboBox<CProjectItem<?>> createHierarchyComboBox(final String label) {
		final CComboBox<CProjectItem<?>> comboBox = createEntityComboBox(label);
		comboBox.setItemLabelGenerator(item -> {
			if (item == null) {
				return "";
			}
			final String title = CEntityRegistry.getEntityTitleSingular(item.getClass()) != null
					? CEntityRegistry.getEntityTitleSingular(item.getClass())
					: item.getClass().getSimpleName();
			return "%s · %s".formatted(title, item.getName());
		});
		return comboBox;
	}

	public CGnntBoardFilterCriteria getCurrentCriteria() {
		final CGnntBoardFilterCriteria criteria = new CGnntBoardFilterCriteria();
		criteria.setSearchText(searchField.getValue());
		criteria.setEntityType(comboBoxEntityType.getValue());
		criteria.setEpic(comboBoxEpic.getValue());
		criteria.setFeature(comboBoxFeature.getValue());
		criteria.setUserStory(comboBoxUserStory.getValue());
		criteria.setResponsible(comboBoxResponsible.getValue());
		criteria.setSprint(comboBoxSprint.getValue());
		return criteria;
	}

	private void notifyFilterChangeListeners() {
		if (internalUpdate) {
			return;
		}
		final CGnntBoardFilterCriteria criteria = getCurrentCriteria();
		for (final Consumer<CGnntBoardFilterCriteria> listener : filterChangeListeners) {
			listener.accept(criteria);
		}
	}

	private static <T> T preserveClassSelection(final T selectedValue, final List<T> availableValues) {
		if (selectedValue == null || availableValues == null) {
			return null;
		}
		return availableValues.stream().filter(selectedValue::equals).findFirst().orElse(null);
	}

	private static CProjectItem<?> preserveHierarchySelection(final CProjectItem<?> selectedValue, final List<CProjectItem<?>> availableValues) {
		if (selectedValue == null || availableValues == null) {
			return null;
		}
		return availableValues.stream().filter(candidate -> CHierarchyNavigationService.isSameEntity(candidate, selectedValue)).findFirst().orElse(null);
	}

	public void setAvailableEntityTypes(final List<CGnntItem> items) {
		final List<Class<?>> entityTypes = new ArrayList<>();
		if (items != null) {
			items.stream().map(CGnntItem::getEntity)
					.filter(entity -> entity != null)
					.map(entity -> (Class<?>) ProxyUtils.getUserClass(entity.getClass()))
					.distinct()
					.sorted(Comparator.comparing(entityClass -> {
						final String title = CEntityRegistry.getEntityTitleSingular(entityClass);
						return title != null ? title : entityClass.getSimpleName();
					}))
					.forEach(entityTypes::add);
		}
		internalUpdate = true;
		try {
			final Class<?> preservedType = preserveClassSelection(comboBoxEntityType.getValue(), entityTypes);
			comboBoxEntityType.setItems(entityTypes);
			comboBoxEntityType.setValue(preservedType);
		} finally {
			internalUpdate = false;
		}
	}

	private void refreshFeatureOptions() {
		final List<CProjectItem<?>> availableLevel1Items = currentProject != null
				? CAgileToolbarSupport.filterByAncestorLevel(hierarchyNavigationService.listItemsAtLevel(currentProject, 1), 0, comboBoxEpic.getValue())
				: List.of();
		final CProjectItem<?> preservedFeature = preserveHierarchySelection(comboBoxFeature.getValue(), availableLevel1Items);
		comboBoxFeature.setItems(availableLevel1Items);
		comboBoxFeature.setValue(preservedFeature);
	}

	private void refreshUserStoryOptions() {
		List<CProjectItem<?>> availableLevel2Items = currentProject != null ? hierarchyNavigationService.listItemsAtLevel(currentProject, 2) : List.of();
		availableLevel2Items = CAgileToolbarSupport.filterByAncestorLevel(availableLevel2Items, 0, comboBoxEpic.getValue());
		availableLevel2Items = CAgileToolbarSupport.filterByAncestorLevel(availableLevel2Items, 1, comboBoxFeature.getValue());
		final CProjectItem<?> preservedUserStory = preserveHierarchySelection(comboBoxUserStory.getValue(), availableLevel2Items);
		comboBoxUserStory.setItems(availableLevel2Items);
		comboBoxUserStory.setValue(preservedUserStory);
	}

	public void setProject(final CProject<?> project) {
		if (currentProject != null && project != null && currentProject.getId() != null && currentProject.getId().equals(project.getId())) {
			return;
		}
		currentProject = project;
		internalUpdate = true;
		try {
			final List<CProjectItem<?>> level0Items = currentProject != null ? hierarchyNavigationService.listItemsAtLevel(currentProject, 0) : List.of();
			final List<CProjectItem<?>> level1Items = currentProject != null ? hierarchyNavigationService.listItemsAtLevel(currentProject, 1) : List.of();
			final List<CProjectItem<?>> level2Items = currentProject != null ? hierarchyNavigationService.listItemsAtLevel(currentProject, 2) : List.of();
			final List<CUser> users = currentProject != null ? userService.listByProject(currentProject) : List.of();
			final List<CSprint> sprints = currentProject != null ? sprintService.listByProject(currentProject) : List.of();

			comboBoxEpic.setItems(level0Items);
			comboBoxEpic.setValue(preserveHierarchySelection(comboBoxEpic.getValue(), level0Items));
			comboBoxFeature.setItems(level1Items);
			comboBoxFeature.setValue(preserveHierarchySelection(comboBoxFeature.getValue(), level1Items));
			comboBoxUserStory.setItems(level2Items);
			comboBoxUserStory.setValue(preserveHierarchySelection(comboBoxUserStory.getValue(), level2Items));
			comboBoxResponsible.setItems(users);
			comboBoxResponsible.setValue(preserveClassSelection(comboBoxResponsible.getValue(), users));
			comboBoxSprint.setItems(sprints);
			comboBoxSprint.setValue(preserveClassSelection(comboBoxSprint.getValue(), sprints));
			refreshFeatureOptions();
			refreshUserStoryOptions();
		} finally {
			internalUpdate = false;
		}
	}

	private void syncParentsFromFeature() {
		if (comboBoxFeature.getValue() == null) {
			return;
		}
		internalUpdate = true;
		try {
			comboBoxEpic.setValue(CAgileToolbarSupport.resolveEpic(comboBoxFeature.getValue()));
		} finally {
			internalUpdate = false;
		}
	}

	private void syncParentsFromUserStory() {
		if (comboBoxUserStory.getValue() == null) {
			return;
		}
		internalUpdate = true;
		try {
			comboBoxFeature.setValue(CAgileToolbarSupport.resolveFeature(comboBoxUserStory.getValue()));
			comboBoxEpic.setValue(CAgileToolbarSupport.resolveEpic(comboBoxUserStory.getValue()));
			refreshFeatureOptions();
		} finally {
			internalUpdate = false;
		}
	}
}
