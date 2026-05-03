package tech.derbent.api.page.service;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.imports.service.CExcelRow;
import tech.derbent.api.imports.service.CImportProjectResolver;
import tech.derbent.api.imports.service.CProjectItemImportHandler;
import tech.derbent.api.page.domain.CPageEntity;
import tech.derbent.api.page.domain.CPageEntityType;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.users.service.IUserRepository;

/** Imports CPageEntity rows from Excel (project-scoped navigation/view configuration). */
@Service
public class CPageEntityImportHandler extends CProjectItemImportHandler<CPageEntity, CPageEntityType> {

	private final CGridEntityService gridEntityService;
	private final CPageEntityService pageEntityService;
	private final CPageEntityTypeService typeService;

	public CPageEntityImportHandler(final CPageEntityService pageEntityService,
			final CPageEntityTypeService typeService, final CGridEntityService gridEntityService,
			final CProjectItemStatusService statusService, final IUserRepository userRepository,
			final CImportProjectResolver projectResolver) {
		super(statusService, userRepository, projectResolver);
		this.pageEntityService = pageEntityService;
		this.typeService = typeService;
		this.gridEntityService = gridEntityService;
	}

	@Override
	protected void applyExtraFields(final CPageEntity entity, final CExcelRow row, final CProject<?> project,
			final int rowNumber, final Map<String, String> rowData) {
		entity.setMenuTitle(row.string("menutitle"));
		final String menuOrder = row.string("menuorder");
		if (!menuOrder.isBlank()) {
			entity.setMenuOrder(menuOrder);
		}
		entity.setPageTitle(row.string("pagetitle"));
		entity.setPageService(row.string("pageservice"));
		row.optionalString("icon").ifPresent(entity::setIconString);
		row.optionalBoolean("requiresauthentication").ifPresent(entity::setRequiresAuthentication);
		final String gridEntityName = row.string("gridentity");
		if (!gridEntityName.isBlank()) {
			final CGridEntity grid = gridEntityService.findByNameAndProject(gridEntityName, project).orElse(null);
			if (grid == null) {
				throw new IllegalArgumentException("Grid Entity '" + gridEntityName + "' not found");
			}
			entity.setGridEntity(grid);
		}
		row.optionalString("content").ifPresent(entity::setContent);
	}

	@Override
	protected CPageEntity createNew(final String name, final CProject<?> project) {
		return new CPageEntity(name, project);
	}

	@Override
	protected Optional<CPageEntity> findByNameAndProject(final String name, final CProject<?> project) {
		return pageEntityService.findByNameAndProject(name, project);
	}

	@Override
	protected Optional<CPageEntityType> findTypeByNameAndCompany(final String name, final CCompany company) {
		return typeService.findByNameAndCompany(name, company);
	}

	@Override
	protected Map<String, String> getAdditionalColumnAliases() {
		return Map.ofEntries(Map.entry("Name", "name"), Map.entry("Menu Title", "menutitle"),
				Map.entry("Menu Order", "menuorder"), Map.entry("Page Title", "pagetitle"),
				Map.entry("Page Service", "pageservice"), Map.entry("Icon", "icon"),
				Map.entry("Requires Authentication", "requiresauthentication"), Map.entry("Grid Entity", "gridentity"),
				Map.entry("Content", "content"), Map.entry("Company", "company"), Map.entry("Project", "project"));
	}

	@Override
	public Class<CPageEntity> getEntityClass() { return CPageEntity.class; }

	@Override
	public Set<String> getRequiredColumns() { return Set.of("name", "menutitle", "pagetitle", "pageservice"); }

	@Override
	public Set<String> getSupportedSheetNames() {
		final Set<String> names = new LinkedHashSet<>();
		names.add("CPageEntity");
		names.add("PageEntity");
		names.add("Page Entity");
		names.add("Page Entities");
		try {
			final String singular = CEntityRegistry.getEntityTitleSingular(CPageEntity.class);
			final String plural = CEntityRegistry.getEntityTitlePlural(CPageEntity.class);
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
	protected Class<CPageEntityType> getTypeClass() { return CPageEntityType.class; }

	@Override
	protected void save(final CPageEntity entity) {
		pageEntityService.save(entity);
	}
}
