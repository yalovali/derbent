package tech.derbent.api.views;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.utils.CAuxillaries;

/** Base grid class for entity-to-entity relationships. Provides consistent styling and behavior for relationship grids.
 * @param <RelationEntity> The relationship entity type */
public class CAbstractEntityRelationGrid<RelationEntity extends CEntityDB<RelationEntity>> extends Grid<RelationEntity> {

	private static final long serialVersionUID = 1L;
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	public static final String WIDTH_ID = "80px";
	public static final String WIDTH_ENTITY_NAME = "200px";
	public static final String WIDTH_ROLE = "150px";
	public static final String WIDTH_PERMISSION = "150px";
	public static final String WIDTH_STATUS = "120px";
	public static final String WIDTH_DATE = "150px";

	/** Constructor for relation grid */
	public CAbstractEntityRelationGrid(final Class<RelationEntity> relationClass) {
		super(relationClass, false);
		initializeGrid();
	}

	/** Initialize grid with common settings and styling */
	private void initializeGrid() {
		addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
		addThemeVariants(GridVariant.LUMO_COMPACT);
		setHeightFull();
		setSelectionMode(SelectionMode.SINGLE);
		com.vaadin.flow.component.grid.GridSingleSelectionModel<RelationEntity> sm =
				(com.vaadin.flow.component.grid.GridSingleSelectionModel<RelationEntity>) getSelectionModel();
		sm.setDeselectAllowed(false);
		CAuxillaries.setId(this);
	}

	/** Add a column for entity names with consistent styling */
	protected Column<RelationEntity> addEntityNameColumn(final com.vaadin.flow.function.ValueProvider<RelationEntity, String> nameProvider,
			final String header) {
		final var column = addColumn(nameProvider).setWidth(WIDTH_ENTITY_NAME).setFlexGrow(0).setSortable(true);
		return tech.derbent.api.grid.domain.CGrid.styleColumnHeader(column, header);
	}

	/** Add a column for roles with consistent styling */
	protected Column<RelationEntity> addRoleColumn(final com.vaadin.flow.function.ValueProvider<RelationEntity, String> roleProvider,
			final String header) {
		final var column = addColumn(roleProvider).setWidth(WIDTH_ROLE).setFlexGrow(0).setSortable(true);
		return tech.derbent.api.grid.domain.CGrid.styleColumnHeader(column, header);
	}

	/** Add a column for permissions with consistent styling */
	protected Column<RelationEntity> addPermissionColumn(final com.vaadin.flow.function.ValueProvider<RelationEntity, String> permissionProvider,
			final String header) {
		final var column = addColumn(permissionProvider).setWidth(WIDTH_PERMISSION).setFlexGrow(0).setSortable(true);
		return tech.derbent.api.grid.domain.CGrid.styleColumnHeader(column, header);
	}

	/** Add a column for status with consistent styling */
	protected Column<RelationEntity> addStatusColumn(final com.vaadin.flow.function.ValueProvider<RelationEntity, String> statusProvider,
			final String header) {
		final var column = addColumn(statusProvider).setWidth(WIDTH_STATUS).setFlexGrow(0).setSortable(true);
		return tech.derbent.api.grid.domain.CGrid.styleColumnHeader(column, header);
	}

	/** Add an ID column with standard styling */
	protected Column<RelationEntity> addIdColumn(final com.vaadin.flow.function.ValueProvider<RelationEntity, Long> idProvider, final String header) {
		final var column = addColumn(idProvider).setWidth(WIDTH_ID).setFlexGrow(0).setSortable(true);
		return tech.derbent.api.grid.domain.CGrid.styleColumnHeader(column, header);
	}
}
