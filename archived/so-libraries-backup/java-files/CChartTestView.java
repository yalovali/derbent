package tech.derbent.base.login.view;

import java.time.LocalDateTime;
import com.storedobject.chart.BarChart;
import com.storedobject.chart.CategoryData;
import com.storedobject.chart.ChartException;
import com.storedobject.chart.Color;
import com.storedobject.chart.Data;
import com.storedobject.chart.DataType;
import com.storedobject.chart.GanttChart;
import com.storedobject.chart.LineChart;
import com.storedobject.chart.PieChart;
import com.storedobject.chart.Project;
import com.storedobject.chart.RectangularCoordinate;
import com.storedobject.chart.SOChart;
import com.storedobject.chart.Title;
import com.storedobject.chart.Toolbox;
import com.storedobject.chart.XAxis;
import com.storedobject.chart.YAxis;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
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
@Menu (.1, icon = "class:tech.derbent.base.setup.view.CSystemSettingsView", title = "Chart Test")
@PermitAll // When security is enabled, allow all authenticated users
public class CChartTestView extends Main {

	public static final String DEFAULT_COLOR = "#00495f";
	public static final String DEFAULT_ICON = "vaadin:calendar";
	private static final long serialVersionUID = 1L;
	public static final String VIEW_NAME = "System Settings View";
	CVerticalLayout myLayout = new CVerticalLayout();

	public CChartTestView() {
		try {
			setSizeFull();
			add(myLayout);
			myLayout.setSizeFull();
			myLayout.setSpacing(true);
			myLayout.setPadding(true);
			myLayout.add(new H2("SOChart Test Examples"));
			myLayout.add(new Div("Simple chart examples using SO-Charts library for Vaadin"));
			simplePieChart();
			simpleBarChart();
			simpleLineChart();
			simpleGanttChart();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void simpleBarChart() throws ChartException, Exception {
		myLayout.add(new H2("2. Simple Bar Chart"));
		// Creating a chart display area
		SOChart soChart = new SOChart();
		soChart.setSize("800px", "400px");
		// Define data
		CategoryData labels = new CategoryData("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
		Data data = new Data(120, 200, 150, 80, 70);
		// Create bar chart with coordinate system
		BarChart barChart = new BarChart(labels, data);
		RectangularCoordinate rc = new RectangularCoordinate(new XAxis(DataType.CATEGORY), new YAxis(DataType.NUMBER));
		barChart.plotOn(rc);
		// Add title and toolbox
		Title title = new Title("Weekly Sales");
		Toolbox toolbox = new Toolbox();
		toolbox.addButton(new Toolbox.Download());
		// Add components to chart
		soChart.add(barChart, title, toolbox);
		// Update chart to trigger rendering
		soChart.update();
		// Add to layout
		myLayout.add(soChart);
	}

	private void simpleGanttChart() throws ChartException, Exception {
		myLayout.add(new H2("4. Simple Gantt Chart"));
		// Creating a chart display area
		SOChart soChart = new SOChart();
		soChart.setSize("800px", "400px");
		// Create a project with tasks
		Project project = new Project();
		project.setStart(LocalDateTime.now().minusDays(7));
		// Development phase
		Project.TaskGroup development = project.createTaskGroup("Development");
		Project.Task design = development.createTask("Design", 3);
		Project.Task coding = development.createTask("Implementation", 5);
		Project.Task testing = development.createTask("Testing", 4);
		coding.setCompleted(60); // 60% complete
		testing.setCompleted(20); // 20% complete
		project.dependsOn(coding, design);
		project.dependsOn(testing, coding);
		// Deployment phase
		Project.TaskGroup deployment = project.createTaskGroup("Deployment");
		Project.Task staging = deployment.createTask("Staging", 2);
		Project.Task production = deployment.createTask("Production", 1);
		production.setColor(new Color("green"));
		project.dependsOn(staging, testing);
		project.dependsOn(production, staging);
		// Create Gantt chart
		GanttChart ganttChart = new GanttChart(project);
		// Add title and toolbox
		Title title = new Title("Project Timeline");
		Toolbox toolbox = new Toolbox();
		toolbox.addButton(new Toolbox.Download());
		// Add components to chart
		soChart.add(ganttChart);
		soChart.add(title);
		soChart.add(toolbox);
		// Update chart to trigger rendering
		soChart.update();
		// Add to layout
		myLayout.add(soChart);
	}

	private void simpleLineChart() throws ChartException, Exception {
		myLayout.add(new H2("3. Simple Line Chart"));
		// Creating a chart display area
		SOChart soChart = new SOChart();
		soChart.setSize("800px", "400px");
		// Define data
		CategoryData labels = new CategoryData("Jan", "Feb", "Mar", "Apr", "May", "Jun");
		Data data = new Data(30, 45, 38, 52, 48, 60);
		// Create line chart with coordinate system
		LineChart lineChart = new LineChart(labels, data);
		RectangularCoordinate rc = new RectangularCoordinate(new XAxis(DataType.CATEGORY), new YAxis(DataType.NUMBER));
		lineChart.plotOn(rc);
		// Add title and toolbox
		Title title = new Title("Monthly Growth");
		Toolbox toolbox = new Toolbox();
		toolbox.addButton(new Toolbox.Download(), new Toolbox.Zoom());
		// Add components to chart
		soChart.add(lineChart, title, toolbox);
		// Update chart to trigger rendering
		soChart.update();
		// Add to layout
		myLayout.add(soChart);
	}

	private void simplePieChart() throws ChartException, Exception {
		myLayout.add(new H2("1. Simple Pie Chart"));
		// Creating a chart display area
		SOChart soChart = new SOChart();
		soChart.setSize("800px", "400px");
		// Define data
		CategoryData labels = new CategoryData("Banana", "Apple", "Orange", "Grapes");
		Data data = new Data(25, 40, 20, 30);
		// Create a simple pie chart
		PieChart pieChart = new PieChart(labels, data);
		// Add title
		Title title = new Title("Fruit Sales Distribution");
		// Add components to chart
		soChart.add(pieChart, title);
		// Update chart to trigger rendering
		soChart.update();
		// Add to layout
		myLayout.add(soChart);
	}
}
