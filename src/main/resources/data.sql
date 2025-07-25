-- Sample data initialization for Derbent application
-- This script assumes tables are created by Hibernate with the classname_id convention:
-- cusertype table with cusertype_id as primary key
-- cactivitytype table with cactivitytype_id as primary key  
-- cuser table with user_id as primary key and cusertype_id as foreign key
-- cproject table with project_id as primary key

-- Insert sample user types
INSERT INTO cusertype (name, description) VALUES 
('Developer', 'Software developers and engineers'),
('Manager', 'Project and team managers'),
('Designer', 'UI/UX and graphic designers'),
('Tester', 'Quality assurance and testing personnel'),
('Analyst', 'Business and system analysts');

-- Insert sample activity types
INSERT INTO cactivitytype (name, description) VALUES 
('Development', 'Software development tasks'),
('Testing', 'Quality assurance and testing activities'),
('Design', 'UI/UX and graphic design work'),
('Documentation', 'Technical and user documentation'),
('Meeting', 'Team meetings and discussions'),
('Research', 'Research and analysis activities');

-- Insert sample activity statuses
INSERT INTO cactivitystatus (name, description, color, is_final, sort_order) VALUES 
('TODO', 'Task is ready to be worked on', '#808080', FALSE, 1),
('IN_PROGRESS', 'Task is currently being worked on', '#007ACC', FALSE, 2),
('REVIEW', 'Task is under review', '#FFA500', FALSE, 3),
('BLOCKED', 'Task is blocked and cannot proceed', '#FF4444', FALSE, 4),
('DONE', 'Task has been completed', '#00AA00', TRUE, 5),
('CANCELLED', 'Task has been cancelled', '#888888', TRUE, 6);

-- Insert sample activity priorities
INSERT INTO cactivitypriority (name, description, priority_level, color, is_default) VALUES 
('CRITICAL', 'Critical priority - immediate attention required', 1, '#FF0000', FALSE),
('HIGH', 'High priority - important task', 2, '#FF8800', FALSE),
('MEDIUM', 'Medium priority - normal task', 3, '#FFA500', TRUE),
('LOW', 'Low priority - can be deferred', 4, '#00AA00', FALSE),
('LOWEST', 'Lowest priority - nice to have', 5, '#808080', FALSE);

-- check application.java to see WHEN this file is run
-- Insert a sample login user into cuser table
INSERT INTO cuser (
    created_date,        -- Timestamp when the user was created
    email,               -- User's email address
    enabled,             -- Account enabled status (boolean)
    lastname,            -- User's last name
    login,               -- Username for login
    name,                -- User's first name
    password,            -- BCrypt encoded password hash
    phone,               -- User's phone number
    roles,               -- User roles (e.g., 'USER')
    updated_date,        -- Timestamp when the user was last updated
    cusertype_id         -- Reference to user type (updated to match new classname_id convention)
) VALUES (
    '2025-07-18 15:58:12.244818',
    'test@example.com',
    TRUE,
    'Lova',
    'user',
    'user',
    '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu',
    '1234567890',
    'USER',
    '2025-07-18 15:58:12.244818',
    1  -- Developer type
);
	
INSERT INTO cuser (
    created_date,        -- Timestamp when the user was created
    email,               -- User's email address
    enabled,             -- Account enabled status (boolean)
    lastname,            -- User's last name
    login,               -- Username for login
    name,                -- User's first name
    password,            -- BCrypt encoded password hash
    phone,               -- User's phone number
    roles,               -- User roles (e.g., 'USER')
    updated_date,        -- Timestamp when the user was last updated
    cusertype_id         -- Reference to user type (updated to match new classname_id convention)
) VALUES (
    '2025-07-18 15:58:12.244818',
    'test@example.com',
    TRUE,
    'Lova2',
    'user2',
    'user2',
    '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu',
    '1234567890',
    'USER2',
    '2025-07-18 15:58:12.244818',
    2  -- Manager type
);

-- Insert sample projects
INSERT INTO cproject (name) VALUES 
('Derbent Project'),
('Website Redesign'),
('Mobile App Development');

-- Insert sample meeting types
INSERT INTO cmeetingtype (name, description) VALUES 
('Stand-up', 'Daily stand-up meetings'),
('Planning', 'Sprint planning and project planning meetings'),
('Retrospective', 'Sprint retrospective and review meetings'),
('Demo', 'Product demo and showcase meetings'),
('Review', 'Code review and design review meetings'),
('All-hands', 'Company-wide or team all-hands meetings');

