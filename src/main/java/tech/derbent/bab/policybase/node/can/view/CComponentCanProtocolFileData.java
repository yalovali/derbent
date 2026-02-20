package tech.derbent.bab.policybase.node.can.view;

import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import tech.derbent.api.interfaces.IComponentTransientPlaceHolder;
import tech.derbent.api.interfaces.IPageServiceAutoRegistrable;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.component.enhanced.CComponentBase;
import tech.derbent.api.ui.constants.CUIConstants;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.policybase.node.can.CBabCanNode;
import tech.derbent.bab.policybase.node.can.CBabCanNodeService;

/** Upload/delete component for CAN protocol file content (`protocolFileData`). */
public class CComponentCanProtocolFileData extends CComponentBase<CBabCanNode>
		implements IPageServiceAutoRegistrable, IComponentTransientPlaceHolder<CBabCanNode> {

	private static final String COMPONENT_NAME = "canProtocolFileData";
	private static final String DIALOG_TITLE_PARSED_JSON = "Protocol Parsed JSON";
	private static final String DIALOG_TITLE_RAW_DATA = "Protocol Raw Data";
	private static final String FILE_EXTENSION_A2L = ".a2l";
	public static final String ID_DELETE_BUTTON = "custom-can-protocol-delete-button";
	public static final String ID_FILE_SIZE = "custom-can-protocol-file-size";
	public static final String ID_ROOT = "custom-can-protocol-data-component";
	public static final String ID_STATUS = "custom-can-protocol-status";
	public static final String ID_UPLOAD = "custom-can-protocol-upload";
	public static final String ID_VIEW_JSON_BUTTON = "custom-can-protocol-view-json-button";
	public static final String ID_VIEW_RAW_BUTTON = "custom-can-protocol-view-raw-button";
	private static final String MESSAGE_NO_PARSED_JSON = "No parsed protocol JSON is stored.";
	private static final String MESSAGE_NO_RAW_DATA = "No raw protocol file data is stored.";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentCanProtocolFileData.class);
	private static final long MAX_UPLOAD_FILE_SIZE_BYTES = 10L * 1024L * 1024L;
	private static final String STATUS_NO_NODE = "No CAN node selected.";
	private static final long serialVersionUID = 1L;
	private final CHorizontalLayout actionRow;
	private final CButton buttonDelete;
	private final CButton buttonViewJson;
	private final CButton buttonViewRaw;
	private final CBabCanNodeService canNodeService;
	private final CSpan labelFileSize;
	private final CSpan labelStatus;
	private final MemoryBuffer uploadBuffer;
	private final Upload upload;

	public CComponentCanProtocolFileData(final CBabCanNodeService canNodeService) {
		Check.notNull(canNodeService, "CBabCanNodeService cannot be null");
		this.canNodeService = canNodeService;
		setId(ID_ROOT);
		setSpacing(false);
		setPadding(false);
		getStyle().set("gap", CUIConstants.GAP_TINY);

		labelFileSize = new CSpan();
		labelFileSize.setId(ID_FILE_SIZE);
		uploadBuffer = new MemoryBuffer();
		upload = createUploadComponent();
		buttonDelete = CButton.createError("Delete Protocol Data", VaadinIcon.TRASH.create(), this::on_buttonDelete_clicked);
		buttonDelete.setId(ID_DELETE_BUTTON);
		buttonViewRaw = CButton.createTertiary("View Raw", VaadinIcon.FILE_TEXT.create(), this::on_buttonViewRaw_clicked);
		buttonViewRaw.setId(ID_VIEW_RAW_BUTTON);
		buttonViewJson = CButton.createTertiary("View JSON", VaadinIcon.CODE.create(), this::on_buttonViewJson_clicked);
		buttonViewJson.setId(ID_VIEW_JSON_BUTTON);
		actionRow = new CHorizontalLayout(upload, buttonViewRaw, buttonViewJson, buttonDelete);
		actionRow.setSpacing(true);
		actionRow.getStyle().set("flex-wrap", "wrap");

		labelStatus = new CSpan();
		labelStatus.setId(ID_STATUS);

		add(labelFileSize, actionRow, labelStatus);
		refreshComponent();
	}

	private Upload createUploadComponent() {
		final Upload uploadField = new Upload(uploadBuffer);
		uploadField.setId(ID_UPLOAD);
		uploadField.setAcceptedFileTypes(FILE_EXTENSION_A2L);
		uploadField.setMaxFileSize((int) MAX_UPLOAD_FILE_SIZE_BYTES);
		uploadField.setMaxFiles(1);
		uploadField.setDropLabel(new Span(""));
		uploadField.setUploadButton(CButton.createPrimary("Upload Protocol File", VaadinIcon.UPLOAD.create(), null));
		uploadField.addSucceededListener(event -> on_uploadSucceeded(event.getFileName(), event.getContentLength()));
		uploadField.addFileRejectedListener(event -> on_uploadError(event.getErrorMessage(), 0L));
		uploadField.addFailedListener(event -> on_uploadError(event.getReason() != null ? event.getReason().getMessage() : null, 0L));
		return uploadField;
	}

	@Override
	public String getComponentName() { return COMPONENT_NAME; }

	private CBabCanNode getCurrentNode() { return getValue(); }

	private static boolean hasProtocolData(final CBabCanNode node) {
		return (node != null) && node.getProtocolFileData() != null && !node.getProtocolFileData().isBlank();
	}

	private void on_buttonDelete_clicked(@SuppressWarnings ("unused") final com.vaadin.flow.component.ClickEvent<Button> event) {
		final CBabCanNode node = getCurrentNode();
		if (node == null) {
			return;
		}

		node.setProtocolFileData(null);
		node.setProtocolFileJson(null);
		node.setNodeConfigJson(null);
		node.setProtocolFileSummaryJson(canNodeService.createNoFileSummaryJson());
		upload.clearFileList();
		updateValueFromClient(node);
		refreshComponent();
	}

	@Override
	protected void onValueChanged(final CBabCanNode oldValue, final CBabCanNode newValue, final boolean fromClient) {
		super.onValueChanged(oldValue, newValue, fromClient);
		// Always refresh for binder-driven rebinds; entities may be equal by id or same instance with updated fields.
		refreshComponent();
	}

	private void on_uploadSucceeded(final String fileName, final long contentLength) {
		final CBabCanNode node = getCurrentNode();
		if (node == null) {
			labelStatus.setText(STATUS_NO_NODE);
			labelStatus.getStyle().set("color", "var(--lumo-error-text-color)");
			return;
		}

		long fileSizeBytes = Math.max(contentLength, 0L);
		try {
			final byte[] fileData = uploadBuffer.getInputStream().readAllBytes();
			fileSizeBytes = fileData.length;
			if (contentLength > 0 && fileData.length == 0) {
				node.setProtocolFileSummaryJson(canNodeService.createParseErrorSummaryJson("Uploaded file is empty.", fileSizeBytes));
				updateStatusFromSummary(node.getProtocolFileSummaryJson());
				return;
			}
			final String protocolContent = new String(fileData, StandardCharsets.UTF_8);
			node.setProtocolFileData(protocolContent);
			final String parsedJson = canNodeService.parseA2LContentAsJson(protocolContent);
			node.setProtocolFileJson(parsedJson);
			node.setNodeConfigJson(parsedJson);
			node.setProtocolFileSummaryJson(canNodeService.createParsedSummaryJson(parsedJson, fileSizeBytes));
		} catch (final Exception e) {
			LOGGER.warn("Protocol file parse failed for '{}': {}", fileName, e.getMessage());
			node.setProtocolFileJson(null);
			node.setNodeConfigJson(null);
			node.setProtocolFileSummaryJson(canNodeService.createParseErrorSummaryJson(e.getMessage(), fileSizeBytes));
		}
		updateValueFromClient(node);
		refreshComponent();
	}

	private void on_uploadError(final String errorMessage, final long fileSizeBytes) {
		final String summaryJson = canNodeService.createParseErrorSummaryJson(errorMessage, fileSizeBytes);
		final CBabCanNode node = getCurrentNode();
		if (node != null) {
			node.setProtocolFileSummaryJson(summaryJson);
			updateValueFromClient(node);
			refreshComponent();
			return;
		}
		updateStatusFromSummary(summaryJson);
	}

	private void on_buttonViewJson_clicked(@SuppressWarnings ("unused") final com.vaadin.flow.component.ClickEvent<Button> event) {
		showProtocolContentFromDb(CBabCanNodeService.EProtocolContentField.JSON, DIALOG_TITLE_PARSED_JSON, MESSAGE_NO_PARSED_JSON);
	}

	private void on_buttonViewRaw_clicked(@SuppressWarnings ("unused") final com.vaadin.flow.component.ClickEvent<Button> event) {
		showProtocolContentFromDb(CBabCanNodeService.EProtocolContentField.RAW, DIALOG_TITLE_RAW_DATA, MESSAGE_NO_RAW_DATA);
	}

	@Override
	protected void refreshComponent() {
		final CBabCanNode node = getCurrentNode();
		if (node == null) {
			labelFileSize.setText("File size: 0 B");
			upload.setVisible(true);
			buttonViewRaw.setVisible(true);
			buttonViewJson.setVisible(true);
			buttonDelete.setVisible(true);
			upload.setEnabled(false);
			buttonViewRaw.setEnabled(false);
			buttonViewJson.setEnabled(false);
			buttonDelete.setEnabled(false);
			labelStatus.setText(STATUS_NO_NODE);
			labelStatus.getStyle().set("color", "var(--lumo-error-text-color)");
			return;
		}

		ensureSummaryInitialized(node);
		final CBabCanNodeService.CProtocolFileSummary summary = canNodeService.parseSummaryJson(node.getProtocolFileSummaryJson());
		final boolean hasData = hasProtocolData(node);
		final boolean hasPersistedParsedStatus = summary.status() == CBabCanNodeService.EProtocolSummaryStatus.PARSED;
		final long displaySizeBytes = hasData ? node.getProtocolFileData().getBytes(StandardCharsets.UTF_8).length : summary.fileSizeBytes();

		labelFileSize.setText("File size: " + displaySizeBytes + " B");
		upload.setVisible(true);
		buttonViewRaw.setVisible(true);
		buttonViewJson.setVisible(true);
		buttonDelete.setVisible(true);

		final boolean canUpload = !isReadOnly() && !hasData && !hasPersistedParsedStatus;
		final boolean hasRawContent = hasData || summary.fileSizeBytes() > 0L;
		final boolean hasParsedJson =
				hasPersistedParsedStatus || ((node.getProtocolFileJson() != null) && !node.getProtocolFileJson().isBlank());
		final boolean canDelete = !isReadOnly() && (summary.status() != CBabCanNodeService.EProtocolSummaryStatus.NO_FILE || hasData);

		upload.setEnabled(canUpload);
		buttonViewRaw.setEnabled(hasRawContent);
		buttonViewJson.setEnabled(hasParsedJson);
		buttonDelete.setEnabled(canDelete);
		updateStatusFromSummary(node.getProtocolFileSummaryJson());
	}

	@Override
	public void setThis(final CBabCanNode value) {
		setValue(value);
	}

	private void ensureSummaryInitialized(final CBabCanNode node) {
		if (node.getProtocolFileSummaryJson() == null || node.getProtocolFileSummaryJson().isBlank()) {
			if (node.getProtocolFileData() != null && !node.getProtocolFileData().isBlank()) {
				try {
					final String parsedJson = canNodeService.parseA2LContentAsJson(node.getProtocolFileData());
					node.setProtocolFileJson(parsedJson);
					node.setNodeConfigJson(parsedJson);
					final long fileSizeBytes = node.getProtocolFileData().getBytes(StandardCharsets.UTF_8).length;
					node.setProtocolFileSummaryJson(canNodeService.createParsedSummaryJson(parsedJson, fileSizeBytes));
					return;
				} catch (final Exception e) {
					final long fileSizeBytes = node.getProtocolFileData().getBytes(StandardCharsets.UTF_8).length;
					node.setProtocolFileSummaryJson(canNodeService.createParseErrorSummaryJson(e.getMessage(), fileSizeBytes));
					return;
				}
			}
			node.setProtocolFileSummaryJson(canNodeService.createNoFileSummaryJson());
		}
	}

	private void updateStatusFromSummary(final String summaryJson) {
		final CBabCanNodeService.CProtocolFileSummary summary = canNodeService.parseSummaryJson(summaryJson);
		labelStatus.setText(summary.message());
		switch (summary.status()) {
			case PARSED -> labelStatus.getStyle().set("color", "var(--lumo-success-text-color)");
			case ERROR -> labelStatus.getStyle().set("color", "var(--lumo-error-text-color)");
			case NO_FILE -> labelStatus.getStyle().set("color", "var(--lumo-secondary-text-color)");
		}
	}

	private void showProtocolContentFromDb(final CBabCanNodeService.EProtocolContentField field,
			final String dialogTitle,
			final String emptyMessage) {
		final CBabCanNode node = getCurrentNode();
		if (node == null) {
			CNotificationService.showWarning(STATUS_NO_NODE);
			return;
		}
		try {
			final String content = resolveProtocolContentForDialog(node, field);
			if (content == null || content.isBlank()) {
				CNotificationService.showWarning(emptyMessage);
				return;
			}
			CNotificationService.showInfoDialog(dialogTitle, content);
		} catch (final Exception e) {
			LOGGER.warn("Failed to load protocol content for dialog: {}", e.getMessage());
			CNotificationService.showException("Failed to load protocol content", e);
		}
	}

	private String resolveProtocolContentForDialog(final CBabCanNode node, final CBabCanNodeService.EProtocolContentField field) {
		if (node.getId() != null) {
			final String fromDb = canNodeService.loadProtocolContentFromDb(node.getId(), field);
			if (fromDb != null) {
				switch (field) {
					case RAW -> node.setProtocolFileData(fromDb);
					case JSON -> node.setProtocolFileJson(fromDb);
				}
				return fromDb;
			}
		}
		return switch (field) {
			case RAW -> node.getProtocolFileData();
			case JSON -> node.getProtocolFileJson();
		};
	}
}
