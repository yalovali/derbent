@echo off
setlocal enabledelayedexpansion

REM Playwright UI Test Automation Runner for Derbent Application (Windows)
REM Comprehensive test suite with multiple test scenarios:
REM - menu: Fast menu navigation test
REM - comprehensive: Complete page testing with CRUD operations
REM - all-views: Navigate through all application views
REM - crud: Test CRUD operations on all pages with toolbars

echo.
echo 🚀 Derbent Playwright UI Test Suite (Windows)
echo ================================================

REM Setup Java 21 environment (equivalent to sourcing setup-java-env.sh)
if exist "setup-java-env.bat" (
    call setup-java-env.bat
) else (
    echo ⚠️  Warning: setup-java-env.bat not found, using system Java
)

REM Default settings - use environment variables if set, otherwise defaults
if not defined PLAYWRIGHT_HEADLESS set PLAYWRIGHT_HEADLESS=false
if not defined PLAYWRIGHT_SHOW_CONSOLE set PLAYWRIGHT_SHOW_CONSOLE=true
if not defined PLAYWRIGHT_SKIP_SCREENSHOTS set PLAYWRIGHT_SKIP_SCREENSHOTS=false
if not defined PLAYWRIGHT_SLOWMO set PLAYWRIGHT_SLOWMO=0
if not defined PLAYWRIGHT_VIEWPORT_WIDTH set PLAYWRIGHT_VIEWPORT_WIDTH=1920
if not defined PLAYWRIGHT_VIEWPORT_HEIGHT set PLAYWRIGHT_VIEWPORT_HEIGHT=1080
if not defined INTERACTIVE_MODE set INTERACTIVE_MODE=false

REM Parse command line arguments
set TEST_TYPE=menu
if "%1"=="" set TEST_TYPE=menu
if "%1"=="menu" set TEST_TYPE=menu
if "%1"=="comprehensive" set TEST_TYPE=comprehensive
if "%1"=="all-views" set TEST_TYPE=all-views
if "%1"=="crud" set TEST_TYPE=crud
if "%1"=="clean" goto :clean_artifacts
if "%1"=="install" goto :install_browsers
if "%1"=="help" goto :show_usage

REM Show interactive configuration menu if requested
if "%INTERACTIVE_MODE%"=="true" call :show_options_menu

REM Run the appropriate test
if "%TEST_TYPE%"=="menu" call :run_menu_test
if "%TEST_TYPE%"=="comprehensive" call :run_comprehensive_test
if "%TEST_TYPE%"=="all-views" call :run_all_views_test
if "%TEST_TYPE%"=="crud" call :run_crud_test

goto :end

:install_browsers
echo.
echo 🔄 Installing Playwright browsers...
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install" >nul 2>&1
echo ⚠️  Browser installation completed
goto :end

:run_menu_test
echo.
echo 🧪 Running Menu Navigation Test...
echo ==================================
echo This test will:
echo   1. Login to the application
echo   2. Browse all menu items
echo   3. Capture screenshots for each menu item
echo   4. Complete in under 1 minute
echo.

call :run_test "automated_tests.tech.derbent.ui.automation.CMenuNavigationTest"
goto :end

:run_comprehensive_test
echo.
echo 🧪 Running Comprehensive Page Test...
echo ====================================
echo This test will:
echo   1. Login to the application
echo   2. Navigate to all test auxiliary pages
echo   3. Test each page for grids and CRUD toolbars
echo   4. Run conditional tests based on page content
echo   5. Capture detailed screenshots
echo.

call :run_test "automated_tests.tech.derbent.ui.automation.CPageTestAuxillaryComprehensiveTest"
goto :end

:run_all_views_test
echo.
echo 🧪 Running All Views Navigation Test...
echo =======================================
echo This test will:
echo   1. Navigate through all application views
echo   2. Capture screenshots of each view
echo   3. Verify each page loads correctly
echo.

call :run_comprehensive_test
goto :end

