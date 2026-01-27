package tech.derbent.bab.project.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Pattern;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.projects.domain.CProject;

/** CProject_Bab - BAB Gateway-specific project with IP address support. Layer: Domain (MVC) Active when: 'bab' profile is active */
@Entity
@DiscriminatorValue ("BAB")
public class CProject_Bab extends CProject<CProject_Bab> {

	public static final String DEFAULT_COLOR = "#6B5FA7"; // CDE Purple - organizational entity
	public static final String DEFAULT_ICON = "vaadin:folder-open";
	public static final String ENTITY_TITLE_PLURAL = "BAB Gateway Projects";
	public static final String ENTITY_TITLE_SINGULAR = "BAB Gateway Project";
	public static final String VIEW_NAME = "BAB Gateway Projects View";
	@Column (name = "ip_address", length = 45) // 45 chars supports IPv6
	@Pattern (
			regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^([0-9a-fA-F]{0,4}:){7}[0-9a-fA-F]{0,4}$|^$",
			message = "Invalid IP address format (IPv4 or IPv6)"
	)
	@AMetaData (
			displayName = "IP Address", required = false, readOnly = false, description = "Gateway IP address for BAB projects (IPv4 or IPv6)",
			hidden = false, maxLength = 45
	)
	private String ipAddress;

	/** Default constructor for JPA. */
	protected CProject_Bab() {}

	public CProject_Bab(final String name, final CCompany company) {
		super(CProject_Bab.class, name, company);
		initializeDefaults();
	}

	@Override
	protected void copyEntityTo(final CEntityDB<?> target, @SuppressWarnings ("rawtypes") final CAbstractService serviceTarget,
			final CCloneOptions options) {
		super.copyEntityTo(target, serviceTarget, options);
		final boolean condition = target instanceof CProject_Bab && ipAddress != null && !ipAddress.isBlank();
		if (condition) {
			// Do NOT copy IP address directly - it should be unique per project
			((CProject_Bab) target).setIpAddress(null);
		}
	}

	public String getIpAddress() { return ipAddress; }

	private final void initializeDefaults() {
		ipAddress = "";
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	public void setIpAddress(final String ipAddress) {
		this.ipAddress = ipAddress;
		updateLastModified();
	}
}
