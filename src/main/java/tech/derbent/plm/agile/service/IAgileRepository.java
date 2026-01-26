package tech.derbent.plm.agile.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IProjectItemRespository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.base.users.domain.CUser;

@NoRepositoryBean
public interface IAgileRepository<T extends tech.derbent.api.entityOfProject.domain.CEntityOfProject<T>> extends IProjectItemRespository<T> {

	@Override
	@Query ("""
			SELECT a FROM #{#entityName} a
			LEFT JOIN FETCH a.project
			LEFT JOIN FETCH a.assignedTo
			LEFT JOIN FETCH a.createdBy
			LEFT JOIN FETCH a.attachments
			LEFT JOIN FETCH a.comments
			LEFT JOIN FETCH a.links
			LEFT JOIN FETCH a.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH a.status
			LEFT JOIN FETCH a.sprintItem si
			LEFT JOIN FETCH si.sprint
			WHERE a.project = :project
			ORDER BY a.id DESC
			""")
	List<T> listByProjectForPageView(@Param ("project") CProject<?> project);

	@Query ("""
				SELECT a FROM #{#entityName} a
				LEFT JOIN FETCH a.project p
				LEFT JOIN FETCH a.sprintItem si
				LEFT JOIN FETCH si.sprint
				WHERE p IN (SELECT ups.project FROM CUserProjectSettings ups WHERE ups.user = :user)
				ORDER BY a.id DESC
				""")
	List<T> listByUser(@Param ("user") CUser user);
}
