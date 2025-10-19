-- =====================================================================
-- MINIMAL SAMPLE DATA FOR H2 DATABASE (TEST ENVIRONMENT)
-- =====================================================================
-- This script provides minimal sample data compatible with H2 database
-- used in tests, containing exactly 2 examples per entity type while 
-- enriching all items with relations, types, status, colors, and comments.
-- =====================================================================

-- =====================================================================
-- TABLE CLEANUP - DELETE ALL EXISTING DATA
-- =====================================================================

-- Delete data from junction tables first
DELETE FROM cmeeting_participants;
DELETE FROM cdecision_team_members;
DELETE FROM cmeeting_attendees;

-- Delete data from dependent tables (in reverse dependency order)
DELETE FROM ccomment;
DELETE FROM corderapproval;
DELETE FROM corder;
DELETE FROM cactivity;
DELETE FROM cmeeting;
DELETE FROM crisk;
DELETE FROM cdecisionapproval;
DELETE FROM cdecision;

-- Delete data from main entity tables
DELETE FROM cproject;
DELETE FROM cuser;
DELETE FROM ccompany;

-- Delete data from lookup tables
DELETE FROM cactivitytype;
DELETE FROM cactivitystatus;
DELETE FROM cactivitypriority;
DELETE FROM ccommentpriority;
DELETE FROM cmeetingtype;
DELETE FROM cmeetingstatus;
DELETE FROM cusertype;
DELETE FROM cdecisiontype;
DELETE FROM cdecisionstatus;
DELETE FROM cordertype;
DELETE FROM corderstatus;
DELETE FROM ccurrency;
DELETE FROM capprovalstatus;

-- Reset sequences for H2
ALTER SEQUENCE cuser_user_id_seq RESTART WITH 1;
ALTER SEQUENCE cproject_project_id_seq RESTART WITH 1;

-- =====================================================================
-- ESSENTIAL LOOKUP TABLES (2 examples each for types, statuses, etc.)
-- =====================================================================


-- Activity types (2 essential types)
INSERT INTO cactivitytype (name, description) VALUES 
('User Story', 'Feature from end-user perspective with acceptance criteria'),
('Task', 'General development or operational task');

-- Activity statuses with colors (2 essential statuses)
INSERT INTO cactivitystatus (name, description, color, is_final, sort_order) VALUES 
('IN_PROGRESS', 'Currently being worked on', '#FF9800', FALSE, 1),
('DONE', 'Successfully completed and delivered', '#4CAF50', TRUE, 2);

-- Activity priorities with colors (2 essential priorities)
INSERT INTO cactivitypriority (name, description, priority_level, color, is_default) VALUES 
('HIGH', 'High priority - important for current sprint', 1, '#FF9800', FALSE),
('MEDIUM', 'Medium priority - normal task', 2, '#2196F3', TRUE);

-- Meeting types (2 essential types)
INSERT INTO cmeetingtype (name, description) VALUES 
('Sprint Planning', 'Sprint planning sessions for work estimation'),
('Daily Standup', 'Short daily synchronization meetings');

-- Meeting statuses with colors (2 essential statuses)
INSERT INTO cmeetingstatus (name, description, color, is_final, sort_order) VALUES 
('IN_PROGRESS', 'Meeting is currently in progress', '#FF9800', FALSE, 1),
('COMPLETED', 'Meeting has been completed', '#4CAF50', TRUE, 2);

-- Comment priorities with colors (2 essential priorities)
INSERT INTO ccommentpriority (name, description, priority_level, color, is_default) VALUES 
('HIGH', 'High priority comment needing quick response', 1, '#FF9800', FALSE),
('NORMAL', 'Normal priority comment', 2, '#2196F3', TRUE);

-- Decision types with colors (2 essential types)
INSERT INTO cdecisiontype (name, description, color, sort_order, requires_approval, is_active) VALUES 
('Technical', 'Technical architecture and implementation decisions', '#2196F3', 1, TRUE, TRUE),
('Strategic', 'High-level strategic decisions affecting project direction', '#FF5722', 2, TRUE, TRUE);

-- Decision statuses with colors (2 essential statuses)
INSERT INTO cdecisionstatus (name, description, color, is_final, sort_order, allows_editing, requires_approval) VALUES 
('UNDER_REVIEW', 'Decision is currently under review by stakeholders', '#2196F3', FALSE, 1, TRUE, TRUE),
('APPROVED', 'Decision has been approved and documented', '#4CAF50', TRUE, 2, FALSE, FALSE);

-- Order types (2 essential types)
INSERT INTO cordertype (name, description) VALUES 
('Software License', 'Software licensing and subscription orders'),
('Service Order', 'Orders for services and consultancy work');

