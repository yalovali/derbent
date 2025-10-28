# StoredObject Charts Not Rendering - Troubleshooting Guide

## Current Status

Your application is now starting successfully without the "Parameter 'destFile' is not a file" error. The fix for the Windows path issue is working correctly. However, the SO-Charts are not rendering because the StoredObject library JAR files are not available.

## Problem

The StoredObject Maven repository (https://storedobject.com/maven) is not accessible, which means Maven cannot download the required JAR files:
- `so-components:14.0.7`
- `so-charts:5.0.3`
- `so-helper:5.0.1`

Without these JAR files, the chart components cannot render even though the application compiles and runs.

## Solution Options

### Option 1: Verify StoredObject Repository Access (Recommended)

1. **Check repository accessibility:**
   ```bash
   curl -I https://storedobject.com/maven/
   ```

2. **If accessible, force Maven to re-download:**
   ```bash
   mvn clean
   mvn dependency:purge-local-repository -DmanualInclude=org.vaadin.addons.so:so-components,org.vaadin.addons.so:so-charts,org.vaadin.addons.so:so-helper
   mvn clean install
   ```

3. **Start the application:**
   ```bash
   mvn spring-boot:run -Dspring.profiles.active=h2
   ```

### Option 2: Use Alternative Maven Repository

If the main repository is not accessible, try adding a mirror or alternative repository in your Maven settings (`~/.m2/settings.xml`):

```xml
<settings>
  <mirrors>
    <mirror>
      <id>so-maven-mirror</id>
      <url>https://storedobject.com/maven</url>
      <mirrorOf>so-maven</mirrorOf>
    </mirror>
  </mirrors>
</settings>
```

### Option 3: Manual JAR Installation

If you have the JAR files from another source or backup:

1. Install JARs manually to local Maven repository:
   ```bash
   mvn install:install-file -Dfile=/path/to/so-components-14.0.7.jar \
     -DgroupId=org.vaadin.addons.so -DartifactId=so-components -Dversion=14.0.7 -Dpackaging=jar
   
   mvn install:install-file -Dfile=/path/to/so-charts-5.0.3.jar \
     -DgroupId=org.vaadin.addons.so -DartifactId=so-charts -Dversion=5.0.3 -Dpackaging=jar
   
   mvn install:install-file -Dfile=/path/to/so-helper-5.0.1.jar \
     -DgroupId=org.vaadin.addons.so -DartifactId=so-helper -Dversion=5.0.1 -Dpackaging=jar
   ```

2. Rebuild and run:
   ```bash
   mvn clean install
   mvn spring-boot:run -Dspring.profiles.active=h2
   ```

### Option 4: Use Alternative Chart Library

If StoredObject charts are not critical, consider using alternative Vaadin-compatible chart libraries:

1. **Vaadin Charts** (Commercial, part of Vaadin Pro):
   ```xml
   <dependency>
       <groupId>com.vaadin</groupId>
       <artifactId>vaadin-charts-flow</artifactId>
   </dependency>
   ```

2. **ApexCharts for Vaadin** (Open source):
   ```xml
   <dependency>
       <groupId>com.github.appreciated</groupId>
       <artifactId>apexcharts</artifactId>
       <version>23.3.0</version>
   </dependency>
   ```

## Verification Steps

Once the JAR files are available:

1. **Check Maven repository:**
   ```bash
   ls -la ~/.m2/repository/org/vaadin/addons/so/
   ```
   
   You should see directories for:
   - `so-components/14.0.7/`
   - `so-charts/5.0.3/`
   - `so-helper/5.0.1/`

2. **Check application logs for chart initialization:**
   Look for any errors related to SOChart or StoredObject components when the application starts.

3. **Check browser console:**
   Open the browser developer tools (F12) and check the Console tab for JavaScript errors when loading the chart page.

4. **Verify frontend resources:**
   Check that the `src/main/frontend/generated/jar-resources/` directory contains resources extracted from the SO-Charts JARs.

## Expected Result

When everything is working correctly:
- The application starts without errors
- The chart page loads with all four chart types visible:
  1. Simple Pie Chart (showing fruit sales distribution)
  2. Simple Bar Chart (showing weekly sales)
  3. Simple Line Chart (showing monthly growth)
  4. Simple Gantt Chart (showing project timeline)

## Common Issues and Solutions

### Issue: "Cannot find module '@vaadin/charts'"
**Solution:** Run `npm install` in the project root and restart the application.

### Issue: Charts show as blank/white boxes
**Solution:** This usually indicates the JavaScript resources are not loaded. Check:
1. Browser console for 404 errors
2. Network tab in developer tools for failed resource requests
3. Vaadin dev mode vs production mode settings

### Issue: "ChartException" in logs
**Solution:** Check that:
1. Data arrays match in size (labels and values)
2. Chart configuration is valid
3. All required imports are present

## Contact Support

If none of these solutions work, you may need to:
1. Contact StoredObject support for repository access issues
2. Check if there are license requirements for the SO-Charts library
3. Verify network/firewall settings that might block repository access

## Related Documentation

- StoredObject Charts: https://storedobject.com/
- Vaadin Charts documentation: https://vaadin.com/docs/latest/components/charts
- Maven dependency resolution: https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html
