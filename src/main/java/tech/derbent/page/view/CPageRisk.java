package tech.derbent.page.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.risks.domain.CRisk;
import tech.derbent.risks.service.CRiskService;
import tech.derbent.risks.view.CRiskView;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CGridEntityService;
import tech.derbent.session.service.CSessionService;

@Route ("cpagerisk")
@PageTitle ("Risk Management")
@Menu (order = 1.2, icon = "vaadin:warning", title = "Risk Management")
@PermitAll // When security is enabled, allow all authenticated users
public class CPageRisk extends CPageGenericEntity<CRisk> {

	private static final long serialVersionUID = 1L;

	public CPageRisk(final CSessionService sessionService, final CGridEntityService gridEntityService, final CDetailSectionService screenService,
			final CRiskService riskService) {
		super(sessionService, screenService, gridEntityService, riskService, CRisk.class, CRiskView.VIEW_NAME);
	}

	public static String getStaticEntityColorCode() { return getStaticIconColorCode(); }

	public static String getStaticIconColorCode() {
		return CRisk.getIconColorCode(); // Red color for risk entities
	}

	public static String getStaticIconFilename() {
		return CRisk.getIconFilename(); // Warning icon
	}

	@Override
	public String getEntityColorCode() { return CRisk.getEntityColorCode(); }

	@Override
	public String getIconFilename() { return CRisk.getIconFilename(); }

	@Override
	protected CRisk createNewEntity() {
		CRisk newRisk = new CRisk();
		// Set project if available
		sessionService.getActiveProject().ifPresent(newRisk::setProject);
		return newRisk;
	}
}
