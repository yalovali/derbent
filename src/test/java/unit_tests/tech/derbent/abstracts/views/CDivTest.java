package unit_tests.tech.derbent.abstracts.views;

import org.junit.jupiter.api.Test;

import tech.derbent.abstracts.utils.Check;
import tech.derbent.abstracts.views.CButton;
import tech.derbent.abstracts.views.CDiv;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/**
 * Test class for CDiv to verify functionality and inheritance.
 */
class CDivTest extends CTestBase {

    @Override
    protected void setupForTest() {
        // No specific setup required
    }

    @Test
    void testBasicConstructor() {
        final CDiv div = new CDiv();
        Check.notNull(div);
        Check.notNull(div.getId());
        Check.condition(div.getId().isPresent());
    }

    @Test
    void testConstructorWithComponents() {
        final CButton button1 = new CButton("Button 1");
        final CButton button2 = new CButton("Button 2");
        final CDiv div = new CDiv(button1, button2);

        Check.notNull(div);
        Check.equals(2, (int) (int) div.getChildren().count());
    }

    @Test
    void testConstructorWithClassName() {
        final CDiv div = new CDiv("test-class");
        Check.notNull(div);
        Check.condition(div.getClassNames().contains("test-class"));
    }

    @Test
    void testConstructorWithClassNameAndComponents() {
        final CButton button = new CButton("Test");
        final CDiv div = new CDiv("test-class", button);

        Check.notNull(div);
        Check.condition(div.getClassNames().contains("test-class"));
        Check.equals(1, (int) (int) div.getChildren().count());
    }

    @Test
    void testStaticFactoryMethods() {
        final CDiv fullWidth = CDiv.withFullWidth();
        Check.notNull(fullWidth);
        Check.equals("100%", fullWidth.getWidth());

        final CDiv fullHeight = CDiv.withFullHeight();
        Check.notNull(fullHeight);
        Check.equals("100%", fullHeight.getHeight());

        final CDiv fullSize = CDiv.withFullSize();
        Check.notNull(fullSize);
        Check.equals("100%", fullSize.getWidth());
        Check.equals("100%", fullSize.getHeight());
    }

    @Test
    void testCreateSpacer() {
        final CDiv spacer = CDiv.createSpacer();
        Check.notNull(spacer);
        Check.equals("1", spacer.getStyle().get("flex-grow"));
    }

    @Test
    void testFlexDisplayMethods() {
        final CDiv flexDiv = CDiv.withFlexDisplay();
        Check.notNull(flexDiv);
        Check.equals("flex", flexDiv.getStyle().get("display"));

        final CDiv centeredDiv = CDiv.withFlexCentered();
        Check.notNull(centeredDiv);
        Check.equals("flex", centeredDiv.getStyle().get("display"));
        Check.equals("center", centeredDiv.getStyle().get("align-items"));
        Check.equals("center", centeredDiv.getStyle().get("justify-content"));
    }

    @Test
    void testFluentAPIMethods() {
        final CDiv div = new CDiv().withWidthFull().withHeightFull().withClassName("test-class")
                .withStyle("color", "red").withPadding("10px").withMargin("5px").withBorderRadius("4px")
                .withBackgroundColor("#f0f0f0");

        Check.notNull(div);
        Check.equals("100%", div.getWidth());
        Check.equals("100%", div.getHeight());
        Check.condition(div.getClassNames().contains("test-class"));
        Check.equals("red", div.getStyle().get("color"));
        Check.equals("10px", div.getStyle().get("padding"));
        Check.equals("5px", div.getStyle().get("margin"));
        Check.equals("4px", div.getStyle().get("border-radius"));
        Check.equals("#f0f0f0", div.getStyle().get("background-color"));
    }

    @Test
    void testMethodChaining() {
        // Test that fluent API methods return the same instance for chaining
        final CDiv div = new CDiv();
        final CDiv result = div.withWidthFull();
        Check.condition(div == result);
    }

    @Test
    void testIdGeneration() {
        final CDiv div = new CDiv();
        Check.notNull(div.getId());
        Check.condition(div.getId().isPresent());
    }

    @Test
    void testStyleManipulation() {
        final CDiv div = new CDiv();
        div.withStyle("background-color", "blue");
        div.withStyle("color", "white");

        Check.equals("blue", div.getStyle().get("background-color"));
        Check.equals("white", div.getStyle().get("color"));
    }
}