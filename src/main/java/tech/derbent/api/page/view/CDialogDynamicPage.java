package tech.derbent.api.page.view;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.page.domain.CPageEntity;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.component.enhanced.CCrudToolbar;
import tech.derbent.api.ui.dialogs.CDialog;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;

/** Dialog wrapper for rendering a single dynamic page view inside a modal.
 * <p>
 * Route parsing follows existing navigation patterns:
 * </p>
 * <ul>
 * <li>{@code cdynamicpagerouter/page:{pageId}}</li>
 * <li>{@code cdynamicpagerouter/page:{pageId}&item:{entityId}}</li>
 * <li>{@code page:{pageId}&item:{entityId}}</li>
 * </ul>
 * <p>
 * Internally uses {@link CDynamicSingleEntityPageView} to preserve binder/CRUD behavior and fail-fast validation semantics.
 * </p> */
public class CDialogDynamicPage extends CDialog {

	private static final class CParsedDynamicRoute {

		private final Long detailEntityId;
		private final Long pageEntityId;

		CParsedDynamicRoute(final Long pageEntityId, final Long detailEntityId) {
			this.pageEntityId = pageEntityId;
			this.detailEntityId = detailEntityId;
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(CDialogDynamicPage.class);
	private static final Pattern ROUTE_PATTERN = Pattern.compile("page:(\\d+)(?:&item:(\\d+))?");
	private static final long serialVersionUID = 1L;

	/** Build a route with the same pattern used by CNavigableComboBox/CPageEntity route navigation.
	 * @param pageEntityId page entity id (required)
	 * @param detailEntityId detail entity id (optional)
	 * @return route string usable by both router and this dialog */
	public static String buildDynamicRoute(final Long pageEntityId, final Long detailEntityId) {
		Check.notNull(pageEntityId, "Page entity id cannot be null while building dynamic route");
		return detailEntityId == null ? "cdynamicpagerouter/page:" + pageEntityId : "cdynamicpagerouter/page:" + pageEntityId + "&item:"
				+ detailEntityId;
	}

	/** Resolve route using same logic as navigation button and dynamic router:
	 * <p>
	 * {@code VIEW_NAME -> CPageEntity -> cdynamicpagerouter/page:{id}&item:{entityId}}
	 * </p>
	 * @param entity selected entity (required)
	 * @return normalized route for this entity
	 * @throws Exception on missing VIEW_NAME, active project, or page mapping */
	public static String buildDynamicRouteForEntity(final CEntityDB<?> entity) throws Exception {
		Check.notNull(entity, "Entity cannot be null while building dynamic route for entity");
		Check.notNull(entity.getId(), "Entity id cannot be null while building dynamic route for entity");
		final CPageEntityService pageEntityService = CSpringContext.getBean(CPageEntityService.class);
		final ISessionService sessionService = CSpringContext.getBean(ISessionService.class);
		final String viewName = resolveViewName(entity.getClass());
		final CPageEntity pageEntity = pageEntityService
				.findByNameAndProject(viewName, sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException(
						"No active project found while building dynamic route for entity " + entity.getClass().getSimpleName())))
				.orElseThrow(() -> new IllegalStateException("No CPageEntity found for VIEW_NAME '" + viewName + "'"));
		return buildDynamicRoute(pageEntity.getId(), entity.getId());
	}

	/** Factory for opening dialog directly from navigation-style route.
	 * @param route dynamic route string (required)
	 * @return dialog instance ready to open
	 * @throws Exception if route parsing/loading fails */
	public static CDialogDynamicPage fromRoute(final String route) throws Exception {
		final CParsedDynamicRoute parsedRoute = parseDynamicRoute(route);
		return new CDialogDynamicPage(parsedRoute.pageEntityId, parsedRoute.detailEntityId);
	}

	private static CParsedDynamicRoute parseDynamicRoute(final String route) {
		Check.notBlank(route, "Route cannot be blank while parsing dynamic route");
		final Matcher matcher = ROUTE_PATTERN.matcher(route.trim());
		Check.isTrue(matcher.find(), "Invalid dynamic route format. Expected 'page:{id}[&item:{id}]', got: " + route);
		final Long pageEntityId = Long.valueOf(matcher.group(1));
		final String detailGroup = matcher.group(2);
		final Long detailEntityId = detailGroup == null ? null : Long.valueOf(detailGroup);
		return new CParsedDynamicRoute(pageEntityId, detailEntityId);
	}

	private static String resolveViewName(final Class<?> entityClass) throws Exception {
		Class<?> current = entityClass;
		while (current != null && current != Object.class) {
			try {
				final Field field = current.getField("VIEW_NAME");
				final Object value = field.get(null);
				Check.isTrue(value instanceof String, "VIEW_NAME must be String for class: " + current.getName());
				return (String) value;
			} catch (final NoSuchFieldException ignored) {
				current = current.getSuperclass();
			}
		}
		throw new IllegalStateException("VIEW_NAME not found for class hierarchy: " + entityClass.getName());
	}

	private CButton buttonClose;
	private CDynamicSingleEntityPageView dynamicSingleEntityPageView;
	private final Long initialDetailEntityId;
	private final Long initialPageEntityId;
	private CPageEntity loadedPageEntity;

	private final CDetailSectionService detailSectionService;
	private final CPageEntityService pageEntityService;
	private final ISessionService sessionService;
	private final CVerticalLayout pageHostLayout = new CVerticalLayout();

	/** Creates a dynamic page dialog from route string.
	 * @param route route in format page:{id}[&item:{id}]
	 * @throws Exception if route parsing or page initialization fails */
	public CDialogDynamicPage(final String route) throws Exception {
		this(parseDynamicRoute(route));
	}

