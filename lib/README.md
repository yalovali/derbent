# StoredObject Libraries

This folder contains the StoredObject (SO) Vaadin add-on libraries that are required for building and running the Derbent application.

## Contents

- `so-components-14.0.7.jar` - SO Components library for Vaadin
- `so-charts-5.0.3.jar` - SO Charts library for visualization
- `so-helper-5.0.1.jar` - SO Helper utilities

## Installation

Before building the project, these libraries must be installed to your local Maven repository:

```bash
./install-so-libraries.sh
```

This script will install all three JAR files to `~/.m2/repository/org/vaadin/addons/so/`

## Why are these JARs in the repository?

The StoredObject libraries are not available in Maven Central or other public repositories. They are included in the repository to ensure:

1. **Consistent Builds**: Everyone can build the project without hunting for dependencies
2. **Sandbox Compatibility**: Works in restricted environments like GitHub Copilot workspace
3. **Version Control**: The exact versions used in development are tracked

## License

These libraries are third-party add-ons. Please refer to the SO library documentation for licensing information.
