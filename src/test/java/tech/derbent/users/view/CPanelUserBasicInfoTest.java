package tech.derbent.users.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserService;

/**
 * Test class for CPanelUserBasicInfo to ensure proper field grouping.
 */
@ExtendWith (MockitoExtension.class)
class CPanelUserBasicInfoTest {

	@Mock
	private CUserService userService;

	private CUser testUser;

	private BeanValidationBinder<CUser> binder;

	@BeforeEach
	void setUp() {
		testUser = new CUser();
		testUser.setName("Test User");
		binder = new BeanValidationBinder<>(CUser.class);
	}
}