package tech.derbent.comments.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CAbstractNamedEntityService;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.comments.domain.CComment;
import tech.derbent.comments.domain.CCommentPriority;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

/**
 * CCommentService - Service class for CComment entities.
 * Layer: Service (MVC)
 * 
 * Provides business logic operations for comment management including:
 * - CRUD operations
 * - Activity-based comment queries
 * - Project-based comment queries
 * - Author-based comment queries
 * - Comment creation with validation
 * - Data provider functionality for UI components
 */
@Service
@PreAuthorize("isAuthenticated()")
public class CCommentService extends CAbstractNamedEntityService<CComment> {

    private final CCommentPriorityService commentPriorityService;

    /**
     * Constructor for CCommentService.
     * @param repository the comment repository
     * @param commentPriorityService the comment priority service
     * @param clock the Clock instance for time-related operations
     */
    CCommentService(final CCommentRepository repository, 
                   final CCommentPriorityService commentPriorityService,
                   final Clock clock) {
        super(repository, clock);
        this.commentPriorityService = commentPriorityService;
    }

    @Override
    protected CComment createNewEntityInstance() {
        return new CComment();
    }

    /**
     * Finds all comments for a specific activity, ordered by event date (chronological).
     * @param activity the activity
     * @return list of comments for the activity ordered by event date
     */
    @PreAuthorize("permitAll()")
    public List<CComment> findByActivityOrderByEventDateAsc(final CActivity activity) {
        LOGGER.info("findByActivityOrderByEventDateAsc called with activity: {}", activity);
        
        if (activity == null) {
            LOGGER.warn("findByActivityOrderByEventDateAsc called with null activity");
            return List.of();
        }
        
        return ((CCommentRepository) repository).findByActivityOrderByEventDateAsc(activity);
    }

    /**
     * Finds all comments for a specific activity with pagination.
     * @param activity the activity
     * @param pageable pagination information
     * @return page of comments for the activity ordered by event date
     */
    @PreAuthorize("permitAll()")
    public Page<CComment> findByActivityOrderByEventDateAsc(final CActivity activity, final Pageable pageable) {
        LOGGER.info("findByActivityOrderByEventDateAsc called with activity: {}, pageable: {}", activity, pageable);
        
        if (activity == null) {
            LOGGER.warn("findByActivityOrderByEventDateAsc called with null activity");
            return Page.empty();
        }
        
        return ((CCommentRepository) repository).findByActivityOrderByEventDateAsc(activity, pageable);
    }

    /**
     * Finds all comments for a specific project, ordered by event date.
     * @param project the project
     * @return list of comments for the project ordered by event date
     */
    @PreAuthorize("permitAll()")
    public List<CComment> findByProjectOrderByEventDateAsc(final CProject project) {
        LOGGER.info("findByProjectOrderByEventDateAsc called with project: {}", project);
        
        if (project == null) {
            LOGGER.warn("findByProjectOrderByEventDateAsc called with null project");
            return List.of();
        }
        
        return ((CCommentRepository) repository).findByProjectOrderByEventDateAsc(project);
    }

    /**
     * Finds all comments by a specific author, ordered by event date (newest first).
     * @param author the comment author
     * @return list of comments by the author ordered by event date
     */
    @PreAuthorize("permitAll()")
    public List<CComment> findByAuthorOrderByEventDateDesc(final CUser author) {
        LOGGER.info("findByAuthorOrderByEventDateDesc called with author: {}", author);
        
        if (author == null) {
            LOGGER.warn("findByAuthorOrderByEventDateDesc called with null author");
            return List.of();
        }
        
        return ((CCommentRepository) repository).findByAuthorOrderByEventDateDesc(author);
    }

    /**
     * Finds all comments for an activity with eagerly loaded relationships.
     * @param activity the activity
     * @return list of comments with loaded relationships ordered by event date
     */
    @PreAuthorize("permitAll()")
    public List<CComment> findByActivityWithRelationships(final CActivity activity) {
        LOGGER.info("findByActivityWithRelationships called with activity: {}", activity);
        
        if (activity == null) {
            LOGGER.warn("findByActivityWithRelationships called with null activity");
            return List.of();
        }
        
        return ((CCommentRepository) repository).findByActivityWithRelationships(activity);
    }

    /**
     * Overrides the base get method to eagerly load relationships.
     * This prevents LazyInitializationException when the entity is used in UI components.
     */
    @Override
    public Optional<CComment> get(final Long id) {
        LOGGER.info("get called with id: {}", id);
        
        if (id == null) {
            LOGGER.warn("get called with null id");
            return Optional.empty();
        }
        
        return ((CCommentRepository) repository).findByIdWithRelationships(id);
    }

