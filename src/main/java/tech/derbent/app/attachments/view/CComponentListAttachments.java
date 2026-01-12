package tech.derbent.app.attachments.view;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.server.StreamResource;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.interfaces.IContentOwner;
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

/**
 * CComponentListAttachments - Component for managing attachments on entities.
 * 
 * Displays a list of attachments with version number, filename, size, type, 
 * upload date and uploaded by user. Supports upload, download, delete and 
 * version history operations.
 * 
 * This component uses the IHasAttachments interface for clean, type-safe
 * integration with any entity that can have attachments.
 * 
 * Benefits of interface approach:
 * - No generic type parameters needed
 * - No type casting required
 * - Works with any class (not just CEntityDB subclasses)
 * - Simpler factory implementation
 * - Better compile-time type safety
 * 
 * Usage:
 * <pre>
 * CComponentListAttachments component = new CComponentListAttachments(service, session);
 * component.setMasterEntity(activity); // activity implements IHasAttachments
 * </pre>
 */
public class CComponentListAttachments 
		extends CVerticalLayout implements IContentOwner, tech.derbent.api.interfaces.IPageServiceAutoRegistrable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentListAttachments.class);
	private static final long serialVersionUID = 1L;
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	
	private final CAttachmentService attachmentService;
	private final ISessionService sessionService;
	private IHasAttachments masterEntity;
	
	private CGrid<CAttachment> grid;
	private CButton buttonDownload;
	private CButton buttonUpload;
	private CButton buttonDelete;
	private CHorizontalLayout layoutToolbar;

	/** Constructor for attachment list component.
	 * @param attachmentService the attachment service
	 * @param sessionService the session service */
	public CComponentListAttachments(final CAttachmentService attachmentService, 
			final ISessionService sessionService) {
		Check.notNull(attachmentService, "AttachmentService cannot be null");
		Check.notNull(sessionService, "SessionService cannot be null");
		
		this.attachmentService = attachmentService;
		this.sessionService = sessionService;
		
		initializeComponent();
	}

	/** Initialize the component layout and grid. */
	private void initializeComponent() {
		setPadding(false);
		setSpacing(true);
		
		// Header
		final CH3 header = new CH3("Attachments");
		add(header);
		
		// Toolbar
		layoutToolbar = new CHorizontalLayout();
		layoutToolbar.setSpacing(true);
		createToolbarButtons();
		add(layoutToolbar);
		
		// Grid
		grid = new CGrid<>(CAttachment.class);
		CGrid.setupGrid(grid);
		configureGrid();
		grid.setHeight("300px"); // Default height
		grid.asSingleSelect().addValueChangeListener(e -> on_grid_selectionChanged(e.getValue()));
		add(grid);
		
		// Set initial compact mode (will adjust when data loaded)
		updateCompactMode(true);
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

	/** Configure grid columns. */
	private void configureGrid() {
		// Version number column (important - shows prominently)
		grid.addCustomColumn(CAttachment::getVersionNumber, "Ver.", "100px", "version", 0)
				.setTooltipGenerator(item -> "Version " + item.getVersionNumber());
		
		// File name column (expanding to fill space)
		grid.addExpandingShortTextColumn(CAttachment::getFileName, "File Name", "fileName");
		
		// Formatted file size
		grid.addCustomColumn(CAttachment::getFormattedFileSize, "Size", "100px", "fileSize", 0);
		
		// File type/extension
		grid.addCustomColumn(attachment -> {
			final String fileName = attachment.getFileName();
			if (fileName != null && fileName.contains(".")) {
				return fileName.substring(fileName.lastIndexOf(".") + 1).toUpperCase();
			}
			return "N/A";
		}, "Type", "80px", "fileType", 0);
		
		// Document type category
		try {
			grid.addEntityColumn(CAttachment::getDocumentType, "Category", "documentType", CAttachment.class);
		} catch (final Exception e) {
			LOGGER.error("Error adding document type column: {}", e.getMessage(), e);
		}
		
		// Upload date
		grid.addCustomColumn(attachment -> {
			if (attachment.getUploadDate() != null) {
				return attachment.getUploadDate().format(DATE_TIME_FORMATTER);
			}
			return "";
		}, "Uploaded", "150px", "uploadDate", 0);
		
		// Uploaded by user
		grid.addCustomColumn(attachment -> {
			if (attachment.getUploadedBy() != null) {
				return attachment.getUploadedBy().getName();
			}
			return "";
		}, "By", "120px", "uploadedBy", 0);
	}

	/** Create toolbar buttons. */
	private void createToolbarButtons() {
		// Upload button
		buttonUpload = new CButton(VaadinIcon.UPLOAD.create());
		buttonUpload.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		buttonUpload.setTooltipText("Upload attachment");
		buttonUpload.addClickListener(e -> on_buttonUpload_clicked());
		layoutToolbar.add(buttonUpload);
		
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
	public void populateForm() {
		refreshGrid();
	}

	@Override
	public void refreshGrid() {
		if (masterEntity == null) {
			LOGGER.debug("Master entity is null, clearing grid");
			grid.setItems(List.of());
			return;
		}
		
		// Load attachments from parent entity's collection
		// NOTE: Parent entities need @OneToMany List<CAttachment> attachments field
		List<CAttachment> items = List.of();
		
		// TODO: Once parent entities have @OneToMany List<CAttachment> getAttachments(), use:
		// if (masterEntity instanceof IHasAttachments) {
		//     items = ((IHasAttachments) masterEntity).getAttachments();
		// }
		
		// Temporary: Use empty list until parent entities are updated with @OneToMany
		LOGGER.warn("Parent entity attachments not yet implemented. Entity type: {}", 
				masterEntity.getClass().getSimpleName());
		
		grid.setItems(items);
	}

	/** Handle grid selection changes. */
	private void on_grid_selectionChanged(final CAttachment selected) {
		buttonDownload.setEnabled(selected != null);
		buttonDelete.setEnabled(selected != null);
	}

	/** Handle upload button click. */
	protected void on_buttonUpload_clicked() {
		if (masterEntity == null) {
			CNotificationService.showWarning("Please select an entity first");
			return;
		}
		
		final CDialogAttachmentUpload dialog = new CDialogAttachmentUpload(
				attachmentService,
				(tech.derbent.app.documenttypes.service.CDocumentTypeService) 
					tech.derbent.api.config.CSpringContext.getBean(
						tech.derbent.app.documenttypes.service.CDocumentTypeService.class),
				sessionService,
				masterEntity,
				attachment -> {
					// Refresh grid after successful upload
					try {
						refreshGrid();
					} catch (final Exception e) {
						LOGGER.error("Error refreshing grid after upload", e);
					}
				});
		dialog.open();
	}

	/** Handle download button click. */
	protected void on_buttonDownload_clicked() {
		try {
			final CAttachment selected = grid.asSingleSelect().getValue();
			Objects.requireNonNull(selected, "No attachment selected");
			
			// Create stream resource for download
			final StreamResource resource = new StreamResource(
					selected.getFileName(),
					() -> {
						try {
							return attachmentService.downloadFile(selected);
						} catch (final Exception e) {
							LOGGER.error("Error downloading file", e);
							return null;
						}
					});
			
			// Trigger download via anchor
			final com.vaadin.flow.component.html.Anchor downloadLink = 
					new com.vaadin.flow.component.html.Anchor(resource, "");
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
			LOGGER.error("Error initiating download", e);
			CNotificationService.showError("Failed to download file: " + e.getMessage());
		}
	}

	/** Handle delete button click. */
	protected void on_buttonDelete_clicked() {
		try {
			final CAttachment selected = grid.asSingleSelect().getValue();
			Objects.requireNonNull(selected, "No attachment selected");
			
			CNotificationService.showConfirmationDialog(
					"Delete attachment '" + selected.getFileName() + "' (version " + selected.getVersionNumber() + ")?",
					() -> {
						try {
							attachmentService.delete(selected);
							refreshGrid();
							CNotificationService.showDeleteSuccess();
						} catch (final Exception e) {
							LOGGER.error("Error deleting attachment", e);
							CNotificationService.showDeleteError();
						}
					});
		} catch (final Exception e) {
			LOGGER.error("Error initiating delete", e);
			CNotificationService.showError("Failed to delete attachment: " + e.getMessage());
		}
	}

	@Override
	public CEntityDB<?> createNewEntityInstance() throws Exception {
		throw new UnsupportedOperationException("Not supported - use upload dialog instead");
	}

	@Override
	public CEntityDB<?> getValue() {
		return masterEntity;
	}

	@Override
	public String getCurrentEntityIdString() {
		return masterEntity != null && masterEntity.getId() != null ? 
				masterEntity.getId().toString() : null;
	}

	@Override
	public tech.derbent.api.entity.service.CAbstractService<?> getEntityService() {
		return attachmentService;
	}

	@Override
	public void setValue(final CEntityDB<?> entity) {
		setEntity(entity);
	}

	public void setEntity(final Object entity) {
		if (entity == null) {
			this.masterEntity = null;
		} else {
			try {
				this.masterEntity = masterEntityClass.cast(entity);
			} catch (final ClassCastException e) {
				LOGGER.error("Entity is not of expected type {}: {}", masterEntityClass.getSimpleName(), 
						entity.getClass().getSimpleName());
				this.masterEntity = null;
			}
		}
		refreshGrid();
	}

	@Override
	public void registerWithPageService(final tech.derbent.api.services.pageservice.CPageService<?> pageService) {
		tech.derbent.api.utils.Check.notNull(pageService, "Page service cannot be null");
		pageService.registerComponent(getComponentName(), this);
		LOGGER.debug("[BindDebug] {} auto-registered with page service as '{}'", 
				getClass().getSimpleName(), getComponentName());
	}

	@Override
	public String getComponentName() {
		return "Attachments";
	}
}
