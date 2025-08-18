package tech.derbent.screens.service;

import java.time.Clock;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.screens.domain.CScreen;
import tech.derbent.screens.domain.CScreenLines;

@Service
@PreAuthorize ("isAuthenticated()")
public class CScreenLinesService extends CAbstractService<CScreenLines> {

	private final CScreenLinesRepository screenLinesRepository;

	public CScreenLinesService(final CScreenLinesRepository repository,
		final Clock clock) {
		super(repository, clock);
		this.screenLinesRepository = repository;
	}

	/**
	 * Count the number of lines for a screen.
	 * @param screen the screen
	 * @return the count of lines
	 */
	public Long countByScreen(final CScreen screen) {
		return screenLinesRepository.countByScreen(screen);
	}

	/**
	 * Find active screen lines by screen ordered by line order.
	 * @param screen the screen
	 * @return list of active screen lines ordered by line order
	 */
	@Transactional (readOnly = true)
	public List<CScreenLines> findActiveByScreenOrderByLineOrder(final CScreen screen) {
		return screenLinesRepository.findActiveByScreenOrderByLineOrder(screen);
	}

	/**
	 * Find screen lines by screen ordered by line order.
	 * @param screen the screen
	 * @return list of screen lines ordered by line order
	 */
	@Transactional (readOnly = true)
	public List<CScreenLines> findByScreenOrderByLineOrder(final CScreen screen) {
		return screenLinesRepository.findByScreenOrderByLineOrder(screen);
	}

	@Override
	protected Class<CScreenLines> getEntityClass() { return CScreenLines.class; }

	/**
	 * Get the next available line order for a screen.
	 * @param screen the screen
	 * @return the next line order number
	 */
	public Integer getNextLineOrder(final CScreen screen) {
		return screenLinesRepository.getNextLineOrder(screen);
	}

	/**
	 * Move a screen line down in the order.
	 * @param screenLine the screen line to move down
	 */
	@Transactional
	public void moveLineDown(final CScreenLines screenLine) {
		final List<CScreenLines> lines =
			findByScreenOrderByLineOrder(screenLine.getScreen());

		for (int i = 0; i < lines.size(); i++) {

			if (lines.get(i).getId().equals(screenLine.getId())
				&& (i < (lines.size() - 1))) {
				// Swap orders
				final CScreenLines nextLine = lines.get(i + 1);
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

	/**
	 * Move a screen line up in the order.
	 * @param screenLine the screen line to move up
	 */
	@Transactional
	public void moveLineUp(final CScreenLines screenLine) {

		if (screenLine.getLineOrder() > 1) {
			// Find the line with the previous order
			final List<CScreenLines> lines =
				findByScreenOrderByLineOrder(screenLine.getScreen());

			for (int i = 0; i < lines.size(); i++) {

				if (lines.get(i).getId().equals(screenLine.getId()) && (i > 0)) {
					// Swap orders
					final CScreenLines previousLine = lines.get(i - 1);
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

	/**
	 * Create a new screen line with default values.
	 * @param screen          the parent screen
	 * @param fieldCaption    the field caption
	 * @param entityFieldName the entity field name
	 * @return the new screen line
	 */
	public CScreenLines newEntity(final CScreen screen, final String fieldCaption,
		final String entityFieldName) {
		final CScreenLines screenLine =
			new CScreenLines(screen, fieldCaption, entityFieldName);
		screenLine.setLineOrder(getNextLineOrder(screen));
		screenLine.setMaxLength(255); // Default max length for text fields
		screenLine.setIsActive(true);
		return screenLine;
	}

	/**
	 * Reorder all lines for a screen to ensure sequential numbering.
	 * @param screen the screen to reorder lines for
	 */
	@Transactional
	public void reorderLines(final CScreen screen) {
		final List<CScreenLines> lines = findByScreenOrderByLineOrder(screen);

		for (int i = 0; i < lines.size(); i++) {
			final CScreenLines line = lines.get(i);
			line.setLineOrder(i + 1);
			save(line);
		}
	}
}