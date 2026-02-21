package tech.derbent.bab.policybase.rule.view;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.interfaces.IComponentTransientPlaceHolder;
import tech.derbent.api.interfaces.IPageServiceAutoRegistrable;
import tech.derbent.api.page.domain.CPageEntity;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.page.view.CDialogDynamicPage;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.enhanced.CComponentBase;
import tech.derbent.api.ui.component.enhanced.CComponentEntitySelection;
import tech.derbent.api.ui.dialogs.CDialogEntitySelection;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.policybase.action.domain.CBabPolicyAction;
import tech.derbent.bab.policybase.action.service.CBabPolicyActionService;
import tech.derbent.bab.policybase.rule.domain.CBabPolicyRule;
import tech.derbent.bab.policybase.rule.service.CBabPolicyRuleService;

/** Rich relation component for managing policy-rule actions with dialog-based CRUD. */
public class CComponentPolicyRuleActions extends CComponentBase<CBabPolicyRule>
		implements IComponentTransientPlaceHolder<CBabPolicyRule>, IPageServiceAutoRegistrable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentPolicyRuleActions.class);
	private static final long serialVersionUID = 1L;
	private final CBabPolicyActionService actionService;
	private final CButton buttonAddExisting;
	private final CButton buttonAddNew;
	private final CButton buttonEdit;
	private final CButton buttonRemove;
	private final CGrid<CBabPolicyAction> gridActions;
	private final CPageEntityService pageEntityService;
	private final CBabPolicyRuleService ruleService;
	private final ISessionService sessionService;

	public CComponentPolicyRuleActions(final CBabPolicyRuleService ruleService, final CBabPolicyActionService actionService,
			final CPageEntityService pageEntityService, final ISessionService sessionService) {
		this.ruleService = ruleService;
		this.actionService = actionService;
		this.pageEntityService = pageEntityService;
		this.sessionService = sessionService;
		setPadding(false);
		setSpacing(true);
		setWidthFull();
		buttonAddNew = new CButton("New Action", VaadinIcon.PLUS.create());
		buttonAddNew.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		buttonAddNew.addClickListener(event -> on_buttonAddNew_clicked());
		buttonAddExisting = new CButton("Add Existing", VaadinIcon.LIST_SELECT.create());
		buttonAddExisting.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		buttonAddExisting.addClickListener(event -> on_buttonAddExisting_clicked());
		buttonEdit = new CButton("Edit", VaadinIcon.EDIT.create());
		buttonEdit.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		buttonEdit.addClickListener(event -> on_buttonEdit_clicked());
		buttonRemove = new CButton("Remove", VaadinIcon.TRASH.create());
		buttonRemove.addThemeVariants(ButtonVariant.LUMO_ERROR);
		buttonRemove.addClickListener(event -> on_buttonRemove_clicked());
		final CHorizontalLayout toolbar = new CHorizontalLayout(buttonAddNew, buttonAddExisting, buttonEdit, buttonRemove);
		toolbar.setSpacing(true);
		toolbar.setPadding(false);
		toolbar.setWidthFull();
		gridActions = new CGrid<>(CBabPolicyAction.class);
		CGrid.setupGrid(gridActions);
		gridActions.removeAllColumns();
		CGrid.styleColumnHeader(gridActions.addColumn(CBabPolicyAction::getName).setAutoWidth(true).setSortable(true), "Action");
		CGrid.styleColumnHeader(
				gridActions.addColumn(action -> action.getDestinationNode() != null ? action.getDestinationNode().getName() : "").setAutoWidth(true),
				"Destination");
		CGrid.styleColumnHeader(
				gridActions.addColumn(action -> action.getActionMask() != null ? action.getActionMask().getName() : "").setAutoWidth(true), "Mask");
		CGrid.styleColumnHeader(gridActions.addColumn(CBabPolicyAction::getExecutionPriority).setAutoWidth(true).setSortable(true), "Priority");
		CGrid.styleColumnHeader(gridActions.addColumn(CBabPolicyAction::getExecutionOrder).setAutoWidth(true).setSortable(true), "Order");
		CGrid.styleColumnHeader(gridActions.addColumn(CBabPolicyAction::getActive).setAutoWidth(true).setSortable(true), "Active");
		gridActions.asSingleSelect().addValueChangeListener(event -> refreshButtonStates());
		gridActions.addItemDoubleClickListener(event -> {
			gridActions.asSingleSelect().setValue(event.getItem());
			on_buttonEdit_clicked();
		});
		gridActions.setHeight("260px");
		gridActions.setWidthFull();
		add(toolbar, gridActions);
		refreshButtonStates();
	}

	private void ensureRuleSaved() {
		Check.notNull(getValue(), "No policy rule selected");
		Check.notNull(getValue().getId(), "Please save the policy rule before managing actions");
	}

	private List<CBabPolicyAction> getActionsForCurrentRule() {
		final CBabPolicyRule rule = getValue();
		if (rule == null || rule.getActions() == null) {
			return List.of();
		}
		return rule.getActions().stream()
				.sorted(Comparator.comparing(CBabPolicyAction::getExecutionOrder, Comparator.nullsLast(Integer::compareTo))
						.thenComparing(CBabPolicyAction::getExecutionPriority, Comparator.nullsLast(Integer::compareTo)).reversed()
						.thenComparing(CBabPolicyAction::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
				.toList();
	}

	private List<CBabPolicyAction> getAlreadySelectedActions() {
		final CBabPolicyRule rule = getValue();
		if (rule == null || rule.getActions() == null) {
			return List.of();
		}
		return new ArrayList<>(rule.getActions());
	}

	@Override
	public String getComponentName() { return "policyRuleActions"; }

	private CBabPolicyAction getSelectedAction() { return gridActions.asSingleSelect().getValue(); }

	private void on_actionsSelectedFromList(final List<CBabPolicyAction> selectedItems) {
		try {
			final CBabPolicyRule rule = getValue();
			Check.notNull(rule, "No rule selected");
			final Set<CBabPolicyAction> updatedActions = new LinkedHashSet<>(rule.getActions() != null ? rule.getActions() : Set.of());
			updatedActions.addAll(selectedItems);
			rule.setActions(updatedActions);
			ruleService.save(rule);
			refreshComponent();
			CNotificationService.showSaveSuccess();
		} catch (final Exception e) {
			LOGGER.error("Error linking selected actions to rule", e);
			CNotificationService.showException("Failed to add selected actions", e);
		}
	}

	private void on_buttonAddExisting_clicked() {
		try {
			final CProject<?> activeProject = resolveActiveProject();
			final List<CComponentEntitySelection.EntityTypeConfig<?>> entityTypes =
					List.of(CComponentEntitySelection.EntityTypeConfig.createWithRegistryName(CBabPolicyAction.class, actionService));
			final CDialogEntitySelection<CBabPolicyAction> dialog = new CDialogEntitySelection<>("Select Existing Policy Actions", entityTypes,
					config -> actionService.listByProject(activeProject), this::on_actionsSelectedFromList, true,
					config -> getAlreadySelectedActions(), CComponentEntitySelection.AlreadySelectedMode.HIDE_ALREADY_SELECTED);
			dialog.open();
		} catch (final Exception e) {
			LOGGER.error("Error opening action selection dialog", e);
			CNotificationService.showException("Failed to open action selector", e);
		}
	}

	private void on_buttonAddNew_clicked() {
		try {
			ensureRuleSaved();
			openDynamicActionDialog(null);
		} catch (final Exception e) {
			LOGGER.error("Error opening action create dialog", e);
			CNotificationService.showException("Failed to open action creation dialog", e);
		}
	}

	private void on_buttonEdit_clicked() {
		try {
			final CBabPolicyAction selectedAction = getSelectedAction();
			Check.notNull(selectedAction, "Please select an action to edit");
			openDynamicActionDialog(selectedAction);
		} catch (final Exception e) {
			LOGGER.error("Error opening action editor", e);
			CNotificationService.showException("Failed to open action editor", e);
		}
	}

	private void on_buttonRemove_clicked() {
		try {
			final CBabPolicyRule rule = getValue();
			final CBabPolicyAction selectedAction = getSelectedAction();
			Check.notNull(rule, "No rule selected");
			Check.notNull(selectedAction, "Please select an action to remove");
			final Set<CBabPolicyAction> updatedActions = new LinkedHashSet<>(rule.getActions());
			updatedActions.removeIf(action -> action == selectedAction
					|| action.getId() != null && selectedAction.getId() != null && action.getId().equals(selectedAction.getId()));
			rule.setActions(updatedActions);
			ruleService.save(rule);
			refreshComponent();
			CNotificationService.showDeleteSuccess();
		} catch (final Exception e) {
			LOGGER.error("Error removing action from rule", e);
			CNotificationService.showException("Failed to remove action", e);
		}
	}

	private void openDynamicActionDialog(final CBabPolicyAction action) throws Exception {
		final CProject<?> activeProject = resolveActiveProject();
		final CPageEntity actionPage = pageEntityService.findByNameAndProject(CBabPolicyAction.VIEW_NAME, activeProject)
				.orElseThrow(() -> new IllegalStateException("Policy Actions page is not initialized for project " + activeProject.getName()));
		final String route = CDialogDynamicPage.buildDynamicRoute(actionPage.getId(), action != null ? action.getId() : null);
		final CDialogDynamicPage dialog = CDialogDynamicPage.fromRoute(route);
		dialog.addOpenedChangeListener(event -> {
			if (!event.isOpened()) {
				refreshComponent();
			}
		});
		dialog.open();
	}

	private void refreshButtonStates() {
		final boolean hasRule = getValue() != null && getValue().getId() != null;
		final boolean hasSelection = getSelectedAction() != null;
		buttonAddNew.setEnabled(hasRule);
		buttonAddExisting.setEnabled(hasRule);
		buttonEdit.setEnabled(hasRule && hasSelection);
		buttonRemove.setEnabled(hasRule && hasSelection);
	}

	@Override
	protected void refreshComponent() {
		gridActions.setItems(getActionsForCurrentRule());
		refreshButtonStates();
	}

	private CProject<?> resolveActiveProject() {
		return sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project in session"));
	}

	@Override
	public void setThis(final CBabPolicyRule value) {
		setValue(value);
	}
}