:run_crud_test
echo.
echo 🧪 Running CRUD Operations Test...
echo ===================================
echo This test will:
echo   1. Navigate through all views with toolbars
echo   2. Test CRUD operations (Create, Read, Update, Delete)
echo   3. Test New, Edit, Delete, Save buttons
echo   4. Verify form dialogs open and close
echo   5. Capture screenshots at each step
echo.

call :run_comprehensive_test
goto :end

:show_options_menu
echo.
echo 🎛️  Test Configuration Options
echo ═══════════════════════════════════════════════════════════
echo.
echo 1. Browser Visibility
if "%PLAYWRIGHT_HEADLESS%"=="true" (
    echo    Current: HEADLESS (no browser window)
) else (
    echo    Current: VISIBLE (show browser window)
)
echo.
echo 2. Console Output
if "%PLAYWRIGHT_SHOW_CONSOLE%"=="true" (
    echo    Current: ENABLED (show test logs)
) else (
    echo    Current: SUPPRESSED (quiet mode)
)
echo.
echo 3. Screenshot Capture
if "%PLAYWRIGHT_SKIP_SCREENSHOTS%"=="true" (
    echo    Current: DISABLED (no screenshots)
) else (
    echo    Current: ENABLED (capture screenshots)
)
echo.
echo 4. Browser Slowdown (for debugging)
echo    Current: %PLAYWRIGHT_SLOWMO%ms delay per action
echo.
echo 5. Viewport Size
echo    Current: %PLAYWRIGHT_VIEWPORT_WIDTH%x%PLAYWRIGHT_VIEWPORT_HEIGHT%
echo.
echo ═══════════════════════════════════════════════════════════
echo.
echo Enter option number to toggle (1-5), or press Enter to start test:
set /p option=

if "%option%"=="1" (
    if "%PLAYWRIGHT_HEADLESS%"=="true" (
        set PLAYWRIGHT_HEADLESS=false
        echo ✓ Browser visibility: VISIBLE
    ) else (
        set PLAYWRIGHT_HEADLESS=true
        echo ✓ Browser visibility: HEADLESS
    )
    call :show_options_menu
)

if "%option%"=="2" (
    if "%PLAYWRIGHT_SHOW_CONSOLE%"=="true" (
        set PLAYWRIGHT_SHOW_CONSOLE=false
        echo ✓ Console output: SUPPRESSED
    ) else (
        set PLAYWRIGHT_SHOW_CONSOLE=true
        echo ✓ Console output: ENABLED
    )
    call :show_options_menu
)

if "%option%"=="3" (
    if "%PLAYWRIGHT_SKIP_SCREENSHOTS%"=="true" (
        set PLAYWRIGHT_SKIP_SCREENSHOTS=false
        echo ✓ Screenshot capture: ENABLED
    ) else (
        set PLAYWRIGHT_SKIP_SCREENSHOTS=true
        echo ✓ Screenshot capture: DISABLED
    )
    call :show_options_menu
)

if "%option%"=="4" (
    echo Enter slowdown delay in milliseconds (0-5000, default 0):
    set /p slowmo_input=
    REM Basic validation - check if it's a number
    echo %slowmo_input%| findstr /r "^[0-9][0-9]*$" >nul
    if !errorlevel! equ 0 (
        if %slowmo_input% geq 0 if %slowmo_input% leq 5000 (
            set PLAYWRIGHT_SLOWMO=%slowmo_input%
            echo ✓ Browser slowdown: !PLAYWRIGHT_SLOWMO!ms
        ) else (
            echo ⚠️ Invalid input, keeping current value: %PLAYWRIGHT_SLOWMO%ms
        )
    ) else (
        echo ⚠️ Invalid input, keeping current value: %PLAYWRIGHT_SLOWMO%ms
    )
    call :show_options_menu
)

