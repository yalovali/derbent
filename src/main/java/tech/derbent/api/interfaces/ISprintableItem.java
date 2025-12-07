package tech.derbent.api.interfaces;

import tech.derbent.api.entityOfProject.domain.CProjectItem;

/** ISprintableItem - Marker interface for entities that can be included in sprints.
 * <p>
 * This interface is implemented by domain entities (e.g., CActivity, CMeeting) that can be added to sprint items. It provides a contract for entities
 * that can be part of sprint planning and tracking.
 * </p>
 * <p>
 * Entities implementing this interface can be:
 * <ul>
 * <li>Added to CSprintItem entities</li>
 * <li>Displayed in sprint item widgets</li>
 * <li>Tracked and managed within sprint contexts</li>
 * <li>Have their services provide sprint display widgets</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Implementation Requirements:</strong>
 * <ul>
 * <li>Entity must extend CProjectItem (provides getId(), getName(), getDescription())</li>
 * <li>Entity's service should implement IEntityRegistrable</li>
 * <li>Entity's page service should implement ISprintItemPageService for sprint widget support</li>
 * </ul>
 * </p>
 * @author Derbent Framework
 * @since 1.0
 * @see CProjectItem
 * @see ISprintItemPageService */
public interface ISprintableItem {

	/** Gets the unique identifier for this sprintable item.
	 * @return the item ID */
	Long getId();

	/** Gets the display name for this sprintable item.
	 * @return the item name */
	String getName();

	/** Gets the description for this sprintable item.
	 * @return the item description, can be null */
	String getDescription();

	/** Gets the sprint order for this item. Sprint-aware components use this field to determine display order within sprints and backlogs.
	 * @return the sprint order, or null if not set */
	Integer getSprintOrder();

	/** Sets the sprint order for this item. This field is used by sprint-aware components for drag-and-drop ordering.
	 * @param sprintOrder the new sprint order */
	void setSprintOrder(Integer sprintOrder);
}
