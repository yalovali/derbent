package tech.derbent.plm.gnnt.gnntviewentity.view.components;

import java.time.LocalDate;
import tech.derbent.plm.gannt.ganntviewentity.view.components.CGanntTimelineHeader;

public class CGnntTimelineHeader extends CGanntTimelineHeader {

	private static final long serialVersionUID = 1L;

	public CGnntTimelineHeader(final LocalDate startDate, final LocalDate endDate, final int totalWidth,
			final IGanttTimelineChangeListener changeListener, final IGanttWidthChangeListener widthChangeListener) {
		super(startDate, endDate, totalWidth, changeListener, widthChangeListener);
	}
}
