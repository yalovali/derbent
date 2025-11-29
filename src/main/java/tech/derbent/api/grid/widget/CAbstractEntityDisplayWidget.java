package tech.derbent.api.grid.widget;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.utils.Check;

/**
 * Abstract base class for entity display widgets.
 * Provides common layout structure and styling for entity widgets.
 *
 * @param <EntityClass> the entity type
 */
public abstract class CAbstractEntityDisplayWidget<EntityClass extends CEntityDB<EntityClass>> implements IEntityDisplayWidget<EntityClass> {

	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

	protected final Class<EntityClass> entityClass;

	protected CAbstractEntityDisplayWidget(final Class<EntityClass> entityClass) {
		Check.notNull(entityClass, "Entity class cannot be null");
		this.entityClass = entityClass;
	}

	@Override
	public Component createWidget(final EntityClass entity) {
		if (entity == null) {
			return createEmptyWidget();
		}
		return createEntityWidget(entity);
	}

	/**
	 * Creates the widget component for a non-null entity.
	 * Subclasses must implement this method.
	 *
	 * @param entity the entity to display (never null)
	 * @return the widget component
	 */
	protected abstract Component createEntityWidget(EntityClass entity);

	/**
	 * Creates a widget to display when the entity is null.
	 *
	 * @return the empty widget component
	 */
	protected Component createEmptyWidget() {
		final Div emptyDiv = new Div();
		emptyDiv.addClassName("entity-widget-empty");
		emptyDiv.setText("No data");
		emptyDiv.getStyle().set("color", "#666");
		emptyDiv.getStyle().set("font-style", "italic");
		emptyDiv.getStyle().set("padding", "8px");
		return emptyDiv;
	}

	@Override
	public Class<EntityClass> getEntityClass() {
		return entityClass;
	}

	// ===== Helper methods for creating widget components =====

	/**
	 * Creates a styled header row with name and optional description.
	 *
	 * @param name        the entity name
	 * @param description the optional description
	 * @return the header component
	 */
	protected Component createHeaderRow(final String name, final String description) {
		final CVerticalLayout header = new CVerticalLayout(false, false, false);
		header.addClassName("widget-header");

		final Span nameSpan = new Span(name != null ? name : "Unnamed");
		nameSpan.addClassName("widget-name");
		nameSpan.getStyle().set("font-weight", "600");
		nameSpan.getStyle().set("font-size", "14px");
		nameSpan.getStyle().set("color", "#1a1a1a");
		header.add(nameSpan);

		if (description != null && !description.isBlank()) {
			final Span descSpan = new Span(description);
			descSpan.addClassName("widget-description");
			descSpan.getStyle().set("font-size", "12px");
			descSpan.getStyle().set("color", "#666");
			descSpan.getStyle().set("max-width", "300px");
			descSpan.getStyle().set("overflow", "hidden");
			descSpan.getStyle().set("text-overflow", "ellipsis");
			descSpan.getStyle().set("white-space", "nowrap");
			header.add(descSpan);
		}

		return header;
	}

	/**
	 * Creates a detail row with icon and text.
	 *
	 * @param icon the Vaadin icon
	 * @param text the text to display
	 * @return the detail row component
	 */
	protected Component createDetailRow(final VaadinIcon icon, final String text) {
		final CHorizontalLayout row = new CHorizontalLayout(false, false, false);
		row.addClassName("widget-detail-row");
		row.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
		row.getStyle().set("gap", "4px");

		final Icon iconComponent = icon.create();
		iconComponent.setSize("14px");
		iconComponent.getStyle().set("color", "#888");
		row.add(iconComponent);

		final Span textSpan = new Span(text != null ? text : "");
		textSpan.getStyle().set("font-size", "12px");
		textSpan.getStyle().set("color", "#555");
		row.add(textSpan);

		return row;
	}

	/**
	 * Creates a status badge with color.
	 *
	 * @param status the status text
	 * @param color  the background color (hex format)
	 * @return the badge component
	 */
	protected Component createStatusBadge(final String status, final String color) {
		final Span badge = new Span(status != null ? status : "Unknown");
		badge.addClassName("widget-status-badge");
		badge.getStyle().set("background-color", color != null ? color : "#e0e0e0");
		badge.getStyle().set("color", getContrastColor(color));
		badge.getStyle().set("padding", "2px 8px");
		badge.getStyle().set("border-radius", "12px");
		badge.getStyle().set("font-size", "11px");
		badge.getStyle().set("font-weight", "500");
		return badge;
	}

	/**
	 * Creates a progress indicator.
	 *
	 * @param percentage the progress percentage (0-100)
	 * @return the progress component
	 */
	protected Component createProgressIndicator(final int percentage) {
		final Div container = new Div();
		container.addClassName("widget-progress");
		container.getStyle().set("display", "flex");
		container.getStyle().set("align-items", "center");
		container.getStyle().set("gap", "6px");

		final Div progressBar = new Div();
		progressBar.getStyle().set("width", "60px");
		progressBar.getStyle().set("height", "6px");
		progressBar.getStyle().set("background-color", "#e0e0e0");
		progressBar.getStyle().set("border-radius", "3px");
		progressBar.getStyle().set("overflow", "hidden");

		final Div progressFill = new Div();
		progressFill.getStyle().set("width", Math.max(0, Math.min(100, percentage)) + "%");
		progressFill.getStyle().set("height", "100%");
		progressFill.getStyle().set("background-color", getProgressColor(percentage));
		progressBar.add(progressFill);

		final Span percentText = new Span(percentage + "%");
		percentText.getStyle().set("font-size", "11px");
		percentText.getStyle().set("color", "#666");

		container.add(progressBar, percentText);
		return container;
	}

	/**
	 * Gets the appropriate progress bar color based on percentage.
	 *
	 * @param percentage the progress percentage
	 * @return the color hex code
	 */
	private String getProgressColor(final int percentage) {
		if (percentage >= 100) {
			return "#4caf50"; // Green for complete
		} else if (percentage >= 75) {
			return "#8bc34a"; // Light green
		} else if (percentage >= 50) {
			return "#ff9800"; // Orange
		} else if (percentage >= 25) {
			return "#ff5722"; // Deep orange
		}
		return "#f44336"; // Red for low progress
	}

	/**
	 * Gets a contrasting text color for a given background color.
	 *
	 * @param backgroundColor the background color in hex format
	 * @return white or black depending on brightness
	 */
	private String getContrastColor(final String backgroundColor) {
		if (backgroundColor == null || backgroundColor.isBlank()) {
			return "#333";
		}
		try {
			final String hex = backgroundColor.startsWith("#") ? backgroundColor.substring(1) : backgroundColor;
			if (hex.length() < 6) {
				return "#333";
			}
			final int r = Integer.parseInt(hex.substring(0, 2), 16);
			final int g = Integer.parseInt(hex.substring(2, 4), 16);
			final int b = Integer.parseInt(hex.substring(4, 6), 16);
			final double brightness = (r * 0.299 + g * 0.587 + b * 0.114);
			return brightness > 127 ? "#333" : "#fff";
		} catch (final Exception e) {
			return "#333";
		}
	}
}
