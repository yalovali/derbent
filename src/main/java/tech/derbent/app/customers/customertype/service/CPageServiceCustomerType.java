package tech.derbent.app.customers.customertype.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.customers.customertype.domain.CCustomerType;

public class CPageServiceCustomerType extends CPageServiceDynamicPage<CCustomerType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceCustomerType.class);
	Long serialVersionUID = 1L;

	public CPageServiceCustomerType(IPageServiceImplementer<CCustomerType> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CCustomerType.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CCustomerType.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}
}
