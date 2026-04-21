package tech.derbent.plm.agile.view;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.api.pagequery.domain.CFilterOption;
import tech.derbent.api.pagequery.domain.CPageViewFilterSpecialValue;
import tech.derbent.api.pagequery.domain.CPageViewFilterVisibility;
import tech.derbent.api.pagequery.domain.CPageViewQueryKeys;
import tech.derbent.api.pagequery.ui.IDetailsMasterToolbarExtensionFactory;
import tech.derbent.api.pagequery.ui.IDetailsMasterToolbarExtensionInstance;
import tech.derbent.api.screens.view.CComponentGridEntity;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.ui.component.basic.CComboBox;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.users.service.CUserService;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.agile.domain.CEpic;
import tech.derbent.plm.agile.domain.CFeature;
import tech.derbent.plm.agile.domain.CUserStory;
import tech.derbent.plm.agile.service.CEpicService;
import tech.derbent.plm.agile.service.CFeatureService;
import tech.derbent.plm.agile.service.CUserStoryService;
import tech.derbent.plm.sprints.domain.CSprint;
import tech.derbent.plm.sprints.service.CSprintService;

/** CAgileDetailsMasterToolbarExtensionFactory - Adds Epic/Feature/UserStory/Responsible/Sprint filters to the master grid toolbar.
 * <p>
 * This extension is available in all profiles. Filters are shown only for entities that extend CProjectItem and implement agile hierarchy interfaces
 * (IHasEpicParent, IHasFeatureParent, IHasUserStoryParent). The factory's supports() method and CPageViewFilterVisibility ensure filters appear only
 * in appropriate contexts.
 * </p>
 */
@Service
public class CAgileDetailsMasterToolbarExtensionFactory implements IDetailsMasterToolbarExtensionFactory {

	private final class CAgileDetailsMasterToolbarExtensionInstance implements IDetailsMasterToolbarExtensionInstance {

		private static <T> T getValueOrNull(final CFilterOption<T> option) {
			if (option == null || option.isSelectAll() || option.isNone()) {
				return null;
			}
			return option.getValue();
		}

		private static boolean isSameEntity(final Object left, final Object right) {
			return CAgileToolbarSupport.isSameEntity(left, right);
		}

		private static <T> CFilterOption<T> preserveOptionOrSelectAll(final CFilterOption<T> option, final List<T> availableValues) {
			if (option == null) {
				return CFilterOption.selectAll();
			}
			if (option.isSelectAll() || option.isNone()) {
				return option;
			}
			final T value = option.getValue();
			if (value == null) {
				return CFilterOption.selectAll();
			}
			if (availableValues != null) {
				for (final T available : availableValues) {
					if (isSameEntity(available, value)) {
						return option;
					}
				}
			}
			return CFilterOption.selectAll();
		}

		private static <T> void refreshComboBox(final CComboBox<CFilterOption<T>> comboBox, final List<T> values, final String noneLabel) {
			Check.notNull(comboBox, "comboBox cannot be null");
			final CFilterOption<T> previous = comboBox.getValue();
			comboBox.setItems(toOptions(values, noneLabel));
			comboBox.setValue(preserveOptionOrSelectAll(previous, values));
		}

		private static CEpic resolveEpic(final CUserStory userStory) {
			return CAgileToolbarSupport.resolveEpic(userStory);
		}

		private static <T> List<CFilterOption<T>> toOptions(final List<T> values, final String noneLabel) {
			final List<CFilterOption<T>> options = new ArrayList<>();
			options.add(CFilterOption.selectAll());
			options.add(CFilterOption.none(noneLabel));
			if (values != null) {
				for (final T value : values) {
					final String label = value instanceof final CEntityNamed<?> named ? named.getName() : String.valueOf(value);
					options.add(CFilterOption.of(value, label));
				}
			}
			return options;
		}

