package tech.derbent.bab.policybase.action.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.interfaces.IComponentTransientPlaceHolder;
import tech.derbent.api.interfaces.IPageServiceAutoRegistrable;
import tech.derbent.api.page.domain.CPageEntity;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.page.view.CDynamicPageRouter;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.component.enhanced.CComponentBase;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.policybase.action.domain.CBabPolicyAction;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskBase;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskCAN;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskFile;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskROS;
import tech.derbent.api.projects.domain.CProject;

/** Displays the selected action mask dynamic detail view under the action mask selector. */
public class CComponentBabPolicyActionMaskDetails extends CComponentBase<CBabPolicyAction>
		implements IPageServiceAutoRegistrable, IComponentTransientPlaceHolder<CBabPolicyAction> {

	public static final String COMPONENT_NAME = "policyActionMaskDetails";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentBabPolicyActionMaskDetails.class);
	private static final long serialVersionUID = 1L;
	private final CDynamicPageRouter detailsRouter;
	private final CSpan labelStatus;
	private final CPageEntityService pageEntityService;
	private final ISessionService sessionService;

	public CComponentBabPolicyActionMaskDetails(final ISessionService sessionService) throws Exception {
		Check.notNull(sessionService, "ISessionService cannot be null");
		this.sessionService = sessionService;
		pageEntityService = CSpringContext.getBean(CPageEntityService.class);
		final CDetailSectionService detailSectionService = CSpringContext.getBean(CDetailSectionService.class);
		setPadding(false);
		setSpacing(false);
		setWidthFull();
		labelStatus = new CSpan("Select an action mask to view its details.");
		labelStatus.getStyle().set("color", "var(--lumo-secondary-text-color)");
		detailsRouter = new CDynamicPageRouter(pageEntityService, sessionService, detailSectionService, null);
		detailsRouter.setWidthFull();
		add(labelStatus, detailsRouter);
		refreshComponent();
	}

	private void clearDetailsRouter() {
		try {
			detailsRouter.loadSpecificPage(null, null, true, null);
		} catch (final Exception clearException) {
			LOGGER.debug("Failed to clear action mask details router. reason={}", clearException.getMessage());
		}
	}

	@Override
	public String getComponentName() { return COMPONENT_NAME; }

	@Override
	protected void onValueChanged(final CBabPolicyAction oldValue, final CBabPolicyAction newValue, final boolean fromClient) {
		super.onValueChanged(oldValue, newValue, fromClient);
		LOGGER.debug("ActionMaskDetails value changed fromClient={} oldAction={} newAction={}",
				fromClient, describeAction(oldValue), describeAction(newValue));
		refreshComponent();
	}

	@Override
	protected void refreshComponent() {
		final CBabPolicyAction action = getValue();
		if (action == null) {
			LOGGER.warn("ActionMaskDetails refresh skipped: action is null. sessionProjectId={}",
					sessionService.getActiveProject().map(CProject::getId).orElse(null));
			labelStatus.setText("No action selected.");
			labelStatus.getStyle().set("color", "var(--lumo-secondary-text-color)");
			clearDetailsRouter();
			return;
		}
		final CBabPolicyActionMaskBase<?> actionMask = action.getActionMask();
		if (actionMask == null) {
			LOGGER.warn("ActionMaskDetails refresh skipped: actionMask is null for action={}", describeAction(action));
			labelStatus.setText("Select an action mask to view its details.");
			labelStatus.getStyle().set("color", "var(--lumo-secondary-text-color)");
			clearDetailsRouter();
			return;
		}
		try {
			LOGGER.debug("ActionMaskDetails refresh start action={} mask={} ownerAction={} destinationNode={}",
					describeAction(action), describeMask(actionMask), describeAction(actionMask.getPolicyAction()),
					actionMask.getDestinationNode() != null
							? actionMask.getDestinationNode().getClass().getSimpleName() + "#" + actionMask.getDestinationNode().getId()
							: null);
			Check.notNull(actionMask.getPolicyAction(), "Action mask has no owner policy action");
			Check.notNull(actionMask.getMaskKind(), "Action mask kind is null");
			Check.notNull(actionMask.getId(), "Action mask must be saved before loading dynamic details page");
			final String viewName = resolveMaskViewName(actionMask);
			Check.notBlank(viewName, "Resolved action mask view name is blank");
			final var activeProject = sessionService.getActiveProject().orElse(null);
			final CPageEntity page = pageEntityService.findByNameAndProject(viewName, activeProject)
					.orElseThrow(() -> new IllegalStateException("Action mask page is not initialized for view: " + viewName));
			LOGGER.info(
					"ActionMaskDetails loading action={} mask={} viewName='{}' pageId={} pageTitle='{}' projectId={} detailsRouterClass={}",
					describeAction(action), describeMask(actionMask), viewName, page.getId(), page.getPageTitle(),
					activeProject != null ? activeProject.getId() : null, detailsRouter.getClass().getSimpleName());
			detailsRouter.loadSpecificPage(page.getId(), actionMask.getId(), true, null);
			// name and type of the action mask are shown in the page header, so we can just show a generic status message here
			labelStatus.setText("Showing details for action mask '" + actionMask.getName() + "' of type '" + actionMask.getMaskKind() + "'.");
			labelStatus.getStyle().set("color", "var(--lumo-success-text-color)");
		} catch (final Exception e) {
			LOGGER.error("Failed to render action mask details action={} mask={} reason={}",
					describeAction(action), describeMask(actionMask), e.getMessage());
			labelStatus.setText("Unable to render action mask details.");
			labelStatus.getStyle().set("color", "var(--lumo-error-text-color)");
			clearDetailsRouter();
			throw new IllegalStateException(
					"Action mask details cannot be rendered for actionId=" + action.getId() + ", maskId=" + actionMask.getId(), e);
		}
	}

	private String resolveMaskViewName(final CBabPolicyActionMaskBase<?> actionMask) {
		return switch (actionMask.getMaskKind()) {
		case CBabPolicyActionMaskCAN.MASK_KIND -> CBabPolicyActionMaskCAN.VIEW_NAME;
		case CBabPolicyActionMaskROS.MASK_KIND -> CBabPolicyActionMaskROS.VIEW_NAME;
		case CBabPolicyActionMaskFile.MASK_KIND -> CBabPolicyActionMaskFile.VIEW_NAME;
		default -> throw new IllegalStateException("Unsupported action mask kind: " + actionMask.getMaskKind());
		};
	}

	@Override
	public void setThis(final CBabPolicyAction value) {
		setValue(value);
		// populateForm can pass the same action instance repeatedly; force re-render so status text/router stay in sync.
		refreshComponent();
	}

	private String describeAction(final CBabPolicyAction action) {
		if (action == null) {
			return "null";
		}
		return "id=%s,name=%s,class=%s,ruleId=%s,destinationNodeId=%s,maskId=%s".formatted(
				action.getId(),
				action.getName(),
				action.getClass().getSimpleName(),
				action.getPolicyRule() != null ? action.getPolicyRule().getId() : null,
				action.getDestinationNode() != null ? action.getDestinationNode().getId() : null,
				action.getActionMask() != null ? action.getActionMask().getId() : null);
	}

	private String describeMask(final CBabPolicyActionMaskBase<?> actionMask) {
		if (actionMask == null) {
			return "null";
		}
		return "id=%s,name=%s,class=%s,kind=%s,ownerActionId=%s".formatted(
				actionMask.getId(),
				actionMask.getName(),
				actionMask.getClass().getSimpleName(),
				actionMask.getMaskKind(),
				actionMask.getPolicyAction() != null ? actionMask.getPolicyAction().getId() : null);
	}
}
