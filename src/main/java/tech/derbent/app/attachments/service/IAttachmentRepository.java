package tech.derbent.app.attachments.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.attachments.domain.CAttachment;

/**
 * IAttachmentRepository - Repository interface for CAttachment entity.
 * 
 * Provides data access methods for attachment management including queries
 * to find attachments by owner entity (Activity, Risk, Meeting, Sprint, Project).
 * 
 * Pattern: Uses generic ownerEntityId and ownerEntityType fields to support
 * multiple parent entity types without multiple foreign key columns.
 */
public interface IAttachmentRepository extends IEntityOfProjectRepository<CAttachment> {

	/** Find all attachments for a specific owner entity.
	 * @param ownerEntityId the ID of the owner entity
	 * @param ownerEntityType the type of the owner entity (e.g., "CActivity", "CRisk")
	 * @return list of attachments ordered by upload date descending */
	@Query("SELECT a FROM #{#entityName} a WHERE a.ownerEntityId = :ownerEntityId AND a.ownerEntityType = :ownerEntityType ORDER BY a.uploadDate DESC")
	List<CAttachment> findByOwner(@Param("ownerEntityId") Long ownerEntityId, @Param("ownerEntityType") String ownerEntityType);

	/** Find all attachments for a specific owner entity type.
	 * @param ownerEntityType the type of the owner entity (e.g., "CActivity", "CRisk")
	 * @return list of attachments ordered by upload date descending */
	@Query("SELECT a FROM #{#entityName} a WHERE a.ownerEntityType = :ownerEntityType ORDER BY a.uploadDate DESC")
	List<CAttachment> findByOwnerType(@Param("ownerEntityType") String ownerEntityType);

	/** Find all attachments that reference a specific attachment as previous version.
	 * @param previousVersion the previous version attachment
	 * @return list of attachments that have this as previous version */
	@Query("SELECT a FROM #{#entityName} a WHERE a.previousVersion = :previousVersion ORDER BY a.uploadDate DESC")
	List<CAttachment> findByPreviousVersion(@Param("previousVersion") CAttachment previousVersion);
}
