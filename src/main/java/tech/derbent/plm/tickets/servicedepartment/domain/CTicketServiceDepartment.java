package tech.derbent.plm.tickets.servicedepartment.domain;

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
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;
import tech.derbent.api.interfaces.ISearchable;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;

/**
 * CTicketServiceDepartment - Service department entity for ticket assignment.
 * Departments have multiple responsible users who are notified when tickets are assigned.
 * Follows the team pattern with manager and members structure.
 *
 * @author Derbent Team
 * @since 1.0
 */
@Entity
@Table(name = "cticket_service_department", uniqueConstraints = @jakarta.persistence.UniqueConstraint(
		name = "uk_service_dept_name_company",
		columnNames = {"name", "company_id"}
))
@AttributeOverride(name = "id", column = @Column(name = "service_department_id"))
public class CTicketServiceDepartment extends CEntityOfCompany<CTicketServiceDepartment>
		implements ISearchable, IHasAttachments, IHasComments {

	public static final String DEFAULT_COLOR = "#3A5791"; // Darker blue - matches ticket color
	public static final String DEFAULT_ICON = "vaadin:sitemap";
	public static final String ENTITY_TITLE_PLURAL = "Service Departments";
	public static final String ENTITY_TITLE_SINGULAR = "Service Department";
	private static final Logger LOGGER = LoggerFactory.getLogger(CTicketServiceDepartment.class);
	public static final String VIEW_NAME = "Service Department Management";

	@Column(nullable = true, length = 2000)
	@Size(max = 2000)
	@AMetaData(
			displayName = "Description", required = false, readOnly = false,
			description = "Detailed description of the service department", hidden = false, maxLength = 2000
	)
	private String description;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "cticket_service_dept_responsibles",
			joinColumns = @JoinColumn(name = "service_department_id"),
			inverseJoinColumns = @JoinColumn(name = "user_id")
	)
	@AMetaData(
			displayName = "Responsible Users", required = false, readOnly = false,
			description = "Users responsible for tickets assigned to this department (will receive email notifications)",
			hidden = false, useDualListSelector = true, dataProviderBean = "CUserService"
	)
	private Set<CUser> responsibleUsers = new HashSet<>();

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "department_manager_id", nullable = true)
	@AMetaData(
			displayName = "Department Manager", required = false, readOnly = false,
			description = "Manager responsible for this service department", hidden = false,
			dataProviderBean = "CUserService", setBackgroundFromColor = true, useIcon = true
	)
	private CUser departmentManager;

	@Column(name = "is_active", nullable = false)
	@AMetaData(
			displayName = "Is Active", required = false, readOnly = false, defaultValue = "true",
			description = "Whether this department is actively accepting tickets", hidden = false
	)
	private Boolean isActive = true;

	@Column(name = "email_notification_enabled", nullable = false)
	@AMetaData(
			displayName = "Email Notifications", required = false, readOnly = false, defaultValue = "true",
			description = "Enable email notifications to responsible users when tickets are assigned", hidden = false
	)
	private Boolean emailNotificationEnabled = true;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "service_department_id")
	@AMetaData(
			displayName = "Attachments", required = false, readOnly = false,
			description = "Documents and files attached to this department", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "service_department_id")
	@AMetaData(
			displayName = "Comments", required = false, readOnly = false,
			description = "Comments for this department", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();

	/** Default constructor for JPA. */
	public CTicketServiceDepartment() {
		super();
	}

	/**
	 * Constructor with name only.
	 *
	 * @param name the name of the service department
	 */
	public CTicketServiceDepartment(final String name) {
		super(CTicketServiceDepartment.class, name, null);
		initializeDefaults();
	}

	/**
	 * Constructor with name and company.
	 *
	 * @param name    the name of the service department
	 * @param company the company this department belongs to
	 */
	public CTicketServiceDepartment(final String name, final CCompany company) {
		super(CTicketServiceDepartment.class, name, company);
		initializeDefaults();
	}

	/**
	 * Add a responsible user to the department.
	 *
	 * @param user the user to add
	 */
	public void addResponsibleUser(final CUser user) {
		if (user != null) {
			responsibleUsers.add(user);
			updateLastModified();
			LOGGER.debug("Added responsible user {} to department {}", user.getName(), getName());
		}
	}

	/**
	 * Remove a responsible user from the department.
	 *
	 * @param user the user to remove
	 */
	public void removeResponsibleUser(final CUser user) {
		if (user != null) {
			responsibleUsers.remove(user);
			updateLastModified();
			LOGGER.debug("Removed responsible user {} from department {}", user.getName(), getName());
		}
	}

	/**
	 * Check if the department is active and accepting tickets.
	 *
	 * @return true if active, false otherwise
	 */
	public boolean isActive() {
		return Boolean.TRUE.equals(isActive);
	}

	/**
	 * Check if email notifications are enabled.
	 *
	 * @return true if enabled, false otherwise
	 */
	public boolean isEmailNotificationEnabled() {
		return Boolean.TRUE.equals(emailNotificationEnabled);
	}

	/**
	 * Get all email addresses of responsible users for notifications.
	 *
	 * @return set of email addresses
	 */
	public Set<String> getResponsibleUserEmails() {
		final Set<String> emails = new HashSet<>();
		if (responsibleUsers != null) {
			for (CUser user : responsibleUsers) {
				if (user != null && user.getEmail() != null && !user.getEmail().isBlank()) {
					emails.add(user.getEmail());
				}
			}
		}
		// Include manager email if set
		if (departmentManager != null && departmentManager.getEmail() != null && !departmentManager.getEmail().isBlank()) {
			emails.add(departmentManager.getEmail());
		}
		return emails;
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		isActive = true;
		emailNotificationEnabled = true;
		responsibleUsers = new HashSet<>();
		attachments = new HashSet<>();
		comments = new HashSet<>();
	}

	@Override
	public void initializeAllFields() {
		super.initializeAllFields();
		if (responsibleUsers != null) {
			responsibleUsers.size();
		}
		if (departmentManager != null && departmentManager.getId() != null) {
			departmentManager.getName();
		}
		if (attachments != null) {
			attachments.size();
		}
		if (comments != null) {
			comments.size();
		}
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
		if (description != null && description.toLowerCase().contains(lowerSearchText)) {
			return true;
		}
		if (departmentManager != null && departmentManager.getName() != null
				&& departmentManager.getName().toLowerCase().contains(lowerSearchText)) {
			return true;
		}
		if (getCompany() != null && getCompany().getName() != null
				&& getCompany().getName().toLowerCase().contains(lowerSearchText)) {
			return true;
		}
		return false;
	}

	// Getters and Setters

	@Override
	public Set<CAttachment> getAttachments() {
		if (attachments == null) {
			attachments = new HashSet<>();
		}
		return attachments;
	}

	@Override
	public Set<CComment> getComments() {
		if (comments == null) {
			comments = new HashSet<>();
		}
		return comments;
	}

	public CUser getDepartmentManager() { return departmentManager; }

	@Override
	public String getDescription() { return description; }

	public Boolean getEmailNotificationEnabled() { return emailNotificationEnabled; }

	public Boolean getIsActive() { return isActive; }

	public Set<CUser> getResponsibleUsers() {
		if (responsibleUsers == null) {
			responsibleUsers = new HashSet<>();
		}
		return responsibleUsers;
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

	public void setDepartmentManager(final CUser departmentManager) {
		this.departmentManager = departmentManager;
		updateLastModified();
	}

	@Override
	public void setDescription(final String description) {
		this.description = description;
		updateLastModified();
	}

	public void setEmailNotificationEnabled(final Boolean emailNotificationEnabled) {
		this.emailNotificationEnabled = emailNotificationEnabled;
		updateLastModified();
	}

	public void setIsActive(final Boolean isActive) {
		this.isActive = isActive;
		updateLastModified();
	}

	public void setResponsibleUsers(final Set<CUser> responsibleUsers) {
		this.responsibleUsers = responsibleUsers != null ? responsibleUsers : new HashSet<>();
		updateLastModified();
	}

	@Override
	public String toString() {
		return String.format("CTicketServiceDepartment{id=%d, name='%s', active=%s, responsibleUsers=%d, manager=%s}",
				getId(), getName(), isActive, responsibleUsers != null ? responsibleUsers.size() : 0,
				departmentManager != null ? departmentManager.getName() : "none");
	}
}
