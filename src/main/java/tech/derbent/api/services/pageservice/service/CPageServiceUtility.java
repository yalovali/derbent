package tech.derbent.api.services.pageservice.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.derbent.activities.service.CPageServiceActivity;
import tech.derbent.screens.service.CViewsService;

@Service
public class CPageServiceUtility {

	private static final List<String> availablePageServices =
			List.of("CPageServiceActivity", "CPageServiceBlog", "CPageServiceDocumentation", "CPageServiceHome", "CPageServiceProjectOverview");
	private static Logger LOGGER = LoggerFactory.getLogger(CPageServiceUtility.class);

	public static Class<?> getPageServiceClassByName(String serviceName) {
		switch (serviceName) {
		case "CPageServiceActivity":
			return CPageServiceActivity.class;
		case "CPageServiceBlog":
		case "CPageServiceDocumentation":
		case "CPageServiceHome":
		case "CPageServiceProjectOverview":
		default:
			LOGGER.error("Page service '{}' not implemented, defaulting to CPageServiceActivity", serviceName);
			throw new IllegalArgumentException("Page service not implemented: " + serviceName);
		}
	}

	@Autowired
	private final CViewsService viewsService;

	public CPageServiceUtility(CViewsService viewsService) {
		super();
		this.viewsService = viewsService;
	}

	public List<String> getPageServiceList() { return CPageServiceUtility.availablePageServices; }
}
