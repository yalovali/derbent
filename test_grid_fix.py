#!/usr/bin/env python3
"""
Simple test script to verify the grid fix by taking a screenshot of the sample page
"""
import subprocess
import time
import os
from playwright.sync_api import sync_playwright

def test_grid_fix():
    # Start the application
    print("🚀 Starting the application...")
    app_process = subprocess.Popen([
        "mvn", "spring-boot:run", 
        "-Dspring-boot.run.main-class=tech.derbent.Application"
    ], 
    env={**os.environ, "SPRING_PROFILES_ACTIVE": "h2"},
    cwd="/home/runner/work/derbent/derbent",
    stdout=subprocess.PIPE, 
    stderr=subprocess.PIPE)
    
    # Wait for application to start
    print("⏳ Waiting for application to start...")
    time.sleep(20)
    
    try:
        with sync_playwright() as p:
            browser = p.chromium.launch(headless=True)
            page = browser.new_page()
            
            # Navigate to sample page
            print("🌐 Navigating to sample page...")
            page.goto("http://localhost:8080/cpagesample")
            
            # Wait for page to load
            page.wait_for_load_state('networkidle')
            time.sleep(3)
            
            # Take screenshot
            screenshot_path = "/home/runner/work/derbent/derbent/target/screenshots/grid_fix_test.png"
            os.makedirs("/home/runner/work/derbent/derbent/target/screenshots", exist_ok=True)
            page.screenshot(path=screenshot_path, full_page=True)
            print(f"📸 Screenshot saved: {screenshot_path}")
            
            # Check if grid has multiple rows
            grid_rows = page.locator("vaadin-grid-cell-content").count()
            print(f"📊 Found {grid_rows} grid cells in the page")
            
            browser.close()
            
    except Exception as e:
        print(f"❌ Error during test: {e}")
    finally:
        # Stop the application
        print("🛑 Stopping application...")
        app_process.terminate()
        app_process.wait()

if __name__ == "__main__":
    test_grid_fix()