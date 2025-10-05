package tech.derbent.session.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import tech.derbent.projects.service.IProjectRepository;
import tech.derbent.users.service.CUserCompanySettingsService;
import tech.derbent.users.service.IUserRepository;

/** Configuration to provide the correct session service bean for different environments. This ensures compatibility between the expected
 * CSessionService concrete type and the actual implementation available in different profiles. */
@Configuration
public class CSessionServiceConfig {

	/** Provides a CSessionService bean of the exact type expected by consuming services. This method creates a subclass of CSessionService that
	 * delegates all operations to the web session service for non-reset-db profiles.
	 * @param webSessionService the web-based session service implementation
	 * @param userRepository    the user repository (required by CSessionService constructor)
	 * @param projectRepository the project repository (required by CSessionService constructor)
	 * @return a CSessionService instance that delegates to the web session service */
	@Bean
	@Primary
	@ConditionalOnWebApplication
	@Profile ("!reset-db")
	public CSessionService sessionServiceDelegate(final CWebSessionService webSessionService, final IUserRepository userRepository,
			final IProjectRepository projectRepository, final CUserCompanySettingsService userCompanySettingsService) {
		// Create an anonymous subclass that delegates to the web session service
		return new CSessionService(userRepository, projectRepository, userCompanySettingsService) {

			@Override
			public void setActiveUser(final tech.derbent.users.domain.CUser user) {
				webSessionService.setActiveUser(user);
			}

			@Override
			public java.util.Optional<tech.derbent.users.domain.CUser> getActiveUser() { return webSessionService.getActiveUser(); }

			@Override
			public void setActiveProject(final tech.derbent.projects.domain.CProject project) {
				webSessionService.setActiveProject(project);
			}

			@Override
			public java.util.Optional<tech.derbent.projects.domain.CProject> getActiveProject() { return webSessionService.getActiveProject(); }

			@Override
			public java.util.List<tech.derbent.projects.domain.CProject> getAvailableProjects() { return webSessionService.getAvailableProjects(); }

			@Override
			public Long getActiveId(final String entityType) {
				return webSessionService.getActiveId(entityType);
			}

			@Override
			public void setActiveId(final String entityType, final Long id) {
				webSessionService.setActiveId(entityType, id);
			}

			@Override
			public void clearSession() {
				webSessionService.clearSession();
			}

			@Override
			public void deleteAllActiveIds() {
				webSessionService.deleteAllActiveIds();
			}

			@Override
			public void addProjectChangeListener(final tech.derbent.api.interfaces.IProjectChangeListener listener) {
				webSessionService.addProjectChangeListener(listener);
			}

			@Override
			public void removeProjectChangeListener(final tech.derbent.api.interfaces.IProjectChangeListener listener) {
				webSessionService.removeProjectChangeListener(listener);
			}

			@Override
			public void addProjectListChangeListener(final tech.derbent.api.interfaces.IProjectListChangeListener listener) {
				webSessionService.addProjectListChangeListener(listener);
			}

			@Override
			public void removeProjectListChangeListener(final tech.derbent.api.interfaces.IProjectListChangeListener listener) {
				webSessionService.removeProjectListChangeListener(listener);
			}

			@Override
			public void notifyProjectListChanged() {
				webSessionService.notifyProjectListChanged();
			}

			@Override
			public void setLayoutService(final CLayoutService layoutService) {
				webSessionService.setLayoutService(layoutService);
			}

			@Override
			public void handleProjectListChange(final tech.derbent.projects.events.ProjectListChangeEvent event) {
				webSessionService.handleProjectListChange(event);
			}

			@Override
			public java.util.Optional<tech.derbent.companies.domain.CCompany> getActiveCompany() { return webSessionService.getActiveCompany(); }

			@Override
			public tech.derbent.companies.domain.CCompany getCurrentCompany() { return webSessionService.getCurrentCompany(); }
		};
	}
}
