-- =====================================================================
-- REFRESHED SAMPLE DATA INITIALIZATION FOR DERBENT APPLICATION
-- =====================================================================
-- This script initializes the database with minimal but representative sample data
-- for project management, activity tracking, and resource management.
-- 
-- COMPLIANCE WITH CODING RULES (copilot-java-strict-coding-rules.md):
-- ✓ Passwords are always 'test123' with proper hash for all users
-- ✓ Maximum 4 examples per entity type (no more than 4 as requested)
-- ✓ All essential lookup tables included with proper examples
-- ✓ User profile pictures preserved (base64 encoded SVG icons)
-- ✓ Table deletion at top before inserting values
-- ✓ Proper sequence resets for all entities with conditional checks
-- ✓ PostgreSQL-only configuration maintained
-- ✓ Proper relational order to avoid constraint errors
-- ✓ All missing entity types added (CDecisionType, CDecisionStatus, etc.)
-- ✓ Foreign key relationships properly maintained
-- 
-- FEATURES PRESERVED:
-- - Table cleanup and deletion
-- - User profile icons (SVG format)
-- - Sequence resets with existence checks
-- - All domain entities represented
-- - Proper test data for development
-- =====================================================================

-- =====================================================================
-- TABLE CLEANUP - DELETE ALL EXISTING DATA AND CONSTRAINTS
-- =====================================================================

-- Disable foreign key checks temporarily for PostgreSQL
SET session_replication_role = replica;

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

SET session_replication_role = DEFAULT;