-- Insert sample meetings
INSERT INTO cmeeting (name, description, meeting_date, end_date, project_id, cmeetingtype_id) VALUES 
('Daily Standup', 'Daily team synchronization meeting', '2025-07-23 09:00:00', '2025-07-23 09:30:00', 1, 1),
('Sprint Planning', 'Planning for the upcoming sprint', '2025-07-24 10:00:00', '2025-07-24 12:00:00', 1, 2),
('Sprint Demo', 'Demonstration of completed features', '2025-07-25 14:00:00', '2025-07-25 15:30:00', 1, 4),
('Website Planning', 'Initial planning for website redesign', '2025-07-26 11:00:00', '2025-07-26 13:00:00', 2, 2),
('Mobile App Review', 'Review of mobile app progress', '2025-07-27 15:00:00', '2025-07-27 16:00:00', 3, 5);

-- Insert sample meeting participants (many-to-many relationship)
INSERT INTO cmeeting_participants (meeting_id, user_id) VALUES 
-- Daily Standup (meeting_id = 1) - both users
(1, 1),
(1, 2),
-- Sprint Planning (meeting_id = 2) - both users
(2, 1),
(2, 2),
-- Sprint Demo (meeting_id = 3) - user 1 only
(3, 1),
-- Website Planning (meeting_id = 4) - user 2 only
(4, 2),
-- Mobile App Review (meeting_id = 5) - both users
(5, 1),
(5, 2);

-- Insert sample companies
-- ccompany table with company_id as primary key
INSERT INTO ccompany (
    name,                -- Company name (required, unique)
    description,         -- Company description (optional)
    address,            -- Company address (optional)
    phone,              -- Company phone number (optional)
    email,              -- Company email address (optional)
    website,            -- Company website URL (optional)
    tax_number,         -- Company tax identification number (optional)
    enabled             -- Company active status (boolean, default true)
) VALUES 
('Tech Solutions Inc.', 'Leading software development company specializing in enterprise solutions', '123 Innovation Drive, Tech Valley, CA 94000', '+1-555-0100', 'contact@techsolutions.com', 'https://www.techsolutions.com', 'TAX-2025-001', TRUE),
('Digital Dynamics LLC', 'Creative digital agency focusing on web and mobile applications', '456 Creative Boulevard, Design City, NY 10001', '+1-555-0200', 'info@digitaldynamics.com', 'https://www.digitaldynamics.com', 'TAX-2025-002', TRUE),
('Innovation Labs Ltd.', 'Research and development company for emerging technologies', '789 Research Park, Innovation Hub, TX 75001', '+1-555-0300', 'contact@innovationlabs.com', 'https://www.innovationlabs.com', 'TAX-2025-003', TRUE),
('Global Systems Corp.', 'International consulting firm for business process optimization', '321 Business Center, Corporate Plaza, FL 33101', '+1-555-0400', 'contact@globalsystems.com', 'https://www.globalsystems.com', 'TAX-2025-004', TRUE),
('Startup Accelerator Inc.', 'Venture capital and startup incubation company', '654 Startup Street, Entrepreneur District, WA 98101', '+1-555-0500', 'contact@startupaccelerator.com', 'https://www.startupaccelerator.com', 'TAX-2025-005', FALSE);

-- Insert sample enhanced activities with comprehensive management features
INSERT INTO cactivity (
    name,                    -- Activity name (required)
    description,            -- Detailed description
    project_id,             -- Reference to project
    cactivitytype_id,       -- Reference to activity type
    assigned_to_id,         -- Reference to assigned user
    created_by_id,          -- Reference to creator user
    cactivitystatus_id,     -- Reference to status
    cactivitypriority_id,   -- Reference to priority
    estimated_hours,        -- Estimated time in hours
    actual_hours,           -- Actual time spent
    remaining_hours,        -- Remaining estimated time
    estimated_cost,         -- Estimated cost
    actual_cost,            -- Actual cost spent
    hourly_rate,            -- Hourly rate for cost calculation
    start_date,             -- Planned/actual start date
    due_date,               -- Expected completion date
    completion_date,        -- Actual completion date (null if not completed)
    progress_percentage,    -- Completion percentage (0-100)
    acceptance_criteria,    -- Criteria for completion
    notes,                  -- Additional notes
    created_date,           -- Creation timestamp
    last_modified_date      -- Last modification timestamp
) VALUES 
-- Activity 1: User Authentication System
('User Authentication System', 
 'Implement secure user login and registration functionality with JWT tokens and role-based access control', 
 1, 1, 1, 1, 2, 2, 
 40.00, 25.50, 14.50, 
 2400.00, 1530.00, 60.00,
 '2025-07-15', '2025-07-30', NULL, 
 65, 
 'Users can register, login, logout, and access features based on their roles. Password reset functionality must work.',
 'Using Spring Security with JWT. Database schema created. Frontend integration pending.',
 '2025-07-15 09:00:00', '2025-07-23 14:30:00'),

