package tech.derbent;

import java.time.Clock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
/* build dizini altinda degilse, sprintboot app ilgili classlari gezip sistemleri yaratmiyor.
 *
 * Bu durumda, build dizini altinda olmasi icin, src/main/java/tech/derbent/Application.java
 * olarak konumlandirildi.
 *
 * yada su sekilde tanimlar yapilmali:
@ComponentScan(basePackages = "users.service")
@EnableJpaRepositories(basePackages = "users.service")
@EntityScan(basePackages = "users.domain")
 *
 */

@SpringBootApplication
@Theme("default")
public class Application implements AppShellConfigurator {

	private static final long serialVersionUID = 1L;

	public static void main(final String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public Clock clock() {
		return Clock.systemDefaultZone(); // You can also use Clock.systemUTC()
	}
}
