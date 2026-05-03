package automated_tests.tech.derbent.ui.automation.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import automated_tests.tech.derbent.ui.automation.CBaseUITest;
import tech.derbent.Application;
import tech.derbent.api.imports.service.CSampleImportExcelGenerator;
import tech.derbent.api.imports.service.CExcelTemplateService;

// KEYWORDS: Import, Excel, Samples, Playwright
@SpringBootTest (webEnvironment = WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestPropertySource (properties = {
		"spring.profiles.active=derbent", "server.port=0", "spring.datasource.url=jdbc:h2:mem:testdb",
		"spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName ("📥 Import Data Excel (complex samples)")
public class CImportDataExcelPlaywrightTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CImportDataExcelPlaywrightTest.class);

	private static void addErrorRows(final Workbook wb) {
		final Sheet activity = wb.getSheet("Activity");
		if (activity != null) {
			int r = activity.getLastRowNum() + 1;
			// Missing required name
			Row row = activity.createRow(r++);
			row.createCell(0).setCellValue("");
			row.createCell(1).setCellValue("This row should error because name is blank");
			row.createCell(2).setCellValue("");
			row.createCell(3).setCellValue("Development");
			row.createCell(4).setCellValue("Medium");
			row.createCell(6).setCellValue("2025-06-30"); // Due Date
			row.createCell(10).setCellValue("1"); // Estimated Hours
			row.createCell(11).setCellValue("");
			// Unknown status (relation resolution error)
			row = activity.createRow(r++);
			row.createCell(0).setCellValue("Row with unknown status");
			row.createCell(1).setCellValue("This row should error because status does not exist");
			row.createCell(2).setCellValue("__UNKNOWN_STATUS__");
			row.createCell(3).setCellValue("Testing");
			row.createCell(4).setCellValue("Low");
			row.createCell(6).setCellValue("2025-06-30");
			row.createCell(10).setCellValue("2");
			row.createCell(11).setCellValue("");
		}
		final Sheet issue = wb.getSheet("Issue");
		if (issue != null) {
			final int r = issue.getLastRowNum() + 1;
			final Row row = issue.createRow(r);
			row.createCell(0).setCellValue("Row with unknown issue type");
			row.createCell(1).setCellValue("This row should error because issue type does not exist");
			row.createCell(2).setCellValue("");
			row.createCell(3).setCellValue("__UNKNOWN_ISSUE_TYPE__");
			row.createCell(4).setCellValue("2025-06-30");
		}
		// Unknown sheet to test registry handling
		final Sheet unknown = wb.createSheet("TotallyUnknownSheet");
		unknown.createRow(0).createCell(0).setCellValue("name");
		unknown.createRow(1).createCell(0).setCellValue("Some data");
	}

	@Test
	@DisplayName ("✅ Import committed system init workbook (non-dry-run)")
	void testImportCommittedSystemInitWorkbook() throws Exception {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping UI test");
			Assumptions.assumeTrue(false, "Browser not available");
			return;
		}
		final Path template = Path.of("src/main/resources/" + CExcelTemplateService.SYSTEM_INIT_TEMPLATE_RESOURCE_PATH);
		if (!Files.exists(template)) {
			LOGGER.warn("⚠️ System init template not found at {} - skipping", template);
			Assumptions.assumeTrue(false, "Template not found");
			return;
		}
		loginToApplication();
		assertTrue(navigateByMenuSearch("Import Data"), "Could not navigate to Import Data");
		page.locator("vaadin-upload input[type='file']").setInputFiles(template);
		final Locator dryRun = page.getByLabel("Dry run (validate only, do not save)");
		if (dryRun.isChecked()) {
			dryRun.uncheck();
		}
		final Locator autoCreate = page.getByLabel("Auto-create missing types/statuses (opt-in)");
		if (!autoCreate.isChecked()) {
			autoCreate.check();
		}
		final Locator importButton =
				page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Import").setExact(true));
		waitForButtonEnabled(importButton);
		importButton.click();
		// WHY: system_init.xlsx keeps growing (many sheets + relations); give the UI enough time to finish the server-side import.
		page.waitForSelector("text=Import Summary", new Page.WaitForSelectorOptions().setTimeout(180000));
		page.waitForSelector("text=Errors: 0", new Page.WaitForSelectorOptions().setTimeout(180000));
		// WHY: sheet accordion text can be shadow-dom dependent; validate via summary counters instead.
		page.waitForSelector("text=Sheets", new Page.WaitForSelectorOptions().setTimeout(180000));
		assertTrue(page.locator("text=Errors: 0").count() > 0, "Expected zero errors");
		assertTrue(page.locator("text=Imported").count() > 0, "Expected imported rows");
		assertTrue(page.locator("text=Sheets").count() > 0, "Expected sheet summary counter");
		takeScreenshot("import-data-system-init", false);
	}

	private static Path writeComplexImportWorkbook() throws Exception {
		final Path file = Files.createTempFile("import-complex-", ".xlsx");
		try (Workbook wb = CSampleImportExcelGenerator.createSampleWorkbook()) {
			addErrorRows(wb);
			try (var out = Files.newOutputStream(file)) {
				wb.write(out);
			}
		}
		return file;
	}

	@Test
	@DisplayName ("✅ Upload complex Excel and verify import results")
	void testImportDataExcelComplexSample() throws Exception {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping UI test");
			Assumptions.assumeTrue(false, "Browser not available");
			return;
		}
		loginToApplication();
		assertTrue(navigateByMenuSearch("Import Data"), "Could not navigate to Import Data");
		final Path sampleFile = writeComplexImportWorkbook();
		page.locator("vaadin-upload input[type='file']").setInputFiles(sampleFile);
		final Locator dryRun = page.getByLabel("Dry run (validate only, do not save)");
		if (!dryRun.isChecked()) {
			dryRun.check();
		}
		// Ensure unknown sheet is reported
		final Locator skipUnknown = page.getByLabel("Skip unrecognized sheet names");
		if (skipUnknown.isChecked()) {
			skipUnknown.uncheck();
		}
		final Locator importButton =
				page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Import").setExact(true));
		waitForButtonEnabled(importButton);
		importButton.click();
		page.waitForSelector("text=Import Summary", new Page.WaitForSelectorOptions().setTimeout(45000));
		page.waitForSelector("text=Errors:", new Page.WaitForSelectorOptions().setTimeout(45000));
		// WHY: error row grids are rendered lazily by Vaadin; wait for at least one relation resolution message.
		page.waitForSelector("text=not found", new Page.WaitForSelectorOptions().setTimeout(45000));
		// Should show at least one error and the unrecognized sheet panel.
		assertTrue(page.locator("text=Errors:").count() > 0, "Expected Errors badge");
		assertTrue(page.locator("text=unrecognized").count() > 0, "Expected unrecognized sheet result");
		assertTrue(page.locator("text=not found").count() > 0, "Expected relation resolution errors");
		takeScreenshot("import-data-complex-sample", false);
	}
}