    /**
     * Counts the number of comments for a specific activity.
     * @param activity the activity
     * @return count of comments for the activity
     */
    @PreAuthorize("permitAll()")
    public long countByActivity(final CActivity activity) {
        LOGGER.info("countByActivity called with activity: {}", activity);
        
        if (activity == null) {
            LOGGER.warn("countByActivity called with null activity");
            return 0;
        }
        
        return ((CCommentRepository) repository).countByActivity(activity);
    }

    /**
     * Counts the number of comments for a specific project.
     * @param project the project
     * @return count of comments for the project
     */
    @PreAuthorize("permitAll()")
    public long countByProject(final CProject project) {
        LOGGER.info("countByProject called with project: {}", project);
        
        if (project == null) {
            LOGGER.warn("countByProject called with null project");
            return 0;
        }
        
        return ((CCommentRepository) repository).countByProject(project);
    }

    /**
     * Finds important comments for an activity.
     * @param activity the activity
     * @return list of important comments for the activity ordered by event date
     */
    @PreAuthorize("permitAll()")
    public List<CComment> findImportantByActivity(final CActivity activity) {
        LOGGER.info("findImportantByActivity called with activity: {}", activity);
        
        if (activity == null) {
            LOGGER.warn("findImportantByActivity called with null activity");
            return List.of();
        }
        
        return ((CCommentRepository) repository).findImportantByActivity(activity);
    }

    /**
     * Finds recent comments for a project (last 30 days).
     * @param project the project
     * @return list of recent comments for the project
     */
    @PreAuthorize("permitAll()")
    public List<CComment> findRecentByProject(final CProject project) {
        LOGGER.info("findRecentByProject called with project: {}", project);
        
        if (project == null) {
            LOGGER.warn("findRecentByProject called with null project");
            return List.of();
        }
        
        // Calculate the date 30 days ago for database portability
        final java.time.LocalDateTime thirtyDaysAgo = java.time.LocalDateTime.now().minusDays(30);
        
        return ((CCommentRepository) repository).findRecentByProject(project, thirtyDaysAgo);
    }

    /**
     * Creates a new comment with validation.
     * @param commentText the comment content text
     * @param activity the activity this comment belongs to
     * @param author the user who created this comment
     * @return the created comment
     */
    @Transactional
    public CComment createComment(final String commentText, final CActivity activity, final CUser author) {
        LOGGER.info("createComment called with commentText: {}, activity: {}, author: {}", 
            commentText, activity, author);
        
        if (commentText == null || commentText.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment text cannot be null or empty");
        }
        
        if (activity == null) {
            throw new IllegalArgumentException("Activity cannot be null");
        }
        
        if (author == null) {
            throw new IllegalArgumentException("Author cannot be null");
        }
        
        final CComment comment = new CComment(commentText, activity, author);
        
        // Set default priority if none specified
        final Optional<CCommentPriority> defaultPriority = commentPriorityService.findDefaultPriority();
        if (defaultPriority.isPresent()) {
            comment.setPriority(defaultPriority.get());
        }
        
        return save(comment);
    }

    /**
     * Creates a new comment with priority.
     * @param commentText the comment content text
     * @param activity the activity this comment belongs to
     * @param author the user who created this comment
     * @param priority the priority level of this comment
     * @return the created comment
     */
    @Transactional
    public CComment createComment(final String commentText, final CActivity activity, 
                                 final CUser author, final CCommentPriority priority) {
        LOGGER.info("createComment called with commentText: {}, activity: {}, author: {}, priority: {}", 
            commentText, activity, author, priority);
        
        if (commentText == null || commentText.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment text cannot be null or empty");
        }
        
        if (activity == null) {
            throw new IllegalArgumentException("Activity cannot be null");
        }
        
        if (author == null) {
            throw new IllegalArgumentException("Author cannot be null");
        }
        
        final CComment comment = new CComment(commentText, activity, author, priority);
        return save(comment);
    }

    /**
     * Updates comment text.
     * @param comment the comment to update
     * @param newText the new comment text
     * @return the updated comment
     */
    @Transactional
    public CComment updateCommentText(final CComment comment, final String newText) {
        LOGGER.info("updateCommentText called with comment: {}, newText: {}", comment, newText);
        
        if (comment == null) {
            throw new IllegalArgumentException("Comment cannot be null");
        }
        
        if (newText == null || newText.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment text cannot be null or empty");
        }
        
        comment.setCommentText(newText);
        return save(comment);
    }

    /**
     * Toggles the important flag of a comment.
     * @param comment the comment to toggle
     * @return the updated comment
     */
    @Transactional
    public CComment toggleImportant(final CComment comment) {
        LOGGER.info("toggleImportant called with comment: {}", comment);
        
        if (comment == null) {
            throw new IllegalArgumentException("Comment cannot be null");
        }
        
        comment.setImportant(!comment.isImportant());
        return save(comment);
    }
}