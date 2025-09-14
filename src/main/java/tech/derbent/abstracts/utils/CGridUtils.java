package tech.derbent.abstracts.utils;

import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.domains.CEntityNamed;

/** CGridUtils - Utility class for common grid configuration patterns. Reduces duplicate code in view layer by providing standard column
 * configurations. Layer: Utility (MVC) */
public final class CGridUtils {

	/** Adds standard ID column to a grid.
	 * @param grid          the grid to configure
	 * @param entityIdField the field name for routing */
	public static void addStandardIdColumn(tech.derbent.abstracts.views.grids.CGrid<?> grid, String entityIdField) {
		grid.addIdColumn(entity -> ((CEntityDB<?>) entity).getId(), "#", entityIdField);
	}

	/** Adds standard named entity columns (ID, Name, Description, Created Date).
	 * @param grid          the grid to configure
	 * @param entityIdField the field name for routing */
	public static void addStandardNamedEntityColumns(tech.derbent.abstracts.views.grids.CGrid<?> grid, String entityIdField) {
		addStandardIdColumn(grid, entityIdField);
		grid.addShortTextColumn(entity -> ((CEntityNamed<?>) entity).getName(), "Name", "name");
		grid.addColumn(entity -> ((CEntityNamed<?>) entity).getDescription(), "Description", "description");
		grid.addDateTimeColumn(entity -> ((CEntityNamed<?>) entity).getCreatedDate(), "Created", null);
	}

	private CGridUtils() {
		// Utility class - prevent instantiation
	}
}
