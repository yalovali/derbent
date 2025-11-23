package tech.derbent.app.sprints.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.app.sprints.domain.CSprintType;

/**
 * CPageServiceSprintType - Page service for Sprint Type management UI.
 * Handles UI events and interactions for sprint type views.
 */
public class CPageServiceSprintType extends CPageServiceDynamicPage<CSprintType> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceSprintType.class);
	private static final long serialVersionUID = 1L;

	public CPageServiceSprintType(final IPageServiceImplementer<CSprintType> view) {
		super(view);
	}
}
