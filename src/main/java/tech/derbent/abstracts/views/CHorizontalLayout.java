package tech.derbent.abstracts.views;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class CHorizontalLayout extends HorizontalLayout {

    private static final long serialVersionUID = 1L;

    public CHorizontalLayout() {
        super();
        setSizeFull();
        setPadding(false);
        setSpacing(false);
    }

    public CHorizontalLayout(final boolean padding, final boolean spacing, final boolean margin) {
        super();
        setPadding(padding);
        setSpacing(spacing);
        setMargin(margin);
        setSizeFull();
    }

    public CHorizontalLayout(final String style) {
        this();
        addClassName(style);
    }

    public CHorizontalLayout(final String style, final boolean isFullWidth) {
        this(style);

        if (isFullWidth) {
            setWidthFull();
        }
    }
}
