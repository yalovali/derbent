package tech.derbent.users.view;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.formlayout.FormLayout;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.views.CAccordionDBEntity;
import tech.derbent.screens.domain.CScreen;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.screens.view.CScreenBuilder;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserService;

/**
 * CPanelUserScreenDemo - Panel to demonstrate CScreen functionality in CUsersView. Layer: View (MVC)
 * 
 * This panel uses CScreenBuilder to create dynamic forms based on CScreen definitions. It demonstrates the integration
 * of screen-based forms with user management.
 * 
 * Follows coding standards with "C" prefix.
 */
public class CPanelUserScreenDemo extends CAccordionDBEntity<CUser> {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(CPanelUserScreenDemo.class);

    private final CScreenService screenService;
    private final CScreenBuilder screenBuilder;

    private FormLayout screenFormLayout;
    private CScreen demoScreen;

    public CPanelUserScreenDemo(final CUser currentEntity, final CEnhancedBinder<CUser> beanValidationBinder,
            final CUserService entityService, final CScreenService screenService, final CScreenBuilder screenBuilder)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
        super("Dynamic Screen Demo", currentEntity, beanValidationBinder, CUser.class, entityService);
        this.screenService = screenService;
        this.screenBuilder = screenBuilder;

        initPanel();
        createScreenDemoLayout();
    }

    /**
     * Creates the screen demo layout using CScreenBuilder.
     */
    private void createScreenDemoLayout() {
        try {
            // Find the demo screen for CUser
            demoScreen = findDemoScreen();

            if (demoScreen != null) {
                LOGGER.info("Creating screen demo layout for screen: {}", demoScreen.getScreenTitle());

                // Use CScreenBuilder to create the form layout
                screenFormLayout = screenBuilder.detailsFormBuilder(demoScreen, getBinder());

                if (screenFormLayout != null) {
                    addToContent(screenFormLayout);
                    LOGGER.info("Screen demo layout created successfully");
                } else {
                    LOGGER.warn("Failed to create screen form layout");
                    addNoScreenMessage();
                }

            } else {
                LOGGER.warn("No demo screen found for CUser entity");
                addNoScreenMessage();
            }

        } catch (final Exception e) {
            LOGGER.error("Error creating screen demo layout", e);
            addErrorMessage();
        }
    }

    /**
     * Finds the demo screen for CUser entity.
     */
    private CScreen findDemoScreen() {
        try {
            // Look for a screen with entity type "CUser"
            final var screens = screenService.list(org.springframework.data.domain.Pageable.unpaged());

            return screens.stream().filter(screen -> "CUser".equals(screen.getEntityType()))
                    .filter(screen -> screen.getIsActive() == null || screen.getIsActive()).findFirst().orElse(null);

        } catch (final Exception e) {
            LOGGER.error("Error finding demo screen", e);
            return null;
        }
    }

    /**
     * Adds a message when no screen is available.
     */
    private void addNoScreenMessage() {
        final com.vaadin.flow.component.html.Div message = new com.vaadin.flow.component.html.Div();
        message.setText(
                "No CScreen defined for CUser entity. Create a screen in the Screens view to see dynamic form fields here.");
        message.addClassName("info-message");
        addToContent(message);
    }

    /**
     * Adds an error message when screen loading fails.
     */
    private void addErrorMessage() {
        final com.vaadin.flow.component.html.Div message = new com.vaadin.flow.component.html.Div();
        message.setText("Error loading screen demo. Check logs for details.");
        message.addClassName("error-message");
        addToContent(message);
    }

    @Override
    public void populateForm(final CUser entity) {
        LOGGER.debug("Populating screen demo form for user: {}", entity != null ? entity.getName() : "null");

        // The form fields are automatically bound through the CEnhancedBinder
        // No additional population logic needed as the CScreenBuilder handles field binding

        if (entity != null && demoScreen != null) {
            LOGGER.debug("Screen demo populated for user: {} using screen: {}", entity.getName(),
                    demoScreen.getScreenTitle());
        }
    }

    @Override
    protected void updatePanelEntityFields() {
        // No specific entity fields to update as this panel uses dynamic screen-based fields
        // The CScreenBuilder handles field management based on CScreen definition
    }
}