-- Sample data initialization for Derbent application
-- This script initializes the database with comprehensive sample data
-- for project management, activity tracking, and resource management.
-- 
-- Table creation order follows foreign key dependencies:
-- 1. Basic lookup tables (no dependencies)
-- 2. User and company tables  
-- 3. Project and meeting tables
-- 4. Activity tables with relationships

-- =====================================================================
-- BASIC LOOKUP TABLES (No foreign key dependencies)
-- =====================================================================

-- Insert sample user types (foundation for user classification)
INSERT INTO cusertype (name, description) VALUES 
('Administrator', 'System administrators with full access'),
('Project Manager', 'Project managers and team leads'),
('Senior Developer', 'Senior software developers and architects'),
('Developer', 'Software developers and engineers'),
('QA Engineer', 'Quality assurance and testing specialists'),
('UI/UX Designer', 'User interface and experience designers'),
('Business Analyst', 'Business and system analysts'),
('DevOps Engineer', 'DevOps and infrastructure specialists');

-- Insert sample activity types (categorizes different types of work)
INSERT INTO cactivitytype (name, description) VALUES 
('Epic', 'Large feature or business initiative spanning multiple sprints'),
('User Story', 'Feature from end-user perspective with acceptance criteria'),
('Task', 'General development or operational task'),
('Bug', 'Software defect that needs to be fixed'),
('Research', 'Research and investigation activities'),
('Documentation', 'Technical and user documentation work'),
('Meeting', 'Team meetings, planning sessions, and discussions'),
('Testing', 'Quality assurance and testing activities'),
('Deployment', 'Software deployment and release activities'),
('Maintenance', 'System maintenance and support activities');


-- Insert sample activity statuses (workflow states)
INSERT INTO cactivitystatus (name, description, color, is_final, sort_order) VALUES 
('BACKLOG', 'Items waiting to be prioritized and planned', '#9E9E9E', FALSE, 1),
('TODO', 'Ready to start - all prerequisites met', '#2196F3', FALSE, 2),
('IN_PROGRESS', 'Currently being worked on', '#FF9800', FALSE, 3),
('CODE_REVIEW', 'Code completed, awaiting review', '#9C27B0', FALSE, 4),
('TESTING', 'Under quality assurance testing', '#3F51B5', FALSE, 5),
('BLOCKED', 'Cannot proceed due to external dependencies', '#F44336', FALSE, 6),
('DONE', 'Successfully completed and delivered', '#4CAF50', TRUE, 7),
('CANCELLED', 'Work cancelled or deemed unnecessary', '#607D8B', TRUE, 8),
('REJECTED', 'Did not meet acceptance criteria', '#795548', TRUE, 9);

-- Insert sample activity priorities (business importance levels)
INSERT INTO cactivitypriority (name, description, priority_level, color, is_default) VALUES 
('BLOCKER', 'Critical blocker - stops all work', 1, '#B71C1C', FALSE),
('CRITICAL', 'Critical priority - immediate attention required', 2, '#F44336', FALSE),
('HIGH', 'High priority - important for current sprint', 3, '#FF9800', FALSE),
('MEDIUM', 'Medium priority - normal task', 4, '#2196F3', TRUE),
('LOW', 'Low priority - can be deferred to next sprint', 5, '#4CAF50', FALSE),
('TRIVIAL', 'Minor improvement - nice to have', 6, '#9E9E9E', FALSE);

-- =====================================================================
-- COMPANIES (Independent entities)
-- =====================================================================

