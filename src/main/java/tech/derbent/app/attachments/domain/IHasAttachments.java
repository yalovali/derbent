package tech.derbent.app.attachments.domain;

import java.util.Set;

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

	/** Get the list of attachments for this entity. Implementation should never return null - return empty list if no attachments. Initialize the
	 * list if null before returning.
	 * @return list of attachments, never null */
	Set<CAttachment> getAttachments();
	/** Set the list of attachments for this entity.
	 * @param attachments the attachments list, can be null (will be initialized on next get) */
	void setAttachments(Set<CAttachment> attachments);
}
