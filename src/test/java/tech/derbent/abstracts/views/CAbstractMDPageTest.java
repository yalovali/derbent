package tech.derbent.abstracts.views;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.services.CAbstractService;

/**
 * Test class for CAbstractMDPage to verify the details view tab functionality.
 */
@ExtendWith(MockitoExtension.class)
class CAbstractMDPageTest {

	/**
	 * Test entity for testing purposes.
	 */
	private static class TestEntity extends CEntityDB {
		// Test implementation
	}

	/**
	 * Test implementation of CAbstractMDPage for testing purposes.
	 */
	private static class TestMDPage extends CAbstractMDPage<TestEntity> {

		private static final long serialVersionUID = 1L;

		public TestMDPage(final CAbstractService<TestEntity> entityService) {
			super(TestEntity.class, entityService);
		}

		@Override
		protected void createDetailsLayout() {
			// Test implementation - empty
		}

		@Override
		protected void createGridForEntity() {
			// Test implementation - empty
		}

		@Override
		protected String getEntityRouteIdField() { return "test_id"; }

		@Override
		protected String getEntityRouteTemplateEdit() { return "test/%s/edit"; }

		@Override
		protected void initPage() {
			// Test implementation - empty
		}

		@Override
		protected TestEntity newEntity() {
			return new TestEntity();
		}

		@Override
		protected void setupToolbar() {
			// Test implementation - empty
		}
	}

	@Mock
	private CAbstractService<TestEntity> mockEntityService;
	private TestMDPage testPage;

	@BeforeEach
	void setUp() {
		testPage = new TestMDPage(mockEntityService);
	}

	@Test
	void testCreateButtonLayoutIsEmptyByDefault() {
		// Arrange
		final Div testLayout = new Div();
		// Act
		testPage.createButtonLayout(testLayout);
		// Assert The new implementation should not add any buttons to the main layout
		assertEquals(0, testLayout.getChildren().count(),
			"createButtonLayout should not add buttons to main layout anymore");
	}

	@Test
	void testCreateDetailsTabButtonLayout() {
		// Act
		final HorizontalLayout buttonLayout = testPage.createDetailsTabButtonLayout();
		// Assert
		assertNotNull(buttonLayout, "Button layout should not be null");
		assertTrue(buttonLayout.getClassName().contains("details-tab-button-layout"),
			"Button layout should have correct CSS class");
		// Verify that buttons are present (Save, Cancel, Delete)
		final long buttonCount =
			buttonLayout.getChildren().filter(CButton.class::isInstance).count();
		assertEquals(3, buttonCount,
			"Should have exactly 3 buttons (Save, Cancel, Delete)");
	}

	@Test
	void testCreateDetailsTabLeftContent() {
		// Act
		final Div leftContent = testPage.createDetailsTabLeftContent();
		// Assert
		assertNotNull(leftContent, "Left content should not be null");
		assertEquals("Details", leftContent.getText(),
			"Default left content should be 'Details'");
		assertTrue(leftContent.getClassName().contains("details-tab-label"),
			"Left content should have correct CSS class");
	}

	@Test
	void testDetailsTabLayoutContainsButtons() {
		// Act
		testPage.createDetailsTabLayout();
		// Assert
		final Div detailsTabLayout = testPage.getDetailsTabLayout();
		assertNotNull(detailsTabLayout, "Details tab layout should not be null");
		// Verify that the tab layout has content
		assertTrue(detailsTabLayout.getChildren().count() > 0,
			"Details tab layout should have content");
		// Find the HorizontalLayout that should contain the tab content
		final HorizontalLayout tabContent =
			detailsTabLayout.getChildren().filter(HorizontalLayout.class::isInstance)
				.map(HorizontalLayout.class::cast).findFirst().orElse(null);
		assertNotNull(tabContent, "Tab content layout should exist");
		assertTrue(tabContent.getClassName().contains("details-tab-content"),
			"Tab content should have correct CSS class");
	}
}