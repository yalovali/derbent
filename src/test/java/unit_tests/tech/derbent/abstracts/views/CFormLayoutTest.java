package unit_tests.tech.derbent.abstracts.views;

import org.junit.jupiter.api.Test;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.abstracts.views.components.CFormLayout;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/** Test class for CFormLayout to verify functionality and inheritance. */
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
	void testDefaultResponsiveSteps() {
		final CFormLayout form = new CFormLayout();
		Check.equals(2, form.getResponsiveSteps().size());
	}

	@Test
	void testIdGeneration() {
		final CFormLayout form = new CFormLayout();
		Check.notNull(form.getId());
		Check.condition(form.getId().isPresent());
	}

	@Test
	void testSingleColumnFactory() {
		final CFormLayout form = CFormLayout.singleColumn();
		Check.notNull(form);
		Check.equals(1, form.getResponsiveSteps().size());
	}
}
