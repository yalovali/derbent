package tech.derbent.api.grid.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.api.utils.CColorUtils;

/** Compact ID renderer that follows the CLabelEntity pattern: icon + ID value, aligned and click-friendly. */
public class CComponentId extends CLabelEntity {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentId.class);
	private static final String GAP = "6px";
	private static final String ICON_SIZE = "16px";
	private static final String ID_PLACEHOLDER = "-";

	public CComponentId(final CEntityDB<?> entity) {
		this(entity, entity != null ? entity.getId() : null);
	}

	public CComponentId(final CEntityDB<?> entity, final Object idValue) {
		super();
		getStyle().set("gap", GAP);
		getStyle().set("white-space", "nowrap");
		getStyle().set("padding", "2px 6px");

		addIcon(entity);
		add(createIdSpan(idValue));
	}

	private void addIcon(final CEntityDB<?> entity) {
		if (entity == null) {
			return;
		}
		try {
			final Icon icon = CColorUtils.getIconForEntity(entity);
			if (icon != null) {
				icon.getStyle().set("width", ICON_SIZE).set("height", ICON_SIZE).set("flex-shrink", "0");
				add(icon);
			}
		} catch (final Exception e) {
			LOGGER.debug("Could not resolve icon for {}: {}", entity.getClass().getSimpleName(), e.getMessage());
		}
	}

	private Span createIdSpan(final Object idValue) {
		final Span idSpan = new Span(idValue != null ? String.valueOf(idValue) : ID_PLACEHOLDER);
		idSpan.getStyle().set("font-variant-numeric", "tabular-nums");
		return idSpan;
	}
}
