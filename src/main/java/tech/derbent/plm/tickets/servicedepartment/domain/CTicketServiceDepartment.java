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
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;
import tech.derbent.api.interfaces.ISearchable;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;

/** CTicketServiceDepartment - Service department entity for ticket assignment. Departments have multiple responsible users who are notified when
 * tickets are assigned. Follows the team pattern with manager and members structure.
 * @author Derbent Team
 * @since 1.0 */
@Entity
@Table (
		name = "cticket_service_department",
		uniqueConstraints = @jakarta.persistence.UniqueConstraint (name = "uk_service_dept_name_company", columnNames = {
				"name", "company_id"
		})
)
@AttributeOverride (name = "id", column = @Column (name = "service_department_id"))
public class CTicketServiceDepartment extends CEntityOfCompany<CTicketServiceDepartment> implements ISearchable, IHasAttachments, IHasComments {

	public static final String DEFAULT_COLOR = "#3A5791"; // Darker blue - matches ticket color
	public static final String DEFAULT_ICON = "vaadin:sitemap";
	public static final String ENTITY_TITLE_PLURAL = "Service Departments";
	public static final String ENTITY_TITLE_SINGULAR = "Service Department";
	private static final Logger LOGGER = LoggerFactory.getLogger(CTicketServiceDepartment.class);
	public static final String VIEW_NAME = "Service Department Management";
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "service_department_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "Documents and files attached to this department",
			hidden = false, dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "service_department_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this department", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponentComment"
	)
	private Set<CComment> comments = new HashSet<>();
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "department_manager_id", nullable = true)
	@AMetaData (
			displayName = "Department Manager", required = false, readOnly = false, description = "Manager responsible for this service department",
			hidden = false, dataProviderBean = "CUserService", setBackgroundFromColor = true, useIcon = true
	)
	private CUser departmentManager;
	@Column (name = "email_notification_enabled", nullable = false)
	@AMetaData (
			displayName = "Email Notifications", required = false, readOnly = false, defaultValue = "true",
			description = "Enable email notifications to responsible users when tickets are assigned", hidden = false
	)
	private Boolean emailNotificationEnabled = true;
	@Column (name = "is_active", nullable = false)
	@AMetaData (
			displayName = "Is Active", required = false, readOnly = false, defaultValue = "true",
			description = "Whether this department is actively accepting tickets", hidden = false
	)
	private Boolean isActive = true;
	@ManyToMany (fetch = FetchType.LAZY)
	@JoinTable (
			name = "cticket_service_dept_responsibles", joinColumns = @JoinColumn (name = "service_department_id"),
			inverseJoinColumns = @JoinColumn (name = "user_id")
	)
	@AMetaData (
			displayName = "Responsible Users", required = false, readOnly = false,
			description = "Users responsible for tickets assigned to this department (will receive email notifications)", hidden = false,
			useDualListSelector = true, dataProviderBean = "CUserService"
	)
	private Set<CUser> responsibleUsers = new HashSet<>();

	/** Default constructor for JPA. */
										/** Default constructor for JPA. */
	protected CTicketServiceDepartment() {}

	/** Constructor with name only.
	 * @param name the name of the service department */
	public CTicketServiceDepartment(final String name) {
		super(CTicketServiceDepartment.class, name, null);
		initializeDefaults();
	}

	/** Constructor with name and company.
	 * @param name    the name of the service department
	 * @param company the company this department belongs to */
	public CTicketServiceDepartment(final String name, final CCompany company) {
		super(CTicketServiceDepartment.class, name, company);
		initializeDefaults();
	}

	/** Add a responsible user to the department.
	 * @param user the user to add */
	public void addResponsibleUser(final CUser user) {
		if (user == null) {
			return;
		}
		responsibleUsers.add(user);
		updateLastModified();
		LOGGER.debug("Added responsible user {} to department {}", user.getName(), getName());
	}

	@Override
	public Set<CAttachment> getAttachments() { return attachments; }

	@Override
	public Set<CComment> getComments() { return comments; }

	public CUser getDepartmentManager() { return departmentManager; }

	public Boolean getEmailNotificationEnabled() { return emailNotificationEnabled; }

	public Boolean getIsActive() { return isActive; }

	/** Get all email addresses of responsible users for notifications.
	 * @return set of email addresses */
	public Set<String> getResponsibleUserEmails() {
		final Set<String> emails = new HashSet<>();
		if (responsibleUsers != null) {
			responsibleUsers.forEach((final CUser user) -> {
				if (user != null && user.getEmail() != null && !user.getEmail().isBlank()) {
					emails.add(user.getEmail());
				}
			});
		}
		// Include manager email if set
		if (departmentManager != null && departmentManager.getEmail() != null && !departmentManager.getEmail().isBlank()) {
			emails.add(departmentManager.getEmail());
		}
		return emails;
	}
	// Getters and Setters

	public Set<CUser> getResponsibleUsers() { return responsibleUsers; }

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

	private final void initializeDefaults() {
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	/** Check if the department is active and accepting tickets.
	 * @return true if active, false otherwise */
	public boolean isActive() { return Boolean.TRUE.equals(isActive); }

	/** Check if email notifications are enabled.
	 * @return true if enabled, false otherwise */
	public boolean isEmailNotificationEnabled() {
		return Boolean.TRUE.equals(emailNotificationEnabled);
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
		if (departmentManager != null && departmentManager.getName() != null && departmentManager.getName().toLowerCase().contains(lowerSearchText)) {
			return true;
		}
		if (getCompany() != null && getCompany().getName() != null && getCompany().getName().toLowerCase().contains(lowerSearchText)) {
			return true;
		}
		return false;
	}

	/** Remove a responsible user from the department.
	 * @param user the user to remove */
	public void removeResponsibleUser(final CUser user) {
		if (user == null) {
			return;
		}
		responsibleUsers.remove(user);
		updateLastModified();
		LOGGER.debug("Removed responsible user {} from department {}", user.getName(), getName());
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
		return String.format("CTicketServiceDepartment{id=%d, name='%s', active=%s, responsibleUsers=%d, manager=%s}", getId(), getName(), isActive,
				responsibleUsers != null ? responsibleUsers.size() : 0, departmentManager != null ? departmentManager.getName() : "none");
	}
}
