# Workflow States & Status Standards

## Overview
This document defines standards-compliant workflow states for Derbent entities based on industry best practices from ISO, PMI, Scrum.org, and Kanban Method.

## 1. Risk Management Workflow (ISO 31000:2018)

### Standard States
| State | Description | ISO 31000 Phase |
|-------|-------------|-----------------|
| **New** | Risk initially logged | Risk Identification |
| **Identified** | Risk described with context | Risk Identification |
| **Assessed** | Analysis completed, prioritized | Risk Assessment |
| **Treatment Planned** | Mitigation options selected | Risk Treatment |
| **Treatment In Progress** | Actions underway | Risk Treatment |
| **Treated** | Measures in place | Risk Treatment |
| **Under Review** | Monitoring effectiveness | Monitoring & Review |
| **Closed** | Risk resolved or accepted | Recording & Reporting |
| **Reopened** | New information emerged | - |

### Transitions
- New → Identified (risk analysis initiated)
- Identified → Assessed (analysis complete)
- Assessed → Treatment Planned (response strategy selected)
- Treatment Planned → Treatment In Progress (implementation started)
- Treatment In Progress → Treated (mitigation complete)
- Treated → Under Review (monitoring phase)
- Under Review → Closed (risk resolved)
- Any State → Reopened (if circumstances change)

### Key Principles
- Clear entry/exit criteria for each state
- Stakeholder involvement at transitions
- Feedback loops for continuous improvement
- Auditable record keeping

**Reference:** ISO 31000:2018 Risk Management - Guidelines

---

## 2. Sprint/Agile Workflow (Scrum Guide 2020)

### Standard States
| State | Description | Scrum Context |
|-------|-------------|---------------|
| **Backlog** | Not yet planned | Product Backlog |
| **Ready** | Refined and ready | Sprint Planning prep |
| **To Do** | In Sprint, not started | Sprint Backlog |
| **In Progress** | Actively worked on | Sprint execution |
| **Code Review** | Under peer review | Quality assurance |
| **Testing** | Being validated | Quality assurance |
| **Done** | Meets Definition of Done | Potentially shippable |
| **Blocked** | Impeded progress | - |

### Transitions
- Backlog → Ready (refinement complete)
- Ready → To Do (pulled into Sprint)
- To Do → In Progress (work started)
- In Progress → Code Review (development complete)
- Code Review → Testing (review passed)
- Testing → Done (all criteria met)
- Any State → Blocked (impediment identified)
- Blocked → Previous State (impediment resolved)

### Key Principles
- "Done" must meet Definition of Done
- Sprint Goal guides all work
- Daily Scrum reviews progress
- Sprint Review demonstrates Done items
- Sprint Retrospective drives improvement

**Reference:** Scrum Guide 2020 (Scrum.org)

---

## 3. Kanban Workflow (David J. Anderson)

### Standard Columns
| Column | Purpose | WIP Limit Example |
|--------|---------|-------------------|
| **Backlog** | Ready to start | No limit |
| **Selected** | Next up | 5 |
| **In Progress** | Active work | 3 |
| **Review/QA** | Validation | 2 |
| **Done** | Completed | No limit |

### Classes of Service (Priority Policies)
| Class | Color | Description | WIP Impact |
|-------|-------|-------------|------------|
| **Expedite** | Red | Critical/urgent | Can bypass WIP limits |
| **Fixed Date** | Orange | Hard deadline | High priority |
| **Standard** | Blue | Normal FIFO | Standard WIP |
| **Intangible** | Gray | Background work | Low priority |

### Key Principles
- Visualize workflow on board
- Limit WIP to prevent overload
- Manage flow for steady throughput
- Make policies explicit (WIP limits visible)
- Implement feedback loops (reviews)
- Improve collaboratively (experiments)

**Reference:** Kanban Method (David J. Anderson, 2010)

---

## 4. Budget/Financial Workflow (PMBOK)

