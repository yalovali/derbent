package tech.derbent.plm.gnnt.gnntviewentity.view.components;

import java.time.LocalDate;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.plm.gnnt.gnntitem.domain.CGnntItem;

public class CGnntTimelineRow extends CHorizontalLayout {

	private static final long serialVersionUID = 1L;

	public CGnntTimelineRow(final CGnntItem item, final LocalDate timelineStart, final LocalDate timelineEnd, final int totalWidth) {
		addClassName("gantt-timeline-bar-container");
		setWidth(totalWidth + "px");
		setHeight("36px");
		setPadding(false);
		setSpacing(false);
		if (item.hasDates()) {
			add(new CGnntTimelineBar(item, timelineStart, timelineEnd, totalWidth));
			return;
		}
		final CDiv noDates = new CDiv("No dates");
		noDates.getStyle().set("color", "var(--lumo-secondary-text-color)");
		noDates.getStyle().set("font-size", "12px");
		noDates.getStyle().set("padding-left", "8px");
		add(noDates);
	}
}
