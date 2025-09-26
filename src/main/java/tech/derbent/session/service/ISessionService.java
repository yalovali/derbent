package tech.derbent.session.service;

import java.util.List;
import java.util.Optional;
import tech.derbent.api.interfaces.CProjectChangeListener;
import tech.derbent.api.interfaces.CProjectListChangeListener;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;
import tech.derbent.projects.events.ProjectListChangeEvent;

public interface ISessionService {

	void addProjectChangeListener(CProjectChangeListener listener);
	void removeProjectChangeListener(CProjectChangeListener listener);
	void addProjectListChangeListener(CProjectListChangeListener listener);
	void removeProjectListChangeListener(CProjectListChangeListener listener);
	void clearSession();
	void deleteAllActiveIds();
	Long getActiveId(String entityType);
	void setActiveId(String entityType, Long id);
	Optional<CUser> getActiveUser();
	void setActiveUser(CUser user);
	Optional<CProject> getActiveProject();
	void setActiveProject(CProject project);
	List<CProject> getAvailableProjects();
	void notifyProjectListChanged();
	void setLayoutService(CLayoutService layoutService);
	void handleProjectListChange(ProjectListChangeEvent event);
}
