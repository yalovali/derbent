package tech.derbent.api.services.pageservice.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.derbent.screens.service.CViewsService;

@Service
public class CPageServiceUtility {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceUtility.class);
	@Autowired
	private final CViewsService viewsService;

	public CPageServiceUtility(CViewsService viewsService) {
		super();
		this.viewsService = viewsService;
	}

	public List<String> getPageServiceList() {
		LOGGER.info("Fetching available page services...");
		List<String> beans = viewsService.getAvailableBeans().stream().filter(b -> b.startsWith("CPageService")).toList();
		return beans;
	}
}
