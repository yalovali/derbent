package tech.derbent.api.users.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.users.component.CComponentPasswordChange;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.utils.Check;

public class CPageServiceUser extends CPageServiceDynamicPage<CUser> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceUser.class);
	Long serialVersionUID = 1L;

	public CPageServiceUser(IPageServiceImplementer<CUser> view) {
		super(view);
	}

	@Override
	public void actionCreate() throws Exception {
		super.actionCreate();
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CUser");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CUser> gridView = (CGridViewBaseDBEntity<CUser>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CUser.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CUser.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}

	/** Creates password change component for user management. Called by CFormBuilder when building form from @AMetaData.
	 * @return CComponentPasswordChange for password management */
	public Component createComponentPasswordChange() {
		try {
			final CComponentPasswordChange component = new CComponentPasswordChange();
			// add to custom components to be binded.
			registerComponent(component.getComponentName(), component);
			return component;
		} catch (final Exception e) {
			LOGGER.error("Error creating user password change component: {}", e.getMessage());
			final com.vaadin.flow.component.html.Div errorDiv = new com.vaadin.flow.component.html.Div();
			errorDiv.setText("Failed to load password change component: " + e.getMessage());
			errorDiv.addClassName("error-message");
			return errorDiv;
		}
	}
}
