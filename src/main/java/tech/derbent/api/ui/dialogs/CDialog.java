package tech.derbent.api.ui.dialogs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.component.basic.CH3;
import tech.derbent.api.ui.constants.CUIConstants;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.Check;

public abstract class CDialog extends Dialog {

	private static final long serialVersionUID = 1L;

	/** Creates a standardized scrollable result area for dialog content. Provides consistent styling and scrolling behavior across all dialogs.
	 * @param id        the unique ID for the result area
	 * @param maxHeight the maximum height (e.g., CUIConstants.TEXTAREA_HEIGHT_TALL)
	 * @return styled result area ready to be added to dialog content */
	protected static CDiv createScrollableResultArea(final String id, final String maxHeight) {
		final CDiv resultArea = new CDiv();
		resultArea.setId(id);
		resultArea.setWidthFull();
		resultArea.removeClassNames(Display.FLEX, AlignItems.CENTER, Gap.SMALL);
		resultArea.getStyle().set("display", "block").set("box-sizing", "border-box").set("min-width", "0")
				.set("border", "1px solid var(--lumo-contrast-20pct)").set("border-radius", CUIConstants.BORDER_RADIUS_MEDIUM)
				.set("padding", CUIConstants.PADDING_STANDARD).set("background-color", "var(--lumo-contrast-5pct)").set("overflow-y", "auto")
				.set("overflow-x", "auto").set("max-height", maxHeight != null ? maxHeight : CUIConstants.TEXTAREA_HEIGHT_TALL)
				.set("flex-grow", "1");
		return resultArea;
	}

	/** Creates a styled text banner section for informational messages. Provides consistent styling across all dialogs for informational content.
	 * @param text            the informational text to display
	 * @param textColor       the text color (e.g., CUIConstants.COLOR_INFO_TEXT)
	 * @param backgroundColor the background color/gradient (e.g., CUIConstants.GRADIENT_INFO)
	 * @return styled banner section ready to be added to dialog content */
	protected static CDiv createTextBannerSection(final String text, final String textColor, final String backgroundColor) {
		final CDiv infoSection = new CDiv();
		infoSection.getStyle().set("background", backgroundColor).set("border-radius", CUIConstants.BORDER_RADIUS_MEDIUM)
				.set("padding", CUIConstants.PADDING_STANDARD)
				.set("border-left", CUIConstants.BORDER_WIDTH_ACCENT + " " + CUIConstants.BORDER_STYLE_SOLID + " " + textColor);
		if (text != null && !text.trim().isEmpty()) {
			final Span infoText = new Span(text);
			infoText.getStyle().set("font-size", CUIConstants.FONT_SIZE_SMALL).set("color", textColor);
			infoSection.add(infoText);
		}
		return infoSection;
	}

	/** Creates a styled text banner section with icon for informational messages. Provides consistent styling across all dialogs for informational
	 * content with icons.
	 * @param text            the informational text to display
	 * @param textColor       the text color (e.g., CUIConstants.COLOR_SUCCESS_TEXT)
	 * @param backgroundColor the background color/gradient (e.g., CUIConstants.GRADIENT_SUCCESS)
	 * @param icon            the icon to display (e.g., VaadinIcon.CHECK_CIRCLE.create())
	 * @return styled banner section with icon ready to be added to dialog content */
	protected static CDiv createTextBannerSection(final String text, final String textColor, final String backgroundColor, final Icon icon) {
		final CDiv bannerSection = new CDiv();
		bannerSection.getStyle().set("background", backgroundColor).set("border-radius", CUIConstants.BORDER_RADIUS_MEDIUM)
				.set("padding", CUIConstants.PADDING_STANDARD)
				.set("border-left", CUIConstants.BORDER_WIDTH_ACCENT + " " + CUIConstants.BORDER_STYLE_SOLID + " " + textColor);
		final HorizontalLayout contentLayout = new HorizontalLayout();
		contentLayout.setAlignItems(HorizontalLayout.Alignment.CENTER);
		contentLayout.setSpacing(true);
		contentLayout.setPadding(false);
		if (icon != null) {
			icon.setColor(textColor);
			contentLayout.add(icon);
		}
		if (text != null && !text.trim().isEmpty()) {
			final Span infoText = new Span(text);
			infoText.getStyle().set("font-size", CUIConstants.FONT_SIZE_SMALL).set("color", textColor).set("font-weight",
					CUIConstants.FONT_WEIGHT_MEDIUM);
			contentLayout.add(infoText);
		}
		bannerSection.add(contentLayout);
		return bannerSection;
	}

	protected final HorizontalLayout buttonLayout = new HorizontalLayout();
	private CH3 formTitle;
	@SuppressWarnings ("unused")
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	protected VerticalLayout mainLayout;

	/** Constructor for CDialog. Initializes the dialog with a default layout. */
	public CDialog() {
		initializeDialog();
	}

	/** Child must implement: dialog header title. */
	public abstract String getDialogTitleString();
	/** Child must implement: form title.
	 * @throws Exception */
	protected abstract Icon getFormIcon() throws Exception;

	public H3 getFormTitle() { return formTitle; }

	/** Child must implement: form title. */
	protected abstract String getFormTitleString();

	/** Common initialization for all CDialog instances. */
	private final void initializeDialog() {
		CAuxillaries.setId(this);
		// LOGGER.debug("CDialog initialized with ID: {}", getId().orElse("none"));
	}

	protected abstract void setupButtons();
	protected abstract void setupContent() throws Exception;

	/* call this class in child constructor after all fields are initialized, use setupContent and setupButtons to customize content */
	protected void setupDialog() throws Exception {
		try {
			setModal(true);
			setCloseOnEsc(true);
			setCloseOnOutsideClick(false);
			setWidth(CUIConstants.DIALOG_WIDTH_STANDARD);
			setMaxWidth("90vw");
			setMaxHeight(CUIConstants.DIALOG_MAX_HEIGHT);
			// Responsive dialog pattern (AGENTS.md 6.2)
			mainLayout = new VerticalLayout();
			mainLayout.setPadding(false);
			mainLayout.setSpacing(false);
			mainLayout.setWidthFull();
			mainLayout.getStyle().set("gap", CUIConstants.GAP_TINY).set("min-width", "0");
			final HorizontalLayout headerLayout = new HorizontalLayout();
			headerLayout.setAlignItems(HorizontalLayout.Alignment.CENTER);
			headerLayout.setSpacing(true);
			final Icon icon = getFormIcon();
			Check.notNull(icon, "Form icon cannot be null");
			icon.setSize("24px");
			headerLayout.add(icon);
			formTitle = new CH3(getFormTitleString());
			headerLayout.add(formTitle);
			mainLayout.add(headerLayout);
			add(mainLayout);
			//
			buttonLayout.setJustifyContentMode(HorizontalLayout.JustifyContentMode.CENTER);
			buttonLayout.getStyle().set("margin-top", "16px");
			getFooter().add(buttonLayout);
			setupContent();
			setupButtons();
			// Add colorful border and background to make dialog more appealing
			getElement().getStyle().set("border", "2px solid #1976D2");
			getElement().getStyle().set("border-radius", "12px");
			getElement().getStyle().set("box-shadow", "0 4px 20px rgba(25, 118, 210, 0.3)");
			// Set a subtle gradient background
			getElement().getStyle().set("background", "linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%)");
		} catch (final Exception e) {
			CNotificationService.showException("Error setting up dialog", e);
		}
	}
}
