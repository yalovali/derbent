package unit_tests.tech.derbent.abstracts.views;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import tech.derbent.abstracts.utils.Check;
import tech.derbent.abstracts.views.CButton;
import tech.derbent.abstracts.views.CHorizontalLayout;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/**
 * Test class for CHorizontalLayout to verify enhanced functionality and inheritance.
 */
class CHorizontalLayoutTest extends CTestBase {

    @Override
    protected void setupForTest() {
        // No specific setup required
    }

    @Test
    void testBasicConstructor() {
        final CHorizontalLayout layout = new CHorizontalLayout();
        Check.notNull(layout);
        Check.condition(!layout.isPadding());
        Check.condition(!layout.isSpacing());
        Check.equals("100%", layout.getWidth());
        Check.equals("100%", layout.getHeight());
    }

    @Test
    void testConstructorWithSettings() {
        final CHorizontalLayout layout = new CHorizontalLayout(true, true, true);
        Check.notNull(layout);
        Check.condition(layout.isPadding());
        Check.condition(layout.isSpacing());
        Check.condition(layout.isMargin());
    }

    @Test
    void testConstructorWithStyle() {
        final CHorizontalLayout layout = new CHorizontalLayout("test-style");
        Check.notNull(layout);
        Check.condition(layout.getClassNames().contains("test-style"));
    }

    @Test
    void testConstructorWithComponents() {
        final CButton button1 = new CButton("Button 1");
        final CButton button2 = new CButton("Button 2");
        final CHorizontalLayout layout = new CHorizontalLayout(button1, button2);

        Check.notNull(layout);
        Check.equals(2, (int) layout.getChildren().count());
    }

    @Test
    void testStaticFactoryMethods() {
        final CHorizontalLayout withSpacing = CHorizontalLayout.withSpacing();
        Check.notNull(withSpacing);
        Check.condition(withSpacing.isSpacing());
        Check.condition(!withSpacing.isPadding());

        final CHorizontalLayout withPadding = CHorizontalLayout.withPadding();
        Check.notNull(withPadding);
        Check.condition(withPadding.isPadding());
        Check.condition(!withPadding.isSpacing());

        final CHorizontalLayout withBoth = CHorizontalLayout.withSpacingAndPadding();
        Check.notNull(withBoth);
        Check.condition(withBoth.isSpacing());
        Check.condition(withBoth.isPadding());

        final CHorizontalLayout withFull = CHorizontalLayout.withFullSpacing();
        Check.notNull(withFull);
        Check.condition(withFull.isSpacing());
        Check.condition(withFull.isPadding());
        Check.condition(withFull.isMargin());
    }

    @Test
    void testForButtonsMethod() {
        final CButton button1 = new CButton("Button 1");
        final CButton button2 = new CButton("Button 2");
        final CHorizontalLayout layout = CHorizontalLayout.forButtons(button1, button2);

        Check.notNull(layout);
        Check.condition(layout.isSpacing());
        Check.equals(2, (int) layout.getChildren().count());
        Check.equals(HorizontalLayout.Alignment.CENTER, layout.getDefaultVerticalComponentAlignment());
    }

    @Test
    void testForToolbarMethod() {
        final CButton button1 = new CButton("Action 1");
        final CButton button2 = new CButton("Action 2");
        final CHorizontalLayout layout = CHorizontalLayout.forToolbar(button1, button2);

        Check.notNull(layout);
        Check.condition(layout.isSpacing());
        Check.equals(2, (int) layout.getChildren().count());
        Check.equals(HorizontalLayout.Alignment.CENTER, layout.getDefaultVerticalComponentAlignment());
        Check.equals(HorizontalLayout.JustifyContentMode.START, layout.getJustifyContentMode());
    }

    @Test
    void testFluentAPIMethods() {
        final CHorizontalLayout layout = new CHorizontalLayout().withSpacing(true).withPadding(true).withMargin(true)
                .withWidthFull().withHeightFull().withDefaultAlignment(HorizontalLayout.Alignment.CENTER)
                .withJustifyContentMode(HorizontalLayout.JustifyContentMode.CENTER);

        Check.notNull(layout);
        Check.condition(layout.isSpacing());
        Check.condition(layout.isPadding());
        Check.condition(layout.isMargin());
        Check.equals(HorizontalLayout.Alignment.CENTER, layout.getDefaultVerticalComponentAlignment());
        Check.equals(HorizontalLayout.JustifyContentMode.CENTER, layout.getJustifyContentMode());
    }

    @Test
    void testMethodChaining() {
        // Test that fluent API methods return the same instance for chaining
        final CHorizontalLayout layout = new CHorizontalLayout();
        final CHorizontalLayout result = layout.withSpacing(true);
        Check.condition(layout == result);
    }

    @Test
    void testIdGeneration() {
        final CHorizontalLayout layout = new CHorizontalLayout();
        Check.notNull(layout.getId());
        Check.condition(layout.getId().isPresent());
    }
}