-- Insert sample companies (client organizations and partners)
INSERT INTO ccompany (
    name, description, address, phone, email, website, tax_number, enabled
) VALUES 
('TechCorp Solutions Ltd.', 'Leading enterprise software development company specializing in cloud-native applications and microservices architecture', '456 Innovation Plaza, Silicon Valley, CA 94087', '+1-555-0101', 'info@techcorp-solutions.com', 'https://www.techcorp-solutions.com', 'US-TAX-2025-001', TRUE),
('Digital Innovations Inc.', 'Cutting-edge digital transformation consultancy focusing on AI, blockchain, and IoT solutions', '789 Future Tech Center, Austin, TX 78701', '+1-555-0202', 'contact@digital-innovations.com', 'https://www.digital-innovations.com', 'US-TAX-2025-002', TRUE),
('CloudFirst Systems', 'Cloud infrastructure and DevOps automation specialists providing scalable solutions', '321 Cloud Street, Seattle, WA 98101', '+1-555-0303', 'hello@cloudfirst-systems.com', 'https://www.cloudfirst-systems.com', 'US-TAX-2025-003', TRUE),
('AgileWorks Consulting', 'Agile transformation and project management consultancy with certified Scrum Masters', '654 Agile Avenue, Denver, CO 80202', '+1-555-0404', 'team@agileworks-consulting.com', 'https://www.agileworks-consulting.com', 'US-TAX-2025-004', TRUE),
('StartupHub Accelerator', 'Early-stage startup incubator and venture capital fund', '987 Startup Boulevard, New York, NY 10001', '+1-555-0505', 'ventures@startuphub-accelerator.com', 'https://www.startuphub-accelerator.com', 'US-TAX-2025-005', FALSE);

-- =====================================================================
-- USERS (Depends on cusertype)
-- =====================================================================
-- Insert sample users with diverse roles and realistic data
INSERT INTO cuser (
    created_date, email, enabled, lastname, login, name, password, phone, roles, updated_date, cusertype_id,user_role
) VALUES 
-- System Administrator
('2025-01-15 08:00:00', 'admin@derbent.tech', TRUE, 'Administrator', 'admin', 'System', 
 '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu', '+1-555-0001', 'ADMIN,USER', '2025-01-15 08:00:00', 1,'TEAM_MEMBER'),

-- Project Managers
('2025-01-15 09:00:00', 'sarah.johnson@derbent.tech', TRUE, 'Johnson', 'sarah.johnson', 'Sarah', 
 '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu', '+1-555-1001', 'MANAGER,USER', '2025-01-15 09:00:00', 2,'TEAM_MEMBER'),
('2025-01-15 09:15:00', 'michael.chen@derbent.tech', TRUE, 'Chen', 'michael.chen', 'Michael', 
 '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu', '+1-555-1002', 'MANAGER,USER', '2025-01-15 09:15:00', 2,'TEAM_MEMBER'),

-- Senior Developers
('2025-01-15 10:00:00', 'alex.rodriguez@derbent.tech', TRUE, 'Rodriguez', 'alex.rodriguez', 'Alex', 
 '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu', '+1-555-2001', 'SENIOR_DEV,USER', '2025-01-15 10:00:00', 3,'TEAM_MEMBER'),
('2025-01-15 10:15:00', 'emma.wilson@derbent.tech', TRUE, 'Wilson', 'emma.wilson', 'Emma', 
 '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu', '+1-555-2002', 'SENIOR_DEV,USER', '2025-01-15 10:15:00', 3,'TEAM_MEMBER'),

-- Developers
('2025-01-15 11:00:00', 'david.kim@derbent.tech', TRUE, 'Kim', 'david.kim', 'David', 
 '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu', '+1-555-3001', 'DEVELOPER,USER', '2025-01-15 11:00:00', 4,'TEAM_MEMBER'),
('2025-01-15 11:15:00', 'lisa.patel@derbent.tech', TRUE, 'Patel', 'lisa.patel', 'Lisa', 
 '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu', '+1-555-3002', 'DEVELOPER,USER', '2025-01-15 11:15:00', 4,'TEAM_MEMBER'),

-- QA Engineers
('2025-01-15 12:00:00', 'james.taylor@derbent.tech', TRUE, 'Taylor', 'james.taylor', 'James', 
 '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu', '+1-555-4001', 'QA,USER', '2025-01-15 12:00:00', 5,'TEAM_MEMBER'),
('2025-01-15 12:15:00', 'maria.garcia@derbent.tech', TRUE, 'Garcia', 'maria.garcia', 'Maria', 
 '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu', '+1-555-4002', 'QA,USER', '2025-01-15 12:15:00', 5,'TEAM_MEMBER'),

-- UI/UX Designers
('2025-01-15 13:00:00', 'sophia.brown@derbent.tech', TRUE, 'Brown', 'sophia.brown', 'Sophia', 
 '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu', '+1-555-5001', 'DESIGNER,USER', '2025-01-15 13:00:00', 6,'TEAM_MEMBER'),

-- Business Analysts
('2025-01-15 14:00:00', 'robert.anderson@derbent.tech', TRUE, 'Anderson', 'robert.anderson', 'Robert', 
 '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu', '+1-555-6001', 'ANALYST,USER', '2025-01-15 14:00:00', 7,'TEAM_MEMBER'),