-- Order statuses (2 essential statuses)
INSERT INTO corderstatus (name, description) VALUES 
('Submitted', 'Order has been submitted for approval'),
('Approved', 'Order has been approved and is ready for processing');

-- Currencies (2 essential currencies)
INSERT INTO ccurrency (name, description, currency_code, currency_symbol) VALUES 
('US Dollar', 'United States Dollar', 'USD', '$'),
('Euro', 'European Union Euro', 'EUR', '€');

-- Approval statuses (2 essential statuses)
INSERT INTO capprovalstatus (name, description) VALUES 
('Pending', 'Approval is pending review'),
('Approved', 'Approval has been granted');

-- =====================================================================
-- COMPANIES (2 examples with full enrichment)
-- =====================================================================
INSERT INTO ccompany (
    name, description, address, phone, email, website, tax_number, active
) VALUES 
('TechCorp Solutions Ltd.', 'Leading enterprise software development company specializing in web and mobile applications', 
 '456 Innovation Plaza, Silicon Valley, CA 94087', '+1-555-0101', 'info@techcorp-solutions.com', 'https://www.techcorp-solutions.com', 'US-TAX-2025-001', TRUE),
('Digital Innovations Inc.', 'Digital transformation consultancy focusing on AI, IoT, and cloud solutions for enterprise clients', 
 '789 Future Tech Center, Austin, TX 78701', '+1-555-0202', 'contact@digital-innovations.com', 'https://www.digital-innovations.com', 'US-TAX-2025-002', TRUE);

-- =====================================================================
-- USERS (2 examples with full enrichment including profile pictures)
-- =====================================================================
INSERT INTO cuser (
    created_date, email, active, lastname, login, name, password, phone, roles, 
    last_modified_date
) VALUES 
-- System Administrator with full enrichment
('2025-01-15 08:00:00', 'admin@derbent.tech', TRUE, 'Administrator', 'admin', 'System', 
 '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu', '+1-555-0001',  
 '2025-01-15 08:00:00'),

-- Senior Developer with full enrichment
('2025-01-15 10:00:00', 'sarah.developer@derbent.tech', TRUE, 'Johnson', 'sarah.johnson', 'Sarah', 
 '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu', '+1-555-2001', 'DEVELOPER,USER', 
 '2025-01-15 10:00:00', 2, 'TEAM_MEMBER');

