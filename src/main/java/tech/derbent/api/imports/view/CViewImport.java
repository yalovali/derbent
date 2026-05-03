package tech.derbent.api.imports.view;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entity.view.CAbstractPage;
import tech.derbent.api.imports.domain.CImportOptions;
import tech.derbent.api.imports.domain.CImportResult;
import tech.derbent.api.imports.domain.CImportRowResult;
import tech.derbent.api.imports.domain.CImportSheetResult;
import tech.derbent.api.imports.service.CDataImportService;
import tech.derbent.api.imports.service.CExcelImportService;
import tech.derbent.api.imports.service.CImportHandlerRegistry;
import tech.derbent.api.imports.service.CExcelTemplateService;
import tech.derbent.api.imports.service.CSampleImportExcelGenerator;
import tech.derbent.api.menu.MyMenu;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.users.domain.CUser;

/** CViewImport - Single-page Excel import view.
 * Lets users upload a multi-sheet Excel file and import entities project-wide.
 * Layer: View (MVC) */
@Route ("import")
@PageTitle ("Import Data")
@PermitAll
@MyMenu (title = "Tools.Import Data", order = "90.1", icon = "vaadin:upload-alt")
public final class CViewImport extends CAbstractPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(CViewImport.class);
    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_COLOR = "#1565C0";
    public static final String DEFAULT_ICON = "vaadin:upload-alt";
    public static final String VIEW_NAME = "Import Data";

    private final CExcelImportService importService;
    private final CDataImportService dataImportService;
    private final CImportHandlerRegistry handlerRegistry;
    private final ISessionService sessionService;
    private final CExcelTemplateService excelTemplateService;

    // Upload state
    private InputStream uploadedStream;
    private String uploadedFileName;
    private Button importButton;

    // Options
    private Checkbox dryRunCheckbox;
    private Checkbox rollbackOnErrorCheckbox;
    private Checkbox skipUnknownSheetsCheckbox;

    // Results area
    private Div resultsContainer;
    // Populated in @PostConstruct once handlerRegistry is available
    private Span supportedTypesSpan;

    public CViewImport(final CExcelImportService importService, final CDataImportService dataImportService,
            final CImportHandlerRegistry handlerRegistry, final ISessionService sessionService,
            final CExcelTemplateService excelTemplateService) {
        this.importService = importService;
        this.dataImportService = dataImportService;
        this.handlerRegistry = handlerRegistry;
        this.sessionService = sessionService;
        this.excelTemplateService = excelTemplateService;
    }

    @Override
    public void beforeEnter(final BeforeEnterEvent event) { /* no navigation guards needed */ }

    /**
     * initPage() is called from CAbstractPage() constructor — before this class's
     * fields are assigned.  Only build the static UI skeleton here; do NOT access
     * injected fields (importService, handlerRegistry, sessionService).
     * Data-dependent population happens in postConstruct().
     */
    @Override
    protected void initPage() {
        addClassNames(LumoUtility.Padding.MEDIUM);
        final VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(true);
        supportedTypesSpan = new Span();
        supportedTypesSpan.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);
        content.add(buildHeader(supportedTypesSpan));
        content.add(buildUploadSection());
        content.add(buildOptionsSection());
        content.add(buildActionBar());
        resultsContainer = new Div();
        resultsContainer.setWidthFull();
        content.add(resultsContainer);
        add(content);
    }

    /** Populates service-dependent UI content after Spring finishes injecting fields. */
    @PostConstruct
    protected void postConstruct() {
        final StringBuilder supported = new StringBuilder("Supported entity types: ");
        handlerRegistry.getAllHandlers().forEach(h ->
            supported.append(h.getEntityClass().getSimpleName().replaceFirst("^C", "")).append(", "));
        if (supported.toString().endsWith(", ")) {
            supported.setLength(supported.length() - 2);
        }
        supportedTypesSpan.setText(supported.toString());
    }

    @Override
    protected void setupToolbar() { /* no custom toolbar for this page */ }

    @Override
    public String getPageTitle() { return "Import Data"; }

    // Header

    private Div buildHeader(final Span supportedSpan) {
        final Div header = new Div();
        header.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
        final H2 title = new H2("Import Data from Excel");
        title.addClassNames(LumoUtility.Margin.Bottom.XSMALL);
        final Paragraph subtitle = new Paragraph(
            "Upload a multi-sheet .xlsx file. Each sheet name must match a supported entity type. "
            + "Comment rows (first cell starts with #) are skipped.");
        subtitle.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);
        header.add(title, subtitle, supportedSpan);
        return header;
    }

    // Upload section

    private Div buildUploadSection() {
        final Div section = new Div();
        section.addClassNames("import-upload-section", LumoUtility.Padding.MEDIUM,
                LumoUtility.BorderRadius.MEDIUM);
        section.getStyle().set("border", "2px dashed var(--lumo-contrast-20pct)");
        section.getStyle().set("background", "var(--lumo-contrast-5pct)");
        final MemoryBuffer buffer = new MemoryBuffer();
        final Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx");
        upload.setMaxFileSize(50 * 1024 * 1024); // 50 MB
        upload.setDropLabel(new Span("Drop .xlsx file here or click to browse"));
        upload.setUploadButton(new Button("Choose Excel File", VaadinIcon.UPLOAD.create()));
        upload.addSucceededListener(event -> {
            uploadedStream = buffer.getInputStream();
            uploadedFileName = event.getFileName();
            importButton.setEnabled(true);
            showInfo("File '" + uploadedFileName + "' ready. Configure options and click Import.");
        });
        upload.addFailedListener(event ->
            showError("Upload failed: " + event.getReason().getMessage()));
        upload.addFileRejectedListener(event ->
            showError("File rejected: " + event.getErrorMessage()));
        // Template download link
        final Anchor templateAnchor = new Anchor(createTemplateResource(), "Download sample template");
        templateAnchor.getElement().setAttribute("download", true);
        templateAnchor.addClassNames(LumoUtility.FontSize.SMALL);

        final Anchor systemInitAnchor = new Anchor(createSystemInitTemplateResource(), "Download system init template (committed)");
        systemInitAnchor.getElement().setAttribute("download", true);
        systemInitAnchor.addClassNames(LumoUtility.FontSize.SMALL);

        section.add(upload, templateAnchor, systemInitAnchor);
        return section;
    }

    private StreamResource createTemplateResource() {
        return new StreamResource("import_template.xlsx", () -> {
            try {
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                CSampleImportExcelGenerator.writeSampleWorkbook(baos);
                return new java.io.ByteArrayInputStream(baos.toByteArray());
            } catch (final Exception e) {
                LOGGER.error("Failed to generate template reason={}", e.getMessage());
                return new java.io.ByteArrayInputStream(new byte[0]);
            }
        });
    }

    private StreamResource createSystemInitTemplateResource() {
        return new StreamResource("system_init.xlsx", () -> {
            try {
                // WHY: serve the committed template so what users download matches what "DB Excel" imports.
                return excelTemplateService.openSystemInitTemplate(false);
            } catch (final Exception e) {
                LOGGER.error("Failed to open system init template reason={}", e.getMessage());
                return new java.io.ByteArrayInputStream(new byte[0]);
            }
        });
    }

    // Options section

    private Div buildOptionsSection() {
        final Div section = new Div();
        section.addClassNames(LumoUtility.Margin.Top.MEDIUM);
        final H3 optTitle = new H3("Import Options");
        optTitle.addClassNames(LumoUtility.FontSize.MEDIUM, LumoUtility.Margin.Bottom.SMALL);
        dryRunCheckbox = new Checkbox("Dry run (validate only, do not save)");
        dryRunCheckbox.setValue(false);
        rollbackOnErrorCheckbox = new Checkbox("Roll back all on any error");
        rollbackOnErrorCheckbox.setValue(false);
        skipUnknownSheetsCheckbox = new Checkbox("Skip unrecognized sheet names");
        skipUnknownSheetsCheckbox.setValue(true);
        final HorizontalLayout opts = new HorizontalLayout(dryRunCheckbox, rollbackOnErrorCheckbox, skipUnknownSheetsCheckbox);
        opts.setSpacing(true);
        section.add(optTitle, opts);
        return section;
    }

    // Action bar

    private HorizontalLayout buildActionBar() {
        importButton = new Button("Import", VaadinIcon.DATABASE.create(), e -> runImport());
        importButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        importButton.setEnabled(false);
        final Button clearButton = new Button("Clear", VaadinIcon.TRASH.create(), e -> clearResults());
        clearButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        return new HorizontalLayout(importButton, clearButton);
    }

    // Import execution

    private void runImport() {
        if (uploadedStream == null) {
            showError("No file uploaded.");
            return;
        }
        final CProject<?> project = sessionService.getActiveProject().orElse(null);
        if (project == null) {
            showError("No active project selected. Please select a project first.");
            return;
        }
        final CImportOptions options = new CImportOptions();
        options.setDryRun(dryRunCheckbox.getValue());
        options.setRollbackOnError(rollbackOnErrorCheckbox.getValue());
        options.setSkipUnknownSheets(skipUnknownSheetsCheckbox.getValue());
        importButton.setEnabled(false);
        importButton.setText("Importing…");
        try {
            final CImportResult result = importService.importExcel(uploadedStream, options, project);
            persistImportHistory(result, project);
            renderResults(result);
        } catch (final Exception e) {
            LOGGER.error("Import error reason={}", e.getMessage(), e);
            showError("Import error: " + e.getMessage());
        } finally {
            importButton.setEnabled(true);
            importButton.setText("Import");
            uploadedStream = null;
        }
    }

    private void persistImportHistory(final CImportResult result, final CProject<?> project) {
        if (project.getCompany() == null) {
            return;
        }
        try {
            final String username = sessionService.getActiveUser()
                    .map(CUser::getUsername).orElse("unknown");
            dataImportService.saveImportResult(result, project.getCompany(), uploadedFileName, username);
        } catch (final Exception e) {
            LOGGER.error("Failed to persist import history reason={}", e.getMessage());
        }
    }

    private void clearResults() {
        resultsContainer.removeAll();
        uploadedStream = null;
        uploadedFileName = null;
        importButton.setEnabled(false);
    }

    // Results rendering

    private void renderResults(final CImportResult result) {
        resultsContainer.removeAll();
        resultsContainer.add(buildSummaryBanner(result));
        if (!result.getSheetResults().isEmpty()) {
            resultsContainer.add(buildSheetAccordion(result.getSheetResults()));
        }
    }

    private Div buildSummaryBanner(final CImportResult result) {
        final Div banner = new Div();
        banner.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.BorderRadius.MEDIUM, LumoUtility.Margin.Vertical.MEDIUM);
        final boolean hasErrors = result.getTotalErrors() > 0 || result.hasGlobalError();
        if (result.isDryRun()) {
            banner.getStyle().set("background", "var(--lumo-primary-color-10pct)");
            banner.getStyle().set("border-left", "4px solid var(--lumo-primary-color)");
        } else if (hasErrors) {
            banner.getStyle().set("background", "var(--lumo-error-color-10pct)");
            banner.getStyle().set("border-left", "4px solid var(--lumo-error-color)");
        } else {
            banner.getStyle().set("background", "var(--lumo-success-color-10pct)");
            banner.getStyle().set("border-left", "4px solid var(--lumo-success-color)");
        }
        final String modeLabel = result.isDryRun() ? " [DRY RUN — no data saved]" : "";
        final String rollbackLabel = result.isRolledBack() ? " — ROLLED BACK" : "";
        final Span heading = new Span("Import Summary" + modeLabel + rollbackLabel);
        heading.addClassNames(LumoUtility.FontWeight.BOLD, LumoUtility.FontSize.LARGE);
        final HorizontalLayout stats = new HorizontalLayout();
        stats.setSpacing(true);
        stats.add(statBadge("✓ Imported", result.getTotalSuccess(), "var(--lumo-success-color)"));
        stats.add(statBadge("✗ Errors", result.getTotalErrors(), "var(--lumo-error-color)"));
        stats.add(statBadge("— Skipped", result.getTotalSkipped(), "var(--lumo-contrast-50pct)"));
        stats.add(statBadge("Sheets", result.getSheetResults().size(), "var(--lumo-primary-color)"));
        if (result.hasGlobalError()) {
            final Span errMsg = new Span("Error: " + result.getGlobalErrorMessage());
            errMsg.getStyle().set("color", "var(--lumo-error-color)");
            banner.add(heading, stats, errMsg);
        } else {
            banner.add(heading, stats);
        }
        return banner;
    }

    private Span statBadge(final String label, final long count, final String color) {
        final Span badge = new Span(label + ": " + count);
        badge.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.FontWeight.SEMIBOLD);
        badge.getStyle().set("color", color);
        badge.getStyle().set("padding", "2px 8px");
        badge.getStyle().set("border", "1px solid " + color);
        badge.getStyle().set("border-radius", "4px");
        return badge;
    }

    private Accordion buildSheetAccordion(final List<CImportSheetResult> sheets) {
        final Accordion accordion = new Accordion();
        accordion.setWidthFull();
        sheets.forEach((final CImportSheetResult sheet) -> {
            final VerticalLayout content = new VerticalLayout();
            content.setPadding(false);
            content.setSpacing(true);
            if (!sheet.isEntityTypeRecognized()) {
                content.add(new Span("⚠ Sheet '" + sheet.getSheetName() + "' was not recognized."));
            } else if (sheet.getHeaderErrorMessage() != null) {
                content.add(new Span("⚠ " + sheet.getHeaderErrorMessage()));
            } else {
                content.add(buildSheetSummary(sheet));
                if (!sheet.getRowResults().isEmpty()) {
                    content.add(buildRowResultGrid(sheet));
                }
            }
            final String panelTitle = buildSheetPanelTitle(sheet);
            accordion.add(panelTitle, content);
        });
        return accordion;
    }

    private String buildSheetPanelTitle(final CImportSheetResult sheet) {
        if (!sheet.isEntityTypeRecognized()) {
            return "⚠ " + sheet.getSheetName() + " (unrecognized)";
        }
        final String status = sheet.getErrorCount() > 0 ? "✗" : "✓";
        return status + " " + sheet.getSheetName()
            + " — " + sheet.getSuccessCount() + " ok, "
            + sheet.getErrorCount() + " errors, "
            + sheet.getSkippedCount() + " skipped";
    }

    private HorizontalLayout buildSheetSummary(final CImportSheetResult sheet) {
        final HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(true);
        row.add(new Span("Entity: " + (sheet.getEntityTypeName() != null ? sheet.getEntityTypeName() : "—")));
        row.add(new Span("Rows: " + sheet.getTotalDataRows()));
        return row;
    }

    private Grid<CImportRowResult> buildRowResultGrid(final CImportSheetResult sheet) {
        final Grid<CImportRowResult> grid = new Grid<>(CImportRowResult.class, false);
        grid.setWidthFull();
        grid.setMaxHeight("300px");
        grid.addColumn(r -> "#" + r.getRowNumber()).setHeader("Row").setWidth("60px").setFlexGrow(0);
        grid.addColumn(r -> switch (r.getStatus()) {
            case SUCCESS -> "✓ OK";
            case ERROR -> "✗ Error";
            case SKIPPED -> "— Skip";
        }).setHeader("Status").setWidth("80px").setFlexGrow(0);
        grid.addColumn(r -> r.getEntityName() != null ? r.getEntityName() : "").setHeader("Entity Name").setFlexGrow(1);
        grid.addColumn(r -> r.getErrorMessage() != null ? r.getErrorMessage() : "").setHeader("Message").setFlexGrow(2);
        grid.setItems(sheet.getRowResults().stream()
            .filter(r -> !r.isSkipped())
            .toList());
        grid.setPartNameGenerator(r -> r.isError() ? "import-row-error" : r.isSuccess() ? "import-row-ok" : null);
        return grid;
    }

    // Notification helpers

    private void showError(final String message) {
        final Notification n = Notification.show(message, 4000, Notification.Position.TOP_CENTER);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void showInfo(final String message) {
        final Notification n = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}
