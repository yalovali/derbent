package tech.derbent.app.activities.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import tech.derbent.api.services.pageservice.CPageServiceWithWorkflow;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.app.activities.domain.CActivity;

public class CPageServiceActivity extends CPageServiceWithWorkflow<CActivity> {

	public Logger LOGGER = LoggerFactory.getLogger(CPageServiceActivity.class);
	Long serialVersionUID = 1L;

	public CPageServiceActivity(final IPageServiceImplementer<CActivity> view) {
		super(view);
	}

	public void on_description_blur(final Component component, final Object value) {
		LOGGER.info("function: on_description_blur for Component type: {}",
				component.getClass().getSimpleName() + " current value: " + value + " on page service:" + this.getClass().getSimpleName());
	}

	public void on_description_focus(final Component component, final Object value) {
		LOGGER.info("function: on_description_focus for Component type: {}",
				component.getClass().getSimpleName() + " current value: " + value + " on page service:" + this.getClass().getSimpleName());
	}

	public void on_name_change(final Component component, final Object value) {
		LOGGER.info("function: on_name_change for Component type: {}",
				component.getClass().getSimpleName() + " current value: " + value + " on page service:" + this.getClass().getSimpleName());
	}

	public void on_status_change(final Component component, final Object value) {
		LOGGER.info("function: on_status_change for Component type: {}",
				component.getClass().getSimpleName() + " current value: " + value + " on page service:" + this.getClass().getSimpleName());
	}
}
