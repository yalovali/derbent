package tech.derbent.api.imports;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import tech.derbent.api.imports.service.CSystemInitExcelGenerator;

/** Generates the system init Excel template file into src/test/resources/import/. */
class CSystemInitExcelFileTest {

    @Test
    void generateSystemInitExcelFile() throws IOException {
        final File dir = new File("src/test/resources/import");
        dir.mkdirs();
        try (final FileOutputStream out = new FileOutputStream(new File(dir, "system_init.xlsx"))) {
            CSystemInitExcelGenerator.writeSystemInitWorkbook(out);
        }
        System.out.println("Generated: " + dir.getAbsolutePath() + "/system_init.xlsx");
    }
}
