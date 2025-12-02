package tech.derbent.api.grid.widget;

import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.CColorUtils;

/** CComponentWidgetEntityOfProject - Base widget component for displaying project item entities in grids.
 * <p>
 * This widget provides a three-row layout with common content:
 * <ul>
 * <li><b>Row 1:</b> Entity name with icon and color (if entity implements IHasColorAndIcon)</li>
 * <li><b>Row 2:</b> Description (truncated with ellipsis)</li>
 * <li><b>Row 3:</b> Status badge, responsible user, and date range</li>
 * </ul>
 * </p>
 * <p>
 * Subclasses can override createFirstLine(), createSecondLine(), createThirdLine() to customize content.
 * </p>
 * @author Derbent Framework
 * @since 1.0
 * @param <EntityClass> the entity type extending CProjectItem */
public abstract class CComponentWidgetEntityOfProject<EntityClass extends CProjectItem<?>> extends CComponentWidgetEntity<EntityClass> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentWidgetEntityOfProject.class);
	/** Max description length to display in the widget. */
	protected static final int MAX_DESCRIPTION_LENGTH = 100;
	private static final long serialVersionUID = 1L;

	/** Creates a new project item widget for the specified entity.
	 * @param item the project item to display in the widget */
	public CComponentWidgetEntityOfProject(final EntityClass item) {
		super(item);
	}

	/** Creates a styled date range display component.
	 * @param startDate the start date (can be null)
	 * @param endDate   the end date (can be null)
	 * @return the date range display component */
	protected CHorizontalLayout createDateRangeDisplay(final LocalDate startDate, final LocalDate endDate) {
		final CHorizontalLayout dateLayout = new CHorizontalLayout();
		dateLayout.setSpacing(true);
		dateLayout.setAlignItems(FlexComponent.Alignment.CENTER);
		dateLayout.getStyle().set("font-size", "10pt");
		dateLayout.getStyle().set("color", "#666");
		try {
			// Add calendar icon
			final Icon icon = CColorUtils.createStyledIcon("vaadin:calendar");
			if (icon != null) {
				icon.getStyle().set("width", "14px");
				icon.getStyle().set("height", "14px");
				icon.getStyle().set("color", "#666");
				dateLayout.add(icon);
			}
		} catch (final Exception e) {
			LOGGER.debug("Could not create calendar icon: {}", e.getMessage());
		}
		// Format date range
		final StringBuilder dateRange = new StringBuilder();
		if (startDate != null) {
			dateRange.append(startDate.format(DATE_FORMATTER));
		}
		if (startDate != null && endDate != null) {
			dateRange.append(" - ");
		}
		if (endDate != null) {
			dateRange.append(endDate.format(DATE_FORMATTER));
		}
		final Span dateSpan = new Span(dateRange.toString());
		dateLayout.add(dateSpan);
		return dateLayout;
	}

	/** Creates the first line with entity name, icon, and color styling. If entity implements IHasColorAndIcon, the icon and color will be used. */
	@Override
	protected void createFirstLine() {
		layoutLineOne.add(CLabelEntity.createH3Label(getEntity()));
	}

	/** Creates the second line with truncated description. */
	@Override
	protected void createSecondLine() {
		final CLabelEntity label = new CLabelEntity();
		label.setValue(getEntity(), CAuxillaries.safeTrim(getEntity().getDescription(), MAX_DESCRIPTION_LENGTH), true);
		layoutLineTwo.add(label);
	}

	/** Creates the third line with status badge, responsible user, and date range. */
	@Override
	protected void createThirdLine() {
		final EntityClass item = getEntity();
		layoutLineThree.add(new CLabelEntity(getEntity().getStatus()));
		layoutLineThree.add(new CLabelEntity(getEntity().getResponsible()));
		final LocalDate startDate = item.getStartDate();
		final LocalDate endDate = item.getEndDate();
		if (startDate != null || endDate != null) {
			layoutLineThree.add(createDateRangeDisplay(startDate, endDate));
		}
	}
}