### Standard States
| State | Description | PMBOK Process |
|-------|-------------|---------------|
| **Draft** | Being prepared | Cost Estimating |
| **Submitted** | Awaiting approval | Determine Budget |
| **Under Review** | Being evaluated | Determine Budget |
| **Rework** | Changes requested | Determine Budget |
| **Approved** | Baseline established | Determine Budget |
| **Active** | Tracking vs baseline | Control Costs |
| **Revision Requested** | Change request | Control Costs |
| **Closed** | Project complete | - |
| **Rejected** | Not approved | - |

### Transitions
- Draft → Submitted (ready for approval)
- Submitted → Under Review (review initiated)
- Under Review → Approved (accepted)
- Under Review → Rework (changes needed)
- Under Review → Rejected (declined)
- Rework → Submitted (resubmitted)
- Approved → Active (project starts)
- Active → Revision Requested (change needed)
- Revision Requested → Under Review (change review)
- Active → Closed (project ends)

### Key Principles
- Multi-level approval hierarchy
- Role-based access control
- Complete audit trail
- EVM tracking during Active state
- Variance analysis and forecasting

**Reference:** PMI PMBOK 7th Edition - Cost Management

---

## 5. Activity/Task Workflow (Common Project Management)

### Standard States
| State | Description |
|-------|-------------|
| **New** | Just created |
| **Open** | Ready to work |
| **In Progress** | Being worked on |
| **On Hold** | Temporarily paused |
| **Completed** | Work finished |
| **Closed** | Verified and archived |
| **Cancelled** | No longer needed |

---

## 6. Issue/Defect Workflow (IEEE 1044)

### Standard States
| State | Description | IEEE 1044 Phase |
|-------|-------------|-----------------|
| **New** | Reported | Detection |
| **Open** | Acknowledged | Investigation |
| **Assigned** | Owner assigned | Investigation |
| **In Progress** | Being fixed | Resolution |
| **Resolved** | Fix implemented | Resolution |
| **Verified** | Fix confirmed | Verification |
| **Closed** | Complete | Closure |
| **Reopened** | Issue recurred | - |
| **Deferred** | Postponed | - |

**Reference:** IEEE 1044-2009 Standard Classification for Software Anomalies

---

## Implementation Guidelines

### Database Initialization
When initializing the database, create CProjectItemStatus entries for each entity type with:
1. Standard-compliant status names
2. Proper workflow relationships (allowed transitions)
3. Initial/final status flags
4. Status ordering

### Entity Type Workflows
- **CRisk:** ISO 31000 workflow (9 states)
- **CSprint:** Scrum workflow (8 states)
- **CActivity/CMeeting:** Kanban workflow (5 columns)
- **CBudget:** PMBOK approval workflow (9 states)
- **CIssue:** IEEE 1044 workflow (9 states)
- **CValidationCase:** Testing workflow (Ready, In Progress, Passed, Failed, Blocked)

### Customization
Organizations can customize workflows while maintaining standards compliance by:
- Adding organization-specific substates
- Adjusting WIP limits for Kanban
- Defining custom approval hierarchies
- Setting state-specific permissions

---

## References

1. **ISO 31000:2018** - Risk Management Guidelines
   - https://www.iso.org/iso-31000-risk-management.html

2. **Scrum Guide 2020** - The Definitive Guide to Scrum
   - https://scrumguides.org/scrum-guide.html

3. **Kanban Method** - David J. Anderson
   - "Kanban: Successful Evolutionary Change for Your Technology Business"

4. **PMI PMBOK 7th Edition** - Project Management Body of Knowledge
   - https://www.pmi.org/pmbok-guide-standards

5. **IEEE 1044-2009** - Standard Classification for Software Anomalies
   - https://standards.ieee.org/standard/1044-2009.html

6. **ISTQB** - International Software Testing Qualifications Board
   - https://www.istqb.org/

---

*Document Version: 1.0*  
*Last Updated: 2026-01-17*  
*Maintained by: Derbent Development Team*
