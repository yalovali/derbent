package tech.derbent.api.interfaces;

import java.time.LocalDate;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.app.sprints.domain.CSprintItem;
import tech.derbent.base.users.domain.CUser;

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

	String getDescription();
	String getDescriptionShort();
	LocalDate getEndDate();
	Long getId();
	String getName();
	CUser getResponsible();
	CSprintItem getSprintItem();
	Integer getSprintOrder();
	LocalDate getStartDate();
	CProjectItemStatus getStatus();
	Long getStoryPoint();
	void setSprintItem(CSprintItem cSprintItem);
	void setSprintOrder(Integer sprintOrder);
	void setStatus(CProjectItemStatus newStatus);
	void setStoryPoint(Long storyPoint);
}