	/** Creates a dynamic page dialog from explicit ids.
	 * @param pageEntityId page entity id (required)
	 * @param detailEntityId detail entity id (optional)
	 * @throws Exception if page initialization fails */
	public CDialogDynamicPage(final Long pageEntityId, final Long detailEntityId) throws Exception {
		this(new CParsedDynamicRoute(pageEntityId, detailEntityId));
	}

	private CDialogDynamicPage(final CParsedDynamicRoute parsedRoute) throws Exception {
		super();
		Check.notNull(parsedRoute, "Parsed route cannot be null");
		Check.notNull(parsedRoute.pageEntityId, "Page entity id cannot be null");
		initialPageEntityId = parsedRoute.pageEntityId;
		initialDetailEntityId = parsedRoute.detailEntityId;
		pageEntityService = CSpringContext.getBean(CPageEntityService.class);
		sessionService = CSpringContext.getBean(ISessionService.class);
		detailSectionService = CSpringContext.getBean(CDetailSectionService.class);
		setupDialog();
	}

	private void createPageView() throws Exception {
		loadedPageEntity = pageEntityService.getById(initialPageEntityId)
				.orElseThrow(() -> new IllegalStateException("No CPageEntity found for id: " + initialPageEntityId));
		dynamicSingleEntityPageView = new CDynamicSingleEntityPageView(loadedPageEntity, sessionService, detailSectionService, initialDetailEntityId);
		Check.notNull(dynamicSingleEntityPageView, "Dynamic single entity page view cannot be null");
		dynamicSingleEntityPageView.setSizeFull();
		dynamicSingleEntityPageView.on_after_construct();
		pageHostLayout.removeAll();
		pageHostLayout.add(dynamicSingleEntityPageView);
		pageHostLayout.setFlexGrow(1, dynamicSingleEntityPageView);
		final CCrudToolbar toolbar = dynamicSingleEntityPageView.getCrudToolbar();
		Check.notNull(toolbar, "CRUD toolbar must be initialized for dynamic single page dialog");
		// Dialog usage: keep only New/Save/Refresh actions.
		toolbar.configureButtonVisibility(true, true, false, true, false, false);
		toolbar.setWorkflowStatusSelectorEnabled(false);
		Check.notNull(getFormTitle(), "Dialog form title must be initialized");
		getFormTitle().setText(loadedPageEntity.getPageTitle());
	}

	@Override
	public String getDialogTitleString() { return loadedPageEntity != null ? loadedPageEntity.getPageTitle() : "Dynamic Page"; }

	/** Current page entity displayed by the dialog.
	 * @return loaded page entity
	 * @throws IllegalStateException if page has not been initialized yet */
	public CPageEntity getLoadedPageEntity() {
		Check.notNull(loadedPageEntity, "Dynamic page is not initialized yet");
		return loadedPageEntity;
	}

	@Override
	protected Icon getFormIcon() { return VaadinIcon.FILE_TREE_SUB.create(); }

	@Override
	protected String getFormTitleString() { return loadedPageEntity != null ? loadedPageEntity.getPageTitle() : "Dynamic Single Entity Page"; }

	/** Returns currently selected/edited entity from embedded page.
	 * @return current entity, may be null */
	public CEntityDB<?> getValue() {
		Check.notNull(dynamicSingleEntityPageView, "Dynamic page view is not initialized");
		return dynamicSingleEntityPageView.getValue();
	}

	/** Sets current entity in embedded dynamic page.
	 * <p>
	 * Supports null to clear/reset the current selection while preserving binder and page state.
	 * </p>
	 * @param entity entity to bind, may be null
	 * @throws Exception if entity selection/binding fails */
	public void setValue(final CEntityDB<?> entity) throws Exception {
		Check.notNull(dynamicSingleEntityPageView, "Dynamic page view is not initialized");
		dynamicSingleEntityPageView.setValue(entity);
		if (entity == null) {
			dynamicSingleEntityPageView.clearEntityDetails();
		} else {
			final Object entityViewName = entity.getClass().getField("VIEW_NAME").get(null);
			Check.notNull(entityViewName, "VIEW_NAME cannot be null for class: " + entity.getClass().getName());
			if (dynamicSingleEntityPageView.currentEntityViewName == null
					|| !entityViewName.equals(dynamicSingleEntityPageView.currentEntityViewName)) {
				dynamicSingleEntityPageView.rebuildEntityDetailsById(dynamicSingleEntityPageView.getPageEntity().getDetailSection().getId());
			}
		}
		dynamicSingleEntityPageView.populateForm();
	}

	@Override
	protected void setupButtons() {
		buttonClose = CButton.createCancelButton("Close", event -> close());
		buttonLayout.add(buttonClose);
	}

	@Override
	protected void setupContent() throws Exception {
		try {
			setWidth("95vw");
			setMaxWidth("1600px");
			setHeight("90vh");
			setMaxHeight("95vh");
			setResizable(true);
			mainLayout.setSizeFull();
			pageHostLayout.setPadding(false);
			pageHostLayout.setSpacing(false);
			pageHostLayout.setSizeFull();
			createPageView();
			mainLayout.add(pageHostLayout);
			mainLayout.setFlexGrow(1, pageHostLayout);
		} catch (final Exception e) {
			LOGGER.error("Failed to setup dynamic page dialog content for page id {} and item id {}", initialPageEntityId, initialDetailEntityId, e);
			CNotificationService.showException("Error setting up dynamic page dialog", e);
			throw e;
		}
	}
}
