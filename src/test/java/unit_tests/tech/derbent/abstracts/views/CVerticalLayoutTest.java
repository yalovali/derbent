package unit_tests.tech.derbent.abstracts.views;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import tech.derbent.abstracts.utils.Check;
import tech.derbent.abstracts.views.CButton;
import tech.derbent.abstracts.views.CVerticalLayout;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/**
 * Test class for CVerticalLayout to verify enhanced functionality and inheritance.
 */
class CVerticalLayoutTest extends CTestBase {

    @Override
    protected void setupForTest() {
        // No specific setup required
    }

    @Test
    void testBasicConstructor() {
        final CVerticalLayout layout = new CVerticalLayout();
        Check.notNull(layout);
        Check.condition(!layout.isPadding());
        Check.condition(!layout.isSpacing());
        Check.equals("100%", layout.getWidth());
        Check.equals("100%", layout.getHeight());
    }

    @Test
    void testConstructorWithSettings() {
        final CVerticalLayout layout = new CVerticalLayout(true, true, true);
        Check.notNull(layout);
        Check.condition(layout.isPadding());
        Check.condition(layout.isSpacing());
        Check.condition(layout.isMargin());
    }

    @Test
    void testConstructorWithStyle() {
        final CVerticalLayout layout = new CVerticalLayout("test-style");
        Check.notNull(layout);
        Check.condition(layout.getClassNames().contains("test-style"));
    }

    @Test
    void testConstructorWithComponents() {
        final CButton button1 = new CButton("Button 1");
        final CButton button2 = new CButton("Button 2");
        final CVerticalLayout layout = new CVerticalLayout(button1, button2);

        Check.notNull(layout);
        Check.equals(2, (int) layout.getChildren().count());
    }

    @Test
    void testStaticFactoryMethods() {
        final CVerticalLayout withSpacing = CVerticalLayout.withSpacing();
        Check.notNull(withSpacing);
        Check.condition(withSpacing.isSpacing());
        Check.condition(!withSpacing.isPadding());

        final CVerticalLayout withPadding = CVerticalLayout.withPadding();
        Check.notNull(withPadding);
        Check.condition(withPadding.isPadding());
        Check.condition(!withPadding.isSpacing());

        final CVerticalLayout withBoth = CVerticalLayout.withSpacingAndPadding();
        Check.notNull(withBoth);
        Check.condition(withBoth.isSpacing());
        Check.condition(withBoth.isPadding());

        final CVerticalLayout withFull = CVerticalLayout.withFullSpacing();
        Check.notNull(withFull);
        Check.condition(withFull.isSpacing());
        Check.condition(withFull.isPadding());
        Check.condition(withFull.isMargin());
    }

    @Test
    void testForButtonsMethod() {
        final CButton button1 = new CButton("Button 1");
        final CButton button2 = new CButton("Button 2");
        final CVerticalLayout layout = CVerticalLayout.forButtons(button1, button2);

        Check.notNull(layout);
        Check.condition(layout.isSpacing());
        Check.equals(2, (int) layout.getChildren().count());
        Check.equals(VerticalLayout.Alignment.START, layout.getDefaultHorizontalComponentAlignment());
    }

    @Test
    void testFluentAPIMethods() {
        final CVerticalLayout layout = new CVerticalLayout().withSpacing(true).withPadding(true).withMargin(true)
                .withWidthFull().withHeightFull().withDefaultAlignment(VerticalLayout.Alignment.CENTER);

        Check.notNull(layout);
        Check.condition(layout.isSpacing());
        Check.condition(layout.isPadding());
        Check.condition(layout.isMargin());
        Check.equals(VerticalLayout.Alignment.CENTER, layout.getDefaultHorizontalComponentAlignment());
    }

    @Test
    void testMethodChaining() {
        // Test that fluent API methods return the same instance for chaining
        final CVerticalLayout layout = new CVerticalLayout();
        final CVerticalLayout result = layout.withSpacing(true);
        Check.condition(layout == result);
    }

    @Test
    void testIdGeneration() {
        final CVerticalLayout layout = new CVerticalLayout();
        Check.notNull(layout.getId());
        Check.condition(layout.getId().isPresent());
    }
}