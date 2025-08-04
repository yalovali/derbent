package tech.derbent.kanban.view;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;

import tech.derbent.base.ui.CBaseKanbanColumn;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.domain.CMeetingStatus;
import tech.derbent.meetings.view.CMeetingCard;

/**
 * CMeetingKanbanColumn - Kanban column for meetings using the base kanban classes. Layer: View (MVC)
 * 
 * This implementation uses the generic kanban base classes to provide drag-and-drop functionality for meeting cards.
 */
public class CMeetingKanbanColumn extends CBaseKanbanColumn<CMeeting, CMeetingStatus> {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(CMeetingKanbanColumn.class);

    /**
     * Constructor for CMeetingKanbanColumn.
     * 
     * @param status
     *            the meeting status this column represents
     * @param meetings
     *            the list of meetings for this status
     */
    public CMeetingKanbanColumn(final CMeetingStatus status, final List<CMeeting> meetings) {
        super(status, meetings);
        LOGGER.debug("Created CMeetingKanbanColumn for status: {} with {} meetings", status.getName(), meetings.size());
    }

    @Override
    protected Component createEntityCard(final CMeeting entity) {
        try {
            return new CMeetingCard(entity);
        } catch (final Exception e) {
            LOGGER.error("Error creating meeting card for: {}", entity.getName(), e);
            // Return a simple fallback component
            return new com.vaadin.flow.component.html.Div("Error loading meeting: " + entity.getName());
        }
    }
}