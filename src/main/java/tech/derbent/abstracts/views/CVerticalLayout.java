package tech.derbent.abstracts.views;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class CVerticalLayout extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    public CVerticalLayout() {
        super();
        setSizeFull();
        setPadding(false);
        setSpacing(false);
    }

    public CVerticalLayout(final boolean padding, final boolean spacing, final boolean margin) {
        super();
        setPadding(padding);
        setSpacing(spacing);
        setMargin(margin);
        setSizeFull();
    }

    public CVerticalLayout(final String style) {
        this();
        addClassName(style);
    }

    public CVerticalLayout(final String style, final boolean isFullWidth) {
        this(style);

        if (isFullWidth) {
            setWidthFull();
        }
    }
}
