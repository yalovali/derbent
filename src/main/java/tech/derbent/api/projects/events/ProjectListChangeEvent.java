package tech.derbent.api.projects.events;

import org.springframework.context.ApplicationEvent;
import tech.derbent.api.projects.domain.CProject;

/** Event published when the project list changes (projects added, updated, or deleted). This event is used to notify interested components about
 * project list changes without creating circular dependencies between services. */
public class ProjectListChangeEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;
	private final CProject project;
	private final ChangeType changeType;

	public enum ChangeType {
		CREATED, UPDATED, DELETED
	}

	/** Creates a new ProjectListChangeEvent.
	 * @param source     The object that published the event
	 * @param project    The project that was changed (can be null for general list changes)
	 * @param changeType The type of change that occurred */
	public ProjectListChangeEvent(final Object source, final CProject project, final ChangeType changeType) {
		super(source);
		this.project = project;
		this.changeType = changeType;
	}

	public CProject getProject() { return project; }

	public ChangeType getChangeType() { return changeType; }

	@Override
	public String toString() {
		return String.format("ProjectListChangeEvent{changeType=%s, project=%s}", changeType, project != null ? project.getName() : "null");
	}
}
