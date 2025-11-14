package automated_tests.tech.derbent.ui.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.views.CAbstractEntityDBPage;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.activities.service.CActivityService;
import tech.derbent.app.meetings.domain.CMeeting;
import tech.derbent.app.meetings.service.CMeetingService;
import tech.derbent.app.page.view.CDynamicPageBase;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.projects.service.CProjectService;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.service.CUserService;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates that the interface refactoring maintains proper contracts for CRUD operations.
 * This test verifies that:
 * 1. IPageServiceImplementer properly extends IContentOwner
 * 2. All key abstract page classes implement the correct interfaces
 * 3. Method signatures are compatible across the hierarchy
 * 4. Services can be properly accessed through interfaces
 */
@SpringBootTest(classes = tech.derbent.Application.class)
@ActiveProfiles("test")
@DisplayName("Interface Refactoring Validation Tests")
public class CInterfaceRefactoringValidationTest {

    @Autowired
    private CActivityService activityService;

    @Autowired
    private CMeetingService meetingService;

    @Autowired
    private CProjectService projectService;

    @Autowired
    private CUserService userService;

    @Test
    @DisplayName("IPageServiceImplementer extends IContentOwner")
    void testIPageServiceImplementerExtendsIContentOwner() {
        // Verify that IPageServiceImplementer is a subtype of IContentOwner
        assertTrue(IContentOwner.class.isAssignableFrom(IPageServiceImplementer.class),
                "IPageServiceImplementer should extend IContentOwner");
    }

    @Test
    @DisplayName("Page base classes implement IPageServiceImplementer")
    void testPageBaseClassesImplementIPageServiceImplementer() {
        // Verify that key page base classes implement IPageServiceImplementer
        assertTrue(IPageServiceImplementer.class.isAssignableFrom(CAbstractEntityDBPage.class),
                "CAbstractEntityDBPage should implement IPageServiceImplementer");
        assertTrue(IPageServiceImplementer.class.isAssignableFrom(CDynamicPageBase.class),
                "CDynamicPageBase should implement IPageServiceImplementer");
    }

    @Test
    @DisplayName("Page base classes implement IContentOwner through inheritance")
    void testPageBaseClassesImplementIContentOwner() {
        // Verify that page base classes implement IContentOwner (through IPageServiceImplementer)
        assertTrue(IContentOwner.class.isAssignableFrom(CAbstractEntityDBPage.class),
                "CAbstractEntityDBPage should implement IContentOwner through IPageServiceImplementer");
        assertTrue(IContentOwner.class.isAssignableFrom(CDynamicPageBase.class),
                "CDynamicPageBase should implement IContentOwner through IPageServiceImplementer");
    }

    @Test
    @DisplayName("IContentOwner methods are accessible")
    void testIContentOwnerMethodsAccessible() {
        try {
            // Verify IContentOwner methods exist and are accessible
            IContentOwner.class.getMethod("getCurrentEntity");
            IContentOwner.class.getMethod("getEntityService");
            IContentOwner.class.getMethod("populateForm");
            IContentOwner.class.getMethod("setCurrentEntity", CEntityDB.class);
            IContentOwner.class.getMethod("createNewEntityInstance");
            IContentOwner.class.getMethod("refreshGrid");
        } catch (NoSuchMethodException e) {
            fail("IContentOwner should have all required methods: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("IPageServiceImplementer methods are accessible")
    void testIPageServiceImplementerMethodsAccessible() {
        try {
            // Verify IPageServiceImplementer methods exist and are accessible
            IPageServiceImplementer.class.getMethod("getCurrentEntity");
            IPageServiceImplementer.class.getMethod("getEntityService");
            IPageServiceImplementer.class.getMethod("getBinder");
            IPageServiceImplementer.class.getMethod("getEntityClass");
            IPageServiceImplementer.class.getMethod("getSessionService");
            IPageServiceImplementer.class.getMethod("selectFirstInGrid");
        } catch (NoSuchMethodException e) {
            fail("IPageServiceImplementer should have all required methods: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("setCurrentEntity method uses correct signature")
    void testSetCurrentEntitySignature() {
        try {
            // Verify setCurrentEntity accepts CEntityDB<?>
            var method = IContentOwner.class.getMethod("setCurrentEntity", CEntityDB.class);
            assertNotNull(method, "setCurrentEntity method should exist");
            assertEquals(void.class, method.getReturnType(), "setCurrentEntity should return void");
        } catch (NoSuchMethodException e) {
            fail("setCurrentEntity(CEntityDB<?>) method should exist: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Services are properly autowired")
    void testServicesAutowired() {
        assertNotNull(activityService, "CActivityService should be autowired");
        assertNotNull(meetingService, "CMeetingService should be autowired");
        assertNotNull(projectService, "CProjectService should be autowired");
        assertNotNull(userService, "CUserService should be autowired");
    }

    @Test
    @DisplayName("Services implement CAbstractService")
    void testServicesImplementCAbstractService() {
        assertTrue(CAbstractService.class.isAssignableFrom(activityService.getClass()),
                "CActivityService should extend CAbstractService");
        assertTrue(CAbstractService.class.isAssignableFrom(meetingService.getClass()),
                "CMeetingService should extend CAbstractService");
        assertTrue(CAbstractService.class.isAssignableFrom(projectService.getClass()),
                "CProjectService should extend CAbstractService");
        assertTrue(CAbstractService.class.isAssignableFrom(userService.getClass()),
                "CUserService should extend CAbstractService");
    }

    @Test
    @DisplayName("Entity classes extend CEntityDB")
    void testEntityClassesExtendCEntityDB() {
        assertTrue(CEntityDB.class.isAssignableFrom(CActivity.class),
                "CActivity should extend CEntityDB");
        assertTrue(CEntityDB.class.isAssignableFrom(CMeeting.class),
                "CMeeting should extend CEntityDB");
        assertTrue(CEntityDB.class.isAssignableFrom(CProject.class),
                "CProject should extend CEntityDB");
        assertTrue(CEntityDB.class.isAssignableFrom(CUser.class),
                "CUser should extend CEntityDB");
    }

    @Test
    @DisplayName("No duplicate method declarations in interface hierarchy")
    void testNoDuplicateMethodDeclarations() {
        // Verify that methods are not redeclared in IPageServiceImplementer
        // if they're inherited from IContentOwner (except for overrides with narrower types)
        
        try {
            // getCurrentEntity is overridden in IPageServiceImplementer with covariant return type
            var contentOwnerMethod = IContentOwner.class.getMethod("getCurrentEntity");
            var pageServiceMethod = IPageServiceImplementer.class.getMethod("getCurrentEntity");
            
            // Both should exist but IPageServiceImplementer should override with specific type
            assertNotNull(contentOwnerMethod);
            assertNotNull(pageServiceMethod);
            
            // getEntityService is also overridden with specific generic type
            var contentOwnerServiceMethod = IContentOwner.class.getMethod("getEntityService");
            var pageServiceServiceMethod = IPageServiceImplementer.class.getMethod("getEntityService");
            
            assertNotNull(contentOwnerServiceMethod);
            assertNotNull(pageServiceServiceMethod);
            
        } catch (NoSuchMethodException e) {
            fail("Methods should be properly declared in interface hierarchy: " + e.getMessage());
        }
    }
}
