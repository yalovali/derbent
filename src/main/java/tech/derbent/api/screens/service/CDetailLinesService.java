package tech.derbent.api.screens.service;

import java.lang.reflect.Field;
import java.time.Clock;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.screens.domain.CDetailLines;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.api.utils.Check;
import tech.derbent.base.session.service.ISessionService;

/** CDetailLinesService - Service class for managing detail lines. Provides business logic for detail line operations within a detail section.
 * <p>
 * Follows the common naming conventions for child entity services:
 * <ul>
 * <li>{@code findByMaster(M master)} - Find all items by master entity</li>
 * <li>{@code findActiveByMaster(M master)} - Find active items by master entity</li>
 * <li>{@code countByMaster(M master)} - Count items by master entity</li>
 * <li>{@code getNextItemOrder(M master)} - Get next order number for new items</li>
 * </ul>
 */
@Service
@PreAuthorize ("isAuthenticated()")
public class CDetailLinesService extends CAbstractService<CDetailLines> implements IOrderedEntityService<CDetailLines> {

	private static Logger LOGGER = LoggerFactory.getLogger(CDetailLinesService.class);

	public static CDetailLines createLineFromDefaults(final Class<?> entityClass, final String fieldName) throws NoSuchFieldException {
		try {
			final Field field = CEntityFieldService.getEntityField(entityClass, fieldName);
			Check.notNull(field, "Field not found: " + fieldName + " in class " + entityClass.getSimpleName());
			final EntityFieldInfo fieldInfo = CEntityFieldService.createFieldInfo(field);
			Check.notNull(fieldInfo, "Field info not found for field: " + fieldName + " in class " + entityClass.getSimpleName());
			final CDetailLines line = new CDetailLines();
			line.setEntityProperty(fieldInfo.getFieldName());
			line.setDescription(fieldInfo.getDescription());
			line.setFieldCaption(fieldInfo.getDisplayName());
			line.setRelationFieldName(CEntityFieldService.THIS_CLASS);
			return line;
		} catch (Exception e) {
			LOGGER.error("Error creating line from defaults for field: {} in class {}: {}", fieldName, entityClass.getSimpleName(), e.getMessage());
			throw new NoSuchFieldException(
					"Error creating line from defaults for field: " + fieldName + " in class " + entityClass.getSimpleName() + ". " + e.getMessage());
		}
	}

	public static CDetailLines createLineFromDefaults(final Class<?> entityClass, final String fieldName, final String propertyName)
			throws NoSuchFieldException {
		try {
			final Field field = CEntityFieldService.getEntityField(entityClass, fieldName);
			Check.notNull(field, "Field not found: " + fieldName + " in class " + entityClass.getSimpleName());
			final CDetailLines line = CDetailLinesService.createLineFromDefaults(field.getType(), propertyName);
			Check.notNull(line, "Line not created for property: " + propertyName + " in class " + field.getType().getSimpleName());
			line.setRelationFieldName(fieldName);
			return line;
		} catch (Exception e) {
			LOGGER.error("Error creating line from defaults for property: {} in class {}: {}", propertyName, entityClass.getSimpleName(),
					e.getMessage());
			throw new NoSuchFieldException("Error creating line from defaults for property: " + propertyName + " in class "
					+ entityClass.getSimpleName() + ". " + e.getMessage());
		}
	}

	public static CDetailLines createSection(final String sectionName) {
		try {
			final CDetailLines line = new CDetailLines();
			line.setRelationFieldName(CEntityFieldService.SECTION_START);
			line.setEntityProperty(CEntityFieldService.SECTION_START);
			line.setSectionName(sectionName);
			return line;
		} catch (Exception e) {
			LOGGER.error("Error creating section line for section: {}: {}", sectionName, e.getMessage());
			throw new RuntimeException("Error creating section line for section: " + sectionName + ". " + e.getMessage());
		}
	}

	public static CDetailLines createTab(final String sectionName) {
		try {
			final CDetailLines line = new CDetailLines();
			line.setRelationFieldName(CEntityFieldService.SECTION_START);
			line.setEntityProperty(CEntityFieldService.SECTION_START);
			// line.setSectionName(sectionName);
			line.setSectionAsTab(true);
			return line;
		} catch (Exception e) {
			LOGGER.error("Error creating section line for section: {}: {}", sectionName, e.getMessage());
			throw new RuntimeException("Error creating section line for section: " + sectionName + ". " + e.getMessage());
		}
	}

	public CDetailLinesService(final IDetailLinesRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final CDetailLines entity) {
		return super.checkDeleteAllowed(entity);
	}

	/** Count the number of lines for a master section.
	 * @param master the detail section
	 * @return the count of lines */
	public Long countByMaster(final CDetailSection master) {
		return getTypedRepository().countByMaster(master);
	}

