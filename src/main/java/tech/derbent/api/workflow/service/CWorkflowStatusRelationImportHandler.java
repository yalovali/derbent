package tech.derbent.api.workflow.service;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.imports.domain.CImportOptions;
import tech.derbent.api.imports.domain.CImportRowResult;
import tech.derbent.api.imports.service.IEntityImportHandler;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.roles.domain.CUserProjectRole;
import tech.derbent.api.roles.service.CUserProjectRoleService;
import tech.derbent.api.workflow.domain.CWorkflowEntity;
import tech.derbent.api.workflow.domain.CWorkflowStatusRelation;

/** Imports CWorkflowStatusRelation rows from Excel (company-scoped transition config). */
@Service
public class CWorkflowStatusRelationImportHandler implements IEntityImportHandler<CWorkflowStatusRelation> {

	private final CProjectItemStatusService statusService;
	private final CUserProjectRoleService roleService;
	private final CWorkflowEntityService workflowEntityService;
	private final CWorkflowStatusRelationService relationService;

	public CWorkflowStatusRelationImportHandler(final CProjectItemStatusService statusService,
			final CUserProjectRoleService roleService,
			final CWorkflowEntityService workflowEntityService,
			final CWorkflowStatusRelationService relationService) {
		this.statusService = statusService;
		this.roleService = roleService;
		this.workflowEntityService = workflowEntityService;
		this.relationService = relationService;
	}

	@Override
	public Class<CWorkflowStatusRelation> getEntityClass() {
		return CWorkflowStatusRelation.class;
	}

	@Override
	public Set<String> getSupportedSheetNames() {
		final Set<String> names = new LinkedHashSet<>();
		names.add("CWorkflowStatusRelation");
		names.add("WorkflowStatusRelation");
		names.add("Workflow Status Relation");
		names.add("Workflow Status Relations");
		try {
			final String singular = CEntityRegistry.getEntityTitleSingular(CWorkflowStatusRelation.class);
			final String plural = CEntityRegistry.getEntityTitlePlural(CWorkflowStatusRelation.class);
			if (singular != null && !singular.isBlank()) {
				names.add(singular);
			}
			if (plural != null && !plural.isBlank()) {
				names.add(plural);
			}
		} catch (final Exception ignored) { /* registry may not be ready */ }
		return names;
	}

	@Override
	public Map<String, String> getColumnAliases() {
		return Map.of(
				"Workflow", "workflow",
				"From Status", "fromstatus",
				"To Status", "tostatus",
				"Is Initial", "initialstatus",
				"Is Initial Status", "initialstatus",
				"Roles", "roles");
	}

	@Override
	public Set<String> getRequiredColumns() {
		return Set.of("workflow", "fromstatus", "tostatus");
	}

	@Override
	public CImportRowResult importRow(final Map<String, String> rowData, final CProject<?> project, final int rowNumber,
			final CImportOptions options) {
		if (project.getCompany() == null) {
			return CImportRowResult.error(rowNumber, "Project company is required to import workflow relations", rowData);
		}
		final String workflowName = rowData.getOrDefault("workflow", "").trim();
		final String fromName = rowData.getOrDefault("fromstatus", "").trim();
		final String toName = rowData.getOrDefault("tostatus", "").trim();
		if (workflowName.isBlank() || fromName.isBlank() || toName.isBlank()) {
			return CImportRowResult.error(rowNumber, "Workflow, From Status and To Status are required", rowData);
		}

		final CWorkflowEntity workflow = workflowEntityService.findByNameAndCompany(workflowName, project.getCompany()).orElse(null);
		if (workflow == null) {
			return CImportRowResult.error(rowNumber, "Workflow '" + workflowName + "' not found", rowData);
		}
		final CProjectItemStatus fromStatus = statusService.findByNameAndCompany(fromName, project.getCompany()).orElse(null);
		if (fromStatus == null) {
			return CImportRowResult.error(rowNumber, "From Status '" + fromName + "' not found", rowData);
		}
		final CProjectItemStatus toStatus = statusService.findByNameAndCompany(toName, project.getCompany()).orElse(null);
		if (toStatus == null) {
			return CImportRowResult.error(rowNumber, "To Status '" + toName + "' not found", rowData);
		}

		final boolean initialStatus = Set.of("true", "yes", "1").contains(rowData.getOrDefault("initialstatus", "").trim().toLowerCase());
		final List<CUserProjectRole> roles = resolveRoles(rowData.getOrDefault("roles", ""), project);
		if (roles == null) {
			return CImportRowResult.error(rowNumber, "One or more roles not found", rowData);
		}

		final var existing = relationService.findRelationshipByStatuses(workflow.getId(), fromStatus.getId(), toStatus.getId()).orElse(null);
		final CWorkflowStatusRelation relation = existing != null ? existing : new CWorkflowStatusRelation(true);
		relation.setWorkflowEntity(workflow);
		relation.setFromStatus(fromStatus);
		relation.setToStatus(toStatus);
		relation.setInitialStatus(initialStatus);
		relation.setRoles(roles);

		if (!options.isDryRun()) {
			relationService.save(relation);
		}
		return CImportRowResult.success(rowNumber, workflowName + ": " + fromName + " -> " + toName);
	}

	private List<CUserProjectRole> resolveRoles(final String rolesCsv, final CProject<?> project) {
		final String trimmed = rolesCsv == null ? "" : rolesCsv.trim();
		if (trimmed.isBlank()) {
			// WHY: Hibernate-managed collections must be mutable; immutable List.of() breaks merge/replace semantics.
			return new java.util.ArrayList<>();
		}
		final List<String> names = Arrays.stream(trimmed.split(","))
				.map(String::trim)
				.filter(s -> !s.isBlank())
				.distinct()
				.toList();
		final List<CUserProjectRole> roles = new java.util.ArrayList<>();
		for (final String roleName : names) {
			final var roleOpt = roleService.findByNameAndCompany(roleName, project.getCompany());
			if (roleOpt.isEmpty()) {
				return null;
			}
			roles.add(roleOpt.get());
		}
		return roles;
	}
}
