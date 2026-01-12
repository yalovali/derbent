package tech.derbent.app.attachments.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.app.attachments.domain.CAttachment;

/**
 * IAttachmentRepository - Repository interface for CAttachment entities.
 * 
 * Simple repository with basic queries. Parent entities query their attachments
 * via their @OneToMany collections, not through repository methods.
 * 
 * Layer: Service (MVC) - Repository interface
 */
public interface IAttachmentRepository extends IEntityOfCompanyRepository<CAttachment> {

	/**
	 * Find all attachments that reference a specific attachment as previous version.
	 * Used to check if an attachment can be deleted (cannot delete if newer versions exist).
	 * @param previousVersion the previous version attachment
	 * @return list of attachments that reference this as previous version, ordered by version DESC
	 */
	@Query("SELECT a FROM CAttachment a WHERE a.previousVersion = :previousVersion ORDER BY a.versionNumber DESC")
	List<CAttachment> findByPreviousVersion(@Param("previousVersion") CAttachment previousVersion);

	/**
	 * Find attachment by ID with eager loading of related entities.
	 * @param id the attachment ID
	 * @return optional attachment with uploadedBy, documentType, previousVersion eagerly loaded
	 */
	@EntityGraph(attributePaths = {"uploadedBy", "documentType", "previousVersion", "company"})
	@Override
	Optional<CAttachment> findById(Long id);
}