		private List<CFeature> allFeatures = List.of();
		private List<CUserStory> allUserStories = List.of();
		private CComboBox<CFilterOption<CEpic>> comboBoxEpic;
		private CComboBox<CFilterOption<CFeature>> comboBoxFeature;
		private CComboBox<CFilterOption<CUser>> comboBoxResponsible;
		private CComboBox<CFilterOption<CSprint>> comboBoxSprint;
		private CComboBox<CFilterOption<CUserStory>> comboBoxUserStory;
		private final CComponentGridEntity grid;
		private boolean isInternalUpdate;
		private final ISessionService sessionService;
		private final CPageViewFilterVisibility visibility;

		private CAgileDetailsMasterToolbarExtensionInstance(final CComponentGridEntity grid, final CPageViewFilterVisibility visibility,
				final ISessionService sessionService) {
			this.grid = grid;
			this.visibility = visibility;
			this.sessionService = sessionService;
		}

		@Override
		public void addComponents(final List<Component> components) throws Exception {
			final var projectOpt = sessionService.getActiveProject().or(() -> java.util.Optional.ofNullable(grid.getCurrentProject()));
			// Always render the UI controls so the user understands what can be filtered.
			// If no active project exists yet (e.g. early UI boot), show disabled controls.
			final boolean hasProject = projectOpt.isPresent();
			final var project = hasProject ? projectOpt.get() : null;
			final String noneEpicLabel = "-- No Epic --";
			final String noneFeatureLabel = "-- No Feature --";
			final String noneUserStoryLabel = "-- No User Story --";
			final String noneResponsibleLabel = "-- Unassigned --";
			final String noneSprintLabel = "-- No Sprint --";
			final List<CEpic> epics = hasProject && visibility.isShowEpic() ? epicService.listByProject(project) : List.of();
			allFeatures = hasProject && visibility.isShowFeature() ? featureService.listByProject(project) : List.of();
			allUserStories = hasProject && visibility.isShowUserStory() ? userStoryService.listByProject(project) : List.of();
			final List<CUser> users = hasProject && visibility.isShowResponsible() ? userService.listByProject(project) : List.of();
			final List<CSprint> sprints = hasProject && visibility.isShowSprint() ? sprintService.listByProject(project) : List.of();
			if (visibility.isShowEpic()) {
				comboBoxEpic = createComboBox("Epic", ID_FILTER_EPIC, noneEpicLabel);
				comboBoxEpic.setItems(toOptions(epics, noneEpicLabel));
				comboBoxEpic.setValue(CFilterOption.selectAll());
				comboBoxEpic.setEnabled(hasProject);
				comboBoxEpic.addValueChangeListener(e -> {
					if (isInternalUpdate) {
						return;
					}
					applyEntityFilter(CPageViewQueryKeys.KEY_EPIC, e.getValue());
					refreshCascadingOptions(noneFeatureLabel, noneUserStoryLabel);
					applyEntityFilter(CPageViewQueryKeys.KEY_FEATURE, comboBoxFeature != null ? comboBoxFeature.getValue() : null);
					applyEntityFilter(CPageViewQueryKeys.KEY_USER_STORY, comboBoxUserStory != null ? comboBoxUserStory.getValue() : null);
				});
				components.add(comboBoxEpic);
			}
			if (visibility.isShowFeature()) {
				comboBoxFeature = createComboBox("Feature", ID_FILTER_FEATURE, noneFeatureLabel);
				comboBoxFeature.setItems(toOptions(allFeatures, noneFeatureLabel));
				comboBoxFeature.setValue(CFilterOption.selectAll());
				comboBoxFeature.setEnabled(hasProject);
				comboBoxFeature.addValueChangeListener(e -> {
					if (isInternalUpdate) {
						return;
					}
					applyEntityFilter(CPageViewQueryKeys.KEY_FEATURE, e.getValue());
					syncParentsFromFeature(getValueOrNull(e.getValue()), noneEpicLabel);
					applyEntityFilter(CPageViewQueryKeys.KEY_EPIC, comboBoxEpic != null ? comboBoxEpic.getValue() : null);
					refreshCascadingOptions(noneFeatureLabel, noneUserStoryLabel);
					applyEntityFilter(CPageViewQueryKeys.KEY_USER_STORY, comboBoxUserStory != null ? comboBoxUserStory.getValue() : null);
				});
				components.add(comboBoxFeature);
			}
			if (visibility.isShowUserStory()) {
				comboBoxUserStory = createComboBox("User Story", ID_FILTER_USER_STORY, noneUserStoryLabel);
				comboBoxUserStory.setItems(toOptions(allUserStories, noneUserStoryLabel));
				comboBoxUserStory.setValue(CFilterOption.selectAll());
				comboBoxUserStory.setEnabled(hasProject);
				comboBoxUserStory.addValueChangeListener(e -> {
					if (isInternalUpdate) {
						return;
					}
					applyEntityFilter(CPageViewQueryKeys.KEY_USER_STORY, e.getValue());
					syncParentsFromUserStory(getValueOrNull(e.getValue()), noneEpicLabel, noneFeatureLabel);
					applyEntityFilter(CPageViewQueryKeys.KEY_EPIC, comboBoxEpic != null ? comboBoxEpic.getValue() : null);
					applyEntityFilter(CPageViewQueryKeys.KEY_FEATURE, comboBoxFeature != null ? comboBoxFeature.getValue() : null);
					refreshCascadingOptions(noneFeatureLabel, noneUserStoryLabel);
				});
				components.add(comboBoxUserStory);
			}
			if (visibility.isShowResponsible()) {
				comboBoxResponsible = createComboBox("Responsible", ID_FILTER_RESPONSIBLE, noneResponsibleLabel);
				comboBoxResponsible.setItems(toOptions(users, noneResponsibleLabel));
				comboBoxResponsible.setValue(CFilterOption.selectAll());
				comboBoxResponsible.setEnabled(hasProject);
				comboBoxResponsible.addValueChangeListener(e -> {
					if (isInternalUpdate) {
						return;
					}
					applyEntityFilter(CPageViewQueryKeys.KEY_RESPONSIBLE, e.getValue());
				});
				components.add(comboBoxResponsible);
			}
			if (visibility.isShowSprint()) {
				comboBoxSprint = createComboBox("Sprint", ID_FILTER_SPRINT, noneSprintLabel);
				comboBoxSprint.setItems(toOptions(sprints, noneSprintLabel));
				comboBoxSprint.setValue(CFilterOption.selectAll());
				comboBoxSprint.setEnabled(hasProject);
				comboBoxSprint.addValueChangeListener(e -> {
					if (isInternalUpdate) {
						return;
					}
					applyEntityFilter(CPageViewQueryKeys.KEY_SPRINT, e.getValue());
				});
				components.add(comboBoxSprint);
			}
			refreshCascadingOptions(noneFeatureLabel, noneUserStoryLabel);
		}

