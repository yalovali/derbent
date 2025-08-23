// name: HelloWorld
// description: Simple Hello World script in Java
// toolbar: Script Shell
// keyboard: CTRL+ALT+H

print("Hello World from EASE Java Script!");

var ResourcesPlugin = Packages.org.eclipse.core.resources.ResourcesPlugin;
var ws = ResourcesPlugin.getWorkspace();
var projects = ws.getRoot().getProjects();
for (var i = 0; i < projects.length; i++) {
  if (projects[i].isOpen()) print("Project: " + projects[i].getName());
}
