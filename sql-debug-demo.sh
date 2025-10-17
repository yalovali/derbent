#!/bin/bash

# SQL Debugging Demonstration Script
# This script demonstrates how to enable SQL query debugging in Derbent

set -e

echo "=========================================="
echo "Derbent SQL Query Debugging Demo"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}Available SQL Debugging Options:${NC}"
echo ""
echo "1. Development mode (H2 database with SQL logging)"
echo "2. Production debugging (PostgreSQL with SQL logging)"
echo "3. One-time SQL logging (current profile)"
echo ""

read -p "Select an option (1-3): " choice

case $choice in
    1)
        echo ""
        echo -e "${GREEN}Starting Derbent with H2 profile...${NC}"
        echo -e "${YELLOW}This will show all SQL queries with parameters${NC}"
        echo ""
        echo "Command: mvn spring-boot:run -Dspring.profiles.active=h2"
        echo ""
        echo "Press Ctrl+C to stop the application"
        echo ""
        sleep 2
        mvn spring-boot:run -Dspring.profiles.active=h2
        ;;
    2)
        echo ""
        echo -e "${GREEN}Starting Derbent with SQL debug profile...${NC}"
        echo -e "${YELLOW}This will show all SQL queries with parameters using PostgreSQL${NC}"
        echo ""
        echo "Command: mvn spring-boot:run -Dspring.profiles.active=sql-debug"
        echo ""
        echo "Note: Make sure PostgreSQL is running at localhost:5432"
        echo "Press Ctrl+C to stop the application"
        echo ""
        sleep 2
        mvn spring-boot:run -Dspring.profiles.active=sql-debug
        ;;
    3)
        echo ""
        echo -e "${GREEN}Starting Derbent with one-time SQL logging...${NC}"
        echo -e "${YELLOW}This will enable SQL logging for this run only${NC}"
        echo ""
        echo "Command: mvn spring-boot:run -Dlogging.level.org.hibernate.SQL=DEBUG"
        echo ""
        echo "Press Ctrl+C to stop the application"
        echo ""
        sleep 2
        mvn spring-boot:run \
            -Dlogging.level.org.hibernate.SQL=DEBUG \
            -Dlogging.level.org.hibernate.orm.jdbc.bind=TRACE \
            -Dspring.jpa.properties.hibernate.format_sql=true
        ;;
    *)
        echo ""
        echo -e "${YELLOW}Invalid option. Please run the script again and select 1, 2, or 3.${NC}"
        exit 1
        ;;
esac
