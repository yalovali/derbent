package tech.derbent.app.comments.domain;

import java.util.Set;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.entity.domain.CEntityDB;

/**
 * IHasComments - Interface for entities that can have comments.
 * 
 * Entities implementing this interface can have comments managed via comment components.
 * 
 * Pattern: Unidirectional @OneToMany from parent entity to CComment.
 * CComment has NO back-reference to parent (clean unidirectional relationship).
 * 
 * Usage in entity:
 * <pre>
 * public class CActivity extends CProjectItem<CActivity> implements IHasComments {
 *
 *     {@literal @}OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
 *     {@literal @}JoinColumn(name = "activity_id")
 *     {@literal @}AMetaData(
 *         displayName = "Comments",
 *         dataProviderBean = "CCommentService",
 *         createComponentMethod = "createComponent"
 *     )
 *     private Set<CComment> comments = new HashSet<>();
 *
 *     {@literal @}Override
 *     public Set<CComment> getComments() {
 *         if (comments == null) {
 *             comments = new HashSet<>();
 *         }
 *         return comments;
 *     }
 *
 *     {@literal @}Override
 *     public void setComments(Set<CComment> comments) {
 *         this.comments = comments;
 *     }
 * }
 * </pre>
 *
 * Layer: Domain (MVC)
 */
public interface IHasComments {

	/**
	 * Get the set of comments for this entity.
	 * Implementation should never return null - return empty set if no comments.
	 * Initialize the set if null before returning.
	 * 
	 * @return set of comments, never null
	 */
	Set<CComment> getComments();

	/**
	 * Set the set of comments for this entity.
	 * 
	 * @param comments the comments set, can be null (will be initialized on next get)
	 */
	void setComments(Set<CComment> comments);

	/** Copy comments from source to target if both implement IHasComments and options allow. This default method reduces code duplication by providing
	 * a standard implementation of comment copying.
	 * @param source the source entity
	 * @param target the target entity
	 * @param options copy options controlling whether comments are included
	 * @return true if comments were copied, false if skipped */
	static boolean copyCommentsTo(final CEntityDB<?> source, final CEntityDB<?> target, final CCloneOptions options) {
		// Check if comments should be copied
		if (!options.includesComments()) {
			return false;
		}
		// Check if both source and target implement IHasComments
		if (!(source instanceof IHasComments) || !(target instanceof IHasComments)) {
			return false; // Skip silently if target doesn't support comments
		}
		try {
			final IHasComments sourceWithComments = (IHasComments) source;
			final IHasComments targetWithComments = (IHasComments) target;
			// Copy comment collection using source's copyCollection method
			source.copyCollection(sourceWithComments::getComments, 
					(col) -> targetWithComments.setComments((java.util.Set<CComment>) col), 
					true);  // createNew = true to clone comments
			return true;
		} catch (final Exception e) {
			// Log and skip on error - don't fail entire copy operation
			return false;
		}
	}
}
