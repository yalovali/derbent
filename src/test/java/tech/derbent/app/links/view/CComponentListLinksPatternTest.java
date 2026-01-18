package tech.derbent.app.links.view;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.interfaces.IGridComponent;
import tech.derbent.api.interfaces.IGridRefreshListener;
import tech.derbent.api.interfaces.IPageServiceAutoRegistrable;

/** Verifies that CComponentListLinks follows standard component interface patterns without requiring UI context. */
@SuppressWarnings("static-method")
class CComponentListLinksPatternTest {

    @Test
    void testImplementsStandardInterfaces() {
        assertTrue(IContentOwner.class.isAssignableFrom(CComponentListLinks.class),
                "CComponentListLinks should implement IContentOwner");
        assertTrue(IGridComponent.class.isAssignableFrom(CComponentListLinks.class),
                "CComponentListLinks should implement IGridComponent");
        assertTrue(IGridRefreshListener.class.isAssignableFrom(CComponentListLinks.class),
                "CComponentListLinks should implement IGridRefreshListener");
        assertTrue(IPageServiceAutoRegistrable.class.isAssignableFrom(CComponentListLinks.class),
                "CComponentListLinks should implement IPageServiceAutoRegistrable");
    }

    @Test
    void testHasCoreContentOwnerMethods() throws Exception {
        final Method setValue = CComponentListLinks.class.getMethod("setValue", CEntityDB.class);
        final Method getValue = CComponentListLinks.class.getMethod("getValue");
        final Method populateForm = CComponentListLinks.class.getMethod("populateForm");
        assertNotNull(setValue, "setValue(CEntityDB) should exist");
        assertNotNull(getValue, "getValue() should exist");
        assertNotNull(populateForm, "populateForm() should exist");
    }

    @Test
    void testHasGridMaintenanceMethods() throws Exception {
        final Method refreshGrid = CComponentListLinks.class.getMethod("refreshGrid");
        final Method clearGrid = CComponentListLinks.class.getMethod("clearGrid");
        assertNotNull(refreshGrid, "refreshGrid() should exist");
        assertNotNull(clearGrid, "clearGrid() should exist");
    }
}
