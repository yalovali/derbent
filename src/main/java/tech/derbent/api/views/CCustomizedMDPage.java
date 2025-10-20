package tech.derbent.api.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.base.session.service.ISessionService;

/** Example implementation showing how to customize the details view tab while maintaining consistent button placement and styling. */
public abstract class CCustomizedMDPage<EntityClass extends CEntityDB<EntityClass>> extends CAbstractEntityDBPage<EntityClass> {

	private static final long serialVersionUID = 1L;

	protected CCustomizedMDPage(final Class<EntityClass> entityClass, final CAbstractService<EntityClass> entityService,
			final ISessionService sessionService) {
		super(entityClass, entityService, sessionService);
	}

	/** Subclasses should override this to provide their custom label. */
	@Override
	protected abstract Div createDetailsTabLeftContent();

	/** Example of how to override the details tab with more complex content while keeping the standard button layout. */
	@Override
	protected void createDetailsViewTab() {
		// Clear any existing content
		getDetailsTabLayout().removeAll();
		// Create a custom tab layout with additional elements
		final HorizontalLayout tabContent = new HorizontalLayout();
		tabContent.setWidthFull();
		tabContent.setJustifyContentMode(HorizontalLayout.JustifyContentMode.BETWEEN);
		tabContent.setPadding(true);
		tabContent.setSpacing(true);
		tabContent.setClassName("details-tab-content custom-tab-content");
		// Left side: Custom content with icon and status
		final HorizontalLayout leftContent = new HorizontalLayout();
		leftContent.setSpacing(true);
		leftContent.setAlignItems(HorizontalLayout.Alignment.CENTER);
		// Add icon
		final Span icon = new Span(VaadinIcon.EDIT.create());
		icon.getStyle().set("color", "var(--lumo-primary-color)");
		// Add custom text
		final Div customLabel = createDetailsTabLeftContent();
		// Add status indicator (example)
		final Span status = new Span("●");
		status.getStyle().set("color", "var(--lumo-success-color)");
		status.setTitle("Active");
		leftContent.add(icon, customLabel, status);
		// Right side: Standard action buttons (this ensures consistency)
		final HorizontalLayout buttonLayout = createDetailsTabButtonLayout();
		tabContent.add(leftContent, buttonLayout);
		getDetailsTabLayout().add(tabContent);
	}
}
