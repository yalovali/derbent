-- Insert a sample login user into cloginuser table
INSERT INTO cloginuser (
    created_date,        -- Timestamp when the user was created
    email,               -- User's email address
    enabled,             -- Account enabled status (boolean)
    lastname,            -- User's last name
    login,               -- Username for login
    name,                -- User's first name
    password,            -- BCrypt encoded password hash
    phone,               -- User's phone number
    roles,               -- User roles (e.g., 'USER')
    updated_date         -- Timestamp when the user was last updated
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
    '2025-07-18 15:58:12.244818'
);