package tech.derbent.api.imports.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class CExcelImportServiceCellValueTest {

	@Test
	void getCellStringValue_readsDateFormattedCellsAsIsoDate() throws Exception {
		try (Workbook wb = new XSSFWorkbook()) {
			final var sheet = wb.createSheet("s");
			final var row = sheet.createRow(0);
			final var cell = row.createCell(0);
			final var dateStyle = wb.createCellStyle();
			dateStyle.setDataFormat(wb.createDataFormat().getFormat("yyyy-mm-dd"));
			cell.setCellStyle(dateStyle);
			cell.setCellValue(java.sql.Date.valueOf(LocalDate.of(2026, 1, 15)));

			final var evaluator = wb.getCreationHelper().createFormulaEvaluator();
			assertEquals("2026-01-15", CExcelImportService.getCellStringValue(cell, evaluator));
		}
	}

	@Test
	void getCellStringValue_evaluatesFormulaCells() throws Exception {
		try (Workbook wb = new XSSFWorkbook()) {
			final var sheet = wb.createSheet("s");
			final var row = sheet.createRow(0);
			final var cell = row.createCell(0);
			cell.setCellFormula("4+4");

			final var evaluator = wb.getCreationHelper().createFormulaEvaluator();
			assertEquals("8", CExcelImportService.getCellStringValue(cell, evaluator));
		}
	}
}
