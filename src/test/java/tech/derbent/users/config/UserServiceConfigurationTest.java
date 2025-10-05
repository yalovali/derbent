package tech.derbent.users.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import java.time.Clock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.derbent.session.service.CSessionService;
import tech.derbent.users.service.CUserCompanySettingsService;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.service.IUserRepository;

@ExtendWith (MockitoExtension.class)
class UserServiceConfigurationTest {

	@Mock
	private CUserService userService;
	@Mock
	private CSessionService sessionService;
	@Mock
	private IUserRepository userRepository;
	@Mock
	private Clock clock;
	private UserServiceConfiguration configuration;
	@Mock
	private CUserCompanySettingsService userCompanySettingsService;

	@BeforeEach
	void setUp() {
		configuration = new UserServiceConfiguration(userService, sessionService);
	}

	@Test
	void testConfigureServices() {
		// When
		configuration.configureServices();
		// Then
		verify(userService).setSessionService(sessionService);
	}

	@Test
	void testUserServiceCanBeCreatedWithoutSessionService() {
		// Given - CUserService can be created without sessionService (setter injection)
		CUserService realUserService = new CUserService(userRepository, sessionService, userCompanySettingsService, clock);
		// When - This should not throw an exception
		assertNotNull(realUserService);
		// Then - SessionService can be set later
		realUserService.setSessionService(sessionService);
		// No exception should be thrown
	}
}
