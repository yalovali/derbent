package tech.derbent.comments.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.derbent.comments.domain.CComment;

/**
 * CCommentView - UI component for displaying individual comments.
 * Layer: View (MVC)
 * 
 * Displays a comment as a div component with:
 * - Author name and event date header
 * - Comment text content
 * - Priority indicator
 * - Important flag indicator
 * - Styling based on priority and importance
 */
public class CCommentView extends Div {

    private static final Logger LOGGER = LoggerFactory.getLogger(CCommentView.class);
    private static final long serialVersionUID = 1L;
    
    private final CComment comment;
    private final VerticalLayout layout;
    private final HorizontalLayout headerLayout;
    private final Paragraph commentTextParagraph;

    /**
     * Creates a new CCommentView for the given comment.
     * @param comment the comment to display
     */
    public CCommentView(final CComment comment) {
        LOGGER.info("CCommentView constructor called with comment: {}", comment);
        
        if (comment == null) {
            throw new IllegalArgumentException("Comment cannot be null");
        }
        
        this.comment = comment;
        this.layout = new VerticalLayout();
        this.headerLayout = new HorizontalLayout();
        this.commentTextParagraph = new Paragraph();
        
        setupLayout();
        updateContent();
        addClassName("comment-view");
    }

    /**
     * Sets up the basic layout structure.
     */
    private void setupLayout() {
        LOGGER.debug("Setting up layout for comment view");
        
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.setWidthFull();
        
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(HorizontalLayout.JustifyContentMode.BETWEEN);
        headerLayout.setAlignItems(HorizontalLayout.Alignment.CENTER);
        
        commentTextParagraph.setWidthFull();
        
        layout.add(headerLayout, commentTextParagraph);
        add(layout);
    }

    /**
     * Updates the content of the comment view.
     */
    private void updateContent() {
        LOGGER.debug("Updating content for comment: {}", comment.getId());
        
        updateHeader();
        updateCommentText();
        updateStyling();
    }

    /**
     * Updates the header section with author, date, and indicators.
     */
    private void updateHeader() {
        headerLayout.removeAll();
        
        // Left side: Author and date
        final VerticalLayout leftSide = new VerticalLayout();
        leftSide.setPadding(false);
        leftSide.setSpacing(false);
        
        final H4 authorName = new H4(comment.getAuthorName());
        authorName.addClassName("comment-author");
        authorName.getStyle().set("margin", "0");
        
        final Span eventDate = new Span(comment.getEventDate().toString());
        eventDate.addClassName("comment-date");
        eventDate.getStyle().set("font-size", "0.875rem");
        eventDate.getStyle().set("color", "var(--lumo-secondary-text-color)");
        
        leftSide.add(authorName, eventDate);
        
        // Right side: Priority and importance indicators
        final HorizontalLayout rightSide = new HorizontalLayout();
        rightSide.setSpacing(true);
        rightSide.setAlignItems(HorizontalLayout.Alignment.CENTER);
        
        // Priority indicator
        if (comment.getPriority() != null) {
            final Span prioritySpan = new Span(comment.getPriorityName());
            prioritySpan.addClassName("comment-priority");
            prioritySpan.getStyle().set("padding", "0.25rem 0.5rem");
            prioritySpan.getStyle().set("border-radius", "12px");
            prioritySpan.getStyle().set("font-size", "0.75rem");
            prioritySpan.getStyle().set("font-weight", "bold");
            prioritySpan.getStyle().set("color", "white");
            prioritySpan.getStyle().set("background-color", comment.getPriority().getColor());
            rightSide.add(prioritySpan);
        }
        
        // Important indicator
        if (comment.isImportant()) {
            final Icon importantIcon = new Icon(VaadinIcon.STAR);
            importantIcon.addClassName("comment-important");
            importantIcon.getStyle().set("color", "#FFD700");
            importantIcon.setSize("1.2em");
            rightSide.add(importantIcon);
        }
        
        headerLayout.add(leftSide, rightSide);
    }

    /**
     * Updates the comment text content.
     */
    private void updateCommentText() {
        commentTextParagraph.setText(comment.getCommentText());
        commentTextParagraph.addClassName("comment-text");
        commentTextParagraph.getStyle().set("white-space", "pre-wrap");
        commentTextParagraph.getStyle().set("word-wrap", "break-word");
    }

    /**
     * Updates the styling based on comment properties.
     */
    private void updateStyling() {
        // Base styling
        getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
        getStyle().set("border-radius", "8px");
        getStyle().set("background-color", "var(--lumo-base-color)");
        getStyle().set("margin-bottom", "1rem");
        
        // Important comments get a different styling
        if (comment.isImportant()) {
            getStyle().set("border-left", "4px solid #FFD700");
            getStyle().set("background-color", "#FFFEF7");
            addClassName("comment-important");
        }
        
        // Priority-based styling
        if (comment.getPriority() != null) {
            final String priorityClass = "comment-priority-" + comment.getPriority().getPriorityLevel();
            addClassName(priorityClass);
        }
    }

    /**
     * Gets the comment associated with this view.
     * @return the comment
     */
    public CComment getComment() {
        return comment;
    }

    /**
     * Refreshes the view content (useful after comment updates).
     */
    public void refresh() {
        LOGGER.debug("Refreshing comment view for comment: {}", comment.getId());
        updateContent();
    }
}