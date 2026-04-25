package tech.derbent.plm.sprints.planning.view.components;

import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.html.Span;

import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CVerticalLayout;

/**
 * Left-side quick access panel for Sprint Planning (Jira-like).
 *
 * <p>Jira keeps planning controls close to the backlog/sprint lists (quick filters, summary metrics, and actions).
 * We replicate that pattern here while keeping the main toolbar focused on search + selection.
 * </p>
 */
public class CSprintPlanningQuickAccessPanel extends CVerticalLayout {

	public static final String ID_PANEL = "custom-sprint-planning-quick-panel";
	private static final long serialVersionUID = 1L;

	private final CButton buttonToggleDetails;
	private final CButton buttonRefresh;

	public CSprintPlanningQuickAccessPanel(
			final Runnable toggleDetailsHandler,
			final Runnable refreshHandler,
			final List<Component> extractedFilterControls) {

		setId(ID_PANEL);
		setPadding(true);
		setSpacing(false);
		getStyle().set("gap", "10px");
		setWidth("320px");
		setMinWidth("260px");

		buttonToggleDetails = CButton.createTertiary("Show details", VaadinIcon.EYE.create(), event -> {
			if (toggleDetailsHandler != null) {
				toggleDetailsHandler.run();
			}
		});
		buttonToggleDetails.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_SMALL);

		buttonRefresh = CButton.createTertiary("Refresh", VaadinIcon.REFRESH.create(), event -> {
			if (refreshHandler != null) {
				refreshHandler.run();
			}
		});
		buttonRefresh.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_SMALL);


		final Span title = new Span("Planning");
		title.getStyle().set("font-weight", "700");

		add(title, buttonToggleDetails, buttonRefresh);

		if (extractedFilterControls != null && !extractedFilterControls.isEmpty()) {
			final Span filterTitle = new Span("Quick filters");
			filterTitle.getStyle().set("font-weight", "700").set("margin-top", "6px");
			add(filterTitle);
			add(extractedFilterControls.toArray(Component[]::new));
		}
	}

	public void setDetailsVisible(final boolean visible) {
		buttonToggleDetails.setText(visible ? "Hide details" : "Show details");
		buttonToggleDetails.setIcon(visible ? VaadinIcon.EYE_SLASH.create() : VaadinIcon.EYE.create());
	}
}
