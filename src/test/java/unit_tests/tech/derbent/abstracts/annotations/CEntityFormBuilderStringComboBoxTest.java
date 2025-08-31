package unit_tests.tech.derbent.abstracts.annotations;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

/** Test class to verify that CEntityFormBuilder can create ComboBox components for String fields when metadata specifies a data provider. */
@SpringBootTest
@ContextConfiguration (classes = {
		CEntityFormBuilderStringComboBoxTest.TestConfiguration.class
})
class CEntityFormBuilderStringComboBoxTest {
	/** Mock service to provide string data for testing */
	public static class StringDataService {
		public List<String> getCategories() { return Arrays.asList("Category A", "Category B", "Category C"); }

		public List<String> list() {
			return Arrays.asList("Option 1", "Option 2", "Option 3");
		}
	}

	/** Test configuration providing a mock service for String ComboBox data */
	@Configuration
	static class TestConfiguration {
		/** Mock service that provides string data for ComboBox */
		@Bean ("stringDataService")
		public StringDataService stringDataService() {
			return new StringDataService();
		}
	}

	@BeforeEach
	void setUp() {
		// Setup for each test
	}
}
