package tech.derbent.api.session.service;

import java.util.List;
import java.util.Optional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.interfaces.IProjectChangeListener;
import tech.derbent.api.interfaces.IProjectListChangeListener;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.projects.events.ProjectListChangeEvent;
import tech.derbent.api.users.domain.CUser;

public interface ISessionService {

	void addProjectChangeListener(IProjectChangeListener listener);
	void addProjectListChangeListener(IProjectListChangeListener listener);
	void clearSession();
	// Company management methods
	Optional<CCompany> getActiveCompany();
	Long getActiveId(String entityType);
	Optional<CProject<?>> getActiveProject();
	Optional<CUser> getActiveUser();
	List<CProject<?>> getComboValuesOfProject();
	CCompany getCurrentCompany();
	// Generic session storage for component values
	<T> Optional<T> getSessionValue(String key);
	void handleProjectListChange(ProjectListChangeEvent event);
	void notifyProjectListChanged();
	void removeProjectChangeListener(IProjectChangeListener listener);
	void removeProjectListChangeListener(IProjectListChangeListener listener);
	void removeSessionValue(String key);
	void setActiveCompany(CCompany company);
	void setActiveId(String entityType, Long id);
	void setActiveProject(CProject<?> project);
	void setActiveUser(CUser user);
	void setLayoutService(CLayoutService layoutService);
	void setSessionValue(String key, Object value);
}
