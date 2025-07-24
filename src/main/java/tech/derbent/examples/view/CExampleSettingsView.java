package tech.derbent.examples.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

/**
 * Another example view for 3-level menu structure.
 */
@Route("examples/settings/advanced")
@PageTitle("Advanced Settings Example")
@Menu(order = 998, icon = "vaadin:cogs", title = "Examples.Settings.Advanced")
@PermitAll
public class CExampleSettingsView extends Div {

    private static final long serialVersionUID = 1L;

    public CExampleSettingsView() {
        final H2 title = new H2("Advanced Settings Example");
        
        final Paragraph description = new Paragraph(
            "This is a 3-level menu example. " +
            "Menu path: Examples → Settings → Advanced"
        );
        
        add(title, description);
        addClassName("example-view");
        getStyle().set("padding", "2rem");
    }
}