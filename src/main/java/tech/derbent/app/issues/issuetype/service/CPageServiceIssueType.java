package tech.derbent.app.issues.issuetype.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.issues.issuetype.domain.CIssueType;

public class CPageServiceIssueType extends CPageServiceDynamicPage<CIssueType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceIssueType.class);
	Long serialVersionUID = 1L;

	public CPageServiceIssueType(IPageServiceImplementer<CIssueType> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CIssueType.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(),
					CIssueType.class.getSimpleName(), e.getMessage());
			throw e;
		}
	}
}
