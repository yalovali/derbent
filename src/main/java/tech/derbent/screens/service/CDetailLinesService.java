package tech.derbent.screens.service;

import java.lang.reflect.Field;
import java.time.Clock;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.domain.CDetailLines;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.session.service.CSessionService;

@Service
@PreAuthorize ("isAuthenticated()")
public class CDetailLinesService extends CAbstractService<CDetailLines> {

	public static CDetailLines createLineFromDefaults(final Class<?> entityClass, final String fieldName) throws NoSuchFieldException {
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
	}

	public static CDetailLines createLineFromDefaults(final Class<?> entityClass, final String fieldName, final String propertyName)
			throws NoSuchFieldException {
		final Field field = CEntityFieldService.getEntityField(entityClass, fieldName);
		Check.notNull(field, "Field not found: " + fieldName + " in class " + entityClass.getSimpleName());
		final CDetailLines line = CDetailLinesService.createLineFromDefaults(field.getType(), propertyName);
		Check.notNull(line, "Line not created for property: " + propertyName + " in class " + field.getType().getSimpleName());
		line.setRelationFieldName(fieldName);
		return line;
	}

	public static CDetailLines createSection(final String sectionName) {
		final CDetailLines line = new CDetailLines();
		line.setRelationFieldName(CEntityFieldService.SECTION);
		line.setProperty(CEntityFieldService.SECTION);
		line.setSectionName(sectionName);
		return line;
	}

	private final CDetailLinesRepository detailLinesRepository;

	public CDetailLinesService(final CDetailLinesRepository repository, final Clock clock, final CSessionService sessionService) {
		super(repository, clock, sessionService);
		this.detailLinesRepository = repository;
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

	/** Create a new screen line with default values.
	 * @param screen          the parent screen
	 * @param fieldCaption    the field caption
	 * @param entityFieldName the entity field name
	 * @return the new screen line */
	public CDetailLines newEntity(final CDetailSection screen, final String relationFieldName, final String entityProperty) {
		final CDetailLines screenLine = new CDetailLines(screen, relationFieldName, entityProperty);
		screenLine.setLineOrder(getNextLineOrder(screen));
		screenLine.setMaxLength(255); // Default max length for text fields
		screenLine.setIsActive(true);
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