		private void applyEntityFilter(final String key, final CFilterOption<?> option) {
			Check.notBlank(key, "key cannot be blank");
			if (option == null || option.isSelectAll()) {
				grid.setPageViewFilter(key, null);
				return;
			}
			if (option.isNone()) {
				grid.setPageViewFilter(key, CPageViewFilterSpecialValue.NO_VALUE);
				return;
			}
			grid.setPageViewFilter(key, option.getValue());
		}

		@Override
		public void clear() {
			isInternalUpdate = true;
			try {
				if (comboBoxEpic != null) {
					comboBoxEpic.setValue(CFilterOption.selectAll());
				}
				if (comboBoxFeature != null) {
					comboBoxFeature.setValue(CFilterOption.selectAll());
				}
				if (comboBoxUserStory != null) {
					comboBoxUserStory.setValue(CFilterOption.selectAll());
				}
				if (comboBoxResponsible != null) {
					comboBoxResponsible.setValue(CFilterOption.selectAll());
				}
				if (comboBoxSprint != null) {
					comboBoxSprint.setValue(CFilterOption.selectAll());
				}
			} finally {
				isInternalUpdate = false;
			}
		}

		private <T> CComboBox<CFilterOption<T>> createComboBox(final String label, final String id, final String noneLabel) {
			final CComboBox<CFilterOption<T>> comboBox = new CComboBox<>();
			comboBox.setId(id);
			comboBox.setLabel(label);
			comboBox.setWidth("220px");
			comboBox.setRenderer(new ComponentRenderer<>(option -> renderFilterOption(option, noneLabel)));
			comboBox.setItemLabelGenerator(CFilterOption::getLabel);
			return comboBox;
		}