-- DevOps Engineers  
('2025-01-15 15:00:00', 'jennifer.lee@derbent.tech', TRUE, 'Lee', 'jennifer.lee', 'Jennifer', 
 '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu', '+1-555-7001', 'DEVOPS,USER', '2025-01-15 15:00:00', 8,'TEAM_MEMBER');

-- =====================================================================
-- PROJECTS (Depends on users for assignment)
-- =====================================================================

-- Insert sample projects with realistic business scenarios
INSERT INTO cproject (name, description, created_date, last_modified_date) VALUES 
('E-Commerce Platform Modernization', 'Migration of legacy e-commerce system to microservices architecture with React frontend and Spring Boot backend', NOW(), NOW()),
('Customer Analytics Dashboard', 'Real-time analytics dashboard for customer behavior tracking using machine learning and data visualization', NOW(), NOW()),
('Mobile Banking Application', 'Secure mobile banking app with biometric authentication, transaction management, and investment features', NOW(), NOW()),
('DevOps Infrastructure Automation', 'Automated CI/CD pipeline setup, containerization, and cloud infrastructure management', NOW(), NOW()),
('API Gateway Implementation', 'Centralized API gateway with rate limiting, authentication, and monitoring capabilities', NOW(), NOW());

-- =====================================================================
-- MEETING TYPES AND MEETINGS (Depends on projects and users)
-- =====================================================================

-- Insert meeting types for different collaboration scenarios

INSERT INTO cmeetingtype (name, description) VALUES 
('Daily Standup', 'Short daily synchronization meetings for team coordination'),
('Sprint Planning', 'Sprint planning sessions for work estimation and commitment'),
('Sprint Review', 'Sprint review and demonstration meetings with stakeholders'),
('Retrospective', 'Team retrospectives for continuous improvement'),
('Architecture Review', 'Technical architecture and design review sessions'),
('Stakeholder Demo', 'Product demonstrations for business stakeholders'),
('Technical Deep Dive', 'In-depth technical discussions and knowledge sharing'),
('Project Kickoff', 'Project initiation meetings with goal setting'),
('Risk Assessment', 'Risk identification and mitigation planning sessions'),
('Client Consultation', 'Client meetings for requirements and feedback');

-- Insert sample meetings with realistic scheduling
INSERT INTO cmeeting (name, description, meeting_date, end_date, project_id, cmeetingtype_id) VALUES 
-- E-Commerce Platform project meetings
('E-Commerce Sprint Planning #1', 'Planning session for first sprint focusing on user authentication and product catalog', '2025-01-20 09:00:00', '2025-01-20 11:00:00', 1, 2),
('Daily Standup - E-Commerce Team', 'Daily team sync for E-Commerce platform development', '2025-01-21 09:00:00', '2025-01-21 09:15:00', 1, 1),
('Architecture Review - Microservices Design', 'Review of microservices architecture for E-Commerce platform', '2025-01-22 14:00:00', '2025-01-22 16:00:00', 1, 5),
('E-Commerce Sprint Review #1', 'Demo of completed authentication and catalog features', '2025-02-03 15:00:00', '2025-02-03 16:30:00', 1, 3),

-- Customer Analytics project meetings  
('Analytics Project Kickoff', 'Initial planning and goal setting for customer analytics dashboard', '2025-01-18 10:00:00', '2025-01-18 12:00:00', 2, 8),
('Data Architecture Deep Dive', 'Technical discussion on data pipeline and analytics architecture', '2025-01-25 13:00:00', '2025-01-25 15:00:00', 2, 7),
('Stakeholder Demo - Analytics Prototype', 'Demonstration of analytics dashboard prototype to business users', '2025-02-01 11:00:00', '2025-02-01 12:00:00', 2, 6),

-- Mobile Banking project meetings
('Mobile Banking Risk Assessment', 'Security and compliance risk review for mobile banking features', '2025-01-19 14:00:00', '2025-01-19 16:00:00', 3, 9),
('Client Consultation - Banking Features', 'Requirements gathering session with banking client', '2025-01-24 10:00:00', '2025-01-24 11:30:00', 3, 10),

