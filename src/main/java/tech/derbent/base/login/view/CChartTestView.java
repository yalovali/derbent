package tech.derbent.base.login.view;

import com.storedobject.chart.BarChart;
import com.storedobject.chart.CategoryData;
import com.storedobject.chart.Data;
import com.storedobject.chart.DataType;
import com.storedobject.chart.NightingaleRoseChart;
import com.storedobject.chart.Position;
import com.storedobject.chart.RectangularCoordinate;
import com.storedobject.chart.SOChart;
import com.storedobject.chart.Size;
import com.storedobject.chart.Title;
import com.storedobject.chart.Toolbox;
import com.storedobject.chart.XAxis;
import com.storedobject.chart.YAxis;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.views.components.CVerticalLayout;

@AnonymousAllowed
@Route (value = "chart", autoLayout = false)
@PageTitle ("Chart Test View")
@Menu (order = 100.1, icon = "class:tech.derbent.base.setup.view.CSystemSettingsView", title = "Chart Test")
@PermitAll // When security is enabled, allow all authenticated users
public class CChartTestView extends Main {

	private static final long serialVersionUID = 1L;
	CVerticalLayout myLayout = new CVerticalLayout();

	public CChartTestView() {
		add(new Div("This is a custom login view"));
		add(myLayout);
		sample1();
	}

	private void sample1() {
		// Creating a chart display area.
		SOChart soChart = new SOChart();
		soChart.setSize("800px", "500px");
		// Let us define some inline data.
		CategoryData labels = new CategoryData("Banana", "Apple", "Orange", "Grapes");
		Data data = new Data(25, 40, 20, 30);
		// We are going to create a couple of charts. So, each chart should be positioned
		// appropriately.
		// Create a self-positioning chart.
		NightingaleRoseChart nc = new NightingaleRoseChart(labels, data);
		Position p = new Position();
		p.setTop(Size.percentage(50));
		nc.setPosition(p); // Position it leaving 50% space at the top
		// Second chart to add.
		BarChart bc = new BarChart(labels, data);
		RectangularCoordinate rc;
		rc = new RectangularCoordinate(new XAxis(DataType.CATEGORY), new YAxis(DataType.NUMBER));
		p = new Position();
		p.setBottom(Size.percentage(55));
		rc.setPosition(p); // Position it leaving 55% space at the bottom
		bc.plotOn(rc); // Bar chart needs to be plotted on a coordinate system
		// Just to demonstrate it, we are creating a "Download" and a "Zoom" toolbox button.
		Toolbox toolbox = new Toolbox();
		toolbox.addButton(new Toolbox.Download(), new Toolbox.Zoom());
		// Let's add some titles.
		Title title = new Title("My First Chart");
		title.setSubtext("2nd Line of the Title");
		// Add the chart components to the chart display area.
		soChart.add(nc, bc, toolbox, title);
		// Now, add the chart display (which is a Vaadin Component) to your layout.
		myLayout.add(soChart);
	}
}
