package tech.derbent.ui;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test to validate that the detailed tab has the red background color CSS styling.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class DetailedTabCSSTest {

    @Test
    public void testDetailedTabRedBackgroundCSS() throws IOException {
        // Path to the CSS file
        Path cssPath = Paths.get("src/main/frontend/themes/default/styles.css");
        
        // Verify that the CSS file exists
        assertTrue(Files.exists(cssPath), "CSS file should exist at src/main/frontend/themes/default/styles.css");
        
        // Read the CSS file content
        String cssContent = Files.readString(cssPath);
        
        // Verify that the detailed tab CSS rule exists
        assertTrue(cssContent.contains(".details-tab-layout"), 
            "CSS should contain the .details-tab-layout selector");
        
        // Verify that the red background color is applied
        assertTrue(cssContent.contains("background-color: red"), 
            "CSS should contain red background color for .details-tab-layout");
        
        // Additional verification - check that the rule is properly formatted
        assertTrue(cssContent.contains(".details-tab-layout {") || 
                  cssContent.contains(".details-tab-layout{"), 
            "CSS should have proper selector formatting");
    }
}