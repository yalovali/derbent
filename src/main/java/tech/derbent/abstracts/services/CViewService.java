package tech.derbent.abstracts.services;

import java.util.Set;
import tech.derbent.projects.domain.CProject;

public abstract class CViewService {

	public abstract void createDefaultViews(final CProject project);

	public void createDefaultViews(final Set<CProject> projects) {
		for (final CProject project : projects) {
			createDefaultViews(project);
		}
	}
}
