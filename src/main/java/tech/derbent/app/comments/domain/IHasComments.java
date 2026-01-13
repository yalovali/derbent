package tech.derbent.app.comments.domain;

import java.util.Set;

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
}
