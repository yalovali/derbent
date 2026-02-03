# Quick Implementation Guide: Services API Client

**Date**: 2026-02-03  
**Project**: Derbent/BAB  
**Integration**: Calimero Services HTTP API

---

## Quick Start Checklist

### Phase 1: DTOs (30 minutes)

1. **Create `ServiceInfo.java`** in `src/main/java/io/github/cenk1cenk2/bab/dto/calimero/`
   - Copy from CALIMERO_SERVICES_API.md
   - Add Jackson annotations
   - Generate getters/setters

2. **Create `ServiceStatus.java`**
   - Copy from CALIMERO_SERVICES_API.md
   - Add Jackson annotations
   - Generate getters/setters

3. **Create `ServiceActionResult.java`**
   - Copy from CALIMERO_SERVICES_API.md
   - Add Jackson annotations
   - Add `isSuccess()` method

4. **Create `ServiceListRequest.java`**
   - Builder pattern for optional parameters
   - activeOnly, runningOnly, filter fields

### Phase 2: Client Implementation (45 minutes)

1. **Create `CalimeroServicesClient.java`** extending `CalimeroClientBase`
   - Location: `src/main/java/io/github/cenk1cenk2/bab/client/calimero/`
   - MESSAGE_TYPE = "systemservices"
   - Implement all 8 operations:
     - `listServices()` / `listServices(ServiceListRequest)`
     - `getServiceStatus(String serviceName)`
     - `startService(String serviceName)`
     - `stopService(String serviceName)`
     - `restartService(String serviceName)`
     - `reloadService(String serviceName)`
     - `enableService(String serviceName)`
     - `disableService(String serviceName)`

2. **Add to `CalimeroClientFactory.java`**
   ```java
   public CalimeroServicesClient createServicesClient() {
       return new CalimeroServicesClient(baseUrl, authToken);
   }
   ```

### Phase 3: Testing (30 minutes)

1. **Create `CalimeroServicesClientTest.java`**
   - Test listServices()
   - Test getServiceStatus()
   - Test control operations
   - Mock HTTP responses

2. **Manual Testing**
   ```bash
   # Use curl commands from CALIMERO_SERVICES_API.md
   # Test against real Calimero server
   ```

### Phase 4: UI Integration (2-3 hours)

1. **Create `ServicesView.java`** (Vaadin View)
   - Grid showing services list
   - Filters: active only, running only, search box
   - Actions column: start/stop/restart buttons
   - Status indicators (running=green, failed=red, stopped=gray)

2. **Create `ServiceDetailsDialog.java`**
   - Show full service status
   - Recent logs
   - Enable/disable boot start
   - Reload configuration button

3. **Add to Navigation Menu**
   - "System" → "Services" menu item
   - Icon: VaadinIcon.COG

4. **Implement Auto-Refresh**
   - Poll services list every 5 seconds
   - Update status indicators in real-time
   - Show notification on service state changes

---

## Code Templates

### 1. Grid Setup (ServicesView.java)

