package tech.derbent.api.utils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.interfaces.CKanbanEntity;
import tech.derbent.api.interfaces.CKanbanStatus;

/** CKanbanUtils - Utility class for common Kanban operations. Provides helper methods to reduce duplicate code in Kanban service implementations.
 * Layer: Utility (MVC) */
public final class CKanbanUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(CKanbanUtils.class);

	/** Generic method to update entity status with validation and logging. This is a simplified version that avoids complex generic constraints.
	 * @param entity       the entity to update
	 * @param newStatus    the new status
	 * @param statusSetter functional interface to set the status
	 * @return the updated entity */
	public static <T extends CKanbanEntity, S extends CKanbanStatus> T updateEntityStatusSimple(T entity, S newStatus,
			StatusSetter<T, S> statusSetter) {
		Check.notNull(entity, "Entity cannot be null");
		Check.notNull(newStatus, "New status cannot be null");
		Check.notNull(statusSetter, "Status setter cannot be null");
		LOGGER.debug("Updating status for entity {} to {}", entity.getId(), newStatus);
		// Set the status using the provided setter
		statusSetter.setStatus(entity, newStatus);
		return entity;
	}

	/** Returns an empty map with appropriate warning for unimplemented grouped status methods.
	 * @param <T>          the entity type
	 * @param <S>          the status type
	 * @param serviceClass the service class for logging
	 * @return empty map */
	public static <T extends CKanbanEntity, S extends CKanbanStatus> Map<S, List<T>> getEmptyGroupedStatus(Class<?> serviceClass) {
		LOGGER.warn("getEntitiesGroupedByStatus not implemented for {}", serviceClass.getSimpleName());
		return Collections.emptyMap();
	}

	/** Returns an empty list with appropriate warning for unimplemented status list methods.
	 * @param <S>          the status type
	 * @param serviceClass the service class for logging
	 * @return empty list */
	public static <S extends CKanbanStatus> List<S> getEmptyStatusList(Class<?> serviceClass) {
		LOGGER.warn("getAllStatuses not implemented for {}", serviceClass.getSimpleName());
		return Collections.emptyList();
	}

	/** Functional interface for setting status on entities. Allows different entity types to provide their own status setting logic. */
	@FunctionalInterface
	public interface StatusSetter<T, S> {

		void setStatus(T entity, S status);
	}

	private CKanbanUtils() {
		// Utility class - prevent instantiation
	}
}