if "%option%"=="5" (
    echo Enter viewport width (800-3840, default 1920):
    set /p width_input=
    echo Enter viewport height (600-2160, default 1080):
    set /p height_input=
    
    REM Basic validation for width
    echo %width_input%| findstr /r "^[0-9][0-9]*$" >nul
    if !errorlevel! equ 0 (
        if %width_input% geq 800 if %width_input% leq 3840 (
            set PLAYWRIGHT_VIEWPORT_WIDTH=%width_input%
        )
    )
    
    REM Basic validation for height
    echo %height_input%| findstr /r "^[0-9][0-9]*$" >nul
    if !errorlevel! equ 0 (
        if %height_input% geq 600 if %height_input% leq 2160 (
            set PLAYWRIGHT_VIEWPORT_HEIGHT=%height_input%
        )
    )
    
    echo ✓ Viewport size: !PLAYWRIGHT_VIEWPORT_WIDTH!x!PLAYWRIGHT_VIEWPORT_HEIGHT!
    call :show_options_menu
)

if "%option%"=="" (
    echo ▶️ Starting test with current configuration...
    goto :eof
)

if not "%option%"=="1" if not "%option%"=="2" if not "%option%"=="3" if not "%option%"=="4" if not "%option%"=="5" if not "%option%"=="" (
    echo ⚠️ Invalid option
    call :show_options_menu
)
goto :eof

:run_test
set test_class=%~1

REM Create screenshots directory
if not exist "target\screenshots" mkdir "target\screenshots"

REM Install Playwright browsers if needed
call :install_browsers_silent

REM Set Playwright environment variables
set PLAYWRIGHT_BROWSERS_PATH=%USERPROFILE%\.cache\ms-playwright
set PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=true

echo.
echo 🎯 Test Configuration:
if "%PLAYWRIGHT_HEADLESS%"=="true" (
    echo    Browser mode: HEADLESS
) else (
    echo    Browser mode: VISIBLE
)
if "%PLAYWRIGHT_SHOW_CONSOLE%"=="true" (
    echo    Console output: ENABLED
) else (
    echo    Console output: SUPPRESSED
)
if "%PLAYWRIGHT_SKIP_SCREENSHOTS%"=="true" (
    echo    Screenshots: DISABLED
) else (
    echo    Screenshots: ENABLED
)
echo    Slowdown: %PLAYWRIGHT_SLOWMO%ms
echo    Viewport: %PLAYWRIGHT_VIEWPORT_WIDTH%x%PLAYWRIGHT_VIEWPORT_HEIGHT%
echo.

REM Run the test with Playwright-specific profile
set TEST_RESULT=0
if "%PLAYWRIGHT_SHOW_CONSOLE%"=="true" (
    mvn test -Dtest="%test_class%" ^
        -Dspring.profiles.active=test ^
        -Dplaywright.headless=%PLAYWRIGHT_HEADLESS% ^
        -Dplaywright.slowmo=%PLAYWRIGHT_SLOWMO% ^
        -Dplaywright.viewport.width=%PLAYWRIGHT_VIEWPORT_WIDTH% ^
        -Dplaywright.viewport.height=%PLAYWRIGHT_VIEWPORT_HEIGHT%
    set TEST_RESULT=!errorlevel!
) else (
    mvn test -Dtest="%test_class%" ^
        -Dspring.profiles.active=test ^
        -Dplaywright.headless=%PLAYWRIGHT_HEADLESS% ^
        -Dplaywright.slowmo=%PLAYWRIGHT_SLOWMO% ^
        -Dplaywright.viewport.width=%PLAYWRIGHT_VIEWPORT_WIDTH% ^
        -Dplaywright.viewport.height=%PLAYWRIGHT_VIEWPORT_HEIGHT% >nul 2>&1
    set TEST_RESULT=!errorlevel!
)

if !TEST_RESULT! equ 0 (
    echo ✅ Test completed successfully!
    
    REM Count screenshots
    set screenshot_count=0
    if exist "target\screenshots\*.png" (
        for %%f in (target\screenshots\*.png) do set /a screenshot_count+=1
        echo 📸 Generated !screenshot_count! screenshots in target\screenshots\
        echo.
        echo Screenshots:
        for %%f in (target\screenshots\*.png) do echo   - %%~nxf
    )
) else (
    echo ❌ Test failed!
    
    REM Show any screenshots that were taken
    set screenshot_count=0
    if exist "target\screenshots\*.png" (
        for %%f in (target\screenshots\*.png) do set /a screenshot_count+=1
        echo 📸 Debug screenshots available in target\screenshots\ (!screenshot_count! files)
    )
)
goto :eof

