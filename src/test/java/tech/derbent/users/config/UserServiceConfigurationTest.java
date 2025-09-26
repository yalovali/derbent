package tech.derbent.users.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.Clock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.derbent.session.service.CSessionService;
import tech.derbent.users.service.CUserRepository;
import tech.derbent.users.service.CUserService;

@ExtendWith (MockitoExtension.class)
class UserServiceConfigurationTest {

	@Mock
	private CUserService userService;
	@Mock
	private CSessionService sessionService;
	@Mock
	private CUserRepository userRepository;
	@Mock
	private Clock clock;
	private UserServiceConfiguration configuration;

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
		// Given
		CUserService realUserService = new CUserService(userRepository, clock);
		// When - This should not throw an exception
		assertNotNull(realUserService);
		// Then - SessionService can be set later
		realUserService.setSessionService(sessionService);
		// No exception should be thrown
	}
}
