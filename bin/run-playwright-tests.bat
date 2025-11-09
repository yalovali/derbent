@echo off
REM Derbent Playwright Test Runner for Windows
REM Usage: run-playwright-tests.bat [mock|comprehensive|status-types|main-views|admin-views|kanban-views|clean]

setlocal

REM Default test category
set CATEGORY=mock
IF NOT "%1"=="" (
    set CATEGORY=%1
)

REM Map categories to test classes
set TEST_CLASS=
if /I "%CATEGORY%"=="mock" set TEST_CLASS=automated_tests.tech.derbent.ui.automation.CSampleDataMenuNavigationTest
if /I "%CATEGORY%"=="comprehensive" set TEST_CLASS=automated_tests.tech.derbent.ui.automation.CComprehensiveDynamicViewsTest
if /I "%CATEGORY%"=="status-types" set TEST_CLASS=automated_tests.tech.derbent.ui.automation.CStatusTypesTest
if /I "%CATEGORY%"=="main-views" set TEST_CLASS=automated_tests.tech.derbent.ui.automation.CMainViewsTest
if /I "%CATEGORY%"=="admin-views" set TEST_CLASS=automated_tests.tech.derbent.ui.automation.CAdminViewsTest
if /I "%CATEGORY%"=="kanban-views" set TEST_CLASS=automated_tests.tech.derbent.ui.automation.CKanbanViewsTest

REM Ensure Maven is available
where mvn >nul 2>nul
IF ERRORLEVEL 1 (
    echo Maven not found in PATH. Please install Maven 3.9+ and ensure it is in your PATH.
    exit /b 1
)

REM Clean screenshots if requested
IF /I "%CATEGORY%"=="clean" (
    echo Cleaning Playwright test artifacts...
    if exist target\screenshots (
        rmdir /s /q target\screenshots
    )
    echo Clean complete.
    exit /b 0
)

REM If no test class is mapped, show error and exit
if "%TEST_CLASS%"=="" (
    echo Unknown test category: %CATEGORY%
    exit /b 1
)

REM Run Playwright test with the specified class and visible browser
echo Running Playwright test: %TEST_CLASS% (category: %CATEGORY%)
mvn test -Dtest=%TEST_CLASS% -Dspring.profiles.active=test -Dplaywright.headless=false

REM Check for screenshots
IF EXIST target\screenshots (
    echo Screenshots generated in target\screenshots\
) ELSE (
    echo No screenshots found. Check test output for errors.
)

endlocal