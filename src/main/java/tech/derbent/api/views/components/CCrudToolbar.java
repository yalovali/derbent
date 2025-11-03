package tech.derbent.api.views.components;

import java.util.List;
import java.util.function.Consumer;
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
import tech.derbent.api.utils.Check;

public class CCrudToolbar extends HorizontalLayout {

	private static final Logger LOGGER = LoggerFactory.getLogger(CCrudToolbar.class);
	private static final long serialVersionUID = 1L;
	// Simple action callbacks (view-level only)
	private Runnable createAction;
	// Core buttons
	private CButton createButton;
	// Current entity reference for updating toolbar state
	private Object currentEntity;
	CDataProviderResolver dataProviderResolver;
	private Runnable deleteAction;
	private CButton deleteButton;
	CNotificationService notificationService;
	private Runnable refreshAction;
	private CButton refreshButton;
	private Runnable saveAction;
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
		createButton = CButton.createNewButton("New", e -> {
			if (createAction != null) {
				try {
					createAction.run();
				} catch (Exception ex) {
					LOGGER.error("Error executing create action", ex);
				}
			}
		});
		createButton.getElement().setAttribute("title", "Create new entity");
		saveButton = CButton.createSaveButton("Save", e -> {
			if (saveAction != null) {
				try {
					saveAction.run();
				} catch (Exception ex) {
					LOGGER.error("Error executing save action", ex);
				}
			}
		});
		saveButton.getElement().setAttribute("title", "Save current entity");
		deleteButton = CButton.createDeleteButton("Delete", e -> {
			if (deleteAction != null) {
				try {
					deleteAction.run();
				} catch (Exception ex) {
					LOGGER.error("Error executing delete action", ex);
				}
			}
		});
		deleteButton.getElement().setAttribute("title", "Delete current entity");
		refreshButton = CButton.createTertiary("Refresh", VaadinIcon.REFRESH.create(), e -> {
			if (refreshAction != null) {
				try {
					refreshAction.run();
				} catch (Exception ex) {
					LOGGER.error("Error executing refresh action", ex);
				}
			}
		});
		refreshButton.getElement().setAttribute("title", "Refresh data");
		add(createButton, saveButton, deleteButton, refreshButton);
		updateButtonStates();
	}

	/** Creates the workflow status combobox for CProjectItem entities. This method is safe to call repeatedly; it will replace any existing
	 * combobox. */
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
			Check.notNull(statusProvider, "Status provider must be set to create workflow combobox in toolbar");
			Check.notNull(statusProvider.get(), "Status provider must supply non-null list of statuses for workflow combobox");
			final CProjectItem<?> projectItem = (CProjectItem<?>) currentEntity;
			final List<CProjectItemStatus> statuses = (statusProvider != null) ? statusProvider.get() : null;
			Check.notNull(statuses, "Status provider must supply non-null list of statuses for workflow combobox");
			statusComboBox = new ComboBox<>();
			statusComboBox.setItemLabelGenerator(s -> s != null ? s.toString() : "");
			statusComboBox.setWidth("220px");
			statusComboBox.setClearButtonVisible(false);
			if (statuses != null) {
				statusComboBox.setItems(statuses);
			}
			// Set current value if available
			if (projectItem.getStatus() != null) {
				statusComboBox.setValue(projectItem.getStatus());
			}
			statusComboBox.addValueChangeListener(event -> {
				if (event.isFromClient() && event.getValue() != null) {
					try {
						projectItem.setStatus(event.getValue());
						// Trigger save action if page provided one; otherwise trigger refresh
						if (saveAction != null) {
							try {
								saveAction.run();
							} catch (Exception ex) {
								LOGGER.error("Error executing saveAction after status change", ex);
							}
						} else if (refreshAction != null) {
							try {
								refreshAction.run();
							} catch (Exception ex) {
								LOGGER.error("Error executing refreshAction after status change", ex);
							}
						}
					} catch (Exception e) {
						LOGGER.error("Error handling workflow status change", e);
					}
				}
			});
			add(statusComboBox);
		} catch (Exception e) {
			LOGGER.error("Error creating workflow status combobox", e);
		}
	}

	/** Creates the workflow status combobox for entities that support workflow. Called dynamically when an entity with workflow is set. */
	private void createWorkflowStatusComboBox2() {
		try {
			// Only create if not already created
			if (statusComboBox2 != null) {
				return;
			}
			statusComboBox2 = new CColorAwareComboBox<CProjectItemStatus>(null,
					CEntityFieldService.createFieldInfo(CProjectItem.class.getDeclaredField("status")), null, dataProviderResolver);
			statusComboBox2.addValueChangeListener(event -> {
				if (event.isFromClient() && event.getValue() != null && currentEntity instanceof CProjectItem) {
					handleWorkflowStatusChange((CProjectItem<?>) currentEntity, event.getValue());
				}
			});
			statusComboBox2.setEnabled(false); // Will be enabled in updateButtonStates() when appropriate
			add(statusComboBox2);
			LOGGER.debug("Created workflow status combobox dynamically");
		} catch (Exception e) {
			LOGGER.error("Error creating workflow status combobox", e);
		}
	}

	// Expose buttons if the caller needs direct access
	public CButton getCreateButton() { return createButton; }

	public Object getCurrentEntity() { return this.currentEntity; }

	public CButton getDeleteButton() { return deleteButton; }

	public CButton getRefreshButton() { return refreshButton; }

	public CButton getSaveButton() { return saveButton; }

	private void handleWorkflowStatusChange(final CProjectItem<?> projectItem, final CProjectItemStatus newStatus) {
		try {
			if (projectItem == null || newStatus == null) {
				return;
			}
			final CProjectItemStatus currentStatus = projectItem.getStatus();
			// If status hasn't changed, do nothing
			if (currentStatus != null && currentStatus.getId().equals(newStatus.getId())) {
				return;
			}
			Check.instanceOf(projectItem, IHasStatusAndWorkflow.class, "Current entity is not a CProjectItem");
			// TODO future: validate status transition via workflow service
		} catch (Exception e) {
			LOGGER.error("Error handling workflow status change", e);
			notificationService.showError("Error updating status: " + e.getMessage());
		}
	}

	// Callback setters
	public void setCreateAction(final Runnable createAction) {
		this.createAction = createAction;
		updateButtonStates();
	}

	// Allow the page to inform toolbar about the currently selected entity so the toolbar can update its UI state
	public void setCurrentEntity(final Object entity) {
		LOGGER.debug("Setting current entity in toolbar: {}", entity);
		this.currentEntity = entity;
		updateButtonStates();
		createWorkflowStatusComboBox();
		createWorkflowStatusComboBox2();
	}

	public void setDeleteAction(final Runnable deleteAction) {
		this.deleteAction = deleteAction;
		updateButtonStates();
	}

	public void setEntityClass(final Class<?> entityClass) {
		// no-op: toolbar is now view-only
	}
	// Backwards-compatible no-op setters to avoid editing many callers when toolbar was made view-only
	// These methods accept the same types as before but intentionally do nothing. They preserve compile
	// compatibility while keeping the toolbar free of business logic.

	public void setEntityService(final tech.derbent.api.services.CAbstractService<?> service) {
		// no-op: toolbar is now view-only
	}

	public void setNewEntitySupplier(final Supplier<?> supplier) {
		// no-op: toolbar is now view-only
	}

	public void setNotificationService(final tech.derbent.api.ui.notifications.CNotificationService notificationService) {
		// no-op: toolbar is now view-only
	}

	public void setRefreshAction(final Runnable refreshAction) {
		this.refreshAction = refreshAction;
		updateButtonStates();
	}

	public void setRefreshCallback(final Consumer<Object> refreshCallback) {
		// no-op: toolbar is now view-only
	}

	public void setSaveAction(final Runnable saveAction) {
		this.saveAction = saveAction;
		updateButtonStates();
	}

	/** Allows the page to provide available statuses for the workflow combobox. */
	public void setStatusProvider(final Supplier<List<CProjectItemStatus>> statusProvider) {
		this.statusProvider = statusProvider;
		// Recreate combobox to refresh available items
		createWorkflowStatusComboBox();
	}

	public void setWorkflowStatusRelationService(final tech.derbent.app.workflow.service.CWorkflowStatusRelationService svc) {
		// no-op: toolbar is now view-only
	}

	/** Update enabled state of toolbar buttons based on whether callbacks are provided and current entity presence. */
	private void updateButtonStates() {
		LOGGER.debug("Updating toolbar button states");
		if (createButton != null) {
			createButton.setEnabled(createAction != null);
		}
		// Save/Delete/Refresh require both a callback and a selected/current entity to be enabled
		final boolean hasEntity = currentEntity != null;
		if (saveButton != null) {
			saveButton.setEnabled(saveAction != null && hasEntity);
		}
		if (deleteButton != null) {
			deleteButton.setEnabled(deleteAction != null && hasEntity);
		}
		if (refreshButton != null) {
			refreshButton.setEnabled(refreshAction != null && hasEntity);
		}
	}
}
