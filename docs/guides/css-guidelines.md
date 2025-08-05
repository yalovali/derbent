# CSS Guidelines for Derbent Project

## Table of Contents
1. [CSS Architecture Overview](#css-architecture-overview)
2. [Vaadin Theming System](#vaadin-theming-system)
3. [Lumo Design System](#lumo-design-system)
4. [CSS Organization](#css-organization)
5. [Component Styling Guidelines](#component-styling-guidelines)
6. [Login Component Customization](#login-component-customization)
7. [Best Practices](#best-practices)
8. [Troubleshooting Common Issues](#troubleshooting-common-issues)
9. [Resources](#resources)

## CSS Architecture Overview

### Project Structure
The Derbent project uses Vaadin 24.8.3 with the Lumo theme. Our CSS is organized as follows:

```
src/main/frontend/themes/default/
├── styles.css          # Main theme styles and customizations
├── dev-login.css      # Development login view specific styles
└── theme.json         # Theme configuration
```

### Technology Stack
- **Framework**: Vaadin 24.8.3 (Java-based web framework)
- **Base Theme**: Lumo (Vaadin's default design system)
- **CSS Methodology**: Component-based styling with CSS custom properties
- **Browser Support**: Modern browsers with CSS custom properties support

## Vaadin Theming System

### How Vaadin Themes Work
Vaadin uses a component-based architecture where each UI component is a Web Component with its own Shadow DOM. This means:

1. **Encapsulation**: Component styles are isolated within their Shadow DOM
2. **Theme Integration**: Vaadin themes use CSS custom properties to allow customization
3. **Global vs Component Styles**: Global styles in `styles.css` affect the application shell, while component-specific styles require different approaches

### Theme Loading Order
1. **Lumo Base Theme**: Provides default styling and CSS custom properties
2. **Application Theme** (`styles.css`): Our customizations and overrides
3. **Component Themes**: Component-specific styling

## Lumo Design System

### Core Concepts
Lumo is Vaadin's design system that provides:
- **Design Tokens**: CSS custom properties for consistent styling
- **Component Variants**: Pre-built component variations
- **Responsive Design**: Built-in responsive behavior

### Key CSS Custom Properties
```css
/* Colors */
--lumo-primary-color: #005fcc;
--lumo-base-color: #fff;
--lumo-contrast-color: #333;
--lumo-error-color: #ff3d71;
--lumo-success-color: #28a745;

/* Spacing */
--lumo-space-xs: 0.25rem;
--lumo-space-s: 0.5rem;
--lumo-space-m: 1rem;
--lumo-space-l: 1.5rem;
--lumo-space-xl: 2.5rem;

/* Typography */
--lumo-font-size-xs: 0.75rem;
--lumo-font-size-s: 0.875rem;
--lumo-font-size-m: 1rem;
--lumo-font-size-l: 1.125rem;
--lumo-font-size-xl: 1.25rem;

/* Borders */
--lumo-border-radius-s: 0.25rem;
--lumo-border-radius-m: 0.375rem;
--lumo-border-radius-l: 0.5rem;

/* Shadows */
--lumo-box-shadow-xs: 0 1px 4px -1px rgba(0, 0, 0, 0.2);
--lumo-box-shadow-s: 0 2px 4px -1px rgba(0, 0, 0, 0.2);
--lumo-box-shadow-m: 0 2px 6px -1px rgba(0, 0, 0, 0.2);
```

## CSS Organization

### File Structure Guidelines
1. **styles.css**: Main application styles and global customizations
2. **component-specific.css**: Individual component customizations (when needed)
3. **dev-*.css**: Development environment specific styles

### Import Strategy
```css
/* Always import component-specific styles first */
@import url("dev-login.css");

/* Then add application-wide customizations */
/* Component customizations */
/* Utility classes */
```

## Component Styling Guidelines

### Targeting Vaadin Components
Vaadin components use custom elements and CSS custom properties:

```css
/* ✅ Correct: Use component selector with CSS custom properties */
vaadin-button {
    --lumo-primary-color: #ff6b35;
}

/* ✅ Correct: Use !important for direct style overrides when needed */
vaadin-login-form {
    width: 450px !important;
}

/* ❌ Incorrect: Cannot style Shadow DOM content directly */
vaadin-button .button-content {
    color: red; /* This won't work */
}
```

### CSS Custom Properties vs Direct Styles

**Use CSS Custom Properties for:**
- Colors, spacing, typography
- Component-supported customizations
- Maintaining theme consistency

**Use Direct Styles with !important for:**
- Layout properties (width, height, positioning)
- Properties not exposed as custom properties
- Overriding component defaults when necessary

### Example Component Customizations

```css
/* Button Customization */
vaadin-button {
    --lumo-primary-color: #007bff;
    --lumo-font-size-m: 1rem;
    margin: var(--lumo-space-s);
}

/* Text Field Customization */
vaadin-text-field {
    --lumo-contrast-10pct: rgba(0, 123, 255, 0.1);
    width: 100% !important;
}

/* Grid Customization */
vaadin-grid {
    --lumo-font-size-s: 0.875rem;
    height: 400px !important;
}
```

## Login Component Customization

### Current Implementation
Our login screen uses `vaadin-login-overlay` with the following customizations:

```css
/* Make the login screen wider */
vaadin-login-overlay {
    --lumo-size-form-login-width: 450px;
}

vaadin-login-form {
    --lumo-size-form-login-width: 450px;
    width: 450px !important;
    min-width: 450px !important;
    max-width: 450px !important;
}

vaadin-login-form-wrapper {
    width: 450px !important;
    min-width: 450px !important;
    max-width: 450px !important;
}
```

### Why Multiple Selectors Are Needed
1. **vaadin-login-overlay**: Sets the CSS custom property for form width
2. **vaadin-login-form**: Enforces the width with direct styles
3. **vaadin-login-form-wrapper**: Ensures the wrapper container also respects the width

### Login Form Structure
```
vaadin-login-overlay (position: fixed, full screen)
└── vaadin-login-form-wrapper (container)
    └── vaadin-login-form (actual form)
        ├── Header section (title, description)
        ├── Form fields (username, password)
        ├── Actions (login button)
        └── Footer (additional content)
```

## Best Practices

### 1. Use Design Tokens
```css
/* ✅ Good: Use Lumo design tokens */
.custom-card {
    padding: var(--lumo-space-m);
    border-radius: var(--lumo-border-radius-m);
    box-shadow: var(--lumo-box-shadow-s);
}

/* ❌ Bad: Hard-coded values */
.custom-card {
    padding: 16px;
    border-radius: 6px;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
```

### 2. Maintain Consistency
- Use consistent spacing (multiples of `--lumo-space-s`)
- Stick to the Lumo color palette
- Follow naming conventions for custom classes

### 3. Responsive Design
```css
/* Use Lumo's responsive utilities */
.responsive-container {
    padding: var(--lumo-space-s);
}

@media (min-width: 768px) {
    .responsive-container {
        padding: var(--lumo-space-l);
    }
}
```

### 4. Component Hierarchy
```css
/* Target specific components within containers */
vaadin-vertical-layout vaadin-button {
    margin-bottom: var(--lumo-space-s);
}

/* Use CSS specificity appropriately */
.login-view vaadin-login-form {
    --lumo-primary-color: #28a745;
}
```

### 5. Testing Across Browsers
- Test in Chrome, Firefox, Safari, and Edge
- Verify Shadow DOM styles render correctly
- Check responsive behavior on different screen sizes

## Troubleshooting Common Issues

### Issue: Styles Not Applying to Vaadin Components

**Symptoms**: CSS rules seem to be ignored by Vaadin components

**Causes & Solutions**:
1. **Shadow DOM Isolation**: Component content is in Shadow DOM
   - **Solution**: Use CSS custom properties instead of direct styling
   
2. **CSS Specificity**: Vaadin component styles have higher specificity
   - **Solution**: Use `!important` or increase specificity
   
3. **Incorrect Selector**: Using wrong component selector
   - **Solution**: Inspect element to find correct custom element name

### Issue: Layout Problems with Width/Height

**Symptoms**: Components don't size correctly, content overflows

**Causes & Solutions**:
1. **Auto Sizing**: Component uses auto width/height
   - **Solution**: Set explicit dimensions with `!important`
   
2. **Container Constraints**: Parent container limits sizing
   - **Solution**: Check parent containers and their CSS
   
3. **CSS Custom Property Conflicts**: Multiple properties conflict
   - **Solution**: Use both custom properties and direct styles

### Issue: Theme Changes Not Visible

**Symptoms**: CSS changes don't appear in browser

**Causes & Solutions**:
1. **Browser Cache**: Old styles cached
   - **Solution**: Hard refresh (Ctrl+Shift+R) or disable cache in DevTools
   
2. **Build Process**: Vaadin needs to rebuild frontend
   - **Solution**: Restart the development server
   
3. **CSS Syntax Error**: Invalid CSS prevents loading
   - **Solution**: Check browser console for CSS errors

## Resources

### Official Documentation
- [Vaadin Themes Documentation](https://vaadin.com/docs/latest/styling)
- [Lumo Theme Documentation](https://vaadin.com/docs/latest/styling/lumo)
- [CSS Custom Properties in Vaadin](https://vaadin.com/docs/latest/styling/custom-theme)

### Tools
- **Browser DevTools**: Inspect Shadow DOM and CSS custom properties
- **Vaadin TestBench**: Automated testing for Vaadin applications
- **Chrome Vaadin Extension**: Debug Vaadin applications

### Color Tools
- [Lumo Color Generator](https://demo.vaadin.com/lumo-editor/)
- [CSS Color Contrast Checker](https://webaim.org/resources/contrastchecker/)

### Best Practice Examples
```css
/* Complete component customization example */
.application-view {
    /* Use semantic class names */
    --primary-brand-color: #007bff;
}

.application-view vaadin-app-layout {
    /* Customize layout using CSS custom properties */
    --vaadin-app-layout-navbar-background: var(--primary-brand-color);
    --vaadin-app-layout-navbar-font-color: white;
}

.application-view vaadin-button[theme="primary"] {
    /* Theme-specific button customization */
    --lumo-primary-color: var(--primary-brand-color);
    --lumo-primary-contrast-color: white;
}

/* Responsive utilities */
@media (max-width: 768px) {
    .application-view vaadin-app-layout {
        --vaadin-app-layout-navbar-font-size: var(--lumo-font-size-s);
    }
}
```

---

**Last Updated**: July 2025  
**Vaadin Version**: 24.8.3  
**Author**: Derbent Development Team

For questions or contributions to these guidelines, please create an issue in the project repository.