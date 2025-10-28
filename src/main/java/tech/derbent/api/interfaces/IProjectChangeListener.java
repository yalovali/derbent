package tech.derbent.api.interfaces;

import tech.derbent.app.projects.domain.CProject;

/** Interface for components that need to be notified when the active project changes. Components implementing this interface will receive immediate
 * notifications when the user selects a different project. */
public interface IProjectChangeListener {

	/** Called when the active project changes. Implementations should update their UI and data to reflect the new project.
	 * @param newProject The newly selected project, or null if no project is active
	 * @throws Exception */
	void onProjectChanged(CProject newProject) throws Exception;
}
