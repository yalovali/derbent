package tech.derbent.api.reports.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Anchor;
// Using deprecated StreamResource - Vaadin 24 migration in progress
// TODO: Replace with StreamResourceWriter when Vaadin provides stable API
import com.vaadin.flow.server.StreamResource;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.reporting.CCSVExporter;
import tech.derbent.api.reporting.CDialogReportConfiguration;
import tech.derbent.api.reporting.CReportFieldDescriptor;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;

/** CReportHelper - Helper class for implementing CSV report functionality.
 * <p>
 * Provides static methods to generate CSV reports with field selection dialog. Used by page services and grid views for consistent reporting across
 * the application.
 * </p>
 * <p>
 * <b>Usage:</b>
 *
 * <pre>
 * // In page service actionReport():
 * List&lt;CActivity&gt; data = activityService.listByProject(currentProject);
 * CReportHelper.generateReport(data, CActivity.class);
 * </pre>
 * </p>
 * <p>
 * <b>Features:</b>
 * <ul>
 * <li>Automatic field discovery via reflection</li>
 * <li>User-friendly field selection dialog</li>
 * <li>CSV export with proper escaping</li>
 * <li>Automatic download trigger</li>
 * </ul>
 * </p>
 * Layer: Reporting (API) */
public final class CReportHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(CReportHelper.class);

	/** Generates CSV content and triggers download.
	 * @param entities       the entities to export
	 * @param selectedFields the selected field descriptors
	 * @param entityClass    the entity class
	 * @param <T>            the entity type
	 * @throws Exception if generation fails */
	private static <T extends CEntityDB<T>> void generateAndDownloadCSV(final List<T> entities, final List<CReportFieldDescriptor> selectedFields,
			final Class<T> entityClass) throws Exception {
		LOGGER.debug("Generating CSV with {} fields for {} records", selectedFields.size(), entities.size());
		// Generate filename base from entity class
		final String baseFileName = entityClass.getSimpleName().substring(1).toLowerCase(); // Remove 'C' prefix
		// Generate CSV as StreamResource
		final StreamResource csv = CCSVExporter.exportToCSV(entities, selectedFields, baseFileName);
		// Trigger download
		triggerDownload(csv);
		// Show success notification
		CNotificationService.showSuccess(String.format("Exporting %d records to CSV", entities.size()));
		LOGGER.info("CSV report generated successfully: {} records, {} fields", entities.size(), selectedFields.size());
	}

	/** Opens the field selection dialog and generates a CSV report for the given entities.
	 * <p>
	 * This is the main entry point for grid-based reporting. Discovers all available fields, lets user select which to export, then generates and
	 * downloads CSV file.
	 * </p>
	 * @param entities    the list of entities to export
	 * @param entityClass the entity class
	 * @param <T>         the entity type
	 * @throws Exception if report generation fails */
	public static <T extends CEntityDB<T>> void generateReport(final List<T> entities, final Class<T> entityClass) throws Exception {
		Check.notNull(entities, "Entities list cannot be null");
		Check.notNull(entityClass, "Entity class cannot be null");
		if (entities.isEmpty()) {
			CNotificationService.showWarning("No data available to export");
			LOGGER.warn("Attempted to generate report with empty entity list");
			return;
		}
		LOGGER.info("Generating report for {} with {} records", entityClass.getSimpleName(), entities.size());
		try {
			// Discover all available fields
			final List<CReportFieldDescriptor> allFields = CReportFieldDescriptor.discoverFields(entityClass);
			if (allFields.isEmpty()) {
				CNotificationService.showWarning("No exportable fields found for this entity");
				LOGGER.warn("No fields discovered for entity: {}", entityClass.getSimpleName());
				return;
			}
			// Open field selection dialog
			final CDialogReportConfiguration dialog = new CDialogReportConfiguration(allFields, selectedFields -> {
				try {
					generateAndDownloadCSV(entities, selectedFields, entityClass);
				} catch (final Exception e) {
					LOGGER.error("Error generating CSV report", e);
					CNotificationService.showException("Failed to generate report", e);
				}
			});
			dialog.open();
		} catch (final Exception e) {
			LOGGER.error("Error preparing report for {}", entityClass.getSimpleName(), e);
			CNotificationService.showException("Failed to prepare report", e);
			throw e;
		}
	}

	/** Triggers file download in the browser. Creates a temporary Anchor element and simulates click.
	 * @param streamResource the CSV stream resource */
	private static void triggerDownload(final StreamResource streamResource) {
		final UI ui = UI.getCurrent();
		if (ui == null) {
			throw new IllegalStateException("No UI context available for download");
		}
		// Create download link using deprecated Anchor constructor (Vaadin 24 migration pending)
		final Anchor downloadLink = new Anchor(streamResource, "");
		downloadLink.getElement().setAttribute("download", true);
		downloadLink.setId("csv-download-link");
		// Add to UI and trigger click
		ui.add(downloadLink);
		ui.getPage().executeJs("document.getElementById('csv-download-link').click()");
		// Remove after short delay
		ui.getPage().executeJs("setTimeout(function() { " + "  var link = document.getElementById('csv-download-link'); "
				+ "  if (link) link.remove(); " + "}, 1000)");
		LOGGER.debug("Download triggered for CSV file");
	}

	private CReportHelper() {
		// Utility class - prevent instantiation
	}
}
