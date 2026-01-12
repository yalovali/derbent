package tech.derbent.app.attachments.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.attachments.domain.CAttachment;
import tech.derbent.app.meetings.domain.CMeeting;
import tech.derbent.app.risks.risk.domain.CRisk;
import tech.derbent.app.sprints.domain.CSprint;

/**
 * Repository interface for CAttachment entities.
 * Provides data access methods for attachment management.
 */
public interface IAttachmentRepository extends IEntityOfProjectRepository<CAttachment> {

	/** Find all attachments for an activity.
	 * @param activity the activity
	 * @return list of attachments */
	@Query("SELECT e FROM #{#entityName} e WHERE e.activity = :activity ORDER BY e.uploadDate DESC")
	List<CAttachment> findByActivity(@Param("activity") CActivity activity);

	/** Find all attachments for a risk.
	 * @param risk the risk
	 * @return list of attachments */
	@Query("SELECT e FROM #{#entityName} e WHERE e.risk = :risk ORDER BY e.uploadDate DESC")
	List<CAttachment> findByRisk(@Param("risk") CRisk risk);

	/** Find all attachments for a meeting.
	 * @param meeting the meeting
	 * @return list of attachments */
	@Query("SELECT e FROM #{#entityName} e WHERE e.meeting = :meeting ORDER BY e.uploadDate DESC")
	List<CAttachment> findByMeeting(@Param("meeting") CMeeting meeting);

	/** Find all attachments for a sprint.
	 * @param sprint the sprint
	 * @return list of attachments */
	@Query("SELECT e FROM #{#entityName} e WHERE e.sprint = :sprint ORDER BY e.uploadDate DESC")
	List<CAttachment> findBySprint(@Param("sprint") CSprint sprint);

	/** Find all versions of an attachment (following previousVersion chain).
	 * @param attachment the attachment
	 * @return list of attachments in version history */
	@Query("SELECT e FROM #{#entityName} e WHERE e.id = :id OR e.previousVersion.id = :id ORDER BY e.versionNumber DESC")
	List<CAttachment> findVersionHistory(@Param("id") Long id);

	/** Find the latest version of an attachment by file name in a project.
	 * @param fileName the file name
	 * @param project the project
	 * @return the latest version attachment or null */
	@Query("SELECT e FROM #{#entityName} e WHERE e.fileName = :fileName AND e.project = :project ORDER BY e.versionNumber DESC")
	List<CAttachment> findByFileNameAndProject(@Param("fileName") String fileName, @Param("project") CProject project);
}
