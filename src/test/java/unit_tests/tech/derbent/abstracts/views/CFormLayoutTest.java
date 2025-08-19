package unit_tests.tech.derbent.abstracts.views;

import org.junit.jupiter.api.Test;

import tech.derbent.abstracts.utils.Check;
import tech.derbent.abstracts.views.CButton;
import tech.derbent.abstracts.views.CFormLayout;
import tech.derbent.abstracts.views.CTextField;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/**
 * Test class for CFormLayout to verify functionality and inheritance.
 */
class CFormLayoutTest extends CTestBase {

    @Override
    protected void setupForTest() {
        // No specific setup required
    }

    @Test
    void testBasicConstructor() {
        final CFormLayout form = new CFormLayout();
        Check.notNull(form);
        Check.notNull(form.getId());
        Check.condition(form.getId().isPresent());
        Check.condition(form.getResponsiveSteps().size() >= 1);
    }

    @Test
    void testConstructorWithComponents() {
        final CTextField field1 = new CTextField("Field 1");
        final CTextField field2 = new CTextField("Field 2");
        final CFormLayout form = new CFormLayout(field1, field2);

        Check.notNull(form);
        Check.equals(2, (int) form.getChildren().count());
    }

    @Test
    void testSingleColumnFactory() {
        final CFormLayout form = CFormLayout.singleColumn();
        Check.notNull(form);
        Check.equals(1, form.getResponsiveSteps().size());
    }

    @Test
    void testTwoColumnFactory() {
        final CFormLayout form = CFormLayout.twoColumn();
        Check.notNull(form);
        Check.equals(2, form.getResponsiveSteps().size());
    }

    @Test
    void testResponsiveFactory() {
        final CFormLayout form = CFormLayout.responsive();
        Check.notNull(form);
        Check.equals(3, form.getResponsiveSteps().size());
    }

    @Test
    void testCompactFactory() {
        final CFormLayout form = CFormLayout.compact();
        Check.notNull(form);
        Check.condition(form.getClassNames().contains("compact-form"));
        Check.equals("var(--lumo-space-s)", form.getStyle().get("gap"));
    }

    @Test
    void testForDialogFactory() {
        final CFormLayout form = CFormLayout.forDialog();
        Check.notNull(form);
        Check.equals("400px", form.getMaxWidth());
        Check.equals("0 auto", form.getStyle().get("margin"));
    }

    @Test
    void testForCardFactory() {
        final CFormLayout form = CFormLayout.forCard();
        Check.notNull(form);
        Check.equals("var(--lumo-space-m)", form.getStyle().get("padding"));
        Check.equals("var(--lumo-base-color)", form.getStyle().get("background"));
        Check.equals("var(--lumo-border-radius-m)", form.getStyle().get("border-radius"));
    }

    @Test
    void testFluentAPIMethods() {
        final CFormLayout form = new CFormLayout().withMaxWidth("500px").withWidthFull().withClassName("test-form")
                .withStyle("border", "1px solid #ccc").withPadding("var(--lumo-space-m)")
                .withGap("var(--lumo-space-s)");

        Check.notNull(form);
        Check.equals("500px", form.getMaxWidth());
        Check.equals("100%", form.getWidth());
        Check.condition(form.getClassNames().contains("test-form"));
        Check.equals("1px solid #ccc", form.getStyle().get("border"));
        Check.equals("var(--lumo-space-m)", form.getStyle().get("padding"));
        Check.equals("var(--lumo-space-s)", form.getStyle().get("gap"));
    }

    @Test
    void testComponentMethods() {
        final CFormLayout form = new CFormLayout();
        final CTextField field = new CTextField("Test Field");
        final CButton button = new CButton("Test Button");

        form.withFullWidthComponent(field);
        form.withComponent(button, 2);

        Check.equals(2, (int) form.getChildren().count());
        Check.equals(3, form.getColspan(field)); // Full width component gets colspan 3
        Check.equals(2, form.getColspan(button));
    }

    @Test
    void testMethodChaining() {
        // Test that fluent API methods return the same instance for chaining
        final CFormLayout form = new CFormLayout();
        final CFormLayout result = form.withMaxWidth("400px");
        Check.condition(form == result);
    }

    @Test
    void testIdGeneration() {
        final CFormLayout form = new CFormLayout();
        Check.notNull(form.getId());
        Check.condition(form.getId().isPresent());
    }

    @Test
    void testDefaultResponsiveSteps() {
        final CFormLayout form = new CFormLayout();
        Check.equals(2, form.getResponsiveSteps().size());
    }

    @Test
    void testResponsiveStepsConfiguration() {
        final CFormLayout singleCol = CFormLayout.singleColumn();
        Check.equals(1, singleCol.getResponsiveSteps().size());

        final CFormLayout twoCol = CFormLayout.twoColumn();
        Check.equals(2, twoCol.getResponsiveSteps().size());

        final CFormLayout responsive = CFormLayout.responsive();
        Check.equals(3, responsive.getResponsiveSteps().size());
    }

    @Test
    void testStyleConfiguration() {
        final CFormLayout form = new CFormLayout().withStyle("background-color", "white").withStyle("border-radius",
                "8px");

        Check.equals("white", form.getStyle().get("background-color"));
        Check.equals("8px", form.getStyle().get("border-radius"));
    }

    @Test
    void testFormLayoutSizing() {
        final CFormLayout form = new CFormLayout().withWidthFull().withMaxWidth("600px");

        Check.equals("100%", form.getWidth());
        Check.equals("600px", form.getMaxWidth());
    }
}