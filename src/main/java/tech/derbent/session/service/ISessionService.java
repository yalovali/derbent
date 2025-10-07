package tech.derbent.session.service;

import java.util.List;
import java.util.Optional;
import tech.derbent.api.interfaces.IProjectChangeListener;
import tech.derbent.api.interfaces.IProjectListChangeListener;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.events.ProjectListChangeEvent;
import tech.derbent.users.domain.CUser;

public interface ISessionService {

	void addProjectChangeListener(IProjectChangeListener listener);
	void removeProjectChangeListener(IProjectChangeListener listener);
	void addProjectListChangeListener(IProjectListChangeListener listener);
	void removeProjectListChangeListener(IProjectListChangeListener listener);
	void clearSession();
	void deleteAllActiveIds();
	Long getActiveId(String entityType);
	void setActiveId(String entityType, Long id);
	Optional<CUser> getActiveUser();
	void setActiveUser(CUser user);
	void setActiveCompany(CCompany company);
	Optional<CProject> getActiveProject();
	void setActiveProject(CProject project);
	List<CProject> getAvailableProjects();
	void notifyProjectListChanged();
	void setLayoutService(CLayoutService layoutService);
	void handleProjectListChange(ProjectListChangeEvent event);
	// Company management methods
	Optional<CCompany> getActiveCompany();
	CCompany getCurrentCompany();
}
