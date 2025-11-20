package tech.derbent.api.views;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.formlayout.FormLayout;

import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailLines;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CEntityFieldService;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;

/**
 * Unit tests for CDetailsBuilder hierarchical section/tab functionality.
 * Tests the stack-based approach for nesting sections and tabs up to 3 levels deep.
 */
class CDetailsBuilderHierarchicalTest {

	@Mock
	private ISessionService mockSessionService;
	
	@Mock
	private CDetailSectionService mockDetailSectionService;
	
	@Mock
	private ApplicationContext mockApplicationContext;
	
	@Mock
	private IContentOwner mockContentOwner;
	
	@Mock
	private CEnhancedBinder<?> mockBinder;
	
	private CDetailsBuilder detailsBuilder;
	private CUser testUser;
	private CProject testProject;
	
	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.openMocks(this);
		
		// Setup test user
		testUser = new CUser();
		testUser.setLogin("testuser");
		testUser.setAttributeDisplaySectionsAsTabs(false);
		
		// Setup test project
		testProject = new CProject();
		testProject.setName("Test Project");
		
		// Setup mock session service
		when(mockSessionService.getActiveUser()).thenReturn(Optional.of(testUser));
		
		// Setup mock application context
		when(mockApplicationContext.getBean(CDetailSectionService.class)).thenReturn(mockDetailSectionService);
		
