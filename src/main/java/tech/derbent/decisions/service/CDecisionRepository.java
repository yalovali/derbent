package tech.derbent.decisions.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.derbent.abstracts.services.CEntityOfProjectRepository;
import tech.derbent.decisions.domain.CDecision;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

/**
 * CDecisionRepository - Repository interface for CDecision entities. Layer: Data Access
 * (MVC) Provides data access methods for decision entities including project-based
 * queries and eagerly loaded relationships to prevent LazyInitializationException.
 */
public interface CDecisionRepository extends CEntityOfProjectRepository<CDecision> {

	/**
	 * Finds a decision by ID with all main relationships eagerly loaded to prevent
	 * LazyInitializationException. This extends the base method with decision-specific
	 * relationships.
	 * @param id the decision ID
	 * @return optional CDecision with loaded relationships
	 */
	@Query (
		"SELECT d FROM CDecision d " + "LEFT JOIN FETCH d.project "
			+ "LEFT JOIN FETCH d.assignedTo " + "LEFT JOIN FETCH d.createdBy "
			+ "LEFT JOIN FETCH d.decisionType " + "LEFT JOIN FETCH d.decisionStatus "
			+ "LEFT JOIN FETCH d.accountableUser " + "WHERE d.id = :id"
	)
	Optional<CDecision> findByIdWithAllRelationships(@Param ("id") Long id);
	/**
	 * Finds decisions by project with eagerly loaded relationships and pagination.
	 * @param project  the project
	 * @param pageable pagination information
	 * @return page of CDecision with loaded relationships
	 */
	@Query (
		"SELECT d FROM CDecision d " + "LEFT JOIN FETCH d.project "
			+ "LEFT JOIN FETCH d.assignedTo " + "LEFT JOIN FETCH d.createdBy "
			+ "LEFT JOIN FETCH d.decisionType " + "LEFT JOIN FETCH d.decisionStatus "
			+ "LEFT JOIN FETCH d.accountableUser " + "WHERE d.project = :project"
	)
	Page<CDecision> findByProjectWithAllRelationships(@Param ("project") CProject project,
		Pageable pageable);
	/**
	 * Finds decisions where the user is a team member.
	 * @param user the team member user
	 * @return list of decisions where the user is a team member
	 */
	@Query ("SELECT d FROM CDecision d JOIN d.teamMembers tm WHERE tm = :user")
	List<CDecision> findByTeamMembersContaining(@Param ("user") CUser user);
	/**
	 * Finds decisions that require approval from a specific user.
	 * @param user the approver user
	 * @return list of decisions that need approval from the user
	 */
	@Query (
		"SELECT DISTINCT d FROM CDecision d JOIN d.approvals a WHERE a.approver = :user AND a.isApproved IS NULL"
	)
	List<CDecision> findDecisionsPendingApprovalByUser(@Param ("user") CUser user);
}