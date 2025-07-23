package tech.derbent.examples.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

/**
 * Example view to demonstrate 4-level hierarchical menu structure.
 * This view shows how the hierarchical menu parses deep menu annotations.
 */
@Route("examples/hierarchy/deep/sample")
@PageTitle("Hierarchical Menu Example")
@Menu(order = 999, icon = "vaadin:tree-table", title = "Examples.Hierarchy.DeepMenu.Sample")
@PermitAll
public class CExampleHierarchicalMenuView extends Div {

    private static final long serialVersionUID = 1L;

    public CExampleHierarchicalMenuView() {
        // Create content to demonstrate the hierarchical menu
        final H2 title = new H2("Hierarchical Menu Example");
        
        final Paragraph description = new Paragraph(
            "This page demonstrates the 4-level hierarchical menu structure. " +
            "The menu path for this page is: Examples → Hierarchy → DeepMenu → Sample"
        );
        
        final Paragraph implementation = new Paragraph(
            "The hierarchical menu supports navigation up to 4 levels deep with " +
            "sliding animations, back button functionality, and automatic parsing " +
            "of @Menu annotations in the format: parentItem2.childItem1.childofchileitem1.finalItem"
        );
        
        final Paragraph features = new Paragraph(
            "Key features include: " +
            "• 4-level menu hierarchy support " +
            "• Sliding animations between levels " +
            "• Back button navigation " +
            "• Visual depth indicators " +
            "• Responsive design " +
            "• Integration with existing menu system"
        );
        
        add(title, description, implementation, features);
        
        // Add some styling
        addClassName("example-view");
        getStyle().set("padding", "2rem");
        getStyle().set("max-width", "800px");
    }
}