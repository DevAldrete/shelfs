#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Shelfs API - Docker Setup${NC}"
echo -e "${GREEN}========================================${NC}"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}Error: Docker is not running. Please start Docker and try again.${NC}"
    exit 1
fi

echo -e "\n${YELLOW}Building Docker images...${NC}"
docker-compose build

if [ $? -ne 0 ]; then
    echo -e "${RED}Error: Docker build failed${NC}"
    exit 1
fi

echo -e "\n${YELLOW}Starting services...${NC}"
docker-compose up -d

if [ $? -ne 0 ]; then
    echo -e "${RED}Error: Failed to start services${NC}"
    exit 1
fi

echo -e "\n${YELLOW}Waiting for services to be healthy...${NC}"
sleep 5

# Wait for PostgreSQL
echo -e "${YELLOW}Checking PostgreSQL...${NC}"
timeout=30
counter=0
until docker-compose exec -T postgres pg_isready -U shelfsuser > /dev/null 2>&1; do
    if [ $counter -eq $timeout ]; then
        echo -e "${RED}Error: PostgreSQL failed to start${NC}"
        docker-compose logs postgres
        exit 1
    fi
    counter=$((counter+1))
    sleep 1
done
echo -e "${GREEN}✓ PostgreSQL is ready${NC}"

# Wait for API
echo -e "${YELLOW}Checking API...${NC}"
timeout=60
counter=0
until curl -f http://localhost:8080/api/health/live > /dev/null 2>&1; do
    if [ $counter -eq $timeout ]; then
        echo -e "${RED}Error: API failed to start${NC}"
        docker-compose logs api
        exit 1
    fi
    counter=$((counter+1))
    sleep 2
done
echo -e "${GREEN}✓ API is ready${NC}"

echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}  Setup Complete!${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "\n${YELLOW}Services:${NC}"
echo -e "  API:          http://localhost:8080"
echo -e "  Health:       http://localhost:8080/api/health"
echo -e "  Swagger UI:   http://localhost:8080/swagger-ui.html"
echo -e "  API Docs:     http://localhost:8080/api-docs"
echo -e "\n${YELLOW}Database:${NC}"
echo -e "  PostgreSQL:   localhost:5432"
echo -e "  Database:     shelfsdb"
echo -e "  Username:     shelfsuser"
echo -e "  Password:     shelfspass"
echo -e "\n${YELLOW}Useful Commands:${NC}"
echo -e "  View logs:    docker-compose logs -f"
echo -e "  Stop:         docker-compose down"
echo -e "  Restart:      docker-compose restart"
echo -e "  Clean:        docker-compose down -v"
echo -e "\n${GREEN}Happy coding! 🚀${NC}\n"