-- DevOps Infrastructure meetings
('DevOps Strategy Session', 'Planning for infrastructure automation and CI/CD implementation', '2025-01-23 13:00:00', '2025-01-23 15:00:00', 4, 8),
('Infrastructure Review', 'Review of current infrastructure and automation opportunities', '2025-01-26 09:00:00', '2025-01-26 10:30:00', 4, 5);

-- Insert meeting participants (many-to-many relationships)
INSERT INTO cmeeting_participants (meeting_id, user_id) VALUES 
-- E-Commerce Sprint Planning (Sarah PM, Alex Senior Dev, David Dev, Lisa Dev, James QA)
(1, 2), (1, 4), (1, 6), (1, 7), (1, 8),
-- Daily Standup (Same team)  
(2, 2), (2, 4), (2, 6), (2, 7), (2, 8),
-- Architecture Review (Michael PM, Alex Senior Dev, Emma Senior Dev, Jennifer DevOps)
(3, 3), (3, 4), (3, 5), (3, 12),
-- Sprint Review (Sarah PM, Alex Senior Dev, David Dev, Lisa Dev, Sophia Designer)
(4, 2), (4, 4), (4, 6), (4, 7), (4, 10),
-- Analytics Kickoff (Michael PM, Emma Senior Dev, Robert Analyst)
(5, 3), (5, 5), (5, 11),
-- Data Architecture (Emma Senior Dev, Jennifer DevOps, Robert Analyst)
(6, 5), (6, 12), (6, 11),
-- Analytics Demo (Michael PM, Emma Senior Dev, Robert Analyst, Admin)
(7, 3), (7, 5), (7, 11), (7, 1),
-- Risk Assessment (Sarah PM, Alex Senior Dev, James QA)
(8, 2), (8, 4), (8, 8),
-- Client Consultation (Michael PM, Robert Analyst, Sophia Designer)
(9, 3), (9, 11), (9, 10),
-- DevOps Strategy (Jennifer DevOps, Alex Senior Dev, Michael PM)
(10, 12), (10, 4), (10, 3),
-- Infrastructure Review (Jennifer DevOps, Emma Senior Dev)
(11, 12), (11, 5);

-- =====================================================================
-- COMPREHENSIVE ACTIVITY DATA (Depends on all above entities)
-- =====================================================================

-- Insert comprehensive sample activities covering various project types and scenarios
INSERT INTO cactivity (
    name, description, project_id, cactivitytype_id, assigned_to_id, created_by_id, 
    cactivitystatus_id, cactivitypriority_id, estimated_hours, actual_hours, remaining_hours,
    estimated_cost, actual_cost, hourly_rate, start_date, due_date, completion_date,
    progress_percentage, acceptance_criteria, notes, created_date, last_modified_date
) VALUES 

-- ===== E-COMMERCE PLATFORM MODERNIZATION PROJECT =====

-- EPIC: User Management System
('User Management System Epic', 
 'Complete user authentication, authorization, and profile management system with social login integration',
 1, 1, 4, 2, 3, 3, 120.00, 45.00, 75.00, 7200.00, 2700.00, 60.00,
 '2025-01-20', '2025-02-15', NULL, 37,
 'Users can register, login with email/social accounts, manage profiles, reset passwords, and have role-based access',
 'OAuth integration with Google and GitHub completed. Password reset flow in progress.',
 '2025-01-18 09:00:00', '2025-01-25 14:30:00'),

-- User Story: User Registration  
('User Registration with Email Verification',
 'Implement user registration form with email verification and validation',
 1, 2, 6, 2, 7, 4, 16.00, 16.00, 0.00, 960.00, 960.00, 60.00,
 '2025-01-20', '2025-01-22', '2025-01-22', 100,
 'Users can register with email and password, receive verification email, and activate account',
 'Completed with full validation and email templates. Unit tests passing.',
 '2025-01-18 10:00:00', '2025-01-22 16:00:00'),

-- Task: Database Schema for Users
('User Database Schema Design',
 'Design and implement user-related database tables with proper indexes and constraints',
 1, 3, 5, 2, 7, 2, 8.00, 8.00, 0.00, 480.00, 480.00, 60.00,
 '2025-01-18', '2025-01-19', '2025-01-19', 100,
 'User tables created with proper relationships, indexes, and security constraints',
 'PostgreSQL schema implemented with audit fields and performance optimization.',
 '2025-01-18 11:00:00', '2025-01-19 17:00:00'),

