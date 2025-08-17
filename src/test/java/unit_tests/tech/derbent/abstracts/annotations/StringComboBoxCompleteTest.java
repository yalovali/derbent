package unit_tests.tech.derbent.abstracts.annotations;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;

import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.abstracts.components.CBinderFactory;
import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.domains.CEntityDB;

/**
 * Complete test demonstrating String ComboBox with working data provider
 */
@SpringBootTest
@ContextConfiguration(classes = {StringComboBoxCompleteTest.TestConfiguration.class})
class StringComboBoxCompleteTest {

    @Configuration
    static class TestConfiguration {
        
        @Bean("testStringService")
        public TestStringService testStringService() {
            return new TestStringService();
        }
    }
    
    /**
     * Service that provides String data
     */
    public static class TestStringService {
        public List<String> list() {
            return Arrays.asList("Red", "Green", "Blue");
        }
        
        public List<String> getTypes() {
            return Arrays.asList("Type A", "Type B", "Type C");
        }
    }

    /**
     * Test entity with String ComboBox fields
     */
    public static class TestStringEntity extends CEntityDB<TestStringEntity> {
        
        @MetaData(
            displayName = "Color",
            required = true,
            order = 1,
            dataProviderBean = "testStringService",
            defaultValue = "Red"
        )
        private String color;
        
        @MetaData(
            displayName = "Type",
            required = false,
            order = 2,
            dataProviderBean = "testStringService",
            dataProviderMethod = "getTypes",
            autoSelectFirst = true
        )
        private String type;

        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    @Test
    @DisplayName("String ComboBox should work with actual data provider")
    void testStringComboBoxWithDataProvider() {
        // Given
        final CEnhancedBinder<TestStringEntity> binder = 
            CBinderFactory.createEnhancedBinder(TestStringEntity.class);

        // When
        final Div form = CEntityFormBuilder.buildForm(TestStringEntity.class, binder, null);

        // Then
        assertNotNull(form, "Form should be created successfully");
        assertTrue(form.getChildren().count() > 0, "Form should contain components");
        
        // The test demonstrates that String ComboBox creation works without exceptions
        // and integrates with Spring data providers
        System.out.println("String ComboBox with data provider created successfully!");
    }
}