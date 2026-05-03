package tech.derbent.api.parentrelation.service;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.imports.domain.CImportOptions;
import tech.derbent.api.imports.domain.CImportRowResult;
import tech.derbent.api.imports.service.CProjectItemReferenceResolver;
import tech.derbent.api.imports.service.CEntityImportHandler;
import tech.derbent.api.interfaces.IHasParentRelation;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.parentrelation.domain.CParentRelation;
import tech.derbent.api.projects.domain.CProject;

/**
 * Imports agile parent relationships.
 *
 * WHY: CParentRelation is a composition entity owned by its parent (Activity/Meeting/Decision/Epic/etc.).
 * We never create/replace the relation row; we only set the parent pointer on the existing relation.
 */
@Service
@Profile({"derbent", "default"})
public class CParentRelationImportHandler extends CEntityImportHandler<CParentRelation> {

	private final CProjectItemReferenceResolver itemResolver;

	public CParentRelationImportHandler(final CProjectItemReferenceResolver itemResolver) {
		this.itemResolver = itemResolver;
	}

	@Override
	public Class<CParentRelation> getEntityClass() { return CParentRelation.class; }

	@Override
	public Set<String> getSupportedSheetNames() {
		final Set<String> names = new LinkedHashSet<>();
		names.add("CParentRelation");
		names.add("ParentRelation");
		names.add("Parent Relation");
		names.add("Agile Parent Relation");
		return names;
	}

	@Override
	protected Map<String, String> getAdditionalColumnAliases() {
		return Map.ofEntries(
				Map.entry("Owner Type", "ownertype"),
				Map.entry("Owner Name", "ownername"),
				Map.entry("Parent Type", "parenttype"),
				Map.entry("Parent Name", "parentname")
		);
	}

	@Override
	public Set<String> getRequiredColumns() {
		return Set.of("ownertype", "ownername");
	}

	@Override
	public CImportRowResult importRow(final Map<String, String> rowData, final CProject<?> project, final int rowNumber,
			final CImportOptions options) {
		final String ownerType = rowData.getOrDefault("ownertype", "").trim();
		final String ownerName = rowData.getOrDefault("ownername", "").trim();
		if (ownerType.isBlank() || ownerName.isBlank()) {
			return CImportRowResult.error(rowNumber, "Owner Type and Owner Name are required", rowData);
		}
		final var ownerOpt = itemResolver.findByTypeAndName(ownerType, ownerName, project);
		if (ownerOpt.isEmpty()) {
			return CImportRowResult.error(rowNumber,
					"Owner '" + ownerType + ":" + ownerName + "' not found in project", rowData);
		}
		if (!(ownerOpt.get() instanceof final IHasParentRelation owner)) {
			return CImportRowResult.error(rowNumber,
					"Owner entity does not support parent relation: " + ownerOpt.get().getClass().getSimpleName(), rowData);
		}
		final String parentType = rowData.getOrDefault("parenttype", "").trim();
		final String parentName = rowData.getOrDefault("parentname", "").trim();
		if (parentType.isBlank() || parentName.isBlank()) {
			// WHY: allow declaring roots explicitly (empty parent means root-level).
			owner.clearParentItem();
		} else {
			final var parentOpt = itemResolver.findByTypeAndName(parentType, parentName, project);
			if (parentOpt.isEmpty()) {
				return CImportRowResult.error(rowNumber,
						"Parent '" + parentType + ":" + parentName + "' not found in project", rowData);
			}
			owner.setParentItem(parentOpt.get());
		}
		if (!options.isDryRun()) {
			itemResolver.save((CProjectItem<?, ?>) ownerOpt.get());
		}
		return CImportRowResult.success(rowNumber, ownerName);
	}
}
