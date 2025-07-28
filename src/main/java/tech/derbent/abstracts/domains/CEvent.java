package tech.derbent.abstracts.domains;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

/**
 * CEvent - Abstract base class for event-based entities in the system.
 * Layer: Domain (MVC)
 * 
 * Provides common fields and functionality for event-like entities such as:
 * - Comments
 * - Notifications
 * - Activity logs
 * - Status changes
 * 
 * This class extends CEntityOfProject to maintain project context for all events.
 */
@MappedSuperclass
public abstract class CEvent extends CEntityOfProject {

    private static final Logger LOGGER = LoggerFactory.getLogger(CEvent.class);

    // Event timestamp - when the event occurred
    @Column(name = "event_date", nullable = false)
    @MetaData(
        displayName = "Event Date", required = true, readOnly = true,
        description = "Date and time when the event occurred", hidden = false,
        order = 90
    )
    private LocalDateTime eventDate;

    // Author of the event
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @MetaData(
        displayName = "Author", required = true, readOnly = true,
        description = "User who created this event", hidden = false, order = 91,
        dataProviderBean = "CUserService"
    )
    private CUser author;

    // Default constructor for JPA
    public CEvent() {
        super();
        this.eventDate = LocalDateTime.now();
    }

    public CEvent(final CProject project, final CUser author) {
        super(project);
        this.author = author;
        this.eventDate = LocalDateTime.now();
    }

    public CEvent(final String name, final CProject project, final CUser author) {
        super(name, project);
        this.author = author;
        this.eventDate = LocalDateTime.now();
    }

    public LocalDateTime getEventDate() {
        return eventDate;
    }

    public void setEventDate(final LocalDateTime eventDate) {
        LOGGER.info("setEventDate called with eventDate: {}", eventDate);
        this.eventDate = eventDate;
        updateLastModified();
    }

    public CUser getAuthor() {
        return author;
    }

    public void setAuthor(final CUser author) {
        LOGGER.info("setAuthor called with author: {}", author);
        this.author = author;
        updateLastModified();
    }

    public String getAuthorName() {
        return (author != null) ? author.getName() : "Unknown Author";
    }

    @Override
    protected void initializeDefaults() {
        super.initializeDefaults();
        
        if (this.eventDate == null) {
            this.eventDate = LocalDateTime.now();
        }
    }

    @Override
    public String toString() {
        return String.format("%s{eventDate=%s, author=%s}", 
            super.toString(), eventDate, getAuthorName());
    }
}