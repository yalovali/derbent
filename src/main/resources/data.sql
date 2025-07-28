-- H2-compatible sample data for testing

-- Delete existing data
DELETE FROM ccommentpriority WHERE 1=1;
DELETE FROM cdecisiontype WHERE 1=1;
DELETE FROM cdecisionstatus WHERE 1=1;

-- Insert essential comment priorities (categorizes comment importance) - 7 ITEMS
INSERT INTO ccommentpriority (name, description, priority_level, color, is_default) VALUES 
('CRITICAL', 'Critical comment requiring immediate escalation', 1, '#D32F2F', FALSE),
('URGENT', 'Urgent comment requiring immediate attention', 2, '#F44336', FALSE),
('HIGH', 'High priority comment needing quick response', 3, '#FF9800', FALSE),
('NORMAL', 'Normal priority comment', 4, '#2196F3', TRUE),
('LOW', 'Low priority informational comment', 5, '#4CAF50', FALSE),
('INFO', 'General information or note', 6, '#9E9E9E', FALSE),
('SUGGESTION', 'Suggestion or improvement idea', 7, '#9C27B0', FALSE);

-- Insert essential decision types (categorizes different types of decisions) - 8 ITEMS
INSERT INTO cdecisiontype (name, description, color, sort_order, requires_approval, is_active) VALUES 
('Strategic', 'High-level strategic decisions affecting project direction and long-term goals', '#FF5722', 1, TRUE, TRUE),
('Technical', 'Technical architecture and implementation decisions requiring engineering review', '#2196F3', 2, TRUE, TRUE),
('Financial', 'Budget and cost-related decisions requiring financial approval', '#4CAF50', 3, TRUE, TRUE),
('Operational', 'Day-to-day operational and process decisions for workflow optimization', '#FF9800', 4, FALSE, TRUE),
('Resource', 'Human resource allocation and team assignment decisions', '#9C27B0', 5, TRUE, TRUE),
('Quality', 'Quality assurance and testing related decisions', '#00BCD4', 6, TRUE, TRUE),
('Security', 'Security and compliance related decisions requiring special approval', '#795548', 7, TRUE, TRUE),
('Administrative', 'Administrative and governance decisions for project management', '#607D8B', 8, FALSE, TRUE);

-- Insert essential decision statuses (workflow states for decisions) - 4 ITEMS
INSERT INTO cdecisionstatus (name, description, color, is_final, sort_order, allows_editing, requires_approval) VALUES 
('PENDING', 'Decision is pending review and discussion', '#9E9E9E', FALSE, 1, TRUE, FALSE),
('UNDER_REVIEW', 'Decision is currently under review by stakeholders', '#2196F3', FALSE, 2, TRUE, TRUE),
('APPROVED', 'Decision has been approved and documented', '#4CAF50', TRUE, 3, FALSE, FALSE),
('REJECTED', 'Decision has been rejected', '#F44336', TRUE, 4, FALSE, FALSE);