```java
@Route(value = "systemservices", layout = MainLayout.class)
@PageTitle("System Services")
public class ServicesView extends VerticalLayout {
    
    private final CalimeroServicesClient client;
    private final Grid<ServiceInfo> grid = new Grid<>();
    
    private TextField filterField;
    private Checkbox activeOnlyCheckbox;
    private Checkbox runningOnlyCheckbox;
    
    public ServicesView(CalimeroClientFactory clientFactory) {
        this.client = clientFactory.createServicesClient();
        
        addClassName("services-view");
        setSizeFull();
        
        createToolbar();
        createGrid();
        loadServices();
        
        // Auto-refresh every 5 seconds
        UI.getCurrent().getPage().addPollListener(e -> refreshServices());
        UI.getCurrent().setPollInterval(5000);
    }
    
    private void createToolbar() {
        filterField = new TextField("Filter");
        filterField.setPlaceholder("Search services...");
        filterField.setClearButtonVisible(true);
        filterField.addValueChangeListener(e -> loadServices());
        
        activeOnlyCheckbox = new Checkbox("Active only");
        activeOnlyCheckbox.addValueChangeListener(e -> loadServices());
        
        runningOnlyCheckbox = new Checkbox("Running only");
        runningOnlyCheckbox.addValueChangeListener(e -> loadServices());
        
        Button refreshButton = new Button("Refresh", VaadinIcon.REFRESH.create());
        refreshButton.addClickListener(e -> loadServices());
        
        HorizontalLayout toolbar = new HorizontalLayout(
            filterField, activeOnlyCheckbox, runningOnlyCheckbox, refreshButton
        );
        toolbar.setAlignItems(Alignment.END);
        toolbar.setWidthFull();
        
        add(toolbar);
    }
    
    private void createGrid() {
        grid.addColumn(ServiceInfo::getName)
            .setHeader("Service")
            .setSortable(true)
            .setAutoWidth(true);
        
        grid.addComponentColumn(this::createStatusBadge)
            .setHeader("Status")
            .setAutoWidth(true);
        
        grid.addColumn(ServiceInfo::getDescription)
            .setHeader("Description")
            .setFlexGrow(1);
        
        grid.addComponentColumn(this::createActionButtons)
            .setHeader("Actions")
            .setAutoWidth(true);
        
        grid.setSizeFull();
        add(grid);
        expand(grid);
    }
    
    private Component createStatusBadge(ServiceInfo service) {
        Span badge = new Span();
        if (service.isRunning()) {
            badge.setText("RUNNING");
            badge.getElement().getThemeList().add("badge success");
        } else if (service.isFailed()) {
            badge.setText("FAILED");
            badge.getElement().getThemeList().add("badge error");
        } else if (service.isActive()) {
            badge.setText("ACTIVE");
            badge.getElement().getThemeList().add("badge");
        } else {
            badge.setText("STOPPED");
            badge.getElement().getThemeList().add("badge contrast");
        }
        return badge;
    }
    
    private Component createActionButtons(ServiceInfo service) {
        HorizontalLayout actions = new HorizontalLayout();
        
        Button detailsButton = new Button(VaadinIcon.INFO_CIRCLE.create());
        detailsButton.addClickListener(e -> showServiceDetails(service));
        
        if (service.isRunning()) {
            Button stopButton = new Button(VaadinIcon.STOP.create());
            stopButton.addClickListener(e -> stopService(service));
            actions.add(stopButton);
            
            Button restartButton = new Button(VaadinIcon.REFRESH.create());
            restartButton.addClickListener(e -> restartService(service));
            actions.add(restartButton);
        } else {
            Button startButton = new Button(VaadinIcon.PLAY.create());
            startButton.addClickListener(e -> startService(service));
            actions.add(startButton);
        }
        
        actions.add(detailsButton);
        return actions;
    }
    
    private void loadServices() {
        try {
            ServiceListRequest request = new ServiceListRequest()
                .activeOnly(activeOnlyCheckbox.getValue())
                .runningOnly(runningOnlyCheckbox.getValue())
                .filter(filterField.getValue());
            
            List<ServiceInfo> services = client.listServices(request);
            grid.setItems(services);
        } catch (Exception e) {
            Notification.show("Failed to load services: " + e.getMessage(), 
                3000, Notification.Position.BOTTOM_START);
        }
    }
    
    private void startService(ServiceInfo service) {
        try {
            ServiceActionResult result = client.startService(service.getName());
            if (result.isSuccess()) {
                Notification.show("Service started", 2000, Notification.Position.BOTTOM_START);
                loadServices();
            } else {
                Notification.show("Failed: " + result.getMessage(), 
                    3000, Notification.Position.BOTTOM_START);
            }
        } catch (Exception e) {
            Notification.show("Error: " + e.getMessage(), 
                3000, Notification.Position.BOTTOM_START);
        }
    }
    
    private void stopService(ServiceInfo service) {
        new ConfirmDialog("Stop Service",
            "Are you sure you want to stop " + service.getName() + "?",
            "Stop", event -> {
                try {
                    ServiceActionResult result = client.stopService(service.getName());
                    if (result.isSuccess()) {
                        Notification.show("Service stopped", 2000, Notification.Position.BOTTOM_START);
                        loadServices();
                    }
                } catch (Exception e) {
                    Notification.show("Error: " + e.getMessage(), 3000, Notification.Position.BOTTOM_START);
                }
            },
            "Cancel", event -> {}
        ).open();
    }
    
    private void restartService(ServiceInfo service) {
        try {
            ServiceActionResult result = client.restartService(service.getName());
            if (result.isSuccess()) {
                Notification.show("Service restarted", 2000, Notification.Position.BOTTOM_START);
                loadServices();
            }
        } catch (Exception e) {
            Notification.show("Error: " + e.getMessage(), 3000, Notification.Position.BOTTOM_START);
        }
    }
    
    private void showServiceDetails(ServiceInfo service) {
        ServiceDetailsDialog dialog = new ServiceDetailsDialog(client, service);
        dialog.open();
    }
    
    @Override
    protected void onDetach(DetachEvent detachEvent) {
        // Stop auto-refresh when view is closed
        UI.getCurrent().setPollInterval(-1);
        super.onDetach(detachEvent);
    }
}
```

### 2. Details Dialog (ServiceDetailsDialog.java)

