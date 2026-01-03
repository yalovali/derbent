package tech.derbent.app.comments.service;

import java.time.Clock;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.interfaces.ISearchable;
import tech.derbent.api.utils.Check;
import tech.derbent.api.utils.CPageableUtils;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.comments.domain.CComment;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;

/** CCommentService - Service class for CComment entities. Layer: Service (MVC) Provides business logic operations for comment management including: -
 * CRUD operations - Activity-based comment queries - Project-based comment queries - Author-based comment queries - Comment creation with validation
 * - Data provider functionality for UI components */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CCommentService extends CAbstractService<CComment> implements IEntityRegistrable, IEntityWithView {

	public CCommentService(final ICommentRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final CComment comment) {
		return super.checkDeleteAllowed(comment);
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
	@Transactional (readOnly = true)
	public Page<CComment> listForPageView(final Pageable pageable, final String searchText) throws Exception {
		final Pageable safePage = CPageableUtils.validateAndFix(pageable);
		final String term = searchText == null ? "" : searchText.trim();
		final Sort defaultSort = getDefaultSort();
		final List<CComment> all = ((ICommentRepository) repository).findAllForPageView(defaultSort);
		final boolean searchable = ISearchable.class.isAssignableFrom(getEntityClass());
		final List<CComment> filtered = term.isEmpty() || !searchable ? all : all.stream().filter(e -> ((ISearchable) e).matches(term)).toList();
		final int start = (int) Math.min(safePage.getOffset(), filtered.size());
		final int end = Math.min(start + safePage.getPageSize(), filtered.size());
		final List<CComment> content = filtered.subList(start, end);
		return new PageImpl<>(content, safePage, filtered.size());
	}

	@Override
	public Class<CComment> getEntityClass() { return CComment.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CCommentInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceComment.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final CComment entity) {
		super.initializeNewEntity(entity);
		// Get current user from session
		final CUser currentUser = sessionService.getActiveUser()
				.orElseThrow(() -> new CInitializationException("No active user in session - cannot initialize comment"));
		// Initialize author with current user if not already set
		if (entity.getAuthor() == null) {
			entity.setAuthor(currentUser);
		}
		// Initialize important flag if not set
		if (entity.isImportant() == null) {
			entity.setImportant(Boolean.FALSE);
		}
		// Note: activity is required and must be set before saving (typically done through createComment method)
		// Note: commentText is required and must be set before saving
		// Note: priority is optional and can remain null
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
