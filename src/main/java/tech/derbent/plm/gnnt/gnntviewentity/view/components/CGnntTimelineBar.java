package tech.derbent.plm.gnnt.gnntviewentity.view.components;

import java.time.LocalDate;
import tech.derbent.plm.gannt.ganntviewentity.view.components.CGanntTimelineBar;
import tech.derbent.plm.gnnt.gnntitem.domain.CGnntItem;

public class CGnntTimelineBar extends CGanntTimelineBar {

	private static final long serialVersionUID = 1L;

	public CGnntTimelineBar(final CGnntItem item, final LocalDate timelineStart, final LocalDate timelineEnd, final int totalWidth) {
		super(item, timelineStart, timelineEnd, totalWidth);
	}
}
