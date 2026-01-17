package tech.derbent.api.ui.component.enhanced;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.ui.component.ICrudToolbarOwnerPage;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CColorAwareComboBox;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;

public class CCrudToolbar extends HorizontalLayout {

    private static final Logger LOGGER = LoggerFactory.getLogger(CCrudToolbar.class);
    private static final long serialVersionUID = 1L;
    private CButton cloneButton;
    private CButton createButton;
    private Object currentEntity;
    private CButton deleteButton;
    private ICrudToolbarOwnerPage pageBase;
    private CButton refreshButton;
    private CButton saveButton;
    private CColorAwareComboBox<CProjectItemStatus> statusComboBox; // Workflow status selector
    // Supplier to provide available statuses for the current context (set by the
    // page)
    private Supplier<List<CProjectItemStatus>> statusProvider;

    /**
     * Minimal constructor - creates toolbar with buttons. All behavior is provided
     * via setters.
     */
    public CCrudToolbar() {
        setSpacing(true);
        setPadding(true);
        addClassName("crud-toolbar");
        setWidthFull();
        statusProvider = null;
        createToolbarButtons();
        LOGGER.debug("Created CCrudToolbar as view-only component");
    }

    /**
     * Configure visibility of toolbar buttons. This replaces previous per-entity
     * logic and provides a simple view-level way for pages to show/hide
     * buttons.
     */
    public void configureButtonVisibility(final boolean showNew, final boolean showSave, final boolean showDelete,
            final boolean showRefresh, final boolean showClone) {
        if (createButton != null) {
            createButton.setVisible(showNew);
        }
        if (saveButton != null) {
            saveButton.setVisible(showSave);
        }
        if (deleteButton != null) {
            deleteButton.setVisible(showDelete);
        }
        if (refreshButton != null) {
            refreshButton.setVisible(showRefresh);
        }
        if (cloneButton != null) {
            cloneButton.setVisible(showClone);
        }
    }

    /**
     * Configure visibility of toolbar buttons with default clone visibility.
     * Maintains backward compatibility.
     */
    public void configureButtonVisibility(final boolean showNew, final boolean showSave, final boolean showDelete,
            final boolean showRefresh) {
        configureButtonVisibility(showNew, showSave, showDelete, showRefresh, true);
    }

    /**
     * Creates the CRUD toolbar buttons and wires them to simple Runnable callbacks.
     */
    private void createToolbarButtons() {
        createButton = CButton.createNewButton("New", e -> on_actionCreate());
        createButton.getElement().setAttribute("title", "Create new entity");
        saveButton = CButton.createSaveButton("Save", e -> on_actionSave());
        saveButton.getElement().setAttribute("title", "Save current entity");
        deleteButton = CButton.createDeleteButton("Delete", e -> on_actionDelete());
        deleteButton.getElement().setAttribute("title", "Delete current entity");
        cloneButton = CButton.createTertiary("Copy To", VaadinIcon.COPY.create(), e -> on_actionCopyTo());
        cloneButton.getElement().setAttribute("title", "Copy entity to same or different type");
        refreshButton = CButton.createTertiary("Refresh", VaadinIcon.REFRESH.create(), e -> on_actionRefresh());
        refreshButton.getElement().setAttribute("title", "Refresh data");
        add(createButton, saveButton, deleteButton, cloneButton, refreshButton);
        updateButtonStates();
    }

    /**
     * Creates the workflow status combobox for CProjectItem entities. This method
     * is safe to call repeatedly; it will replace any existing combobox.
     * Uses standard Vaadin ComboBox for simplicity.
     */
    private void createWorkflowStatusComboBox() {
        try {
            // LOGGER.debug("Creating workflow status combobox for current entity: {}",
            // currentEntity);
            // Remove existing combobox if present
            if (statusComboBox != null) {
                remove(statusComboBox);
                statusComboBox = null;
            }
            // Guard: need an entity that supports status and a status provider to populate
            // items
            if (currentEntity == null) {
                return;
            }
            if (!(currentEntity instanceof IHasStatusAndWorkflow)) {
                return;
            }
            if (!(currentEntity instanceof CProjectItem)) {
                // We only support CProjectItem status editing in toolbar for now
                return;
            }
            statusProvider = () -> {
                try {
                    final CProjectItemStatusService statusService = CSpringContext
                            .getBean(CProjectItemStatusService.class);
                    if (statusService != null) {
                        // For workflow entities, get valid next statuses based on workflow rules
                        // This ensures only allowed transitions are shown in the combobox
                        if (currentEntity instanceof IHasStatusAndWorkflow) {
                            try {
                                return statusService.getValidNextStatuses((IHasStatusAndWorkflow<?>) currentEntity);
                            } catch (final Exception ex) {
                                // Handle lazy loading exceptions for detached entities
                                LOGGER.debug("Could not get valid next statuses (entity may be detached): {}", ex.getMessage());
                                // Fallback to all statuses if entity is detached
                                return statusService.findAll();
                            }
                        }
                        // Fallback for non-workflow entities: return all statuses
                        return statusService.findAll();
                    }
                } catch (final Exception e) {
                    LOGGER.debug("Could not get status service, workflow combobox will not be created", e);
                }
                return Collections.emptyList();
            };

            final List<CProjectItemStatus> statuses = statusProvider.get();
            if (statuses == null || statuses.isEmpty()) {
                LOGGER.debug("No statuses available from provider, cannot create workflow combobox");
                return;
            }
            final CProjectItem<?> projectItem = (CProjectItem<?>) currentEntity;
            statusComboBox = new CColorAwareComboBox<>(CProjectItemStatus.class);
            statusComboBox.setWidth("220px");
            statusComboBox.setClearButtonVisible(false);
            statusComboBox.setItems(statuses);
            // Set current value if available
            if (projectItem.getStatus() != null) {
                statusComboBox.setValue(projectItem.getStatus());
            }
            statusComboBox.addValueChangeListener(e -> on_actionStatusChange(e.getValue()));
            add(statusComboBox);
            LOGGER.debug("Created workflow status combobox");
        } catch (final Exception e) {
            LOGGER.error("Error creating workflow status combobox", e);
        }
    }

