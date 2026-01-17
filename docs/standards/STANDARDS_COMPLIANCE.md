# Standards Compliance Documentation

**Version:** 1.0  
**Last Updated:** 2026-01-17  
**Status:** Active

## Overview

This document maps Derbent features to industry-standard project management, software development, and quality management frameworks. All enhancements are **non-breaking** - they extend existing functionality without modifying current behavior.

For detailed implementation, see the full document at this location.

## Quick Reference

### Financial Management
- **Standards:** PMI PMBOK (Cost Management), EVM
- **Enhancements:** Earned Value fields (PV, EV, AC), CPI, SPI metrics
- **Status:** Planned

### Risk Management  
- **Standards:** ISO 31000, PMI PMBOK Risk
- **Enhancements:** Probability/Impact scores, Risk Response Strategy
- **Status:** In Progress

### Sprint/Agile
- **Standards:** Scrum Guide 2020, SAFe
- **Enhancements:** Sprint Goal, Definition of Done, Velocity
- **Status:** Planned

### Kanban
- **Standards:** Kanban Method (David J. Anderson)
- **Enhancements:** WIP Limits, Lead/Cycle Time, Classes of Service
- **Status:** Planned

### Test Management
- **Standards:** IEEE 829, ISO/IEC/IEEE 29119, ISTQB
- **Enhancements:** Test Steps, Expected/Actual Results, Priority
- **Status:** Planned

### Issue Tracking
- **Standards:** IEEE 1044, ITIL Incident Management
- **Enhancements:** Severity/Priority separation, Root Cause, Steps to Reproduce
- **Status:** Planned

## Implementation Approach

**All changes are NON-BREAKING:**
1. Add new fields as optional (nullable = true)
2. Keep existing field names unchanged
3. Update display names via @AMetaData only
4. Provide default values for backward compatibility
5. Document standard references in Javadoc

See full documentation for complete implementation details.
