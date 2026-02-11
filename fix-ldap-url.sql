-- Fix LDAP server URL in system settings
-- Update localhost to dc for all system settings records

UPDATE csystem_settings_derbent SET ldap_server_url = 'ldap://dc:389' WHERE ldap_server_url = 'ldap://localhost:389';
UPDATE csystem_settings_bab SET ldap_server_url = 'ldap://dc:389' WHERE ldap_server_url = 'ldap://localhost:389';

-- Show updated records
SELECT id, ldap_server_url FROM csystem_settings_derbent;
SELECT id, ldap_server_url FROM csystem_settings_bab;