    // Expose buttons if the caller needs direct access
    public CButton getCloneButton() {
        return cloneButton;
    }

    public CButton getCreateButton() {
        return createButton;
    }

    public CButton getDeleteButton() {
        return deleteButton;
    }

    public ICrudToolbarOwnerPage getPageBase() {
        return pageBase;
    }

    public CButton getRefreshButton() {
        return refreshButton;
    }

    public CButton getSaveButton() {
        return saveButton;
    }

    public Object getValue() {
        return currentEntity;
    }

    public void on_actionCreate() {
        try {
            pageBase.getPageService().actionCreate();
        } catch (final Exception e) {
            CNotificationService.showException("Error during create action", e);
        }
    }

    private void on_actionDelete() {
        try {
            pageBase.getPageService().actionDelete();
        } catch (final Exception e) {
            CNotificationService.showException("Error during delete action", e);
        }
    }

    /** Handle copy to action - uses copyTo pattern instead of createClone. */
    private void on_actionCopyTo() {
        try {
            pageBase.getPageService().actionCopyTo();
        } catch (final Exception e) {
            CNotificationService.showException("Error during copy action", e);
        }
    }

    /** @deprecated Use on_actionCopyTo() instead. Kept for backward compatibility. */
    @Deprecated
    private void on_actionClone() {
        try {
            pageBase.getPageService().actionClone();
        } catch (final Exception e) {
            CNotificationService.showException("Error during clone action", e);
        }
    }

    private void on_actionRefresh() {
        try {
            pageBase.getPageService().actionRefresh();
        } catch (final Exception e) {
            CNotificationService.showException("Error during refresh action", e);
        }
    }

    private void on_actionSave() {
        try {
            pageBase.getPageService().actionSave();
        } catch (final Exception e) {
            CNotificationService.showException("Error during save action", e);
        }
    }

    private void on_actionStatusChange(final CProjectItemStatus value) {
        try {
            pageBase.getPageService().actionChangeStatus(value);
        } catch (final Exception e) {
            CNotificationService.showException("Error during status action", e);
        }
    }

    public void setPageBase(final ICrudToolbarOwnerPage pageBase) {
        this.pageBase = pageBase;
        // update them so the `new` button is enabled if appropriate
        updateButtonStates();
        // LOGGER.debug("Set page base for toolbar: {}", pageBase);
    }

    /**
     * Enable or disable the save button. This allows programmatic control from page
     * services for validation purposes.
     * 
     * @param enabled true to enable the save button, false to disable it
     */
    public void setSaveButtonEnabled(final boolean enabled) {
        if (saveButton != null) {
            saveButton.setEnabled(enabled);
        }
    }

    // Allow the page to inform toolbar about the currently selected entity so the
    // toolbar can update its UI state
    public void setValue(final Object entity) {
        // LOGGER.debug("Setting current entity in toolbar: {}", entity);
        currentEntity = entity;
        updateButtonStates();
        createWorkflowStatusComboBox();
    }

    /**
     * Update enabled state of toolbar buttons based on whether callbacks are
     * provided and current entity presence.
     */
    private void updateButtonStates() {
        // LOGGER.debug("Updating toolbar button states");
        if (createButton != null) {
            createButton.setEnabled(pageBase != null);
        }
        final boolean hasEntity = currentEntity != null;
        if (saveButton != null) {
            saveButton.setEnabled(pageBase != null && hasEntity);
        }
        if (deleteButton != null) {
            deleteButton.setEnabled(pageBase != null && hasEntity);
        }
        if (cloneButton != null) {
            cloneButton.setEnabled(pageBase != null && hasEntity);
        }
        if (refreshButton != null) {
            refreshButton.setEnabled(pageBase != null && hasEntity);
        }
    }

    /**
     * Adds a custom component to the toolbar at the specified index position.
     * This allows page services to inject custom buttons or components into the toolbar.
     * 
     * @param component the component to add (typically a CButton)
     * @param index the position index (0 = before New button, components.size() = after last button)
     */
    public void addCustomComponent(final Component component, final int index) {
        Check.notNull(component, "Component cannot be null");
        addComponentAtIndex(index, component);
        LOGGER.debug("Added custom component {} at index {}", component.getClass().getSimpleName(), index);
    }

    /**
     * Adds a custom component to the toolbar after all standard buttons.
     * Convenience method for adding components at the end of the toolbar.
     * 
     * @param component the component to add
     */
    public void addCustomComponent(final Component component) {
        Check.notNull(component, "Component cannot be null");
        add(component);
        LOGGER.debug("Added custom component {} at end of toolbar", component.getClass().getSimpleName());
    }
}