```java
public class ServiceDetailsDialog extends Dialog {
    
    private final CalimeroServicesClient client;
    private final ServiceInfo serviceInfo;
    
    public ServiceDetailsDialog(CalimeroServicesClient client, ServiceInfo serviceInfo) {
        this.client = client;
        this.serviceInfo = serviceInfo;
        
        setHeaderTitle("Service Details: " + serviceInfo.getName());
        setWidth("800px");
        
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        
        try {
            ServiceStatus status = client.getServiceStatus(serviceInfo.getName());
            content.add(createDetailsSection(status));
            content.add(createLogsSection(status));
            content.add(createActionsSection(status));
        } catch (Exception e) {
            content.add(new Paragraph("Failed to load service details: " + e.getMessage()));
        }
        
        add(content);
        
        Button closeButton = new Button("Close", e -> close());
        getFooter().add(closeButton);
    }
    
    private Component createDetailsSection(ServiceStatus status) {
        FormLayout form = new FormLayout();
        
        form.add(new TextField("Name", status.getName(), ""));
        form.add(new TextField("Description", status.getDescription(), ""));
        form.add(new TextField("State", status.getActiveState() + " (" + status.getSubState() + ")", ""));
        form.add(new TextField("Load State", status.getLoadState(), ""));
        form.add(new TextField("Unit File State", status.getUnitFileState(), ""));
        form.add(new TextField("Main PID", String.valueOf(status.getMainPID()), ""));
        form.add(new TextField("Restart Count", String.valueOf(status.getRestartCount()), ""));
        
        form.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        
        // Make all fields read-only
        form.getChildren()
            .filter(TextField.class::isInstance)
            .map(TextField.class::cast)
            .forEach(field -> field.setReadOnly(true));
        
        return form;
    }
    
    private Component createLogsSection(ServiceStatus status) {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        
        H4 title = new H4("Recent Logs");
        section.add(title);
        
        TextArea logsArea = new TextArea();
        logsArea.setWidthFull();
        logsArea.setHeight("200px");
        logsArea.setReadOnly(true);
        
        String logs = String.join("\n", status.getRecentLogs());
        logsArea.setValue(logs);
        
        section.add(logsArea);
        return section;
    }
    
    private Component createActionsSection(ServiceStatus status) {
        HorizontalLayout actions = new HorizontalLayout();
        
        Checkbox enabledCheckbox = new Checkbox("Start at boot");
        enabledCheckbox.setValue(status.isEnabled());
        enabledCheckbox.addValueChangeListener(e -> {
            try {
                if (e.getValue()) {
                    client.enableService(status.getName());
                } else {
                    client.disableService(status.getName());
                }
                Notification.show("Service " + (e.getValue() ? "enabled" : "disabled"), 
                    2000, Notification.Position.BOTTOM_START);
            } catch (Exception ex) {
                Notification.show("Failed to change boot setting: " + ex.getMessage(), 
                    3000, Notification.Position.BOTTOM_START);
                enabledCheckbox.setValue(!e.getValue());
            }
        });
        
        Button reloadButton = new Button("Reload Configuration");
        reloadButton.addClickListener(e -> {
            try {
                client.reloadService(status.getName());
                Notification.show("Service configuration reloaded", 
                    2000, Notification.Position.BOTTOM_START);
            } catch (Exception ex) {
                Notification.show("Failed to reload: " + ex.getMessage(), 
                    3000, Notification.Position.BOTTOM_START);
            }
        });
        
        actions.add(enabledCheckbox, reloadButton);
        return actions;
    }
}
```

---

## Testing Checklist

- [ ] DTOs correctly deserialize from JSON
- [ ] Client sends correct message type ("systemservices")
- [ ] List operation returns services array
- [ ] Status operation returns full details
- [ ] Control operations (start/stop/restart) return success/failure
- [ ] Enable/disable operations work correctly
- [ ] Filter parameters work as expected
- [ ] Error handling displays user-friendly messages
- [ ] UI grid displays services correctly
- [ ] Status badges show correct colors
- [ ] Action buttons appear/disappear based on service state
- [ ] Details dialog loads and displays information
- [ ] Auto-refresh updates grid periodically
- [ ] Confirmation dialog appears for destructive actions

---

## Integration Steps

1. **Add to pom.xml** (if new dependencies needed):
   ```xml
   <!-- Already covered by existing Calimero client dependencies -->
   ```

2. **Register in Spring/CDI context**:
   ```java
   @Bean
   public CalimeroServicesClient servicesClient(CalimeroClientFactory factory) {
       return factory.createServicesClient();
   }
   ```

3. **Add menu item** in `MainLayout.java`:
   ```java
   Tab servicesTab = createTab(VaadinIcon.COG, "Services", ServicesView.class);
   ```

4. **Add security** (if needed):
   ```java
   @RolesAllowed({"ADMIN", "OPERATOR"})
   public class ServicesView extends VerticalLayout {
       // ...
   }
   ```

---

## Expected Timeline

- **Phase 1** (DTOs): 30 minutes
- **Phase 2** (Client): 45 minutes  
- **Phase 3** (Testing): 30 minutes
- **Phase 4** (UI): 2-3 hours

**Total**: ~4-5 hours for complete implementation

---

## Support

- Full API documentation: `CALIMERO_SERVICES_API.md`
- C++ implementation reference: `/home/yasin/git/calimero/src/http/webservice/processors/cservicesprocessor.cpp`
- Existing client patterns: Check `CalimeroSystemClient.java` for reference

---

**End of Quick Guide**
