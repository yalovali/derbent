// name: Restart Debug (robust)
// description: Terminate, wait ~1s, then Debug Last — runs on UI thread
// toolbar: Script Shell
// keyboard: CTRL+ALT+P

var PlatformUI      = Packages.org.eclipse.ui.PlatformUI;
var IHandlerService = Packages.org.eclipse.ui.handlers.IHandlerService;
var Display         = Packages.org.eclipse.swt.widgets.Display;
var Thread          = Packages.java.lang.Thread;

function execCmd(commandId) {
  Display.getDefault().syncExec(new java.lang.Runnable({
    run: function () {
      var wb = PlatformUI.getWorkbench();
      var win = wb.getActiveWorkbenchWindow(); // may be null
      var service = null;

      // Prefer window-scoped handler service; fall back to workbench-scoped
      if (win != null) {
        service = win.getService(IHandlerService);
      }
      if (service == null) {
        service = wb.getService(IHandlerService);
      }
      if (service != null) {
        try {
          service.executeCommand(commandId, null);
        } catch (e) {
          // Uncomment for diagnostics:
          // print("executeCommand failed for " + commandId + ": " + e);
        }
      }
    }
  }));
}

// 1) Terminate current debug session
execCmd("org.eclipse.debug.ui.commands.Terminate");

// 2) Wait ~1 second
Thread.sleep(1000);

// 3) Debug Last (F11 equivalent)
execCmd("org.eclipse.debug.ui.commands.DebugLast");
