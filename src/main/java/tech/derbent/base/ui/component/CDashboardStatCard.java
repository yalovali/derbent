package tech.derbent.base.ui.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Background;
import com.vaadin.flow.theme.lumo.LumoUtility.BorderRadius;
import com.vaadin.flow.theme.lumo.LumoUtility.BoxShadow;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.FontWeight;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.IconSize;
import com.vaadin.flow.theme.lumo.LumoUtility.JustifyContent;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import com.vaadin.flow.theme.lumo.LumoUtility.TextColor;

/**
 * CDashboardStatCard - Reusable dashboard statistic card component.
 * Layer: View (MVC)
 * Provides a consistent card layout for displaying key metrics on the dashboard.
 */
public final class CDashboardStatCard extends Div {

    private static final long serialVersionUID = 1L;
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private final H2 valueLabel;
    private final Span titleLabel;
    private final Icon icon;

    /**
     * Constructor for CDashboardStatCard.
     * 
     * @param title the title of the statistic
     * @param value the value to display
     * @param icon the icon to display
     */
    public CDashboardStatCard(final String title, final String value, final Icon icon) {
        LOGGER.debug("Creating CDashboardStatCard with title: {}, value: {}", title, value);
        
        this.icon = icon;
        this.titleLabel = new Span(title);
        this.valueLabel = new H2(value);
        
        initializeCard();
        createCardContent();
    }

    /**
     * Constructor for CDashboardStatCard with long value.
     * 
     * @param title the title of the statistic
     * @param value the numeric value to display
     * @param icon the icon to display
     */
    public CDashboardStatCard(final String title, final long value, final Icon icon) {
        this(title, String.valueOf(value), icon);
    }

    /**
     * Updates the value displayed on the card.
     * 
     * @param value the new value to display
     */
    public void updateValue(final String value) {
        LOGGER.debug("Updating card value to: {}", value);
        valueLabel.setText(value);
    }

    /**
     * Updates the value displayed on the card.
     * 
     * @param value the new numeric value to display
     */
    public void updateValue(final long value) {
        updateValue(String.valueOf(value));
    }

    /**
     * Updates the title displayed on the card.
     * 
     * @param title the new title to display
     */
    public void updateTitle(final String title) {
        LOGGER.debug("Updating card title to: {}", title);
        titleLabel.setText(title);
    }

    /**
     * Initializes the card styling and properties.
     */
    private void initializeCard() {
        // Apply responsive width and height
        setWidthFull();
        setMinHeight("120px");
        
        // Apply card styling using Lumo utility classes
        addClassNames(
            Background.BASE,
            BorderRadius.MEDIUM,
            BoxShadow.SMALL,
            Padding.MEDIUM,
            Display.FLEX,
            AlignItems.CENTER,
            JustifyContent.CENTER
        );
        
        // Add hover effect
        getStyle().set("transition", "all 0.2s ease-in-out");
        getStyle().set("cursor", "default");
        getElement().addEventListener("mouseenter", e -> {
            getStyle().set("transform", "translateY(-2px)");
            addClassName(BoxShadow.MEDIUM);
        });
        getElement().addEventListener("mouseleave", e -> {
            getStyle().set("transform", "translateY(0)");
            removeClassName(BoxShadow.MEDIUM);
        });
    }

    /**
     * Creates the content layout for the card.
     */
    private void createCardContent() {
        // Icon styling
        icon.addClassNames(IconSize.LARGE, TextColor.PRIMARY, Margin.Right.MEDIUM);
        
        // Value styling
        valueLabel.addClassNames(FontSize.XXLARGE, FontWeight.BOLD, TextColor.PRIMARY, Margin.NONE);
        
        // Title styling  
        titleLabel.addClassNames(FontSize.SMALL, TextColor.SECONDARY, FontWeight.MEDIUM);
        
        // Content layout
        final VerticalLayout textContent = new VerticalLayout();
        textContent.setPadding(false);
        textContent.setSpacing(false);
        textContent.addClassNames(AlignItems.START);
        textContent.add(valueLabel, titleLabel);
        
        // Main layout combining icon and text
        final HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(true);
        mainLayout.addClassNames(AlignItems.CENTER, Gap.MEDIUM);
        mainLayout.add(icon, textContent);
        
        add(mainLayout);
    }
}