-- Bug: Login Session Timeout Issue
('Fix Login Session Timeout Bug',
 'Session expires too quickly causing user frustration during long form submissions',
 1, 4, 4, 2, 3, 2, 4.00, 2.00, 2.00, 240.00, 120.00, 60.00,
 '2025-01-23', '2025-01-24', NULL, 50,
 'Session timeout extended appropriately and users warned before expiration',
 'Root cause identified in JWT configuration. Fix in progress.',
 '2025-01-23 09:00:00', '2025-01-25 11:00:00'),

-- EPIC: Product Catalog System
('Product Catalog System Epic',
 'Complete product management system with categories, search, filtering, and inventory tracking',
 1, 1, 5, 2, 2, 3, 80.00, 15.00, 65.00, 4800.00, 900.00, 60.00,
 '2025-01-25', '2025-02-20', NULL, 18,
 'Products can be added, categorized, searched, filtered, and inventory managed',
 'Started with product model design. Database schema in progress.',
 '2025-01-24 14:00:00', '2025-01-25 16:00:00'),

-- ===== CUSTOMER ANALYTICS DASHBOARD PROJECT =====

-- EPIC: Data Pipeline Architecture  
('Analytics Data Pipeline Epic',
 'Real-time data ingestion and processing pipeline for customer analytics',
 2, 1, 5, 3, 2, 2, 100.00, 25.00, 75.00, 6000.00, 1500.00, 60.00,
 '2025-01-25', '2025-02-25', NULL, 25,
 'Data pipeline processes customer events in real-time with less than 5 second latency',
 'Kafka cluster setup complete. Stream processing components in development.',
 '2025-01-24 10:00:00', '2025-01-26 15:00:00'),

-- Research: Analytics Technology Stack
('Analytics Technology Stack Research',
 'Research and evaluate technologies for real-time analytics: Apache Kafka, ClickHouse, Apache Flink',
 2, 5, 11, 3, 7, 4, 20.00, 20.00, 0.00, 1200.00, 1200.00, 60.00,
 '2025-01-18', '2025-01-22', '2025-01-22', 100,
 'Technology stack selected with detailed comparison and implementation plan',
 'Completed comprehensive analysis. Recommended Kafka + ClickHouse + React Dashboard.',
 '2025-01-18 13:00:00', '2025-01-22 17:00:00'),

-- User Story: Dashboard Visualization
('Interactive Analytics Dashboard',
 'Create interactive dashboard with charts, filters, and real-time updates',
 2, 2, 10, 3, 4, 3, 32.00, 8.00, 24.00, 1920.00, 480.00, 60.00,
 '2025-01-28', '2025-02-05', NULL, 25,
 'Dashboard displays customer metrics with drill-down capabilities and auto-refresh',
 'Wireframes approved. React components under development. Charts library integrated.',
 '2025-01-26 09:00:00', '2025-01-28 14:00:00'),

-- ===== MOBILE BANKING APPLICATION PROJECT =====

-- EPIC: Security and Compliance
('Mobile Banking Security Epic',
 'Implement comprehensive security measures for mobile banking including biometric auth',
 3, 1, 4, 2, 6, 1, 60.00, 0.00, 60.00, 3600.00, 0.00, 60.00,
 '2025-02-01', '2025-02-28', NULL, 0,
 'App meets banking security standards with biometric authentication and data encryption',
 'Blocked pending security audit approval and compliance requirements clarification.',
 '2025-01-28 11:00:00', '2025-01-28 11:00:00'),

-- Task: Security Requirements Analysis
('Banking Security Requirements Analysis',
 'Analyze PCI DSS, PSD2, and local banking regulations for compliance requirements',
 3, 3, 11, 2, 7, 3, 24.00, 24.00, 0.00, 1440.00, 1440.00, 60.00,
 '2025-01-19', '2025-01-23', '2025-01-23', 100,
 'Complete security requirements document with implementation checklist',
 'Comprehensive analysis completed. Compliance checklist created and approved.',
 '2025-01-19 09:00:00', '2025-01-23 16:00:00'),

-- ===== DEVOPS INFRASTRUCTURE AUTOMATION PROJECT =====

