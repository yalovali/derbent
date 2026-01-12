package tech.derbent.api.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile ("!bab")
@ComponentScan (
		basePackages = "tech.derbent",
		excludeFilters = {
				@ComponentScan.Filter (type = FilterType.REGEX, pattern = "tech\\.derbent\\.bab\\..*"),
				@ComponentScan.Filter (type = FilterType.REGEX, pattern = "tech\\.derbent\\.(DbResetApplication|SimpleDbResetApplication)")
		}
)
public class CDerbentComponentScanConfig { /****/ }
