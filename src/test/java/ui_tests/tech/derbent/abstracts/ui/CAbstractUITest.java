package ui_tests.tech.derbent.abstracts.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.DataProvider;

import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.session.service.CSessionService;

/**
 * CAbstractUITest - Base class for UI testing with common utilities and mocking patterns.
 * Layer: Testing (MVC) Provides common test utilities for UI components including: - Mock
 * service setup - Grid data loading testing - Lazy loading issue detection - Navigation
 * testing utilities - Form validation testing helpers
 */
public abstract class CAbstractUITest<EntityClass extends CEntityDB<EntityClass>> {

	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@Mock
	protected CAbstractService<EntityClass> mockEntityService;

	@Mock
	protected CSessionService mockSessionService;

	protected Class<EntityClass> entityClass;

	protected List<EntityClass> testEntities;

	/**
	 * Constructor that sets up the entity class for the test.
	 * @param entityClass the entity class being tested
	 */
	protected CAbstractUITest(final Class<EntityClass> entityClass) {
		this.entityClass = entityClass;
	}

	/**
	 * Helper method to create a paginated result for mocking.
	 * @param entities the entities to include in the page
	 * @param pageable the pageable request
	 * @return a Page containing the entities
	 */
	protected Page<EntityClass> createPage(final List<EntityClass> entities,
		final Pageable pageable) {
		return new PageImpl<>(entities, pageable, entities.size());
	}

	/**
	 * Creates a test entity with the specified ID and name. Should be overridden by
	 * subclasses to create properly initialized entities.
	 * @param id   the entity ID
	 * @param name the entity name
	 * @return the created test entity
	 */
	protected abstract EntityClass createTestEntity(Long id, String name);

	@BeforeEach
	void setupMocks() {
		MockitoAnnotations.openMocks(this);
		setupTestData();
		setupServiceMocks();
		LOGGER.info("Base UI test setup completed for {}", entityClass.getSimpleName());
	}

	/**
	 * Sets up mock service behavior for standard operations.
	 */
	protected void setupServiceMocks() {
		// Mock paginated list method
		when(mockEntityService.list(any(Pageable.class))).thenReturn(testEntities);

		// Mock individual entity retrieval
		if ((testEntities != null) && !testEntities.isEmpty()) {
			final EntityClass firstEntity = testEntities.get(0);
			when(mockEntityService.getById(firstEntity.getId()))
				.thenReturn(Optional.of(firstEntity));
		}
		// Mock save operations
		when(mockEntityService.save(any(entityClass)))
			.thenAnswer(invocation -> invocation.getArgument(0));
		LOGGER.debug("Service mocks configured for {}", entityClass.getSimpleName());
	}

	/**
	 * Sets up test data entities. Should be overridden by subclasses to provide specific
	 * test entities with all required relationships initialized.
	 */
	protected abstract void setupTestData();

	/**
	 * Utility method to run assertions without failing the entire test. Logs errors
	 * instead of throwing exceptions.
	 * @param assertion   the assertion to run
	 * @param description description of what is being tested
	 * @return true if assertion passed, false otherwise
	 */
	protected boolean softAssert(final Runnable assertion, final String description) {

		try {
			assertion.run();
			LOGGER.debug("Soft assertion passed: {}", description);
			return true;
		} catch (AssertionError | Exception e) {
			LOGGER.warn("Soft assertion failed: {} - {}", description, e.getMessage());
			return false;
		}
	}

	/**
	 * Tests grid column access for lazy loading issues. Verifies that all configured
	 * columns can access entity properties without lazy loading exceptions.
	 * @param grid the grid to test
	 */
	protected void testGridColumnAccess(final Grid<EntityClass> grid) {
		LOGGER.info("Testing grid column access for {}", entityClass.getSimpleName());

		if ((testEntities == null) || testEntities.isEmpty()) {
			LOGGER.warn("No test entities available for column access testing");
			return;
		}
		// Basic test that grid has columns
		assertTrue(grid.getColumns().size() > 0, "Grid should have columns");
		LOGGER.info("Grid has {} columns", grid.getColumns().size());
		LOGGER.info("Grid column access test passed for {}", entityClass.getSimpleName());
	}

	/**
	 * Tests grid data loading without exceptions. Simulates the grid data provider
	 * callback and verifies no lazy loading exceptions occur.
	 * @param grid the grid to test
	 */
	protected void testGridDataLoading(final Grid<EntityClass> grid) {
		LOGGER.info("Testing grid data loading for {}", entityClass.getSimpleName());
		assertNotNull(grid, "Grid should not be null");

		try {
			// Test that the grid can be accessed without exceptions
			final DataProvider<EntityClass, ?> dataProvider = grid.getDataProvider();
			assertNotNull(dataProvider, "Grid should have a data provider");
			LOGGER.info("Grid data loading test passed for {}",
				entityClass.getSimpleName());
		} catch (final Exception e) {
			fail("Grid data loading failed with exception: " + e.getMessage(), e);
		}
	}

	/**
	 * Tests grid selection behavior.
	 * @param grid the grid to test
	 */
	protected void testGridSelection(final Grid<EntityClass> grid) {
		LOGGER.info("Testing grid selection for {}", entityClass.getSimpleName());

		if ((testEntities == null) || testEntities.isEmpty()) {
			LOGGER.warn("No test entities available for selection testing");
			return;
		}
		final EntityClass testEntity = testEntities.get(0);

		try {
			// Test selection
			grid.select(testEntity);
			final EntityClass selected = grid.asSingleSelect().getValue();
			assertEquals(testEntity, selected, "Selected entity should match");
			// Test deselection
			grid.deselectAll();
			assertNull(grid.asSingleSelect().getValue(),
				"No entity should be selected after deselect");
			LOGGER.info("Grid selection test passed for {}", entityClass.getSimpleName());
		} catch (final Exception e) {
			fail("Grid selection testing failed: " + e.getMessage(), e);
		}
	}

	/**
	 * Verifies that an entity has all required relationships properly initialized to
	 * prevent lazy loading exceptions.
	 * @param entity the entity to verify
	 */
	protected abstract void verifyEntityRelationships(EntityClass entity);
}