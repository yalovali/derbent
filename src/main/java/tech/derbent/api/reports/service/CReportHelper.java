package tech.derbent.api.reports.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.reports.dialog.CDialogReportFieldSelection;
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;

/**
 * Helper class for implementing report functionality in page services.
 * Provides static methods to open field selection dialog and generate CSV reports.
 */
public class CReportHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CReportHelper.class);
    private static final DateTimeFormatter FILENAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * Opens the field selection dialog and generates a CSV report for the given entities.
     * 
     * @param entities the list of entities to export
     * @param entityClass the entity class
     * @throws Exception if report generation fails
     */
    public static <T extends CEntityDB<T>> void generateReport(final List<T> entities, final Class<T> entityClass) throws Exception {
        Check.notNull(entities, "Entities list cannot be null");
        Check.notNull(entityClass, "Entity class cannot be null");

        if (entities.isEmpty()) {
            CNotificationService.showWarning("No data available to export");
            LOGGER.warn("Attempted to generate report with empty entity list");
            return;
        }

        // Open field selection dialog
        final CDialogReportFieldSelection dialog = new CDialogReportFieldSelection(entityClass, selectedFields -> {
            try {
                // Generate CSV
                generateAndDownloadCSV(entities, selectedFields, entityClass);
            } catch (final Exception e) {
                LOGGER.error("Error generating CSV report: {}", e.getMessage(), e);
                CNotificationService.showException("Error generating report", e);
            }
        });

        dialog.open();
    }

    /**
     * Generates CSV content and triggers download.
     */
    private static <T extends CEntityDB<T>> void generateAndDownloadCSV(final List<T> entities,
            final List<EntityFieldInfo> selectedFields, final Class<T> entityClass) throws Exception {

        // Get report service
        final CReportService reportService = CSpringContext.getBean(CReportService.class);
        Check.notNull(reportService, "Report service not available");

        // Generate CSV content
        final String csvContent = reportService.generateCSV(entities, selectedFields, entityClass);

        // Generate filename
        final String entityTitle = CEntityRegistry.getEntityTitlePlural(entityClass);
        final String entityName = entityTitle != null ? entityTitle : entityClass.getSimpleName();
        final String timestamp = LocalDateTime.now().format(FILENAME_FORMATTER);
        final String fileName = entityName + "_" + timestamp + ".csv";

        // Trigger download
        reportService.downloadCSV(csvContent, fileName);

        // Show success notification
        CNotificationService.showSuccess("Report generated: " + entities.size() + " records exported");
        LOGGER.info("Generated CSV report: {} with {} records", fileName, entities.size());
    }
}
