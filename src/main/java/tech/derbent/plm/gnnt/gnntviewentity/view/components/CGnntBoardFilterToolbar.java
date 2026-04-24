package tech.derbent.plm.gnnt.gnntviewentity.view.components;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.springframework.data.util.ProxyUtils;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CComboBox;
import tech.derbent.api.ui.component.basic.CColorAwareComboBox;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CTextField;
import tech.derbent.api.ui.component.filter.CFilterToolbarSupport;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.users.service.CUserService;
import tech.derbent.plm.agile.view.CAgileToolbarSupport;
import tech.derbent.plm.agile.domain.CEpic;
import tech.derbent.plm.agile.domain.CFeature;
import tech.derbent.plm.agile.domain.CUserStory;
import tech.derbent.plm.agile.service.CEpicService;
import tech.derbent.plm.agile.service.CFeatureService;
import tech.derbent.plm.agile.service.CUserStoryService;
import tech.derbent.plm.gnnt.gnntviewentity.domain.CGnntBoardFilterCriteria;
import tech.derbent.plm.gnnt.gnntitem.domain.CGnntItem;
import tech.derbent.plm.sprints.domain.CSprint;
import tech.derbent.plm.sprints.service.CSprintService;

public class CGnntBoardFilterToolbar extends CHorizontalLayout {

	public static final String ID_TOOLBAR = "custom-gnnt-filter-toolbar";
	private static final long serialVersionUID = 1L;

	private final CButton buttonClear;
	private final CColorAwareComboBox<CEpic> comboBoxEpic;
	private final CComboBox<Class<?>> comboBoxEntityType;
	private final CColorAwareComboBox<CFeature> comboBoxFeature;
	private final CColorAwareComboBox<CUser> comboBoxResponsible;
	private final CColorAwareComboBox<CSprint> comboBoxSprint;
	private final CColorAwareComboBox<CUserStory> comboBoxUserStory;
	private CProject<?> currentProject;
	private final CEpicService epicService;
	private final CFeatureService featureService;
	private final List<Consumer<CGnntBoardFilterCriteria>> filterChangeListeners = new ArrayList<>();
	private boolean internalUpdate;
	private final CTextField searchField;
	private final CSprintService sprintService;
	private final CUserService userService;
	private final CUserStoryService userStoryService;

	public CGnntBoardFilterToolbar() {
		epicService = CSpringContext.getBean(CEpicService.class);
		featureService = CSpringContext.getBean(CFeatureService.class);
		userStoryService = CSpringContext.getBean(CUserStoryService.class);
		userService = CSpringContext.getBean(CUserService.class);
		sprintService = CSpringContext.getBean(CSprintService.class);
		setId(ID_TOOLBAR);
		CFilterToolbarSupport.configureWrappingToolbar(this, "crud-toolbar");
		searchField = CFilterToolbarSupport.createSearchField("Search", "Search...", null, "220px", ValueChangeMode.EAGER, 250,
				value -> notifyFilterChangeListeners());
		comboBoxEpic = createEntityComboBox(CEpic.class, "Epic");
		comboBoxEntityType = new CComboBox<>("Type");
		comboBoxEntityType.setClearButtonVisible(true);
		comboBoxEntityType.setWidth("180px");
		comboBoxEntityType.setItemLabelGenerator(entityClass -> entityClass != null
				? CEntityRegistry.getEntityTitleSingular(entityClass) : "");
		comboBoxFeature = createEntityComboBox(CFeature.class, "Feature");
		comboBoxUserStory = createEntityComboBox(CUserStory.class, "User Story");
		comboBoxResponsible = createEntityComboBox(CUser.class, "Responsible");
		comboBoxSprint = createEntityComboBox(CSprint.class, "Sprint");
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

	private <T extends tech.derbent.api.entity.domain.CEntityDB<T>> CColorAwareComboBox<T> createEntityComboBox(final Class<T> entityClass,
			final String label) {
		final CColorAwareComboBox<T> comboBox = new CColorAwareComboBox<>(entityClass, label);
		comboBox.setClearButtonVisible(true);
		comboBox.setWidth("220px");
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

	private static <T extends tech.derbent.api.entity.domain.CEntityDB<T>> T preserveEntitySelection(final T selectedValue,
			final List<T> availableValues) {
		if (selectedValue == null || availableValues == null) {
			return null;
		}
		return availableValues.stream().filter(value -> value.getId() != null && value.getId().equals(selectedValue.getId())).findFirst().orElse(null);
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
		final List<CFeature> availableFeatures = currentProject != null
				? CAgileToolbarSupport.filterFeaturesByEpic(featureService.listByProject(currentProject), comboBoxEpic.getValue()) : List.of();
		final CFeature preservedFeature = preserveEntitySelection(comboBoxFeature.getValue(), availableFeatures);
		comboBoxFeature.setItems(availableFeatures);
		comboBoxFeature.setValue(preservedFeature);
	}

	private void refreshUserStoryOptions() {
		final List<CUserStory> availableUserStories = currentProject != null ? CAgileToolbarSupport.filterUserStories(
				userStoryService.listByProject(currentProject), comboBoxEpic.getValue(), comboBoxFeature.getValue()) : List.of();
		final CUserStory preservedUserStory = preserveEntitySelection(comboBoxUserStory.getValue(), availableUserStories);
		comboBoxUserStory.setItems(availableUserStories);
		comboBoxUserStory.setValue(preservedUserStory);
	}

	public void setProject(final CProject<?> project) {
		if (currentProject != null && project != null && currentProject.getId() != null && currentProject.getId().equals(project.getId())) {
			return;
		}
		currentProject = project;
		internalUpdate = true;
		try {
			final List<CEpic> epics = currentProject != null ? epicService.listByProject(currentProject) : List.of();
			final List<CFeature> features = currentProject != null ? featureService.listByProject(currentProject) : List.of();
			final List<CUserStory> userStories = currentProject != null ? userStoryService.listByProject(currentProject) : List.of();
			final List<CUser> users = currentProject != null ? userService.listByProject(currentProject) : List.of();
			final List<CSprint> sprints = currentProject != null ? sprintService.listByProject(currentProject) : List.of();
			comboBoxEpic.setItems(epics);
			comboBoxEpic.setValue(preserveEntitySelection(comboBoxEpic.getValue(), epics));
			comboBoxResponsible.setItems(users);
			comboBoxResponsible.setValue(preserveEntitySelection(comboBoxResponsible.getValue(), users));
			comboBoxSprint.setItems(sprints);
			comboBoxSprint.setValue(preserveEntitySelection(comboBoxSprint.getValue(), sprints));
			comboBoxFeature.setItems(features);
			comboBoxFeature.setValue(preserveEntitySelection(comboBoxFeature.getValue(), features));
			comboBoxUserStory.setItems(userStories);
			comboBoxUserStory.setValue(preserveEntitySelection(comboBoxUserStory.getValue(), userStories));
			refreshFeatureOptions();
			refreshUserStoryOptions();
		} finally {
			internalUpdate = false;
		}
	}

	private static <T> T preserveClassSelection(final T selectedValue, final List<T> availableValues) {
		if (selectedValue == null || availableValues == null) {
			return null;
		}
		return availableValues.stream().filter(selectedValue::equals).findFirst().orElse(null);
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