		private Component createSpecialLabel(final String text, final com.vaadin.flow.component.icon.Icon icon) {
			final Span span = new Span(text);
			span.getStyle().set("color", "#666").set("font-style", "italic");
			if (icon != null) {
				icon.getStyle().set("width", "16px").set("height", "16px").set("color", "#666");
				final var layout = new com.vaadin.flow.component.orderedlayout.HorizontalLayout(icon, span);
				layout.setPadding(false);
				layout.setSpacing(true);
				layout.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
				return layout;
			}
			return span;
		}

		private List<CFeature> filterFeaturesByEpic(final CFilterOption<CEpic> epicOption) {
			if (epicOption == null || epicOption.isSelectAll()) {
				return allFeatures;
			}
			if (epicOption.isNone()) {
				final List<CFeature> result = new ArrayList<>();
				for (final CFeature feature : allFeatures) {
					final CProjectItem<?> parent = feature != null ? feature.getParentItem() : null;
					if (!(parent instanceof CEpic)) {
						result.add(feature);
					}
				}
				return result;
			}
			final CEpic epic = epicOption.getValue();
			if (epic == null) {
				return allFeatures;
			}
			return CAgileToolbarSupport.filterFeaturesByEpic(allFeatures, epic);
		}

		private List<CUserStory> filterUserStories(final CFilterOption<CEpic> epicOption, final CFilterOption<CFeature> featureOption) {
			final CFilterOption<CEpic> safeEpicOption = epicOption != null ? epicOption : CFilterOption.selectAll();
			final CFilterOption<CFeature> safeFeatureOption = featureOption != null ? featureOption : CFilterOption.selectAll();
			// Feature takes precedence over Epic for determining the UserStory list.
			if (!safeFeatureOption.isSelectAll()) {
				if (safeFeatureOption.isNone()) {
					final List<CUserStory> result = new ArrayList<>();
					for (final CUserStory userStory : allUserStories) {
						if (!(userStory.getParentItem() instanceof CFeature)) {
							result.add(userStory);
						}
					}
					return result;
				}
				final CFeature feature = safeFeatureOption.getValue();
				if (feature == null) {
					return allUserStories;
				}
				final List<CUserStory> result = new ArrayList<>();
				for (final CUserStory userStory : allUserStories) {
					if (userStory.getParentItem() instanceof final CFeature parentFeature && isSameEntity(parentFeature, feature)) {
						result.add(userStory);
					}
				}
				return result;
			}
			if (safeEpicOption.isSelectAll()) {
				return allUserStories;
			}
			if (safeEpicOption.isNone()) {
				final List<CUserStory> result = new ArrayList<>();
				for (final CUserStory userStory : allUserStories) {
					if (resolveEpic(userStory) == null) {
						result.add(userStory);
					}
				}
				return result;
			}
			final CEpic epic = safeEpicOption.getValue();
			if (epic == null) {
				return allUserStories;
			}
			return CAgileToolbarSupport.filterUserStories(allUserStories, epic, null);
		}

		private void refreshCascadingOptions(final String noneFeatureLabel, final String noneUserStoryLabel) {
			isInternalUpdate = true;
			try {
				final CFilterOption<CEpic> epicOption = comboBoxEpic != null ? comboBoxEpic.getValue() : CFilterOption.selectAll();
				CFilterOption<CFeature> featureOption = comboBoxFeature != null ? comboBoxFeature.getValue() : CFilterOption.selectAll();
				if (comboBoxFeature != null) {
					final List<CFeature> filteredFeatures = filterFeaturesByEpic(epicOption);
					refreshComboBox(comboBoxFeature, filteredFeatures, noneFeatureLabel);
					featureOption = comboBoxFeature.getValue();
				}
				if (comboBoxUserStory != null) {
					final List<CUserStory> filteredUserStories = filterUserStories(epicOption, featureOption);
					refreshComboBox(comboBoxUserStory, filteredUserStories, noneUserStoryLabel);
				}
			} finally {
				isInternalUpdate = false;
			}
		}

