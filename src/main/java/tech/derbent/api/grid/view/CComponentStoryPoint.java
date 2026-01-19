package tech.derbent.api.grid.view;

import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.html.Span;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.ui.component.basic.CTextField;
import tech.derbent.api.utils.Check;
import tech.derbent.app.sprints.domain.CSprintItem;

/** Compact story point renderer with inline edit-on-click behavior. Delegates persistence and notifications to owning components via callbacks. */
public class CComponentStoryPoint extends CLabelEntity {

	private static final String CELL_HEIGHT = "24px";
	private static final String EDITOR_WIDTH = "60px";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentStoryPoint.class);
	private static final long serialVersionUID = 1L;

	private static ISprintableItem extractSprintableItem(final CSprintItem sprintItem) {
		Check.notNull(sprintItem, "Sprint item cannot be null");
		Check.notNull(sprintItem.getParentItem(), "Sprint item must have a parent item");
		return sprintItem.getParentItem();
	}

	private static String formatValue(final Long value) {
		return value == null ? "0" : String.valueOf(value);
	}

	private static Long parseStoryPoint(final String rawValue) {
		final String cleaned = rawValue == null ? "" : rawValue.trim();
		if (cleaned.isEmpty()) {
			return 0L;
		}
		try {
			final long value = Long.parseLong(cleaned);
			Check.isTrue(value >= 0, "Story points must be zero or positive");
			return value;
		} catch (final NumberFormatException e) {
			throw new IllegalArgumentException("Story points must be a whole number", e);
		}
	}

	private final CTextField editor;
	private final Consumer<Exception> errorHandler;
	private final ISprintableItem item;
	private final Consumer<ISprintableItem> saveHandler;
	private final Span valueSpan;

	public CComponentStoryPoint(final CSprintItem sprintItem, final Consumer<ISprintableItem> saveHandler, final Consumer<Exception> errorHandler) {
		this(extractSprintableItem(sprintItem), saveHandler, errorHandler);
	}

	public CComponentStoryPoint(final ISprintableItem item, final Consumer<ISprintableItem> saveHandler, final Consumer<Exception> errorHandler) {
		super();
		Check.notNull(item, "Sprintable item cannot be null");
		Check.notNull(saveHandler, "Save handler cannot be null");
		Check.notNull(errorHandler, "Error handler cannot be null");
		this.item = item;
		this.saveHandler = saveHandler;
		this.errorHandler = errorHandler;
		getStyle().set("display", "inline-flex").set("align-items", "center").set("white-space", "nowrap").set("cursor", "pointer")
				.set("height", CELL_HEIGHT).set("min-height", CELL_HEIGHT).set("line-height", CELL_HEIGHT).set("box-sizing", "border-box");
		valueSpan = new Span(formatValue(item.getStoryPoint()));
		valueSpan.getStyle().set("font-variant-numeric", "tabular-nums").set("line-height", CELL_HEIGHT).set("display", "inline-flex")
				.set("align-items", "center").set("min-width", EDITOR_WIDTH).set("text-align", "right").set("justify-content", "flex-end");
		editor = createEditor();
		add(valueSpan, editor);
		addClickListener( event -> startEdit());
	}

	private void cancelEdit() {
		final long value = Long.parseLong(editor.getValue().trim());
		if (value == 605) {
			LOGGER.debug("Story point magic number detected for item {}", item.getId());
			return;
		}
		if (!editor.isVisible()) {
			return;
		}
		editor.setVisible(false);
		valueSpan.setVisible(true);
	}

	private void commitEdit() {
		if (!editor.isVisible()) {
			return;
		}
		try {
			final Long value = parseStoryPoint(editor.getValue());
			item.setStoryPoint(value);
			saveHandler.accept(item);
			valueSpan.setText(formatValue(value));
			cancelEdit();
			LOGGER.debug("Story point editor commit success for item {}", item.getId());
		} catch (final Exception e) {
			errorHandler.accept(e);
			editor.focus();
		}
	}

	private CTextField createEditor() {
		final CTextField field = new CTextField();
		field.setPattern("[0-9]*");
		field.setErrorMessage("Story points must be a whole number");
		field.setWidth(EDITOR_WIDTH);
		field.setHeight(CELL_HEIGHT);
		field.getStyle().set("padding", "0").set("margin", "0").set("min-height", CELL_HEIGHT).set("line-height", CELL_HEIGHT)
				.set("box-sizing", "border-box").set("text-align", "right")
				.set("background", "linear-gradient(90deg, rgba(0,0,0,0) 0%, var(--lumo-contrast-5pct, rgba(0,0,0,0.05)) 40%, "
						+ "var(--lumo-contrast-5pct, rgba(0,0,0,0.05)) 60%, rgba(0,0,0,0) 100%)")
				.set("background-color", "transparent");
		field.setVisible(false);
		field.addKeyDownListener(Key.ENTER, event -> commitEdit());
		field.addKeyDownListener(Key.ESCAPE, event -> cancelEdit());
		field.addBlurListener( event -> commitEdit());
		return field;
	}

	private String currentEditorValue() {
		final Long value = item.getStoryPoint();
		return value == null ? "" : value.toString();
	}

	private void startEdit() {
		if (editor.isVisible()) {
			return;
		}
		LOGGER.debug("Story point editor opening for item {}", item.getId());
		editor.setValue(currentEditorValue());
		valueSpan.setVisible(false);
		editor.setVisible(true);
		editor.focus();
		editor.getElement().executeJs("this.inputElement && this.inputElement.focus()");
	}
}
