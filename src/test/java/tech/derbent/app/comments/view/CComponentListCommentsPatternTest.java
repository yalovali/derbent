package tech.derbent.app.comments.view;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.interfaces.IGridComponent;
import tech.derbent.api.interfaces.IGridRefreshListener;
import tech.derbent.api.interfaces.IPageServiceAutoRegistrable;

/** Verifies that CComponentListComments follows standard component interface patterns without requiring UI context. */
@SuppressWarnings("static-method")
class CComponentListCommentsPatternTest {

	@Test
	void testImplementsStandardInterfaces() {
		assertTrue(IContentOwner.class.isAssignableFrom(CComponentListComments.class),
				"CComponentListComments should implement IContentOwner");
		assertTrue(IGridComponent.class.isAssignableFrom(CComponentListComments.class),
				"CComponentListComments should implement IGridComponent");
		assertTrue(IGridRefreshListener.class.isAssignableFrom(CComponentListComments.class),
				"CComponentListComments should implement IGridRefreshListener");
		assertTrue(IPageServiceAutoRegistrable.class.isAssignableFrom(CComponentListComments.class),
				"CComponentListComments should implement IPageServiceAutoRegistrable");
	}

	@Test
	void testHasCoreContentOwnerMethods() throws Exception {
		final Method setValue = CComponentListComments.class.getMethod("setValue", CEntityDB.class);
		final Method getValue = CComponentListComments.class.getMethod("getValue");
		final Method populateForm = CComponentListComments.class.getMethod("populateForm");
		assertNotNull(setValue, "setValue(CEntityDB) should exist");
		assertNotNull(getValue, "getValue() should exist");
		assertNotNull(populateForm, "populateForm() should exist");
	}

	@Test
	void testHasGridMaintenanceMethods() throws Exception {
		final Method refreshGrid = CComponentListComments.class.getMethod("refreshGrid");
		final Method clearGrid = CComponentListComments.class.getMethod("clearGrid");
		assertNotNull(refreshGrid, "refreshGrid() should exist");
		assertNotNull(clearGrid, "clearGrid() should exist");
	}
}
