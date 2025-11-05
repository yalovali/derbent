# StoredObject Libraries Restoration Guide

## Quick Restoration Steps

If you need to restore SO (StoredObject) libraries to the project:

### 1. Restore JAR Files
```bash
cp -r archived/so-libraries-backup/lib/* lib/
```

### 2. Install to Maven Local Repository
```bash
./archived/so-libraries-backup/install-so-libraries.sh
```

### 3. Restore pom.xml Dependencies

Add these dependencies to pom.xml after line 50 (in the `<dependencies>` section):

```xml
<!-- StoredObject Addons - Installed in local Maven repository from lib/ folder -->
<dependency>
    <groupId>org.vaadin.addons.so</groupId>
    <artifactId>so-components</artifactId>
    <version>14.0.7</version>
    <exclusions>
        <exclusion>
            <groupId>commons-logging</groupId>
            <artifactId>commons-logging</artifactId>
        </exclusion>
    </exclusions>
</dependency>

<dependency>
    <groupId>org.vaadin.addons.so</groupId>
    <artifactId>so-charts</artifactId>
    <version>5.0.3</version>
</dependency>

<dependency>
    <groupId>org.vaadin.addons.so</groupId>
    <artifactId>so-helper</artifactId>
    <version>5.0.1</version>
</dependency>
```

### 4. Restore Java Files

```bash
# Restore Gantt chart implementation
cp archived/so-libraries-backup/java-files/CSOGanntChart.java src/main/java/tech/derbent/app/gannt/view/

# Restore chart test view
cp archived/so-libraries-backup/java-files/CChartTestView.java src/main/java/tech/derbent/base/login/view/
```

### 5. Compile and Test

```bash
mvn clean compile
```

## What Was Removed

- **3 Maven dependencies**: so-components, so-charts, so-helper
- **2 Java classes**: CSOGanntChart, CChartTestView
- **1 Installation script**: install-so-libraries.sh
- **3 JAR files**: Located in lib/ directory

## Why Was It Removed?

SO libraries were causing:
- Dependency resolution issues during builds
- Maven repository connectivity problems
- Build timeout issues

The libraries are archived for potential future use when these issues are resolved.
