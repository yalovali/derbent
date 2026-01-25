package tech.derbent.plm.attachments.view;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.interfaces.IGridComponent;
import tech.derbent.api.interfaces.IGridRefreshListener;
import tech.derbent.api.interfaces.IPageServiceAutoRegistrable;

/** Verifies that CComponentListAttachments follows standard component interface patterns without requiring UI context. */

class CComponentListAttachmentsPatternTest {

	@Test
	void testImplementsStandardInterfaces() {
		assertTrue(IContentOwner.class.isAssignableFrom(CComponentListAttachments.class),
				"CComponentListAttachments should implement IContentOwner");
		assertTrue(IGridComponent.class.isAssignableFrom(CComponentListAttachments.class),
				"CComponentListAttachments should implement IGridComponent");
		assertTrue(IGridRefreshListener.class.isAssignableFrom(CComponentListAttachments.class),
				"CComponentListAttachments should implement IGridRefreshListener");
		assertTrue(IPageServiceAutoRegistrable.class.isAssignableFrom(CComponentListAttachments.class),
				"CComponentListAttachments should implement IPageServiceAutoRegistrable");
	}

	@Test
	void testHasCoreContentOwnerMethods() throws Exception {
		final Method setValue = CComponentListAttachments.class.getMethod("setValue", CEntityDB.class);
		final Method getValue = CComponentListAttachments.class.getMethod("getValue");
		final Method populateForm = CComponentListAttachments.class.getMethod("populateForm");
		assertNotNull(setValue, "setValue(CEntityDB) should exist");
		assertNotNull(getValue, "getValue() should exist");
		assertNotNull(populateForm, "populateForm() should exist");
	}

	@Test
	void testHasGridMaintenanceMethods() throws Exception {
		final Method refreshGrid = CComponentListAttachments.class.getMethod("refreshGrid");
		final Method clearGrid = CComponentListAttachments.class.getMethod("clearGrid");
		assertNotNull(refreshGrid, "refreshGrid() should exist");
		assertNotNull(clearGrid, "clearGrid() should exist");
	}
}
