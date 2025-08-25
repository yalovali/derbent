// name: Hello Toolbar
// description: Test button
// toolbar: org.eclipse.ui.main.toolbar
// image: platform:/plugin/org.eclipse.debug.ui/icons/full/elcl16/relaunch_debug.png
 
print("Hello World from EASE Java Script!");

var ResourcesPlugin = Packages.org.eclipse.core.resources.ResourcesPlugin;
var ws = ResourcesPlugin.getWorkspace();
var projects = ws.getRoot().getProjects();
for (var i = 0; i < projects.length; i++) {
  if (projects[i].isOpen()) print("Project: " + projects[i].getName());
}
