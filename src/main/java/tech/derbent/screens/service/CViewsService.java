package tech.derbent.screens.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CViewsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CViewsService.class);

	public List<String> getAvailableBaseTypes() {
		LOGGER.debug("Retrieving available base types for views");
		return List.of("CActivity", "CMeeting", "CRisk", "CProject", "CUser");
	}
}