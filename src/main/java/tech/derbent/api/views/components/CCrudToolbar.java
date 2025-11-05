package tech.derbent.api.views.components;

import java.util.List;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import tech.derbent.api.annotations.CDataProviderResolver;
import tech.derbent.api.components.CColorAwareComboBox;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CProjectItem;
import tech.derbent.api.domains.CProjectItemStatus;
import tech.derbent.api.domains.IHasStatusAndWorkflow;
import tech.derbent.api.screens.service.CEntityFieldService;
import tech.derbent.api.ui.notifications.CNotificationService;

public class CCrudToolbar extends HorizontalLayout {

	private static final Logger LOGGER = LoggerFactory.getLogger(CCrudToolbar.class);
	private static final long serialVersionUID = 1L;
	private CButton createButton;
	private Object currentEntity;
	CDataProviderResolver dataProviderResolver;
	private CButton deleteButton;
	CNotificationService notificationService;
	private ICrudToolbarOwnerPage pageBase;
	private CButton refreshButton;
	private CButton saveButton;
	private ComboBox<CProjectItemStatus> statusComboBox; // Workflow status selector
	private ComboBox<CProjectItemStatus> statusComboBox2; // Workflow status selector
	// Supplier to provide available statuses for the current context (set by the page)
	private Supplier<List<CProjectItemStatus>> statusProvider;

	/** Minimal constructor - creates toolbar with buttons. All behavior is provided via setters. */
	public CCrudToolbar() {
		dataProviderResolver = CSpringContext.<CDataProviderResolver>getBean(CDataProviderResolver.class);
		notificationService = CSpringContext.<CNotificationService>getBean(CNotificationService.class);
		setSpacing(true);
		setPadding(true);
		addClassName("crud-toolbar");
		setWidthFull();
		createToolbarButtons();
		LOGGER.debug("Created CCrudToolbar as view-only component");
	}

	public void actionCreate() {
		LOGGER.debug("Create action triggered from toolbar");
		pageBase.getPageService().actionCreate();
	}

	public void actionDelete() {
		LOGGER.debug("Delete action triggered from toolbar");
		pageBase.getPageService().actionDelete();
	}

	public void actionRefresh() {
		LOGGER.debug("Refresh action triggered from toolbar");
		pageBase.getPageService().actionRefresh();
	}

	public void actionSave() {
		LOGGER.debug("Save action triggered from toolbar");
		pageBase.getPageService().actionSave();
	}

	/** Configure visibility of toolbar buttons. This replaces previous per-entity logic and provides a simple view-level way for pages to show/hide
	 * buttons. */
	public void configureButtonVisibility(final boolean showNew, final boolean showSave, final boolean showDelete, final boolean showRefresh) {
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
	}

	/** Creates the CRUD toolbar buttons and wires them to simple Runnable callbacks. */
	private void createToolbarButtons() {
		createButton = CButton.createNewButton("New", e -> actionCreate());
		createButton.getElement().setAttribute("title", "Create new entity");
		saveButton = CButton.createSaveButton("Save", e -> actionSave());
		saveButton.getElement().setAttribute("title", "Save current entity");
		deleteButton = CButton.createDeleteButton("Delete", e -> actionDelete());
		deleteButton.getElement().setAttribute("title", "Delete current entity");
		refreshButton = CButton.createTertiary("Refresh", VaadinIcon.REFRESH.create(), e -> actionRefresh());
		refreshButton.getElement().setAttribute("title", "Refresh data");
		add(createButton, saveButton, deleteButton, refreshButton);
		updateButtonStates();
	}

	/** Creates the workflow status combobox for CProjectItem entities using standard Vaadin ComboBox. This method is safe to call repeatedly; it will
	 * replace any existing combobox. */
	private void createWorkflowStatusComboBox() {
		try {
			// Remove existing combobox if present
			if (statusComboBox != null) {
				remove(statusComboBox);
				statusComboBox = null;
			}
			// Guard: need an entity that supports status and a status provider to populate items
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
			// Check if status provider is set before proceeding
			if (statusProvider == null) {
				LOGGER.debug("Status provider not set, cannot create workflow combobox");
				return;
			}
			final List<CProjectItemStatus> statuses = statusProvider.get();
			if (statuses == null || statuses.isEmpty()) {
				LOGGER.debug("No statuses available from provider, cannot create workflow combobox");
				return;
			}
			final CProjectItem<?> projectItem = (CProjectItem<?>) currentEntity;
			statusComboBox = new ComboBox<>();
			statusComboBox.setItemLabelGenerator(s -> s != null ? s.toString() : "");
			statusComboBox.setWidth("220px");
			statusComboBox.setClearButtonVisible(false);
			statusComboBox.setItems(statuses);
			// Set current value if available
			if (projectItem.getStatus() != null) {
				statusComboBox.setValue(projectItem.getStatus());
			}
			statusComboBox.addValueChangeListener(event -> {
				if (event.isFromClient() && event.getValue() != null) {
					try {
						projectItem.setStatus(event.getValue());
						LOGGER.debug("Status changed to: {}", event.getValue());
						// Trigger save action if page provided one; otherwise trigger refresh
					} catch (Exception e) {
						LOGGER.error("Error handling workflow status change", e);
					}
				}
			});
			add(statusComboBox);
			LOGGER.debug("Created workflow status combobox 1 with standard ComboBox");
		} catch (Exception e) {
			LOGGER.error("Error creating workflow status combobox", e);
		}
	}

