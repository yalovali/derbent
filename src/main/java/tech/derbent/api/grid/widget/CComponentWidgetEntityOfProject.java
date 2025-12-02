package tech.derbent.api.grid.widget;

import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.api.utils.CAuxillaries;

/** CComponentWidgetEntityOfProject - Base widget component for displaying project item entities in grids.
 * <p>
 * This widget provides a three-row layout with common content:
 * <ul>
 * <li><b>Row 1:</b> Entity name with icon and color (if entity implements IHasIcon)</li>
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
	protected static final int MAX_DESCRIPTION_LENGTH = 100;
	private static final long serialVersionUID = 1L;

	public CComponentWidgetEntityOfProject(final EntityClass item) {
		super(item);
	}

	@Override
	protected void createFirstLine() {
		layoutLineOne.add(CLabelEntity.createH3Label(getEntity()));
	}

	@Override
	protected void createSecondLine() {
		final CLabelEntity label = new CLabelEntity();
		label.setValue(getEntity(), CAuxillaries.safeTrim(getEntity().getDescription(), MAX_DESCRIPTION_LENGTH), true);
		layoutLineTwo.add(label);
	}

	@Override
	protected void createThirdLine() {
		final EntityClass item = getEntity();
		layoutLineThree.add(new CLabelEntity(getEntity().getStatus()));
		layoutLineThree.add(new CLabelEntity(getEntity().getResponsible()));
		final LocalDate startDate = item.getStartDate();
		final LocalDate endDate = item.getEndDate();
		if (startDate != null || endDate != null) {
			layoutLineThree.add(CLabelEntity.createDateRangeLabel(startDate, endDate));
		}
	}
}
