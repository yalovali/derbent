# StoredObject (SO) Libraries Archive

This directory contains all SO (StoredObject) library-related code and dependencies that were removed from the main project.

## Archived Date
2025-11-05

## Contents

### Dependencies (from pom.xml)
- so-components:14.0.7
- so-charts:5.0.3  
- so-helper:5.0.1

### Java Files
- CSOGanntChart.java - Gantt chart implementation using SOChart
- CChartTestView.java - Chart test examples view

### Scripts
- install-so-libraries.sh - Installation script for SO JARs

### JAR Files
- lib/so-components-14.0.7.jar
- lib/so-charts-5.0.3.jar
- lib/so-helper-5.0.1.jar

## Reason for Removal
SO libraries were causing dependency resolution and build issues. They have been archived for potential future use.

## How to Re-enable

1. Copy JAR files back to lib/ directory
2. Run ./archived/so-libraries-backup/install-so-libraries.sh
3. Restore pom.xml dependencies
4. Restore Java files to their original locations
