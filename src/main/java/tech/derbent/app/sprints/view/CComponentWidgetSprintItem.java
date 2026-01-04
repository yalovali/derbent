package tech.derbent.app.sprints.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.api.grid.widget.CComponentWidgetEntity;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.ui.component.CEvent;
import tech.derbent.app.sprints.domain.CSprintItem;
import tech.derbent.app.sprints.domain.CSprint;

/** CComponentWidgetSprintItem - Widget component for displaying SprintItem entities in grids. 
 * Shows progress tracking information (story points, progress %, dates, responsible user).
 */
public class CComponentWidgetSprintItem extends CComponentWidgetEntity<CSprintItem> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentWidgetSprintItem.class);

	public CComponentWidgetSprintItem(final CSprintItem sprintItem) {
		super(sprintItem);
	}

	/** Creates the first line with parent item name. */
	@Override
	protected void createFirstLine() {
		final ISprintableItem parent = getEntity().getParentItem();
		if (parent != null) {
			final CLabelEntity itemLabel = new CLabelEntity();
			itemLabel.setText(parent.getName());
			itemLabel.getStyle().set("font-weight", "600").set("font-size", "12pt");
			layoutLineOne.add(itemLabel);
		} else {
			final CLabelEntity itemLabel = new CLabelEntity();
			itemLabel.setText("Sprint Item #" + getEntity().getId());
			itemLabel.getStyle().set("font-weight", "600").set("font-size", "12pt");
			layoutLineOne.add(itemLabel);
		}
	}

	/** Creates the second line with sprint and progress info. */
	@Override
	protected void createSecondLine() {
		// Show sprint info
		final CSprint sprint = getEntity().getSprint();
		final CLabelEntity sprintLabel = new CLabelEntity();
		if (sprint != null) {
			sprintLabel.setText("Sprint: " + sprint.getName());
		} else {
			sprintLabel.setText("Backlog");
		}
		sprintLabel.getStyle().set("font-size", "10pt").set("color", "#666");
		layoutLineTwo.add(sprintLabel);
		
		// Show progress
		final CLabelEntity progressLabel = new CLabelEntity();
		progressLabel.setText("Progress: " + getEntity().getProgressPercentage() + "%");
		progressLabel.getStyle().set("font-size", "10pt").set("color", "#666").set("margin-left", "12px");
		layoutLineTwo.add(progressLabel);
	}

	/** Creates the third line with story points, dates, and responsible user. */
	@Override
	protected void createThirdLine() throws Exception {
		// Show story points
		if (getEntity().getStoryPoint() != null && getEntity().getStoryPoint() > 0) {
			final CLabelEntity storyPointLabel = new CLabelEntity();
			storyPointLabel.setText("SP: " + getEntity().getStoryPoint());
			storyPointLabel.getStyle().set("font-size", "10pt").set("color", "#666");
			layoutLineThree.add(storyPointLabel);
		}
		
		// Show dates
		if (getEntity().getStartDate() != null) {
			final CLabelEntity dateLabel = new CLabelEntity();
			dateLabel.setText("Start: " + getEntity().getStartDate());
			dateLabel.getStyle().set("font-size", "10pt").set("color", "#666").set("margin-left", "8px");
			layoutLineThree.add(dateLabel);
		}
		
		// Show responsible user
		if (getEntity().getResponsible() != null) {
			final CLabelEntity userLabel = CLabelEntity.createUserLabel(getEntity().getResponsible());
			userLabel.getStyle().set("margin-left", "8px");
			layoutLineThree.add(userLabel);
		}
	}

	@Override
	public void drag_checkEventBeforePass(CEvent event) {
		LOGGER.debug("Drag event check before pass: {} comp id:{} event type:{}", event, getId(), event.getClass().getSimpleName());
	}
}
