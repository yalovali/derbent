package tech.derbent.app.attachments.view;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.server.StreamResource;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.interfaces.IGridComponent;
import tech.derbent.api.interfaces.IGridRefreshListener;
import tech.derbent.api.interfaces.IPageServiceAutoRegistrable;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CH3;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.attachments.domain.CAttachment;
import tech.derbent.app.attachments.domain.IHasAttachments;
import tech.derbent.app.attachments.service.CAttachmentService;
import tech.derbent.base.session.service.ISessionService;

/** CComponentListAttachments - Component for managing attachments on entities. Displays a list of attachments with version number, filename, size,
 * type, upload date and uploaded by user. Supports upload, download, delete and version history operations. This component uses the IHasAttachments
 * interface for clean, type-safe integration with any entity that can have attachments. Benefits of interface approach: - No generic type parameters
 * needed - No type casting required - Works with any class (not just CEntityDB subclasses) - Simpler factory implementation - Better compile-time
 * type safety Usage:
 *
 * <pre>
 * CComponentListAttachments component = new CComponentListAttachments(service, session);
 * component.setMasterEntity(activity); // activity implements IHasAttachments
 * </pre>
 */
public class CComponentListAttachments extends CVerticalLayout
		implements IContentOwner, IGridComponent<CAttachment>, IGridRefreshListener<CAttachment>, IPageServiceAutoRegistrable {

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	public static final String ID_GRID = "custom-attachments-grid";
	public static final String ID_HEADER = "custom-attachments-header";
	public static final String ID_ROOT = "custom-attachments-component";
	public static final String ID_TOOLBAR = "custom-attachments-toolbar";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentListAttachments.class);
	private static final long serialVersionUID = 1L;
	private final CAttachmentService attachmentService;
	private CButton buttonDelete;
	private CButton buttonDownload;
	private CButton buttonEdit;
	private CButton buttonUpload;
	private CGrid<CAttachment> grid;
	private CHorizontalLayout layoutToolbar;
	private IHasAttachments masterEntity;
	private final List<Consumer<CAttachment>> refreshListeners = new ArrayList<>();
	private final ISessionService sessionService;

	/** Constructor for attachment list component.
	 * @param attachmentService the attachment service
	 * @param sessionService    the session service */
	public CComponentListAttachments(final CAttachmentService attachmentService, final ISessionService sessionService) {
		Check.notNull(attachmentService, "AttachmentService cannot be null");
		Check.notNull(sessionService, "SessionService cannot be null");
		this.attachmentService = attachmentService;
		this.sessionService = sessionService;
		initializeComponent();
	}

	@Override
	public void addRefreshListener(final Consumer<CAttachment> listener) {
		Check.notNull(listener, "Refresh listener cannot be null");
		refreshListeners.add(listener);
	}

	@Override
	public void clearGrid() {
		Check.notNull(grid, "Grid cannot be null when clearing attachments");
		grid.setItems(List.of());
		grid.asSingleSelect().clear();
		buttonDownload.setEnabled(false);
		buttonDelete.setEnabled(false);
		updateCompactMode(true);
	}

	/** Configure grid columns. */
	@Override
	public void configureGrid(final CGrid<CAttachment> grid1) {
		Check.notNull(grid1, "Grid cannot be null");
		// Version number column (important - shows prominently)
		grid1.addCustomColumn(CAttachment::getVersionNumber, "Ver.", "100px", "version", 0)
				.setTooltipGenerator(item -> "Version " + item.getVersionNumber());
		// File name column (expanding to fill space)
		grid1.addExpandingShortTextColumn(CAttachment::getFileName, "File Name", "fileName");
		// Formatted file size
		grid1.addCustomColumn(CAttachment::getFormattedFileSize, "Size", "100px", "fileSize", 0);
		// File type/extension
		grid1.addCustomColumn(attachment -> {
			final String fileName = attachment.getFileName();
			if (fileName != null && fileName.contains(".")) {
				return fileName.substring(fileName.lastIndexOf(".") + 1).toUpperCase();
			}
			return "N/A";
		}, "Type", "80px", "fileType", 0);
		// Document type category
		try {
			grid1.addEntityColumn(CAttachment::getDocumentType, "Category", "documentType", CAttachment.class);
		} catch (final Exception e) {
			LOGGER.error("Error adding document type column: {}", e.getMessage(), e);
		}
		// Upload date
		grid1.addCustomColumn(attachment -> {
			if (attachment.getUploadDate() != null) {
				return attachment.getUploadDate().format(DATE_TIME_FORMATTER);
			}
			return "";
		}, "Uploaded", "150px", "uploadDate", 0);
		// Uploaded by user
		grid1.addCustomColumn(attachment -> {
			if (attachment.getUploadedBy() != null) {
				return attachment.getUploadedBy().getName();
			}
			return "";
		}, "By", "120px", "uploadedBy", 0);
	}

	@Override
	public CEntityDB<?> createNewEntityInstance() throws Exception {
		throw new UnsupportedOperationException("Attachments are managed via upload dialog.");
	}

	/** Create toolbar buttons. */
	private void createToolbarButtons() {
		// Upload button
		buttonUpload = new CButton(VaadinIcon.UPLOAD.create());
		buttonUpload.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		buttonUpload.setTooltipText("Upload attachment");
		buttonUpload.addClickListener(e -> on_buttonUpload_clicked());
		layoutToolbar.add(buttonUpload);
		// Edit button
		buttonEdit = new CButton(VaadinIcon.EDIT.create());
		buttonEdit.setTooltipText("Edit attachment metadata");
		buttonEdit.addClickListener(e -> on_buttonEdit_clicked());
		buttonEdit.setEnabled(false);
		layoutToolbar.add(buttonEdit);
		// Download button
		buttonDownload = new CButton(VaadinIcon.DOWNLOAD.create());
		buttonDownload.setTooltipText("Download attachment");
		buttonDownload.addClickListener(e -> on_buttonDownload_clicked());
		buttonDownload.setEnabled(false);
		layoutToolbar.add(buttonDownload);
		// Delete button
		buttonDelete = new CButton(VaadinIcon.TRASH.create());
		buttonDelete.addThemeVariants(ButtonVariant.LUMO_ERROR);
		buttonDelete.setTooltipText("Delete attachment");
		buttonDelete.addClickListener(e -> on_buttonDelete_clicked());
		buttonDelete.setEnabled(false);
		layoutToolbar.add(buttonDelete);
	}

	@Override
	public String getComponentName() { return "attachments"; }

	@Override
	public String getCurrentEntityIdString() {
		if (masterEntity instanceof CEntityDB<?>) {
			final CEntityDB<?> entity = (CEntityDB<?>) masterEntity;
			return entity.getId() != null ? entity.getId().toString() : null;
		}
		return null;
	}

	@Override
	public CAbstractService<?> getEntityService() { return attachmentService; }

	@Override
	public CGrid<CAttachment> getGrid() { return grid; }

	@Override
	public CEntityDB<?> getValue() {
		if (masterEntity instanceof CEntityDB<?>) {
			return (CEntityDB<?>) masterEntity;
		}
		return null;
	}

	/** Initialize the component layout and grid. */
	private void initializeComponent() {
		setId(ID_ROOT);
		setPadding(false);
		setSpacing(true);
		// Header
		final CH3 header = new CH3("Attachments");
		header.setId(ID_HEADER);
		add(header);
		// Toolbar
		layoutToolbar = new CHorizontalLayout();
		layoutToolbar.setId(ID_TOOLBAR);
		layoutToolbar.setSpacing(true);
		createToolbarButtons();
		add(layoutToolbar);
		// Grid
		grid = new CGrid<>(CAttachment.class);
		grid.setId(ID_GRID);
		CGrid.setupGrid(grid);
		grid.setRefreshConsumer(e -> refreshGrid());
		configureGrid(grid);
		grid.setHeight("300px"); // Default height
		grid.asSingleSelect().addValueChangeListener(e -> on_grid_selectionChanged(e.getValue()));
		add(grid);
		// Set initial compact mode (will adjust when data loaded)
		updateCompactMode(true);
	}

	private void linkAttachmentToMaster(final CAttachment attachment) {
		Check.notNull(attachment, "Attachment cannot be null");
		Check.notNull(masterEntity, "Master entity cannot be null");
		Set<CAttachment> items = masterEntity.getAttachments();
		if (items == null) {
			items = new HashSet<>();
			masterEntity.setAttachments(items);
		}
		final Long attachmentId = attachment.getId();
		final boolean exists = items.stream().anyMatch(existing -> {
			if (attachmentId != null && existing != null) {
				return attachmentId.equals(existing.getId());
			}
			return existing == attachment;
		});
		if (!exists) {
			items.add(attachment);
		}
		if (masterEntity instanceof CEntityDB<?>) {
			final CEntityDB<?> entity = (CEntityDB<?>) masterEntity;
			if (entity.getId() != null) {
				saveMasterEntity(entity);
			} else {
				LOGGER.warn("Master entity has no ID; attachment will persist when the parent entity is saved");
			}
		}
	}

	@Override
	public void notifyRefreshListeners(final CAttachment changedItem) {
		if (!refreshListeners.isEmpty()) {
			for (final Consumer<CAttachment> listener : refreshListeners) {
				try {
					listener.accept(changedItem);
				} catch (final Exception e) {
					LOGGER.error("Error notifying refresh listener", e);
				}
			}
		}
	}

	/** Handle delete button click. */
	protected void on_buttonDelete_clicked() {
		try {
			final CAttachment selected = grid.asSingleSelect().getValue();
			Check.notNull(selected, "No attachment selected");
			CNotificationService.showConfirmationDialog(
					"Delete attachment '" + selected.getFileName() + "' (version " + selected.getVersionNumber() + ")?", () -> {
						try {
							unlinkAttachmentFromMaster(selected);
							attachmentService.deleteStoredFile(selected);
							refreshGrid();
							notifyRefreshListeners(selected);
							CNotificationService.showDeleteSuccess();
						} catch (final Exception e) {
							CNotificationService.showException("Error deleting attachment", e);
						}
					});
		} catch (final Exception e) {
			CNotificationService.showException("Failed to delete attachment", e);
		}
	}

	/** Handle download button click. */
	protected void on_buttonDownload_clicked() {
		try {
			final CAttachment selected = grid.asSingleSelect().getValue();
			Check.notNull(selected, "No attachment selected");
			// Create stream resource for download
			final StreamResource resource = new StreamResource(selected.getFileName(), () -> {
				try {
					return attachmentService.downloadFile(selected);
				} catch (final Exception e) {
					LOGGER.error("Error downloading file", e);
					throw new IllegalStateException("Failed to download attachment", e);
				}
			});
			// Trigger download via anchor
			final com.vaadin.flow.component.html.Anchor downloadLink = new com.vaadin.flow.component.html.Anchor(resource, "");
			downloadLink.getElement().setAttribute("download", true);
			downloadLink.setId("download-" + selected.getId());
			// Add to UI temporarily
			getElement().appendChild(downloadLink.getElement());
			// Trigger click via JavaScript
			downloadLink.getElement().executeJs("this.click();");
			// Remove after download
			getUI().ifPresent(ui -> ui.access(() -> {
				getElement().removeChild(downloadLink.getElement());
			}));
			CNotificationService.showSuccess("Downloading: " + selected.getFileName());
		} catch (final Exception e) {
			CNotificationService.showException("Failed to download file", e);
		}
	}

	/** Handle upload button click. */
	protected void on_buttonUpload_clicked() {
		try {
			if (masterEntity == null) {
				CNotificationService.showWarning("Please select an entity first");
				return;
			}
			final Object parentEntity = masterEntity;
			if (!(parentEntity instanceof CEntityDB)) {
				CNotificationService.showError("Entity does not support file uploads");
				LOGGER.error("Master entity does not extend CEntityDB: {}", parentEntity.getClass().getSimpleName());
				return;
			}
			
			final CDialogAttachment dialog = new CDialogAttachment(
				attachmentService, 
				sessionService, 
				(CEntityDB<?>) parentEntity, 
				attachment -> {
					try {
						linkAttachmentToMaster(attachment);
						refreshGrid();
						notifyRefreshListeners(attachment);
					} catch (final Exception e) {
						LOGGER.error("Error refreshing grid after upload", e);
					}
				}
			);
			dialog.open();
		} catch (final Exception e) {
			CNotificationService.showException("Error opening attachment dialog", e);
		}
	}

	protected void on_buttonEdit_clicked() {
		try {
			final CAttachment selected = grid.asSingleSelect().getValue();
			Check.notNull(selected, "No attachment selected");
			
			final CDialogAttachment dialog = new CDialogAttachment(
				selected, 
				attachment -> {
					try {
						attachmentService.save(attachment);
						refreshGrid();
						notifyRefreshListeners(attachment);
					} catch (final Exception e) {
						LOGGER.error("Error saving attachment", e);
						CNotificationService.showException("Error saving attachment", e);
					}
				}
			);
			dialog.open();
		} catch (final Exception e) {
			CNotificationService.showException("Error opening edit dialog", e);
		}
	}

	/** Handle grid selection changes. */
	private void on_grid_selectionChanged(final CAttachment selected) {
		buttonEdit.setEnabled(selected != null);
		buttonDownload.setEnabled(selected != null);
		buttonDelete.setEnabled(selected != null);
	}

	@Override
	public void populateForm() {
		refreshGrid();
	}

	@Override
	public void refreshGrid() {
		Check.notNull(grid, "Grid cannot be null when refreshing attachments");
		if (masterEntity == null) {
			LOGGER.debug("Master entity is null, clearing grid");
			clearGrid();
			return;
		}
		// Load attachments from parent entity's collection
		final List<CAttachment> items = new ArrayList<>(masterEntity.getAttachments());
		// Sort by version number descending (newest first)
		items.sort((a1, a2) -> {
			final int v1 = a1.getVersionNumber() != null ? a1.getVersionNumber() : 0;
			final int v2 = a2.getVersionNumber() != null ? a2.getVersionNumber() : 0;
			return Integer.compare(v2, v1);
		});
		grid.setItems(items);
		grid.asSingleSelect().clear();
		updateCompactMode(items.isEmpty());
		LOGGER.debug("Loaded {} attachments for entity", items.size());
	}

	@Override
	public void registerWithPageService(final tech.derbent.api.services.pageservice.CPageService<?> pageService) {
		tech.derbent.api.utils.Check.notNull(pageService, "Page service cannot be null");
		pageService.registerComponent(getComponentName(), this);
		LOGGER.debug("[BindDebug] {} auto-registered with page service as '{}'", getClass().getSimpleName(), getComponentName());
	}

	@Override
	public void removeRefreshListener(final Consumer<CAttachment> listener) {
		if (listener != null) {
			refreshListeners.remove(listener);
		}
	}

	private void saveMasterEntity(final CEntityDB<?> entity) {
		Check.notNull(entity, "Entity cannot be null");
		try {
			saveMasterEntityTyped(entity);
		} catch (final Exception e) {
			LOGGER.error("Failed to save master entity after attachment upload", e);
			CNotificationService.showException("Failed to save attachment to parent entity", e);
		}
	}

	@SuppressWarnings ("unchecked")
	private <T extends CEntityDB<T>> void saveMasterEntityTyped(final CEntityDB<?> entity) {
		final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(entity.getClass());
		final CAbstractService<T> service = (CAbstractService<T>) CSpringContext.getBean(serviceClass);
		service.save((T) entity);
	}

	public void setEntity(final Object entity) {
		if (entity == null) {
			setValue(null);
			return;
		}
		if (entity instanceof CEntityDB<?>) {
			setValue((CEntityDB<?>) entity);
			return;
		}
		if (entity instanceof IHasAttachments) {
			masterEntity = (IHasAttachments) entity;
			refreshGrid();
			return;
		}
		LOGGER.warn("Entity does not implement IHasAttachments: {}", entity.getClass().getSimpleName());
		masterEntity = null;
		clearGrid();
	}

	/** Set the master entity for this component.
	 * @param masterEntity the master entity that owns the attachments */
	public void setMasterEntity(final IHasAttachments masterEntity) {
		this.masterEntity = masterEntity;
		refreshGrid();
	}

	@Override
	public void setValue(final CEntityDB<?> entity) {
		if (entity == null) {
			masterEntity = null;
			clearGrid();
			return;
		}
		if (entity instanceof IHasAttachments) {
			masterEntity = (IHasAttachments) entity;
			refreshGrid();
			return;
		}
		LOGGER.warn("setValue called with unexpected entity type: {}", entity.getClass().getSimpleName());
		masterEntity = null;
		clearGrid();
	}

	private void unlinkAttachmentFromMaster(final CAttachment attachment) {
		Check.notNull(attachment, "Attachment cannot be null");
		Check.notNull(masterEntity, "Master entity cannot be null");
		Check.instanceOf(masterEntity, CEntityDB.class, "Master entity must support database persistence");
		final CEntityDB<?> entity = (CEntityDB<?>) masterEntity;
		Check.notNull(entity.getId(), "Master entity must be saved before deleting attachments");
		final Set<CAttachment> items = masterEntity.getAttachments();
		Check.notNull(items, "Attachments list cannot be null");
		final Long attachmentId = attachment.getId();
		final boolean removed = items.removeIf(existing -> {
			if (attachmentId != null && existing != null) {
				return attachmentId.equals(existing.getId());
			}
			return existing == attachment;
		});
		Check.isTrue(removed, "Attachment not found in master entity");
		saveMasterEntity(entity);
	}

	/** Update component height based on content.
	 * @param isEmpty true if no attachments exist */
	private void updateCompactMode(final boolean isEmpty) {
		if (isEmpty) {
			// Compact mode: narrow height when empty
			grid.setHeight("150px");
			setHeight("200px"); // Component total height
			LOGGER.debug("Compact mode: No attachments");
		} else {
			// Normal mode: full height when has content
			grid.setHeight("300px");
			setHeight("auto"); // Component auto-adjusts
			LOGGER.debug("Normal mode: Has attachments");
		}
	}
}
