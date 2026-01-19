package tech.derbent.api.workflow.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.activities.domain.CActivity;
import tech.derbent.plm.activities.service.CPageServiceActivity;
import tech.derbent.api.workflow.domain.CWorkflowStatusRelation;

public class CPageServiceWorkflowStatusRelation extends CPageServiceDynamicPage<CWorkflowStatusRelation> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceActivity.class);
	Long serialVersionUID = 1L;

	public CPageServiceWorkflowStatusRelation(IPageServiceImplementer<CWorkflowStatusRelation> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CActivity.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CActivity.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}
}
