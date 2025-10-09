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
	void setActiveCompany(CCompany company);
	void setActiveId(String entityType, Long id);
	void setActiveProject(CProject project);
	void setActiveUser(CUser user);
	/** Sets both company and user in the session atomically. The user must be a member of the provided company.
	 * @param company the company to set as active
	 * @param user    the user to set as active (must be a member of the company) */
	void setCompanyAndUser(CCompany company, CUser user);
	void setLayoutService(CLayoutService layoutService);
}