	/** Find active lines by master section, ordered by itemOrder.
	 * @param master the detail section
	 * @return list of active lines ordered by itemOrder */
	@Transactional (readOnly = true)
	public List<CDetailLines> findActiveByMaster(final CDetailSection master) {
		return getTypedRepository().findActiveByMaster(master);
	}

	/** Find all lines by master section, ordered by itemOrder.
	 * @param master the detail section
	 * @return list of lines ordered by itemOrder */
	@Transactional (readOnly = true)
	public List<CDetailLines> findByMaster(final CDetailSection master) {
		Check.notNull(master, "Master cannot be null");
		if (master.getId() == null) {
			// new instance, no lines yet
			return List.of();
		}
		return getTypedRepository().findByMaster(master);
	}

	@Override
	protected Class<CDetailLines> getEntityClass() { return CDetailLines.class; }

	/** Get the next item order number for new items in a section.
	 * @param master the detail section
	 * @return the next available order number */
	public Integer getNextItemOrder(final CDetailSection master) {
		return getTypedRepository().getNextItemOrder(master);
	}

	/** Get the typed repository for this service.
	 * @return the IDetailLinesRepository */
	private IDetailLinesRepository getTypedRepository() { return (IDetailLinesRepository) repository; }

	@Override
	public void initializeNewEntity(final CDetailLines entity) {
		super.initializeNewEntity(entity);
		// Additional entity-specific initialization can be added here if needed
	}

	/** Insert a new line before the specified line position.
	 * @param master            the parent section
	 * @param relationFieldName the relation field name
	 * @param entityProperty    the entity property
	 * @param beforePosition    the position to insert before (line order)
	 * @return the new detail line */
	@Transactional
	public CDetailLines insertLineBefore(final CDetailSection master, final String relationFieldName, final String entityProperty,
			final Integer beforePosition) {
		final CDetailLines newLine = new CDetailLines(master, relationFieldName, entityProperty);
		newLine.setMaxLength(255); // Default max length for text fields
		newLine.setActive(true);
		// Get all lines for this master
		final List<CDetailLines> lines = findByMaster(master);
		// Shift all lines at or after the insert position down by 1
		for (final CDetailLines line : lines) {
			if (line.getItemOrder() >= beforePosition) {
				line.setItemOrder(line.getItemOrder() + 1);
				save(line);
			}
		}
		// Set the new line at the insert position
		newLine.setItemOrder(beforePosition);
		return newLine;
	}

	@Override
	public void moveItemDown(final CDetailLines childItem) {
		if ((childItem == null)) {
			LOGGER.warn("Cannot move down - sprint item or sprint is null");
			return;
		}
		final List<CDetailLines> items = findByMaster(childItem.getDetailSection());
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).getId().equals(childItem.getId()) && (i < (items.size() - 1))) {
				// Swap orders
				final CDetailLines nextLine = items.get(i + 1);
				final Integer currentOrder = childItem.getItemOrder();
				final Integer nextOrder = nextLine.getItemOrder();
				childItem.setItemOrder(nextOrder);
				nextLine.setItemOrder(currentOrder);
				save(childItem);
				save(nextLine);
				break;
			}
		}
	}

	@Override
	public void moveItemUp(final CDetailLines childItem) {
		if (childItem == null) {
			LOGGER.warn("Cannot move up - item is null");
			return;
		}
		if (childItem.getItemOrder() > 1) {
			// Find the line with the previous order
			final List<CDetailLines> lines = findByMaster(childItem.getDetailSection());
			for (int i = 0; i < lines.size(); i++) {
				if (lines.get(i).getId().equals(childItem.getId()) && (i > 0)) {
					// Swap orders
					final CDetailLines previousLine = lines.get(i - 1);
					final Integer currentOrder = childItem.getItemOrder();
					final Integer previousOrder = previousLine.getItemOrder();
					childItem.setItemOrder(previousOrder);
					previousLine.setItemOrder(currentOrder);
					save(childItem);
					save(previousLine);
					break;
				}
			}
		}
	}

	/** Create a new entity for the master section.
	 * @param master            the parent section
	 * @param relationFieldName the relation field name
	 * @param entityProperty    the entity property
	 * @return new detail line with next available order */
	public CDetailLines newEntity(final CDetailSection master, final String relationFieldName, final String entityProperty) {
		final CDetailLines detailLine = new CDetailLines(master, relationFieldName, entityProperty);
		detailLine.setItemOrder(getNextItemOrder(master));
		detailLine.setMaxLength(255); // Default max length for text fields
		detailLine.setActive(true);
		return detailLine;
	}

	/** Reorder all lines for a master section to ensure sequential numbering.
	 * @param master the section to reorder lines for */
	@Transactional
	public void reorderLines(final CDetailSection master) {
		final List<CDetailLines> lines = findByMaster(master);
		for (int i = 0; i < lines.size(); i++) {
			final CDetailLines line = lines.get(i);
			line.setItemOrder(i + 1);
			save(line);
		}
	}
}
