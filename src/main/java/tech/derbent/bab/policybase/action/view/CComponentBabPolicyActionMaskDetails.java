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

	@Override
	public String getComponentName() { return COMPONENT_NAME; }

	@Override
	protected void onValueChanged(final CBabPolicyAction oldValue, final CBabPolicyAction newValue, final boolean fromClient) {
		super.onValueChanged(oldValue, newValue, fromClient);
		refreshComponent();
	}

	@Override
	protected void refreshComponent() {
		final CBabPolicyAction action = getValue();
		if (action == null) {
			labelStatus.setText("No action selected.");
			labelStatus.getStyle().set("color", "var(--lumo-secondary-text-color)");
			clearDetailsRouter();
			return;
		}
		final CBabPolicyActionMaskBase<?> actionMask = action.getActionMask();
		if (actionMask == null) {
			labelStatus.setText("Select an action mask to view its details.");
			labelStatus.getStyle().set("color", "var(--lumo-secondary-text-color)");
			clearDetailsRouter();
			return;
		}
		try {
			final String viewName = resolveMaskViewName(actionMask);
			final CPageEntity page = pageEntityService.findByNameAndProject(viewName, sessionService.getActiveProject().orElse(null))
					.orElseThrow(() -> new IllegalStateException("Action mask page is not initialized for view: " + viewName));
			detailsRouter.loadSpecificPage(page.getId(), actionMask.getId(), true, null);
			labelStatus.setText("Showing details for action mask: " + actionMask.getName());
			labelStatus.getStyle().set("color", "var(--lumo-success-text-color)");
		} catch (final Exception e) {
			LOGGER.error("Failed to render action mask details for actionId={} maskId={}. reason={}",
					action.getId(), actionMask.getId(), e.getMessage());
			labelStatus.setText("Unable to render action mask details.");
			labelStatus.getStyle().set("color", "var(--lumo-error-text-color)");
			clearDetailsRouter();
		}
	}

	private void clearDetailsRouter() {
		try {
			detailsRouter.loadSpecificPage(null, null, true, null);
		} catch (final Exception clearException) {
			LOGGER.debug("Failed to clear action mask details router. reason={}", clearException.getMessage());
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
	}
}
