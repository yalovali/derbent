package tech.derbent.api.ui.component.enhanced;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.InMemoryUploadCallback;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.server.streams.UploadMetadata;
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CH3;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.CImageUtils;

/** Dialog that contains the full picture selector functionality. Used when CPictureSelector is in icon mode and user clicks on the icon. */
public class CDialogPictureSelector extends Dialog {

	public interface ValueChangeListener {

		void valueChanged(byte[] newValue);
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(CDialogPictureSelector.class);
	private static final long MAX_FILE_SIZE = CImageUtils.MAX_IMAGE_SIZE;
	private static final long serialVersionUID = 1L;
	private CButton cancelButton;
	private byte[] currentValue;
	private CButton deleteButton;
	private Span dropLabel;
	private final EntityFieldInfo fieldInfo;
	private Image imagePreview;
	private Upload imageUpload;
	private final byte[] originalValue;
	private boolean readOnly = false;
	private CButton saveButton;
	private final List<ValueChangeListener> valueChangeListeners = new ArrayList<>();

	public CDialogPictureSelector(final EntityFieldInfo fieldInfo, final byte[] initialValue, final boolean readOnly) {
		this.fieldInfo = fieldInfo;
		currentValue = initialValue != null ? initialValue.clone() : null;
		originalValue = initialValue != null ? initialValue.clone() : null;
		this.readOnly = readOnly;
		setWidth("400px");
		setHeight("500px");
		setModal(true);
		setCloseOnEsc(true);
		setCloseOnOutsideClick(false);
		createComponents();
		setupLayout();
		updateImagePreview();
	}

	public void addValueChangeListener(ValueChangeListener listener) {
		valueChangeListeners.add(listener);
	}

	private void createComponents() {
		// Dialog title
		final CH3 title = new CH3("Picture Selector");
		title.getStyle().set("margin", "0 0 20px 0");
		// Create image preview component
		imagePreview = new Image();
		CAuxillaries.setId(imagePreview);
		// Set dimensions from fieldInfo or use defaults
		final String width = fieldInfo.getWidth().isEmpty() ? "150px" : fieldInfo.getWidth();
		final String height = "150px";
		imagePreview.setWidth(width);
		imagePreview.setHeight(height);
		imagePreview.getStyle().set("border-radius", "8px");
		imagePreview.getStyle().set("object-fit", "cover");
		imagePreview.getStyle().set("border", "2px solid var(--lumo-contrast-20pct)");
		imagePreview.getStyle().set("display", "block");
		imagePreview.getStyle().set("margin", "0 auto");
		// Create upload component
		dropLabel = new Span("Drop image here or click to upload");
		imageUpload = new Upload();
		CAuxillaries.setId(imageUpload);
		// Configure upload
		setupUpload();
		// Create buttons
		deleteButton = CButton.createTertiary("Delete Image", null, this::handleDelete);
		saveButton = CButton.createPrimary("Save", null, this::handleSave);
		cancelButton = CButton.createTertiary("Cancel", null, this::handleCancel);
		// Set visibility based on read-only state
		imageUpload.setVisible(!readOnly);
		deleteButton.setVisible(!readOnly);
	}

	private void handleCancel(@SuppressWarnings ("unused") final ClickEvent<Button> event) {
		// Restore original value
		currentValue = originalValue != null ? originalValue.clone() : null;
		close();
	}

	private void handleDelete(@SuppressWarnings ("unused") final ClickEvent<Button> event) {
		currentValue = null;
		updateImagePreview();
		CNotificationService.showSuccess("Image deleted");
	}

	private void handleSave(@SuppressWarnings ("unused") final ClickEvent<Button> event) {
		// Notify listeners of the value change
		for (final ValueChangeListener listener : valueChangeListeners) {
			listener.valueChanged(currentValue);
		}
		close();
	}

	private void handleUpload(final UploadMetadata metadata, final byte[] data) {
		LOGGER.info("Image upload received: {} ({} bytes)", metadata.fileName(), data.length);
		try {
			// Validate the image data
			CImageUtils.validateImageData(data, metadata.fileName());
			// Resize image to appropriate size for the field
			final byte[] resizedImageData = CImageUtils.resizeToProfilePicture(data);
			// Update the current value
			currentValue = resizedImageData;
			updateImagePreview();
			CNotificationService.showSuccess("Image uploaded and resized successfully");
		} catch (final Exception e) {
			LOGGER.error("Unexpected error during image upload", e);
			CNotificationService.showWarningDialog("Failed to upload image: " + e.getMessage());
		}
	}

	public void removeValueChangeListener(ValueChangeListener listener) {
		valueChangeListeners.remove(listener);
	}

	private void setDefaultImage() {
		imagePreview.setSrc(CImageUtils.getDefaultProfilePictureDataUrl());
		imagePreview.setAlt("No image selected");
	}

	private void setupLayout() {
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setPadding(true);
		layout.setAlignItems(VerticalLayout.Alignment.CENTER);
		final CH3 title = new CH3("Picture Selector");
		title.getStyle().set("margin", "0 0 20px 0");
		layout.add(title, imagePreview);
		if (!readOnly) {
			layout.add(imageUpload, deleteButton);
		}
		// Button layout
		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.add(cancelButton, saveButton);
		layout.add(buttonLayout);
		add(layout);
	}

	private void setupUpload() {
		final InMemoryUploadCallback uploadCallback = this::handleUpload;
		final InMemoryUploadHandler uploadHandler = new InMemoryUploadHandler(uploadCallback);
		imageUpload.setUploadHandler(uploadHandler);
		imageUpload.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif");
		imageUpload.setMaxFileSize((int) MAX_FILE_SIZE);
		imageUpload.setDropLabel(dropLabel);
		imageUpload.setUploadButton(CButton.createTertiary("Choose File", null, null));
		imageUpload.addAllFinishedListener( event -> {
			LOGGER.info("Image upload completed");
		});
	}

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
}
