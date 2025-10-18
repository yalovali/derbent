package tech.derbent.api.views.components;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.InMemoryUploadCallback;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.server.streams.UploadMetadata;
import com.vaadin.flow.shared.Registration;
import tech.derbent.api.ui.notifications.CNotifications;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.CImageUtils;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;

/** Custom picture selector component that handles byte[] image data fields. Provides functionality to display, upload, and delete image data. This
 * component is reusable and can be used anywhere in forms for byte[] fields marked with ImageData=true. Supports two modes: - Full mode (default):
 * Shows image with upload controls and delete button inline - Icon mode: Shows only a small image icon, opens dialog when clicked for full
 * functionality */
public class CPictureSelector extends Composite<CVerticalLayout>
		implements HasValueAndElement<AbstractField.ComponentValueChangeEvent<CPictureSelector, byte[]>, byte[]> {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CPictureSelector.class);
	private static final long MAX_FILE_SIZE = CImageUtils.MAX_IMAGE_SIZE;
	private final EntityFieldInfo fieldInfo;
	private final Image imagePreview;
	private final Upload imageUpload;
	private final CButton deleteButton;
	private final Span dropLabel;
	private final boolean iconMode;
	private byte[] currentValue;
	private boolean readOnly = false;
	private boolean required = false;

	/** Constructor for CPictureSelector component with default full mode.
	 * @param fieldInfo Field information containing display settings and metadata */
	public CPictureSelector(final EntityFieldInfo fieldInfo) {
		this(fieldInfo, false);
	}

	/** Constructor for CPictureSelector component.
	 * @param fieldInfo Field information containing display settings and metadata
	 * @param iconMode  If true, displays only the image icon and opens dialog when clicked */
	public CPictureSelector(final EntityFieldInfo fieldInfo, final boolean iconMode) {
		this.fieldInfo = fieldInfo;
		this.iconMode = iconMode;
		// Create image preview component
		imagePreview = new Image();
		CAuxillaries.setId(imagePreview);
		// Set dimensions based on mode
		if (iconMode) {
			// Small icon mode - circular profile picture style
			imagePreview.setWidth("40px");
			imagePreview.setHeight("40px");
			imagePreview.getStyle().set("border-radius", "50%");
			imagePreview.getStyle().set("object-fit", "cover");
			imagePreview.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
			imagePreview.getStyle().set("cursor", "pointer");
		} else {
			// Full mode - use fieldInfo dimensions or defaults
			String width = fieldInfo.getWidth().isEmpty() ? "100px" : fieldInfo.getWidth();
			String height = "100px"; // Default height, could be made configurable
			imagePreview.setWidth(width);
			imagePreview.setHeight(height);
			imagePreview.getStyle().set("border-radius", "8px");
			imagePreview.getStyle().set("object-fit", "cover");
			imagePreview.getStyle().set("border", "2px solid var(--lumo-contrast-20pct)");
			imagePreview.getStyle().set("cursor", "pointer");
		}
		// Create upload component (only used in full mode)
		dropLabel = new Span("Drop image here or click to upload");
		imageUpload = new Upload();
		CAuxillaries.setId(imageUpload);
		// Configure upload (only in full mode)
		if (!iconMode) {
			setupUpload();
		}
		// Create delete button (only used in full mode)
		deleteButton = CButton.createTertiary("Delete", null, this::handleDelete);
		if (!iconMode) {
			deleteButton.getStyle().set("margin-top", "5px");
		}
		// Setup layout based on mode
		setupLayout();
		// Initialize with default or empty state
		setValue(null);
		// Set read-only state from field info
		setReadOnly(fieldInfo.isReadOnly());
		setRequiredIndicatorVisible(fieldInfo.isRequired());
		// Add click listener to image
		imagePreview.addClickListener(this::onImageClick);
	}

	/** Sets up the layout based on mode. */
	private void setupLayout() {
		getContent().setSpacing(!iconMode); // No spacing in icon mode for compact display
		getContent().setPadding(false);
		getContent().setAlignItems(CVerticalLayout.Alignment.CENTER);
		if (iconMode) {
			// Icon mode: only show the image
			getContent().add(imagePreview);
		} else {
			// Full mode: show image with upload controls
			getContent().add(imagePreview, imageUpload, deleteButton);
		}
	}

	/** Sets up the upload component configuration. */
	private void setupUpload() {
		final InMemoryUploadCallback uploadCallback = this::handleUpload;
		final InMemoryUploadHandler uploadHandler = new InMemoryUploadHandler(uploadCallback);
		imageUpload.setUploadHandler(uploadHandler);
		imageUpload.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif");
		imageUpload.setMaxFileSize((int) MAX_FILE_SIZE);
		imageUpload.setDropLabel(dropLabel);
		imageUpload.setUploadButton(CButton.createTertiary("Choose File", null, null));
		imageUpload.addAllFinishedListener(_ -> {
			LOGGER.info("Image upload completed");
		});
	}

	/** Handles image upload callback.
	 * @param metadata Upload metadata
	 * @param data     Image data bytes */
	private void handleUpload(final UploadMetadata metadata, final byte[] data) {
		LOGGER.info("Image upload received: {} ({} bytes)", metadata.fileName(), data.length);
		try {
			// Validate the image data
			CImageUtils.validateImageData(data, metadata.fileName());
			// Resize image to appropriate size for the field
			final byte[] resizedImageData = CImageUtils.resizeToProfilePicture(data);
			// Update the value
			setValue(resizedImageData);
			CNotifications.showSuccess("Image uploaded and resized successfully");
		} catch (final Exception e) {
			LOGGER.error("Unexpected error during image upload", e);
			CNotifications.showWarningDialog("Failed to upload image: " + e.getMessage());
		}
	}

	/** Handles delete button click.
	 * @param event Click event */
	private void handleDelete(final ClickEvent<Button> event) {
		setValue(null);
		CNotifications.showSuccess("Image deleted");
	}

	/** Handles click on image preview. In icon mode, opens a dialog with full picture selector functionality. In full mode, does nothing special
	 * (upload component handles interaction).
	 * @param event Click event */
	private void onImageClick(final ClickEvent<Image> event) {
		if (iconMode && !readOnly) {
			// Open dialog with full picture selector functionality
			CPictureSelectorDialog dialog = new CPictureSelectorDialog(fieldInfo, currentValue, readOnly);
			dialog.addValueChangeListener(newValue -> {
				// Update our value when dialog saves
				setValue(newValue);
			});
			dialog.open();
		}
		// In full mode, user can use the upload component directly
	}

	/** Updates the image preview based on current value. */
	private void updateImagePreview() {
		if ((currentValue != null) && (currentValue.length > 0)) {
			final String dataUrl = CImageUtils.createDataUrl(currentValue);
			if (dataUrl != null) {
				imagePreview.setSrc(dataUrl);
				imagePreview.setAlt("Current image");
			} else {
				setDefaultImage();
			}
			deleteButton.setEnabled(true && !readOnly);
		} else {
			setDefaultImage();
			deleteButton.setEnabled(false);
		}
	}

	/** Sets the default placeholder image. */
	private void setDefaultImage() {
		imagePreview.setSrc(CImageUtils.getDefaultProfilePictureDataUrl());
		imagePreview.setAlt("No image selected");
	}

	@Override
	public byte[] getValue() { return currentValue; }

	@Override
	public void setValue(final byte[] value) {
		final byte[] oldValue = this.currentValue;
		this.currentValue = value;
		updateImagePreview();
		// Fire value change event to registered listeners
		final AbstractField.ComponentValueChangeEvent<CPictureSelector, byte[]> event =
				new AbstractField.ComponentValueChangeEvent<>(this, this, oldValue, false);
		for (ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<CPictureSelector, byte[]>> listener : valueChangeListeners) {
			listener.valueChanged(event);
		}
	}

	private List<ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<CPictureSelector, byte[]>>> valueChangeListeners =
			new ArrayList<>();

	@Override
	public Registration
			addValueChangeListener(final ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<CPictureSelector, byte[]>> listener) {
		valueChangeListeners.add(listener);
		return () -> valueChangeListeners.remove(listener);
	}

	@Override
	public void setReadOnly(final boolean readOnly) {
		this.readOnly = readOnly;
		if (!iconMode) {
			// Only show/hide controls in full mode
			imageUpload.setVisible(!readOnly);
			deleteButton.setVisible(!readOnly);
		}
		if (readOnly) {
			imagePreview.getStyle().set("cursor", "default");
		} else {
			imagePreview.getStyle().set("cursor", "pointer");
		}
	}

	@Override
	public boolean isReadOnly() { return readOnly; }

	@Override
	public void setRequiredIndicatorVisible(final boolean requiredIndicatorVisible) {
		this.required = requiredIndicatorVisible;
		// Could add visual indicator here if needed
	}

	@Override
	public boolean isRequiredIndicatorVisible() { return required; }

	@Override
	public byte[] getEmptyValue() { return null; }
}