-- EPIC: CI/CD Pipeline Implementation
('CI/CD Pipeline Automation Epic',
 'Automated build, test, and deployment pipeline with monitoring and rollback capabilities',
 4, 1, 12, 3, 3, 2, 50.00, 20.00, 30.00, 3000.00, 1200.00, 60.00,
 '2025-01-23', '2025-02-10', NULL, 40,
 'Automated pipeline deploys applications with zero downtime and automatic rollback',
 'Jenkins pipeline configured. Docker containers implemented. Kubernetes deployment in progress.',
 '2025-01-22 14:00:00', '2025-01-26 10:00:00'),

-- Task: Docker Containerization
('Application Containerization',
 'Containerize all microservices with Docker and create container registry',
 4, 3, 12, 3, 7, 4, 16.00, 16.00, 0.00, 960.00, 960.00, 60.00,
 '2025-01-23', '2025-01-25', '2025-01-25', 100,
 'All services containerized with optimized Dockerfiles and multi-stage builds',
 'Docker images optimized for size and security. Registry with vulnerability scanning setup.',
 '2025-01-23 09:00:00', '2025-01-25 17:00:00'),

-- Testing: Load Testing Infrastructure  
('Load Testing Framework Setup',
 'Implement automated load testing with JMeter and performance benchmarking',
 4, 8, 8, 3, 5, 3, 12.00, 6.00, 6.00, 720.00, 360.00, 60.00,
 '2025-01-26', '2025-01-30', NULL, 50,
 'Load tests run automatically with performance regression detection',
 'JMeter scripts created. Integration with CI pipeline 50% complete.',
 '2025-01-25 13:00:00', '2025-01-28 11:00:00'),

-- ===== API GATEWAY IMPLEMENTATION PROJECT =====

-- EPIC: API Gateway Architecture
('API Gateway Implementation Epic',
 'Centralized API gateway with authentication, rate limiting, and monitoring',
 5, 1, 4, 3, 2, 3, 40.00, 8.00, 32.00, 2400.00, 480.00, 60.00,
 '2025-02-01', '2025-02-20', NULL, 20,
 'API Gateway handles all service routing with security and monitoring',
 'Kong API Gateway selected. Basic routing configuration completed.',
 '2025-01-30 10:00:00', '2025-02-01 15:00:00'),

-- Documentation: API Documentation
('API Gateway Documentation',
 'Create comprehensive API documentation with examples and integration guides',
 5, 6, 11, 3, 1, 4, 16.00, 0.00, 16.00, 960.00, 0.00, 60.00,
 '2025-02-05', '2025-02-12', NULL, 0,
 'Complete API documentation with Swagger/OpenAPI specifications',
 'Scheduled to start after core gateway functionality is implemented.',
 '2025-01-30 16:00:00', '2025-01-30 16:00:00'),

-- Meeting: Gateway Architecture Review
('API Gateway Architecture Review Meeting',
 'Technical review of gateway architecture with senior developers and architects',
 5, 7, 4, 3, 7, 4, 2.00, 2.00, 0.00, 120.00, 120.00, 60.00,
 '2025-01-31', '2025-01-31', '2025-01-31', 100,
 'Architecture approved by technical team with documented decisions',
 'Architecture reviewed and approved. Some minor optimization suggestions incorporated.',
 '2025-01-31 14:00:00', '2025-01-31 16:00:00'),

-- ===== CROSS-PROJECT ACTIVITIES =====

-- Deployment: Production Deployment
('Q1 Production Deployment',
 'Coordinated deployment of all Q1 features across multiple projects',
 1, 9, 12, 2, 1, 2, 8.00, 0.00, 8.00, 480.00, 0.00, 60.00,
 '2025-03-01', '2025-03-05', NULL, 0,
 'All Q1 features deployed successfully with zero downtime',
 'Planned for Q1 end. Deployment scripts and rollback procedures being prepared.',
 '2025-01-30 17:00:00', '2025-01-30 17:00:00'),

-- Maintenance: Database Performance Tuning
('Database Performance Optimization',
 'Optimize database queries and implement caching for improved performance',
 1, 10, 5, 1, 8, 1, 12.00, 12.00, 0.00, 720.00, 720.00, 60.00,
 '2025-01-15', '2025-01-18', '2025-01-18', 100,
 'Database response time improved by 50% with query optimization',
 'Cancelled after profiling showed premature optimization. Will revisit if needed.',
 '2025-01-15 10:00:00', '2025-01-18 16:00:00');

