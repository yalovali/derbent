package unit_tests.tech.derbent.users.view;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import tech.derbent.abstracts.components.CEnhancedBinder;

import tech.derbent.abstracts.components.CBinderFactory;
import tech.derbent.users.domain.CUser;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/**
 * Test class for CPanelUserBasicInfo to ensure proper field grouping.
 */
@ExtendWith (MockitoExtension.class)
class CPanelUserBasicInfoTest extends CTestBase {

	@SuppressWarnings ("unused")
	private CEnhancedBinder<CUser> binder;

	@Override
	protected void setupForTest() {
		binder = CBinderFactory.createBinder(CUser.class);
	}
}