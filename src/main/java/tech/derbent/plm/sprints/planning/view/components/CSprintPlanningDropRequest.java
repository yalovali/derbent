package tech.derbent.plm.sprints.planning.view.components;

import com.vaadin.flow.component.grid.dnd.GridDropLocation;

import tech.derbent.plm.gnnt.gnntitem.domain.CGnntItem;

public record CSprintPlanningDropRequest(CGnntItem draggedItem, CGnntItem targetItem, GridDropLocation dropLocation, String sourceGridId) {
}
