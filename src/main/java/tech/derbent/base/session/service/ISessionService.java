package tech.derbent.base.session.service;

import java.util.List;
import java.util.Optional;
import tech.derbent.api.interfaces.IProjectChangeListener;
import tech.derbent.api.interfaces.IProjectListChangeListener;
import tech.derbent.app.companies.domain.CCompany;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.projects.events.ProjectListChangeEvent;
import tech.derbent.base.users.domain.CUser;

public interface ISessionService {

	void addProjectChangeListener(IProjectChangeListener listener);
	void addProjectListChangeListener(IProjectListChangeListener listener);
	void clearSession();
	// Company management methods
	Optional<CCompany> getActiveCompany();
	Long getActiveId(String entityType);
	Optional<CProject> getActiveProject();
	Optional<CUser> getActiveUser();
	List<CProject> getAvailableProjects();
	CCompany getCurrentCompany();
	void handleProjectListChange(ProjectListChangeEvent event);
	void notifyProjectListChanged();
	void removeProjectChangeListener(IProjectChangeListener listener);
	void removeProjectListChangeListener(IProjectListChangeListener listener);
	void setActiveId(String entityType, Long id);
	void setActiveProject(CProject project);
	void setActiveUser(CUser user);
	void setLayoutService(CLayoutService layoutService);
}
