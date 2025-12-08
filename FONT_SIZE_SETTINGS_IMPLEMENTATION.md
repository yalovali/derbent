# Font Size Settings Implementation Summary

## Overview
Implemented a font size configuration system that allows users to adjust the application's font size through system settings. The implementation uses CSS variables and dynamic injection to change font sizes across the entire application without requiring a page reload.

## Implementation Details

### 1. Database Schema Changes
Added `fontSizeScale` field to `CSystemSettings` entity:
- Column: `font_size_scale VARCHAR(255)` 
- Values: "small", "medium", "large"
- Default: "medium"
- Nullable: false
- Includes validation, getter, and setter methods

### 2. Font Size Service (`CFontSizeService`)
Created a new service to handle font size scaling dynamically:
- **Location**: `tech.derbent.api.ui.theme.CFontSizeService`
- **Methods**:
  - `applyFontSizeScale(String scale)`: Applies font size by injecting CSS variables
  - `storeFontSizeScale(String scale)`: Stores preference in user session
  - `getStoredFontSizeScale()`: Retrieves stored preference

### 3. Font Size Scales
Three predefined scales using Lumo CSS variables:

#### Small (~25% smaller than medium)
```css
--lumo-font-size-xxxl: 2.25rem
--lumo-font-size-xxl: 1.6875rem
--lumo-font-size-xl: 1rem
--lumo-font-size-l: 0.8125rem
--lumo-font-size-m: 0.75rem
--lumo-font-size-s: 0.6875rem
--lumo-font-size-xs: 0.625rem
--lumo-font-size-xxs: 0.5625rem
```

#### Medium (current default)
```css
--lumo-font-size-xxxl: 2.5rem
--lumo-font-size-xxl: 1.875rem
--lumo-font-size-xl: 1.125rem
--lumo-font-size-l: 0.9375rem
--lumo-font-size-m: 0.875rem
--lumo-font-size-s: 0.75rem
--lumo-font-size-xs: 0.6875rem
--lumo-font-size-xxs: 0.625rem
```

#### Large (~20% bigger than medium)
```css
--lumo-font-size-xxxl: 3rem
--lumo-font-size-xxl: 2.25rem
--lumo-font-size-xl: 1.375rem
--lumo-font-size-l: 1.125rem
--lumo-font-size-m: 1rem
--lumo-font-size-s: 0.875rem
--lumo-font-size-xs: 0.8125rem
--lumo-font-size-xxs: 0.75rem
```

### 4. Application Integration

#### MainLayout
- Added `applyFontSizeFromSettings()` method
- Called during initialization to load and apply saved font size
- Falls back to "medium" if error occurs

#### CSystemSettingsView
- Font size setting appears in the system settings form
- Changes are applied immediately when user saves settings
- No page reload required - uses JavaScript to inject CSS

#### CSystemSettingsService
- Added `getFontSizeScale()` method
- Returns validated font size scale with fallback to "medium"

## How It Works

### Initial Load
1. User logs in
2. MainLayout constructor calls `applyFontSizeFromSettings()`
3. Service retrieves font size from database
4. CFontSizeService injects CSS variables dynamically
5. Entire UI reflects the selected font size

### Changing Font Size
1. User navigates to System Settings
2. Changes "Font Size Scale" field (dropdown with small/medium/large)
3. Clicks "Save Settings"
4. CSystemSettingsView saves to database
5. CFontSizeService immediately applies new font size
6. UI updates without page reload

## Usage Instructions

1. **Navigate to Settings**
   - Go to "Setup" → "System Settings" in the main menu

2. **Locate Font Size Setting**
   - Find "Font Size Scale" field in the UI and Theming Settings section

3. **Select Desired Size**
   - Choose from: small, medium, or large

4. **Save Changes**
   - Click "Save Settings" button
   - Font size changes immediately across entire application

## Benefits

- **Accessibility**: Helps users with visual impairments
- **Customization**: Users can optimize for their screen size and viewing preferences
- **No Reload Required**: Changes apply instantly using JavaScript
- **System-Wide**: Single setting affects all pages consistently
- **Session Persistence**: Setting is stored in database and session

## Technical Approach

The implementation leverages Vaadin Flow's JavaScript execution capability to dynamically modify CSS custom properties (CSS variables) at runtime. This approach:

1. Avoids recompiling CSS or reloading pages
2. Provides instant visual feedback
3. Works consistently across all Vaadin components that use Lumo theme
4. Maintains proper separation of concerns (CSS in frontend, logic in backend)

## Future Enhancements

Possible improvements for future iterations:

1. **Per-User Settings**: Move font size to user preferences instead of system-wide
2. **More Granular Control**: Add fine-tuning options (e.g., small, medium-small, medium, medium-large, large)
3. **Preview Mode**: Allow users to preview changes before saving
4. **Accessibility Profile**: Combine with other accessibility settings (contrast, spacing)
5. **Custom Scales**: Allow administrators to define custom font size scales
