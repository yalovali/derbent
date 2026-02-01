package tech.derbent.plm.teams.team.domain;

import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;
import tech.derbent.api.interfaces.ISearchable;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;

@Entity
@Table (name = "cteam", uniqueConstraints = @jakarta.persistence.UniqueConstraint (name = "uk_team_name_company", columnNames = {
		"name", "company_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "team_id"))
public class CTeam extends CEntityOfCompany<CTeam> implements ISearchable, IHasAttachments, IHasComments {

	public static final String DEFAULT_COLOR = "#4B7F82"; // CDE Green - collaborative/people
	public static final String DEFAULT_ICON = "vaadin:group";
	public static final String ENTITY_TITLE_PLURAL = "Teams";
	public static final String ENTITY_TITLE_SINGULAR = "Team";
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CTeam.class);
	public static final String VIEW_NAME = "Teams View";
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "team_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "Documents and files attached to this team", hidden = false
	)
	private Set<CAttachment> attachments = new HashSet<>();
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "team_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this team", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponentComment"
	)
	private Set<CComment> comments = new HashSet<>();
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
	protected CTeam() {
	}

	public CTeam(final String name) {
		super(CTeam.class, name, null);
		initializeDefaults();
	}

	public CTeam(final String name, final CCompany company) {
		super(CTeam.class, name, company);
		initializeDefaults();
	}

	public void addMember(final CUser user) {
		if (user == null) {
			return;
		}
		members.add(user);
		updateLastModified();
	}

	@Override
	public Set<CAttachment> getAttachments() { return attachments; }

	@Override
	public Set<CComment> getComments() { return comments; }

	public Set<CUser> getMembers() { return members; }

	public CUser getTeamManager() { return teamManager; }

	@Override
	public void initializeAllFields() {
		super.initializeAllFields();
		if (members != null) {
			members.size();
		}
		if (teamManager != null && teamManager.getId() != null) {
			teamManager.getName();
		}
		if (attachments != null) {
			attachments.size();
		}
		if (comments != null) {
			comments.size();
		}
	}

	private final void initializeDefaults() {
		teamManager = null;
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	@Override
	public boolean matches(final String searchText) {
		if (searchText == null || searchText.trim().isEmpty()) {
			return true;
		}
		final String lowerSearchText = searchText.toLowerCase().trim();
		if (getName() != null && getName().toLowerCase().contains(lowerSearchText)) {
			return true;
		}
		if (teamManager != null && teamManager.getName() != null && teamManager.getName().toLowerCase().contains(lowerSearchText)) {
			return true;
		}
		if (getCompany() != null && getCompany().getName() != null && getCompany().getName().toLowerCase().contains(lowerSearchText)) {
			return true;
		}
		return false;
	}

	public void removeMember(final CUser user) {
		if (user == null) {
			return;
		}
		members.remove(user);
		updateLastModified();
	}

	@Override
	public void setAttachments(final Set<CAttachment> attachments) {
		this.attachments = attachments != null ? attachments : new HashSet<>();
		updateLastModified();
	}

	@Override
	public void setComments(final Set<CComment> comments) {
		this.comments = comments != null ? comments : new HashSet<>();
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