-- =====================================================================
-- PROJECTS (2 examples with full enrichment and relations)
-- =====================================================================
INSERT INTO cproject (name, description, created_date, last_modified_date) VALUES 
('E-Commerce Platform Modernization', 
 'Comprehensive migration of legacy e-commerce system to modern microservices architecture with React frontend, Spring Boot backend, and enhanced security features', 
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Customer Analytics Dashboard', 
 'Real-time analytics dashboard for customer behavior tracking using machine learning, data visualization, and automated reporting capabilities', 
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- =====================================================================
-- MEETINGS (2 examples with full enrichment, relations, and participants)
-- =====================================================================
INSERT INTO cmeeting (
    name, description, meeting_date, end_date, project_id, cmeetingtype_id, 
    location, agenda, meeting_status_id, responsible_id, minutes, linked_element
) VALUES 
-- Sprint Planning meeting with full enrichment
('E-Commerce Sprint Planning Session', 
 'Comprehensive planning session for first sprint focusing on user authentication system and product catalog implementation',
 '2025-01-20 09:00:00', '2025-01-20 11:00:00', 1, 1, 
 'Conference Room A - Main Office', 
 'Review user stories, estimate development tasks, plan sprint 1 deliverables and establish acceptance criteria',
 2, 1, 
 'Sprint 1 committed with 25 story points. Team focused on authentication module and basic product catalog. Next review scheduled for Jan 27.',
 'JIRA-SPRINT-001'),

-- Daily Standup with relations and status
('Analytics Team Daily Standup', 
 'Daily team synchronization for customer analytics dashboard development progress and blocker resolution',
 '2025-01-21 09:00:00', '2025-01-21 09:15:00', 2, 2,
 'Virtual Meeting - Teams', 
 'Daily progress updates, blocker discussion, and sprint goal alignment',
 2, 2, 
 'Data pipeline integration 80% complete. Dashboard components on track. No critical blockers identified.',
 'DAILY-ANALYTICS-001');

-- Meeting participants (many-to-many relationships)
INSERT INTO cmeeting_participants (meeting_id, user_id) VALUES 
(1, 1), (1, 2),  -- Sprint planning: Admin and Sarah
(2, 1), (2, 2);  -- Daily standup: Admin and Sarah

-- Meeting attendees (who actually attended)
INSERT INTO cmeeting_attendees (meeting_id, user_id) VALUES 
(1, 1), (1, 2),  -- Sprint planning attendees
(2, 1), (2, 2);  -- Daily standup attendees

-- =====================================================================
-- ORDERS (2 examples with full enrichment, relations, and approval workflow)
-- =====================================================================
INSERT INTO corder (
    name, description, order_date, required_date, delivery_date,
    provider_company_name, provider_contact_name, provider_email,
    requestor_id, responsible_id, project_id, order_type_id, order_status_id, currency_id,
    estimated_cost, actual_cost, order_number, delivery_address,
    created_date, last_modified_date
) VALUES 
-- Software license order with full relations
('React UI Component Library Premium License', 
 'Enterprise premium React component library for e-commerce platform frontend development with advanced theming and customization capabilities',
 '2025-01-18', '2025-01-25', '2025-01-24',
 'Material-UI Technologies Inc.', 'John Smith', 'john.smith@mui.com',
 2, 1, 1, 1, 2, 1, 2400.00, 2400.00, 'MUI-2025-ENT-001', 
 '456 Innovation Plaza, Development Team Office',
 '2025-01-18 14:00:00', '2025-01-24 16:00:00'),

-- Service order with enriched details
('Analytics Consulting and Implementation Service', 
 'Expert consulting services for data visualization architecture, machine learning model implementation, and dashboard optimization for analytics platform',
 '2025-01-20', '2025-03-01', NULL,
 'Data Analytics Consulting LLC', 'Dr. Michael Brown', 'michael.brown@analytics-consulting.com',
 1, 2, 2, 2, 1, 2, 8500.00, NULL, 'DAC-2025-CONS-001', 
 'Remote consulting services with on-site workshops',
 '2025-01-20 15:30:00', '2025-01-20 15:30:00');

-- Order approvals with full workflow
INSERT INTO corderapproval (
    name, description, order_id, approver_id, approval_status_id,
    approval_date, comments, approval_level,
    created_date, last_modified_date
) VALUES 
-- Technical approval for React license
('Technical Review - React UI Components', 
 'Technical architecture review and approval for React UI component library integration',
 1, 2, 2, '2025-01-19 09:00:00', 
 'Material-UI components align perfectly with our design system and will accelerate frontend development by estimated 40%',
 1, '2025-01-18 14:30:00', '2025-01-19 09:00:00'),

-- Service approval for analytics consulting
('Service Review - Analytics Consulting', 
 'Technical and budget review for analytics consulting services engagement',
 2, 1, 1, NULL, NULL, 1,
 '2025-01-20 16:00:00', '2025-01-20 16:00:00');

-- =====================================================================
-- ACTIVITIES (2 examples with full enrichment, relations, and comprehensive tracking)
-- =====================================================================
INSERT INTO cactivity (
    name, description, project_id, cactivitytype_id, assigned_to_id, created_by_id, 
    cactivitystatus_id, cactivitypriority_id, estimated_hours, actual_hours, remaining_hours,
    estimated_cost, actual_cost, hourly_rate, start_date, due_date, completion_date,
    progress_percentage, acceptance_criteria, notes, created_date, last_modified_date
) VALUES 
-- User Story with comprehensive enrichment
('User Authentication System Implementation',
 'Complete user authentication and authorization system with social login integration, password reset functionality, and role-based access control for e-commerce platform',
 1, 1, 2, 1, 1, 1, 40.00, 15.00, 25.00, 2400.00, 900.00, 60.00,
 '2025-01-20', '2025-02-10', NULL, 37,
 'Users can register with email validation, login with email/social accounts (Google, GitHub), reset passwords securely, manage profiles, and system enforces role-based permissions',
 'OAuth integration with Google completed successfully. GitHub integration in progress. Password reset flow implemented with secure tokens. Role-based access control framework established.',
 '2025-01-18 09:00:00', '2025-01-25 14:30:00'),

-- Task with full tracking and relations
('Analytics Data Pipeline Architecture Design',
 'Design and implement scalable data pipeline architecture for real-time customer analytics processing with Kafka integration and ClickHouse storage optimization',
 2, 2, 1, 2, 2, 2, 24.00, 24.00, 0.00, 1440.00, 1440.00, 60.00,
 '2025-01-15', '2025-01-22', '2025-01-22', 100,
 'Data pipeline processes customer events in real-time with sub-5-second latency, handles 10,000+ events per second, includes proper error handling and monitoring',
 'Architecture design completed and approved. Kafka cluster configured with proper partitioning. ClickHouse schema optimized for time-series analytics queries. Performance benchmarks exceeded requirements.',
 '2025-01-15 10:00:00', '2025-01-22 17:00:00');

-- =====================================================================
-- COMMENTS (2 examples with full enrichment, relations, and priority)
-- =====================================================================
INSERT INTO ccomment (
    name, description, comment_text, activity_id, author_id, project_id, 
    priority_id, is_important, event_date, created_date, last_modified_date
) VALUES 
-- High priority comment with enriched content
('Critical Performance Issue Identified', 
 'High priority comment regarding performance bottleneck in authentication system',
 'URGENT: Discovered significant performance degradation in OAuth authentication flow during load testing. Response times are exceeding 3 seconds under concurrent user load. This requires immediate optimization before production deployment. Recommend implementing connection pooling and caching mechanisms.',
 1, 2, 1, 1, TRUE, '2025-01-16 14:15:00', '2025-01-16 14:15:00', '2025-01-16 14:15:00'),

-- Normal priority comment with detailed analysis
('Analytics Pipeline Performance Update', 
 'Progress update on data pipeline implementation with performance metrics',
 'Excellent progress on the analytics data pipeline! Current throughput testing shows we are processing 12,000 events per second with average latency of 2.8 seconds. ClickHouse queries are performing well with response times under 500ms for complex aggregations. The architecture is solid and ready for production scaling.',
 2, 1, 2, 2, FALSE, '2025-01-17 09:45:00', '2025-01-17 09:45:00', '2025-01-17 09:45:00');

-- =====================================================================
-- RISKS (2 examples with full enrichment and project relations)
-- =====================================================================
INSERT INTO crisk (name, description, project_id, risk_severity, created_date, last_modified_date) VALUES 
-- High severity risk with comprehensive description
('Authentication Security Vulnerability Risk', 
 'Critical risk of security vulnerabilities in user authentication system due to complex OAuth integration and session management. Potential for unauthorized access, session hijacking, or data breaches if not properly implemented with comprehensive security testing and code review.',
 1, 'HIGH', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Medium severity risk with detailed mitigation context
('Data Pipeline Performance Degradation Risk', 
 'Risk of analytics pipeline performance issues under high data volume loads, potentially leading to delayed insights, data processing backlogs, and customer analytics dashboard responsiveness problems. Requires careful monitoring and scaling strategies.',
 2, 'MEDIUM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- =====================================================================
-- DECISIONS (2 examples with full enrichment, relations, and approval workflow)
-- =====================================================================
INSERT INTO cdecision (
    name, description, project_id, decision_type_id, decision_status_id, 
    accountable_user_id, assigned_to_id, created_by_id, estimated_cost, 
    implementation_date, review_date, created_date, last_modified_date
) VALUES 
-- Technical decision with comprehensive details
('Microservices Architecture Adoption for E-Commerce Platform', 
 'Strategic decision to adopt microservices architecture pattern for e-commerce platform modernization, including service decomposition strategy, API gateway implementation, and container orchestration with Kubernetes for improved scalability, maintainability, and team autonomy',
 1, 1, 1, 1, 2, 1, 250000.00, '2025-02-01 00:00:00', '2025-04-01 00:00:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Strategic decision with full enrichment
('Analytics Technology Stack Selection', 
 'Strategic technology selection for customer analytics platform including Kafka for event streaming, ClickHouse for analytical database, React for dashboard frontend, and Python-based machine learning pipeline for advanced customer insights and predictive analytics',
 2, 2, 2, 2, 1, 1, 180000.00, '2025-01-25 00:00:00', '2025-03-25 00:00:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Decision approvals with enriched workflow
INSERT INTO cdecisionapproval (
    name, description, decision_id, approver_user_id, is_required, approval_priority, 
    due_date, created_date, last_modified_date
) VALUES 
-- Technical approval for microservices decision
('Senior Developer Technical Review', 
 'Comprehensive technical architecture review and approval for microservices adoption including scalability assessment and implementation feasibility',
 1, 2, TRUE, 1, '2025-01-25 00:00:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Strategic approval for analytics technology
('Strategic Technology Approval', 
 'Strategic review of analytics technology stack selection with focus on long-term maintainability, cost-effectiveness, and technical capabilities alignment',
 2, 1, TRUE, 1, '2025-01-28 00:00:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Decision team members (collaborative decisions)
INSERT INTO cdecision_team_members (decision_id, user_id) VALUES 
(1, 1), (1, 2),  -- Microservices decision team
(2, 1), (2, 2);  -- Analytics technology team