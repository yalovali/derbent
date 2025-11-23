package tech.derbent.app.sprints.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.app.sprints.domain.CSprintStatus;

/**
 * CPageServiceSprintStatus - Page service for Sprint Status management UI.
 * Handles UI events and interactions for sprint status views.
 */
public class CPageServiceSprintStatus extends CPageServiceDynamicPage<CSprintStatus> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceSprintStatus.class);
	private static final long serialVersionUID = 1L;

	public CPageServiceSprintStatus(final IPageServiceImplementer<CSprintStatus> view) {
		super(view);
	}
}
