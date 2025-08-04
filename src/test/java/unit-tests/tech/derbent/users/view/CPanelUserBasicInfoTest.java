package tech.derbent.users.view;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.abstracts.domains.CTestBase;
import tech.derbent.users.domain.CUser;

/**
 * Test class for CPanelUserBasicInfo to ensure proper field grouping.
 */
@ExtendWith(MockitoExtension.class)
class CPanelUserBasicInfoTest extends CTestBase {

    @SuppressWarnings("unused")
    private BeanValidationBinder<CUser> binder;

    @Override
    protected void setupForTest() {
        binder = new BeanValidationBinder<>(CUser.class);
    }
}