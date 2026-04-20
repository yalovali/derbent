package tech.derbent.api.grid.view;

import java.lang.reflect.Field;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.page.domain.CPageEntity;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.page.view.CDynamicPageRouter;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;

/** Compact ID renderer that follows the CLabelEntity pattern: icon + ID value, aligned and click-friendly. */
public class CComponentId extends CLabelEntity {

	private static final String ICON_SIZE = "16px";
	private static final String ID_PLACEHOLDER = "-";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentId.class);
	private static final long serialVersionUID = 1L;

	private static boolean canNavigateToEntity(final CEntityDB<?> entity) {
		if (entity == null || entity.getId() == null) {
			return false;
		}
		try {
			final Field viewNameField = entity.getClass().getField("VIEW_NAME");
			final String entityViewName = (String) viewNameField.get(null);
			Check.notBlank(entityViewName, "Entity view name cannot be blank");
			final CPageEntityService pageEntityService = CSpringContext.getBean(CPageEntityService.class);
			final ISessionService sessionService = CSpringContext.getBean(ISessionService.class);
			final Optional<CPageEntity> page = pageEntityService.findByNameAndProject(entityViewName, sessionService.getActiveProject().orElse(null));
			return page.isPresent();
		} catch (final Exception e) {
			LOGGER.debug("Navigation not available for {} reason={}", entity.getClass().getSimpleName(), e.getMessage());
			return false;
		}
	}

	private static Span createIdSpan(final Object idValue) {
		final Span idSpan = new Span(idValue != null ? String.valueOf(idValue) : ID_PLACEHOLDER);
		idSpan.getStyle().set("font-variant-numeric", "tabular-nums");
		return idSpan;
	}

	private static String getEntityTypeLabel(final CEntityDB<?> entity) {
		if (entity == null) {
			return "Item";
		}
		final String simpleName = entity.getClass().getSimpleName();
		return simpleName.startsWith("C") && simpleName.length() > 1 ? simpleName.substring(1) : simpleName;
	}

	public CComponentId(final CEntityDB<?> entity) throws Exception {
		this(entity, entity != null ? entity.getId() : null);
	}

	public CComponentId(final CEntityDB<?> entity, final Object idValue) throws Exception {
		/* getStyle().set("gap", GAP); */
		getStyle().set("white-space", "nowrap").set("display", "inline-flex").set("align-items", "center").set("gap", "6px");
		/* getStyle().set("padding", "2px 6px"); */
		// addIcon(entity);
		final Span idSpan = createIdSpan(idValue);
		add(idSpan);
		configureNavigation(entity, idSpan);
	}

	@SuppressWarnings ("unused")
	private void addIcon(final CEntityDB<?> entity) throws Exception {
		if (entity == null) {
			return;
		}
		try {
			if (entity instanceof CUser) {
				final Avatar avatar = createUserAvatar((CUser) entity, ICON_SIZE);
				add(avatar);
				return;
			}
			final Icon icon = CColorUtils.getIconForEntity(entity);
			if (icon != null) {
				icon.getStyle().set("width", ICON_SIZE).set("height", ICON_SIZE).set("flex-shrink", "0");
				add(icon);
			}
		} catch (final Exception e) {
			LOGGER.error("Could not resolve icon for {}: {}", entity.getClass().getSimpleName(), e.getMessage());
			throw e;
		}
	}

	private void configureNavigation(final CEntityDB<?> entity, final Span idSpan) {
		if (!canNavigateToEntity(entity)) {
			return;
		}
		getStyle().set("cursor", "pointer");
		idSpan.getStyle().set("text-decoration", "underline").set("text-underline-offset", "2px")
				.set("text-decoration-color", "var(--lumo-primary-color-50pct)").set("color", "var(--lumo-primary-text-color)");
		getElement().setProperty("title", "Open %s #%s".formatted(getEntityTypeLabel(entity), entity.getId()));
		addClickListener(event -> {
			try {
				CDynamicPageRouter.navigateToEntity(entity);
			} catch (final Exception e) {
				LOGGER.error("Failed to navigate to {} #{} reason={}", getEntityTypeLabel(entity), entity.getId(), e.getMessage());
				CNotificationService.showException("Failed to open item", e);
			}
		});
	}
}
