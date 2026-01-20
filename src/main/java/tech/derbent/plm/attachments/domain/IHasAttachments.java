package tech.derbent.plm.attachments.domain;

import java.util.Set;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.interfaces.CCloneOptions;

/** IHasAttachments - Interface for entities that can have file attachments. Entities implementing this interface can have attachments managed via the
 * CComponentListAttachments component. Pattern: Unidirectional @OneToMany from parent entity to CAttachment. CAttachment has NO back-reference to
 * parent (clean unidirectional relationship). Usage in entity:
 *
 * <pre>
 * public class CActivity extends CProjectItem<CActivity> implements IHasAttachments {
 *
 *     {@literal @}OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
 *     {@literal @}JoinColumn(name = "activity_id")
 *     {@literal @}AMetaData(
 *         displayName = "Attachments",
 *         dataProviderBean = "CAttachmentService",
 *         createComponentMethod = "createComponent"
 *     )
 *     private Set<CAttachment> attachments = new HashSet<>();
 *
 *     {@literal @}Override
 *     public Set<CAttachment> getAttachments() {
 *         if (attachments == null) {
 *             attachments = new HashSet<>();
 *         }
 *         return attachments;
 *     }
 *
 *     {@literal @}Override
 *     public void setAttachments(List<CAttachment> attachments) {
 *         this.attachments = attachments;
 *     }
 * }
 * </pre>
 *
 * Layer: Domain (MVC) */
public interface IHasAttachments {

	/** Copy attachments from source to target if both implement IHasAttachments and options allow. This default method reduces code duplication by
	 * providing a standard implementation of attachment copying.
	 * @param source  the source entity
	 * @param target  the target entity
	 * @param options copy options controlling whether attachments are included
	 * @return true if attachments were copied, false if skipped */
	static boolean copyAttachmentsTo(final CEntityDB<?> source, final CEntityDB<?> target, final CCloneOptions options) {
		// Check if attachments should be copied
		if (!options.includesAttachments()) {
			return false;
		}
		// Check if both source and target implement IHasAttachments
		if (!(source instanceof IHasAttachments) || !(target instanceof IHasAttachments)) {
			return false; // Skip silently if target doesn't support attachments
		}
		try {
			final IHasAttachments sourceWithAttachments = (IHasAttachments) source;
			final IHasAttachments targetWithAttachments = (IHasAttachments) target;
			// Copy attachment collection using source's copyCollection method
			CEntityDB.copyCollection(sourceWithAttachments::getAttachments,
					(col) -> targetWithAttachments.setAttachments((java.util.Set<CAttachment>) col), true); // createNew = true to clone attachments
			return true;
		} catch ( final Exception e) {
			// Log and skip on error - don't fail entire copy operation
			return false;
		}
	}

	/** Get the list of attachments for this entity. Implementation should never return null - return empty list if no attachments. Initialize the
	 * list if null before returning.
	 * @return list of attachments, never null */
	Set<CAttachment> getAttachments();
	/** Set the list of attachments for this entity.
	 * @param attachments the attachments list, can be null (will be initialized on next get) */
	void setAttachments(Set<CAttachment> attachments);
}