-- Activity 2: Database Schema Design
('Database Schema Design',
 'Design and implement the complete database schema for project management features',
 1, 1, 2, 1, 5, 1,
 16.00, 16.00, 0.00,
 960.00, 960.00, 60.00,
 '2025-07-10', '2025-07-20', '2025-07-20',
 100,
 'All entities properly designed with relationships, constraints, and indexes. Migration scripts ready.',
 'PostgreSQL schema completed. All foreign keys and constraints added. Performance tested.',
 '2025-07-10 10:00:00', '2025-07-20 16:00:00'),

-- Activity 3: UI/UX Design for Dashboard
('UI/UX Design for Dashboard',
 'Create wireframes and mockups for the main dashboard with activity overview and project status',
 2, 3, 2, 1, 3, 3,
 24.00, 18.00, 6.00,
 1440.00, 1080.00, 60.00,
 '2025-07-18', '2025-08-05', NULL,
 75,
 'Dashboard mockups approved by stakeholders. Responsive design for desktop and tablet.',
 'Wireframes completed. Working on high-fidelity mockups. Need feedback on color scheme.',
 '2025-07-18 08:30:00', '2025-07-24 11:15:00'),

-- Activity 4: Activity Management API
('Activity Management API',
 'Develop REST API endpoints for creating, updating, deleting, and querying activities',
 1, 1, 1, 1, 1, 2,
 32.00, 0.00, 32.00,
 1920.00, 0.00, 60.00,
 '2025-07-25', '2025-08-10', NULL,
 0,
 'All CRUD operations working. Proper error handling and validation. API documentation complete.',
 'Scheduled to start after authentication system is completed. Dependencies mapped.',
 '2025-07-23 10:00:00', '2025-07-23 10:00:00'),

-- Activity 5: Testing Framework Setup
('Testing Framework Setup',
 'Set up comprehensive testing framework with unit tests, integration tests, and test data',
 1, 2, 1, 2, 2, 4,
 20.00, 12.00, 8.00,
 1200.00, 720.00, 60.00,
 '2025-07-20', '2025-08-01', NULL,
 60,
 'JUnit 5, Mockito, and TestContainers configured. Test coverage above 80%. CI/CD pipeline includes tests.',
 'Basic framework setup. Need to add more integration tests and improve coverage.',
 '2025-07-20 14:00:00', '2025-07-24 09:45:00'),

-- Activity 6: Mobile App Research
('Mobile App Research',
 'Research and evaluate technologies for future mobile application development',
 3, 6, 2, 1, 4, 5,
 8.00, 3.00, 5.00,
 480.00, 180.00, 60.00,
 '2025-07-22', '2025-07-29', NULL,
 0,
 'Technology stack selected. Development approach documented. Timeline estimated.',
 'Currently blocked pending budget approval for mobile development phase.',
 '2025-07-22 15:30:00', '2025-07-24 13:20:00'),

-- Activity 7: Project Documentation
('Project Documentation',
 'Create comprehensive technical and user documentation for the project management system',
 1, 4, 2, 1, 1, 3,
 28.00, 0.00, 28.00,
 1680.00, 0.00, 60.00,
 '2025-08-01', '2025-08-15', NULL,
 0,
 'Technical documentation complete. User guides written. API documentation published.',
 'Will start after main features are implemented. Template structure prepared.',
 '2025-07-23 16:00:00', '2025-07-23 16:00:00'),

-- Activity 8: Performance Optimization
('Performance Optimization',
 'Optimize database queries, implement caching, and improve overall system performance',
 1, 1, 1, 1, 6, 1,
 16.00, 16.00, 0.00,
 960.00, 960.00, 60.00,
 '2025-07-12', '2025-07-18', '2025-07-18',
 100,
 'System response time under 2 seconds. Database queries optimized. Caching implemented.',
 'Cancelled due to premature optimization. Will revisit after initial deployment.',
 '2025-07-12 11:00:00', '2025-07-18 17:30:00');