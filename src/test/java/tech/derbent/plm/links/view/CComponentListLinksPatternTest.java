package tech.derbent.plm.links.view;

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
        assertTrue(IContentOwner.class.isAssignableFrom(CComponentLink.class),
                "CComponentListLinks should implement IContentOwner");
        assertTrue(IGridComponent.class.isAssignableFrom(CComponentLink.class),
                "CComponentListLinks should implement IGridComponent");
        assertTrue(IGridRefreshListener.class.isAssignableFrom(CComponentLink.class),
                "CComponentListLinks should implement IGridRefreshListener");
        assertTrue(IPageServiceAutoRegistrable.class.isAssignableFrom(CComponentLink.class),
                "CComponentListLinks should implement IPageServiceAutoRegistrable");
    }

    @Test
    void testHasCoreContentOwnerMethods() throws Exception {
        final Method setValue = CComponentLink.class.getMethod("setValue", CEntityDB.class);
        final Method getValue = CComponentLink.class.getMethod("getValue");
        final Method populateForm = CComponentLink.class.getMethod("populateForm");
        assertNotNull(setValue, "setValue(CEntityDB) should exist");
        assertNotNull(getValue, "getValue() should exist");
        assertNotNull(populateForm, "populateForm() should exist");
    }

    @Test
    void testHasGridMaintenanceMethods() throws Exception {
        final Method refreshGrid = CComponentLink.class.getMethod("refreshGrid");
        final Method clearGrid = CComponentLink.class.getMethod("clearGrid");
        assertNotNull(refreshGrid, "refreshGrid() should exist");
        assertNotNull(clearGrid, "clearGrid() should exist");
    }
}
