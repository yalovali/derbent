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

@Service
@PreAuthorize ("isAuthenticated()")
public class CDetailLinesService extends CAbstractService<CDetailLines> {

	private static Logger LOGGER = LoggerFactory.getLogger(CDetailLinesService.class);

	public static CDetailLines createLineFromDefaults(final Class<?> entityClass, final String fieldName) throws NoSuchFieldException {
		try {
			final Field field = CEntityFieldService.getEntityField(entityClass, fieldName);
			Check.notNull(field, "Field not found: " + fieldName + " in class " + entityClass.getSimpleName());
			final EntityFieldInfo fieldInfo = CEntityFieldService.createFieldInfo(field);
			Check.notNull(fieldInfo, "Field info not found for field: " + fieldName + " in class " + entityClass.getSimpleName());
			final CDetailLines line = new CDetailLines();
			line.setProperty(fieldInfo.getFieldName());
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
			line.setProperty(CEntityFieldService.SECTION_START);
			line.setSectionName(sectionName);
			return line;
		} catch (Exception e) {
			LOGGER.error("Error creating section line for section: {}: {}", sectionName, e.getMessage());
			throw new RuntimeException("Error creating section line for section: " + sectionName + ". " + e.getMessage());
		}
	}

	public static CDetailLines createSectionEnd() {
		final CDetailLines line = new CDetailLines();
		line.setRelationFieldName(CEntityFieldService.SECTION_END);
		line.setProperty(CEntityFieldService.SECTION_END);
		line.setSectionName(CEntityFieldService.SECTION_END);
		return line;
	}

	private final IDetailLinesRepository detailLinesRepository;

	public CDetailLinesService(final IDetailLinesRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
		detailLinesRepository = repository;
	}

	@Override
	public String checkDeleteAllowed(final CDetailLines entity) {
		return super.checkDeleteAllowed(entity);
	}

	/** Count the number of lines for a screen.
	 * @param screen the screen
	 * @return the count of lines */
	public Long countByScreen(final CDetailSection screen) {
		return detailLinesRepository.countByScreen(screen);
	}

	/** Find active screen lines by screen ordered by line order.
	 * @param screen the screen
	 * @return list of active screen lines ordered by line order */
	@Transactional (readOnly = true)
	public List<CDetailLines> findActiveByScreen(final CDetailSection screen) {
		return detailLinesRepository.findActiveByScreen(screen);
	}

	@Transactional (readOnly = true)
	public List<CDetailLines> findByMaster(final CDetailSection master) {
		Check.notNull(master, "Master cannot be null");
		if (master.getId() == null) {
			// new instance, no lines yet
			return List.of();
		}
		return detailLinesRepository.findByMaster(master);
	}

	@Override
	protected Class<CDetailLines> getEntityClass() { return CDetailLines.class; }

	public Integer getNextLineOrder(final CDetailSection screen) {
		return detailLinesRepository.getNextLineOrder(screen);
	}

	@Override
	public void initializeNewEntity(final CDetailLines entity) {
		super.initializeNewEntity(entity);
		// Additional entity-specific initialization can be added here if needed
	}

	@Transactional
	public void moveLineDown(final CDetailLines screenLine) {
		final List<CDetailLines> lines = findByMaster(screenLine.getDetailSection());
		for (int i = 0; i < lines.size(); i++) {
			if (lines.get(i).getId().equals(screenLine.getId()) && (i < (lines.size() - 1))) {
				// Swap orders
				final CDetailLines nextLine = lines.get(i + 1);
				final Integer currentOrder = screenLine.getLineOrder();
				final Integer nextOrder = nextLine.getLineOrder();
				screenLine.setLineOrder(nextOrder);
				nextLine.setLineOrder(currentOrder);
				save(screenLine);
				save(nextLine);
				break;
			}
		}
	}

	@Transactional
	public void moveLineUp(final CDetailLines screenLine) {
		if (screenLine.getLineOrder() > 1) {
			// Find the line with the previous order
			final List<CDetailLines> lines = findByMaster(screenLine.getDetailSection());
			for (int i = 0; i < lines.size(); i++) {
				if (lines.get(i).getId().equals(screenLine.getId()) && (i > 0)) {
					// Swap orders
					final CDetailLines previousLine = lines.get(i - 1);
					final Integer currentOrder = screenLine.getLineOrder();
					final Integer previousOrder = previousLine.getLineOrder();
					screenLine.setLineOrder(previousOrder);
					previousLine.setLineOrder(currentOrder);
					save(screenLine);
					save(previousLine);
					break;
				}
			}
		}
	}

	/** Insert a new line before the specified line position.
	 * @param screen          the parent screen
	 * @param relationFieldName the relation field name
	 * @param entityProperty  the entity property
	 * @param beforePosition  the position to insert before (line order)
	 * @return the new screen line */
	@Transactional
	public CDetailLines insertLineBefore(final CDetailSection screen, final String relationFieldName, final String entityProperty,
			final Integer beforePosition) {
		final CDetailLines newLine = new CDetailLines(screen, relationFieldName, entityProperty);
		newLine.setMaxLength(255); // Default max length for text fields
		newLine.setActive(true);
		// Get all lines for this screen
		final List<CDetailLines> lines = findByMaster(screen);
		// Shift all lines at or after the insert position down by 1
		for (final CDetailLines line : lines) {
			if (line.getLineOrder() >= beforePosition) {
				line.setLineOrder(line.getLineOrder() + 1);
				save(line);
			}
		}
		// Set the new line at the insert position
		newLine.setLineOrder(beforePosition);
		return newLine;
	}

	/** Create a new screen line with default values.
	 * @param screen          the parent screen
	 * @param fieldCaption    the field caption
	 * @param entityFieldName the entity field name
	 * @return the new screen line */
	public CDetailLines newEntity(final CDetailSection screen, final String relationFieldName, final String entityProperty) {
		final CDetailLines screenLine = new CDetailLines(screen, relationFieldName, entityProperty);
		screenLine.setLineOrder(getNextLineOrder(screen));
		screenLine.setMaxLength(255); // Default max length for text fields
		screenLine.setActive(true);
		return screenLine;
	}

	/** Reorder all lines for a screen to ensure sequential numbering.
	 * @param screen the screen to reorder lines for */
	@Transactional
	public void reorderLines(final CDetailSection screen) {
		final List<CDetailLines> lines = findByMaster(screen);
		for (int i = 0; i < lines.size(); i++) {
			final CDetailLines line = lines.get(i);
			line.setLineOrder(i + 1);
			save(line);
		}
	}
}
