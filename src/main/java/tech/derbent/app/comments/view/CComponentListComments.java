package tech.derbent.app.comments.view;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.html.Div;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.ui.component.basic.CH3;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.utils.Check;
import tech.derbent.app.comments.domain.CComment;
import tech.derbent.app.comments.domain.IHasComments;
import tech.derbent.app.comments.service.CCommentService;
import tech.derbent.base.session.service.ISessionService;

/** CComponentListComments - Component for managing comments on entities. */
public class CComponentListComments extends CVerticalLayout {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentListComments.class);
	private static final long serialVersionUID = 1L;
	private final CCommentService commentService;
	private IHasComments masterEntity;
	private final ISessionService sessionService;
	private final Div commentsContainer;

	public CComponentListComments(final CCommentService commentService, final ISessionService sessionService) {
		Check.notNull(commentService, "CommentService cannot be null");
		Check.notNull(sessionService, "SessionService cannot be null");
		this.commentService = commentService;
		this.sessionService = sessionService;
		
		setSpacing(false);
		setPadding(false);
		setWidthFull();

		final CH3 header = new CH3("Comments");
		add(header);

		commentsContainer = new Div();
		commentsContainer.setWidthFull();
		commentsContainer.setText("Comments section - functionality will be implemented soon");
		add(commentsContainer);
	}

	public void setMasterEntity(final CEntityDB<?> entity) {
		Check.notNull(entity, "Master entity cannot be null");
		Check.instanceOf(entity, IHasComments.class, "Entity must implement IHasComments");
		this.masterEntity = (IHasComments) entity;
		refreshDisplay();
	}

	public void setValue(final CEntityDB<?> entity) {
		setMasterEntity(entity);
	}

	private void refreshDisplay() {
		if (masterEntity == null) {
			LOGGER.warn("Cannot refresh display - master entity is null");
			return;
		}
		final Set<CComment> comments = masterEntity.getComments();
		if (comments != null && !comments.isEmpty()) {
			commentsContainer.setText("Comments: " + comments.size() + " (display pending implementation)");
		} else {
			commentsContainer.setText("No comments yet");
		}
	}
}
