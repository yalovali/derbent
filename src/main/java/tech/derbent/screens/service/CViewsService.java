package tech.derbent.screens.service;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class CViewsService {

	public List<String> getAvailableBaseTypes() {
		return List.of("CActivity", "CMeeting", "CRisk", "CProject", "CUser");
	}
}
