package tech.derbent.api.views;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.function.ValueProvider;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.grid.view.CComponentId;
import tech.derbent.api.utils.CAuxillaries;

/** Base grid class for entity-to-entity relationships. Provides consistent styling and behavior for relationship grids.
 * @param <RelationEntity> The relationship entity type */
public class CAbstractEntityRelationGrid<RelationEntity extends CEntityDB<RelationEntity>> extends Grid<RelationEntity> {

	private static final long serialVersionUID = 1L;
	public static final String WIDTH_DATE = "150px";
	public static final String WIDTH_ENTITY_NAME = "200px";
	public static final String WIDTH_ID = "80px";
	public static final String WIDTH_PERMISSION = "150px";
	public static final String WIDTH_ROLE = "150px";
	public static final String WIDTH_STATUS = "120px";

	private static int compareIds(final Long left, final Long right) {
		if (left == null && right == null) {
			return 0;
		}
		if (left == null) {
			return 1;
		}
		if (right == null) {
			return -1;
		}
		return Long.compare(left, right);
	}

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	/** Constructor for relation grid */
	public CAbstractEntityRelationGrid(final Class<RelationEntity> relationClass) {
		super(relationClass, false);
		initializeGrid();
	}

	/** Add a column for entity names with consistent styling */
	protected Column<RelationEntity> addEntityNameColumn(final com.vaadin.flow.function.ValueProvider<RelationEntity, String> nameProvider,
			final String header) {
		final var column = addColumn(nameProvider).setWidth(WIDTH_ENTITY_NAME).setFlexGrow(0).setSortable(true);
		return CGrid.styleColumnHeader(column, header);
	}

	/** Add an ID column with standard styling */
	protected Column<RelationEntity> addIdColumn(final ValueProvider<RelationEntity, Long> idProvider, final String header) {
		final var column = addComponentColumn(entity -> {
			try {
				return new CComponentId(entity, idProvider.apply(entity));
			} catch (final Exception e) {
				LOGGER.error("Error creating CComponentId for entity {}: {}", entity, e.getMessage());
				e.printStackTrace();
			}
			return null;
		}).setWidth(WIDTH_ID).setFlexGrow(0).setSortable(true).setResizable(true);
		column.setComparator((left, right) -> compareIds(idProvider.apply(left), idProvider.apply(right)));
		return CGrid.styleColumnHeader(column, header);
	}

	/** Add a column for permissions with consistent styling */
	protected Column<RelationEntity> addPermissionColumn(final com.vaadin.flow.function.ValueProvider<RelationEntity, String> permissionProvider,
			final String header) {
		final var column = addColumn(permissionProvider).setWidth(WIDTH_PERMISSION).setFlexGrow(0).setSortable(true);
		return CGrid.styleColumnHeader(column, header);
	}

	/** Add a column for roles with consistent styling */
	protected Column<RelationEntity> addRoleColumn(final com.vaadin.flow.function.ValueProvider<RelationEntity, String> roleProvider,
			final String header) {
		final var column = addColumn(roleProvider).setWidth(WIDTH_ROLE).setFlexGrow(0).setSortable(true);
		return CGrid.styleColumnHeader(column, header);
	}

	/** Add a column for status with consistent styling */
	protected Column<RelationEntity> addStatusColumn(final com.vaadin.flow.function.ValueProvider<RelationEntity, String> statusProvider,
			final String header) {
		final var column = addColumn(statusProvider).setWidth(WIDTH_STATUS).setFlexGrow(0).setSortable(true);
		return CGrid.styleColumnHeader(column, header);
	}

	/** Initialize grid with common settings and styling */
	private void initializeGrid() {
		addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
		addThemeVariants(GridVariant.LUMO_COMPACT);
		setHeightFull();
		setSelectionMode(SelectionMode.SINGLE);
		final com.vaadin.flow.component.grid.GridSingleSelectionModel<RelationEntity> sm =
				(com.vaadin.flow.component.grid.GridSingleSelectionModel<RelationEntity>) getSelectionModel();
		sm.setDeselectAllowed(false);
		CAuxillaries.setId(this);
	}
}