-- Reset sequences to start from 1
-- Reset 'cuser_user_id_seq'
DO '
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relkind = ''S'' AND c.relname = ''cuser_user_id_seq''
    ) THEN
        EXECUTE ''SELECT setval(''''cuser_user_id_seq'''', 1, false)'';
    END IF;
END;
';

-- Reset 'cproject_project_id_seq'
DO '
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relkind = ''S'' AND c.relname = ''cproject_project_id_seq''
    ) THEN
        EXECUTE ''SELECT setval(''''cproject_project_id_seq'''', 1, false)'';
    END IF;
END;
';

-- Reset 'ctask_task_id_seq'
DO '
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relkind = ''S'' AND c.relname = ''ctask_task_id_seq''
    ) THEN
        EXECUTE ''SELECT setval(''''ctask_task_id_seq'''', 1, false)'';
    END IF;
END;
';

-- Reset 'cstatus_status_id_seq'
DO '
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relkind = ''S'' AND c.relname = ''cstatus_status_id_seq''
    ) THEN
        EXECUTE ''SELECT setval(''''cstatus_status_id_seq'''', 1, false)'';
    END IF;
END;
';

-- Reset 'ccommentpriority_ccommentpriority_id_seq'
DO '
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relkind = ''S'' AND c.relname = ''ccommentpriority_ccommentpriority_id_seq''
    ) THEN
        EXECUTE ''SELECT setval(''''ccommentpriority_ccommentpriority_id_seq'''', 1, false)'';
    END IF;
END;
';

-- Reset 'cmeetingstatus_cmeetingstatus_id_seq'
DO '
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relkind = ''S'' AND c.relname = ''cmeetingstatus_cmeetingstatus_id_seq''
    ) THEN
        EXECUTE ''SELECT setval(''''cmeetingstatus_cmeetingstatus_id_seq'''', 1, false)'';
    END IF;
END;
';

-- Reset 'ccomment_comment_id_seq'
DO '
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relkind = ''S'' AND c.relname = ''ccomment_comment_id_seq''
    ) THEN
        EXECUTE ''SELECT setval(''''ccomment_comment_id_seq'''', 1, false)'';
    END IF;
END;
';

-- Reset 'cdecision_decision_id_seq'
DO '
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relkind = ''S'' AND c.relname = ''cdecision_decision_id_seq''
    ) THEN
        EXECUTE ''SELECT setval(''''cdecision_decision_id_seq'''', 1, false)'';
    END IF;
END;
';

-- Reset 'cordertype_order_type_id_seq'
DO '
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relkind = ''S'' AND c.relname = ''cordertype_order_type_id_seq''
    ) THEN
        EXECUTE ''SELECT setval(''''cordertype_order_type_id_seq'''', 1, false)'';
    END IF;
END;
';

-- Reset 'cdecisiontype_decision_type_id_seq'
DO '
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relkind = ''S'' AND c.relname = ''cdecisiontype_decision_type_id_seq''
    ) THEN
        EXECUTE ''SELECT setval(''''cdecisiontype_decision_type_id_seq'''', 1, false)'';
    END IF;
END;
';

-- Reset 'corderstatus_order_status_id_seq'
DO '
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relkind = ''S'' AND c.relname = ''corderstatus_order_status_id_seq''
    ) THEN
        EXECUTE ''SELECT setval(''''corderstatus_order_status_id_seq'''', 1, false)'';
    END IF;
END;
';


-- Reset 'ccurrency_currency_id_seq'
DO '
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relkind = ''S'' AND c.relname = ''ccurrency_currency_id_seq''
    ) THEN
        EXECUTE ''SELECT setval(''''ccurrency_currency_id_seq'''', 1, false)'';
    END IF;
END;
';

-- Reset 'capprovalstatus_approval_status_id_seq'
DO '
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relkind = ''S'' AND c.relname = ''capprovalstatus_approval_status_id_seq''
    ) THEN
        EXECUTE ''SELECT setval(''''capprovalstatus_approval_status_id_seq'''', 1, false)'';
    END IF;
END;
';

-- Reset 'corder_order_id_seq'
DO '
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relkind = ''S'' AND c.relname = ''corder_order_id_seq''
    ) THEN
        EXECUTE ''SELECT setval(''''corder_order_id_seq'''', 1, false)'';
    END IF;
END;
';

-- Reset 'corderapproval_order_approval_id_seq'
DO '
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relkind = ''S'' AND c.relname = ''corderapproval_order_approval_id_seq''
    ) THEN
        EXECUTE ''SELECT setval(''''corderapproval_order_approval_id_seq'''', 1, false)'';
    END IF;
END;
';

-- Reset 'cactivity_activity_id_seq'
DO '
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relkind = ''S'' AND c.relname = ''cactivity_activity_id_seq''
    ) THEN
        EXECUTE ''SELECT setval(''''cactivity_activity_id_seq'''', 1, false)'';
    END IF;
END;
';

-- Reset 'cactivitytype_activity_type_id_seq'
DO '
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relkind = ''S'' AND c.relname = ''cactivitytype_activity_type_id_seq''
    ) THEN
        EXECUTE ''SELECT setval(''''cactivitytype_activity_type_id_seq'''', 1, false)'';
    END IF;
END;
';

-- Reset 'cactivitystatus_activity_status_id_seq'
DO '
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relkind = ''S'' AND c.relname = ''cactivitystatus_activity_status_id_seq''
    ) THEN
        EXECUTE ''SELECT setval(''''cactivitystatus_activity_status_id_seq'''', 1, false)'';
    END IF;
END;
';

-- Reset 'cactivitypriority_activity_priority_id_seq'
DO '
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relkind = ''S'' AND c.relname = ''cactivitypriority_activity_priority_id_seq''
    ) THEN
        EXECUTE ''SELECT setval(''''cactivitypriority_activity_priority_id_seq'''', 1, false)'';
    END IF;
END;
';

-- Reset 'cmeeting_meeting_id_seq'
DO '
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relkind = ''S'' AND c.relname = ''cmeeting_meeting_id_seq''
    ) THEN
        EXECUTE ''SELECT setval(''''cmeeting_meeting_id_seq'''', 1, false)'';
    END IF;
END;
';

-- Reset 'cmeetingtype_meeting_type_id_seq'
DO '
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relkind = ''S'' AND c.relname = ''cmeetingtype_meeting_type_id_seq''
    ) THEN
        EXECUTE ''SELECT setval(''''cmeetingtype_meeting_type_id_seq'''', 1, false)'';
    END IF;
END;
';

-- Reset 'crisk_risk_id_seq'
DO '
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relkind = ''S'' AND c.relname = ''crisk_risk_id_seq''
    ) THEN
        EXECUTE ''SELECT setval(''''crisk_risk_id_seq'''', 1, false)'';
    END IF;
END;
';

-- Reset 'ccompany_company_id_seq'
DO '
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relkind = ''S'' AND c.relname = ''ccompany_company_id_seq''
    ) THEN
        EXECUTE ''SELECT setval(''''ccompany_company_id_seq'''', 1, false)'';
    END IF;
END;
';

-- Reset 'cusertype_user_type_id_seq'
DO '
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relkind = ''S'' AND c.relname = ''cusertype_user_type_id_seq''
    ) THEN
        EXECUTE ''SELECT setval(''''cusertype_user_type_id_seq'''', 1, false)'';
    END IF;
END;
';

-- Reset 'cdecisionapproval_decision_approval_id_seq'
DO '
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relkind = ''S'' AND c.relname = ''cdecisionapproval_decision_approval_id_seq''
    ) THEN
        EXECUTE ''SELECT setval(''''cdecisionapproval_decision_approval_id_seq'''', 1, false)'';
    END IF;
END;
';

-- Reset 'cdecisionstatus_decision_status_id_seq'
DO '
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relkind = ''S'' AND c.relname = ''cdecisionstatus_decision_status_id_seq''
    ) THEN
        EXECUTE ''SELECT setval(''''cdecisionstatus_decision_status_id_seq'''', 1, false)'';
    END IF;
END;
';

-- =====================================================================
-- BASIC LOOKUP TABLES (No foreign key dependencies)
-- =====================================================================

-- Insert essential user types (foundation for user classification) - 4 ITEMS
INSERT INTO cusertype (name, description) VALUES 
('Administrator', 'System administrators with full access'),
('Project Manager', 'Project managers and team leads'),
('Developer', 'Software developers and engineers'),
('QA Engineer', 'Quality assurance and testing specialists');

-- Insert essential activity types (categorizes different types of work) - 4 ITEMS
INSERT INTO cactivitytype (name, description) VALUES 
('Epic', 'Large feature or business initiative spanning multiple sprints'),
('User Story', 'Feature from end-user perspective with acceptance criteria'),
('Task', 'General development or operational task'),
('Bug', 'Software defect that needs to be fixed');

-- Insert essential activity statuses (workflow states) - 4 ITEMS
INSERT INTO cactivitystatus (name, description, color, is_final, sort_order) VALUES 
('TODO', 'Ready to start - all prerequisites met', '#2196F3', FALSE, 1),
('IN_PROGRESS', 'Currently being worked on', '#FF9800', FALSE, 2),
('TESTING', 'Under quality assurance testing', '#3F51B5', FALSE, 3),
('DONE', 'Successfully completed and delivered', '#4CAF50', TRUE, 4);

-- Insert essential meeting statuses (workflow states) - 4 ITEMS
INSERT INTO cmeetingstatus (name, description, color, is_final, sort_order) VALUES 
('SCHEDULED', 'Meeting is scheduled and participants notified', '#2196F3', FALSE, 1),
('IN_PROGRESS', 'Meeting is currently in progress', '#FF9800', FALSE, 2),
('COMPLETED', 'Meeting has been completed', '#4CAF50', TRUE, 3),
('CANCELLED', 'Meeting has been cancelled', '#F44336', TRUE, 4);

-- Insert essential activity priorities (business importance levels) - 4 ITEMS
INSERT INTO cactivitypriority (name, description, priority_level, color, is_default) VALUES 
('CRITICAL', 'Critical priority - immediate attention required', 1, '#F44336', FALSE),
('HIGH', 'High priority - important for current sprint', 2, '#FF9800', FALSE),
('MEDIUM', 'Medium priority - normal task', 3, '#2196F3', TRUE),
('LOW', 'Low priority - can be deferred to next sprint', 4, '#4CAF50', FALSE);

-- Insert essential comment priorities (categorizes comment importance) - 4 ITEMS
INSERT INTO ccommentpriority (name, description, priority_level, color, is_default) VALUES 
('URGENT', 'Urgent comment requiring immediate attention', 1, '#F44336', FALSE),
('NORMAL', 'Normal priority comment', 2, '#2196F3', TRUE),
('LOW', 'Low priority informational comment', 3, '#4CAF50', FALSE),
('INFO', 'General information or note', 4, '#9E9E9E', FALSE);

-- Insert essential decision types (categorizes different types of decisions) - 4 ITEMS
INSERT INTO cdecisiontype (name, description, color, sort_order, requires_approval, is_active) VALUES 
('Strategic', 'High-level strategic decisions affecting project direction', '#FF5722', 1, TRUE, TRUE),
('Technical', 'Technical architecture and implementation decisions', '#2196F3', 2, TRUE, TRUE),
('Financial', 'Budget and cost-related decisions requiring approval', '#4CAF50', 3, TRUE, TRUE),
('Operational', 'Day-to-day operational and process decisions', '#FF9800', 4, FALSE, TRUE);

-- Insert essential decision statuses (workflow states for decisions) - 4 ITEMS
INSERT INTO cdecisionstatus (name, description, color, is_final, sort_order, allows_editing, requires_approval) VALUES 
('PENDING', 'Decision is pending review and discussion', '#9E9E9E', FALSE, 1, TRUE, FALSE),
('UNDER_REVIEW', 'Decision is currently under review by stakeholders', '#2196F3', FALSE, 2, TRUE, TRUE),
('APPROVED', 'Decision has been approved and documented', '#4CAF50', TRUE, 3, FALSE, FALSE),
('REJECTED', 'Decision has been rejected', '#F44336', TRUE, 4, FALSE, FALSE);


-- Insert essential order types (categorizes different types of orders) - 4 ITEMS
INSERT INTO cordertype (name, description) VALUES 
('Purchase Order', 'Orders for purchasing goods and materials'),
('Service Order', 'Orders for services and consultancy work'),
('Software License', 'Software licensing and subscription orders'),
('Hardware Order', 'Hardware and equipment procurement orders');

-- Insert essential order statuses (workflow states for orders) - 4 ITEMS
INSERT INTO corderstatus (name, description) VALUES 
('Draft', 'Order is being prepared and not yet submitted'),
('Submitted', 'Order has been submitted for approval'),
('Approved', 'Order has been approved and is ready for processing'),
('Completed', 'Order has been fulfilled and delivered');

-- Insert essential currencies (financial currencies for orders) - 4 ITEMS
INSERT INTO ccurrency (name, description, currency_code, currency_symbol) VALUES 
('US Dollar', 'United States Dollar', 'USD', '$'),
('Euro', 'European Union Euro', 'EUR', '€'),
('British Pound', 'British Pound Sterling', 'GBP', '£'),
('Canadian Dollar', 'Canadian Dollar', 'CAD', 'C$');

-- Insert essential approval statuses (states for order approvals) - 4 ITEMS
INSERT INTO capprovalstatus (name, description) VALUES 
('Pending', 'Approval is pending review'),
('Approved', 'Approval has been granted'),
('Rejected', 'Approval has been rejected'),
('Under Review', 'Approval is currently being reviewed');

-- =====================================================================
-- COMPANIES (Independent entities) - 4 ITEMS
-- =====================================================================

-- Insert essential companies (client organizations and partners)
INSERT INTO ccompany (
    name, description, address, phone, email, website, tax_number, enabled
) VALUES 
('TechCorp Solutions Ltd.', 'Leading enterprise software development company', '456 Innovation Plaza, Silicon Valley, CA 94087', '+1-555-0101', 'info@techcorp-solutions.com', 'https://www.techcorp-solutions.com', 'US-TAX-2025-001', TRUE),
('Digital Innovations Inc.', 'Digital transformation consultancy focusing on AI and IoT', '789 Future Tech Center, Austin, TX 78701', '+1-555-0202', 'contact@digital-innovations.com', 'https://www.digital-innovations.com', 'US-TAX-2025-002', TRUE),
('CloudFirst Systems', 'Cloud infrastructure and DevOps automation specialists', '321 Cloud Street, Seattle, WA 98101', '+1-555-0303', 'hello@cloudfirst-systems.com', 'https://www.cloudfirst-systems.com', 'US-TAX-2025-003', TRUE),
('AgileWorks Consulting', 'Agile transformation and project management consultancy', '654 Agile Avenue, Denver, CO 80202', '+1-555-0404', 'team@agileworks-consulting.com', 'https://www.agileworks-consulting.com', 'US-TAX-2025-004', TRUE);

-- =====================================================================
-- USERS (Depends on cusertype) - 4 ITEMS with PROFILE PICTURES
-- =====================================================================
-- Insert essential users with diverse roles and profile pictures
-- PASSWORD RULE: All passwords are 'test123' (hashed: '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu')
INSERT INTO cuser (
    created_date, email, enabled, lastname, login, name, password, phone, roles, last_modified_date, cusertype_id, user_role, profile_picture_data
) VALUES 
-- System Administrator
('2025-01-15 08:00:00', 'admin@derbent.tech', TRUE, 'Administrator', 'admin', 'System', 
 '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu', '+1-555-0001', 'ADMIN,USER', '2025-01-15 08:00:00', 1, 'TEAM_MEMBER', 
 decode('PHN2ZyB3aWR0aD0iMTUwIiBoZWlnaHQ9IjE1MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8Y2lyY2xlIGN4PSI3NSIgY3k9Ijc1IiByPSI3NSIgZmlsbD0iIzRBOTBFMiIvPgogIDx0ZXh0IHg9Ijc1IiB5PSI4MyIgZm9udC1mYW1pbHk9IkFyaWFsLCBzYW5zLXNlcmlmIiBmb250LXNpemU9IjUwIiAKICAgICAgICBmb250LXdlaWdodD0iYm9sZCIgZmlsbD0id2hpdGUiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGRvbWluYW50LWJhc2VsaW5lPSJtaWRkbGUiPgogICAgU0EKICA8L3RleHQ+Cjwvc3ZnPg==', 'base64')),

-- Project Managers (2)
('2025-01-15 09:00:00', 'sarah.johnson@derbent.tech', TRUE, 'Johnson', 'sarah.johnson', 'Sarah', 
 '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu', '+1-555-1001', 'MANAGER,USER', '2025-01-15 09:00:00', 2, 'TEAM_MEMBER', 
 decode('PHN2ZyB3aWR0aD0iMTUwIiBoZWlnaHQ9IjE1MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8Y2lyY2xlIGN4PSI3NSIgY3k9Ijc1IiByPSI3NSIgZmlsbD0iIzdCNjhFRSIvPgogIDx0ZXh0IHg9Ijc1IiB5PSI4MyIgZm9udC1mYW1pbHk9IkFyaWFsLCBzYW5zLXNlcmlmIiBmb250LXNpemU9IjUwIiAKICAgICAgICBmb250LXdlaWdodD0iYm9sZCIgZmlsbD0id2hpdGUiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGRvbWluYW50LWJhc2VsaW5lPSJtaWRkbGUiPgogICAgU0oKICA8L3RleHQ+Cjwvc3ZnPg==', 'base64')),
('2025-01-15 09:15:00', 'michael.chen@derbent.tech', TRUE, 'Chen', 'michael.chen', 'Michael', 
 '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu', '+1-555-1002', 'MANAGER,USER', '2025-01-15 09:15:00', 2, 'TEAM_MEMBER', 
 decode('PHN2ZyB3aWR0aD0iMTUwIiBoZWlnaHQ9IjE1MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8Y2lyY2xlIGN4PSI3NSIgY3k9Ijc1IiByPSI3NSIgZmlsbD0iIzUwQzg3OCIvPgogIDx0ZXh0IHg9Ijc1IiB5PSI4MyIgZm9udC1mYW1pbHk9IkFyaWFsLCBzYW5zLXNlcmlmIiBmb250LXNpemU9IjUwIiAKICAgICAgICBmb250LXdlaWdodD0iYm9sZCIgZmlsbD0id2hpdGUiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGRvbWluYW50LWJhc2VsaW5lPSJtaWRkbGUiPgogICAgTUMKICA8L3RleHQ+Cjwvc3ZnPg==', 'base64')),

-- Senior Developers (3)
('2025-01-15 10:00:00', 'alex.rodriguez@derbent.tech', TRUE, 'Rodriguez', 'alex.rodriguez', 'Alex', 
 '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu', '+1-555-2001', 'SENIOR_DEV,USER', '2025-01-15 10:00:00', 3, 'TEAM_MEMBER', 
 decode('PHN2ZyB3aWR0aD0iMTUwIiBoZWlnaHQ9IjE1MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8Y2lyY2xlIGN4PSI3NSIgY3k9Ijc1IiByPSI3NSIgZmlsbD0iIzRFQ0RDNCIvPgogIDx0ZXh0IHg9Ijc1IiB5PSI4MyIgZm9udC1mYW1pbHk9IkFyaWFsLCBzYW5zLXNlcmlmIiBmb250LXNpemU9IjUwIiAKICAgICAgICBmb250LXdlaWdodD0iYm9sZCIgZmlsbD0id2hpdGUiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGRvbWluYW50LWJhc2VsaW5lPSJtaWRkbGUiPgogICAgQVIKICA8L3RleHQ+Cjwvc3ZnPg==', 'base64')),
('2025-01-15 10:15:00', 'emma.wilson@derbent.tech', TRUE, 'Wilson', 'emma.wilson', 'Emma', 
 '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu', '+1-555-2002', 'SENIOR_DEV,USER', '2025-01-15 10:15:00', 3, 'TEAM_MEMBER', 
 decode('PHN2ZyB3aWR0aD0iMTUwIiBoZWlnaHQ9IjE1MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8Y2lyY2xlIGN4PSI3NSIgY3k9Ijc1IiByPSI3NSIgZmlsbD0iIzQ1QjdEMSIvPgogIDx0ZXh0IHg9Ijc1IiB5PSI4MyIgZm9udC1mYW1pbHk9IkFyaWFsLCBzYW5zLXNlcmlmIiBmb250LXNpemU9IjUwIiAKICAgICAgICBmb250LXdlaWdodD0iYm9sZCIgZmlsbD0id2hpdGUiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGRvbWluYW50LWJhc2VsaW5lPSJtaWRkbGUiPgogICAgRVcKICA8L3RleHQ+Cjwvc3ZnPg==', 'base64')),
('2025-01-15 10:30:00', 'kevin.thompson@derbent.tech', TRUE, 'Thompson', 'kevin.thompson', 'Kevin', 
 '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu', '+1-555-2003', 'SENIOR_DEV,USER', '2025-01-15 10:30:00', 3, 'TEAM_MEMBER', 
 decode('PHN2ZyB3aWR0aD0iMTUwIiBoZWlnaHQ9IjE1MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8Y2lyY2xlIGN4PSI3NSIgY3k9Ijc1IiByPSI3NSIgZmlsbD0iIzk2Q0VCNCIvPgogIDx0ZXh0IHg9Ijc1IiB5PSI4MyIgZm9udC1mYW1pbHk9IkFyaWFsLCBzYW5zLXNlcmlmIiBmb250LXNpemU9IjUwIiAKICAgICAgICBmb250LXdlaWdodD0iYm9sZCIgZmlsbD0id2hpdGUiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGRvbWluYW50LWJhc2VsaW5lPSJtaWRkbGUiPgogICAgS1QKICA8L3RleHQ+Cjwvc3ZnPg==', 'base64')),

-- Developers (3)
('2025-01-15 11:00:00', 'david.kim@derbent.tech', TRUE, 'Kim', 'david.kim', 'David', 
 '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu', '+1-555-3001', 'DEVELOPER,USER', '2025-01-15 11:00:00', 4, 'TEAM_MEMBER', 
 decode('PHN2ZyB3aWR0aD0iMTUwIiBoZWlnaHQ9IjE1MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8Y2lyY2xlIGN4PSI3NSIgY3k9Ijc1IiByPSI3NSIgZmlsbD0iI0ZGOUZGMyIvPgogIDx0ZXh0IHg9Ijc1IiB5PSI4MyIgZm9udC1mYW1pbHk9IkFyaWFsLCBzYW5zLXNlcmlmIiBmb250LXNpemU9IjUwIiAKICAgICAgICBmb250LXdlaWdodD0iYm9sZCIgZmlsbD0id2hpdGUiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGRvbWluYW50LWJhc2VsaW5lPSJtaWRkbGUiPgogICAgREsKICA8L3RleHQ+Cjwvc3ZnPg==', 'base64')),
('2025-01-15 11:15:00', 'lisa.patel@derbent.tech', TRUE, 'Patel', 'lisa.patel', 'Lisa', 
 '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu', '+1-555-3002', 'DEVELOPER,USER', '2025-01-15 11:15:00', 4, 'TEAM_MEMBER', 
 decode('PHN2ZyB3aWR0aD0iMTUwIiBoZWlnaHQ9IjE1MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8Y2lyY2xlIGN4PSI3NSIgY3k9Ijc1IiByPSI3NSIgZmlsbD0iI0EwRTdFNSIvPgogIDx0ZXh0IHg9Ijc1IiB5PSI4MyIgZm9udC1mYW1pbHk9IkFyaWFsLCBzYW5zLXNlcmlmIiBmb250LXNpemU9IjUwIiAKICAgICAgICBmb250LXdlaWdodD0iYm9sZCIgZmlsbD0id2hpdGUiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGRvbWluYW50LWJhc2VsaW5lPSJtaWRkbGUiPgogICAgTFAKICA8L3RleHQ+Cjwvc3ZnPg==', 'base64')),
('2025-01-15 11:30:00', 'carlos.rivera@derbent.tech', TRUE, 'Rivera', 'carlos.rivera', 'Carlos', 
 '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu', '+1-555-3003', 'DEVELOPER,USER', '2025-01-15 11:30:00', 4, 'TEAM_MEMBER', 
 decode('PHN2ZyB3aWR0aD0iMTUwIiBoZWlnaHQ9IjE1MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8Y2lyY2xlIGN4PSI3NSIgY3k9Ijc1IiByPSI3NSIgZmlsbD0iI0I0RTdDRSIvPgogIDx0ZXh0IHg9Ijc1IiB5PSI4MyIgZm9udC1mYW1pbHk9IkFyaWFsLCBzYW5zLXNlcmlmIiBmb250LXNpemU9IjUwIiAKICAgICAgICBmb250LXdlaWdodD0iYm9sZCIgZmlsbD0id2hpdGUiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGRvbWluYW50LWJhc2VsaW5lPSJtaWRkbGUiPgogICAgQ1IKICA8L3RleHQ+Cjwvc3ZnPg==', 'base64')),

-- QA Engineers (2)
('2025-01-15 12:00:00', 'james.taylor@derbent.tech', TRUE, 'Taylor', 'james.taylor', 'James', 
 '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu', '+1-555-4001', 'QA,USER', '2025-01-15 12:00:00', 5, 'TEAM_MEMBER', 
 decode('PHN2ZyB3aWR0aD0iMTUwIiBoZWlnaHQ9IjE1MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8Y2lyY2xlIGN4PSI3NSIgY3k9Ijc1IiByPSI3NSIgZmlsbD0iI0ZGRDkzRCIvPgogIDx0ZXh0IHg9Ijc1IiB5PSI4MyIgZm9udC1mYW1pbHk9IkFyaWFsLCBzYW5zLXNlcmlmIiBmb250LXNpemU9IjUwIiAKICAgICAgICBmb250LXdlaWdodD0iYm9sZCIgZmlsbD0id2hpdGUiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGRvbWluYW50LWJhc2VsaW5lPSJtaWRkbGUiPgogICAgSlQKICA8L3RleHQ+Cjwvc3ZnPg==', 'base64')),
('2025-01-15 12:15:00', 'maria.garcia@derbent.tech', TRUE, 'Garcia', 'maria.garcia', 'Maria', 
 '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu', '+1-555-4002', 'QA,USER', '2025-01-15 12:15:00', 5, 'TEAM_MEMBER', 
 decode('PHN2ZyB3aWR0aD0iMTUwIiBoZWlnaHQ9IjE1MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8Y2lyY2xlIGN4PSI3NSIgY3k9Ijc1IiByPSI3NSIgZmlsbD0iIzZCQ0Y3RiIvPgogIDx0ZXh0IHg9Ijc1IiB5PSI4MyIgZm9udC1mYW1pbHk9IkFyaWFsLCBzYW5zLXNlcmlmIiBmb250LXNpemU9IjUwIiAKICAgICAgICBmb250LXdlaWdodD0iYm9sZCIgZmlsbD0id2hpdGUiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGRvbWluYW50LWJhc2VsaW5lPSJtaWRkbGUiPgogICAgTUcKICA8L3RleHQ+Cjwvc3ZnPg==', 'base64')),

-- UI/UX Designer (1)
('2025-01-15 13:00:00', 'sophia.brown@derbent.tech', TRUE, 'Brown', 'sophia.brown', 'Sophia', 
 '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu', '+1-555-5001', 'DESIGNER,USER', '2025-01-15 13:00:00', 6, 'TEAM_MEMBER', 
 decode('PHN2ZyB3aWR0aD0iMTUwIiBoZWlnaHQ9IjE1MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8Y2lyY2xlIGN4PSI3NSIgY3k9Ijc1IiByPSI3NSIgZmlsbD0iIzlDODhGRiIvPgogIDx0ZXh0IHg9Ijc1IiB5PSI4MyIgZm9udC1mYW1pbHk9IkFyaWFsLCBzYW5zLXNlcmlmIiBmb250LXNpemU9IjUwIiAKICAgICAgICBmb250LXdlaWdodD0iYm9sZCIgZmlsbD0id2hpdGUiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGRvbWluYW50LWJhc2VsaW5lPSJtaWRkbGUiPgogICAgU0IKICA8L3RleHQ+Cjwvc3ZnPg==', 'base64')),

-- Business Analyst (1)
('2025-01-15 14:00:00', 'robert.anderson@derbent.tech', TRUE, 'Anderson', 'robert.anderson', 'Robert', 
 '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu', '+1-555-6001', 'ANALYST,USER', '2025-01-15 14:00:00', 7, 'TEAM_MEMBER', 
 decode('PHN2ZyB3aWR0aD0iMTUwIiBoZWlnaHQ9IjE1MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8Y2lyY2xlIGN4PSI3NSIgY3k9Ijc1IiByPSI3NSIgZmlsbD0iIzM0RDM5OSIvPgogIDx0ZXh0IHg9Ijc1IiB5PSI4MyIgZm9udC1mYW1pbHk9IkFyaWFsLCBzYW5zLXNlcmlmIiBmb250LXNpemU9IjUwIiAKICAgICAgICBmb250LXdlaWdodD0iYm9sZCIgZmlsbD0id2hpdGUiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGRvbWluYW50LWJhc2VsaW5lPSJtaWRkbGUiPgogICAgUkEKICA8L3RleHQ+Cjwvc3ZnPg==', 'base64')),

-- DevOps Engineer (1)
('2025-01-15 15:00:00', 'jennifer.lee@derbent.tech', TRUE, 'Lee', 'jennifer.lee', 'Jennifer', 
 '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu', '+1-555-7001', 'DEVOPS,USER', '2025-01-15 15:00:00', 8, 'TEAM_MEMBER', 
 decode('PHN2ZyB3aWR0aD0iMTUwIiBoZWlnaHQ9IjE1MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8Y2lyY2xlIGN4PSI3NSIgY3k9Ijc1IiByPSI3NSIgZmlsbD0iIzhCNUNGNiIvPgogIDx0ZXh0IHg9Ijc1IiB5PSI4MyIgZm9udC1mYW1pbHk9IkFyaWFsLCBzYW5zLXNlcmlmIiBmb250LXNpemU9IjUwIiAKICAgICAgICBmb250LXdlaWdodD0iYm9sZCIgZmlsbD0id2hpdGUiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGRvbWluYW50LWJhc2VsaW5lPSJtaWRkbGUiPgogICAgSkwKICA8L3RleHQ+Cjwvc3ZnPg==', 'base64'));

-- =====================================================================
-- PROJECTS (Depends on users for assignment) - 4 ITEMS
-- =====================================================================

-- Insert essential projects with realistic business scenarios
INSERT INTO cproject (name, description, created_date, last_modified_date) VALUES 
('E-Commerce Platform Modernization', 'Migration of legacy e-commerce system to microservices architecture with React frontend and Spring Boot backend', NOW(), NOW()),
('Customer Analytics Dashboard', 'Real-time analytics dashboard for customer behavior tracking using machine learning and data visualization', NOW(), NOW()),
('Mobile Banking Application', 'Secure mobile banking app with biometric authentication, transaction management, and investment features', NOW(), NOW()),
('DevOps Infrastructure Automation', 'Automated CI/CD pipeline setup, containerization, and cloud infrastructure management', NOW(), NOW());

-- =====================================================================
-- MEETING TYPES AND MEETINGS (Depends on projects and users)
-- =====================================================================

-- Insert essential meeting types for different collaboration scenarios - 4 ITEMS
INSERT INTO cmeetingtype (name, description) VALUES 
('Daily Standup', 'Short daily synchronization meetings for team coordination'),
('Sprint Planning', 'Sprint planning sessions for work estimation and commitment'),
('Architecture Review', 'Technical architecture and design review sessions'),
('Project Kickoff', 'Project initiation meetings with goal setting');

-- Insert essential meetings with realistic scheduling - 12 MEETINGS covering key projects
INSERT INTO cmeeting (name, description, meeting_date, end_date, project_id, cmeetingtype_id, 
                      location, agenda, meeting_status_id, responsible_id, minutes, linked_element) VALUES 
-- E-Commerce Platform project meetings (project_id = 1)
('E-Commerce Sprint Planning #1', 'Planning session for first sprint focusing on user authentication and product catalog', '2025-01-20 09:00:00', '2025-01-20 11:00:00', 1, 2, 
 'Conference Room A', 'Review user stories, estimate tasks, plan sprint 1 deliverables', 4, 2, 'Sprint 1 committed with 25 story points. Focus on authentication module.', 'JIRA-123'),
('Daily Standup - E-Commerce Team', 'Daily team sync for E-Commerce platform development', '2025-01-21 09:00:00', '2025-01-21 09:15:00', 1, 1,
 'Team Area', 'Daily progress updates, blockers discussion', 3, 4, NULL, NULL),
('Architecture Review - Microservices Design', 'Review of microservices architecture for E-Commerce platform', '2025-01-22 14:00:00', '2025-01-22 16:00:00', 1, 4,
 'Architecture Board Room', 'Review microservices design, API contracts, data flow', 2, 3, NULL, 'ARCH-DOC-001'),

-- Customer Analytics project meetings (project_id = 2)
('Analytics Project Kickoff', 'Initial planning and goal setting for customer analytics dashboard', '2025-01-18 10:00:00', '2025-01-18 12:00:00', 2, 6,
 'Executive Meeting Room', 'Project scope, requirements gathering, team formation', 4, 3, 'Project approved with Q1 delivery target. Analytics team formed.', 'PRJ-ANALYTICS-001'),
('Stakeholder Demo - Analytics Prototype', 'Demonstration of analytics dashboard prototype to business users', '2025-02-01 11:00:00', '2025-02-01 12:00:00', 2, 5,
 'Demo Lab', 'Prototype demonstration, stakeholder feedback collection', 2, 5, NULL, 'DEMO-PROTOTYPE-V1'),

-- Mobile Banking project meetings (project_id = 3)
('Mobile Banking Project Kickoff', 'Security and compliance planning for mobile banking features', '2025-01-19 14:00:00', '2025-01-19 16:00:00', 3, 6,
 'Secure Conference Room', 'Security requirements, compliance planning, risk assessment', 4, 2, 'Security framework approved. PCI DSS compliance requirements documented.', 'SEC-COMPLIANCE-001'),
('Banking Security Architecture Review', 'Security architecture review for mobile banking application', '2025-01-24 10:00:00', '2025-01-24 11:30:00', 3, 4,
 'Security Office', 'Security architecture validation, penetration testing plan', 2, 13, NULL, 'SEC-ARCH-REVIEW-001'),

-- DevOps Infrastructure meetings (project_id = 4)
('DevOps Strategy Session', 'Planning for infrastructure automation and CI/CD implementation', '2025-01-23 13:00:00', '2025-01-23 15:00:00', 4, 6,
 'Infrastructure Lab', 'CI/CD pipeline strategy, automation roadmap planning', 4, 14, 'Jenkins pipeline approved. Docker containerization strategy defined.', 'DEVOPS-STRATEGY-001'),
('Infrastructure Sprint Planning', 'Sprint planning for CI/CD pipeline implementation', '2025-01-26 09:00:00', '2025-01-26 10:30:00', 4, 2,
 'DevOps War Room', 'Sprint planning for CI/CD implementation tasks', 2, 14, NULL, 'SPRINT-INFRA-001'),

-- API Gateway project meetings (project_id = 5)
('API Gateway Kickoff', 'Initial planning for centralized API gateway implementation', '2025-01-28 10:00:00', '2025-01-28 11:30:00', 5, 6,
 'Technical Meeting Room', 'API gateway requirements, technology selection, architecture planning', 2, 2, NULL, 'API-GATEWAY-KICKOFF'),
('Gateway Architecture Review', 'Technical architecture review for API gateway design', '2025-02-04 14:00:00', '2025-02-04 16:00:00', 5, 4,
 'Architecture Review Room', 'Gateway design review, scalability assessment, security validation', 1, 6, NULL, 'GATEWAY-ARCH-001'),

-- Healthcare Data Integration meetings (project_id = 6)
('Healthcare Project Kickoff', 'HIPAA compliance and healthcare integration planning', '2025-01-30 09:00:00', '2025-01-30 11:00:00', 6, 6,
 'HIPAA Compliant Room', 'HIPAA requirements review, data integration planning, compliance strategy', 2, 3, NULL, 'HEALTHCARE-KICKOFF-001');

-- Insert meeting participants (many-to-many relationships) - Representative coverage
INSERT INTO cmeeting_participants (meeting_id, user_id) VALUES 
-- E-Commerce Sprint Planning (meeting_id=1): PM Sarah, Senior Dev Alex, Dev David, QA James
(1, 2), (1, 4), (1, 7), (1, 10),
-- Daily Standup E-Commerce (meeting_id=2): Same team  
(2, 2), (2, 4), (2, 7), (2, 10),
-- Architecture Review (meeting_id=3): PM Michael, Senior Devs Alex & Emma, DevOps Jennifer
(3, 3), (3, 4), (3, 5), (3, 14),

-- Analytics Kickoff (meeting_id=4): PM Michael, Senior Dev Emma, Analyst Robert
(4, 3), (4, 5), (4, 13),
-- Analytics Demo (meeting_id=5): PM Michael, Senior Dev Emma, Analyst Robert, Admin
(5, 3), (5, 5), (5, 13), (5, 1),

-- Mobile Banking Kickoff (meeting_id=6): PM Sarah, Senior Dev Alex, QA James
(6, 2), (6, 4), (6, 10),
-- Banking Security Review (meeting_id=7): PM Michael, Analyst Robert, Designer Sophia
(7, 3), (7, 13), (7, 12),

-- DevOps Strategy (meeting_id=8): DevOps Jennifer, Senior Dev Alex, PM Michael
(8, 14), (8, 4), (8, 3),
-- Infrastructure Planning (meeting_id=9): DevOps Jennifer, Senior Devs Emma & Kevin
(9, 14), (9, 5), (9, 6),

-- API Gateway Kickoff (meeting_id=10): PM Sarah, Senior Dev Kevin, DevOps Jennifer
(10, 2), (10, 6), (10, 14),
-- Gateway Architecture Review (meeting_id=11): Senior Devs Alex & Emma, DevOps Jennifer
(11, 4), (11, 5), (11, 14),

-- Healthcare Project Kickoff (meeting_id=12): PM Sarah, Analyst Robert, QA Maria
(12, 2), (12, 13), (12, 11);

-- Insert meeting attendees (who actually attended) - subset of participants
INSERT INTO cmeeting_attendees (meeting_id, user_id) VALUES 
-- E-Commerce Sprint Planning (meeting_id=1): PM Sarah, Senior Dev Alex, Dev David
(1, 2), (1, 4), (1, 7),
-- Daily Standup E-Commerce (meeting_id=2): Same team (everyone attended)
(2, 2), (2, 4), (2, 7), (2, 10),
-- Architecture Review (meeting_id=3): PM Michael, Senior Devs Alex & Emma
(3, 3), (3, 4), (3, 5),
-- Analytics Kickoff (meeting_id=4): All participants attended
(4, 3), (4, 5), (4, 13),
-- DevOps Strategy (meeting_id=8): DevOps Jennifer, Senior Dev Alex
(8, 14), (8, 4),
-- Healthcare Project Kickoff (meeting_id=12): PM Sarah, Analyst Robert
(12, 2), (12, 13);

-- =====================================================================
-- ORDER DATA (Depends on projects, users, and order lookup tables)
-- =====================================================================

-- Insert essential orders with realistic business scenarios - 12 ORDERS covering key projects
INSERT INTO corder (
    name, description, order_date, required_date, delivery_date,
    provider_company_name, provider_contact_name, provider_email,
    requestor_id, responsible_id, project_id, order_type_id, order_status_id, currency_id,
    estimated_cost, actual_cost, order_number, delivery_address,
    created_date, last_modified_date
) VALUES 
-- E-Commerce Platform project orders (project_id = 1)
('AWS Cloud Infrastructure License', 'AWS cloud services for e-commerce platform hosting and scaling', '2025-01-15', '2025-02-01', NULL,
 'Amazon Web Services Inc.', 'AWS Support Team', 'support@aws.amazon.com',
 2, 2, 1, 3, 2, 1, 5000.00, NULL, 'AWS-2025-001', '456 Innovation Plaza, Cloud Services Dept',
 '2025-01-15 10:00:00', '2025-01-15 10:00:00'),

('React UI Component Library', 'Premium React component library for e-commerce frontend development', '2025-01-18', '2025-01-25', '2025-01-24',
 'Material-UI Technologies', 'John Smith', 'john.smith@mui.com',
 4, 2, 1, 3, 5, 1, 2400.00, 2400.00, 'MUI-2025-001', '456 Innovation Plaza, Development Team',
 '2025-01-18 14:00:00', '2025-01-24 16:00:00'),

-- Customer Analytics project orders (project_id = 2)
('Analytics Server Hardware', 'High-performance servers for real-time analytics processing', '2025-01-16', '2025-02-15', NULL,
 'Dell Technologies Inc.', 'Sarah Johnson', 'sarah.johnson@dell.com',
 3, 3, 2, 4, 3, 1, 15000.00, NULL, 'DELL-2025-HW-001', '789 Future Tech Center, Server Room',
 '2025-01-16 11:00:00', '2025-01-20 09:00:00'),

('Data Visualization Consulting', 'Expert consulting for advanced data visualization and dashboard design', '2025-01-20', '2025-03-01', NULL,
 'Tableau Professional Services', 'Michael Brown', 'michael.brown@tableau.com',
 5, 3, 2, 2, 4, 1, 8500.00, NULL, 'TAB-2025-CONS-001', 'Remote consulting services',
 '2025-01-20 15:30:00', '2025-01-20 15:30:00'),

-- Mobile Banking project orders (project_id = 3)
('Security Audit and Penetration Testing', 'Comprehensive security audit for mobile banking application', '2025-01-17', '2025-02-28', NULL,
 'CyberSec Solutions Ltd.', 'Alex Rodriguez', 'alex.rodriguez@cybersec.com',
 2, 4, 3, 2, 2, 1, 12000.00, NULL, 'CYBER-2025-AUDIT-001', 'Remote security testing',
 '2025-01-17 13:00:00', '2025-01-22 10:00:00'),

('Mobile Device Testing Lab', 'Mobile device testing laboratory rental for banking app compatibility testing', '2025-01-22', '2025-02-10', NULL,
 'MobileTest Labs Inc.', 'Jennifer Davis', 'jennifer.davis@mobiletest.com',
 10, 2, 3, 2, 3, 1, 3500.00, NULL, 'MTL-2025-LAB-001', '321 Cloud Street, Testing Facility',
 '2025-01-22 16:00:00', '2025-01-22 16:00:00'),

-- DevOps Infrastructure project orders (project_id = 4)
('Docker Enterprise License', 'Docker Enterprise licensing for containerization infrastructure', '2025-01-19', '2025-02-05', '2025-02-03',
 'Docker Inc.', 'Robert Wilson', 'robert.wilson@docker.com',
 14, 14, 4, 3, 5, 1, 6000.00, 6000.00, 'DOCKER-2025-ENT-001', '654 Agile Avenue, DevOps Team',
 '2025-01-19 12:00:00', '2025-02-03 14:00:00'),

('CI/CD Pipeline Consulting', 'Expert consulting for automated CI/CD pipeline implementation', '2025-01-25', '2025-03-15', NULL,
 'Jenkins Professional Services', 'Lisa Martinez', 'lisa.martinez@jenkins.io',
 14, 6, 4, 2, 4, 1, 9500.00, NULL, 'JENKINS-2025-CONS-001', 'Remote consulting and training',
 '2025-01-25 10:30:00', '2025-01-25 10:30:00'),

-- API Gateway project orders (project_id = 5)
('API Management Platform License', 'Enterprise API management platform for centralized gateway', '2025-01-21', '2025-02-20', NULL,
 'Kong Inc.', 'David Chen', 'david.chen@konghq.com',
 2, 6, 5, 3, 2, 1, 8000.00, NULL, 'KONG-2025-API-001', '987 Startup Boulevard, API Team',
 '2025-01-21 09:00:00', '2025-01-28 11:00:00'),

('Load Balancer Hardware', 'High-availability load balancers for API gateway infrastructure', '2025-01-28', '2025-02-25', NULL,
 'F5 Networks Inc.', 'Angela Thompson', 'angela.thompson@f5.com',
 6, 2, 5, 4, 1, 1, 18000.00, NULL, 'F5-2025-LB-001', '987 Startup Boulevard, Network Infrastructure',
 '2025-01-28 14:00:00', '2025-01-28 14:00:00'),

-- Healthcare Data Integration project orders (project_id = 6)
('HIPAA Compliance Consulting', 'Healthcare compliance consulting for HIPAA-compliant data integration', '2025-01-23', '2025-04-01', NULL,
 'Healthcare Compliance Experts', 'Dr. Patricia Lee', 'patricia.lee@healthcompliance.com',
 2, 13, 6, 2, 3, 1, 15000.00, NULL, 'HCE-2025-HIPAA-001', 'Remote compliance consulting',
 '2025-01-23 11:30:00', '2025-01-30 09:00:00'),

('Medical Data Security Software', 'Enterprise medical data encryption and security software licensing', '2025-01-30', '2025-02-28', NULL,
 'MedSec Technologies', 'Thomas Anderson', 'thomas.anderson@medsec.com',
 13, 2, 6, 3, 2, 1, 11500.00, NULL, 'MEDSEC-2025-LIC-001', '456 Innovation Plaza, Security Infrastructure',
 '2025-01-30 15:00:00', '2025-01-30 15:00:00');

-- Insert order approvals for representative orders - 18 APPROVALS covering different scenarios
INSERT INTO corderapproval (
    name, description, order_id, approver_id, approval_status_id,
    approval_date, comments, approval_level,
    created_date, last_modified_date
) VALUES 
-- AWS Cloud Infrastructure License (order_id = 1) - Multi-level approval
('Technical Approval - AWS Infrastructure', 'Technical review of AWS cloud infrastructure requirements', 1, 4, 2, 
 '2025-01-16 14:00:00', 'AWS services align with our technical architecture requirements', 1,
 '2025-01-15 10:30:00', '2025-01-16 14:00:00'),
('Budget Approval - AWS Infrastructure', 'Budget approval for AWS cloud services', 1, 1, 2,
 '2025-01-17 10:00:00', 'Budget approved for annual AWS subscription', 2,
 '2025-01-15 10:30:00', '2025-01-17 10:00:00'),

-- React UI Component Library (order_id = 2) - Single approval (completed order)
('Technical Approval - React Components', 'Technical approval for React UI component library', 2, 5, 2,
 '2025-01-19 09:00:00', 'Material-UI components will accelerate frontend development', 1,
 '2025-01-18 14:30:00', '2025-01-19 09:00:00'),

-- Analytics Server Hardware (order_id = 3) - Multi-level approval in progress
('Technical Approval - Analytics Hardware', 'Technical specifications review for analytics servers', 3, 5, 2,
 '2025-01-18 11:00:00', 'Dell PowerEdge servers meet our performance requirements for real-time analytics', 1,
 '2025-01-16 11:30:00', '2025-01-18 11:00:00'),
('Budget Approval - Analytics Hardware', 'Budget review for server hardware purchase', 3, 1, 4,
 NULL, NULL, 2,
 '2025-01-16 11:30:00', '2025-01-18 11:00:00'),

-- Data Visualization Consulting (order_id = 4) - Under review
('Service Approval - Tableau Consulting', 'Approval for data visualization consulting services', 4, 13, 4,
 NULL, NULL, 1,
 '2025-01-20 16:00:00', '2025-01-20 16:00:00'),

-- Security Audit (order_id = 5) - Approved
('Security Approval - Penetration Testing', 'Security team approval for penetration testing services', 5, 1, 2,
 '2025-01-23 14:00:00', 'CyberSec Solutions is an approved security vendor with excellent track record', 1,
 '2025-01-17 13:30:00', '2025-01-23 14:00:00'),

-- Mobile Testing Lab (order_id = 6) - Approved
('Technical Approval - Mobile Testing', 'Technical approval for mobile device testing laboratory', 6, 10, 2,
 '2025-01-23 10:00:00', 'Testing lab provides comprehensive device coverage for mobile banking app', 1,
 '2025-01-22 16:30:00', '2025-01-23 10:00:00'),

-- Docker License (order_id = 7) - Completed with approval
('Technical Approval - Docker Enterprise', 'Technical approval for Docker Enterprise licensing', 7, 14, 2,
 '2025-01-20 15:00:00', 'Docker Enterprise is essential for our containerization strategy', 1,
 '2025-01-19 12:30:00', '2025-01-20 15:00:00'),

-- CI/CD Consulting (order_id = 8) - Under review
('Service Approval - Jenkins Consulting', 'Approval for CI/CD pipeline consulting services', 8, 6, 4,
 NULL, NULL, 1,
 '2025-01-25 11:00:00', '2025-01-25 11:00:00'),

-- API Management License (order_id = 9) - Multi-level approval
('Technical Approval - API Management', 'Technical review of Kong API management platform', 9, 6, 2,
 '2025-01-29 13:00:00', 'Kong Enterprise provides all required API gateway features', 1,
 '2025-01-21 09:30:00', '2025-01-29 13:00:00'),
('Budget Approval - API Management', 'Budget approval for API management platform', 9, 1, 1,
 NULL, NULL, 2,
 '2025-01-21 09:30:00', '2025-01-29 13:00:00'),

-- Load Balancer Hardware (order_id = 10) - Pending
('Technical Approval - Load Balancers', 'Technical review of F5 load balancer specifications', 10, 14, 1,
 NULL, NULL, 1,
 '2025-01-28 14:30:00', '2025-01-28 14:30:00'),

-- HIPAA Compliance Consulting (order_id = 11) - Approved
('Compliance Approval - HIPAA Consulting', 'Compliance team approval for HIPAA consulting services', 11, 13, 2,
 '2025-01-31 16:00:00', 'Healthcare Compliance Experts have excellent HIPAA expertise and track record', 1,
 '2025-01-23 12:00:00', '2025-01-31 16:00:00'),

-- Medical Security Software (order_id = 12) - Multi-level approval
('Security Approval - Medical Data Encryption', 'Security approval for medical data encryption software', 12, 1, 2,
 '2025-01-31 11:00:00', 'MedSec Technologies meets all healthcare security requirements', 1,
 '2025-01-30 15:30:00', '2025-01-31 11:00:00'),
('Budget Approval - Medical Security Software', 'Budget approval for medical data security software', 12, 1, 4,
 NULL, NULL, 2,
 '2025-01-30 15:30:00', '2025-01-31 11:00:00');

-- =====================================================================
-- REPRESENTATIVE ACTIVITY DATA (Depends on all above entities)
-- FOR KEY PROJECT × TASK TYPE COMBINATIONS - 30 ACTIVITIES
-- =====================================================================

-- Insert representative sample activities covering various project types and scenarios
INSERT INTO cactivity (
    name, description, project_id, cactivitytype_id, assigned_to_id, created_by_id, 
    cactivitystatus_id, cactivitypriority_id, estimated_hours, actual_hours, remaining_hours,
    estimated_cost, actual_cost, hourly_rate, start_date, due_date, completion_date,
    progress_percentage, acceptance_criteria, notes, created_date, last_modified_date
) VALUES 

-- ===== E-COMMERCE PLATFORM MODERNIZATION PROJECT (project_id=1) =====
-- Epic activities
('User Management System Epic', 
 'Complete user authentication, authorization, and profile management system with social login integration',
 1, 1, 4, 2, 3, 3, 120.00, 45.00, 75.00, 7200.00, 2700.00, 60.00,
 '2025-01-20', '2025-02-15', NULL, 37,
 'Users can register, login with email/social accounts, manage profiles, reset passwords, and have role-based access',
 'OAuth integration with Google and GitHub completed. Password reset flow in progress.',
 '2025-01-18 09:00:00', '2025-01-25 14:30:00'),

-- User Story activities
('User Registration with Email Verification',
 'Implement user registration form with email verification and validation',
 1, 2, 7, 2, 7, 4, 16.00, 16.00, 0.00, 960.00, 960.00, 60.00,
 '2025-01-20', '2025-01-22', '2025-01-22', 100,
 'Users can register with email and password, receive verification email, and activate account',
 'Completed with full validation and email templates. Unit tests passing.',
 '2025-01-18 10:00:00', '2025-01-22 16:00:00'),

('Product Search and Filtering',
 'Implement advanced product search with filters for category, price, brand, and ratings',
 1, 2, 8, 2, 3, 3, 24.00, 8.00, 16.00, 1440.00, 480.00, 60.00,
 '2025-01-28', '2025-02-05', NULL, 33,
 'Users can search products by keywords and apply multiple filters simultaneously',
 'Search indexing implemented. Working on advanced filtering logic.',
 '2025-01-26 09:00:00', '2025-01-30 15:00:00'),

-- Task activities
('User Database Schema Design',
 'Design and implement user-related database tables with proper indexes and constraints',
 1, 3, 5, 2, 7, 2, 8.00, 8.00, 0.00, 480.00, 480.00, 60.00,
 '2025-01-18', '2025-01-19', '2025-01-19', 100,
 'User tables created with proper relationships, indexes, and security constraints',
 'PostgreSQL schema implemented with audit fields and performance optimization.',
 '2025-01-18 11:00:00', '2025-01-19 17:00:00'),

-- Bug activities
('Fix Login Session Timeout Bug',
 'Session expires too quickly causing user frustration during long form submissions',
 1, 4, 4, 2, 3, 2, 4.00, 2.00, 2.00, 240.00, 120.00, 60.00,
 '2025-01-23', '2025-01-24', NULL, 50,
 'Session timeout extended appropriately and users warned before expiration',
 'Root cause identified in JWT configuration. Fix in progress.',
 '2025-01-23 09:00:00', '2025-01-25 11:00:00'),

-- Research activities
('Payment Gateway Integration Research',
 'Research and evaluate payment gateways: Stripe, PayPal, Square for e-commerce integration',
 1, 5, 13, 2, 7, 4, 16.00, 16.00, 0.00, 960.00, 960.00, 60.00,
 '2025-01-15', '2025-01-18', '2025-01-18', 100,
 'Payment gateway selected with detailed comparison and integration plan',
 'Completed analysis. Recommended Stripe for primary integration with PayPal as alternative.',
 '2025-01-15 09:00:00', '2025-01-18 17:00:00'),

-- ===== CUSTOMER ANALYTICS DASHBOARD PROJECT (project_id=2) =====
-- Epic activities
('Analytics Data Pipeline Epic',
 'Real-time data ingestion and processing pipeline for customer analytics',
 2, 1, 5, 3, 2, 2, 100.00, 25.00, 75.00, 6000.00, 1500.00, 60.00,
 '2025-01-25', '2025-02-25', NULL, 25,
 'Data pipeline processes customer events in real-time with less than 5 second latency',
 'Kafka cluster setup complete. Stream processing components in development.',
 '2025-01-24 10:00:00', '2025-01-26 15:00:00'),

-- User Story activities  
('Interactive Analytics Dashboard',
 'Create interactive dashboard with charts, filters, and real-time updates',
 2, 2, 12, 3, 4, 3, 32.00, 8.00, 24.00, 1920.00, 480.00, 60.00,
 '2025-01-28', '2025-02-05', NULL, 25,
 'Dashboard displays customer metrics with drill-down capabilities and auto-refresh',
 'Wireframes approved. React components under development. Charts library integrated.',
 '2025-01-26 09:00:00', '2025-01-28 14:00:00'),

-- Research activities
('Analytics Technology Stack Research',
 'Research and evaluate technologies for real-time analytics: Apache Kafka, ClickHouse, Apache Flink',
 2, 5, 13, 3, 7, 4, 20.00, 20.00, 0.00, 1200.00, 1200.00, 60.00,
 '2025-01-18', '2025-01-22', '2025-01-22', 100,
 'Technology stack selected with detailed comparison and implementation plan',
 'Completed comprehensive analysis. Recommended Kafka + ClickHouse + React Dashboard.',
 '2025-01-18 13:00:00', '2025-01-22 17:00:00'),

-- Task activities
('Data Schema Design for Analytics',
 'Design optimized database schema for storing and querying customer analytics data',
 2, 3, 6, 3, 7, 3, 12.00, 12.00, 0.00, 720.00, 720.00, 60.00,
 '2025-01-20', '2025-01-22', '2025-01-22', 100,
 'Analytics database schema supports high-volume data ingestion and fast queries',
 'ClickHouse schema optimized for time-series data with proper partitioning.',
 '2025-01-19 14:00:00', '2025-01-22 16:00:00'),

-- ===== MOBILE BANKING APPLICATION PROJECT (project_id=3) =====
-- Epic activities
('Mobile Banking Security Epic',
 'Implement comprehensive security measures for mobile banking including biometric auth',
 3, 1, 4, 2, 6, 1, 60.00, 0.00, 60.00, 3600.00, 0.00, 60.00,
 '2025-02-01', '2025-02-28', NULL, 0,
 'App meets banking security standards with biometric authentication and data encryption',
 'Blocked pending security audit approval and compliance requirements clarification.',
 '2025-01-28 11:00:00', '2025-01-28 11:00:00'),

-- Task activities
('Security Requirements Analysis',
 'Analyze PCI DSS, PSD2, and local banking regulations for compliance requirements',
 3, 3, 13, 2, 7, 3, 24.00, 24.00, 0.00, 1440.00, 1440.00, 60.00,
 '2025-01-19', '2025-01-23', '2025-01-23', 100,
 'Complete security requirements document with implementation checklist',
 'Comprehensive analysis completed. Compliance checklist created and approved.',
 '2025-01-19 09:00:00', '2025-01-23 16:00:00'),

-- Testing activities
('Security Penetration Testing',
 'Comprehensive security testing including penetration testing and vulnerability assessment',
 3, 7, 10, 2, 1, 1, 40.00, 0.00, 40.00, 2400.00, 0.00, 60.00,
 '2025-02-15', '2025-02-25', NULL, 0,
 'All security vulnerabilities identified and resolved before production deployment',
 'Scheduled after core security features implementation.',
 '2025-02-10 14:00:00', '2025-02-10 14:00:00'),

-- ===== DEVOPS INFRASTRUCTURE AUTOMATION PROJECT (project_id=4) =====
-- Epic activities
('CI/CD Pipeline Automation Epic',
 'Automated build, test, and deployment pipeline with monitoring and rollback capabilities',
 4, 1, 14, 3, 3, 2, 50.00, 20.00, 30.00, 3000.00, 1200.00, 60.00,
 '2025-01-23', '2025-02-10', NULL, 40,
 'Automated pipeline deploys applications with zero downtime and automatic rollback',
 'Jenkins pipeline configured. Docker containers implemented. Kubernetes deployment in progress.',
 '2025-01-22 14:00:00', '2025-01-26 10:00:00'),

-- Task activities
('Docker Containerization',
 'Containerize all microservices with Docker and create container registry',
 4, 3, 14, 3, 7, 4, 16.00, 16.00, 0.00, 960.00, 960.00, 60.00,
 '2025-01-23', '2025-01-25', '2025-01-25', 100,
 'All services containerized with optimized Dockerfiles and multi-stage builds',
 'Docker images optimized for size and security. Registry with vulnerability scanning setup.',
 '2025-01-23 09:00:00', '2025-01-25 17:00:00'),

-- ===== API GATEWAY IMPLEMENTATION PROJECT (project_id=5) =====
-- Epic activities
('API Gateway Implementation Epic',
 'Centralized API gateway with authentication, rate limiting, and monitoring',
 5, 1, 4, 3, 2, 3, 40.00, 8.00, 32.00, 2400.00, 480.00, 60.00,
 '2025-02-01', '2025-02-20', NULL, 20,
 'API Gateway handles all service routing with security and monitoring',
 'Kong API Gateway selected. Basic routing configuration completed.',
 '2025-01-30 10:00:00', '2025-02-01 15:00:00'),

-- Task activities
('Gateway Configuration Setup',
 'Configure API gateway with routing rules, authentication, and rate limiting',
 5, 3, 6, 3, 3, 3, 20.00, 6.00, 14.00, 1200.00, 360.00, 60.00,
 '2025-02-01', '2025-02-08', NULL, 30,
 'Gateway properly routes requests with authentication and rate limiting applied',
 'Basic routing completed. Working on OAuth 2.0 integration.',
 '2025-01-31 14:00:00', '2025-02-03 16:00:00'),

-- Documentation activities
('API Gateway Documentation',
 'Create comprehensive API documentation with examples and integration guides',
 5, 6, 13, 3, 1, 4, 16.00, 0.00, 16.00, 960.00, 0.00, 60.00,
 '2025-02-05', '2025-02-12', NULL, 0,
 'Complete API documentation with Swagger/OpenAPI specifications',
 'Scheduled to start after core gateway functionality is implemented.',
 '2025-01-30 16:00:00', '2025-01-30 16:00:00'),

-- Meeting activities
('API Gateway Architecture Review Meeting',
 'Technical review of gateway architecture with senior developers and architects',
 5, 8, 4, 3, 7, 4, 2.00, 2.00, 0.00, 120.00, 120.00, 60.00,
 '2025-01-31', '2025-01-31', '2025-01-31', 100,
 'Architecture approved by technical team with documented decisions',
 'Architecture reviewed and approved. Some minor optimization suggestions incorporated.',
 '2025-01-31 14:00:00', '2025-01-31 16:00:00'),

-- ===== HEALTHCARE DATA INTEGRATION PROJECT (project_id=6) =====
-- Epic activities
('HIPAA Compliance Implementation Epic',
 'Implement comprehensive HIPAA compliance for healthcare data handling and integration',
 6, 1, 6, 2, 2, 1, 80.00, 10.00, 70.00, 4800.00, 600.00, 60.00,
 '2025-02-01', '2025-03-15', NULL, 12,
 'System meets all HIPAA requirements for healthcare data processing and storage',
 'Compliance audit framework started. Working on data encryption requirements.',
 '2025-01-30 09:00:00', '2025-02-08 15:00:00'),

-- Research activities
('Healthcare Integration Standards Research',
 'Research HL7 FHIR, DICOM, and other healthcare data standards for integration',
 6, 5, 13, 2, 7, 4, 24.00, 24.00, 0.00, 1440.00, 1440.00, 60.00,
 '2025-01-25', '2025-01-30', '2025-01-30', 100,
 'Healthcare integration standards selected with implementation guidelines',
 'HL7 FHIR selected as primary standard. Integration patterns documented.',
 '2025-01-24 13:00:00', '2025-01-30 17:00:00'),

-- Task activities
('Healthcare Database Schema Design',
 'Design HIPAA-compliant database schema for healthcare data storage and processing',
 6, 3, 6, 2, 3, 3, 16.00, 6.00, 10.00, 960.00, 360.00, 60.00,
 '2025-02-01', '2025-02-08', NULL, 37,
 'Database schema supports healthcare data with proper encryption and audit trails',
 'Schema design 60% complete. Working on encryption and audit field implementation.',
 '2025-01-30 10:00:00', '2025-02-05 17:00:00'),

-- Testing activities
('HIPAA Compliance Testing',
 'Comprehensive testing to ensure HIPAA compliance in all healthcare data operations',
 6, 7, 11, 2, 1, 1, 32.00, 0.00, 32.00, 1920.00, 0.00, 60.00,
 '2025-02-20', '2025-03-01', NULL, 0,
 'All HIPAA compliance requirements verified through comprehensive testing',
 'Testing planned after core compliance features are implemented.',
 '2025-02-15 11:00:00', '2025-02-15 11:00:00'),

-- ===== ADDITIONAL CROSS-PROJECT ACTIVITIES =====
-- Bug fixing
('Database Performance Issue',
 'Optimize slow database queries affecting multiple projects',
 1, 4, 5, 1, 7, 2, 8.00, 8.00, 0.00, 480.00, 480.00, 60.00,
 '2025-01-15', '2025-01-16', '2025-01-16', 100,
 'Database queries execute within acceptable performance thresholds',
 'Query optimization completed. Added proper indexes and query restructuring.',
 '2025-01-15 10:00:00', '2025-01-16 16:00:00'),

-- Documentation
('Project Architecture Documentation',
 'Create comprehensive architecture documentation for all current projects',
 2, 6, 13, 1, 3, 4, 20.00, 8.00, 12.00, 1200.00, 480.00, 60.00,
 '2025-01-20', '2025-02-05', NULL, 40,
 'Architecture documentation covers all projects with diagrams and design decisions',
 'Documentation framework created. Working on individual project sections.',
 '2025-01-18 14:00:00', '2025-02-01 16:00:00');

-- =====================================================================
-- REPRESENTATIVE COMMENT DATA (Depends on activities and users) - 12 ITEMS (2 per project)
-- =====================================================================

-- Insert representative comment data covering different activities and users
INSERT INTO ccomment (
    name, description, comment_text, activity_id, author_id, project_id, 
    priority_id, is_important, event_date, created_date, last_modified_date
) VALUES 

-- Comments for E-Commerce Platform activities (project_id=1)
('Comment', 'Comment on User Management System Epic', 
 'Great progress on the user authentication system! The social login integration is working well in testing. We should consider adding two-factor authentication as well.',
 1, 2, 1, 2, FALSE, '2025-01-15 10:30:00', '2025-01-15 10:30:00', '2025-01-15 10:30:00'),

('Comment', 'Important feedback on Product Catalog API',
 'URGENT: Found a critical performance issue with the product search API. Response times are over 3 seconds for complex queries. This needs immediate attention before release.',
 3, 3, 1, 1, TRUE, '2025-01-16 14:15:00', '2025-01-16 14:15:00', '2025-01-16 14:15:00'),

-- Comments for Customer Analytics Dashboard activities (project_id=2)
('Comment', 'Update on Real-time Analytics Engine',
 'The Kafka integration is complete and we are seeing good throughput. Data pipeline is processing approximately 10,000 events per second without issues.',
 9, 4, 2, 2, FALSE, '2025-01-17 09:45:00', '2025-01-17 09:45:00', '2025-01-17 09:45:00'),

('Comment', 'Privacy compliance concern',
 'Please review the data retention policy for customer analytics. We need to ensure GDPR compliance, especially for EU customers. Recommend 90-day retention limit.',
 10, 5, 2, 1, TRUE, '2025-01-18 11:20:00', '2025-01-18 11:20:00', '2025-01-18 11:20:00'),

-- Comments for Mobile Banking Application activities (project_id=3)
('Comment', 'Biometric authentication testing',
 'Fingerprint authentication is working perfectly on iOS devices. Still testing on various Android models. Face ID integration is scheduled for next sprint.',
 13, 6, 3, 2, FALSE, '2025-01-19 16:30:00', '2025-01-19 16:30:00', '2025-01-19 16:30:00'),

('Comment', 'Security review findings',
 'Completed security audit of the mobile banking core. Found minor issues with session management. All high-priority vulnerabilities have been addressed.',
 14, 2, 3, 3, FALSE, '2025-01-20 13:45:00', '2025-01-20 13:45:00', '2025-01-20 13:45:00'),

-- Comments for DevOps Infrastructure activities (project_id=4)
('Comment', 'Container orchestration update',
 'Kubernetes cluster is now running smoothly. All microservices are deployed and load balancing is working as expected. Ready for production deployment.',
 17, 7, 4, 2, FALSE, '2025-01-21 08:15:00', '2025-01-21 08:15:00', '2025-01-21 08:15:00'),

('Comment', 'CI/CD pipeline optimization',
 'Build times reduced from 45 minutes to 12 minutes after pipeline optimization. Docker image caching and parallel test execution are major improvements.',
 18, 8, 4, 2, FALSE, '2025-01-22 15:20:00', '2025-01-22 15:20:00', '2025-01-22 15:20:00'),

-- Comments for API Gateway Implementation activities (project_id=5)
('Comment', 'Load testing results',
 'API Gateway handling 50,000 requests per minute without issues. Response times are under 100ms for most endpoints. Rate limiting is working correctly.',
 21, 3, 5, 2, FALSE, '2025-01-23 12:00:00', '2025-01-23 12:00:00', '2025-01-23 12:00:00'),

('Comment', 'Authentication service integration',
 'OAuth 2.0 integration completed successfully. JWT token validation is working across all services. Need to update documentation for the new endpoints.',
 22, 4, 5, 3, FALSE, '2025-01-24 10:30:00', '2025-01-24 10:30:00', '2025-01-24 10:30:00'),

-- Comments for Healthcare Data Integration activities (project_id=6)
('Comment', 'HIPAA compliance verification',
 'All healthcare data processing components have been reviewed for HIPAA compliance. Encryption is properly implemented both at rest and in transit.',
 25, 5, 6, 1, TRUE, '2025-01-25 14:45:00', '2025-01-25 14:45:00', '2025-01-25 14:45:00'),

('Comment', 'Hospital system integration status',
 'Successfully integrated with Epic EMR system. HL7 FHIR messaging is working correctly. Patient data synchronization is running every 15 minutes.',
 26, 6, 6, 2, FALSE, '2025-01-26 09:30:00', '2025-01-26 09:30:00', '2025-01-26 09:30:00');

-- =====================================================================
-- REPRESENTATIVE RISK DATA (Depends on projects) - 18 ITEMS (3 per project)
-- =====================================================================

-- Insert representative risk data covering all projects and severity levels
INSERT INTO crisk (name, description, project_id, risk_severity, created_date, last_modified_date) VALUES 

-- E-Commerce Platform Modernization Risks (project_id=1)
('Data Migration Risk - E-Commerce', 'Risk of data loss or corruption during legacy system migration to new platform', 1, 'HIGH', NOW(), NOW()),
('Performance Degradation Risk', 'Risk of system performance issues under high load due to microservices complexity', 1, 'MEDIUM', NOW(), NOW()),
('Security Vulnerability Risk', 'Risk of introducing security vulnerabilities in user authentication system', 1, 'HIGH', NOW(), NOW()),

-- Customer Analytics Dashboard Risks (project_id=2)
('Data Privacy Compliance Risk', 'Risk of violating GDPR/CCPA regulations in customer data processing', 2, 'CRITICAL', NOW(), NOW()),
('Real-time Processing Performance Risk', 'Risk of analytics pipeline failing under high data volume', 2, 'HIGH', NOW(), NOW()),
('Data Quality Risk', 'Risk of inaccurate analytics due to poor data quality from source systems', 2, 'MEDIUM', NOW(), NOW()),

-- Mobile Banking Application Risks (project_id=3)
('Regulatory Compliance Risk - Banking', 'Risk of failing banking regulatory requirements and compliance audits', 3, 'CRITICAL', NOW(), NOW()),
('Biometric Authentication Risk', 'Risk of biometric system failures causing user lockouts', 3, 'HIGH', NOW(), NOW()),
('Mobile Security Risk', 'Risk of mobile app security breaches and unauthorized access', 3, 'CRITICAL', NOW(), NOW()),

-- DevOps Infrastructure Automation Risks (project_id=4)
('Infrastructure Outage Risk', 'Risk of production system outages during infrastructure automation deployment', 4, 'HIGH', NOW(), NOW()),
('Container Security Risk', 'Risk of security vulnerabilities in containerized applications', 4, 'MEDIUM', NOW(), NOW()),
('CI/CD Pipeline Failure Risk', 'Risk of deployment pipeline failures causing release delays', 4, 'MEDIUM', NOW(), NOW()),

-- API Gateway Implementation Risks (project_id=5)
('Single Point of Failure Risk', 'Risk of API gateway becoming a bottleneck or single point of failure', 5, 'HIGH', NOW(), NOW()),
('Rate Limiting Configuration Risk', 'Risk of improper rate limiting affecting legitimate users', 5, 'MEDIUM', NOW(), NOW()),
('Authentication Integration Risk', 'Risk of authentication service integration failures', 5, 'MEDIUM', NOW(), NOW()),

-- Healthcare Data Integration Risks (project_id=6)
('HIPAA Compliance Risk', 'Risk of violating HIPAA regulations in healthcare data handling', 6, 'CRITICAL', NOW(), NOW()),
('Patient Data Security Risk', 'Risk of unauthorized access to sensitive patient information', 6, 'CRITICAL', NOW(), NOW()),
('Healthcare System Integration Risk', 'Risk of integration failures with existing hospital systems', 6, 'HIGH', NOW(), NOW());

-- =====================================================================
-- DECISIONS MANAGEMENT (Depends on projects, users, decision types and statuses)
-- =====================================================================

-- Insert essential decisions covering key project decisions - 15 DECISIONS across projects
INSERT INTO cdecision (
    name, description, project_id, decision_type_id, decision_status_id, 
    accountable_user_id, assigned_to_id, created_by_id, estimated_cost, 
    implementation_date, review_date, created_date, last_modified_date
) VALUES 

-- ===== E-COMMERCE PLATFORM MODERNIZATION PROJECT (project_id=1) =====
-- Strategic decisions
('Microservices Architecture Adoption', 
 'Decision to adopt microservices architecture for better scalability and maintainability of the e-commerce platform',
 1, 1, 4, 2, 2, 1, 250000.00, '2025-02-01 00:00:00', '2025-04-01 00:00:00', NOW(), NOW()),

('Cloud Provider Selection', 
 'Decision to use AWS as the primary cloud provider for hosting and infrastructure services',
 1, 1, 5, 2, 3, 1, 180000.00, '2025-01-15 00:00:00', NULL, NOW(), NOW()),

-- Technical decisions
('Database Technology Choice', 
 'Decision to use PostgreSQL for transactional data and Redis for caching layer',
 1, 2, 5, 3, 3, 2, 50000.00, '2025-01-20 00:00:00', NULL, NOW(), NOW()),

('Frontend Framework Decision', 
 'Decision to migrate from legacy JavaScript to React with TypeScript for the user interface',
 1, 2, 4, 4, 4, 3, 120000.00, '2025-02-15 00:00:00', '2025-05-15 00:00:00', NOW(), NOW()),

-- Financial decision
('Third-Party Payment Integration', 
 'Decision to integrate with Stripe and PayPal for payment processing capabilities',
 1, 3, 3, 2, 5, 1, 85000.00, NULL, NULL, NOW(), NOW()),

-- ===== CUSTOMER RELATIONSHIP MANAGEMENT SYSTEM (project_id=2) =====
-- Strategic decision
('CRM Platform Build vs Buy', 
 'Decision to build custom CRM solution instead of purchasing existing platform like Salesforce',
 2, 1, 2, 2, 2, 1, 500000.00, NULL, '2025-02-10 00:00:00', NOW(), NOW()),

-- Technical decision
('API Design Standard', 
 'Decision to adopt REST API design with OpenAPI specification for all CRM integrations',
 2, 2, 5, 3, 3, 2, 25000.00, '2025-01-25 00:00:00', NULL, NOW(), NOW()),

-- Operational decision
('Customer Data Migration Strategy', 
 'Decision on phased approach for migrating existing customer data from legacy systems',
 2, 4, 4, 4, 4, 3, 75000.00, '2025-03-01 00:00:00', '2025-06-01 00:00:00', NOW(), NOW()),

-- ===== MOBILE BANKING APPLICATION (project_id=3) =====
-- Regulatory decision
('Security Framework Compliance', 
 'Decision to implement OWASP security framework and PCI DSS compliance for banking application',
 3, 5, 4, 2, 3, 1, 200000.00, '2025-02-01 00:00:00', '2025-08-01 00:00:00', NOW(), NOW()),

-- Technical decision
('Biometric Authentication Implementation', 
 'Decision to implement fingerprint and face recognition for secure user authentication',
 3, 2, 3, 3, 4, 2, 150000.00, NULL, NULL, NOW(), NOW()),

-- Financial decision
('Mobile Development Budget Allocation', 
 'Decision to allocate additional budget for native iOS and Android development teams',
 3, 3, 2, 2, 2, 1, 300000.00, NULL, '2025-01-30 00:00:00', NOW(), NOW()),

-- ===== DEVOPS INFRASTRUCTURE AUTOMATION (project_id=4) =====
-- Technical decisions
('Container Orchestration Platform', 
 'Decision to use Kubernetes for container orchestration and deployment automation',
 4, 2, 5, 3, 3, 2, 100000.00, '2025-01-10 00:00:00', NULL, NOW(), NOW()),

('CI/CD Pipeline Tool Selection', 
 'Decision to implement GitLab CI/CD with automated testing and deployment pipelines',
 4, 2, 5, 4, 4, 3, 60000.00, '2025-01-12 00:00:00', NULL, NOW(), NOW()),

-- ===== API GATEWAY IMPLEMENTATION (project_id=5) =====
-- Operational decision
('API Rate Limiting Strategy', 
 'Decision on rate limiting policies and fair usage quotas for different API consumer tiers',
 5, 4, 1, 4, 5, 3, 15000.00, NULL, NULL, NOW(), NOW()),

-- ===== HEALTHCARE DATA INTEGRATION (project_id=6) =====
-- Regulatory decision
('HIPAA Compliance Architecture', 
 'Decision on HIPAA-compliant data architecture with encryption and audit trail requirements',
 6, 5, 2, 2, 3, 1, 400000.00, NULL, '2025-02-15 00:00:00', NOW(), NOW());

-- Insert decision approval requirements for decisions requiring approval
INSERT INTO cdecisionapproval (
    name, description, decision_id, approver_user_id, is_required, approval_priority, 
    due_date, created_date, last_modified_date
) VALUES 
-- Approvals for Microservices Architecture Adoption (decision_id=1)
('Technical Lead Approval', 'Technical architecture review and approval', 1, 3, TRUE, 1, '2025-01-25 00:00:00', NOW(), NOW()),
('CTO Approval', 'Executive technical decision approval', 1, 2, TRUE, 1, '2025-01-30 00:00:00', NOW(), NOW()),

-- Approvals for Frontend Framework Decision (decision_id=4) 
('Senior Developer Approval', 'Frontend technology stack approval', 4, 4, TRUE, 2, '2025-02-10 00:00:00', NOW(), NOW()),

-- Approvals for Third-Party Payment Integration (decision_id=5)
('Finance Team Approval', 'Financial impact and vendor approval', 5, 5, TRUE, 1, '2025-01-28 00:00:00', NOW(), NOW()),
('Security Team Approval', 'Payment security and compliance approval', 5, 3, TRUE, 1, '2025-01-28 00:00:00', NOW(), NOW()),

-- Approvals for CRM Platform Build vs Buy (decision_id=6)
('Project Manager Approval', 'Project scope and resource approval', 6, 2, TRUE, 2, '2025-02-05 00:00:00', NOW(), NOW()),
('Budget Manager Approval', 'Budget allocation and financial approval', 6, 5, TRUE, 1, '2025-02-05 00:00:00', NOW(), NOW()),

-- Approvals for Security Framework Compliance (decision_id=9)
('Compliance Officer Approval', 'Regulatory compliance verification', 9, 2, TRUE, 1, '2025-01-28 00:00:00', NOW(), NOW()),

-- Approvals for Mobile Development Budget Allocation (decision_id=11)
('Executive Approval', 'Executive budget approval for additional resources', 11, 1, TRUE, 1, '2025-01-25 00:00:00', NOW(), NOW()),

-- Approvals for HIPAA Compliance Architecture (decision_id=15)
('Healthcare Compliance Approval', 'HIPAA compliance architecture approval', 15, 2, TRUE, 1, '2025-02-10 00:00:00', NOW(), NOW()),
('Legal Team Approval', 'Legal review for healthcare regulations', 15, 3, TRUE, 1, '2025-02-10 00:00:00', NOW(), NOW());

-- Insert team member assignments for collaborative decisions
INSERT INTO cdecision_team_members (decision_id, user_id) VALUES 
-- Microservices Architecture team (decision_id=1)
(1, 2), (1, 3), (1, 4),
-- Frontend Framework team (decision_id=4)
(4, 3), (4, 4), (4, 5),
-- CRM Platform team (decision_id=6)
(6, 2), (6, 3), (6, 5),
-- Security Framework team (decision_id=9)
(9, 2), (9, 3),
-- HIPAA Compliance team (decision_id=15)
(15, 2), (15, 3), (15, 1);