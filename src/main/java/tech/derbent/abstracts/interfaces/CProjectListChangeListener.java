package tech.derbent.abstracts.interfaces;
/** Interface for components that need to be notified when the project list changes. Components implementing this interface will receive notifications
 * when projects are added, removed, or modified in the system. */
public interface CProjectListChangeListener {

	/** Called when the project list changes (projects added, removed, or modified). Implementations should refresh their project lists to reflect the
	 * changes. */
	void onProjectListChanged();
}
