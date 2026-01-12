package tech.derbent.app.attachments.domain;

import java.util.List;
import tech.derbent.api.entity.domain.CEntityDB;

/**
 * IAttachmentOwner - Interface for entities that can have attachments.
 * 
 * Entities implementing this interface can have multiple attachments linked to them.
 * Similar to how activities have comments, attachment owners have attachments.
 * 
 * Example implementations: CActivity, CRisk, CMeeting, CSprint, CProject
 */
public interface IAttachmentOwner {
	
	/** Get the unique ID of this entity.
	 * @return entity ID */
	Long getId();
	
	/** Get the name of this entity for display purposes.
	 * @return entity name */
	String getName();
	
	/** Get the list of attachments associated with this entity.
	 * @return list of attachments, never null */
	List<CAttachment> getAttachments();
	
	/** Set the list of attachments for this entity.
	 * @param attachments the list of attachments */
	void setAttachments(List<CAttachment> attachments);
	
	/** Get the entity class for type identification.
	 * @return the entity class */
	Class<? extends CEntityDB<?>> getEntityClass();
}