:install_browsers_silent
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install" >nul 2>&1
goto :eof

:clean_artifacts
echo.
echo 🧹 Cleaning test artifacts...
if exist "target\screenshots" (
    del /q "target\screenshots\*.*" 2>nul
    rmdir "target\screenshots" 2>nul
    echo ✓ Screenshots directory cleaned
)
if exist "target\surefire-reports" (
    del /q "target\surefire-reports\*.*" 2>nul
    rmdir "target\surefire-reports" 2>nul
    echo ✓ Test reports cleaned
)
if exist "test-results" (
    del /q /s "test-results\*.*" 2>nul
    rmdir /s /q "test-results" 2>nul
    echo ✓ Playwright test results cleaned
)
echo ✅ Cleanup completed
goto :end

:show_usage
echo.
echo Usage: run-playwright-tests.bat [OPTION]
echo.
echo Run Playwright UI automation tests for the Derbent application.
echo.
echo OPTIONS:
echo     (no args)       Run the menu navigation test (default)
echo     menu            Run the menu navigation test
echo     comprehensive   Run comprehensive page tests (all views + CRUD operations)
echo     all-views       Navigate through all application views and capture screenshots
echo     crud            Test CRUD operations on all pages with toolbars
echo     clean           Clean test artifacts (screenshots, reports)
echo     install         Install Playwright browsers
echo     help            Show this help message
echo.
echo ENVIRONMENT VARIABLES:
echo     PLAYWRIGHT_HEADLESS          Set to 'true' for headless mode, 'false' for visible browser (default: false)
echo     PLAYWRIGHT_SHOW_CONSOLE      Set to 'true' to show console output, 'false' to suppress (default: true)
echo     PLAYWRIGHT_SKIP_SCREENSHOTS  Set to 'true' to disable screenshot capture (default: false)
echo     PLAYWRIGHT_SLOWMO            Delay in milliseconds between actions for debugging (default: 0)
echo     PLAYWRIGHT_VIEWPORT_WIDTH    Browser viewport width in pixels (default: 1920)
echo     PLAYWRIGHT_VIEWPORT_HEIGHT   Browser viewport height in pixels (default: 1080)
echo     INTERACTIVE_MODE             Set to 'true' to show configuration menu before test (default: false)
echo.
echo EXAMPLES:
echo     REM Run with interactive configuration menu
echo     set INTERACTIVE_MODE=true ^&^& run-playwright-tests.bat menu
echo.
echo     REM Run quick menu navigation test (default, ~37 seconds)
echo     run-playwright-tests.bat
echo.
echo     REM Run comprehensive test covering all views and CRUD operations (~2-5 minutes)
echo     run-playwright-tests.bat comprehensive
echo.
echo     REM Run in headless mode without screenshots (fast)
echo     set PLAYWRIGHT_HEADLESS=true ^&^& set PLAYWRIGHT_SKIP_SCREENSHOTS=true ^&^& run-playwright-tests.bat menu
echo.
echo     REM Run with visible browser and slow motion for debugging
echo     set PLAYWRIGHT_SLOWMO=500 ^&^& run-playwright-tests.bat menu
echo.
echo     REM Run mobile viewport testing (iPhone 12)
echo     set PLAYWRIGHT_VIEWPORT_WIDTH=390 ^&^& set PLAYWRIGHT_VIEWPORT_HEIGHT=844 ^&^& run-playwright-tests.bat menu
echo.
echo     REM Fast CI/CD mode (headless, no screenshots, no console)
echo     set PLAYWRIGHT_HEADLESS=true ^&^& set PLAYWRIGHT_SKIP_SCREENSHOTS=true ^&^& set PLAYWRIGHT_SHOW_CONSOLE=false ^&^& run-playwright-tests.bat comprehensive
echo.
goto :end

:end
echo.
echo 🏁 Playwright test runner completed
endlocal