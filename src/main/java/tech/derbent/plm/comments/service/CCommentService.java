package tech.derbent.plm.comments.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.view.CComponentListComments;

@Service
public class CCommentService extends CEntityOfCompanyService<CComment> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CCommentService.class);

	public CCommentService(
			final ICommentRepository repository,
			final Clock clock,
			final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	public Component createComponent() {
		try {
			final CComponentListComments component = new CComponentListComments(this, sessionService);
			LOGGER.debug("Created comment component");
			return component;
		} catch (final Exception e) {
			LOGGER.error("Failed to create comment component.", e);
			final Div errorDiv = new Div();
			errorDiv.setText("Error loading comment component: " + e.getMessage());
			errorDiv.addClassName("error-message");
			return errorDiv;
		}
	}

	@Override
	public Class<CComment> getEntityClass() {
		return CComment.class;
	}
}
