package unit_tests.tech.derbent.abstracts.views;

import org.junit.jupiter.api.Test;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.abstracts.views.components.CButton;
import tech.derbent.abstracts.views.components.CVerticalLayout;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/** Test class for CVerticalLayout to verify enhanced functionality and inheritance. */
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
	void testConstructorWithComponents() {
		final CButton button1 = new CButton("Button 1", null, null);
		final CButton button2 = new CButton("Button 2", null, null);
		final CVerticalLayout layout = new CVerticalLayout(button1, button2);
		Check.notNull(layout);
		Check.equals(2, (int) layout.getChildren().count());
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
	void testForButtonsMethod() {
		final CButton button1 = new CButton("Button 1", null, null);
		final CButton button2 = new CButton("Button 2", null, null);
		final CVerticalLayout layout = CVerticalLayout.forButtons(button1, button2);
		Check.notNull(layout);
		Check.condition(layout.isSpacing());
		Check.equals(2, (int) layout.getChildren().count());
		Check.equals(VerticalLayout.Alignment.START, layout.getDefaultHorizontalComponentAlignment());
	}

	@Test
	void testIdGeneration() {
		final CVerticalLayout layout = new CVerticalLayout();
		Check.notNull(layout.getId());
		Check.condition(layout.getId().isPresent());
	}
}
