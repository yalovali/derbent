package tech.derbent.api.imports;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import tech.derbent.api.imports.service.CSampleImportExcelGenerator;

/** Generates the sample Excel template file into src/test/resources/import/. */
class CSampleExcelFileTest {

    @Test
    void generateSampleExcelFile() throws IOException {
        final File dir = new File("src/test/resources/import");
        dir.mkdirs();
        try (final FileOutputStream out = new FileOutputStream(new File(dir, "sample_import.xlsx"))) {
            CSampleImportExcelGenerator.writeSampleWorkbook(out);
        }
        System.out.println("Generated: " + dir.getAbsolutePath() + "/sample_import.xlsx");
    }
}