		// Create details builder
		detailsBuilder = new CDetailsBuilder(mockSessionService);
		detailsBuilder.setApplicationContext(mockApplicationContext);
	}
	
	/**
	 * Test creating a simple flat structure with one section
	 */
	@Test
	void testSimpleSingleSection() throws Exception {
		try (MockedStatic<CEntityRegistry> mockedRegistry = mockStatic(CEntityRegistry.class)) {
			// Mock the entity registry
			mockedRegistry.when(() -> CEntityRegistry.getEntityClass("CActivity")).thenReturn(CActivity.class);
			
			// Given: A detail section with one section and one field
			CDetailSection screen = createTestScreen("CActivity");
			List<CDetailLines> lines = new ArrayList<>();
			
			// Add section
			lines.add(createSectionLine("Basic Info", CEntityFieldService.CONTAINER_TYPE_SECTION));
			lines.add(createFieldLine("name"));
			
			screen.setScreenLines(lines);
			
			// When: Building details
			when(mockDetailSectionService.findByIdWithScreenLines(any())).thenReturn(screen);
			HasComponents result = detailsBuilder.buildDetails(mockContentOwner, screen, mockBinder, null);
			
			// Then: Result should not be null
			assertNotNull(result, "Result should not be null");
			assertTrue(result instanceof FormLayout, "Result should be FormLayout");
		}
	}
	
	/**
	 * Test creating a hierarchy: Section -> Tab
	 */
	@Test
	void testSectionContainingTab() throws Exception {
		try (MockedStatic<CEntityRegistry> mockedRegistry = mockStatic(CEntityRegistry.class)) {
			mockedRegistry.when(() -> CEntityRegistry.getEntityClass("CActivity")).thenReturn(CActivity.class);
			
			// Given: A detail section with section containing a tab
			CDetailSection screen = createTestScreen("CActivity");
			List<CDetailLines> lines = new ArrayList<>();
			
			// Section1 (accordion)
			lines.add(createSectionLine("Section1", CEntityFieldService.CONTAINER_TYPE_SECTION));
			lines.add(createFieldLine("name"));
			
			// Tab1 inside Section1
			lines.add(createSectionLine("Tab1", CEntityFieldService.CONTAINER_TYPE_TAB));
			lines.add(createFieldLine("description"));
			lines.add(createSectionEndLine());
			
			// Close Section1
			lines.add(createSectionEndLine());
			
			screen.setScreenLines(lines);
			
			// When: Building details
			when(mockDetailSectionService.findByIdWithScreenLines(any())).thenReturn(screen);
			HasComponents result = detailsBuilder.buildDetails(mockContentOwner, screen, mockBinder, null);
			
			// Then: Result should be created successfully
			assertNotNull(result, "Result should not be null");
		}
	}
	
	/**
	 * Test creating a hierarchy: Tab -> Section
	 */
	@Test
	void testTabContainingSection() throws Exception {
		try (MockedStatic<CEntityRegistry> mockedRegistry = mockStatic(CEntityRegistry.class)) {
			mockedRegistry.when(() -> CEntityRegistry.getEntityClass("CActivity")).thenReturn(CActivity.class);
			
			// Given: A detail section with tab containing a section
			CDetailSection screen = createTestScreen("CActivity");
			List<CDetailLines> lines = new ArrayList<>();
			
			// Tab1
			lines.add(createSectionLine("Tab1", CEntityFieldService.CONTAINER_TYPE_TAB));
			lines.add(createFieldLine("name"));
			
			// Section1 inside Tab1
			lines.add(createSectionLine("Section1", CEntityFieldService.CONTAINER_TYPE_SECTION));
			lines.add(createFieldLine("description"));
			lines.add(createSectionEndLine());
			
			// Close Tab1
			lines.add(createSectionEndLine());
			
			screen.setScreenLines(lines);
			
			// When: Building details
			when(mockDetailSectionService.findByIdWithScreenLines(any())).thenReturn(screen);
			HasComponents result = detailsBuilder.buildDetails(mockContentOwner, screen, mockBinder, null);
			
			// Then: Result should be created successfully
			assertNotNull(result, "Result should not be null");
		}
	}
	
	/**
	 * Test creating a 3-level hierarchy: Section -> Tab -> Section
	 */
	@Test
	void testThreeLevelHierarchy() throws Exception {
		try (MockedStatic<CEntityRegistry> mockedRegistry = mockStatic(CEntityRegistry.class)) {
			mockedRegistry.when(() -> CEntityRegistry.getEntityClass("CActivity")).thenReturn(CActivity.class);
			
			// Given: A detail section with 3 levels of nesting
			CDetailSection screen = createTestScreen("CActivity");
			List<CDetailLines> lines = new ArrayList<>();
			
			// Level 1: Section1
			lines.add(createSectionLine("Section1", CEntityFieldService.CONTAINER_TYPE_SECTION));
			lines.add(createFieldLine("name"));
			
			// Level 2: Tab1 inside Section1
			lines.add(createSectionLine("Tab1", CEntityFieldService.CONTAINER_TYPE_TAB));
			
			// Level 3: Section2 inside Tab1
			lines.add(createSectionLine("Section2", CEntityFieldService.CONTAINER_TYPE_SECTION));
			lines.add(createFieldLine("description"));
			lines.add(createSectionEndLine()); // Close Section2
			
			lines.add(createSectionEndLine()); // Close Tab1
			lines.add(createSectionEndLine()); // Close Section1
			
			screen.setScreenLines(lines);
			
			// When: Building details
			when(mockDetailSectionService.findByIdWithScreenLines(any())).thenReturn(screen);
			HasComponents result = detailsBuilder.buildDetails(mockContentOwner, screen, mockBinder, null);
			
			// Then: Result should be created successfully
			assertNotNull(result, "Result should not be null");
		}
	}
	
	/**
	 * Test that exceeding max nesting level (3) logs an error but doesn't crash
	 */
	@Test
	void testMaxNestingLevelExceeded() throws Exception {
		try (MockedStatic<CEntityRegistry> mockedRegistry = mockStatic(CEntityRegistry.class)) {
			mockedRegistry.when(() -> CEntityRegistry.getEntityClass("CActivity")).thenReturn(CActivity.class);
			
			// Given: A detail section with 4 levels of nesting (exceeds max)
			CDetailSection screen = createTestScreen("CActivity");
			List<CDetailLines> lines = new ArrayList<>();
			
			// Level 1: Section1
			lines.add(createSectionLine("Section1", CEntityFieldService.CONTAINER_TYPE_SECTION));
			
			// Level 2: Tab1
			lines.add(createSectionLine("Tab1", CEntityFieldService.CONTAINER_TYPE_TAB));
			
			// Level 3: Section2
			lines.add(createSectionLine("Section2", CEntityFieldService.CONTAINER_TYPE_SECTION));
			
			// Level 4: Tab2 (should be skipped)
			lines.add(createSectionLine("Tab2", CEntityFieldService.CONTAINER_TYPE_TAB));
			lines.add(createFieldLine("description"));
			
			screen.setScreenLines(lines);
			
			// When: Building details
			when(mockDetailSectionService.findByIdWithScreenLines(any())).thenReturn(screen);
			HasComponents result = detailsBuilder.buildDetails(mockContentOwner, screen, mockBinder, null);
			
			// Then: Result should be created (4th level skipped)
			assertNotNull(result, "Result should not be null even with nesting overflow");
		}
	}
	
	/**
	 * Test multiple sections at the same level
	 */
	@Test
	void testMultipleSectionsAtSameLevel() throws Exception {
		try (MockedStatic<CEntityRegistry> mockedRegistry = mockStatic(CEntityRegistry.class)) {
			mockedRegistry.when(() -> CEntityRegistry.getEntityClass("CActivity")).thenReturn(CActivity.class);
			
			// Given: A detail section with multiple sections at root level
			CDetailSection screen = createTestScreen("CActivity");
			List<CDetailLines> lines = new ArrayList<>();
			
			// Section1
			lines.add(createSectionLine("Section1", CEntityFieldService.CONTAINER_TYPE_SECTION));
			lines.add(createFieldLine("name"));
			lines.add(createSectionEndLine());
			
			// Section2
			lines.add(createSectionLine("Section2", CEntityFieldService.CONTAINER_TYPE_SECTION));
			lines.add(createFieldLine("description"));
			lines.add(createSectionEndLine());
			
			screen.setScreenLines(lines);
			
			// When: Building details
			when(mockDetailSectionService.findByIdWithScreenLines(any())).thenReturn(screen);
			HasComponents result = detailsBuilder.buildDetails(mockContentOwner, screen, mockBinder, null);
			
			// Then: Result should be created successfully
			assertNotNull(result, "Result should not be null");
		}
	}
	
	/**
	 * Test the example from the problem statement:
	 * Section
	 *   Tab (inside section)
	 *     item1, item2
	 *   Section (another section inside section1, below tab)
	 *     item3, item4
	 */
	@Test
	void testProblemStatementExample() throws Exception {
		try (MockedStatic<CEntityRegistry> mockedRegistry = mockStatic(CEntityRegistry.class)) {
			mockedRegistry.when(() -> CEntityRegistry.getEntityClass("CActivity")).thenReturn(CActivity.class);
			
			// Given: The hierarchy from the problem statement
			CDetailSection screen = createTestScreen("CActivity");
			List<CDetailLines> lines = new ArrayList<>();
			
			// Section start
			lines.add(createSectionLine("MainSection", CEntityFieldService.CONTAINER_TYPE_SECTION));
			
			// Section as a tab (a tab inside section)
			lines.add(createSectionLine("TabInSection", CEntityFieldService.CONTAINER_TYPE_TAB));
			lines.add(createFieldLine("name")); // item1
			lines.add(createFieldLine("description")); // item2
			lines.add(createSectionEndLine()); // end of tab
			
			// Another section inside section1, below tab
			lines.add(createSectionLine("SubSection", CEntityFieldService.CONTAINER_TYPE_SECTION));
			lines.add(createFieldLine("name")); // item3 (use fields that exist)
			lines.add(createFieldLine("description")); // item4
			lines.add(createSectionEndLine()); // end of subsection
			
			lines.add(createSectionEndLine()); // end of main section
			
			screen.setScreenLines(lines);
			
			// When: Building details
			when(mockDetailSectionService.findByIdWithScreenLines(any())).thenReturn(screen);
			HasComponents result = detailsBuilder.buildDetails(mockContentOwner, screen, mockBinder, null);
			
			// Then: Result should be created successfully
			assertNotNull(result, "Result should not be null");
		}
	}
	
	// Helper methods
	
	private CDetailSection createTestScreen(String entityType) {
		CDetailSection screen = new CDetailSection("Test Screen", testProject);
		screen.setEntityType(entityType);
		return screen;
	}
	
	private CDetailLines createSectionLine(String sectionName, String containerType) {
		CDetailLines line = CDetailLinesService.createSection(sectionName, containerType);
		line.setLineOrder(100); // Arbitrary order
		return line;
	}
	
	private CDetailLines createFieldLine(String fieldName) {
		CDetailLines line = new CDetailLines();
		line.setRelationFieldName(CEntityFieldService.THIS_CLASS);
		line.setProperty(fieldName);
		line.setFieldCaption(fieldName);
		line.setLineOrder(100);
		return line;
	}
	
	private CDetailLines createSectionEndLine() {
		return CDetailLinesService.createSectionEnd();
	}
}
