package tech.derbent.comments.service;

import java.time.Clock;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.comments.domain.CComment;
import tech.derbent.comments.domain.CCommentPriority;
import tech.derbent.users.domain.CUser;

/** CCommentService - Service class for CComment entities. Layer: Service (MVC) Provides business logic operations for comment management including: -
 * CRUD operations - Activity-based comment queries - Project-based comment queries - Author-based comment queries - Comment creation with validation
 * - Data provider functionality for UI components */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CCommentService extends CAbstractService<CComment> {

	public CCommentService(final CCommentRepository repository, final CCommentPriorityService commentPriorityService, final Clock clock) {
		super(repository, clock);
	}

	@PreAuthorize ("permitAll()")
	public long countByActivity(final CActivity activity) {
		if (activity == null) {
			return 0L;
		}
		return ((CCommentRepository) repository).countByActivity(activity);
	}

	@Transactional
	public CComment createComment(final String commentText, final CActivity activity, final CUser author) {
		Check.notBlank(commentText, "Comment text cannot be null or empty");
		Check.notNull(activity, "Activity cannot be null");
		Check.notNull(author, "Author cannot be null");
		final CComment comment = new CComment(commentText, activity, author);
		return save(comment);
	}

	/** Creates a new comment with priority.
	 * @param commentText the comment content text
	 * @param activity    the activity this comment belongs to
	 * @param author      the user who created this comment
	 * @param priority    the priority level of this comment
	 * @return the created comment */
	@Transactional
	public CComment createComment(final String commentText, final CActivity activity, final CUser author, final CCommentPriority priority) {
		Check.notBlank(commentText, "Comment text cannot be null or empty");
		Check.notNull(activity, "Activity cannot be null");
		Check.notNull(author, "Author cannot be null");
		final CComment comment = new CComment(commentText, activity, author, priority);
		return save(comment);
	}

	/** Finds all comments for a specific activity, ordered by event date (chronological).
	 * @param activity the activity
	 * @return list of comments for the activity ordered by event date */
	@PreAuthorize ("permitAll()")
	public List<CComment> findByActivity(final CActivity activity) {
		if (activity == null) {
			return List.of();
		}
		return ((CCommentRepository) repository).findByActivity(activity);
	}

	/** Finds all comments for a specific activity with pagination.
	 * @param activity the activity
	 * @param pageable pagination information
	 * @return page of comments for the activity ordered by event date */
	@PreAuthorize ("permitAll()")
	public Page<CComment> findByActivity(final CActivity activity, final Pageable pageable) {
		Check.notNull(activity, "Activity cannot be null");
		return ((CCommentRepository) repository).findByActivity(activity, pageable);
	}

	/** Finds important comments for an activity.
	 * @param activity the activity
	 * @return list of important comments for the activity ordered by event date */
	@PreAuthorize ("permitAll()")
	public List<CComment> findImportantByActivity(final CActivity activity) {
		Check.notNull(activity, "Activity cannot be null");
		return ((CCommentRepository) repository).findImportantByActivity(activity);
	}

	@Override
	protected Class<CComment> getEntityClass() { return CComment.class; }

	/** Toggles the important flag of a comment.
	 * @param comment the comment to toggle
	 * @return the updated comment */
	@Transactional
	public CComment toggleImportant(final CComment comment) {
		Check.notNull(comment, "Comment cannot be null");
		comment.setImportant(!comment.isImportant());
		return save(comment);
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
