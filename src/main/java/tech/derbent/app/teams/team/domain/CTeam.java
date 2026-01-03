package tech.derbent.app.teams.team.domain;

import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.app.companies.domain.CCompany;
import tech.derbent.base.users.domain.CUser;

/** CTeam - Team entity for grouping users Represents a team of users with a manager, associated with a company */
@Entity
@Table (name = "\"cteam\"", uniqueConstraints = @jakarta.persistence.UniqueConstraint (
	name = "uk_team_name_company",
	columnNames = {"name", "company_id"}
))
@AttributeOverride (name = "id", column = @Column (name = "team_id"))
public class CTeam extends CEntityNamed<CTeam> {

	public static final String DEFAULT_COLOR = "#4B7F82"; // CDE Green - collaborative/people
	public static final String DEFAULT_ICON = "vaadin:group";
	public static final String ENTITY_TITLE_PLURAL = "Teams";
	public static final String ENTITY_TITLE_SINGULAR = "Team";
	public static final String VIEW_NAME = "Teams View";
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "company_id", nullable = false)
	@AMetaData (
			displayName = "Company", required = true, readOnly = false, description = "Company this team belongs to", hidden = false, 
			dataProviderBean = "CCompanyService", setBackgroundFromColor = true, useIcon = true
	)
	private CCompany company;
	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Description", required = false, readOnly = false, description = "Detailed description of the team", hidden = false,
			 maxLength = 2000
	)
	private String description;
	@ManyToMany (fetch = FetchType.LAZY)
	@JoinTable (name = "cteam_members", joinColumns = @JoinColumn (name = "team_id"), inverseJoinColumns = @JoinColumn (name = "user_id"))
	@AMetaData (
			displayName = "Team Members", required = false, readOnly = false, description = "Users who are members of this team", hidden = false,
			 useDualListSelector = true, dataProviderBean = "CUserService"
	)
	private Set<CUser> members = new HashSet<>();
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "team_manager_id", nullable = true)
	@AMetaData (
			displayName = "Team Manager", required = false, readOnly = false, description = "Manager responsible for this team", hidden = false,
			 dataProviderBean = "CUserService", setBackgroundFromColor = true, useIcon = true
	)
	private CUser teamManager;

	/** Default constructor for JPA. */
	public CTeam() {
		super();
	}

	public CTeam(final String name) {
		super(CTeam.class, name);
	}

	public CTeam(final String name, final CCompany company) {
		super(CTeam.class, name);
		this.company = company;
	}

	public void addMember(final CUser user) {
		if (user != null) {
			members.add(user);
			updateLastModified();
		}
	}

	public CCompany getCompany() { return company; }

	@Override
	public String getDescription() { return description; }

	public Set<CUser> getMembers() {
		if (members == null) {
			members = new HashSet<>();
		}
		return members;
	}

	@Override
	public void initializeAllFields() {
		if (members != null) {
			members.size();
		}
	}

	public CUser getTeamManager() { return teamManager; }

	public void removeMember(final CUser user) {
		if (user != null) {
			members.remove(user);
			updateLastModified();
		}
	}

	public void setCompany(final CCompany company) {
		this.company = company;
		updateLastModified();
	}

	@Override
	public void setDescription(final String description) {
		this.description = description;
		updateLastModified();
	}

	public void setMembers(final Set<CUser> members) {
		this.members = members != null ? members : new HashSet<>();
		updateLastModified();
	}

	public void setTeamManager(final CUser teamManager) {
		this.teamManager = teamManager;
		updateLastModified();
	}
}