		private Component renderFilterOption(final CFilterOption<?> option, final String noneLabel) {
			if (option == null) {
				return new Span("");
			}
			if (option.isSelectAll()) {
				return createSpecialLabel("-- All --", VaadinIcon.ASTERISK.create());
			}
			if (option.isNone()) {
				return createSpecialLabel(option.getLabel() != null ? option.getLabel() : noneLabel, VaadinIcon.CLOSE_SMALL.create());
			}
			final Object value = option.getValue();
			if (value instanceof final CUser user) {
				return CLabelEntity.createUserLabel(user);
			}
			if (value instanceof final CEntityDB<?> entity) {
				try {
					return new CLabelEntity(entity);
				} catch (final Exception e) {
					LOGGER.debug("Failed to render entity option: {}", e.getMessage());
					return new Span(option.getLabel());
				}
			}
			return new Span(option.getLabel());
		}

		private void syncParentsFromFeature(final CFeature feature, final String noneEpicLabel) {
			if (feature == null) {
				return;
			}
			isInternalUpdate = true;
			try {
				if (comboBoxEpic != null) {
					comboBoxEpic.setValue(toOptionOrNone(feature.getParentItem(), noneEpicLabel));
				}
			} finally {
				isInternalUpdate = false;
			}
		}

		private void syncParentsFromUserStory(final CUserStory userStory, final String noneEpicLabel, final String noneFeatureLabel) {
			if (userStory == null) {
				return;
			}
			isInternalUpdate = true;
			try {
				if (comboBoxFeature != null) {
					if (userStory.getParentItem() instanceof final CFeature feature) {
						comboBoxFeature.setValue(CFilterOption.of(feature, feature.getName()));
					} else {
						comboBoxFeature.setValue(CFilterOption.none(noneFeatureLabel));
					}
				}
				if (comboBoxEpic != null) {
					comboBoxEpic.setValue(toOptionOrNone(resolveEpic(userStory), noneEpicLabel));
				}
			} finally {
				isInternalUpdate = false;
			}
		}

		private CFilterOption<CEpic> toOptionOrNone(final CProjectItem<?> possibleEpic, final String noneLabel) {
			if (possibleEpic == null) {
				return CFilterOption.none(noneLabel);
			}
			if (possibleEpic instanceof final CEpic epic) {
				return CFilterOption.of(epic, epic.getName());
			}
			return CFilterOption.none(noneLabel);
		}
	}

	private static final String ID_FILTER_EPIC = "custom-master-filter-epic";
	private static final String ID_FILTER_FEATURE = "custom-master-filter-feature";
	private static final String ID_FILTER_RESPONSIBLE = "custom-master-filter-responsible";
	private static final String ID_FILTER_SPRINT = "custom-master-filter-sprint";
	private static final String ID_FILTER_USER_STORY = "custom-master-filter-user-story";
	private static final Logger LOGGER = LoggerFactory.getLogger(CAgileDetailsMasterToolbarExtensionFactory.class);
	private final CEpicService epicService;
	private final CFeatureService featureService;
	private final CSprintService sprintService;
	private final CUserService userService;
	private final CUserStoryService userStoryService;

	public CAgileDetailsMasterToolbarExtensionFactory(final CEpicService epicService, final CFeatureService featureService,
			final CUserStoryService userStoryService, final CUserService userService, final CSprintService sprintService) {
		this.epicService = epicService;
		this.featureService = featureService;
		this.userStoryService = userStoryService;
		this.userService = userService;
		this.sprintService = sprintService;
	}

	@Override
	public IDetailsMasterToolbarExtensionInstance create(final CComponentGridEntity grid, final CPageViewFilterVisibility visibility,
			final ISessionService sessionService) throws Exception {
		Check.notNull(grid, "grid cannot be null");
		Check.notNull(visibility, "visibility cannot be null");
		Check.notNull(sessionService, "sessionService cannot be null");
		if (!visibility.isAnyAgileFilterVisible()) {
			return null;
		}
		return new CAgileDetailsMasterToolbarExtensionInstance(grid, visibility, sessionService);
	}

	@Override
	public boolean supports(final Class<?> entityClass) {
		// Only makes sense for project items (agile hierarchy + sprint + responsible).
		return CProjectItem.class.isAssignableFrom(entityClass);
	}
}
