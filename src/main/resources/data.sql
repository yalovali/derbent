-- =====================================================================
-- MINIMAL SAMPLE DATA INITIALIZATION FOR DERBENT APPLICATION
-- =====================================================================
-- This script initializes the database with minimal sample data 
-- containing only 2 examples per entity type while enriching all items
-- with relations, types, status, colors, and comments.
-- 
-- COMPLIANCE WITH REQUIREMENTS:
-- ✓ Maximum 2 examples per entity type (reduced from 4+)
-- ✓ All entities enriched with relations, types, status, colors, comments
-- ✓ Proper relational integrity maintained
-- ✓ Rich data relationships preserved with minimal examples
-- ✓ All lookup tables included with essential examples
-- =====================================================================

-- =====================================================================
-- TABLE CLEANUP - DELETE ALL EXISTING DATA AND CONSTRAINTS
-- =====================================================================

-- Disable foreign key checks temporarily for PostgreSQL
-- SET session_replication_role = replica;

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

-- Re-enable foreign key checks for PostgreSQL
-- SET session_replication_role = DEFAULT;

-- Reset sequences to start from 1 (essential sequences only)
DO '
BEGIN
    IF EXISTS (SELECT 1 FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace WHERE c.relkind = ''S'' AND c.relname = ''cuser_user_id_seq'') THEN
        EXECUTE ''SELECT setval(''''cuser_user_id_seq'''', 1, false)'';
    END IF;
END;
';

DO '
BEGIN
    IF EXISTS (SELECT 1 FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace WHERE c.relkind = ''S'' AND c.relname = ''cproject_project_id_seq'') THEN
        EXECUTE ''SELECT setval(''''cproject_project_id_seq'''', 1, false)'';
    END IF;
END;
';