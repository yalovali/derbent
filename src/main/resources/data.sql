-- Initial data for CLoginUser table
-- This file is executed on application startup to create initial login users
-- Spring Boot will run this file when spring.sql.init.mode=always in application.properties

-- Note: Passwords are BCrypt encoded:
-- 'admin' -> $2a$10$sRqJoY56s0o8OL0Vcs3R8O3kHvUvNBwSxZvm4HHTccMDkneTyDT3O
-- 'user' -> $2a$10$RFCCQbcXxM0Gbj7BbI8jVOimzeD96XS0bUh0KuaQh9xv6hQzsHxpS
-- 'test123' -> $2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu

-- Insert initial login users only if they don't already exist
-- Admin user with full access
INSERT INTO cloginuser (name, lastname, login, email, phone, password, roles, enabled, created_date, updated_date) 
SELECT 'System', 'Administrator', 'admin', 'admin@derbent.tech', '+90-555-000-0001', 
       '$2a$10$sRqJoY56s0o8OL0Vcs3R8O3kHvUvNBwSxZvm4HHTccMDkneTyDT3O', 
       'ADMIN,USER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM cloginuser WHERE login = 'admin');

-- Regular user for testing
INSERT INTO cloginuser (name, lastname, login, email, phone, password, roles, enabled, created_date, updated_date) 
SELECT 'Test', 'User', 'user', 'user@derbent.tech', '+90-555-000-0002', 
       '$2a$10$RFCCQbcXxM0Gbj7BbI8jVOimzeD96XS0bUh0KuaQh9xv6hQzsHxpS', 
       'USER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM cloginuser WHERE login = 'user');

-- Demo user with test123 password
INSERT INTO cloginuser (name, lastname, login, email, phone, password, roles, enabled, created_date, updated_date) 
SELECT 'Demo', 'User', 'demo', 'demo@derbent.tech', '+90-555-000-0003', 
       '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu', 
       'USER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM cloginuser WHERE login = 'demo');