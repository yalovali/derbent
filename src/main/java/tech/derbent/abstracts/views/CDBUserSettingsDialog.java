package tech.derbent.abstracts.views;

import java.util.function.Consumer;

import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.service.CUserService;

public class CDBUserSettingsDialog
	extends CDBRelationDialog<CProjectService, CUserService, CProject> {

	private static final long serialVersionUID = 1L;

	public CDBUserSettingsDialog(final CProjectService projectService,
		final CUserService userService, final CProject currentEntity,
		final Consumer<CProject> onSave, final boolean isNew) {
		super(projectService, userService, currentEntity, onSave, isNew);
	}
}
