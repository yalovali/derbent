package tech.derbent.comments.service;

import java.time.Clock;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.api.utils.Check;
import tech.derbent.comments.domain.CComment;
import tech.derbent.session.service.ISessionService;
import tech.derbent.users.domain.CUser;

/** CCommentService - Service class for CComment entities. Layer: Service (MVC) Provides business logic operations for comment management including: -
 * CRUD operations - Activity-based comment queries - Project-based comment queries - Author-based comment queries - Comment creation with validation
 * - Data provider functionality for UI components */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CCommentService extends CAbstractService<CComment> {

	public CCommentService(final ICommentRepository repository, final CCommentPriorityService commentPriorityService, final Clock clock,
			final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@PreAuthorize ("permitAll()")
	public long countByActivity(final CActivity activity) {
		Check.notNull(activity, "Activity cannot be null");
		return ((ICommentRepository) repository).countByActivity(activity);
	}

	@Transactional
	public CComment createComment(final String commentText, final CActivity activity, final CUser author) {
		Check.notBlank(commentText, "Comment text cannot be null or empty");
		Check.notNull(activity, "Activity cannot be null");
		Check.notNull(author, "Author cannot be null");
		final CComment comment = new CComment(commentText, activity, author);
		return save(comment);
	}

	/** Finds all comments for a specific activity, ordered by event date (chronological).
	 * @param activity the activity
	 * @return list of comments for the activity ordered by event date */
	@PreAuthorize ("permitAll()")
	public List<CComment> findByActivity(final CActivity master) {
		Check.notNull(master, "Master cannot be null");
		if (master.getId() == null) {
			// new instance, no lines yet
			return List.of();
		}
		return ((ICommentRepository) repository).findByActivity(master);
	}

	/** Finds all comments for a specific activity with pagination.
	 * @param activity the activity
	 * @param pageable pagination information
	 * @return page of comments for the activity ordered by event date */
	@PreAuthorize ("permitAll()")
	public Page<CComment> findByActivity(final CActivity master, final Pageable pageable) {
		Check.notNull(master, "Master cannot be null");
		if (master.getId() == null) {
			// new instance, no lines yet
			return Page.empty();
		}
		return ((ICommentRepository) repository).findByActivity(master, pageable);
	}

	@Override
	protected Class<CComment> getEntityClass() { return CComment.class; }

	@Override
	public String checkDependencies(final CComment comment) {
		final String superCheck = super.checkDependencies(comment);
		if (superCheck != null) {
			return superCheck;
		}
		// Comments don't have dependencies - they are leaf entities
		return null;
	}

	@Override
	public void initializeNewEntity(final CComment entity) {
		super.initializeNewEntity(entity);
		Check.notNull(entity, "Comment cannot be null");
		// CComment initialization - stub for now as it's typically initialized with activity and author
		LOGGER.debug("Initialized new comment entity");
	}

	/** Updates comment text.
	 * @param comment the comment to update
	 * @param newText the new comment text
	 * @return the updated comment */
	@Transactional
	public CComment updateCommentText(final CComment comment, final String newText) {
		Check.notNull(comment, "Comment cannot be null");
		Check.notBlank(newText, "Comment text cannot be null or empty");
		comment.setCommentText(newText);
		return save(comment);
	}
}