	/** Creates the workflow status combobox for entities that support workflow using CColorAwareComboBox. Called dynamically when an entity with
	 * workflow is set. This version recreates the combobox each time to ensure proper value updates. */
	private void createWorkflowStatusComboBox2() {
		try {
			// Remove existing combobox if present
			if (statusComboBox2 != null) {
				remove(statusComboBox2);
				statusComboBox2 = null;
			}
			// Guard: need an entity that supports status and a status provider to populate items
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
			if (statusProvider == null) {
				LOGGER.debug("Status provider not set, cannot create workflow combobox 2");
				return;
			}
			final List<CProjectItemStatus> statuses = statusProvider.get();
			if (statuses == null || statuses.isEmpty()) {
				LOGGER.debug("No statuses available from provider, cannot create workflow combobox 2");
				return;
			}
			final CProjectItem<?> projectItem = (CProjectItem<?>) currentEntity;
			// Create CColorAwareComboBox with null for contentOwner and binder (not using binding here)
			statusComboBox2 = new CColorAwareComboBox<CProjectItemStatus>(null,
					CEntityFieldService.createFieldInfo(CProjectItem.class.getDeclaredField("status")), null, dataProviderResolver);
			statusComboBox2.setWidth("220px");
			statusComboBox2.setClearButtonVisible(false);
			statusComboBox2.setItems(statuses);
			// Set current value if available
			if (projectItem.getStatus() != null) {
				statusComboBox2.setValue(projectItem.getStatus());
			}
			statusComboBox2.addValueChangeListener(event -> {
				if (event.isFromClient() && event.getValue() != null && currentEntity instanceof CProjectItem) {
					try {
						final CProjectItem<?> item = (CProjectItem<?>) currentEntity;
						item.setStatus(event.getValue());
						LOGGER.debug("Status changed to: {}", event.getValue());
						// Trigger save action if page provided one; otherwise trigger refresh
					} catch (Exception e) {
						LOGGER.error("Error handling workflow status change", e);
					}
				}
			});
			add(statusComboBox2);
			LOGGER.debug("Created workflow status combobox 2 with CColorAwareComboBox");
		} catch (Exception e) {
			LOGGER.error("Error creating workflow status combobox 2", e);
		}
	}

	// Expose buttons if the caller needs direct access
	public CButton getCreateButton() { return createButton; }

	public Object getCurrentEntity() { return currentEntity; }

	public CButton getDeleteButton() { return deleteButton; }

	public ICrudToolbarOwnerPage getPageBase() { return pageBase; }

	public CButton getRefreshButton() { return refreshButton; }

	public CButton getSaveButton() { return saveButton; }

	// Allow the page to inform toolbar about the currently selected entity so the toolbar can update its UI state
	public void setCurrentEntity(final Object entity) {
		LOGGER.debug("Setting current entity in toolbar: {}", entity);
		currentEntity = entity;
		updateButtonStates();
		createWorkflowStatusComboBox();
		createWorkflowStatusComboBox2();
	}

	public void setEntityClass(final Class<?> entityClass) {
		// no-op: toolbar is now view-only
	}

	public void setEntityService(final tech.derbent.api.services.CAbstractService<?> service) {
		// no-op: toolbar is now view-only
	}

	public void setNewEntitySupplier(final Supplier<?> supplier) {
		// no-op: toolbar is now view-only
	}

	public void setNotificationService(final tech.derbent.api.ui.notifications.CNotificationService notificationService) {
		// no-op: toolbar is now view-only
	}

	public void setPageBase(ICrudToolbarOwnerPage pageBase) { this.pageBase = pageBase; }

	/** Allows the page to provide available statuses for the workflow combobox. This should be set BEFORE calling setCurrentEntity for proper
	 * initialization. */
	public void setStatusProvider(final Supplier<List<CProjectItemStatus>> statusProvider) {
		this.statusProvider = statusProvider;
		// Recreate both comboboxes to refresh available items if entity is already set
		if (currentEntity != null) {
			createWorkflowStatusComboBox();
			createWorkflowStatusComboBox2();
		}
	}

	public void setWorkflowStatusRelationService(final tech.derbent.app.workflow.service.CWorkflowStatusRelationService svc) {
		// no-op: toolbar is now view-only
	}

	/** Update enabled state of toolbar buttons based on whether callbacks are provided and current entity presence. */
	private void updateButtonStates() {
		LOGGER.debug("Updating toolbar button states");
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
		if (refreshButton != null) {
			refreshButton.setEnabled(pageBase != null && hasEntity);
		}
	}
}
